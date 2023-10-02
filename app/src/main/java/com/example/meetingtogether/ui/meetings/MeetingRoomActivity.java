package com.example.meetingtogether.ui.meetings;

import static com.example.meetingtogether.common.Common.ROOMID;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
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
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import io.socket.client.IO;
import io.socket.client.Socket;

import static io.socket.client.Socket.EVENT_CONNECT;
import static io.socket.client.Socket.EVENT_DISCONNECT;
import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;

import com.example.meetingtogether.R;
import com.example.meetingtogether.common.ColorType;
import com.example.meetingtogether.common.Common;
import com.example.meetingtogether.databinding.ActivityMeetingRoomBinding;
import com.example.meetingtogether.databinding.FragmentPeersBinding;
import com.example.meetingtogether.databinding.FragmentWhiteboardBinding;
import com.example.meetingtogether.services.MeetingService;
import com.example.meetingtogether.ui.meetings.DTO.ColorModel;
import com.example.meetingtogether.ui.meetings.fragments.PeersFragment;
import com.example.meetingtogether.ui.meetings.fragments.WhiteboardFragment;
import com.example.meetingtogether.ui.meetings.DTO.UserModel;

public class MeetingRoomActivity extends AppCompatActivity {
    private static final String TAG = "TEST";
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String SCREEN_TRACK_ID = "ARDAMSv0";
    public static final int VIDEO_RESOLUTION_WIDTH = 1280;
    public static final int VIDEO_RESOLUTION_HEIGHT = 720;
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

    private Handler handler;

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

    // 미팅 서비스
    public static MeetingService meetingService;

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

    /**
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
    private ActivityResultLauncher meetingPermissionLauncher =  registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            Log.d(TAG, ""+result.toString());

            Boolean areAllGranted = true;
            // 모든 권한에 동의 했는지 확인
            Iterator iterator = result.keySet().iterator();
            while(iterator.hasNext()){
                String permissionName = iterator.next().toString();
                boolean isAllowed = result.get(permissionName);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                    areAllGranted = areAllGranted && isAllowed;
                }else{
                    if(permissionName.equals("android.permission.FOREGROUND_SERVICE")) continue;
                    if(permissionName.equals("android.permission.POST_NOTIFICATIONS")) continue;
                }
            }

            // 모든 권한에 동의
            if(areAllGranted) {
                // 화상 채팅 연결 시작
                start();

                // 시그널링 서버와 연결
                connectToSignallingServer();
                // 모든 권한에 동의 하지 않음
            }else{
                showPermissionDialog();
            }
        }
    });

    private void showPermissionDialog(){
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
        localBuilder.setTitle("권한 설정")
                .setMessage("권한 거절로 인해 일부기능이 제한됩니다.")
                .setPositiveButton("권한 설정하러 가기", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt){
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
                    }})
                .setNegativeButton("취소하기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                        Toast.makeText(MeetingRoomActivity.this, "권한을 취소하셨습니다.",Toast.LENGTH_SHORT).show();
                    }})
                .create()
                .show();
    }

    /**
     * SYSTEM_ALERT_WINDOW 권한 체크
     */
    private ActivityResultLauncher systemPermissionLauncher =  registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(Settings.canDrawOverlays(MeetingRoomActivity.this)){
                Log.d(TAG, "SYSTEM_ALERT_WINDOW 권한 완료");
                // 여기서 화면 프로젝션 토큰을 받아오자.
                mediaProjectionManager = getSystemService(MediaProjectionManager.class);
                mediaProjectionLauncher.launch(mediaProjectionManager.createScreenCaptureIntent());
            }else{
                Log.d(TAG, "SYSTEM_ALERT_WINDOW 권한 없음");
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
                    if(!"".equals(shareId) && shareId.equals(socketId)){
                        for (int i = 0; i < userList.size(); i++){
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
            if(customCapturer == null) {
                meetingService.createCapturer(Common.SCREEN);
            }else{
                // 카메라 캡처 시작
                VideoCapturer videoCapturer = customCapturer.getVideoCapturer();
                videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);

                // 화면 캡처중 표시하기
                meetingService.enableCaptureDisplayRectangle(true);

                // [화면 공유 Peer 생성]
                if(!"".equals(shareId) && shareId.equals(socketId)){
                    for (int i = 0; i < userList.size(); i++){
                        UserModel userModel = userList.get(i);
                        createPeerAndDoOffer(userModel.clientId, Common.SCREEN);
                    }
                }
            }

            // 서비스 시작
            Intent intent = new Intent(MeetingRoomActivity.this, MeetingService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            }else{
                startService(intent);
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isMeetingServiceBound = false;
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

                    if(isMeetingServiceBound) return;

                    if(!isForegroundServiceRunning(this, MeetingService.class)){
                        // 여기서 포그라운드 서비스 실행
                        // 미팅 서비스 시작
                        Intent intent = new Intent(this, MeetingService.class);
                        bindService(intent, meetingServiceConn, Context.BIND_AUTO_CREATE);
                    }

                }
            }
    );


    private PeersFragment peersFragment;
    private WhiteboardFragment whiteboardFragment;
    private FragmentPeersBinding peersBinding;
    private FragmentWhiteboardBinding whiteboardBinding;
    private String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate 동작");
        binding = ActivityMeetingRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        roomId = getIntent().getStringExtra(ROOMID);

        /**
         * 프레그 먼트 세팅
         */
        peersFragment = PeersFragment.newInstance(new PeersFragment.CreateResultInterface() {
            @Override
            public void onCreated(FragmentPeersBinding peersBinding) {
                MeetingRoomActivity.this.peersBinding = peersBinding;
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        });
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

        // 프래그먼트 매니저 가져오기
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.mainFrameLayout, peersFragment)
                .add(R.id.mainFrameLayout, whiteboardFragment)
                .show(peersFragment)
                .hide(whiteboardFragment)
                .commit();

        HandlerThread handlerThread = new HandlerThread("WebRTCThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        meetingPermissionLauncher.launch(MEETING_PERMISSIONS);

        binding.cameraOnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoTrack videoTrack = getCustomTrack(Common.VIDEO).getVideoTrack();
                if(isVideoOn){
                    videoTrack.setEnabled(false);
                    isVideoOn = false;
                }else{
                    videoTrack.setEnabled(true);
                    isVideoOn = true;
                }
            }
        });

        binding.audioOnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioTrack audioTrack = getCustomTrack(Common.VIDEO).getAudioTrack();
                if(isAudioOn){
                    audioTrack.setEnabled(false);
                    isAudioOn = false;
                }else{
                    audioTrack.setEnabled(true);
                    isAudioOn = true;
                }
            }
        });

        binding.screenShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(socket == null){
                    Toast.makeText(MeetingRoomActivity.this, "소켓 연길이 되지 않았습니다.",Toast.LENGTH_SHORT).show();
                    return;
                }

                socket.emit("share");
            }
        });

        binding.whiteBoardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(socket == null){
                    Toast.makeText(MeetingRoomActivity.this, "소켓 연길이 되지 않았습니다.",Toast.LENGTH_SHORT).show();
                    return;
                }

                socket.emit("whiteboard");

//                Intent intent = new Intent(MeetingRoomActivity.this, WhiteboardActivity.class);
//                startActivity(intent);



//                if(socket == null){
//                    Toast.makeText(MeetingRoomActivity.this, "소켓 연길이 되지 않았습니다.",Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                socket.emit("whiteboard");
//
//                // 로컬 비디오 트랙 중지
//                videoTrackSetEnabled(false);


//                Bitmap bitmap = getViewBitmap(drawingView);
//
//                for (int i = 0; i < peerConnections.size(); i++){
//                    CustomPeerConnection customPeerConnection = peerConnections.get(i);
//                    DataChannel dataChannel = customPeerConnection.getDataChannel();
//
////                            dataChannel.send(new DataChannel.Buffer(buffer, false));
//
//                    byte[] data = Common.bitmapToByteArray(bitmap);
//
//                    ByteBuffer buffer = ByteBuffer.wrap(data);
//                    dataChannel.send(new DataChannel.Buffer(buffer, true));
//                }



//                drawingView.addFrameListener(new EglRenderer.FrameListener() {
//                    @Override
//                    public void onFrame(Bitmap bitmap) {
//                        Log.d(TAG, "bitmap:" + bitmap);
//
//
//
//                        for (int i = 0; i < peerConnections.size(); i++){
//                            CustomPeerConnection customPeerConnection = peerConnections.get(i);
//                            DataChannel dataChannel = customPeerConnection.getDataChannel();
//
////                            dataChannel.send(new DataChannel.Buffer(buffer, false));
//
//                            byte[] data = Common.bitmapToByteArray(bitmap);
//
//                            ByteBuffer buffer = ByteBuffer.wrap(data);
//                            dataChannel.send(new DataChannel.Buffer(buffer, true));
//                        }
//
//                        runOnUiThread(() -> {
//                            drawingView.removeFrameListener(this);
//                        });
//                    }
//                }, 1);


//                for (int i = 0; i < peerConnections.size(); i++){
//                    CustomPeerConnection customPeerConnection = peerConnections.get(i);
//                    DataChannel dataChannel = customPeerConnection.getDataChannel();
//
//                    String data = "hello world";
//                    ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
//                    dataChannel.send(new DataChannel.Buffer(buffer, false));
//                }
            }
        });

        binding.testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                for (int i = 0; i < peerConnections.size(); i++){
//                    CustomPeerConnection customPeerConnection = peerConnections.get(i);
//                    DataChannel dataChannel = customPeerConnection.getDataChannel();
//
//                    Bitmap bitmap = getViewBitmap(drawingView);
//
//                    byte[] data = Common.bitmapToByteArray(bitmap);
//
//                    ByteBuffer buffer = ByteBuffer.wrap(data);
//                    dataChannel.send(new DataChannel.Buffer(buffer, true));
//                }
            }
        });

//        drawingView = new DrawingView(this, null);
//        binding.frameLayout.addView(drawingView);



//        SurfaceHolder surfaceHolder = binding.drawingView.getHolder();
//
//        // 화이트보드 테스트
//        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceCreated(SurfaceHolder holder) {
//                Log.d(TAG, "surfaceCreated 동작");
//                // Surface가 생성되었을 때 호출되는 부분
//                // 그리기 작업을 수행합니다.
//                Canvas canvas = holder.lockCanvas();
//
//                Paint paint = new Paint();
//                paint.setAntiAlias(true);
//                paint.setColor(Color.WHITE);
//                paint.setStyle(Paint.Style.STROKE);
//                paint.setStrokeJoin(Paint.Join.ROUND);
//                paint.setStrokeWidth(5f);
//
//                canvas.drawCircle(100,100, 100, paint);
//                // 그리기 코드 작성
//                holder.unlockCanvasAndPost(canvas);
//            }
//
//            @Override
//            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//                Log.d(TAG, "surfaceChanged 동작");
//                // Surface의 크기나 포맷이 변경되었을 때 호출되는 부분
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder holder) {
//                Log.d(TAG, "surfaceDestroyed 동작");
//                // Surface가 파괴되기 전에 호출되는 부분
//            }
//        });
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(fragment instanceof WhiteboardFragment){
            fragmentTransaction
                    .hide(peersFragment)
                    .show(fragment)
                    .commit();
        }else {
            fragmentTransaction
                    .hide(whiteboardFragment)
                    .show(fragment)
                    .commit();
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

    private void videoTrackSetEnabled(Boolean isEnable){
        for (int i = 0; i < trackList.size(); i++){
            CustomTrack customTrack = trackList.get(i);
            VideoTrack videoTrack = customTrack.getVideoTrack();
            String tagFlag = getTypeTag(customTrack.getType());

            Log.d(TAG, tagFlag + "videoTrack 활성화 여부: " + isEnable);
            videoTrack.setEnabled(isEnable);
        }
    }

    private CustomPeerConnection createCustomPeerConnection(String clientId, String type, CreateCustomPeerConnResult createCustomPeerConnResult){
        // 자기 자신은 제외
        if(socketId.equals(clientId)) return null;

        // 이미 존재 하는 피어 제외
        CustomPeerConnection customPeerConnection = getCustomPeerConnection(clientId, type);
        if(customPeerConnection != null) return null;

        // 피어 생성
        customPeerConnection = createPeerConnection(factory, clientId, type, createCustomPeerConnResult);

        String tagFlag = getTagFlagString(customPeerConnection.getClientId(), customPeerConnection.getType());
        Log.d(TAG, tagFlag + "상대방 피어 생성 완료");

        // 피어 추가
        peerConnections.add(customPeerConnection);
        Log.d(TAG, "customPeerConnection:" + customPeerConnection);

        return customPeerConnection;
    }

    private String getTagFlagString(String clientId, String type){
        if(clientId.equals("local")) return "[local:" + clientId + "]";
        return "[" + type + " peer:" + clientId + "]";
    }

    // ICE Candidate 데이터가 존재하면 모두 밀어 넣는다.
    private void drainQueue(CustomPeerConnection customPeerConnection, CustomQueue customQueue){
        String tagFlag = getTagFlagString(customPeerConnection.getClientId(), customPeerConnection.getType());

        Queue<IceCandidate> iceQueue = customQueue.getQueue();
        while (iceQueue.size() != 0) {
            IceCandidate iceCandidate = iceQueue.poll();
            customPeerConnection.getPeerConnection().addIceCandidate(iceCandidate);
            Log.d(TAG, tagFlag + "ICE Candidate 추가 완료");
        }
    }

    // 화면 공유 시작
    private void screenSharingStart(){
        /**
         * SYSTEM_ALERT_WINDOW 권한 체크
         */
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            systemPermissionLauncher.launch(intent);
        }else{
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

        super.onDestroy();
    }

    private void start() {
//            Log.d(TAG, "[스레드 네임]" + Thread.currentThread().getName());

        // 미디어 설정 초기화
        initializeSdpMediaCon();

        // 메인 SurfaceView를 초기화 한다
        initializeMainSurfaceViews();

        // 피어 팩토리 초기화
        initializePeerConnectionFactory();

        // 캡처러 초기화
        initializeCapturer(Common.VIDEO, null);

        // 트랙 생성
        CustomTrack customTrack = createTrack(Common.VIDEO);

        // 프록시 싱크 생성
        createProxySink("local", Common.VIDEO);

        // 트랙과 view 연동
        bindTrackAndView("local", customTrack.getType(), peersBinding.mainSurfaceView, null);

        /**
         * 화이트보드 테스트 코드
         */
        // 화이트보드 테스트
//        createProxySink("local", Common.WHITE_BOARD);
//        ProxySink proxySink = getProxySink("local", Common.WHITE_BOARD);
//        String tagFlag = getTagFlagString("local", Common.WHITE_BOARD);
//        proxySink.setTarget(binding.drawingView);
//        CustomTrack customTrackTest = getCustomTrack(Common.VIDEO);
//        VideoTrack videoTrack = customTrackTest.getVideoTrack();
//        videoTrack.addSink(proxySink);


        // 화이트보드 테스트
        // 화면 렌더링 완료 후 실행할 작업을 여기에 작성하세요.
//        ViewCapturer viewCapturer = new ViewCapturer(peersBinding.mainSurfaceView, rootEglBase);
//
//        SurfaceTextureHelper surfaceTextureHelper =
//                SurfaceTextureHelper.create("VideoThread", rootEglBase.getEglBaseContext());
//
//        // 카메라에서 비디오 소스 생성
//        VideoSource videoSource = factory.createVideoSource(viewCapturer.isScreencast());
//
//        // 카메라 초기화 및 캡처 시작
//        viewCapturer.initialize(surfaceTextureHelper, MeetingRoomActivity.this, videoSource.getCapturerObserver());
//        viewCapturer.startCapture(200,200,30);
//
//        // 비디오 소스에서 비디오 트랙 추출
//        VideoTrack videoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
//        videoTrack.setEnabled(true);
//
//        ProxySink proxySink = new ProxySink("test", Common.WHITE_BOARD);
//
//        binding.drawingView.init(rootEglBase.getEglBaseContext(), null);
//        binding.drawingView.setEnableHardwareScaler(true);
//        binding.drawingView.setMirror(false);
//
//        proxySink.setTarget(binding.drawingView);
//        videoTrack.addSink(proxySink);

        // 로컬 세팅
//        ProxySink localProxySink = getProxySink("local", Common.VIDEO);
//        localProxySink.setTarget(peersBinding.mainSurfaceView);
//        videoTrack.addSink(localProxySink);


//        getCustomTrack(Common.VIDEO).getVideoTrack().addSink(proxySink);

        //테스트
//        ProxySink localProxySink = getProxySink("local", Common.VIDEO);
//        localProxySink.setTarget(binding.drawingView);
//        videoTrack.addSink(localProxySink);


        // 이후 리스너를 제거하는 것도 고려하세요.
//        binding.drawingView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    private void initializeCapturer(String type, CustomCapturer screenCapturer){
        CustomCapturer customCapturer = null;
        if(Common.VIDEO.equals(type)){
            customCapturer = new CustomCapturer(type, MeetingRoomActivity.this);
        }else if(Common.SCREEN.equals(type)){
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
        videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);

        // 오디오 소스 생성
        AudioSource audioSource = factory.createAudioSource(audioConstraints);
        customCapturer.setAudioSource(audioSource);

        capturerList.add(customCapturer);
    }

    private ProxySink createProxySink(String clientId, String type){
        ProxySink proxySink = new ProxySink(clientId, type);
        String tagFlag = getTagFlagString(clientId, type);

        Log.d(TAG, tagFlag + "프록시 싱크 생성 완료");

        proxySinks.add(proxySink);

        Log.d(TAG, tagFlag + "프록시 싱크 추가완료:" + proxySinks);

        return proxySink;
    }

    private void bindTrackAndView(String clientId, String type, SurfaceViewRenderer surfaceViewRenderer, VideoTrack remoteVideoTrack){
        ProxySink proxySink = getProxySink(clientId, type);
        String tagFlag = getTagFlagString(clientId, type);

        if(clientId.equals("local")) {
            proxySink.setTarget(surfaceViewRenderer);
            Log.d(TAG, tagFlag + "surfaceViewRenderer:" + surfaceViewRenderer);
            CustomTrack customTrack = getCustomTrack(type);
            VideoTrack videoTrack = customTrack.getVideoTrack();
            videoTrack.addSink(proxySink);
        }else{
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
                socket.emit("join", roomId);
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
                if("".equals(whiteboardSocketId) || whiteboardSocketId == null){
                    stopWhiteboard();
                }

                Log.d(TAG, "[userId:" + clientId + "] userList 에서 제거");
                Log.d(TAG, "userList:"+userList);

            }).on("created", args -> {
                Log.d(TAG, "[방 생성]");
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

                    if("".equals(whiteboardId) || whiteboardId == null){
                        whiteboardId = message.optString("whiteboardId");
                    }
                    whiteboardStatus = message.optString("whiteboardStatus");


                    /**
                     * [userList]
                     */
                    if (type.equals("userList")) {
                        try {
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
                                if (initiator.equals(socketId)){
                                    // 나 자신은 제외
                                    if(userModel.clientId.equals(socketId)) continue;

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
                            if(initiator.equals(socketId)){
                                // shareId 없으면 제외
                                if("".equals(shareId)) return;

                                // 화면 공유자 제외
                                if(shareId.equals(socketId)) return;

                                // 이미 surfaceView 있으면 제외
                                ProxySink proxyScreenSinks = getProxySink(shareId, Common.SCREEN);
                                if(proxyScreenSinks != null) return;

                                // 이미 화면 peer 가 존재 한다면 제외
                                CustomPeerConnection customPeerConnection = getCustomPeerConnection(shareId, Common.SCREEN);
                                if(customPeerConnection != null) return;

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
                        if(!"".equals(shareId) && Common.SCREEN.equals(peerType)) {

                            // SenderId 가 ShareId 가 아니고 Socket.Id 가 ShareId 가 아니면 제외
                            if(!senderId.equals(shareId) && !socketId.equals(shareId)) return;

                            // 이미 surfaceView 있으면 제외
                            ProxySink proxySink = getProxySink(shareId, peerType);
                            if(proxySink != null) return;

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
                    } else if("share".equals(type)){
                        if("".equals(shareStatus) || shareStatus == null) return;

                        if("start".equals(shareStatus)){
                            // 스크린 공유 시작
                            screenSharingStart();
                        }else if("stop".equals(shareStatus)){
                            stopScreenCapture();
                        }else if("ing".equals(shareStatus)){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MeetingRoomActivity.this, "이미 공유중입니다.",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        /**
                         * [whiteboard]
                         */
                    } else if("whiteboard".equals(type)){
                        if("".equals(whiteboardStatus) || whiteboardStatus == null) return;

                        if("start".equals(whiteboardStatus)){
                            // 화이트보드 공유 시작
                            startWhiteboard(whiteboardId);
                        }else if("stop".equals(whiteboardStatus)){
                            // 화이트보드 공유 스탑
                            stopWhiteboard();
                        }else if("ing".equals(whiteboardStatus)){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MeetingRoomActivity.this, "이미 화이트보드가 존재합니다.",Toast.LENGTH_SHORT).show();
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

    private void initDrawingView(ColorModel colorModel){
        if(drawingView == null){
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

    private void startWhiteboard(String whiteboardId){
        if(whiteboardFragment != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(drawingView == null){
                        drawingView = new DrawingView(MeetingRoomActivity.this, null, whiteboardFragment.getColorModel());
                        whiteboardBinding.whiteboardFrameLayout.addView(drawingView);
                    }
                    replaceFragment(whiteboardFragment);
                }
            });
        }
    }

    private void stopWhiteboard(){
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

    private void createPeerAndDoOffer(String clientId, String type){
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

    private void createPeerAndDoAnswer(String clientId, String type, String sdp){
        createPeerProcess(clientId, type, new CreateCustomPeerConnResult() {
            @Override
            public void onCreated(CustomPeerConnection customPeerConnection) {
                try{
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
                }catch (Exception e){
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


    private void createPeerProcess(String clientId, String type, CreateCustomPeerConnResult createCustomPeerConnResult){
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


    private UserModel getUserModel(String clientId){
        for (int i = 0; i < userList.size(); i++){
            UserModel userModel = userList.get(i);
            if(userModel.clientId.equals(clientId)){
                return userModel;
            }
        }
        return null;
    }

    private interface SwapResult{
        void onSuccess(SurfaceViewRenderer surfaceViewRenderer);
        void onError();
    }

    private void exitPeer(String clientId){
        Log.d(TAG, "[소켓 ID}:" + socketId);

        String tagVideoFlag = getTagFlagString(clientId, Common.VIDEO);

        // 일반 피어 닫기
        CustomPeerConnection customPeerConnection = getCustomPeerConnection(clientId, Common.VIDEO);
        if(customPeerConnection != null) {
            customPeerConnection.getPeerConnection().close();

            Log.d(TAG, tagVideoFlag + "peerConnection close");
        }

        String tagScreenFlag = getTagFlagString(clientId, Common.VIDEO);

        // 화면 피어 닫기
        CustomPeerConnection customScreenPeerConnection = getCustomPeerConnection(clientId, Common.SCREEN);
        if(customScreenPeerConnection != null) {
            customScreenPeerConnection.getPeerConnection().close();

            Log.d(TAG, tagScreenFlag + "peerConnection close");
        }

        // 해당 clientId와 일치 하는 모든 Peer를 제거한다.
        removeCustomPeerConn(clientId);
        Log.d(TAG, "peerConnections:"+peerConnections);

        // 해당 clientId와 일치 하는 모든 proxySink 제거
        List<ProxySink> proxySinkList = getProxySinkList(clientId);
        for (int i = 0; i < proxySinkList.size(); i++){
            ProxySink proxySink = proxySinkList.get(i);
            SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) proxySink.getTarget();
            String type = proxySink.getType();

            if(surfaceViewRenderer == peersBinding.mainSurfaceView){
                // UI Thread에서 작업이 완료 되면 Callback 동작
                exchangeSurfaceView(clientId, type, new SwapResult(){
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
            }else{
                if(surfaceViewRenderer != null){
                    removeSurfaceView(clientId);
                }
            }
        }

    }

    private List<ProxySink> getProxySinkList(String clientId){
        List<ProxySink> result = new ArrayList<>();
        for (int i = 0; i < proxySinks.size(); i++){
            ProxySink proxySink = proxySinks.get(i);
            if(proxySink.getClientId().equals(clientId)){
                result.add(proxySink);
            }
        }

        return result;
    }

    // 해당 clientId를 가진 모든 Peer를 제거한다.
    private void removeCustomPeerConn(String clientId){
        for (int i = peerConnections.size()-1; i >= 0; i--){
            CustomPeerConnection customPeerConnection = peerConnections.get(i);
            if(customPeerConnection.getClientId().equals(clientId)){
                peerConnections.remove(i);
                String tagFlag = getTagFlagString(customPeerConnection.getClientId(), customPeerConnection.getType());
                Log.d(TAG, tagFlag + "peerConnections 에서 제거");
            }
        }
    }

    private void stopScreenCapture(){
        CustomCapturer customCapturer = getCustomVideoCapturer(Common.SCREEN);

        // 화면 캡처 중지
        if(customCapturer != null){
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
        for (int i = 0; i < customPeerConnectionList.size(); i++){
            CustomPeerConnection customPeerConnection = customPeerConnectionList.get(i);
            if(customPeerConnection != null){
                customPeerConnection.getPeerConnection().close();
                String tagFlag = getTagFlagString(customPeerConnection.getClientId(), customPeerConnection.getType());
                Log.d(TAG, tagFlag + "screenPeerConnection close");

                peerConnections.remove(customPeerConnection);
                Log.d(TAG, tagFlag + "peerConnections:" + peerConnections);
            }
        }

        // 모든 Screen Proxy target 초기화
        ProxySink proxySink = getProxySinkFromType(Common.SCREEN);

        if(proxySink != null){
            SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) proxySink.getTarget();

            if(surfaceViewRenderer == peersBinding.mainSurfaceView){
                exchangeSurfaceView(proxySink.getClientId(), proxySink.getType(), new SwapResult(){
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
            }else{
                removeSurfaceView(proxySink.getClientId(), proxySink.getType());
            }
        }

        // 회의 바인드 서비스 해제
        if(isMeetingServiceBound) {
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
    private void exchangeSurfaceView(String clientId, String type, SwapResult swapResult){
        String tagFlag = getTagFlagString(clientId, type);

        Log.d(TAG, tagFlag + "해당 피어가 갖고 있는 SurfaceView 는 메인 View 입니다.");
        Log.d(TAG, tagFlag + "그렇기 때문에 다른 SurfaceView 로 대체 해야 합니다.");

        // 메인 view의 clientId가 아닌 ProxySink를 가져온다.
        ProxySink anotherProxySink = getAnotherProxySink(clientId);
        Log.d(TAG, "[peer:"+anotherProxySink.getClientId()+"] <=> [peer:" + clientId + "] SurfaceView 교체");

        /**
         * 프록시 교환
         * ui thread에서 동작 합니다.
         */
        SurfaceViewRenderer anotherSurfaceViewRenderer = (SurfaceViewRenderer) anotherProxySink.getTarget();
        setSwappedFeeds(anotherSurfaceViewRenderer, new SwapResult(){
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

    private void removeSurfaceView(String targetId){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < proxySinks.size(); i++){
                    ProxySink proxySink = proxySinks.get(i);
                    SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) proxySink.getTarget();
                    String type = proxySink.getType();
                    String tagFlag = getTagFlagString(proxySink.getClientId(), type);

                    if(proxySink.getClientId().equals(targetId)){
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

    private void removeSurfaceView(String targetId, String type){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < proxySinks.size(); i++){
                    ProxySink proxySink = proxySinks.get(i);
                    SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) proxySink.getTarget();
                    String tagFlag = getTagFlagString(proxySink.getClientId(), type);

                    if(proxySink.getClientId().equals(targetId) && proxySink.getType().equals(type)){
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

    private ProxySink getAnotherProxySink(String clientId){

        for (int i = 0; i < proxySinks.size(); i++){
            ProxySink proxySink = proxySinks.get(i);
            if(!clientId.equals(proxySink.getClientId())){
                return proxySink;
            }
        }
        return null;
    }

    private void remoteSurfaceViewSetting(String clientId, String type, CreateSurfaceViewRendererResult createSurfaceViewRendererResult){

        // 화면 공유자의 경우 화면 세팅을 하지 않는다.
        if(Common.SCREEN.equals(type) && socketId.equals(shareId)) {
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
                surfaceViewRenderer.setMirror(false);

                /**
                 * [레이아웃 파라미터 생성]
                 * SurfaceView의 길이, 높이 설정
                 */
                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
                final int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
                surfaceViewRenderer.setLayoutParams(layoutParams); // 레이아웃 파라미터 적용

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
                Log.d(TAG, "proxyVideoSinks:"+ proxySinks);

                /**
                 * 화면 SurfaceViewRenderer가 main이 아니라면 교체 한다.
                 */
                if(Common.SCREEN.equals(type)){
                    if(surfaceViewRenderer != peersBinding.mainSurfaceView){
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
                if(mainTarget == null) return;

                // 교환
                mainProxySink.setTarget(clickedTarget);
                clickedProxySink.setTarget(mainTarget);

                if(swapResult != null) swapResult.onSuccess(clickedTarget);
            }
        });
    }

    // SurfaceView 와 연동된 VideoSink 를 가져온다.
    private ProxySink getVideoSinkFromSurfaceView(SurfaceViewRenderer pSurfaceViewRenderer){
        // 먼저 비디오 싱크에서 찾는다
        for (int i = 0; i < proxySinks.size(); i++){
            ProxySink proxySink = proxySinks.get(i);
            SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) proxySink.getTarget();
            if(surfaceViewRenderer.equals(pSurfaceViewRenderer)){
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

//    private PeerConnection getPeerConnection(String clientId) {
//        return peerConnections.get(clientId).getPeerConnection();
//    }

    private void sendMessage(Object message) {
        socket.emit("message", message);
    }

    private void initializeMainSurfaceViews() {
        rootEglBase = EglBase.create();
        peersBinding.mainSurfaceView.init(rootEglBase.getEglBaseContext(), null);
        peersBinding.mainSurfaceView.setEnableHardwareScaler(true);
        peersBinding.mainSurfaceView.setMirror(false);

//        binding.drawingView.init(rootEglBase.getEglBaseContext(), null);
//        binding.drawingView.setEnableHardwareScaler(true);
//        binding.drawingView.setMirror(true);
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
        if(customPeerConnection.getType().equals(Common.SCREEN)){
            if(!shareId.equals(socketId)) return;
        }

        MediaStream mediaStream = factory.createLocalMediaStream("ARDAMS");

        VideoTrack localVideoTrack = getCustomTrack(type).getVideoTrack();
        AudioTrack localAudioTrack = getCustomTrack(type).getAudioTrack();

        if(localVideoTrack != null) mediaStream.addTrack(localVideoTrack);
        if(localAudioTrack != null) mediaStream.addTrack(localAudioTrack);



        peerConnection.addStream(mediaStream);
        Log.d(TAG, tagFlag + "해당 피어에 로컬 stream 연동 완료");
    }

    interface CreateSurfaceViewRendererResult{
        void onSuccess();
        void onError();
    }

    private interface CreateCustomPeerConnResult{
        void onCreated(CustomPeerConnection customPeerConnection);
        void onError(Exception e);
    }

    private CustomPeerConnection createPeerConnection(PeerConnectionFactory factory, String clientId, String type, CreateCustomPeerConnResult createCustomPeerConnResult) {
        CustomPeerConnection customPeerConnection = null;
        try{
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
                    if(remoteAudioTrack != null) remoteAudioTrack.setEnabled(true);
                    if(remoteVideoTrack != null) remoteVideoTrack.setEnabled(true);

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

                    dc.registerObserver(new DataChannel.Observer() {
                        @Override
                        public void onBufferedAmountChange(long previousAmount) {
                            Log.d(TAG, "Data channel buffered amount changed: " + dc.label() + ": " + dc.state());
                        }

                        @Override
                        public void onStateChange() {
                            Log.d(TAG, "Data channel state changed: " + dc.label() + ": " + dc.state());

                            if(dc.state() == DataChannel.State.OPEN){
                                // 화이트 보드 공유중이라면, 해당 프레그먼트를 활성화 시키고, 이미지 요청을 한다.
                                if(!"".equals(whiteboardId) && whiteboardId != null ){
                                    replaceFragment(whiteboardFragment);

                                    if(whiteboardId.equals(clientId)){
                                        CustomPeerConnection whiteBoardHostPeer = getCustomPeerConnection(whiteboardId, Common.VIDEO);
                                        if(whiteBoardHostPeer != null){
                                            Log.d(TAG, "img_req 요청!");
                                            String cmd = "img_req";
                                            ByteBuffer buffer = ByteBuffer.wrap(cmd.getBytes());
                                            whiteBoardHostPeer.getDataChannel().send(new DataChannel.Buffer(buffer, false));
                                        }
                                    }
                                }
                            }

                        }

                        @Override
                        public void onMessage(final DataChannel.Buffer buffer) {
                            Log.d(TAG, "buffer" + buffer);
                            if (buffer.binary) {
                                Log.d(TAG, "Received binary msg over " + dc);

                                ByteBuffer data = buffer.data;
                                final byte[] bytes = new byte[data.capacity()];
                                data.get(bytes);

                                Bitmap bitmap = Common.byteArrayToBitmap(bytes);
                                Log.d(TAG, "bitmap:" + bitmap);
                                drawingView.setBitmap(bitmap);
                                return;
                            }
                            ByteBuffer data = buffer.data;
                            final byte[] bytes = new byte[data.capacity()];
                            data.get(bytes);
                            String strData = new String(bytes, Charset.forName("UTF-8"));

                            String[] splitData = strData.split(";");

                            String cmd = splitData[0];

                            if("draw".equals(cmd)){
                                float x = Float.parseFloat(splitData[1]);
                                float y = Float.parseFloat(splitData[2]);

                                ColorType colorType = ColorType.valueOf(splitData[3]);
                                Integer motion = Integer.parseInt(splitData[4]);

//                                ColorType colorType = MeetingRoomActivity.this.drawingView.currentDrawingModel.getColorType();
//                                String colorTypeName = colorType.name();

                                Log.d(TAG, "x:" + x + ", y:" + y + ", colorType:" + colorType +  ", motion:" + motion);

                                MeetingRoomActivity.this.drawingView.fireDraw(x, y, colorType, clientId, motion);
                            }else if("img_req".equals(cmd)){
                                Log.d(TAG, "img_req 요청을 받았다.");
                                if(drawingView != null){
                                    Bitmap bitmap = getViewBitmap(drawingView);
                                    Log.d(TAG, "bitmap:" + bitmap);
                                    byte[] bitmapData = Common.bitmapToByteArray(bitmap);
                                    Log.d(TAG, "bitmapData:" + bitmapData);
                                    ByteBuffer bitmapByteBuffer = ByteBuffer.wrap(bitmapData);
                                    Log.d(TAG, "bitmapByteBuffer:" + bitmapByteBuffer);
                                    CustomPeerConnection customPeerConnection = getCustomPeerConnection(clientId, Common.VIDEO);
                                    customPeerConnection.getDataChannel().send(new DataChannel.Buffer(bitmapByteBuffer, true));
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
        }catch (Exception e){
            createCustomPeerConnResult.onError(e);
        }

        return customPeerConnection;
    }

    private ProxySink getMainProxyVideoSink(){
        for (int i = 0; i < proxySinks.size(); i++){
            ProxySink proxySink = proxySinks.get(i);
            SurfaceViewRenderer surfaceViewRenderer = (SurfaceViewRenderer) proxySink.getTarget();
            if(surfaceViewRenderer == peersBinding.mainSurfaceView){
                return proxySink;
            }
        }

        return null;
    }

    // 같은 타입의 CustomVideoCapturer를 가져온다.
    private CustomCapturer getCustomVideoCapturer(String type){
        for (int i = 0; i < capturerList.size(); i++){
            CustomCapturer customCapturer = capturerList.get(i);
            // 타입이 같을 경우
            if(customCapturer.getType().equals(type)){
                return customCapturer;
            }
        }
        return null;
    }

    // 같은 타입의 CustomVideoTrack를 가져온다.
    private CustomTrack getCustomTrack(String type){
        for (int i = 0; i < trackList.size(); i++){
            CustomTrack customTrack = trackList.get(i);
            // 타입이 같을 경우
            if(customTrack.getType().equals(type)){
                return customTrack;
            }
        }
        return null;
    }

    // 특정 clientId, type 에 맞는 CustomPeerConnection을 가져온다.
    private CustomPeerConnection getCustomPeerConnection(String clientId, String type){
        for (int i = 0; i < peerConnections.size(); i++){
            CustomPeerConnection customPeerConnection = peerConnections.get(i);
            // 타입이 같을 경우
            if(customPeerConnection.getType().equals(type)
                    && customPeerConnection.getClientId().equals(clientId)){
                return customPeerConnection;
            }
        }
        return null;
    }

    // 특정 clientId, type 에 맞는 CustomPeerConnection을 가져온다.
    private List<CustomPeerConnection> getCustomPeerConnectionFromType(String type){
        List<CustomPeerConnection> result = new ArrayList<>();

        for (int i = 0; i < peerConnections.size(); i++){
            CustomPeerConnection customPeerConnection = peerConnections.get(i);
            // 타입이 같을 경우
            if(customPeerConnection.getType().equals(type)){
                result.add(customPeerConnection);
            }
        }
        return result;
    }

    // 특정 clientId, type 에 맞는 ProxyVideoSink 을 가져온다.
    private ProxySink getProxySink(String clientId, String type){
        for (int i = 0; i < proxySinks.size(); i++){
            ProxySink proxySink = proxySinks.get(i);
            // 타입이 같을 경우
            if(proxySink.getType().equals(type)
                    && proxySink.getClientId().equals(clientId)){
                return proxySink;
            }
        }
        return null;
    }

    private ProxySink getProxySinkFromType(String type){
        for (int i = 0; i < proxySinks.size(); i++){
            ProxySink proxySink = proxySinks.get(i);
            // 타입이 같을 경우
            if(proxySink.getType().equals(type)){
                return proxySink;
            }
        }
        return null;
    }

    // 특정 clientId, type 에 맞는 CustomQueue 을 가져온다.
    private CustomQueue getCustomQueue(String clientId, String type){
        for (int i = 0; i < queueList.size(); i++){
            CustomQueue customQueue = queueList.get(i);
            // 타입이 같을 경우
            if(customQueue.getType().equals(type)
                    && customQueue.getClientId().equals(clientId)){
                return customQueue;
            }
        }
        return null;
    }

    private String getTypeTag(String type){
        return "[" + type + "]";
    }

    // 특정 타입 disconnect
    private void disconnectSpecificType(String type){
        try{
            String typeTag = getTypeTag(type);

            // 캡처러 중지
            for (int i = capturerList.size()-1; i >= 0; i--){
                CustomCapturer customCapturer = capturerList.get(i);

                // 특정 타입의 캡처러만 중지 시킨다.
                if(type.equals(customCapturer.getType())){
                    customCapturer.getVideoCapturer().stopCapture();
                    Log.d(TAG, typeTag + "비디오 캡처 중지");
                }
            }

            // 해당 type peer close 및 초기화
            for (int i = peerConnections.size()-1; i >= 0; i--){
                CustomPeerConnection customPeerConnection = peerConnections.get(i);
                String tagFlag = getTagFlagString(customPeerConnection.getClientId(), customPeerConnection.getType());

                // 특정 type의 CustomPeerConnection만 close
                if(type.equals(customPeerConnection.getType())){
                    customPeerConnection.getPeerConnection().close();
                    Log.d(TAG, tagFlag + "피어 닫기");

                    peerConnections.remove(i);
                    Log.d(TAG, tagFlag + "peerConnections 에서 제거:" + peerConnections);
                }
            }

            // 특정 CustomProxyVideoSink 초기화 및 해제
            for (int i = proxySinks.size()-1; i >= 0; i--){
                ProxySink proxySink = proxySinks.get(i);
                String tagFlag = getTagFlagString(proxySink.getType(), proxySink.getClientId());

                // 특정 type의 ProxyVideoSink의 target을 없앤다.
                if(type.equals(proxySink.getType())){
                    proxySink.setTarget(null);
                    Log.d(TAG, tagFlag + "proxyVideoSink 의 target 초기화");

                    proxySinks.remove(i);
                    Log.d(TAG, tagFlag + "proxyVideoSinks 에서 제거:" + proxySinks);
                }
            }

        }catch (Exception e){
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void disconnect() {
        try {
            // 해당 소켓이 나갔다는 사실을 알린다.
            if(socket != null){
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
            if(isMeetingServiceBound) {
                unbindService(meetingServiceConn);
                Log.d(TAG, "미팅 서비스 언바운드");
            }

            Intent intent = new Intent(this, MeetingService.class);
            stopService(intent);
            Log.d(TAG, "미팅 서비스 중지");

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public DrawingView getDrawingView() {
        return drawingView;
    }
}
