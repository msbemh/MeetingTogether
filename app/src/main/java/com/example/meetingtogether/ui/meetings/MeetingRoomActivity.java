package com.example.meetingtogether.ui.meetings;

import static com.example.meetingtogether.MainActivity.TAG;
import static com.example.meetingtogether.MainActivity.mChatService;
import static com.example.meetingtogether.common.Common.CHAT;
import static com.example.meetingtogether.common.Common.HOST;
import static com.example.meetingtogether.common.Common.IS_ACTIVATE_CAMERA;
import static com.example.meetingtogether.common.Common.PEERS;
import static com.example.meetingtogether.common.Common.ROOMID;
import static com.example.meetingtogether.common.Common.ROOM_NAME;
import static com.example.meetingtogether.common.Common.VIDEO;
import static com.example.meetingtogether.common.Common.WHITE_BOARD;
import static com.example.meetingtogether.sharedPreference.SharedPreferenceRepository.gson;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.VectorDrawable;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.CapturerObserver;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.EglRenderer;
import org.webrtc.GlRectDrawer;
import org.webrtc.GlTextureFrameBuffer;
import org.webrtc.GlUtil;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.RtpReceiver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.TextureBufferImpl;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoFrame;
import org.webrtc.VideoFrameDrawer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.YuvConverter;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static io.socket.client.Socket.EVENT_CONNECT;
import static io.socket.client.Socket.EVENT_DISCONNECT;
import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.meetingtogether.MainActivity;
import com.example.meetingtogether.R;
import com.example.meetingtogether.common.ColorType;
import com.example.meetingtogether.common.Common;
import com.example.meetingtogether.common.CommonRecyclerView;
import com.example.meetingtogether.common.MessageRecyclerView;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.ActivityMeetingRoomBinding;
import com.example.meetingtogether.databinding.FragmentMeetingRoomChatBinding;
import com.example.meetingtogether.databinding.FragmentPeersBinding;
import com.example.meetingtogether.databinding.FragmentWhiteboardBinding;
import com.example.meetingtogether.databinding.MeetingRowItemBinding;
import com.example.meetingtogether.databinding.PlainRowItemBinding;
import com.example.meetingtogether.databinding.ReceiveMessageRowItemBinding;
import com.example.meetingtogether.databinding.SendMessageRowItemBinding;
import com.example.meetingtogether.dialogs.CustomDialog;
import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.model.MessageDTO;
import com.example.meetingtogether.model.ProfileMap;
import com.example.meetingtogether.model.User;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.FileInfo;
import com.example.meetingtogether.retrofit.RetrofitInterface;
import com.example.meetingtogether.retrofit.RetrofitResponse;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.services.MeetingService;
import com.example.meetingtogether.services.TestService;
import com.example.meetingtogether.sharedPreference.SharedPreferenceRepository;
import com.example.meetingtogether.ui.meetings.DTO.ColorModel;
import com.example.meetingtogether.ui.meetings.DTO.MeetingDTO;
import com.example.meetingtogether.ui.meetings.DTO.MessageModel;
import com.example.meetingtogether.ui.meetings.fragments.MeetingListFragment;
import com.example.meetingtogether.ui.meetings.fragments.MeetingRoomChatFragment;
import com.example.meetingtogether.ui.meetings.fragments.MeetingUserListDialogFragment;
import com.example.meetingtogether.ui.meetings.fragments.PeersFragment;
import com.example.meetingtogether.ui.meetings.fragments.WhiteboardFragment;
import com.example.meetingtogether.ui.meetings.DTO.UserModel;
import com.example.meetingtogether.ui.meetings.google.Camera2Capturer;
import com.example.meetingtogether.ui.meetings.google.CameraCaptureInterface;
import com.example.meetingtogether.ui.user.LoginActivity;
import com.example.meetingtogether.ui.users.ProfileDetailActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

public class MeetingRoomActivity extends AppCompatActivity {
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String SCREEN_TRACK_ID = "ARDAMSv0";
    public static final int VIDEO_RESOLUTION_WIDTH = 1440;
    public static final int VIDEO_RESOLUTION_HEIGHT = 1080;
    public static final int FPS = 30;

    private static final String VIDEO_FLEXFEC_FIELDTRIAL =
            "WebRTC-FlexFEC-03-Advertised/Enabled/WebRTC-FlexFEC-03/Enabled/";
    private static final String DISABLE_WEBRTC_AGC_FIELDTRIAL =
            "WebRTC-Audio-MinimizeResamplingOnMobile/Enabled/";
    private ActivityMeetingRoomBinding binding;
    private EglBase rootEglBase;
    private PeerConnectionFactory factory;

    // 소켓
    private Socket socket;
    // 소켓 Id
    private String socketId;



    // 로컬 오디오 트랙
//    private AudioTrack localAudioTrack;
//    private ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
    private List<CustomCapturer> capturerList = new ArrayList<>();
    private List<CustomTrack> trackList = new ArrayList<>();
    public static List<CustomPeerConnection> peerConnections = new ArrayList<>();
    private List<ProxySink> proxySinks = new ArrayList<>();
    private List<CustomQueue> queueList = new ArrayList<>();

    private ViewGroup viewGroup;

    private Handler handler;
    private Handler chatHandler;

    // 미디어 제약사항
    private MediaConstraints sdpMediaConstraints;
    // 오디오 제약사항
    private MediaConstraints audioConstraints;


    // 사용자 리스트
    private List<UserModel> userList = new ArrayList<>();

    // 비디오 On 여부
    boolean isVideoOn = true;
    // 오디오 On 여부
    boolean isAudioOn = true;
    // 화면 공유 on 여부
    boolean isShareOn = false;

    // 미팅 서비스 바운드 여부
    private boolean isMeetingServiceBound = false;
    private boolean isMediaServiceBound = false;

    // 미팅 서비스
    public static MeetingService meetingService;
    public static TestService testService;

    // 미디어 프로젝션 토큰요청 결과 코드
    private int mediaProjectionResultCode;
    // 미디어 프로젝션 토큰요청 결과 데이터
    private Intent mediaProjectionResultData;
    // 미디어 프로젝션 매니저
    private MediaProjectionManager mediaProjectionManager;
    private String shareId;
    private String whiteboardId;
    private String whiteboardStatus;
    private DrawingView drawingView;
    private FaceDetector detector;
    private Boolean isFaceMode = false;

    private Paint paint;

    public float left_eye_x = -1;
    public float left_eye_y = -1;
    public float right_eye_x = -1;
    public float right_eye_y = -1;
    private Bitmap mask1;
    //    private VectorDrawable mask2;
    private int cnt = 0;

    private PeersFragment peersFragment;
    private WhiteboardFragment whiteboardFragment;
    private FragmentPeersBinding peersBinding;
    private FragmentWhiteboardBinding whiteboardBinding;
    public String roomId;
    public String roomName;
    private String host;
    private boolean isActivateCamera = false;
    private boolean isFront = true;

    private MeetingRoomChatFragment meetingRoomChatFragment;
    private FragmentMeetingRoomChatBinding meetingRoomChatBinding;

    /**
     * 마스크1(스파이더맨) 변수
     */
    private float mask1OriginalW = 518;
    private float mask1OriginalH = 518;
    private float mask1OriginalD = 160;
    private float mask1OriginalCX = 259;
    private float mask1OriginalCY = 259;

    private float mask1Ratio = -1;

    private float mask1AdjustedW = -1;
    private float mask1AdjustedH = -1;
    private float mask1AdjustedD = -1;
    private float mask1AdjustedCX = -1;
    private float mask1AdjustedCY = -1;
    private float faceDistance = -1;
    private float faceCX = -1;
    private float faceCY = -1;

    private float mask1FaceRatio = -1;

    private float differenceX = -1;
    private float differenceY = -1;

    private Rect faceRect;
    private String meetingMode = PEERS;
    private List<MessageModel> chatList = new ArrayList<>();
    private MessageRecyclerView messageRecyclerView;

    private Retrofit retrofit;
    private RetrofitInterface service;
    private MeetingUserListDialogFragment meetingUserListDialogFragment;

    /**
     * service
     * 필요한 권한
     */
    private final String[] MEETING_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE
    };

    /**
     * 미팅 권한 요청에 대한 Callback
     */
    private ActivityResultLauncher meetingPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            Log.d(TAG, "" + result.toString());

            Boolean areAllGranted = true;
            // 모든 권한에 동의 했는지 확인
            Iterator iterator = result.keySet().iterator();
            while (iterator.hasNext()) {
                String permissionName = iterator.next().toString();
                boolean isAllowed = result.get(permissionName);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                    areAllGranted = areAllGranted && isAllowed;
                } else {
                    if (permissionName.equals("android.permission.FOREGROUND_SERVICE")) continue;
                    if (permissionName.equals("android.permission.POST_NOTIFICATIONS")) continue;
                }
            }

            // 모든 권한에 동의
            if (areAllGranted) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                intent.putExtra("type", "default");

                /** 윈도우 오버레이 권한 없으면 요청 */
                if (!Settings.canDrawOverlays(MeetingRoomActivity.this)) {
                    systemPermissionLauncher.launch(intent);
                }else{
                    fragAndSocketSetting();
                }

            // 모든 권한에 동의 하지 않음
            } else {
                showPermissionDialog();
            }
        }
    });

    private void fragAndSocketSetting(){
        // 미팅 서비스 바인드 시작
        Intent intent = new Intent(MeetingRoomActivity.this, TestService.class);
        bindService(intent, mediaServiceConn, Context.BIND_AUTO_CREATE);

        // 프래그먼트들 세팅
        fragmentsSetting();
        // 시그널링 서버와 연결
        connectToSignallingServer();
    }

    private Boolean isOpenAlbum = false;

    private void fragmentsSetting(){
        /**
         * 프레그 먼트 세팅
         */
        peersFragment = PeersFragment.newInstance(new PeersFragment.CreateResultInterface() {
            @Override
            public void onCreated(FragmentPeersBinding peersBinding) {
                MeetingRoomActivity.this.peersBinding = peersBinding;
                viewGroup = (ViewGroup) peersBinding.mainSurfaceView.getParent();

                // 로컬 세팅 시작
                localSetting();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        });

        /** 화이트 보드 프레그먼트 생성 */
        whiteboardFragment = WhiteboardFragment.newInstance(new WhiteboardFragment.CreateResultInterface() {
            @Override
            public void onCreated(FragmentWhiteboardBinding whiteboardBinding, ColorModel colorModel) {
                MeetingRoomActivity.this.whiteboardBinding = whiteboardBinding;
                MeetingRoomActivity.this.initDrawingView(colorModel);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        });



        /** 채팅 프레그먼트 */
        meetingRoomChatFragment = MeetingRoomChatFragment.newInstance(new MeetingRoomChatFragment.CreateResultInterface() {
            @Override
            public void onCreated(FragmentMeetingRoomChatBinding binding) {
                meetingRoomChatBinding = binding;

                // 메시지 보내기
                meetingRoomChatBinding.chatSendbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 메시지 추출
                        String msg = meetingRoomChatBinding.messageText.getText().toString();
                        UserModel userModel = getUserModel(socketId);

                        String date = getCurrentLocalDateTime();
                        // 메시지 추가
                        chatList.add(new MessageModel(MessageModel.MessageType.SEND, msg, null, userModel, date));

                        // 리사이클러뷰 인지시키기
                        int addIdx = messageRecyclerView.getAdapter().getItemCount() - 1;

                        // 에디터 초기화
                        meetingRoomChatBinding.messageText.setText("");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 리사이클러뷰 새롭게 인지
                                messageRecyclerView.getAdapter().notifyItemInserted(addIdx);
                                // 스크롤 제일 아래로
                                messageRecyclerView.getRecyclerView().scrollToPosition(messageRecyclerView.getAdapter().getItemCount() - 1);
                            }
                        });

                        // 연결된 피어 모두애게 그리기 정보를 보낸다.
                        for (int i = 0; i < MeetingRoomActivity.peerConnections.size(); i++) {
                            CustomPeerConnection customPeerConnection = MeetingRoomActivity.peerConnections.get(i);
                            DataChannel dataChannel = customPeerConnection.getDataChannel();
                            String cmd = "chat";
                            String type = "txt";
                            String data = cmd + ";" + type + ";" + msg + ";" + date;
                            ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
                            dataChannel.send(new DataChannel.Buffer(buffer, false));
                        }
                    }
                });

                // 이미지 보내기
                meetingRoomChatBinding.imageAddButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isOpenAlbum = true;
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);  // 1개 이미지를 가져올 수 있도록 세팅
                        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        activityAlbumResultLauncher.launch(intent);
                    }
                });

                // 리사이클러뷰
                RecyclerView meetingRoomChatRecyclerView = meetingRoomChatBinding.meetingRoomChatRecyclerView;

                // 리사이클러뷰 바인드
                messageRecyclerView = new MessageRecyclerView(new MessageRecyclerView.OnBind() {
                    // TODO: ViewBind 변경
                    // ViewBind 연동
                    @Override
                    public void onBindViewListener(MessageRecyclerView.MyRecyclerAdapter.ViewHolder viewHolder, View view, int viewType) {
//                        Log.d(TAG, "viewHolder:" + viewHolder);
//                        Log.d(TAG, "view:" + view);
                        if (viewType == MessageModel.MessageType.RECEIVE.getValue()) {
                            viewHolder.setReceiveBinding(ReceiveMessageRowItemBinding.bind(view));
                        } else {
                            viewHolder.setSendBinding(SendMessageRowItemBinding.bind(view));
                        }
                    }

                    // TODO: ViewBind 변경
                    // 실제 View 와 데이터 연동
                    @Override
                    public void onBindViewHolderListener(MessageRecyclerView.MyRecyclerAdapter.ViewHolder holder, int position) {
                        Log.d(TAG, "[onBindViewHolderListener] 동작");
                        Log.d(TAG, "[position:" + position);

                        MessageModel messageModel = chatList.get(position);

                        Log.d(TAG, "messageModel:" + messageModel);

                        // 받은 메시지
                        if (chatList.get(position).getMessageType().getValue() == MessageModel.MessageType.RECEIVE.getValue()) {
                            ReceiveMessageRowItemBinding binding = (ReceiveMessageRowItemBinding) (holder.receiveBinding);
                            /** 이미지 파일이 존재하면 이미지파일을 보여주고 그렇지 않으면 텍스트를 보여주자. */
                            String filename = messageModel.getFilename();

                            Log.d(TAG, "filename:" + filename);

                            if (filename != null) {
                                binding.msgContent.setVisibility(View.GONE);
                                binding.receiveDate.setVisibility(View.GONE);

                                binding.msgImage.setVisibility(View.VISIBLE);
                                binding.receiveImgDate.setVisibility(View.VISIBLE);

                                binding.receiveImgDate.setText(messageModel.getDate());

                                Log.d(TAG, "Glide 동작");
                                // 이미지 glide 를 이용하여 서버 url에 있는 이미지 비동기적으로 로드
                                Glide
                                        .with(MeetingRoomActivity.this)
                                        .load("https://webrtc-sfu.kro.kr:3030/images/" + filename)
                                        /** Glide는 원본 비율을 유지한다. */
                                        .override(500,500)
                                        .thumbnail(Glide.with(MeetingRoomActivity.this).load(R.raw.loading))
                                        .listener(new RequestListener<Drawable>() {
                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                // 이미지 로딩에 실패했을 때 수행할 작업
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                Log.d(TAG, "Glide 이미지 로드 완료");
                                                return false;
                                            }
                                        })
                                        .into(binding.msgImage);

                            } else {
                                binding.msgContent.setVisibility(View.VISIBLE);
                                binding.receiveDate.setVisibility(View.VISIBLE);

                                binding.msgImage.setVisibility(View.GONE);
                                binding.receiveImgDate.setVisibility(View.GONE);

                                binding.msgContent.setText(messageModel.getMsg());
                                binding.receiveDate.setText(messageModel.getDate());
                            }

                            binding.name.setText(messageModel.getSender().getClientId());
                            // 보낸 메시지
                        } else {
                            SendMessageRowItemBinding binding = (SendMessageRowItemBinding) (holder.sendBinding);

                            /** 이미지 파일이 존재하면 이미지파일을 보여주고 그렇지 않으면 텍스트를 보여주자. */
                            String filename = messageModel.getFilename();
                            if (filename != null) {
                                binding.msgContent.setVisibility(View.GONE);
                                binding.msgImage.setVisibility(View.VISIBLE);

                                // 이미지 glide 를 이용하여 서버 url에 있는 이미지 비동기적으로 로드
                                // 여기선 메시지 1개당 이미지 1개이기 때문에 항상 0 인덱스이다.
                                Glide
                                        .with(MeetingRoomActivity.this)
                                        .load("https://webrtc-sfu.kro.kr:3030/images/" + filename)
                                        .override(500,500)
                                        .thumbnail(Glide.with(MeetingRoomActivity.this).load(R.raw.loading))
                                        .listener(new RequestListener<Drawable>() {
                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                // 이미지 로딩에 실패했을 때 수행할 작업
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                return false;
                                            }
                                        })
                                        .into(binding.msgImage);
                            } else {
                                binding.msgContent.setVisibility(View.VISIBLE);
                                binding.msgImage.setVisibility(View.GONE);

                                binding.msgContent.setText(messageModel.getMsg());
                            }

                            binding.sendDate.setText(messageModel.getDate());
                            binding.sendImgDate.setText(messageModel.getDate());
                        }
                    }

                    // TODO: 레이아웃 변경
                    // 레이아웃 설정
                    @Override
                    public void onLayout(Context context, RecyclerView recyclerView) {
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        recyclerView.setLayoutManager(linearLayoutManager);
                    }
                });

                // TODO: 클릭 이벤트 변경
                // 리사이클러뷰 클릭 이벤트
                messageRecyclerView.setOnItemClickListener(new MessageRecyclerView.OnItemClickInterface() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                    }

                    @Override
                    public void onItemLongClickListener(View view, int position) {
                    }
                });

                messageRecyclerView.setContext(MeetingRoomActivity.this);
                // TODO: 데이터 변경
                // 데이터 세팅
                messageRecyclerView.setDataList(chatList);
                messageRecyclerView.setRecyclerView(meetingRoomChatRecyclerView);
                // TODO: row item 레이아웃 변경
                // row item 레이아웃 세팅
                messageRecyclerView.setReceiveRowItem(R.layout.receive_message_row_item);
                messageRecyclerView.setSendRowItem(R.layout.send_message_row_item);
                // 적용
                messageRecyclerView.adapt();

            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        });

        // 프래그먼트 매니저 가져오기
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.mainFrameLayout, peersFragment)
                .add(R.id.mainFrameLayout, whiteboardFragment)
                .add(R.id.mainFrameLayout, meetingRoomChatFragment)
                .show(peersFragment)
                .hide(whiteboardFragment)
                .hide(meetingRoomChatFragment)
                .commit();
    }


    /**
     * 앨범에서 이미지를 선택 했을 경우
     * Intent Callback
     */
    private ActivityResultLauncher activityAlbumResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            try {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();

                    ClipData clipData = data.getClipData();
                    Log.d(TAG, String.valueOf(clipData.getItemCount()));

                    // 선택한 이미지가 11장 이상인 경우
                    if (clipData.getItemCount() > 10) {
                        Toast.makeText(getApplicationContext(), "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show();
                        // 선택한 이미지가 1장 이상 10장 이하인 경우
                    } else {
                        Log.d(TAG, "multiple choice");

                        chatHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ArrayList<MultipartBody.Part> files = new ArrayList<>();
                                for (int i = 0; i < clipData.getItemCount(); i++) {
                                    Uri imageUri = clipData.getItemAt(i).getUri();  // 선택한 이미지들의 uri를 가져온다.
                                    try {
                                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(MeetingRoomActivity.this.getContentResolver(), imageUri);

                                        /** 사이즈 max 500 기준으로 리사이징 (비율 유지) */
                                        bitmap = resizeBitmapImage(bitmap);

                                        /** Width > Height 면, 이미지를 회전 시킨다. */
                                        ExifInterface exifInterface = new ExifInterface(getContentResolver().openInputStream(imageUri));
                                        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                                        // 방향 정보에 따라 이미지를 회전 또는 뒤집을 수 있습니다.
                                        Matrix rotateMatrix = new Matrix();
                                        switch (orientation) {
                                            case ExifInterface.ORIENTATION_ROTATE_90:
                                                rotateMatrix.postRotate(90);
                                                break;
                                            case ExifInterface.ORIENTATION_ROTATE_180:
                                                rotateMatrix.postRotate(180);
                                                break;
                                            case ExifInterface.ORIENTATION_ROTATE_270:
                                                rotateMatrix.postRotate(270);
                                                break;
                                            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                                                // 이미지를 수평으로 뒤집음
                                                break;
                                            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                                                // 이미지를 수직으로 뒤집음
                                                break;
                                        }

                                        bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                                bitmap.getWidth(), bitmap.getHeight(), rotateMatrix, false);

                                        Log.d(TAG, "bitmap:" + bitmap);
                                        File file = saveBitmapToJpeg(bitmap, MeetingRoomActivity.this);

                                        // MultipartBody.Part로 파일 생성
                                        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
//                                        MultipartBody.Part part = MultipartBody.Part.createFormData("photos", file.getName(), requestBody);
                                        MultipartBody.Part part = MultipartBody.Part.createFormData("photo", file.getName(), requestBody);
                                        files.add(part);
                                    } catch (Exception e) {
                                        Log.e(TAG, "File select error", e);
                                    }
                                }

                                /** 한번에 여러 이미지를 보낼 수도 있지만, 1개씩 보내주자 */
                                for(int i=0; i<files.size(); i++){
                                    MultipartBody.Part file = files.get(i);
                                    Call<RetrofitResponse> call = service.postImage(file);
                                    call.enqueue(new Callback<RetrofitResponse>() {
                                        @Override
                                        public void onResponse(Call<RetrofitResponse> call, Response<RetrofitResponse> response) {
                                            // 응답 처리
                                            if (response.isSuccessful()) {
                                                RetrofitResponse retrofitResponse = response.body();
                                                FileInfo fileInfo = retrofitResponse.getFileInfo();
                                                UserModel userModel = getUserModel(socketId);
                                                String date = getCurrentLocalDateTime();
                                                String filename = fileInfo.getFieldname();
                                                // 메시지 추가
                                                chatList.add(new MessageModel(MessageModel.MessageType.SEND, null, filename, userModel, date));

                                                // 연결된 피어 모두애게 그리기 정보를 보낸다.
                                                for (int i = 0; i < MeetingRoomActivity.peerConnections.size(); i++) {
                                                    CustomPeerConnection customPeerConnection = MeetingRoomActivity.peerConnections.get(i);
                                                    DataChannel dataChannel = customPeerConnection.getDataChannel();
                                                    String cmd = "chat";
                                                    String type = "img";
                                                    String data = cmd + ";" + type + ";" + fileInfo.getFieldname() + ";" + date;
                                                    ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
                                                    dataChannel.send(new DataChannel.Buffer(buffer, false));
                                                }

                                                // 성공하면, 자신에게 send Message를 표시해준다.
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        // 리사이클러뷰 새롭게 인지
                                                        meetingRoomChatBinding.meetingRoomChatRecyclerView.getAdapter().notifyDataSetChanged();
                                                    }
                                                });

                                            }
                                            Log.d(TAG, response.message());
                                        }

                                        @Override
                                        public void onFailure(Call<RetrofitResponse> call, Throwable t) {
                                            // 오류 처리
                                            Log.d(TAG, t.getMessage());
                                        }
                                    });
                                }
                                /** files를 한번에 다 보내기 */
//                                Call<RetrofitResponse> call = service.postImages(files);
//                                call.enqueue(new Callback<RetrofitResponse>() {
//                                    @Override
//                                    public void onResponse(Call<RetrofitResponse> call, Response<RetrofitResponse> response) {
//                                        // 응답 처리
//                                        if (response.isSuccessful()) {
//                                            RetrofitResponse retrofitResponse = response.body();
//                                            List<FileInfo> fileInfoList = retrofitResponse.getFileInfo();
//                                            for (FileInfo fileInfo : fileInfoList) {
//                                                UserModel userModel = getUserModel(socketId);
//                                                String date = getCurrentLocalDateTime();
//                                                String filename = fileInfo.getFieldname();
//                                                // 메시지 추가
//                                                chatList.add(new MessageModel(MessageModel.MessageType.SEND, null, filename, userModel, date));
//
//                                                // 연결된 피어 모두애게 그리기 정보를 보낸다.
//                                                for (int i = 0; i < MeetingRoomActivity.peerConnections.size(); i++) {
//                                                    CustomPeerConnection customPeerConnection = MeetingRoomActivity.peerConnections.get(i);
//                                                    DataChannel dataChannel = customPeerConnection.getDataChannel();
//                                                    String cmd = "chat";
//                                                    String type = "img";
//                                                    String data = cmd + ";" + type + ";" + fileInfo.getFieldname() + ";" + date;
//                                                    ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
//                                                    dataChannel.send(new DataChannel.Buffer(buffer, false));
//                                                }
//                                            }
//
//                                            // 성공하면, 자신에게 send Message를 표시해준다.
//                                            runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    // 리사이클러뷰 새롭게 인지
//                                                    meetingRoomChatBinding.meetingRoomChatRecyclerView.getAdapter().notifyDataSetChanged();
//
//                                                    // 스크롤 제일 아래로
//                                                    // messageRecyclerView.getRecyclerView().scrollToPosition(messageRecyclerView.getAdapter().getItemCount() - 1);
//                                                }
//                                            });
//
//                                        }
//                                        Log.d(TAG, response.message());
//                                    }
//
//                                    @Override
//                                    public void onFailure(Call<RetrofitResponse> call, Throwable t) {
//                                        // 오류 처리
//                                        Log.d(TAG, t.getMessage());
//                                    }
//                                });
                            }
                        });
                    }
                    // 취소
                } else {
                    Toast.makeText(MeetingRoomActivity.this, "취소 됐습니다.", Toast.LENGTH_SHORT).show();
                }
                isOpenAlbum = false;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    });

    private void showPermissionDialog() {
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
        localBuilder.setTitle("권한 설정")
                .setMessage("권한 거절로 인해 일부기능이 제한됩니다.")
                .setPositiveButton("권한 설정하러 가기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, e.toString());
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                            startActivity(intent);
                        }
                    }
                })
                .setNegativeButton("취소하기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                        Toast.makeText(MeetingRoomActivity.this, "권한을 취소하셨습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .create()
                .show();
    }

    /**
     * SYSTEM_ALERT_WINDOW 권한 체크
     */
    private ActivityResultLauncher systemPermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (Settings.canDrawOverlays(MeetingRoomActivity.this)) {
                Log.d(TAG, "SYSTEM_ALERT_WINDOW 권한 완료");
                if(result.getData() == null){
                    fragAndSocketSetting();
                }else{
                    String type = result.getData().getStringExtra("type");

                    if("screen_share".equals(type)){
                        // 여기서 화면 프로젝션 토큰을 받아오자.
                        mediaProjectionManager = getSystemService(MediaProjectionManager.class);
                        mediaProjectionLauncher.launch(mediaProjectionManager.createScreenCaptureIntent());
                    }else{
                        fragAndSocketSetting();
                    }
                }
            } else {
                Log.d(TAG, "SYSTEM_ALERT_WINDOW 권한 없음");
                finish();
            }
        }
    });

    public ServiceConnection meetingServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MeetingService.MeetingServiceBinder binder = (MeetingService.MeetingServiceBinder) service;
            meetingService = binder.getService();
            isMeetingServiceBound = true;
            Log.d(TAG, "바인드 받아옴");

            // 바로 미디어 프로젝션 세팅
            meetingService.setMediaProjection(mediaProjectionResultCode, mediaProjectionResultData, mediaProjectionManager);
            meetingService.setContext(MeetingRoomActivity.this);

            meetingService.setInterface(new MeetingService.MeetingServiceResult() {
                @Override
                public void onScreenCapturerCreated(CustomCapturer customCapturer) {
                    Log.d(TAG, "screenCapturer 얻어옴");

                    // 캡처러 초기화
                    initializeCapturer(customCapturer.getType(), customCapturer);

                    // 로컬 화면 트랙 생성
                    createTrack(Common.SCREEN);

                    // 화면 캡처중 표시하기
                    meetingService.enableCaptureDisplayRectangle(true);

                    /**
                     * [화면 공유 Peer 생성]
                     *
                     * 화면 공유 버튼을 클릭하여, 공유 피어를 생성하는 경우에는
                     * 공류를 시작한 당사자만 가능하다.
                     *
                     * 나머지 사람들 전체에 모두 피어를 생성한다.
                     */
                    if (!"".equals(shareId) && shareId.equals(socketId)) {
                        for (int i = 0; i < userList.size(); i++) {
                            UserModel userModel = userList.get(i);
                            createPeerAndDoOffer(userModel.clientId, Common.SCREEN);
                        }
                    }
                }

                @Override
                public void onError(String message) {
                    Log.e(TAG, message);
                }
            });

            CustomCapturer customCapturer = getCustomVideoCapturer(Common.SCREEN);
            if (customCapturer == null) {
                meetingService.createCapturer(Common.SCREEN);
            } else {
                // 카메라 캡처 시작
                VideoCapturer videoCapturer = customCapturer.getVideoCapturer();
                videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);

                // 화면 캡처중 표시하기
                meetingService.enableCaptureDisplayRectangle(true);

                // [화면 공유 Peer 생성]
                if (!"".equals(shareId) && shareId.equals(socketId)) {
                    for (int i = 0; i < userList.size(); i++) {
                        UserModel userModel = userList.get(i);
                        createPeerAndDoOffer(userModel.clientId, Common.SCREEN);
                    }
                }
            }

            // 서비스 시작
            Intent intent = new Intent(MeetingRoomActivity.this, MeetingService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isMeetingServiceBound = false;
        }
    };

    public ServiceConnection mediaServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TestService.TestServiceBinder binder = (TestService.TestServiceBinder) service;
            testService = binder.getService();
            isMediaServiceBound = true;
            Log.d(TAG, "바인드 받아옴");

            // 바로 미디어 프로젝션 세팅
            testService.setContext(MeetingRoomActivity.this);

            CustomCapturer customCapturer = getCustomVideoCapturer(VIDEO);
//            testService.startCapture(customCapturer.getVideoCapturer());

            // 서비스 시작
            Intent intent = new Intent(MeetingRoomActivity.this, TestService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isMediaServiceBound = false;
        }
    };

    /**
     * 여러 활동 결과 호출이 있고
     * 다른 계약을 사용하거나
     * 별개의 콜백을 원하면
     * registerForActivityResult()를 여러 번 호출하여
     * 여러 개의 ActivityResultLauncher 인스턴스를 등록할 수 있습니다.
     */
    private ActivityResultLauncher<Intent> mediaProjectionLauncher = registerForActivityResult(
            // 결과를 생성하는 데 필요한 입력 유형과 결과의 출력 유형을 정의
            // 이 계약은 일반 계약으로, Intent를 입력으로 가져와서 ActivityResult를 반환
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    mediaProjectionResultCode = result.getResultCode();
                    mediaProjectionResultData = result.getData();

                    if (isMeetingServiceBound) return;

                    if (!isForegroundServiceRunning(this, MeetingService.class)) {
                        // 여기서 포그라운드 서비스 실행
                        // 미팅 서비스 시작
                        Intent intent = new Intent(this, MeetingService.class);
                        bindService(intent, meetingServiceConn, Context.BIND_AUTO_CREATE);
                    }

                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "[MeetingRoomActivity] onCreate 동작");
        binding = ActivityMeetingRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        roomId = String.valueOf(getIntent().getIntExtra(ROOMID, -1));

        if("-1".equals(roomId)){
            Toast.makeText(MeetingRoomActivity.this, "잘못된 미팅방입니다.",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        roomName = getIntent().getStringExtra(ROOM_NAME);
        isActivateCamera = getIntent().getBooleanExtra(IS_ACTIVATE_CAMERA, false);

        /**
         * 호스트 데이터 가져오기
         * 실제 호스트 데이터와 현재 유저가 일치할 경우에만 host에 값을 넣어주자.
         */
        Integer roomIdInt = Integer.valueOf(roomId);
        Call<MeetingDTO> call = RetrofitService.getInstance().getService().getMeetingHostInfo(roomIdInt);
        call.enqueue(new Callback<MeetingDTO>() {
            @Override
            public void onResponse(Call<MeetingDTO> call, Response<MeetingDTO> response) {
                // 응답 처리
                if (response.isSuccessful()) {
                    MeetingDTO meetingDTO = response.body();
                    Log.d(TAG, "meetingDTO:" + meetingDTO);
                    if(Util.user.getId().equals(meetingDTO.getHost())){
                        host = meetingDTO.getHost();
                    }
                }
                Log.d(TAG, response.message());
                /** 미팅 권한 launch */
                meetingPermissionLauncher.launch(MEETING_PERMISSIONS);
            }
            @Override
            public void onFailure(Call<MeetingDTO> call, Throwable t) {
                // 오류 처리
                Log.d(TAG, t.getMessage());
            }
        });

        //host = getIntent().getStringExtra(HOST);

        // 방의 현재 인원수 수정
        postCurrentClientUpdate(MeetingDTO.CURRENT_CLIENT_UPDATE_TYPE.INCREASE);

        binding.meetingTitle.setText(roomName);

        /** 얼굴 마스크를 위한 세팅 */
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setAlpha(0xff);

        mask1 = BitmapFactory.decodeResource(getResources(), R.drawable.mask1);
//        mask2 = (VectorDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.test, null);

        /** 실제 이미지 크기(픽셀)과 안드로이드로 불러온 이미지 크기(픽셀)의 차이 비율 계산 */
        mask1Ratio = mask1.getWidth() / mask1OriginalW;

        mask1AdjustedW = mask1.getWidth();
        mask1AdjustedH = mask1.getHeight();
        mask1AdjustedCX = mask1OriginalCX * mask1Ratio;
        mask1AdjustedCY = mask1OriginalCY * mask1Ratio;
        mask1AdjustedD = mask1OriginalD * mask1Ratio;

        HandlerThread handlerThread = new HandlerThread("WebRTCThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        HandlerThread handlerChatThread = new HandlerThread("chatThread");
        handlerChatThread.start();
        chatHandler = new Handler(handlerChatThread.getLooper());

        binding.cameraOnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoTrack videoTrack = getCustomTrack(Common.VIDEO).getVideoTrack();
                if (isVideoOn) {
                    videoTrack.setEnabled(false);

                    binding.cameraImageView.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                    isVideoOn = false;
                } else {
                    videoTrack.setEnabled(true);
                    binding.cameraImageView.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                    isVideoOn = true;
                }
            }
        });

        binding.audioOnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioTrack audioTrack = getCustomTrack(Common.VIDEO).getAudioTrack();
                if (isAudioOn) {
                    audioTrack.setEnabled(false);
                    binding.audioImageView.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                    isAudioOn = false;
                } else {
                    audioTrack.setEnabled(true);
                    binding.audioImageView.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                    isAudioOn = true;
                }
            }
        });

        binding.screenShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (socket == null) {
                    Toast.makeText(MeetingRoomActivity.this, "소켓 연길이 되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                socket.emit("share");
            }
        });

        binding.whiteBoardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (socket == null) {
                    Toast.makeText(MeetingRoomActivity.this, "소켓 연길이 되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                socket.emit("whiteboard");
            }
        });

        binding.faceMaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFaceMode) {
                    isFaceMode = true;
                } else {
                    isFaceMode = false;
                }
            }
        });

        binding.chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (meetingRoomChatFragment != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            replaceFragment(meetingRoomChatFragment);
                        }
                    });
                }
            }
        });

        binding.swithCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomCapturer customCapturer = getCustomVideoCapturer(VIDEO);
                ((CameraVideoCapturer) customCapturer.getVideoCapturer()).switchCamera(null);
            }
        });

        // 참가자 리스트
        binding.userListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "userList:" + userList);
                showUserListDialog();
            }
        });

        /**
         * 얼굴 인식 옵션 초기화
         */
        // High-accuracy landmark detection and face classification
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();

        // Real-time contour detection
        FaceDetectorOptions realTimeOpts =
                new FaceDetectorOptions.Builder()
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .build();

        detector = FaceDetection.getClient(highAccuracyOpts);

        /**
         * Retrofit
         */
        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl("https://webrtc-sfu.kro.kr:3030")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        service = retrofit.create(RetrofitInterface.class);

        binding.exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isClickBackBtn = true;
                /**
                 * 1. 나가는 인원이 호스트 라면, 다른 사람들에게 out하라는 소켓 메시지를 보낸다.
                 * 2. 해당 방 DB를 삭제시킨다.
                 */
                UserModel host = MeetingRoomActivity.this.userList.stream().filter(userModel -> userModel.isHost()).findFirst().orElse(null);
                String hostEmail = null;
                if(host != null){
                    hostEmail = host.getEmail();
                }
                if(Util.user.getId().equals(hostEmail)){
                    // 방 삭제
                    postCheckDeleteMeeting();
                }else{
                    // 방의 현재 인원수 수정
                    postCurrentClientUpdate(MeetingDTO.CURRENT_CLIENT_UPDATE_TYPE.DECREASE);
                }
            }
        });

    }

    private int noCnt = 0;

    private String getCurrentLocalDateTime() {
        // 현재 날짜/시간
        LocalDateTime now = LocalDateTime.now();

        // 포맷팅
        String formatedNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        return formatedNow;
    }

    private void calculateFaceInfo(List<Face> faces, Bitmap bitmap) {

        if (faces.size() == 0) {
            noCnt++;
            if (noCnt >= 7) {
                faceRect = null;
                faceDistance = -1;
                noCnt = 0;
            }
        }

        for (Face face : faces) {
            faceRect = face.getBoundingBox();
            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
            // nose available):
            FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
            FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);

            if (leftEye != null && rightEye != null) {
                PointF leftEyePos = leftEye.getPosition();
                Log.d(TAG, "leftEyePos:" + leftEyePos);
                left_eye_x = leftEyePos.x;
                left_eye_y = leftEyePos.y;

                PointF rightEyePos = rightEye.getPosition();
                Log.d(TAG, "rightEyePos:" + rightEyePos);
                right_eye_x = rightEyePos.x;
                right_eye_y = rightEyePos.y;

                float minEyeX = -1;
                float minEyeY = -1;
                float maxEyeX = -1;
                float maxEyeY = -1;

                if (left_eye_x > right_eye_x) {
                    minEyeX = right_eye_x;
                    maxEyeX = left_eye_x;
                } else if (left_eye_x < right_eye_x) {
                    minEyeX = left_eye_x;
                    maxEyeX = right_eye_x;
                } else {
                    minEyeX = left_eye_x;
                    maxEyeX = left_eye_x;
                }

                if (left_eye_y > right_eye_y) {
                    minEyeY = right_eye_y;
                    maxEyeY = left_eye_y;
                } else if (left_eye_y < right_eye_y) {
                    minEyeY = left_eye_y;
                    maxEyeY = right_eye_y;
                } else {
                    minEyeY = left_eye_y;
                    maxEyeY = left_eye_y;
                }


                // 실제 얼굴 눈 사이의 거리
                faceDistance = calDistance(leftEyePos, rightEyePos);
                faceCX = minEyeX + (maxEyeX - minEyeX) / 2;
                faceCY = minEyeY + (maxEyeY - minEyeY) / 2;

                /**
                 * 실제 얼굴과 조정된 마스크 사이의 비율
                 * 비율 = (실제 얼굴의 눈 사이의 거리)/(조정된 마스크의 눈 사이의 거리)
                 */
                mask1AdjustedD = mask1OriginalD * mask1Ratio;
                mask1FaceRatio = faceDistance / mask1AdjustedD;

                /** 실제 얼굴과 안드로이드 마스크 사이의 비율로 크기 조정  */
                mask1 = resizeBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mask1), mask1FaceRatio);

                mask1AdjustedD = mask1AdjustedD * mask1FaceRatio;
                mask1AdjustedCX = mask1AdjustedCX * mask1FaceRatio;
                mask1AdjustedCY = mask1AdjustedCY * mask1FaceRatio;
                mask1AdjustedW = mask1AdjustedW * mask1FaceRatio;
                mask1AdjustedH = mask1AdjustedH * mask1FaceRatio;

                /** faceRect 높이 만큼 마스크 높이를 늘린다. */
                int changeWidth = (int) (faceRect.width() * 1.6);
                int changeHeight = (int) (faceRect.height() * 1.4);
                float changeWidthRatio = changeWidth / mask1AdjustedW;
                float changeHeightRatio = changeHeight / mask1AdjustedH;

                mask1AdjustedW = mask1AdjustedW * changeWidthRatio;
                mask1AdjustedH = mask1AdjustedH * changeHeightRatio;

                mask1AdjustedCX = mask1AdjustedCX * changeWidthRatio;
                mask1AdjustedCY = mask1AdjustedCY * changeHeightRatio;

                mask1 = Bitmap.createScaledBitmap(mask1, (int) mask1AdjustedW, (int) mask1AdjustedH, true);

                differenceX = faceCX - mask1AdjustedCX;
                differenceY = faceCY - mask1AdjustedCY;

                /**
                 * 백터 이미지 크기 조정을 위한 곳
                 */
//                int left = (int) (faceRect.left - 100);
//                int top = (int) (faceRect.top - 100);
//                int right = (int) (faceRect.right + 100);
//                int bottom = (int) (faceRect.bottom + 100);
//                mask2.setBounds(left, top, right, bottom);
            }


            // If face tracking was enabled:
//            if (face.getTrackingId() != null) {
//                int id = face.getTrackingId();
//            }
            break;
        }

//        faceListener.onSuccess(bitmap);
    }

    // 비트맵 리사이징
    public Bitmap resizeBitmap(Bitmap bitmap, float ratio) {

        int scaleWidth = (int) (bitmap.getWidth() * ratio); //(int) (bitmap.getWidth() * ratio);
        int scaleHeight = (int) (bitmap.getHeight() * ratio); // (int) (bitmap.getHeight() * ratio);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, true);

        return resizedBitmap;
    }

    public void replaceFragment(Fragment fragment) {
        if(this.isFinishing() || this.isDestroyed()){
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (fragment instanceof PeersFragment) {
            fragmentTransaction
                    .hide(whiteboardFragment)
                    .hide(meetingRoomChatFragment)
                    .show(fragment)
                    .commit();
            meetingMode = PEERS;
        } else if (fragment instanceof WhiteboardFragment) {
            fragmentTransaction
                    .hide(peersFragment)
                    .hide(meetingRoomChatFragment)
                    .show(fragment)
                    .commit();
            meetingMode = WHITE_BOARD;
        } else if (fragment instanceof MeetingRoomChatFragment) {
            // 현재 show된 프래그먼트가 채팅일 경우
            if (CHAT.equals(meetingMode)) {
                fragmentTransaction
                        .hide(meetingRoomChatFragment)
                        .hide(whiteboardFragment)
                        .show(peersFragment)
                        .commit();
                meetingMode = PEERS;
            } else {
                fragmentTransaction
                        .hide(whiteboardFragment)
                        .hide(peersFragment)
                        .show(fragment)
                        .commit();
                meetingMode = CHAT;
            }
        }
    }

    private Bitmap getViewBitmap(View view) {
        int width = view.getWidth();
        int height = view.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void videoTrackSetEnabled(Boolean isEnable) {
        for (int i = 0; i < trackList.size(); i++) {
            CustomTrack customTrack = trackList.get(i);
            VideoTrack videoTrack = customTrack.getVideoTrack();
            String tagFlag = getTypeTag(customTrack.getType());

            Log.d(TAG, tagFlag + "videoTrack 활성화 여부: " + isEnable);
            videoTrack.setEnabled(isEnable);
        }
    }

    private CustomPeerConnection createCustomPeerConnection(String clientId, String type, CreateCustomPeerConnResult createCustomPeerConnResult) {
        // 자기 자신은 제외
        if (socketId.equals(clientId)) return null;

        // 이미 존재 하는 피어 제외
        CustomPeerConnection customPeerConnection = getCustomPeerConnection(clientId, type);
        if (customPeerConnection != null) return null;

        // 피어 생성
        customPeerConnection = createPeerConnection(factory, clientId, type, createCustomPeerConnResult);

        String tagFlag = getTagFlagString(customPeerConnection.getClientId(), customPeerConnection.getType());
        Log.d(TAG, tagFlag + "상대방 피어 생성 완료");

        // 피어 추가
        peerConnections.add(customPeerConnection);
        Log.d(TAG, "customPeerConnection:" + customPeerConnection);

        return customPeerConnection;
    }

    private String getTagFlagString(String clientId, String type) {
        if (clientId.equals("local")) return "[local:" + clientId + "]";
        return "[" + type + " peer:" + clientId + "]";
    }

    // ICE Candidate 데이터가 존재하면 모두 밀어 넣는다.
    private void drainQueue(CustomPeerConnection customPeerConnection, CustomQueue customQueue) {
        String tagFlag = getTagFlagString(customPeerConnection.getClientId(), customPeerConnection.getType());

        Queue<IceCandidate> iceQueue = customQueue.getQueue();
        while (iceQueue.size() != 0) {
            IceCandidate iceCandidate = iceQueue.poll();
            customPeerConnection.getPeerConnection().addIceCandidate(iceCandidate);
            Log.d(TAG, tagFlag + "ICE Candidate 추가 완료");
        }
    }

    // 화면 공유 시작
    private void screenSharingStart() {
        /**
         * SYSTEM_ALERT_WINDOW 권한 체크
         */
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            intent.putExtra("type", "screen_share");
            systemPermissionLauncher.launch(intent);
        } else {
            // 여기서 화면 프로젝션 토큰을 받아오자.
            mediaProjectionManager = getSystemService(MediaProjectionManager.class);
            mediaProjectionLauncher.launch(mediaProjectionManager.createScreenCaptureIntent());
        }
    }

    // 현재 foreground service가 동작 중인지 체크하는 메서드
    public boolean isForegroundServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                if (serviceInfo.foreground) {
                    return true; // foreground service 동작 중
                }
            }
        }

        return false; // foreground service 동작 중이 아님
    }

    @Override
    protected void onDestroy() {
        /**
         * 1. 방을 나갔다는 사실을 인원들에게 알림
         * 2. 비디오 중지
         * 3. 소켓 닫기
         * 4. 모든 피어 닫기
         */
        disconnect();

        // 시스템 화면에 보이는 SurfaceView를 끈다.
        toggleMainSurfaceView(false);

        super.onDestroy();
    }

    private ProxySink remoteProxySink;

    private void localSetting() {
        // 미디어 설정 초기화
        initializeSdpMediaCon();

        // 메인 SurfaceView를 초기화 한다
        initializeMainSurfaceViews();

        // 피어 팩토리 초기화
        initializePeerConnectionFactory();

        // 캡처러 초기화
        initializeCapturer(Common.VIDEO, null, true);

        // 트랙 생성
        CustomTrack customTrack = createTrack(Common.VIDEO);

        // 프록시 싱크 생성
        createProxySink("local", Common.VIDEO);

        // 트랙과 view 연동
        bindTrackAndView("local", customTrack.getType(), peersBinding.mainSurfaceView, null);

        // 처음 들어올 때 화상회의 켰냐 안켰냐의 설정에 따라 설정
        VideoTrack videoTrack = customTrack.getVideoTrack();
        videoTrack.setEnabled(isActivateCamera);
        isVideoOn = isActivateCamera;

        if(isActivateCamera){
            binding.cameraImageView.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
        }else{
            binding.cameraImageView.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        }

        AudioTrack audioTrack = customTrack.getAudioTrack();
        audioTrack.setEnabled(true);
        isAudioOn = true;
        binding.audioImageView.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);

        /** Remote View 를 위한 설정 */
        remoteProxySink = createProxySink("remoteLocal", Common.VIDEO);
        remoteProxySink.setVideoTrack(customTrack.getVideoTrack());
        remoteProxySink.setTarget(remoteCustomSurfaceViewRenderer);
        // 카메라 비디오 트랙 <=> ProxySink <=> SurfaceView
        customTrack.getVideoTrack().addSink(remoteProxySink);

        remoteCustomSurfaceViewRenderer.setOnLongClickListener(new View.OnLongClickListener() {
            private int initialX = 0;
            private int initialY = 0;

            @Override
            public boolean onLongClick(View view) {
                remoteCustomSurfaceViewRenderer.setOnTouchListener(new View.OnTouchListener() {
                    float initialTouchX = 0;
                    float initialTouchY = 0;
                    @Override public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
//                            case MotionEvent.ACTION_DOWN:
//                                initialX = params.x;
//                                initialY = params.y;
//                        Log.d(TAG, "[ACTION_DOWN]");
//                        Log.d(TAG, "params.x:" + params.x);
//                        Log.d(TAG, "params.y:" + params.y);
//                        Log.d(TAG, "initialX:" + initialX);
//                        Log.d(TAG, "initialY:" + initialY);
//                                initialTouchX = event.getRawX();
//                                initialTouchY = event.getRawY();
//                        Log.d(TAG, "initialTouchX:" + initialTouchX);
//                        Log.d(TAG, "initialTouchY:" + initialTouchY);
//                                return true;
                            case MotionEvent.ACTION_UP:
                                remoteCustomSurfaceViewRenderer.setOnTouchListener(null);
                                initialX = params.x;
                                initialY = params.y;
                                return true;
                            case MotionEvent.ACTION_MOVE:
                        Log.d(TAG, "[ACTION_MOVE]");
//                        Log.d(TAG, "initialX:" + initialX);
//                        Log.d(TAG, "initialY:" +initialY);
                        Log.d(TAG, "event.getRawX():" +event.getRawX());
                        Log.d(TAG, "event.getRawY() :" +event.getRawY() );
                        Log.d(TAG, "[전]initialTouchX :" +initialTouchX );
                        Log.d(TAG, "[전]initialTouchY :" +initialTouchY );
                                if(initialTouchX == 0 && initialTouchY == 0){
                                    initialTouchX = event.getRawX();
                                    initialTouchY = event.getRawY();
                                }
                                Log.d(TAG, "[후]initialTouchX :" +initialTouchX );
                                Log.d(TAG, "[후]initialTouchY :" +initialTouchY );
                                params.x = initialX + (int) (event.getRawX() - initialTouchX);
                                params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        Log.d(TAG, "params.x:" + params.x);
                        Log.d(TAG, "params.y:" + params.y);
                                WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                                windowManager.updateViewLayout(remoteCustomSurfaceViewRenderer, params);
                                return true;
                        }
                        return false;
                    }
                });
                return true;
            }
        });

        remoteCustomSurfaceViewRenderer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MeetingRoomActivity.this, MeetingRoomActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);

                Toast.makeText(MeetingRoomActivity.this, "클릭!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static int ConvertDPtoPX(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void initializeCapturer(String type, CustomCapturer screenCapturer) {
        initializeCapturer(type, screenCapturer, true);
    }

    private void initializeCapturer(String type, CustomCapturer screenCapturer, boolean isFront) {
        CustomCapturer customCapturer = null;
        if (Common.VIDEO.equals(type)) {
            customCapturer = new CustomCapturer(type, MeetingRoomActivity.this, isFront);
        } else if (Common.SCREEN.equals(type)) {
            customCapturer = screenCapturer;
        }

        String typeFlag = getTypeTag(type);
        Log.d(TAG, typeFlag + "캡처러 생성 완료");

        SurfaceTextureHelper surfaceTextureHelper =
                SurfaceTextureHelper.create("VideoThread" + type, rootEglBase.getEglBaseContext());

        // 카메라에서 비디오 소스 생성
        VideoSource videoSource = factory.createVideoSource(customCapturer.getVideoCapturer().isScreencast());
        customCapturer.setVideoSource(videoSource);

        // 카메라 초기화 및 캡처 시작
        VideoCapturer videoCapturer = customCapturer.getVideoCapturer();
        videoCapturer.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());

        // 텍스처를 1개 생성합니다.
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        YuvConverter yuvConverter = new YuvConverter();

        long start = System.nanoTime();
//        int width = ConvertDPtoPX(this, peersBinding.mainSurfaceView.getWidth());
//        int height = ConvertDPtoPX(this, peersBinding.mainSurfaceView.getHeight());
        TextureBufferImpl buffer = new TextureBufferImpl(VIDEO_RESOLUTION_HEIGHT, VIDEO_RESOLUTION_WIDTH, VideoFrame.TextureBuffer.Type.RGB, textures[0], new Matrix(), surfaceTextureHelper.getHandler(), yuvConverter, null);

        if (VIDEO.equals(type)) {
            ((Camera2Capturer) videoCapturer).setCameraCaptureInterface(new CameraCaptureInterface() {
                @Override
                public void onBeforeCapture(CapturerObserver capturerObserver, VideoFrame frame) {
                    if (isFaceMode) {
                        Bitmap bitmap = convertVideoFrameToBitmap(frame);
//                    Log.d(TAG, "bitmap:" + bitmap);

                        Canvas canvas = new Canvas(bitmap);


//                        Canvas canvas = new Canvas(bitmap);
//                        mask2.setBounds(0, 0, 200, 200);
//                        // x축과 y축으로 각각 0 픽셀씩 이동
//                        canvas.translate(0, 0);
//                        mask2.draw(canvas);


                        if (faceRect != null) {
//                            canvas.drawRect(faceRect, paint);
                        }

                        // 왼쪽눈 오른쪽 눈에 대한 정보가 있다면 그려주자
                        if (faceDistance >= 0) {
//                            mask2.draw(canvas);
                            canvas.drawBitmap(mask1, differenceX, differenceY, null);
                        }

                        /** 50번 캡처하면 그때 1번 얼굴 인식을 한다. */
                        cnt++;
                        if (cnt >= 25) {
                            cnt = 0;
                            Bitmap copyBitmap = bitmap;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    faceDetect(copyBitmap);
                                }
                            });

                        }

                        // 좌우 상하 반전
                        Matrix matrix = new Matrix();
                        matrix.setScale(1, -1);

                        // 이미지 회전
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                        Bitmap copyBitmap2 = bitmap;

                        surfaceTextureHelper.getHandler().post(() -> {
                            // 텍스처를 바인딩합니다.
                            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
                            // 텍스처의 크기가 매핑되는 삼각형과의 크기가 맞지 않을 경우 축소하거나 확대할 때 어떤 식으로 필터링할 것인지 결정
                            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, copyBitmap2, 0);

                            VideoFrame.I420Buffer i420Buf = yuvConverter.convert(buffer);

                            long frameTime = System.nanoTime() - start;
                            VideoFrame videoFrame = new VideoFrame(i420Buf, 0, frameTime);

                            if (isFaceMode) {
                                capturerObserver.onFrameCaptured(videoFrame);
                            }
                            videoFrame.release();

                        });

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        return;
                    }
                    capturerObserver.onFrameCaptured(frame);
//                Log.d(TAG, "테스트");
                }
            });
        }

        videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);

        // 오디오 소스 생성
        AudioSource audioSource = factory.createAudioSource(audioConstraints);
        customCapturer.setAudioSource(audioSource);

        capturerList.add(customCapturer);

    }

    private void faceDetect(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
//        Log.d(TAG, "image:" + image);

        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
//                                        Log.d(TAG, "faces:" + faces);
                                        calculateFaceInfo(faces, bitmap);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "e:" + e.getMessage());
                                    }
                                });
    }

    private GlTextureFrameBuffer bitmapTextureFramebuffer = new GlTextureFrameBuffer(6408);
    private VideoFrameDrawer frameDrawer = new VideoFrameDrawer();
    private Matrix drawMatrix = new Matrix();

    public Bitmap convertVideoFrameToBitmap(VideoFrame frame) {
        return peersBinding.mainSurfaceView.eglRenderer.convertVideoFrameToBitmap(frame);
    }

    public Bitmap convertVideoFrameToBitmap2(VideoFrame videoFrame) {
        // VideoFrame에서 YUV 데이터 가져오기
        VideoFrame.I420Buffer i420Buffer = videoFrame.getBuffer().toI420();

        int width = i420Buffer.getWidth();
        int height = i420Buffer.getHeight();
        int ySize = width * height;
        int uvSize = (width / 2) * (height / 2);

        // YUV 데이터를 RGB로 변환
        byte[] data = new byte[ySize + (uvSize * 2)];
        i420Buffer.getDataY().get(data, 0, ySize);
        i420Buffer.getDataU().get(data, ySize, uvSize);
        i420Buffer.getDataV().get(data, ySize + uvSize, uvSize);

        // YUV 데이터로부터 Bitmap 생성
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, byteArrayOutputStream);
        byte[] jpegData = byteArrayOutputStream.toByteArray();
        bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);

        return bitmap;
    }

    private ProxySink createProxySink(String clientId, String type) {
        ProxySink proxySink = new ProxySink(clientId, type);
        String tagFlag = getTagFlagString(clientId, type);

        Log.d(TAG, tagFlag + "프록시 싱크 생성 완료");

        proxySinks.add(proxySink);

        Log.d(TAG, tagFlag + "프록시 싱크 추가완료:" + proxySinks);

        return proxySink;
    }

    private void bindTrackAndView(String clientId, String type, SurfaceViewRenderer surfaceViewRenderer, VideoTrack remoteVideoTrack) {
        ProxySink proxySink = getProxySink(clientId, type);
        String tagFlag = getTagFlagString(clientId, type);

        if (clientId.equals("local")) {
            proxySink.setTarget(surfaceViewRenderer);
            Log.d(TAG, tagFlag + "surfaceViewRenderer:" + surfaceViewRenderer);
            CustomTrack customTrack = getCustomTrack(type);
            VideoTrack videoTrack = customTrack.getVideoTrack();
            proxySink.setVideoTrack(videoTrack);
            videoTrack.addSink(proxySink);
        } else {
            proxySink.setVideoTrack(remoteVideoTrack);
            remoteVideoTrack.addSink(proxySink);
        }
    }

    private CustomTrack createTrack(String type) {
        CustomCapturer customCapturer = getCustomVideoCapturer(type);
        VideoSource videoSource = customCapturer.getVideoSource();

        CustomTrack customTrack = new CustomTrack(type);

        // 비디오 소스에서 비디오 트랙 추출
        VideoTrack videoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        videoTrack.setEnabled(true);
        customTrack.setVideoTrack(videoTrack);

        // 오디오 트랙 추출
        AudioSource audioSource = customCapturer.getAudioSource();
        AudioTrack audioTrack = factory.createAudioTrack("101", audioSource);
        customTrack.setAudioTrack(audioTrack);

        trackList.add(customTrack);

        return customTrack;
    }

    private void initializeSdpMediaCon() {
        sdpMediaConstraints = new MediaConstraints();

        audioConstraints = new MediaConstraints();

        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("IceRestart", "true"));
        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("internalSctpDataChannels", "true"));
    }

    private void connectToSignallingServer() {
        try {
            // For me this was "http://192.168.1.220:3000";
            String URL = "https://webrtc-sfu.kro.kr:3030/";// "https://calm-badlands-59575.herokuapp.com/"; //
            socket = IO.socket(URL);

            socket.on(EVENT_CONNECT, args -> {
                Log.d(TAG, "[소켓연결]");
//                Log.d(TAG, "[소켓에서 스레드 네임]" + Thread.currentThread().getName());

                MeetingDTO meetingDTO = new MeetingDTO();
                meetingDTO.setId(Integer.valueOf(roomId));
                meetingDTO.setUser(Util.user);
                // host 정보가 있을 때만 서버로 host정보 보내준다.
                if(host != null && !"".equals(host)) meetingDTO.setHost(host);

                String jsonString = gson.toJson(meetingDTO);

                socket.emit("join", jsonString);
            }).on("bye", args -> {
                JSONObject message = (JSONObject) args[0];
                String clientId = message.optString("id");
                String roomId = message.optString("roomId");
                String name = message.optString("name");

                String sharingSocketId = message.optString("sharingSocketId");
                String whiteboardSocketId = message.optString("whiteboardSocketId");

                Log.d(TAG, "[socket:" + clientId + "] 상대방이 방에서 나감. " + "(roomId:" + roomId + ")(name:" + name + ")");

                // 피어 나가기
                exitPeer(clientId);

                // 해당 사용자 제거
                removeElementAboutUserList(userList, clientId);

                // 화이트보드 제거
                if ("".equals(whiteboardSocketId) || whiteboardSocketId == null) {
                    stopWhiteboard();
                }

                Log.d(TAG, "[userId:" + clientId + "] userList 에서 제거");
                Log.d(TAG, "userList:" + userList);

            }).on("created", args -> {
                Log.d(TAG, "[방 생성]");
            }).on("host_out", args -> {
                Log.d(TAG, "[호스트가 방에서 나갔습니다.]");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MeetingRoomActivity.this, "[호스트가 방에서 나갔습니다.]", Toast.LENGTH_SHORT).show();
                    }
                });

                // window view 안나오게 하기 위해서는 back btn true로 설정해야한다.
                isClickBackBtn = true;
                finish();
            }).on("host_change", args -> {
                Log.d(TAG, "[호스트가 변경 됐습니다]");
                JSONArray jsonArray = (JSONArray) args[0];
                String jsonString = jsonArray.toString();

                List<User> userList = gson.fromJson(jsonString, new TypeToken<List<User>>(){}.getType());
                List<UserModel> userModelList = new ArrayList<>();
                // 모든 사용자 리스트에 대해서 프로필 이미지 경로를 가져와서 세팅해주자
                a:for(User user : userList){
                    UserModel newUserModel = new UserModel();
                    newUserModel.setHost(user.isHost());
                    newUserModel.setEmail(user.getId());
                    if(user.getProfileImgPaths() != null){
                        // 제일 먼저 발경 되는 프로필 이미지 경로를 가져온다.
                        b:for(ProfileMap profileMap : user.getProfileImgPaths()){
                            if(profileMap.getType().equals(CustomDialog.Type.PROFILE_IMAGE.name())){
                                newUserModel.setImgPath(profileMap.getProfileImgPath());
                                break b;
                            }
                        }
                    }
                    newUserModel.setName(user.getName());
                    newUserModel.setProfileImgPaths(user.getProfileImgPaths());
                    newUserModel.setClientId(user.getClientId());
                    userModelList.add(newUserModel);
                }
                MeetingRoomActivity.this.userList = userModelList;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UserModel host = MeetingRoomActivity.this.userList.stream().filter(userModel -> userModel.isHost()).findFirst().orElse(null);
                        String hostName = null;
                        if(host != null){
                            hostName = host.getName();
                        }
                        Toast.makeText(MeetingRoomActivity.this, "호스트가 " + hostName + "님으로 변경 됐습니다.",Toast.LENGTH_LONG).show();
                        if(meetingUserListDialogFragment != null) meetingUserListDialogFragment.update(userModelList);
                    }
                });

            }).on("full", args -> {
                Log.d(TAG, "[방이 가득 찼습니다.]");
            }).on("message", args -> {
//                Log.d(TAG, "메시지를 받았습니다.");
            }).on("message", args -> {
                try {
                    JSONObject message = (JSONObject) args[0];
//                    Log.d(TAG, "[받은 메시지] : " + message);

                    String senderId = message.optString("senderId");
                    String targetId = message.optString("targetId");
                    String type = message.getString("type");
                    String peerType = message.optString("peerType");

                    shareId = message.optString("shareId");
                    String shareStatus = message.optString("shareStatus");

                    if ("".equals(whiteboardId) || whiteboardId == null) {
                        whiteboardId = message.optString("whiteboardId");
                    }
                    whiteboardStatus = message.optString("whiteboardStatus");


                    /**
                     * [userList]
                     */
                    if (type.equals("userList")) {
                        try {
                            if(socket == null) return;

                            String initiator = message.optString("initiator");

                            socketId = socket.id();

                            Log.d(TAG, "[proxyVideoSinks]:" + proxySinks);
                            Log.d(TAG, "[initiator]:" + initiator);
                            Log.d(TAG, "[모바일 소켓 ID]:" + socketId);

                            /**
                             * [유저리스트 업데이트]
                             * 1. 새로운 유저는 추가
                             * 2. 잔존하는 유저 삭제
                             *
                             * [피어 생성 및 Offer 생성/세팅/전송]
                             */
                            JSONArray jsonArray = new JSONArray(message.optString("userList"));

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject userObj = (JSONObject) jsonArray.get(i);
                                UserModel userModel = new UserModel(userObj.optString("clientId"));

                                String profileImgPathsStr = userObj.optString("profileImgPaths");
                                List<ProfileMap> profileImgPaths = gson.fromJson(profileImgPathsStr, new TypeToken<ArrayList<ProfileMap>>(){}.getType());
                                String imgPath = null;
                                if(profileImgPaths != null && profileImgPaths.size() > 0){
                                    profileImgPaths = profileImgPaths.stream().filter(profileMap -> profileMap.getType().equals(CustomDialog.Type.PROFILE_IMAGE.name())).collect(Collectors.toList());
                                    imgPath = profileImgPaths.get(0).getProfileImgPath();
                                }

                                boolean isHost = userObj.getBoolean("isHost");

                                userModel.setImgPath(imgPath);
                                userModel.setName(userObj.optString("name"));
                                userModel.setEmail(userObj.optString("id"));
                                userModel.setHost(isHost);

                                if (!isExistUser(userModel.clientId)) {
                                    // 새로운 유저 추가
                                    Log.d(TAG, "[USER]" + userModel.clientId + "추가");
                                    userList.add(userModel);
                                }

                                /**
                                 * [Video Peer]
                                 * 새롭게 들어온 유저(initiator)가 자기 자신인 유저가 피어를 생성하고 Offer를 전송한다.
                                 * 자기 자신에 대한 피어는 필요 없으니 제외
                                 */
                                if (initiator.equals(socketId)) {
                                    // 나 자신은 제외
                                    if (userModel.clientId.equals(socketId)) continue;

                                    // 해당 피어 없을 때만 생성
                                    CustomPeerConnection customPeerConnection = getCustomPeerConnection(userModel.clientId, Common.VIDEO);
                                    if (customPeerConnection == null) {
                                        createPeerAndDoOffer(userModel.clientId, Common.VIDEO);
                                    }
                                }
                            }

                            /**
                             * [Screen Peer]
                             * 공유가 시작된 이후에 들어 오는 유저들은
                             * 공유하고 있는 피어 1개만 생성한다.
                             *
                             * 공유자는 제외한다.
                             *
                             * initiator 만 생성한다.
                             */
                            if (initiator.equals(socketId)) {
                                // shareId 없으면 제외
                                if ("".equals(shareId)) return;

                                // 화면 공유자 제외
                                if (shareId.equals(socketId)) return;

                                // 이미 surfaceView 있으면 제외
                                ProxySink proxyScreenSinks = getProxySink(shareId, Common.SCREEN);
                                if (proxyScreenSinks != null) return;

                                // 이미 화면 peer 가 존재 한다면 제외
                                CustomPeerConnection customPeerConnection = getCustomPeerConnection(shareId, Common.SCREEN);
                                if (customPeerConnection != null) return;

                                createPeerAndDoOffer(shareId, Common.SCREEN);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, e.getMessage());
                        }
                    /**
                     * [offer 받음]
                     */
                    } else if (type.equals("offer")) {
                        String tagFlag = getTagFlagString(senderId, peerType);

                        Log.d(TAG, tagFlag + "offer 받음");
                        CustomPeerConnection customPeerConnection = getCustomPeerConnection(senderId, peerType);
                        if (customPeerConnection == null && Common.VIDEO.equals(peerType)) {
                            Log.d(TAG, "[peer:" + senderId + "]" + "피어가 없으므로 피어 생성하자");

                            // 피어 생성 및 answer 작업
                            createPeerAndDoAnswer(senderId, peerType, message.getString("sdp"));
                        }

                        /**
                         * [Offer 를 받았 을때 화면 Peer 생성 하는 경우]
                         * 1. SenderId 가 ShareId 이면 생성
                         * 2. Socket.id 가 ShareId 이면 생성
                         *
                         * => SenderId 가 ShareId 가 아니고 Socket.Id 가 ShareId 가 아니면 제외
                         */
                        if (!"".equals(shareId) && Common.SCREEN.equals(peerType)) {

                            // SenderId 가 ShareId 가 아니고 Socket.Id 가 ShareId 가 아니면 제외
                            if (!senderId.equals(shareId) && !socketId.equals(shareId)) return;

                            // 이미 surfaceView 있으면 제외
                            ProxySink proxySink = getProxySink(shareId, peerType);
                            if (proxySink != null) return;

                            // 피어 생성 및 answer 작업
                            createPeerAndDoAnswer(senderId, peerType, message.getString("sdp"));

                        }

                    /**
                     * [answer 받음]
                     */
                    } else if (type.equals("answer")) {
                        String tagFlag = getTagFlagString(senderId, peerType);

                        Log.d(TAG, tagFlag + "answer 받음");

                        CustomPeerConnection customPeerConnection = getCustomPeerConnection(senderId, peerType);

                        if (customPeerConnection == null) {
                            Log.d(TAG, tagFlag + "피어가 없어서 answer를 세팅할 수 었습니다.");
                            return;
                        }

                        // 원격 Description 설정
                        customPeerConnection.getPeerConnection().setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(ANSWER, message.getString("sdp")));

                        Log.d(TAG, tagFlag + "answer remote 설정 완료");

                    /**
                     * [candidate 받음]
                     */
                    } else if (type.equals("candidate")) {
                        String tagFlag = getTagFlagString(senderId, peerType);

                        IceCandidate candidate = new IceCandidate(message.getString("id"), message.getInt("label"), message.getString("candidate"));

                        Log.d(TAG, tagFlag + "ICE Candidate 받음");

                        CustomPeerConnection customPeerConnection = getCustomPeerConnection(senderId, peerType);

                        if (customPeerConnection == null) {
                            Log.d(TAG, tagFlag + "아직 피어가 없으니 candidate를 queue에 넣자");

                            CustomQueue customQueue = getCustomQueue(senderId, peerType);
                            if (customQueue == null) {
                                customQueue = new CustomQueue(senderId, peerType);
                                queueList.add(customQueue);
                            }

                            Queue<IceCandidate> iceQueue = customQueue.getQueue();
                            iceQueue.offer(candidate);
                            return;
                        }

                        customPeerConnection.getPeerConnection().addIceCandidate(candidate);
                        Log.d(TAG, tagFlag + "ICE 피어에 추가 완료!!");

                    /**
                     * [share 받음]
                     */
                    } else if ("share".equals(type)) {
                        if ("".equals(shareStatus) || shareStatus == null) return;

                        if ("start".equals(shareStatus)) {
                            // 스크린 공유 시작
                            screenSharingStart();
                        } else if ("stop".equals(shareStatus)) {
                            stopScreenCapture();
                        } else if ("ing".equals(shareStatus)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MeetingRoomActivity.this, "이미 공유중입니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    /**
                     * [whiteboard]
                     */
                    } else if ("whiteboard".equals(type)) {
                        if ("".equals(whiteboardStatus) || whiteboardStatus == null) return;

                        if ("start".equals(whiteboardStatus)) {
                            // 화이트보드 공유 시작
                            startWhiteboard(whiteboardId);
                        } else if ("stop".equals(whiteboardStatus)) {
                            // 화이트보드 공유 스탑
                            stopWhiteboard();
                        } else if ("ing".equals(whiteboardStatus)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MeetingRoomActivity.this, "이미 화이트보드가 존재합니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
            }).on(EVENT_DISCONNECT, args -> {
                Log.d(TAG, "[socket:" + socketId + "]" + "소켓 연결이 해제 됐습니다.");
            });

            /**
             * 소켓 연결 요청
             */
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    private void initDrawingView(ColorModel colorModel) {
        if (drawingView == null) {
            /**
             * 화이트보드 SurfaceViewRenderer 생성
             */
            drawingView = new DrawingView(this, null, colorModel);

            /**
             * [레이아웃 파라미터 생성]
             * SurfaceView의 길이, 높이 설정
             */
//            final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
//            final int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
//            drawingView.setLayoutParams(layoutParams); // 레이아웃 파라미터 적용

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    whiteboardBinding.whiteboardFrameLayout.addView(drawingView);
                }
            });
        }
    }

    private void startWhiteboard(String whiteboardId) {
        if (whiteboardFragment != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (drawingView == null) {
                        drawingView = new DrawingView(MeetingRoomActivity.this, null, whiteboardFragment.getColorModel());
                        whiteboardBinding.whiteboardFrameLayout.addView(drawingView);
                    }
                    replaceFragment(whiteboardFragment);
                }
            });
        }
    }

    private void stopWhiteboard() {
        whiteboardId = null;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                replaceFragment(peersFragment);
                whiteboardBinding.whiteboardFrameLayout.removeView(drawingView);
                drawingView = null;
            }
        });
    }

    private void createPeerAndDoOffer(String clientId, String type) {
        createPeerProcess(clientId, type, new CreateCustomPeerConnResult() {
            @Override
            public void onCreated(CustomPeerConnection customPeerConnection) {
                // 스트림 시작
                startStreamingVideo(customPeerConnection);

                // Offer 세팅 및 보내기
                doOffer(customPeerConnection);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void createPeerAndDoAnswer(String clientId, String type, String sdp) {
        createPeerProcess(clientId, type, new CreateCustomPeerConnResult() {
            @Override
            public void onCreated(CustomPeerConnection customPeerConnection) {
                try {
                    String tagFlag = getTagFlagString(customPeerConnection.getClientId(), customPeerConnection.getType());

                    // 원격 Description 설정
                    customPeerConnection.getPeerConnection().setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, sdp));
                    Log.d(TAG, tagFlag + "offer remote 설정 완료]");

                    // ICE Queue 데이터가 이미 존재한다면 밀어 넣자
                    CustomQueue customQueue = getCustomQueue(customPeerConnection.getClientId(), customPeerConnection.getType());

                    if (customQueue == null) {
                        customQueue = new CustomQueue(customPeerConnection.getClientId(), customPeerConnection.getType());
                        queueList.add(customQueue);
                        Log.d(TAG, tagFlag + "ICE Queue 생성 완료");
                    }

                    Log.d(TAG, tagFlag + "queueList:" + queueList);

                    // ICE Candidate 데이터가 존재하면 모두 밀어 넣는다.
                    if (customQueue.getQueue().size() != 0) {
                        Log.d(TAG, tagFlag + "ICE Queue 에 데이터가 존배하므로, 데이터를 밀어 넣자");
                        drainQueue(customPeerConnection, customQueue);
                    }

                    // 스트림 시작
                    startStreamingVideo(customPeerConnection);

                    // Offer 세팅 및 보내기
                    doAnswer(customPeerConnection);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        });
    }


    private void createPeerProcess(String clientId, String type, CreateCustomPeerConnResult createCustomPeerConnResult) {
        // 원격 surfaceView 생성
        remoteSurfaceViewSetting(clientId, type, new CreateSurfaceViewRendererResult() {
            @Override
            public void onSuccess() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 피어 생성
                        createCustomPeerConnection(clientId, type, createCustomPeerConnResult);
                    }
                });
            }

            @Override
            public void onError() {

            }
        });
    }


    private UserModel getUserModel(String clientId) {
        for (int i = 0; i < userList.size(); i++) {
            UserModel userModel = userList.get(i);
            if (userModel.clientId.equals(clientId)) {
                return userModel;
            }
        }
        return null;
    }

    private interface SwapResult {
        void onSuccess(SurfaceViewRenderer surfaceViewRenderer);

        void onError();
    }

    private void exitPeer(String clientId) {
        Log.d(TAG, "[소켓 ID}:" + socketId);

        String tagVideoFlag = getTagFlagString(clientId, Common.VIDEO);

        // 일반 피어 닫기
        CustomPeerConnection customPeerConnection = getCustomPeerConnection(clientId, Common.VIDEO);
        if (customPeerConnection != null) {
            customPeerConnection.getPeerConnection().close();

            Log.d(TAG, tagVideoFlag + "peerConnection close");
        }

        String tagScreenFlag = getTagFlagString(clientId, Common.VIDEO);

        // 화면 피어 닫기
        CustomPeerConnection customScreenPeerConnection = getCustomPeerConnection(clientId, Common.SCREEN);
        if (customScreenPeerConnection != null) {
            customScreenPeerConnection.getPeerConnection().close();

            Log.d(TAG, tagScreenFlag + "peerConnection close");
        }

        // 해당 clientId와 일치 하는 모든 Peer를 제거한다.
        removeCustomPeerConn(clientId);
        Log.d(TAG, "peerConnections:" + peerConnections);

        // 해당 clientId와 일치 하는 모든 proxySink 제거
        List<ProxySink> proxySinkList = getProxySinkList(clientId);
        for (int i = 0; i < proxySinkList.size(); i++) {
            ProxySink proxySink = proxySinkList.get(i);
            SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) proxySink.getTarget();
            String type = proxySink.getType();

            if (surfaceViewRenderer == peersBinding.mainSurfaceView) {
                // UI Thread에서 작업이 완료 되면 Callback 동작
                exchangeSurfaceView(clientId, type, new SwapResult() {
                    /**
                     * @param pSurfaceViewRenderer : 클릭된 SurfaceView
                     */
                    @Override
                    public void onSuccess(SurfaceViewRenderer pSurfaceViewRenderer) {
                        removeSurfaceView(clientId);
                    }

                    @Override
                    public void onError() {
                        Log.d(TAG, "SurfaceView 교체 에러");
                    }
                });
            } else {
                if (surfaceViewRenderer != null) {
                    removeSurfaceView(clientId);
                }
            }
        }

    }

    private List<ProxySink> getProxySinkList(String clientId) {
        List<ProxySink> result = new ArrayList<>();
        for (int i = 0; i < proxySinks.size(); i++) {
            ProxySink proxySink = proxySinks.get(i);
            if (proxySink.getClientId().equals(clientId)) {
                result.add(proxySink);
            }
        }

        return result;
    }

    // 해당 clientId를 가진 모든 Peer를 제거한다.
    private void removeCustomPeerConn(String clientId) {
        for (int i = peerConnections.size() - 1; i >= 0; i--) {
            CustomPeerConnection customPeerConnection = peerConnections.get(i);
            if (customPeerConnection.getClientId().equals(clientId)) {
                peerConnections.remove(i);
                String tagFlag = getTagFlagString(customPeerConnection.getClientId(), customPeerConnection.getType());
                Log.d(TAG, tagFlag + "peerConnections 에서 제거");
            }
        }
    }

    private void stopScreenCapture() {
        CustomCapturer customCapturer = getCustomVideoCapturer(Common.SCREEN);

        // 화면 캡처 중지
        if (customCapturer != null) {
            VideoCapturer screenCapturer = customCapturer.getVideoCapturer();
            try {
                screenCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
            Log.d(TAG, "화면 캡처 중지");
        }

        // Screen 피어 닫기
        List<CustomPeerConnection> customPeerConnectionList = getCustomPeerConnectionFromType(Common.SCREEN);
        for (int i = 0; i < customPeerConnectionList.size(); i++) {
            CustomPeerConnection customPeerConnection = customPeerConnectionList.get(i);
            if (customPeerConnection != null) {
                customPeerConnection.getPeerConnection().close();
                String tagFlag = getTagFlagString(customPeerConnection.getClientId(), customPeerConnection.getType());
                Log.d(TAG, tagFlag + "screenPeerConnection close");

                peerConnections.remove(customPeerConnection);
                Log.d(TAG, tagFlag + "peerConnections:" + peerConnections);
            }
        }

        // 모든 Screen Proxy target 초기화
        ProxySink proxySink = getProxySinkFromType(Common.SCREEN);

        if (proxySink != null) {
            SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) proxySink.getTarget();

            if (surfaceViewRenderer == peersBinding.mainSurfaceView) {
                exchangeSurfaceView(proxySink.getClientId(), proxySink.getType(), new SwapResult() {
                    /**
                     * @param pSurfaceViewRenderer : 클릭된 SurfaceView
                     */
                    @Override
                    public void onSuccess(SurfaceViewRenderer pSurfaceViewRenderer) {
                        removeSurfaceView(proxySink.getClientId(), proxySink.getType());
                    }

                    @Override
                    public void onError() {
                        Log.d(TAG, "SurfaceView 교체 에러");
                    }
                });
            } else {
                removeSurfaceView(proxySink.getClientId(), proxySink.getType());
            }
        }

        // 회의 바인드 서비스 해제
        if (isMeetingServiceBound) {
            unbindService(meetingServiceConn);
            Log.d(TAG, "미팅 서비스 언바운드");
            isMeetingServiceBound = false;
        }

        // 회의 서비스 중지
        Intent intent = new Intent(this, MeetingService.class);
        stopService(intent);
        Log.d(TAG, "미팅 서비스 중지");
    }

    // 메인이 아닌 다른 SurfaceView 와 교체 후, 해당 SurfaceView를 삭제
    private void exchangeSurfaceView(String clientId, String type, SwapResult swapResult) {
        String tagFlag = getTagFlagString(clientId, type);

        Log.d(TAG, tagFlag + "해당 피어가 갖고 있는 SurfaceView 는 메인 View 입니다.");
        Log.d(TAG, tagFlag + "그렇기 때문에 다른 SurfaceView 로 대체 해야 합니다.");

        // 메인 view의 clientId가 아닌 ProxySink를 가져온다.
        ProxySink anotherProxySink = getAnotherProxySink(clientId);
        Log.d(TAG, "[peer:" + anotherProxySink.getClientId() + "] <=> [peer:" + clientId + "] SurfaceView 교체");

        /**
         * 프록시 교환
         * ui thread에서 동작 합니다.
         */
        SurfaceViewRenderer anotherSurfaceViewRenderer = (SurfaceViewRenderer) anotherProxySink.getTarget();
        setSwappedFeeds(anotherSurfaceViewRenderer, new SwapResult() {
            /**
             * @param surfaceViewRenderer : 클릭된 SurfaceView
             */
            @Override
            public void onSuccess(SurfaceViewRenderer surfaceViewRenderer) {
                swapResult.onSuccess(surfaceViewRenderer);
            }

            @Override
            public void onError() {
                swapResult.onError();
            }
        });
    }

    private void removeSurfaceView(String targetId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < proxySinks.size(); i++) {
                    ProxySink proxySink = proxySinks.get(i);
                    SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) proxySink.getTarget();
                    String type = proxySink.getType();
                    String tagFlag = getTagFlagString(proxySink.getClientId(), type);

                    if (proxySink.getClientId().equals(targetId)) {
                        // 레이아웃에서 제거
                        peersBinding.layout.removeView(surfaceViewRenderer);
                        Log.d(TAG, tagFlag + "surfaceView 레이아웃에서 제거");

                        // 변수에서 제거
                        proxySinks.remove(i);
                        Log.d(TAG, tagFlag + "surfaceView proxyVideoSinks에서 제거");
                    }
                }
            }
        });
    }

    private void removeSurfaceView(String targetId, String type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < proxySinks.size(); i++) {
                    ProxySink proxySink = proxySinks.get(i);
                    SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) proxySink.getTarget();
                    String tagFlag = getTagFlagString(proxySink.getClientId(), type);

                    if (proxySink.getClientId().equals(targetId) && proxySink.getType().equals(type)) {
                        // 레이아웃에서 제거
                        peersBinding.layout.removeView(surfaceViewRenderer);
                        Log.d(TAG, tagFlag + "surfaceView 레이아웃에서 제거");

                        // 변수에서 제거
                        proxySinks.remove(i);
                        Log.d(TAG, tagFlag + "surfaceView proxyVideoSinks에서 제거");
                    }
                }
            }
        });
    }

    private ProxySink getAnotherProxySink(String clientId) {

        for (int i = 0; i < proxySinks.size(); i++) {
            ProxySink proxySink = proxySinks.get(i);
            if (!clientId.equals(proxySink.getClientId())) {
                return proxySink;
            }
        }
        return null;
    }

    private void remoteSurfaceViewSetting(String clientId, String type, CreateSurfaceViewRendererResult createSurfaceViewRendererResult) {

        // 화면 공유자의 경우 화면 세팅을 하지 않는다.
        if (Common.SCREEN.equals(type) && socketId.equals(shareId)) {
            createSurfaceViewRendererResult.onSuccess();
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Log.d(TAG, "[runOnUiThread에서 스레드 네임]" + Thread.currentThread().getName());

                String tagFlag = getTagFlagString(clientId, type);

                /**
                 * 원격 SurfaceView 생성
                 */
                SurfaceViewRenderer surfaceViewRenderer = new SurfaceViewRenderer(MeetingRoomActivity.this);
                surfaceViewRenderer.init(rootEglBase.getEglBaseContext(), null);
                surfaceViewRenderer.setEnableHardwareScaler(true);
                surfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                surfaceViewRenderer.setMirror(true);

                /**
                 * [레이아웃 파라미터 생성]
                 * SurfaceView의 길이, 높이 설정
                 */
                final int height = (int) peersBinding.layout.getHeight();
                final int width = (int) height;

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
                surfaceViewRenderer.setLayoutParams(layoutParams); // 레이아웃 파라미터 적용

                surfaceViewRenderer.getHolder().addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                        Log.d(TAG, tagFlag + "surfaceCreated");
                    }

                    @Override
                    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                    }

                    @Override
                    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                        Log.d(TAG, tagFlag + "surfaceDestroyed");
                    }
                });

                // 클릭 이벤트 추가
                surfaceViewRenderer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        Toast.makeText(MeetingRoomActivity.this, clickedClientId + "화면 클릭!!",Toast.LENGTH_SHORT).show();
                        setSwappedFeeds(surfaceViewRenderer, null);
                    }
                });

                ProxySink proxySink = new ProxySink(clientId, type);
                proxySink.setTarget(surfaceViewRenderer);
                Log.d(TAG, tagFlag + "proxySink(" + proxySink + ")와 surfaceViewRenderer(" + surfaceViewRenderer + ")연동");

                // 레이아웃에 추가
                ViewGroup layout = peersBinding.layout;
                layout.addView(surfaceViewRenderer);

                proxySinks.add(proxySink);
                Log.d(TAG, "proxyVideoSinks:" + proxySinks);

                /**
                 * 화면 SurfaceViewRenderer가 main이 아니라면 교체 한다.
                 */
                if (Common.SCREEN.equals(type)) {
                    if (surfaceViewRenderer != peersBinding.mainSurfaceView) {
                        Log.d(TAG, tagFlag + "메인 위치의 SurfaceView 와 스왑 합니다.");
                        setSwappedFeeds(surfaceViewRenderer, new SwapResult() {
                            @Override
                            public void onSuccess(SurfaceViewRenderer surfaceViewRenderer) {
                                Log.d(TAG, "스왑 성공");
                            }

                            @Override
                            public void onError() {
                                Log.e(TAG, "스왑중 오류 발생");
                            }
                        });
                    }
                }


                Log.d(TAG, tagFlag + " 원격 SurfaceView 생성 완료");
                createSurfaceViewRendererResult.onSuccess();
            }
        });
    }

    private void removeElementAboutUserList(List<UserModel> userList, String clientId) {
        for (int i = userList.size() - 1; i >= 0; i--) {
            if (userList.get(i).clientId.equals(clientId)) {
                userList.remove(i);
                return;
            }
        }
    }

    private int getRandomColor() {
        int red = 0;
        int blue = 0;
        int green = 0;

        red = (int) (Math.random() * 255);
        blue = (int) (Math.random() * 255);
        green = (int) (Math.random() * 255);

        return Color.rgb(red, blue, green);
    }

    private void setSwappedFeeds(SurfaceViewRenderer surfaceViewRenderer, SwapResult swapResult) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProxySink clickedProxySink = getVideoSinkFromSurfaceView(surfaceViewRenderer);
                SurfaceViewRenderer clickedTarget = (SurfaceViewRenderer) clickedProxySink.getTarget();

                // 현재 메인 SurfaceView에 어떤 clientId가 매핑 되어져 있는지 가져오기
                ProxySink mainProxySink = getMainProxyVideoSink();
                SurfaceViewRenderer mainTarget = (SurfaceViewRenderer) mainProxySink.getTarget();
                if (mainTarget == null) return;

                // 교환
                mainProxySink.setTarget(clickedTarget);
                clickedProxySink.setTarget(mainTarget);

                if (swapResult != null) swapResult.onSuccess(clickedTarget);
            }
        });
    }

    // SurfaceView 와 연동된 VideoSink 를 가져온다.
    private ProxySink getVideoSinkFromSurfaceView(SurfaceViewRenderer pSurfaceViewRenderer) {
        // 먼저 비디오 싱크에서 찾는다
        for (int i = 0; i < proxySinks.size(); i++) {
            ProxySink proxySink = proxySinks.get(i);
            SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) proxySink.getTarget();
            if (surfaceViewRenderer.equals(pSurfaceViewRenderer)) {
                return proxySink;
            }
        }
        return null;
    }

    private List<UserModel> getUserListDeepCopy() {
        List<UserModel> result = new ArrayList<>();
        for (int i = 0; i < userList.size(); i++) {
            result.add(userList.get(i));
        }
        return result;
    }

    private Boolean isExistUser(String clientId) {
        for (int i = 0; i < userList.size(); i++) {
            UserModel userModel = userList.get(i);
            if (userModel.clientId.equals(clientId)) {
                return true;
            }
        }
        return false;
    }

    //MirtDPM4
    private void doAnswer(CustomPeerConnection customPeerConnection) {
        PeerConnection peerConnection = customPeerConnection.getPeerConnection();
        String clientId = customPeerConnection.getClientId();
        String type = customPeerConnection.getType();

        String tagFlag = getTagFlagString(customPeerConnection.getClientId(), customPeerConnection.getType());
        String finalTagFlag = tagFlag;


        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, finalTagFlag + "answer 생성 완료");

                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);

                Log.d(TAG, finalTagFlag + "answer 로컬에 설정 완료");

                JSONObject message = new JSONObject();
                try {
                    message.put("type", "answer");
                    message.put("sdp", sessionDescription.description);
                    message.put("senderId", socketId);
                    String targetId = clientId;
                    message.put("targetId", targetId);
                    message.put("peerType", type);

                    Log.d(TAG, finalTagFlag + "answer 전송");
                    sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, sdpMediaConstraints);
    }

    private void doOffer(CustomPeerConnection customPeerConnection) {
        PeerConnection peerConnection = customPeerConnection.getPeerConnection();
        String clientId = customPeerConnection.getClientId();
        String type = customPeerConnection.getType();

        String tagFlag = getTagFlagString(clientId, type);

        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, tagFlag + "Offer 생성 완료");

                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                Log.d(TAG, tagFlag + "Offer 로컬 세팅 완료");

                JSONObject message = new JSONObject();

//                Log.d(TAG, "[offer] sdp:" + sessionDescription.description);

                try {
                    message.put("type", "offer");
                    message.put("sdp", sessionDescription.description);
                    message.put("senderId", socketId);
                    String targetId = clientId;
                    message.put("targetId", clientId);
                    message.put("peerType", type);

                    sendMessage(message);
                    Log.d(TAG, tagFlag + "Offer 전송 완료");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, sdpMediaConstraints);
    }

    private void sendMessage(Object message) {
        socket.emit("message", message);
    }

    private CustomSurfaceViewRenderer remoteCustomSurfaceViewRenderer;

    private void initializeMainSurfaceViews() {
        rootEglBase = EglBase.create();
        peersBinding.mainSurfaceView.init(rootEglBase.getEglBaseContext(), null);
        peersBinding.mainSurfaceView.setEnableHardwareScaler(true);
        peersBinding.mainSurfaceView.setMirror(true);

        peersBinding.mainSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                Log.d(TAG, "[Main SurfaceView] surfaceCreated");
                final int width = (int) peersBinding.mainSurfaceView.getWidth();
                final int height = (int) width;

                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(width, height);
                layoutParams.bottomToTop = R.id.layout;
                layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                peersBinding.mainSurfaceView.setLayoutParams(layoutParams); // 레이아웃 파라미터 적용

//                createSurfaceViewRendererResult.onSuccess();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
//                Log.d(TAG, "surfaceChanged:");
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                Log.d(TAG, "[Main SurfaceView] surfaceDestroyed");
            }
        });

        /** 외부 SurfaceView 생성 */
        remoteCustomSurfaceViewRenderer = new CustomSurfaceViewRenderer(MeetingRoomActivity.this);
        remoteCustomSurfaceViewRenderer.init(rootEglBase.getEglBaseContext(), null);
        remoteCustomSurfaceViewRenderer.setEnableHardwareScaler(true);
        remoteCustomSurfaceViewRenderer.setMirror(true);
    }

    private void initializePeerConnectionFactory() {
        final String fieldTrials = getFieldTrials();

        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(getApplicationContext())
                        .setFieldTrials(fieldTrials)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions());

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();

        final AudioDeviceModule adm = JavaAudioDeviceModule.builder(getApplicationContext())
//                .setSamplesReadyCallback(false)
                .setUseHardwareAcousticEchoCanceler(true)
                .setUseHardwareNoiseSuppressor(true)
                .createAudioDeviceModule();

        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;

        encoderFactory = new DefaultVideoEncoderFactory(
                rootEglBase.getEglBaseContext(), true /* enableIntelVp8Encoder */, true);
        decoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());

        factory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(adm)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
    }

    private static String getFieldTrials() {
        String fieldTrials = "";
        fieldTrials += VIDEO_FLEXFEC_FIELDTRIAL;
        fieldTrials += DISABLE_WEBRTC_AGC_FIELDTRIAL;
        return fieldTrials;
    }


    private void startStreamingVideo(CustomPeerConnection customPeerConnection) {
        PeerConnection peerConnection = customPeerConnection.getPeerConnection();
        String clientId = customPeerConnection.getClientId();
        String type = customPeerConnection.getType();

        String tagFlag = getTagFlagString(clientId, type);

        if (peerConnection == null) {
            Log.d(TAG, tagFlag + "피어가 없기 때문에 스트림 연동은 하지 않습니다.");
            return;
        }

        /**
         * [화면 공유용]
         * 화면 공유를 시작한 사람만 비디오 스트림을 시작 합니다.
         */
        if (customPeerConnection.getType().equals(Common.SCREEN)) {
            if (!shareId.equals(socketId)) return;
        }

        MediaStream mediaStream = factory.createLocalMediaStream("ARDAMS");

        VideoTrack localVideoTrack = getCustomTrack(type).getVideoTrack();
        AudioTrack localAudioTrack = getCustomTrack(type).getAudioTrack();

        if (localVideoTrack != null) mediaStream.addTrack(localVideoTrack);
        if (localAudioTrack != null) mediaStream.addTrack(localAudioTrack);


        peerConnection.addStream(mediaStream);
        Log.d(TAG, tagFlag + "해당 피어에 로컬 stream 연동 완료");
    }

    interface CreateSurfaceViewRendererResult {
        void onSuccess();

        void onError();
    }

    private interface CreateCustomPeerConnResult {
        void onCreated(CustomPeerConnection customPeerConnection);

        void onError(Exception e);
    }

    private CustomPeerConnection createPeerConnection(PeerConnectionFactory factory, String clientId, String type, CreateCustomPeerConnResult createCustomPeerConnResult) {
        CustomPeerConnection customPeerConnection = null;
        try {
            String tagFlag = getTagFlagString(clientId, type);

            customPeerConnection = new CustomPeerConnection(factory, clientId, type, new CustomPeerConnection.Observer() {
                @Override
                public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                    Log.d(TAG, tagFlag + "onSignalingChange: " + signalingState);
                }

                @Override
                public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState, CustomPeerConnection pCustomPeerConnection) {
                    Log.d(TAG, tagFlag + "onIceConnectionChange: " + iceConnectionState);

//                    if(iceConnectionState == PeerConnection.IceConnectionState.FAILED){
//                        Log.d(TAG, tagFlag + " Offer를 생성/세팅/보내서 다시 연결 하자!!!");
//                        /**
//                         * 실해 했을 경우 다시
//                         * 피어를 생성한 소켓이 Offer를 생성한다.
//                         */
//                        doOffer(pCustomPeerConnection);
//                    }
                }

                @Override
                public void onIceConnectionReceivingChange(boolean b) {
                    Log.d(TAG, tagFlag + "onIceConnectionReceivingChange: ");
                }

                @Override
                public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                    Log.d(TAG, tagFlag + "onIceGatheringChange:" + iceGatheringState);
                }

                @Override
                public void onIceCandidate(IceCandidate iceCandidate) {
                    JSONObject message = new JSONObject();
                    try {
                        message.put("type", "candidate");
                        message.put("label", iceCandidate.sdpMLineIndex);
                        message.put("id", iceCandidate.sdpMid);
                        message.put("candidate", iceCandidate.sdp);
                        message.put("senderId", socketId);
                        message.put("targetId", clientId);
                        message.put("peerType", type);

                        Log.d(TAG, tagFlag + "onIceCandidate: sending candidate " + message);
                        sendMessage(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                    Log.d(TAG, tagFlag + "onIceCandidatesRemoved: ");
                }

                @Override
                public void onAddStream(MediaStream mediaStream) {
                    Log.d(TAG, tagFlag + "onAddStream: " + mediaStream.videoTracks.size());
                    Log.d(TAG, tagFlag + "[mediaStream] mediaStream:" + mediaStream);

                    VideoTrack remoteVideoTrack = mediaStream.videoTracks.size() != 0 ? mediaStream.videoTracks.get(0) : null;
                    AudioTrack remoteAudioTrack = mediaStream.audioTracks.size() != 0 ? mediaStream.audioTracks.get(0) : null;
                    if (remoteAudioTrack != null) remoteAudioTrack.setEnabled(true);
                    if (remoteVideoTrack != null) remoteVideoTrack.setEnabled(true);

                    bindTrackAndView(clientId, type, null, remoteVideoTrack);

                }

                @Override
                public void onRemoveStream(MediaStream var1) {
                    Log.d(TAG, tagFlag + "onRemoveStream: ");
                }

                @Override
                public void onDataChannel(DataChannel dc) {
                    Log.d(TAG, tagFlag + "onDataChannel: ");
                    Log.d(TAG, "New Data channel " + dc.label());

                    /** 데이터 받을 때 사용하는 데이터 채널 */
                    dc.registerObserver(new DataChannel.Observer() {
                        @Override
                        public void onBufferedAmountChange(long previousAmount) {
                            Log.d(TAG, "Data channel buffered amount changed: " + dc.label() + ": " + dc.state());
                        }

                        @Override
                        public void onStateChange() {
                            Log.d(TAG, "Data channel state changed: " + dc.label() + ": " + dc.state());

                            if (dc.state() == DataChannel.State.OPEN) {
                                // 화이트 보드 공유중이라면, 해당 프레그먼트를 활성화 시키고, 이미지 요청을 한다.
                                if (!"".equals(whiteboardId) && whiteboardId != null) {
                                    replaceFragment(whiteboardFragment);

                                    if (whiteboardId.equals(clientId)) {
                                        CustomPeerConnection whiteBoardHostPeer = getCustomPeerConnection(whiteboardId, Common.VIDEO);
                                        if (whiteBoardHostPeer != null) {
                                            Log.d(TAG, "img_req 요청!");
                                            String cmd = "img_req";
                                            ByteBuffer buffer = ByteBuffer.wrap(cmd.getBytes());
                                            whiteBoardHostPeer.getDataChannel().send(new DataChannel.Buffer(buffer, false));
                                        }
                                    }
                                }
                            }

                        }

                        /**
                         * WebRTC 데이터 채널 데이터 수신
                         */
                        @Override
                        public void onMessage(final DataChannel.Buffer buffer) {
                            Log.d(TAG, "buffer" + buffer);
                            /** 데이터가 바이너리 일 때 */
                            if (buffer.binary) {
                                Log.d(TAG, "Received binary msg over " + dc);

                                /** ByteBuffer 를 byte[] 로 넘기기 */
                                ByteBuffer data = buffer.data;
                                final byte[] bytes = new byte[data.capacity()];
                                data.get(bytes);

                                /** byte[] 로 부터 Bitmap 가져오기 */
                                Bitmap bitmap = Common.byteArrayToBitmap(bytes);
                                drawingView.setBitmap(bitmap);
                                return;
                            }

                            /**
                             * ByteBuffer => byte[] => String 변환
                             */
                            ByteBuffer data = buffer.data;
                            final byte[] bytes = new byte[data.capacity()];
                            data.get(bytes);
                            String strData = new String(bytes, Charset.forName("UTF-8"));

                            /** ';' 로 구분하여 데이터 정보 가져오기 */
                            String[] splitData = strData.split(";");

                            /**
                             * [cmd]
                             * draw : 화이트보드에서 화면 그릴 때 정보
                             * img_req : 상대방에게 현재 화이트보드에 그려진 이미지 요청
                             * chat : 채팅
                             */
                            String cmd = splitData[0];

                            if ("draw".equals(cmd)) {
                                float x = Float.parseFloat(splitData[1]);
                                float y = Float.parseFloat(splitData[2]);

                                ColorType colorType = ColorType.valueOf(splitData[3]);
                                Integer motion = Integer.parseInt(splitData[4]);

                                Log.d(TAG, "x:" + x + ", y:" + y + ", colorType:" + colorType + ", motion:" + motion);

                                MeetingRoomActivity.this.drawingView.fireDraw(x, y, colorType, clientId, motion);
                            } else if ("img_req".equals(cmd)) {
                                Log.d(TAG, "img_req 요청을 받았다.");
                                if (drawingView != null) {
                                    Bitmap bitmap = getViewBitmap(drawingView);
//                                    Log.d(TAG, "bitmap:" + bitmap);
                                    byte[] bitmapData = Common.bitmapToByteArray(bitmap);
                                    Log.d(TAG, "bitmapData:" + bitmapData);
                                    ByteBuffer bitmapByteBuffer = ByteBuffer.wrap(bitmapData);
                                    Log.d(TAG, "bitmapByteBuffer:" + bitmapByteBuffer);
                                    CustomPeerConnection customPeerConnection = getCustomPeerConnection(clientId, Common.VIDEO);
                                    customPeerConnection.getDataChannel().send(new DataChannel.Buffer(bitmapByteBuffer, true));
                                }
                                /**
                                 * 데이터 채널에서 채팅으로 통신할 때
                                 * cmd;type;msg
                                 */
                            } else if ("chat".equals(cmd)) {
                                String type = splitData[1];
                                String msg = splitData[2];
                                String date = splitData[3];
                                UserModel userModel = getUserModel(clientId);

                                if ("txt".equals(type)) {

                                    MessageModel messageModel = new MessageModel(MessageModel.MessageType.RECEIVE, msg, null, userModel, date);
                                    // 메시지 추가
                                    chatList.add(messageModel);
                                    // 리사이클러뷰 인지시키기
                                    int addIdx = messageRecyclerView.getAdapter().getItemCount() - 1;

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // 리사이클러뷰 새롭게 인지
                                            messageRecyclerView.getAdapter().notifyItemInserted(addIdx);
                                            // 스크롤 제일 아래로
                                            messageRecyclerView.getRecyclerView().scrollToPosition(messageRecyclerView.getAdapter().getItemCount() - 1);
                                        }
                                    });
                                } else if ("img".equals(type)) {
                                    Log.d(TAG, "[데이터 채널] 이미지 받음");
                                    String imagePath = msg;

                                    Log.d(TAG, "imagePath:" + imagePath);

                                    MessageModel messageModel = new MessageModel(MessageModel.MessageType.RECEIVE, null, imagePath, userModel, date);
                                    // 메시지 추가
                                    chatList.add(messageModel);
                                    // 리사이클러뷰 인지시키기
                                    int addIdx = messageRecyclerView.getAdapter().getItemCount() - 1;

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d(TAG, "addIdx:" + addIdx);
                                            Log.d(TAG, "notifyItemInserted 동작");

                                            // 리사이클러뷰 새롭게 인지
                                            messageRecyclerView.getAdapter().notifyDataSetChanged();
                                            // 스크롤 제일 아래로
                                            messageRecyclerView.getRecyclerView().scrollToPosition(messageRecyclerView.getAdapter().getItemCount() - 1);
                                        }
                                    });
                                }
                            }
                        }
                    });
                }

                @Override
                public void onRenegotiationNeeded() {
                    Log.d(TAG, tagFlag + "onRenegotiationNeeded: ");
                }

                @Override
                public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                    Log.d(TAG, tagFlag + "[onAddTrack] rtpReceiver:" + rtpReceiver);
                    Log.d(TAG, tagFlag + "[onAddTrack] mediaStreams:" + mediaStreams);
                }
            });
            createCustomPeerConnResult.onCreated(customPeerConnection);
        } catch (Exception e) {
            createCustomPeerConnResult.onError(e);
        }

        return customPeerConnection;
    }

    private ProxySink getMainProxyVideoSink() {
        for (int i = 0; i < proxySinks.size(); i++) {
            ProxySink proxySink = proxySinks.get(i);
            SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) proxySink.getTarget();
            if (surfaceViewRenderer == peersBinding.mainSurfaceView) {
                return proxySink;
            }
        }

        return null;
    }

    // 같은 타입의 CustomVideoCapturer를 가져온다.
    private CustomCapturer getCustomVideoCapturer(String type) {
        for (int i = 0; i < capturerList.size(); i++) {
            CustomCapturer customCapturer = capturerList.get(i);
            // 타입이 같을 경우
            if (customCapturer.getType().equals(type)) {
                return customCapturer;
            }
        }
        return null;
    }

    // 같은 타입의 CustomVideoTrack를 가져온다.
    private CustomTrack getCustomTrack(String type) {
        for (int i = 0; i < trackList.size(); i++) {
            CustomTrack customTrack = trackList.get(i);
            // 타입이 같을 경우
            if (customTrack.getType().equals(type)) {
                return customTrack;
            }
        }
        return null;
    }

    // 특정 clientId, type 에 맞는 CustomPeerConnection을 가져온다.
    private CustomPeerConnection getCustomPeerConnection(String clientId, String type) {
        for (int i = 0; i < peerConnections.size(); i++) {
            CustomPeerConnection customPeerConnection = peerConnections.get(i);
            // 타입이 같을 경우
            if (customPeerConnection.getType().equals(type)
                    && customPeerConnection.getClientId().equals(clientId)) {
                return customPeerConnection;
            }
        }
        return null;
    }

    // 특정 clientId, type 에 맞는 CustomPeerConnection을 가져온다.
    private List<CustomPeerConnection> getCustomPeerConnectionFromType(String type) {
        List<CustomPeerConnection> result = new ArrayList<>();

        for (int i = 0; i < peerConnections.size(); i++) {
            CustomPeerConnection customPeerConnection = peerConnections.get(i);
            // 타입이 같을 경우
            if (customPeerConnection.getType().equals(type)) {
                result.add(customPeerConnection);
            }
        }
        return result;
    }

    // 특정 clientId, type 에 맞는 ProxyVideoSink 을 가져온다.
    private ProxySink getProxySink(String clientId, String type) {
        for (int i = 0; i < proxySinks.size(); i++) {
            ProxySink proxySink = proxySinks.get(i);
            // 타입이 같을 경우
            if (proxySink.getType().equals(type)
                    && proxySink.getClientId().equals(clientId)) {
                return proxySink;
            }
        }
        return null;
    }

    private ProxySink getProxySinkFromType(String type) {
        for (int i = 0; i < proxySinks.size(); i++) {
            ProxySink proxySink = proxySinks.get(i);
            // 타입이 같을 경우
            if (proxySink.getType().equals(type)) {
                return proxySink;
            }
        }
        return null;
    }

    // 특정 clientId, type 에 맞는 CustomQueue 을 가져온다.
    private CustomQueue getCustomQueue(String clientId, String type) {
        for (int i = 0; i < queueList.size(); i++) {
            CustomQueue customQueue = queueList.get(i);
            // 타입이 같을 경우
            if (customQueue.getType().equals(type)
                    && customQueue.getClientId().equals(clientId)) {
                return customQueue;
            }
        }
        return null;
    }

    private String getTypeTag(String type) {
        return "[" + type + "]";
    }

    // 특정 타입 disconnect
    private void disconnectSpecificType(String type) {
        try {
            String typeTag = getTypeTag(type);

            // 캡처러 중지
            for (int i = capturerList.size() - 1; i >= 0; i--) {
                CustomCapturer customCapturer = capturerList.get(i);

                // 특정 타입의 캡처러만 중지 시킨다.
                if (type.equals(customCapturer.getType())) {
                    customCapturer.getVideoCapturer().stopCapture();
                    Log.d(TAG, typeTag + "비디오 캡처 중지");
                }
            }

            // 해당 type peer close 및 초기화
            for (int i = peerConnections.size() - 1; i >= 0; i--) {
                CustomPeerConnection customPeerConnection = peerConnections.get(i);
                String tagFlag = getTagFlagString(customPeerConnection.getClientId(), customPeerConnection.getType());

                // 특정 type의 CustomPeerConnection만 close
                if (type.equals(customPeerConnection.getType())) {
                    customPeerConnection.getPeerConnection().close();
                    Log.d(TAG, tagFlag + "피어 닫기");

                    peerConnections.remove(i);
                    Log.d(TAG, tagFlag + "peerConnections 에서 제거:" + peerConnections);

                    // 화이트보드 view 피어와 싱크 맞추기
//                    if(drawingView != null && type.equals(Common.VIDEO)) drawingView.syncPeer();
                }
            }

            // 특정 CustomProxyVideoSink 초기화 및 해제
            for (int i = proxySinks.size() - 1; i >= 0; i--) {
                ProxySink proxySink = proxySinks.get(i);
                String tagFlag = getTagFlagString(proxySink.getType(), proxySink.getClientId());

                // 특정 type의 ProxyVideoSink의 target을 없앤다.
                if (type.equals(proxySink.getType())) {
                    proxySink.setTarget(null);
                    Log.d(TAG, tagFlag + "proxyVideoSink 의 target 초기화");

                    proxySinks.remove(i);
                    Log.d(TAG, tagFlag + "proxyVideoSinks 에서 제거:" + proxySinks);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void disconnect() {
        try {
            // 해당 소켓이 나갔다는 사실을 알린다.
            if (socket != null) {
                socket.emit("bye");
                Log.d(TAG, "bye 라고 소켓에 알리기");
            }

            // 비디오 disconnect
            disconnectSpecificType(Common.VIDEO);

            // 화면 캡처 disconnect
            disconnectSpecificType(Common.SCREEN);

            // 소켓을 강제로 닫습니다.
            if (socket != null) {
                socket.disconnect();
                socket = null;
                Log.d(TAG, "[socket:" + socketId + "] 소켓 닫습니다.");
            }

            // 로컬 Proxy target 초기화
//            localProxyVideoSink.setTarget(null);

            // 회의 바인드 서비스 해제
            if (isMeetingServiceBound) {
                unbindService(meetingServiceConn);
                Log.d(TAG, "미팅 서비스 언바운드");
            }

            // 테스트 바인드 서비스 해제
            if(isMediaServiceBound){
                unbindService(mediaServiceConn);
                Log.d(TAG, "미디어 서비스 언바운드");
            }

            Intent intent = new Intent(this, MeetingService.class);
            stopService(intent);
            Log.d(TAG, "미팅 서비스 중지");

            // 테스트 서비스 중지
            Intent testIntent = new Intent(this, TestService.class);
            stopService(testIntent);
            Log.d(TAG, "테스트 서비스 중지");

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }


    private WindowManager.LayoutParams params;
    /**
     * system_alert_window로 이미지 렌더링
     */
    public void toggleMainSurfaceView(boolean on){
        if(on){
            // 메인 피어를 보여주자
            windowManagerSetting();
        }else{
            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            if(remoteCustomSurfaceViewRenderer != null && remoteCustomSurfaceViewRenderer.getWindowToken() != null){
                windowManager.removeView(remoteCustomSurfaceViewRenderer);
            }
        }

    }

    private Boolean isClickBackBtn = false;

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        isClickBackBtn = true;
        Log.d(TAG, "onBackPressed");
        Log.d(TAG, "뒤로가기 동작");
        // super.onBackPressed();
    }

    private void windowManagerSetting(){
        // Add the view to the window.
        params = new WindowManager.LayoutParams(
                500,     // 가로
                500,        // 세로
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,    //  뷰가 다른 앱 위에 그려지도록 하는 윈도우 타입
                // FLAG_NOT_FOCUSABLE : 포커스를 받지 않도록 설정
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN,          // 외부 터치 가능
                PixelFormat.TRANSLUCENT);

        /** Window Display 로 Center x,y 값 추출 */
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);

        float center_x = size.x/2;
        float center_y = size.y/2;

        params.x = 0;
        params.y = 0;
//        params.gravity = Gravity.CENTER;

        ProxySink mainProxySink = getMainProxyVideoSink();
        VideoTrack mainVideoTrack = mainProxySink.getVideoTrack();

        /** 전에 연동된 비디오 싱크 제거 */
        VideoTrack beforeVideoTrack = remoteProxySink.getVideoTrack();
        beforeVideoTrack.removeSink(remoteProxySink);

        /** 메인 비디오 트랙과 연동 */
        remoteProxySink.setVideoTrack(mainVideoTrack);
        mainVideoTrack.addSink(remoteProxySink);

        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(remoteCustomSurfaceViewRenderer, params);
    }

    public DrawingView getDrawingView() {
        return drawingView;
    }

    public interface FaceListener {
        void onSuccess(Bitmap bitmap);
    }

    public float calDistance(PointF point1, PointF point2) {
        return (float) Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
    }

    // 비트맵을 파일로 변환하는 메소드
    public File saveBitmapToJpeg(Bitmap bitmap, Context context) {

        //내부저장소 캐시 경로를 받아옵니다.
        File storage = context.getCacheDir();

        //저장할 파일 이름
        String fileName = String.valueOf(System.currentTimeMillis()) + ".jpg";

        //storage 에 파일 인스턴스를 생성합니다.
        File imgFile = new File(storage, fileName);

        try {

            // 자동으로 빈 파일을 생성합니다.
            imgFile.createNewFile();

            // 파일을 쓸 수 있는 스트림을 준비합니다.
            FileOutputStream out = new FileOutputStream(imgFile);

            // compress 함수를 사용해 스트림에 비트맵을 저장합니다.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, out);
            bitmap.recycle();

            // 스트림 사용후 닫아줍니다.
            out.close();

            return imgFile;

        } catch (FileNotFoundException e) {
            Log.e("MyTag", "FileNotFoundException : " + e.getMessage());
        } catch (IOException e) {
            Log.e("MyTag", "IOException : " + e.getMessage());
        }

        return imgFile;
    }

    int MAX_HEIGHT = 500;
    int MAX_WIDTH = 500;

    // 리샘플링 값 계산 : 타겟 너비와 높이를 기준으로 2의 거듭제곱인 샘플 크기 값을 계산
    private Bitmap resizeBitmapImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int inSampleSize = 1;

        int halfHeight = 0;
        int halfWidth = 0;
        if (height > MAX_HEIGHT || width > MAX_WIDTH) {
            halfHeight = height / 2;
            halfWidth = width / 2;

            while (halfHeight / inSampleSize >= MAX_HEIGHT && halfWidth / inSampleSize >= MAX_WIDTH) {
                inSampleSize *= 2;
            }
        }

        return Bitmap.createScaledBitmap(bitmap, halfWidth / inSampleSize, halfHeight / inSampleSize, true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "[MeetingRoomActivity] onStart 동작");
        if(peersBinding != null){
            Log.d(TAG, "peersBinding.mainSurfaceView:" + peersBinding.mainSurfaceView);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "[MeetingRoomActivity] onStop 동작");

        /**
         * isMeetingServiceBound : 화면 공유 중이면 true
         */
        if(!isClickBackBtn
            && peersBinding != null
            && peersBinding.mainSurfaceView != null
            && !isMeetingServiceBound
            && !isOpenAlbum){
            toggleMainSurfaceView(true);
        }
        isClickBackBtn = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "[MeetingRoomActivity] onPause 동작");

//        for(int i=0; i<trackList.size(); i++){
//            CustomTrack customTrack = trackList.get(i);
//            if(customTrack.getType().equals(VIDEO)){
//                customTrack.getVideoTrack().setEnabled(false);
//                Log.d(TAG, "비디오 트랙 비활성화");
//                break;
//            }
//        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "[MeetingRoomActivity] onRestart 동작");
        toggleMainSurfaceView(false);

//        for(int i=0; i<trackList.size(); i++){
//            CustomTrack customTrack = trackList.get(i);
//            if(customTrack.getType().equals(VIDEO)){
//                customTrack.getVideoTrack().setEnabled(true);
//                Log.d(TAG, "비디오 트랙 비활성화");
//                break;
//            }
//        }
    }

    private void postCheckDeleteMeeting(){
        MeetingDTO meetingDTO = new MeetingDTO();
        meetingDTO.setId(Integer.parseInt(roomId));
        meetingDTO.setHost(host);

        String jsonString = gson.toJson(meetingDTO);
        RequestBody info = RequestBody.create(MediaType.parse("text/plain"), jsonString);

        /** 해당 방의 모든 QR Code 이미지가 존재한다면 데이터와 이미지 삭제 */
        Call<CommonRetrofitResponse> qrCall = RetrofitService.getInstance().getService().postPureDeleteImages(info);
        qrCall.enqueue(new Callback<CommonRetrofitResponse>() {
            @Override
            public void onResponse(Call<CommonRetrofitResponse> call, Response<CommonRetrofitResponse> response) {
                // 응답 처리
                if (response.isSuccessful()) {
                    // 방 삭제된 걸 소켓서비스(자바)로 알린다.
                    MessageDTO sendMsgDTO = new MessageDTO();
                    sendMsgDTO.setType(MessageDTO.RequestType.NOTIFY_MEETING_RESERVE_DELETED);
                    mChatService.sendMsg(sendMsgDTO);
                }
                Log.d(TAG, response.message());
            }
            @Override
            public void onFailure(Call<CommonRetrofitResponse> call, Throwable t) {
                // 오류 처리
                Log.d(TAG, t.getMessage());
            }
        });

        Call<CommonRetrofitResponse> call = RetrofitService.getInstance().getService().postCheckDeleteMeeting(meetingDTO);
        call.enqueue(new Callback<CommonRetrofitResponse>() {
            @Override
            public void onResponse(Call<CommonRetrofitResponse> call, Response<CommonRetrofitResponse> response) {
                // 응답 처리
                if (response.isSuccessful() && response.body().isResult()) {
                    // 1. 소켓으로 호스트 나갔다고 알리기
                    if(socket != null) socket.emit("host_out");

                    finish();
                }
            }

            @Override
            public void onFailure(Call<CommonRetrofitResponse> call, Throwable t) {
                // 오류 처리
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void postCurrentClientUpdate(MeetingDTO.CURRENT_CLIENT_UPDATE_TYPE currentClientUpdateType){
        MeetingDTO meetingDTO = new MeetingDTO();
        meetingDTO.setId(Integer.parseInt(roomId));
        meetingDTO.setCurrentClientUpdateType(currentClientUpdateType);

        Call<CommonRetrofitResponse> call = RetrofitService.getInstance().getService().postCurrentClientUpdate(meetingDTO);
        call.enqueue(new Callback<CommonRetrofitResponse>() {
            @Override
            public void onResponse(Call<CommonRetrofitResponse> call, Response<CommonRetrofitResponse> response) {
                // 응답 처리
                if (response.isSuccessful() && response.body().isResult()) {
                    if(currentClientUpdateType == MeetingDTO.CURRENT_CLIENT_UPDATE_TYPE.DECREASE){
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<CommonRetrofitResponse> call, Throwable t) {
                // 오류 처리
                Log.d(TAG, t.getMessage());
                if(currentClientUpdateType == MeetingDTO.CURRENT_CLIENT_UPDATE_TYPE.DECREASE){
                    finish();
                }
            }
        });
    }

    public void showUserListDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(meetingUserListDialogFragment == null){
            meetingUserListDialogFragment = new MeetingUserListDialogFragment(userList, new MeetingUserListDialogFragment.Listener() {
                @Override
                public void onClick(UserModel selectedUserModel) {
                    UserModel me = MeetingRoomActivity.this.userList.stream().filter(userModel -> userModel.getEmail().equals(Util.user.getId())).findFirst().orElse(null);

                    /** 사용자가 호스트일 경우에만 호스트 이관이 가능하다. */
                    if(me.isHost()){
                        // 이미 호스트를 선택한 경우
                        if(selectedUserModel.isHost()){
                            Toast.makeText(MeetingRoomActivity.this, "선택한 사용자는 이미 호스트입니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        /**
                         * [AlertDialog]
                         * 호스트 이관 Dialog 띄우기
                         */
                        AlertDialog.Builder builder = new AlertDialog.Builder(MeetingRoomActivity.this);
                        builder.setMessage("호스트를 " + selectedUserModel.getName() +"님으로 변경 하시겠습니까?")
                                .setTitle("확인")
                                .setIcon(R.drawable.metting_together_logo);
                        // 긍정 버튼
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 호스트 이관
                                Log.d(TAG, "[호스트 이관]" + selectedUserModel.getName() + "유저로 호스트 이관");
                                MeetingDTO meetingDTO = new MeetingDTO();
                                meetingDTO.setId(Integer.valueOf(roomId));
                                meetingDTO.setUser(Util.user);
                                meetingDTO.setHost(selectedUserModel.getEmail());

                                String jsonString = gson.toJson(meetingDTO);
                                socket.emit("host_change", jsonString);
                            }
                        });
                        // 부정 버튼
                        builder.setNegativeButton("취소", null);
                        builder.show();
                    }
                }
            });
        }
        // 프래그먼트가 생성 되어져 있고, 아직 isAdded 상태가 아닐 때만 show 시킨다
        if(meetingUserListDialogFragment != null && !meetingUserListDialogFragment.isAdded()) {
            meetingUserListDialogFragment.show(fragmentManager, "userListDialog");
        }

    }

}
