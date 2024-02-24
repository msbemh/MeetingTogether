package com.example.meetingtogether.ui.chats;

import static com.example.meetingtogether.MainActivity.TAG;
import static com.example.meetingtogether.common.Common.OTHER_USER_ID;
import static com.example.meetingtogether.common.Common.OTHER_USER_NAME;
import static com.example.meetingtogether.common.Common.ROOMID;
import static com.example.meetingtogether.common.Common.ROOM_NAME;
import static com.example.meetingtogether.common.Common.ROOM_TYPE_ID;
import static com.example.meetingtogether.common.Common.VIDEO;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.meetingtogether.MainActivity;
import com.example.meetingtogether.R;
import com.example.meetingtogether.common.CommonRecyclerView;
import com.example.meetingtogether.common.ChatMessageRecyclerView;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.ActivityChatRoomBinding;
import com.example.meetingtogether.databinding.ActivityMeetingRoomBinding;
import com.example.meetingtogether.databinding.ChatReceiveMessageRowItemBinding;
import com.example.meetingtogether.databinding.ChatSendMessageRowItemBinding;
import com.example.meetingtogether.databinding.UserRowItemBinding;
import com.example.meetingtogether.dialogs.CustomDialog;
import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.model.MessageDTO;
import com.example.meetingtogether.model.ProfileMap;
import com.example.meetingtogether.model.User;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.FileInfo;
import com.example.meetingtogether.retrofit.LocalDateTimeDeserializer;
import com.example.meetingtogether.retrofit.RetrofitResponse;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.services.ChatService;
import com.example.meetingtogether.services.TestService;
import com.example.meetingtogether.sharedPreference.SharedPreferenceRepository;
import com.example.meetingtogether.ui.meetings.CustomCapturer;
import com.example.meetingtogether.ui.meetings.CustomPeerConnection;
import com.example.meetingtogether.ui.meetings.DTO.ChatRoomListDTO;
import com.example.meetingtogether.ui.meetings.DTO.MessageModel;
import com.example.meetingtogether.ui.meetings.DTO.UserModel;
import com.example.meetingtogether.ui.meetings.MeetingRoomActivity;
import com.example.meetingtogether.ui.user.LoginActivity;
import com.example.meetingtogether.ui.user.SignUpActivity;
import com.example.meetingtogether.ui.users.ProfileActivity;
import com.example.meetingtogether.ui.users.ProfileDetailActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.webrtc.DataChannel;

import java.io.File;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomActivity extends AppCompatActivity {
    private ActivityChatRoomBinding binding;

    public static ChatService mChatService;

    private boolean isChatServiceBound = false;
    private int roomId = -1;
    private MessageDTO.RoomType roomType;
    private String friendId;
    private String friendName;
    private String roomName;
    private List<MessageDTO> messageList = new ArrayList<>();
    private ChatMessageRecyclerView chatMessageRecyclerView;
    public static ChatRoomEvent chatRoomEvent;
    private Handler chatRoomHandler;
    private Gson gson;
    private boolean isAlbum = false;
    private boolean is_already_refresh = true;
    public static int create_cnt = 0;

    /**
     * 앨범에서 이미지를 선택 했을 경우
     * Intent Callback
     */
    private ActivityResultLauncher activityAlbumResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            try {
                isAlbum = false;

                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();

                    ClipData clipData = data.getClipData();
                    Log.d(TAG, String.valueOf(clipData.getItemCount()));

                    // 선택한 이미지가 11장 이상인 경우
                    if (clipData.getItemCount() > 10) {
                        Toast.makeText(getApplicationContext(), "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show();
                        return;
                    // 선택한 이미지가 1장 이상 10장 이하인 경우
                    } else {
                        Log.d(TAG, "multiple choice");
                        // 채팅 서버로 메시지 전송
                        MessageDTO sendMsgDTO = new MessageDTO();
                        sendMsgDTO.setRoomUuid(roomId);
                        sendMsgDTO.setRoomType(roomType);
                        sendMsgDTO.setType(MessageDTO.RequestType.IMAGE);
                        sendMsgDTO.setMessage(null);
                        sendMsgDTO.setMessageTempId(UUID.randomUUID().toString());
                        sendMsgDTO.setReceiverUserId(friendId);
                        sendMsgDTO.setStatus(MessageDTO.Status.PROCESS);
                        sendMsgDTO.setMessageType(MessageDTO.MessageType.SEND);
                        String jsonString = gson.toJson(sendMsgDTO);
                        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), jsonString);

//                        mChatService.sendMsg(sendMsgDTO);

                        List<Bitmap> bitmapList = new ArrayList<>();

                        chatRoomHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ArrayList<MultipartBody.Part> files = new ArrayList<>();
                                long fileAllSize = 0;

                                for (int i = 0; i < clipData.getItemCount(); i++) {
                                    Uri imageUri = clipData.getItemAt(i).getUri();  // 선택한 이미지들의 uri를 가져온다.
                                    try {
                                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(ChatRoomActivity.this.getContentResolver(), imageUri);

                                        /** 사이즈 max 500 기준으로 리사이징 (비율 유지) */
                                        bitmap = Util.resizeBitmapImage(bitmap);

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

                                        bitmapList.add(bitmap);

                                        Log.d(TAG, "bitmap:" + bitmap);
                                        File file = Util.saveBitmapToJpeg(bitmap, ChatRoomActivity.this);

                                        fileAllSize += file.length();

                                        // MultipartBody.Part로 파일 생성
                                        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), file);
//                                        MultipartBody.Part part = MultipartBody.Part.createFormData("photos", file.getName(), requestBody);
                                        MultipartBody.Part part = MultipartBody.Part.createFormData("photo" + i, file.getName(), requestBody);
                                        files.add(part);
                                    } catch (Exception e) {
                                        Log.e(TAG, "File select error", e);
                                    }
                                }

                                /**
                                 * 파일 전체 크기 5KB 제한
                                 * fileAllSize는 바이트 단위
                                 */
                                if(fileAllSize >= 50000){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ChatRoomActivity.this, "파일 사이즈가 너무 큽니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    return;
                                }

                                /**
                                 * 파일이 존재한다면, 로컬에 먼저 보여준다.
                                 * 1. 1장이면 그대로
                                 * 2. 2장이면 반반 섞어서
                                 * 3. 3장이면 3등분
                                 * 4. 4장이면 바득판 모양으로 4등분
                                 * 5. 5장 이상이면, 바둑판 모양에 개수를 표시
                                 */
                                Bitmap combinedBitmap = combineBitmap(bitmapList);

                                sendMsgDTO.setBitmap(combinedBitmap);
                                sendMsgDTO.setBitmapList(bitmapList);

                                // 메시지 추가 및 변경 시키기
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageList.add(sendMsgDTO);
                                        recyclerviewNotify();
                                    }
                                });

                                /** files를 한번에 다 보내기 */
                                Call<MessageDTO> call = RetrofitService.getInstance().getService().postUploadImages(files, requestBody);
                                call.enqueue(new Callback<MessageDTO>() {
                                    @Override
                                    public void onResponse(Call<MessageDTO> call, Response<MessageDTO> response) {
                                        // 응답 처리
                                        if (response.isSuccessful()) {
                                            MessageDTO receiveMessageDTO = response.body();
                                            List<FileInfo> fileInfoList = receiveMessageDTO.getFileInfoList();

                                            /** Socket으로 보내자. */
                                            sendMsgDTO.setFileInfoList(fileInfoList);
                                            sendMsgDTO.setCreateDate(receiveMessageDTO.getCreateDate());
                                            sendMsgDTO.setId(receiveMessageDTO.getId());
                                            sendMsgDTO.setRoomName(roomName);
                                            mChatService.sendMsg(sendMsgDTO);
                                        }
                                        Log.d(TAG, response.message());
                                    }
                                    @Override
                                    public void onFailure(Call<MessageDTO> call, Throwable t) {
                                        // 오류 처리
                                        Log.d(TAG, t.getMessage());
                                    }
                                });
                            }
                        });
                    }
                    // 취소
                } else {
                    Toast.makeText(ChatRoomActivity.this, "취소 됐습니다.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    });

    private Bitmap combineBitmap(List<Bitmap> bitmapList){
        if(bitmapList.get(0) == null) return null;

        int combinedWidth = bitmapList.get(0).getWidth();
        int combinedHeight = bitmapList.get(0).getHeight();
        Bitmap combinedBitmap = Bitmap.createBitmap(combinedWidth, combinedHeight, bitmapList.get(0).getConfig());
        Canvas canvas = new Canvas(combinedBitmap);

        if(bitmapList.size() == 1) {
            Bitmap bitmap = bitmapList.get(0);
            canvas.drawBitmap(bitmap, 0, 0, null);
        }else if(bitmapList.size() == 2){
            Bitmap bitmapLeft = bitmapList.get(0);
            bitmapLeft = Bitmap.createScaledBitmap(bitmapLeft, bitmapLeft.getWidth() / 2, bitmapLeft.getHeight(), true);

            Bitmap bitmapRight = bitmapList.get(1);
            bitmapRight = Bitmap.createScaledBitmap(bitmapRight, bitmapRight.getWidth() / 2, bitmapRight.getHeight(), true);

            canvas.drawBitmap(bitmapLeft, 0, 0, null);
            canvas.drawBitmap(bitmapRight, combinedWidth/2, 0, null);

        }else if(bitmapList.size() == 3){
            Bitmap bitmapLeft = bitmapList.get(0);
            bitmapLeft = Bitmap.createScaledBitmap(bitmapLeft, bitmapLeft.getWidth() / 2, bitmapLeft.getHeight()/2, true);

            Bitmap bitmapRight = bitmapList.get(1);
            bitmapRight = Bitmap.createScaledBitmap(bitmapRight, bitmapRight.getWidth() / 2, bitmapRight.getHeight()/2, true);

            Bitmap bitmapBottom = bitmapList.get(2);
            bitmapBottom = Bitmap.createScaledBitmap(bitmapBottom, bitmapBottom.getWidth(), bitmapBottom.getHeight()/2, true);

            canvas.drawBitmap(bitmapLeft, 0, 0, null);
            canvas.drawBitmap(bitmapRight, combinedWidth/2, 0, null);
            canvas.drawBitmap(bitmapBottom, 0, combinedHeight/2, null);

        }else if(bitmapList.size() >= 4){
            Bitmap bitmapTopLeft = bitmapList.get(0);
            bitmapTopLeft = Bitmap.createScaledBitmap(bitmapTopLeft, bitmapTopLeft.getWidth() / 2, bitmapTopLeft.getHeight()/2, true);

            Bitmap bitmapTopRight = bitmapList.get(1);
            bitmapTopRight = Bitmap.createScaledBitmap(bitmapTopRight, bitmapTopRight.getWidth() / 2, bitmapTopRight.getHeight()/2, true);

            Bitmap bitmapBottomLeft = bitmapList.get(2);
            bitmapBottomLeft = Bitmap.createScaledBitmap(bitmapBottomLeft, bitmapBottomLeft.getWidth() / 2, bitmapBottomLeft.getHeight()/2, true);

            Bitmap bitmapBottomRight = bitmapList.get(3);
            bitmapBottomRight = Bitmap.createScaledBitmap(bitmapBottomRight, bitmapBottomRight.getWidth() / 2, bitmapBottomRight.getHeight()/2, true);

            canvas.drawBitmap(bitmapTopLeft, 0, 0, null);
            canvas.drawBitmap(bitmapTopRight, combinedWidth/2, 0, null);
            canvas.drawBitmap(bitmapBottomLeft, 0, combinedHeight/2, null);
            canvas.drawBitmap(bitmapBottomRight, combinedWidth/2, combinedHeight/2, null);
        }

//        for(Bitmap bitmap : bitmapList){
//            bitmap.recycle();
//        }
        return combinedBitmap;
    }

    /**
     * Click Item Interface
     */
    public interface ChatRoomEvent{
        void onOutRoomDateComplete();
        void onInRoomDateComplete();
    }
    public static void setChatRoomEvent(ChatRoomEvent chatRoomEvent){
        ChatRoomActivity.chatRoomEvent = chatRoomEvent;
    }

    public ServiceConnection chatServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ChatService.ChatServiceBinder binder = (ChatService.ChatServiceBinder) service;
            mChatService = binder.getService();
            isChatServiceBound = true;
            Log.d(TAG, "[채팅 룸] 바인드 받아옴");

            if(!is_already_refresh) {
                Log.d(TAG, "전에 서비스 바인딩이 되지 않아 보내지 못한 리프레쉬 요청을 보냅니다.");
                requestOtherUserMessageRefresh();
                is_already_refresh = true;
            }

            mChatService.setContext(ChatRoomActivity.this);
            mChatService.setRoomId(roomId);

            /** 바인딩 이 완료 되면 서비스와 액티비티 인터페이스 설정 */
            setChatServiceInterface();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isChatServiceBound = false;
        }
    };

    private float density;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "[채팅방] onCreate 동작");
        create_cnt++;
        Log.d(TAG, "[채팅방] create_cnt:" + create_cnt);

        HandlerThread handlerChatThread = new HandlerThread("chatRoomHandler");
        handlerChatThread.start();
        chatRoomHandler = new Handler(handlerChatThread.getLooper());

        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatRoomActivity.this.mChatService.close();
            }
        });

        // DP => PX 계산을 위한 density 구해놓기
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int dpi = displayMetrics.densityDpi;
        density = displayMetrics.density; // density에는 dip/160 값이 들어 있음.

        // 방 번호 가져오기
        roomId = getIntent().getIntExtra(ROOMID, -1);

        // 방이 없다면 항상 개인방이다.
        if(roomId == -1){
            roomType = MessageDTO.RoomType.INDIVIDUAL;
        }else{
            roomType = MessageDTO.RoomType.valueOf(getIntent().getStringExtra(ROOM_TYPE_ID));
        }

        if(roomType == MessageDTO.RoomType.INDIVIDUAL){
            friendId = getIntent().getStringExtra(OTHER_USER_ID);
            friendName = getIntent().getStringExtra(OTHER_USER_NAME);
            roomName = friendName;

        }else if(roomType == MessageDTO.RoomType.GROUP){
            friendId = getIntent().getStringExtra(OTHER_USER_ID);
            friendName = getIntent().getStringExtra(OTHER_USER_NAME);
            roomName = getIntent().getStringExtra(ROOM_NAME);;
        }

        binding.chatTitle.setText(roomName);

        binding.chatSendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = binding.messageText.getText().toString();

                // 채팅 서버로 메시지 전송
                MessageDTO sendMsgDTO = new MessageDTO();
                sendMsgDTO.setRoomUuid(roomId);
                sendMsgDTO.setRoomType(roomType);
                sendMsgDTO.setType(MessageDTO.RequestType.MESSAGE);
                sendMsgDTO.setMessage(msg);
                sendMsgDTO.setMessageTempId(UUID.randomUUID().toString());
                sendMsgDTO.setReceiverUserId(friendId);
                sendMsgDTO.setStatus(MessageDTO.Status.PROCESS);
                sendMsgDTO.setRoomName(roomName);
                sendMsgDTO.setMessageType(MessageDTO.MessageType.SEND);

                // 메시지 추가 및 변경 시키기
                messageList.add(sendMsgDTO);
                recyclerviewNotify();

                mChatService.sendMsg(sendMsgDTO);

                // 에디터 초기화
                binding.messageText.setText("");
            }
        });

        binding.imageAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        binding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.imageAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);  // 여러개 이미지를 가져올 수 있도록 세팅
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                isAlbum = true;
                activityAlbumResultLauncher.launch(intent);
            }
        });

        // 채팅 서비스와 바인딩
        Intent intent = new Intent(ChatRoomActivity.this, ChatService.class);
        bindService(intent, chatServiceConn, Context.BIND_AUTO_CREATE);

        // 리사이클러뷰
        RecyclerView recyclerView = binding.roomChatRecyclerView;

        // 리사이클러뷰 바인드
        chatMessageRecyclerView = new ChatMessageRecyclerView(new ChatMessageRecyclerView.OnBind() {
            // TODO: ViewBind 변경
            // ViewBind 연동
            @Override
            public void onBindViewListener(ChatMessageRecyclerView.MyRecyclerAdapter.ViewHolder viewHolder, View view, int viewType) {
//                Log.d(TAG, "viewHolder:" + viewHolder);
//                Log.d(TAG, "view:" + view);
                if (viewType == MessageDTO.MessageType.RECEIVE.ordinal()) {
                    viewHolder.setReceiveBinding(ChatReceiveMessageRowItemBinding.bind(view));
                } else {
                    viewHolder.setSendBinding(ChatSendMessageRowItemBinding.bind(view));
                }
            }

            // TODO: ViewBind 변경
            // 실제 View 와 데이터 연동
            @Override
            public void onBindViewHolderListener(ChatMessageRecyclerView.MyRecyclerAdapter.ViewHolder holder, int position, List<Object> payloads) {
//                Log.d(TAG, "[onBindViewHolderListener] 동작");
//                Log.d(TAG, "[position:" + position);

                MessageDTO messageDTO = messageList.get(position);
//                Log.d(TAG, "messageDTO:" + messageDTO);


                // 서버에서 자기 자신에게는 SEND
                // 서버에서 다른 사람에게는 RECEIVE
                // 받은 메시지
                if (messageDTO.getMessageType() == MessageDTO.MessageType.RECEIVE) {
                    ChatReceiveMessageRowItemBinding binding = (ChatReceiveMessageRowItemBinding) (holder.receiveBinding);

                    MessageDTO.RequestType requestType = messageDTO.getType();

                    /**
                     * payload 에서 NO_READ_CNT 가 존재한다면, 읽지 않은 메시지만! 업데이트 한다.
                     */
                    if(payloads != null && payloads.contains(MessageDTO.MessagePayload.NO_READ_CNT)){
                        if(requestType == MessageDTO.RequestType.IMAGE){
                            binding.imageNoReadCnt.setText(String.valueOf(messageDTO.getNoReadCnt()));
                            if(messageDTO.getNoReadCnt() > 0){
                                binding.imageNoReadCnt.setVisibility(View.VISIBLE);
                            }else{
                                binding.imageNoReadCnt.setVisibility(View.GONE);
                            }
                        }else if(requestType == MessageDTO.RequestType.MESSAGE){
                            binding.noReadCnt.setText(String.valueOf(messageDTO.getNoReadCnt()));
                            if(messageDTO.getNoReadCnt() > 0){
                                binding.noReadCnt.setVisibility(View.VISIBLE);
                            }else{
                                binding.noReadCnt.setVisibility(View.GONE);
                            }
                        }
                        return;
                    }

                    // 프로필 가져오기
                    String profileImgPath = messageDTO.getProfileImgPath();
                    if(profileImgPath != null){
                        chatRoomHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                RequestOptions  requestOptions = new RequestOptions().circleCrop();
                                try {
                                    Bitmap bitmap = Glide
                                            .with(ChatRoomActivity.this)
                                            /** Glide는 원본 비율을 유지한다. */
                                            .asBitmap()
                                            .load("https://webrtc-sfu.kro.kr/" + profileImgPath)
                                            .apply(requestOptions)
                                            .submit()
                                            .get();

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Glide
                                                    .with(ChatRoomActivity.this)
                                                    .load(bitmap)
                                                    /** Glide는 원본 비율을 유지한다. */
                                                    .override(100,100)
                                                    .into(binding.profile);
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e(TAG, e.getMessage());
                                }
                            }
                        });
                    }

                    // [받은] 이미지 타입
                    if (requestType == MessageDTO.RequestType.IMAGE) {
                        // 텍스트 관련
                        binding.msgContent.setVisibility(View.GONE);
                        binding.noReadCnt.setVisibility(View.GONE);
                        binding.receiveDate.setVisibility(View.GONE);

                        // 이미지 관련
                        binding.msgImage.setVisibility(View.VISIBLE);
                        binding.imageNoReadCnt.setVisibility(View.GONE);
                        binding.receiveImgDate.setVisibility(View.VISIBLE);
                        binding.imageGroupCount.setVisibility(View.GONE);

                        // 이미지 로딩 성공 시의 동작
                        int width = 400;
                        int height = 400;
                        ViewGroup.LayoutParams params = binding.msgImage.getLayoutParams();
                        params.width = width;
                        params.height = height;
                        binding.msgImage.setLayoutParams(params);

                        /** 이미지 파일이 존재하면 이미지파일을 보여주고 그렇지 않으면 텍스트를 보여주자. */
                        List<FileInfo> fileInfoList = messageDTO.getFileInfoList();
//                        Log.d(TAG, "fileInfoList:" + fileInfoList);

                        chatRoomHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                /** 이미지 파일 여러개를 병합 시킨다. */
                                List<Bitmap> bitmapList = convertFileInfoListToBitmapList(fileInfoList);
                                Bitmap combinedBitmap = combineBitmap(bitmapList);
//                                Log.d(TAG, "Glide 동작");

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 이미지 glide 를 이용하여 서버 url에 있는 이미지 비동기적으로 로드
                                        Glide
                                            .with(ChatRoomActivity.this)
                                            .load(combinedBitmap)
                                            /** Glide는 원본 비율을 유지한다. */
                                            .override(400,400)
                                            .into(binding.msgImage);
                                    }
                                });
                            }
                        });

                        // 파일 개수 표시
                        if(messageDTO.getFileInfoList() != null){
                            binding.imageGroupCount.setText(messageDTO.getFileInfoList().size() + "개");
                            binding.imageGroupCount.setVisibility(View.VISIBLE);
                        }

                        if(messageDTO.getNoReadCnt() > 0){
                            binding.imageNoReadCnt.setVisibility(View.VISIBLE);
                        }else{
                            binding.imageNoReadCnt.setVisibility(View.GONE);
                        }
                        binding.imageNoReadCnt.setText(String.valueOf(messageDTO.getNoReadCnt()));
                        binding.name.setText(messageDTO.getSenderName());

                        binding.receiveImgDate.setText(messageDTO.getCreateDate().toLocalDate().toString());
                    // [받은] 텍스트 타입
                    } else if(requestType == MessageDTO.RequestType.MESSAGE){
                        // 텍스트 관련
                        binding.msgContent.setVisibility(View.VISIBLE);
                        binding.noReadCnt.setVisibility(View.GONE);
                        binding.receiveDate.setVisibility(View.VISIBLE);

                        // 이미지 관련
                        binding.msgImage.setVisibility(View.GONE);
                        binding.imageNoReadCnt.setVisibility(View.GONE);
                        binding.receiveImgDate.setVisibility(View.GONE);
                        binding.imageGroupCount.setVisibility(View.GONE);

                        if(messageDTO.getNoReadCnt() > 0){
                            binding.noReadCnt.setVisibility(View.VISIBLE);
                        }else{
                            binding.noReadCnt.setVisibility(View.GONE);
                        }
                        binding.noReadCnt.setText(String.valueOf(messageDTO.getNoReadCnt()));
                        binding.name.setText(messageDTO.getSenderName());

                        binding.msgContent.setText(messageDTO.getMessage());
                        binding.receiveDate.setText(messageDTO.getCreateDate().toLocalDate().toString() + " " + messageDTO.getCreateDate().toLocalTime().toString());
                    }

                    binding.name.setText(messageDTO.getSenderName());
                // 보낸 메시지
                } else if(messageDTO.getMessageType() == MessageDTO.MessageType.SEND){
                    ChatSendMessageRowItemBinding binding = (ChatSendMessageRowItemBinding) (holder.sendBinding);

                    MessageDTO.RequestType requestType = messageDTO.getType();

                    /**
                     * payload 에서 NO_READ_CNT 가 존재한다면, 읽지 않은 메시지만! 업데이트 한다.
                     */
                    if(payloads != null && payloads.contains(MessageDTO.MessagePayload.NO_READ_CNT)){
                        if(requestType == MessageDTO.RequestType.IMAGE){
                            binding.imageNoReadCnt.setText(String.valueOf(messageDTO.getNoReadCnt()));
                            if(messageDTO.getNoReadCnt() > 0){
                                binding.imageNoReadCnt.setVisibility(View.VISIBLE);
                            }else{
                                binding.imageNoReadCnt.setVisibility(View.GONE);
                            }
                        }else if(requestType == MessageDTO.RequestType.MESSAGE){
                            binding.noReadCnt.setText(String.valueOf(messageDTO.getNoReadCnt()));
                            if(messageDTO.getNoReadCnt() > 0){
                                binding.noReadCnt.setVisibility(View.VISIBLE);
                            }else{
                                binding.noReadCnt.setVisibility(View.GONE);
                            }
                        }
                        return;
                    }

                    // [보낸] 이미지 타입
                    if (requestType == MessageDTO.RequestType.IMAGE) {
                        // 텍스트 관련
                        binding.msgContent.setVisibility(View.GONE);
                        binding.noReadCnt.setVisibility(View.GONE);
                        binding.msgLoading.setVisibility(View.GONE);
                        binding.sendDate.setVisibility(View.GONE);

                        // 이미지 관련
                        binding.msgImage.setVisibility(View.VISIBLE);
                        binding.imageNoReadCnt.setVisibility(View.GONE);
                        binding.sendImgDate.setVisibility(View.GONE);
                        binding.progressCircular.setVisibility(View.GONE);
                        binding.imageGroupCount.setVisibility(View.GONE);

                        // 이미지 로딩 성공 시의 동작
                        int width = 400;
                        int height = 400;
                        ViewGroup.LayoutParams params = binding.msgImage.getLayoutParams();
                        params.width = width;
                        params.height = height;
                        binding.msgImage.setLayoutParams(params);

                        // 로딩중
                        if(messageDTO.getStatus() == MessageDTO.Status.PROCESS){
                            Log.d(TAG, "PROCESS 로 동작");
                            // 파일 개수 표시
                            if(messageDTO.getBitmapList() != null){
                                binding.imageGroupCount.setText(messageDTO.getBitmapList().size() + "개");
                                binding.imageGroupCount.setVisibility(View.VISIBLE);
                            }

                            // 로딩 중일 때는 이미 로컬에 있는 병합된 bitmap을 보여준다.
                            Bitmap bitmap = messageDTO.getBitmap();
                            Glide
                                .with(ChatRoomActivity.this)
                                .load(bitmap)
                                /** Glide는 원본 비율을 유지한다. */
                                .override(400,400)
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        // 이미지 로딩 실패 시 실행되는 코드
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                        int dp = 20;
                                        int px = (int)( dp * density + 0.5 );

                                        // 이미지 로딩 성공 시의 동작
                                        int width = resource.getIntrinsicWidth() + px; // 이미지의 너비
                                        int height = resource.getIntrinsicHeight() + px; // 이미지의 높이

                                        ViewGroup.LayoutParams params = binding.progressCircular.getLayoutParams();
                                        params.width = width;
                                        params.height = height;
                                        binding.progressCircular.setLayoutParams(params);
                                        return false;
                                    }
                                })
                                .into(binding.msgImage);


                            binding.imageNoReadCnt.setVisibility(View.GONE);
                            binding.progressCircular.setVisibility(View.VISIBLE);

                        // 로딩 완료
                        }else if(messageDTO.getStatus() == MessageDTO.Status.SUCCESS){
//                            Log.d(TAG, "SUCCESS 로 동작");
                            binding.progressCircular.setVisibility(View.GONE);
                            binding.sendImgDate.setVisibility(View.VISIBLE);

                            // 파일 개수 표시
                            if(messageDTO.getFileInfoList() != null){
                                binding.imageGroupCount.setText(messageDTO.getFileInfoList().size() + "개");
                                binding.imageGroupCount.setVisibility(View.VISIBLE);
                            }

                            // 읽지 않은 메시지 개수 표시
                            binding.imageNoReadCnt.setText(String.valueOf(messageDTO.getNoReadCnt()));
                            if(messageDTO.getNoReadCnt() > 0){
                                binding.imageNoReadCnt.setVisibility(View.VISIBLE);
                            }else{
                                binding.imageNoReadCnt.setVisibility(View.GONE);
                            }

                            Bitmap bitmap = messageDTO.getBitmap();
                            if(bitmap == null){
                                chatRoomHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        List<FileInfo> fileInfoList = messageDTO.getFileInfoList();
                                        /** 이미지 파일 여러개를 병합 시킨다. */
                                        List<Bitmap> bitmapList = convertFileInfoListToBitmapList(fileInfoList);
                                        Bitmap combinedBitmap = combineBitmap(bitmapList);
//                                        Log.d(TAG, "Glide 동작");

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                // 이미지 glide 를 이용하여 서버 url에 있는 이미지 비동기적으로 로드
                                                Glide
                                                    .with(ChatRoomActivity.this)
                                                    .load(combinedBitmap)
                                                    /** Glide는 원본 비율을 유지한다. */
                                                    .override(400,400)
                                                    .into(binding.msgImage);
                                            }
                                        });
                                    }
                                });
                            }else{
                                Glide
                                    .with(ChatRoomActivity.this)
                                    .load(messageDTO.getBitmap())
                                    /** Glide는 원본 비율을 유지한다. */
                                    .override(400,400)
                                    .into(binding.msgImage);
                            }
                        // 실패
                        }else if(messageDTO.getStatus() == MessageDTO.Status.FAIL){
                            binding.progressCircular.setVisibility(View.GONE);
                            binding.imageNoReadCnt.setVisibility(View.GONE);
                        }
                    // [보낸] 텍스트 타입
                    } else if(requestType == MessageDTO.RequestType.MESSAGE){
                        binding.msgContent.setText(messageDTO.getMessage().toString());
                        // 텍스트 관련
                        binding.msgContent.setVisibility(View.VISIBLE);
                        binding.noReadCnt.setVisibility(View.GONE);
                        binding.msgLoading.setVisibility(View.GONE);
                        binding.sendDate.setVisibility(View.GONE);

                        // 이미지 관련
                        binding.msgImage.setVisibility(View.GONE);
                        binding.imageNoReadCnt.setVisibility(View.GONE);
                        binding.sendImgDate.setVisibility(View.GONE);
                        binding.progressCircular.setVisibility(View.GONE);
                        binding.imageGroupCount.setVisibility(View.GONE);

                        // 로딩중
                        if(messageDTO.getStatus() == MessageDTO.Status.PROCESS){
//                            Log.d(TAG, "PROCESS 로 동작");
                            binding.msgLoading.setVisibility(View.VISIBLE);
                            binding.noReadCnt.setVisibility(View.GONE);

                            Glide
                                .with(ChatRoomActivity.this)
                                .load(R.raw.loading)
                                /** Glide는 원본 비율을 유지한다. */
                                .override(80,80)
                                .into(binding.msgLoading);
                        // 로딩 완료
                        }else if(messageDTO.getStatus() == MessageDTO.Status.SUCCESS){
//                            Log.d(TAG, "SUCCESS 로 동작");
                            binding.msgLoading.setVisibility(View.GONE);
                            binding.sendDate.setVisibility(View.VISIBLE);

                            binding.noReadCnt.setText(String.valueOf(messageDTO.getNoReadCnt()));
                            if(messageDTO.getNoReadCnt() > 0){
                                binding.noReadCnt.setVisibility(View.VISIBLE);
                            }else{
                                binding.noReadCnt.setVisibility(View.GONE);
                            }
                        // 실패
                        }else if(messageDTO.getStatus() == MessageDTO.Status.FAIL){
                            binding.msgLoading.setVisibility(View.GONE);
                            binding.noReadCnt.setVisibility(View.GONE);
                        }
                    }

                    // 성공일 경우, 보낸 날짜 텍스트 세팅
                    if(messageDTO.getStatus() == MessageDTO.Status.SUCCESS){
                        binding.sendDate.setText(messageDTO.getCreateDate().toLocalDate().toString() + " " + messageDTO.getCreateDate().toLocalTime().toString());
                        binding.sendImgDate.setText(messageDTO.getCreateDate().toLocalDate().toString() + " " + messageDTO.getCreateDate().toLocalTime().toString());
                    }
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
        chatMessageRecyclerView.setOnItemClickListener(new ChatMessageRecyclerView.OnItemClickInterface() {
            @Override
            public void onItemClickListener(View view, int position) {
                MessageDTO messageDTO = messageList.get(position);
                Log.d(TAG, "[TEST]messageDTO:"+messageDTO);

                if(messageDTO.getFileInfoList() == null || messageDTO.getFileInfoList().size() == 0){
                    return;
                }

                String jsonString = gson.toJson(messageDTO);

                Intent intent = new Intent(ChatRoomActivity.this, ChatImgViewActivity.class);
                intent.putExtra("json", jsonString);

                startActivity(intent);
            }

            @Override
            public void onItemLongClickListener(View view, int position) {
            }
        });

        chatMessageRecyclerView.setContext(ChatRoomActivity.this);
        // TODO: 데이터 변경
        // 데이터 세팅
        chatMessageRecyclerView.setDataList(messageList);
        chatMessageRecyclerView.setRecyclerView(recyclerView);
        // TODO: row item 레이아웃 변경
        // row item 레이아웃 세팅
        chatMessageRecyclerView.setReceiveRowItem(R.layout.chat_receive_message_row_item);
        chatMessageRecyclerView.setSendRowItem(R.layout.chat_send_message_row_item);
        // 적용
        chatMessageRecyclerView.adapt();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());
        gson = gsonBuilder.create();
    }

    private List<Bitmap> convertFileInfoListToBitmapList(List<FileInfo> fileInfoList){
        List<Bitmap> bitmapList = new ArrayList<>();
        if(fileInfoList == null) return bitmapList;

        for(FileInfo fileInfo : fileInfoList){
            Bitmap bitmap = null;
            try {
                bitmap = Glide.with(ChatRoomActivity.this)
                        .asBitmap()
                        .load("https://webrtc-sfu.kro.kr/" + fileInfo.getPath())
                        .submit()
                        .get();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
            bitmapList.add(bitmap);
        }

        return bitmapList;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(roomId != -1 && mChatService != null) mChatService.setRoomId(roomId);

        if(roomId != -1 && isAlbum == false){
            renewDate("IN");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(roomId != -1 && isAlbum == false){
            renewDate("OUT");
        }
    }

    private void setChatServiceInterface(){
        mChatService.setRoomInterface(new ChatService.ChatServiceInterface() {
            @Override
            public void onReceived() {

            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onUserAdd() {
                Toast.makeText(ChatRoomActivity.this, "채팅 서버와 연결 완료",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMessage(MessageDTO messageDTO) {
                boolean isCallback = messageDTO.isCallback();

                if(roomId != -1 && messageDTO.getRoomUuid() != roomId){
                    return;
                }

                // 갱신 하라는 요청일 경우 메시지를 전부 다시 PHP에 요청하고 끝
                if(messageDTO.getType() == MessageDTO.RequestType.OTHER_USER_MSG_RENEW){
                    refreshNoReadCnt();
//                    getRoomMessage(roomId);
                    return;
                }

                // 내가 보낸 메시지가 완료 됐을 경우
                // Java Socket Server에서 자신에게 돌려주는 것은 isCallback 을 true로 준다.
                if(isCallback){

                    // 방이 새롭게 생성된 경우 방번호(메모리) 변경시키고,
                    // SharedPreferences 에도 변경해주자.
                    if(messageDTO.getResponseType() == MessageDTO.ResponseType.ROOM_CREATE_SUCCESS){
                        roomId = messageDTO.getRoomUuid();
                        String receiverUserId = messageDTO.getReceiverUserId();

                        List<Contact> friendList = SharedPreferenceRepository.getFriendList();
                        for(Contact friend : friendList){
                            if(friend.getFriendId().equals(receiverUserId)){
                                friend.setRoomId(roomId);
                                break;
                            }
                        }

                        Util.contactList = friendList;

                        // 그냥 덮어씌워서 저장시키자.
                        SharedPreferenceRepository.saveFriendList(friendList);
                    }


                    // 내가 보냈던 메시지가 완료 됐는지는 tempId로 구분한다.
                    String tempId = messageDTO.getMessageTempId();
                    // 메시지 상태를 진행 => 완료로 업데이트
                    // 메시지를 전부 뒤져서 tempId가 일치되는 녀석을 완료로 업데이트
                    for(MessageDTO memMessageDto : messageList){
                        if(memMessageDto.getMessageTempId() != null && memMessageDto.getMessageTempId().equals(tempId)){
                            memMessageDto.setStatus(MessageDTO.Status.SUCCESS);
                            memMessageDto.setId(messageDTO.getId());
                            memMessageDto.setCreateDate(messageDTO.getCreateDate());
                            memMessageDto.setNoReadCnt(messageDTO.getNoReadCnt());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    recyclerviewNotify();
                                }
                            });
                            break;
                        }
                    }
                }else{
                    // 상대방이 보낸 메시지 저장
                    messageList.add(messageDTO);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recyclerviewNotify();
                        }
                    });
                }
            }

            @Override
            public void onAlertMsg(String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder( ChatRoomActivity.this);
                        builder.setTitle("알림")
                                .setMessage(msg)
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .create()
                                .show();
                    }
                });
            }
        });
    }

    private void getRoomMessage(int roomId){
        /** 해당 방의 모든 메시지 요청 */
        Call<List<MessageDTO>> call = RetrofitService.getInstance().getService().getRoomAllMessage(roomId);
        call.enqueue(new Callback<List<MessageDTO>>() {
            @Override
            public void onResponse(Call<List<MessageDTO>> call, Response<List<MessageDTO>> response) {
                List<MessageDTO> responseMessageList = response.body();

                messageList.clear();
                messageList.addAll(responseMessageList);
                recyclerviewNotify();
            }

            @Override
            public void onFailure(Call<List<MessageDTO>> call, Throwable t) {
                // 오류 처리
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void recyclerviewNotify(){
        // 위치 제일 아래로
        binding.roomChatRecyclerView.getLayoutManager().scrollToPosition(messageList.size() -1);
        
        ChatMessageRecyclerView.MyRecyclerAdapter adapter = (ChatMessageRecyclerView.MyRecyclerAdapter) binding.roomChatRecyclerView.getAdapter();
//        adapter.updateList(messageList);
        adapter.notifyDataSetChanged();
    }

    private void refreshNoReadCnt(){
        /** 해당 방의 모든 메시지 요청 */
        Call<List<MessageDTO>> call = RetrofitService.getInstance().getService().getRoomAllMessage(roomId);
        call.enqueue(new Callback<List<MessageDTO>>() {
            @Override
            public void onResponse(Call<List<MessageDTO>> call, Response<List<MessageDTO>> response) {
                List<MessageDTO> responseMessageList = response.body();

                messageList.clear();
                messageList.addAll(responseMessageList);

                ChatMessageRecyclerView.MyRecyclerAdapter adapter = (ChatMessageRecyclerView.MyRecyclerAdapter) binding.roomChatRecyclerView.getAdapter();
                adapter.notifyItemRangeChanged(0, messageList.size(), MessageDTO.MessagePayload.NO_READ_CNT);
            }

            @Override
            public void onFailure(Call<List<MessageDTO>> call, Throwable t) {
                // 오류 처리
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void renewDate(String type){
        if("IN".equals(type)){
            Call<CommonRetrofitResponse> call = RetrofitService.getInstance().getService().postChatRenewInDate(roomId);
            call.enqueue(new Callback<CommonRetrofitResponse>() {
                @Override
                public void onResponse(Call<CommonRetrofitResponse> call, Response<CommonRetrofitResponse> response) {
                    // 메시지 받아오기
                    if(messageList == null || messageList.size() == 0) getRoomMessage(roomId);
                    // 다른 사용자 메시지 갱신하라고 요청 보내기
                    if(mChatService != null) {
                        requestOtherUserMessageRefresh();
                    }else{
                        Log.d(TAG, "현재 서비스가 바인딩 완료 되지 않아, 다른 사람들에게 리프레쉬 하라는 요청을 보내지 않습니다. 추후에 연결되면 요청합니다.");
                        is_already_refresh = false;
                    }

                    // 바깥 화면(채팅방 리스트에 inDate 완료 됐다고 알리기
                    // 그러면 해당 화면은 메시지를 그때! 다시 읽어들여야 읽지 않은 메시지 개수가 잘 나온다.
                    if(ChatRoomActivity.chatRoomEvent != null) ChatRoomActivity.chatRoomEvent.onInRoomDateComplete();
                }

                @Override
                public void onFailure(Call<CommonRetrofitResponse> call, Throwable t) {
                    // 오류 처리
                    Log.d(TAG, t.getMessage());
                }
            });
        }else if("OUT".equals(type)){
            Call<CommonRetrofitResponse> call = RetrofitService.getInstance().getService().postChatRenewOutDate(roomId);
            call.enqueue(new Callback<CommonRetrofitResponse>() {
                @Override
                public void onResponse(Call<CommonRetrofitResponse> call, Response<CommonRetrofitResponse> response) {
                    requestOtherUserMessageRefresh();

                    // 바깥 화면(채팅방 리스트에 outdate 완료 됐다고 알리기
                    // 그러면 해당 화면은 메시지를 그때! 다시 읽어들여야 읽지 않은 메시지 개수가 잘 나온다.
                    if(ChatRoomActivity.chatRoomEvent != null) ChatRoomActivity.chatRoomEvent.onOutRoomDateComplete();
                }

                @Override
                public void onFailure(Call<CommonRetrofitResponse> call, Throwable t) {
                    // 오류 처리
                    Log.d(TAG, t.getMessage());
                }
            });
        }
    }

    private void requestOtherUserMessageRefresh(){
        MessageDTO sendMsgDTO = new MessageDTO();
        sendMsgDTO.setRoomUuid(roomId);
        sendMsgDTO.setSenderId(Util.user.getId());
        sendMsgDTO.setRoomType(roomType);
        sendMsgDTO.setType(MessageDTO.RequestType.OTHER_USER_MSG_RENEW);
        sendMsgDTO.setReceiverUserId(friendId);
        sendMsgDTO.setRoomName(roomName);
        sendMsgDTO.setMessageType(MessageDTO.MessageType.SEND);

        mChatService.sendMsg(sendMsgDTO);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "[채팅방] onDestroy 동작");
        Log.d(TAG, "[채팅방] roomId : " + ChatService.roomId);

        Log.d(TAG, "[채팅방] 변하기전 create_cnt : " + create_cnt);
        /**
         * 화면 진입 후에 pending intent 로 다시 진입할 경유
         * onCreate 동작 이후에 전 activity에 대한 onDestroy가 동작 되어버린다.
         */
        if(create_cnt == 1){
            Log.d(TAG, "[채팅방] 서비스 방 번호 -1 초기화 및 인터페이스 및 언바운드");
            mChatService.setRoomId(-1);
            mChatService.setRoomInterface(null);
            if(chatServiceConn != null) unbindService(chatServiceConn);
        }

        create_cnt--;
        Log.d(TAG, "[채팅방] 변한 후 create_cnt : " + create_cnt);
    }
}