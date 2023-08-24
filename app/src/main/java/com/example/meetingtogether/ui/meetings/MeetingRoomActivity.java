package com.example.meetingtogether.ui.meetings;

import static com.example.meetingtogether.ui.meetings.PeerConfig.CAPTURE_PERMISSION_REQUEST_CODE;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_AECDUMP_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_AUDIOCODEC;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_AUDIO_BITRATE;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_CAMERA2;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_CAPTURETOTEXTURE_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_CMDLINE;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_DATA_CHANNEL_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_DISABLE_BUILT_IN_AEC;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_DISABLE_BUILT_IN_AGC;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_DISABLE_BUILT_IN_NS;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_ENABLE_RTCEVENTLOG;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_FLEXFEC_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_HWCODEC_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_ID;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_LOOPBACK;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_MAX_RETRANSMITS;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_MAX_RETRANSMITS_MS;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_NEGOTIATED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_NOAUDIOPROCESSING_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_OPENSLES_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_ORDERED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_PROTOCOL;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_ROOMID;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_RUNTIME;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_SCREENCAPTURE;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_TRACING;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_URLPARAMETERS;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEOCODEC;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_BITRATE;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_CALL;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_FILE_AS_CAMERA;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_FPS;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_HEIGHT;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_WIDTH;
import static com.example.meetingtogether.ui.meetings.PeerConfig.STAT_CALLBACK_PERIOD;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.meetingtogether.R;
import com.example.meetingtogether.databinding.ActivityMeetingRoomBinding;

import com.example.meetingtogether.ui.meetings.google.AppRTCAudioManager;
import com.example.meetingtogether.ui.meetings.google.AppRTCClient;
import com.example.meetingtogether.ui.meetings.google.CpuMonitor;
import com.example.meetingtogether.ui.meetings.google.CustomPeerConnectionFactory;
import com.example.meetingtogether.ui.meetings.google.PeerConnectionClient;
import com.example.meetingtogether.ui.meetings.google.WebSocketRTCClient;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;


import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.FileVideoCapturer;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RTCStatsReport;
import org.webrtc.RendererCommon;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFileRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MeetingRoomActivity extends AppCompatActivity implements AppRTCClient.SignalingEvents,
        PeerConnectionClient.PeerConnectionEvents, CustomPeerConnectionFactory.CustomPeerFactoryEvents{
    private static final String TAG = "TEST";

    /**
     * 비디오 싱크는 비디오 데이터를 받아 화면에 표시하는 역할을 하며
     */
    private static class ProxyVideoSink implements VideoSink {
        private VideoSink target;

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.");
                return;
            }

            target.onFrame(frame);
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }
    }

    private ActivityMeetingRoomBinding binding;

    private String roomId;

    private List<UserModel> userModelList = new ArrayList<>();

    private static final int CONNECTION_REQUEST = 1;
    private static final int PERMISSION_REQUEST = 2;
    private static final int REMOVE_FAVORITE_INDEX = 0;

    private boolean preferIsac;

    @Nullable
    private PeerConnectionFactory factory;

    private EglBase eglBase;

    @Nullable
    private VideoTrack localVideoTrack;
    @Nullable
    private VideoTrack remoteVideoTrack;
    private int videoWidth;
    private int videoHeight;
    private int videoFps;
    private MediaConstraints audioConstraints;
    private MediaConstraints videoConstraints;
    private MediaConstraints sdpMediaConstraints;
    @Nullable
    private AudioSource audioSource;
    @Nullable
    private AudioTrack localAudioTrack;

    private Intent intent;

    private VideoCapturer videoCapturer;

    /**
     * 필요한  변수
     */
    private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
    @Nullable
    private AppRTCClient appRtcClient;
    @Nullable
    private AppRTCClient.SignalingParameters signalingParameters;
    @Nullable private AppRTCAudioManager audioManager;
    @Nullable
    private SurfaceViewRenderer pipRenderer;
    @Nullable
    private SurfaceViewRenderer fullscreenRenderer;
    @Nullable
    private VideoFileRenderer videoFileRenderer;
    private final List<VideoSink> remoteSinks = new ArrayList<>();
    private Toast logToast;
    private boolean commandLineRun;
    private boolean activityRunning;
    private AppRTCClient.RoomConnectionParameters roomConnectionParameters;
    @Nullable
    private CustomPeerConnectionFactory.PeerConnectionParameters peerConnectionParameters;
    private boolean connected;
    private boolean isError;
    private boolean callControlFragmentVisible = true;
    private long callStartedTimeMs;
    private boolean micEnabled = true;
    private boolean screencaptureEnabled;
    private static Intent mediaProjectionPermissionResultData;
    private static int mediaProjectionPermissionResultCode;
    // True if local view is in the fullscreen renderer.
    private boolean isSwappedFeeds;

    // Controls
    private CpuMonitor cpuMonitor;

    private List<UserModel> userList;
    private Map<String, PeerConnectionClient> peerConnections = new HashMap();
    private String clientId;

    /**
     * 필요한 권한
     */
    private static final String[] PERMISSIONS = {
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.INTERNET
    };

    /**
     * 시스템 권한 다이얼로그에서의 결과에 대한 권한 콜백
     */
    private ActivityResultLauncher activityResultLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                Log.d(TAG, ""+result.toString());

                Boolean areAllGranted = true;
                // 모든 권한에 동의 했는지 확인
                for(Boolean b : result.values()) {
                    areAllGranted = areAllGranted && b;
                }

                // 모든 권한에 동의
                if(areAllGranted) {
                    start();
                // 모든 권한에 동의 하지 않음
                }else{
                    finish();
                }
            }
        });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMeetingRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /**
         * 전달된 Intent 받는 부분
         */
        intent = getIntent();
        roomId = intent.getStringExtra(EXTRA_ROOMID);

        if("".equals(roomId) || roomId == null){
            finish();
        }

        /**
         * 권한이 없을 경우에는 권한을 요청한다.
         */
        if (!checkPermissions()) {
            // 권한 요청
            this.activityResultLauncher.launch(PERMISSIONS);
        }else{
            start();
        }

        /**
         * 버튼 이벤트 리스너 등록
         */
        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mSocket.emit("message", "helloWorld");
            }
        });

        binding.cameraOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (videoCapturer != null) {
                        videoCapturer.stopCapture();
                        videoCapturer = null;
                    }else{
                        getVideoSource();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void start(){
        connected = false;
        signalingParameters = null;

        // Create UI controls.
        pipRenderer = findViewById(R.id.pip_video_view);
        fullscreenRenderer = findViewById(R.id.fullscreen_video_view);

        /**
         * 피드를 swap 합니다
         */
        pipRenderer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSwappedFeeds(!isSwappedFeeds);
            }
        });

        remoteSinks.add(remoteProxyRenderer);

        /**
         * EglBase는 EGL(EGL, Embedded-System Graphics Library)과 관련이 있으며, OpenGL ES와 하드웨어 사이의 상호 작용을 관리합니다.
         */
        eglBase = EglBase.create();

        /**
         * pip 비디오 렌더러 생성
         */
        pipRenderer.init(eglBase.getEglBaseContext(), null);
        pipRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        String saveRemoteVideoToFile = intent.getStringExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);

        /**
         * 파일 형태로 Remote 로부터 비디오를 저장 시킵니다.
         */
        // When saveRemoteVideoToFile is set we save the video from the remote to a file.
        if (saveRemoteVideoToFile != null) {
            int videoOutWidth = intent.getIntExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
            int videoOutHeight = intent.getIntExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
            try {
                videoFileRenderer = new VideoFileRenderer(
                        saveRemoteVideoToFile, videoOutWidth, videoOutHeight, eglBase.getEglBaseContext());
                remoteSinks.add(videoFileRenderer);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to open video file for output: " + saveRemoteVideoToFile, e);
            }
        }

        /**
         * full 비디오 렌더러 생성
         */
        fullscreenRenderer.init(eglBase.getEglBaseContext(), null);
        fullscreenRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

        pipRenderer.setZOrderMediaOverlay(true);
        pipRenderer.setEnableHardwareScaler(true /* enabled */);
        fullscreenRenderer.setEnableHardwareScaler(false /* enabled */);

        /**
         * 로컬 Feed를 full screen으로 시작합니다.
         * 그리고 전화 연결이 됐을때, swap 됩니다.
         */
        setSwappedFeeds(true /* isSwappedFeeds */);

        /**
         * Room uri 체크
         */
        Uri roomUri = intent.getData();
        if (roomUri == null) {
            logAndToast(getString(R.string.missing_url));
            Log.e(TAG, "Didn't get any URL in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        /**
         * Room ID 체크
         */
        String roomId = intent.getStringExtra(EXTRA_ROOMID);
        Log.d(TAG, "Room ID: " + roomId);
        if (roomId == null || roomId.length() == 0) {
            logAndToast(getString(R.string.missing_url));
            Log.e(TAG, "Incorrect room ID in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        /**
         * Peer 연결 파라미터 설정 준비
         */
        boolean loopback = intent.getBooleanExtra(EXTRA_LOOPBACK, false);
        boolean tracing = intent.getBooleanExtra(EXTRA_TRACING, false);

        int videoWidth = intent.getIntExtra(EXTRA_VIDEO_WIDTH, 0);
        int videoHeight = intent.getIntExtra(EXTRA_VIDEO_HEIGHT, 0);

        screencaptureEnabled = intent.getBooleanExtra(EXTRA_SCREENCAPTURE, false);
        // If capturing format is not specified for screencapture, use screen resolution.
        if (screencaptureEnabled && videoWidth == 0 && videoHeight == 0) {
            DisplayMetrics displayMetrics = getDisplayMetrics();
            videoWidth = displayMetrics.widthPixels;
            videoHeight = displayMetrics.heightPixels;
        }
        PeerConnectionClient.DataChannelParameters dataChannelParameters = null;

        /**
         * 데이터 채널 설정
         */
        if (intent.getBooleanExtra(EXTRA_DATA_CHANNEL_ENABLED, false)) {
            dataChannelParameters = new PeerConnectionClient.DataChannelParameters(intent.getBooleanExtra(EXTRA_ORDERED, true),
                    intent.getIntExtra(EXTRA_MAX_RETRANSMITS_MS, -1),
                    intent.getIntExtra(EXTRA_MAX_RETRANSMITS, -1), intent.getStringExtra(EXTRA_PROTOCOL),
                    intent.getBooleanExtra(EXTRA_NEGOTIATED, false), intent.getIntExtra(EXTRA_ID, -1));
        }

        /**
         * Peer 연결 파라미터 생성
         */
        peerConnectionParameters =
                new CustomPeerConnectionFactory.PeerConnectionParameters(intent.getBooleanExtra(EXTRA_VIDEO_CALL, true), loopback,
                        tracing, videoWidth, videoHeight, intent.getIntExtra(EXTRA_VIDEO_FPS, 0),
                        intent.getIntExtra(EXTRA_VIDEO_BITRATE, 0), intent.getStringExtra(EXTRA_VIDEOCODEC),
                        intent.getBooleanExtra(EXTRA_HWCODEC_ENABLED, true),
                        intent.getBooleanExtra(EXTRA_FLEXFEC_ENABLED, false),
                        intent.getIntExtra(EXTRA_AUDIO_BITRATE, 0), intent.getStringExtra(EXTRA_AUDIOCODEC),
                        intent.getBooleanExtra(EXTRA_NOAUDIOPROCESSING_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_AECDUMP_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_OPENSLES_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AEC, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AGC, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_NS, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, false),
                        intent.getBooleanExtra(EXTRA_ENABLE_RTCEVENTLOG, false), dataChannelParameters);
        commandLineRun = intent.getBooleanExtra(EXTRA_CMDLINE, false);
        int runTimeMs = intent.getIntExtra(EXTRA_RUNTIME, 0);

        Log.d(TAG, "VIDEO_FILE: '" + intent.getStringExtra(EXTRA_VIDEO_FILE_AS_CAMERA) + "'");

        /**
         * 피디오 캡처 생성
         */
        if (peerConnectionParameters.videoCallEnabled) {
            videoCapturer = createVideoCapturer();
        }

        /**
         * Room 명이 IP 형태라면 DirectRTCClient를 이용하고
         * 그렇지 않을 경우엔 표준 WebSocketRTCClient를 이용합니다.
         */
        appRtcClient = new WebSocketRTCClient(this, roomId);
//        if (loopback || !DirectRTCClient.IP_PATTERN.matcher(roomId).matches()) {
//            appRtcClient = new WebSocketRTCClient(this, roomId);
//        } else {
//            Log.i(TAG, "Using DirectRTCClient because room name looks like an IP.");
//            appRtcClient = new DirectRTCClient(this);
//        }

        /**
         * Room 연결 파라미터 생성
         */
        String urlParameters = intent.getStringExtra(EXTRA_URLPARAMETERS);
        roomConnectionParameters =
                new AppRTCClient.RoomConnectionParameters(roomUri.toString(), roomId, loopback, urlParameters);


        // For command line execution run connection for <runTimeMs> and exit.
        if (commandLineRun && runTimeMs > 0) {
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    disconnect();
                }
            }, runTimeMs);
        }

        /**
         * Peer 팩토리 생성
         */
        CustomPeerConnectionFactory customPeerConnectionFactory = CustomPeerConnectionFactory.getInstance(getApplicationContext(), eglBase, this, peerConnectionParameters);
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        if (loopback) {
            options.networkIgnoreMask = 0;
        }
        customPeerConnectionFactory.createPeerConnectionFactory(options);

        /**
         * 스크린 캡처여부에 따라 다르게 start 한다.
         */
        if (screencaptureEnabled) {
            startScreenCapture();
        } else {
            startCall();
        }

    }

    /**
     * 권한 체크
     */
    private boolean checkPermissions(){
        // 허용 되지 않은 권한이 있는지 체크
        for(String permission : PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onCustomPeerFactoryError(String errorMessage) {
        Log.d(TAG, errorMessage);
    }

    @Override
    public void onCreatedFactory(PeerConnectionFactory factory) {
        Log.d(TAG, "피어 팩토리 생성 완료");
        this.factory = factory;
    }

    private void showUserList(){
        Log.d("TEST", "==========[유저 리스트 시작]==========");
        for (int i = 0; i < userModelList.size(); i++){
            UserModel userModel = userModelList.get(i);
            Log.d("TEST", userModel.toString());
        }
        Log.d("TEST", "==========[유저 리스트 끝]==========");
    }

    /**
     * 비디오 소스 가져오기
     */
    private void getVideoSource() {

        /**
         * 비디오 소스 생성
         */
        VideoSource videoSource = null;
        if (videoCapturer != null) {
            SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
            videoSource = factory.createVideoSource(videoCapturer.isScreencast());
            videoCapturer.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());
        }

        localVideoTrack = factory.createVideoTrack("100", videoSource);

        /**
         * MediaConstraints 생성
         */
        audioConstraints = new MediaConstraints();
        videoConstraints = new MediaConstraints();

        /**
         * 오디오 소스 생성
         */
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack("101", audioSource);

        if (videoCapturer != null) {
            videoCapturer.startCapture(videoWidth, videoHeight, videoFps);
        }

//        binding.fullscreenVideoView.setVisibility(View.VISIBLE);

        /**
         * 비디오 트랙을 surfaceView에 추가
         */
        localVideoTrack.addSink(binding.fullscreenVideoView);

    }


    @TargetApi(Build.VERSION_CODES.M)
    private String[] getMissingPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return new String[0];
        }

        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Failed to retrieve permissions.");
            return new String[0];
        }

        if (info.requestedPermissions == null) {
            Log.w(TAG, "No requested permissions.");
            return new String[0];
        }

        ArrayList<String> missingPermissions = new ArrayList<>();
        for (int i = 0; i < info.requestedPermissions.length; i++) {
            if ((info.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0) {
                missingPermissions.add(info.requestedPermissions[i]);
            }
        }
        Log.d(TAG, "Missing permissions: " + missingPermissions);

        return missingPermissions.toArray(new String[missingPermissions.size()]);
    }

    private DisplayMetrics getDisplayMetrics() {
        /**
         * DisplayMetrics : 화면의 크기, 밀도 및 해상도와 같은 디스플레이 관련 정보를 얻을 수 있는 도구입니다.
         */
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager =
                (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);

        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    private static int getSystemUiVisibility() {
        /**
         * 전체화면 옵션
         * immersive 모드는 사용자가 화면과 많이 상호작용하는 앱용으로 만들어졌습니다.
         * 시스템 표시줄을 다시 표시하려면 시스템 표시줄이 숨겨진 가장자리에서 스와이프하면 됩니다.
         */
        return View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    }

    private void startScreenCapture() {
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getApplication().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this) && getIntent().getBooleanExtra(EXTRA_CAMERA2, true);
    }

    private boolean captureToTexture() {
        return getIntent().getBooleanExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, false);
    }

    private @Nullable VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private @Nullable VideoCapturer createScreenCapturer() {
        if (mediaProjectionPermissionResultCode != Activity.RESULT_OK) {
            reportError("User didn't give permission to capture the screen.");
            return null;
        }
        return new ScreenCapturerAndroid(
                mediaProjectionPermissionResultData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                reportError("User revoked permission to capture the screen.");
            }
        });
    }

    // Activity interfaces
    @Override
    public void onStop() {
        super.onStop();
        activityRunning = false;
        // Don't stop the video when using screencapture to allow user to show other apps to the remote
        // end.

        /**
         * 갖고 있는 모든 피어 비디오 중지
         */
        Iterator<String> keys = peerConnections.keySet().iterator();
        while(keys.hasNext()){
            String keyClientId = keys.next();
            PeerConnectionClient peerConnectionClient = peerConnections.get(keyClientId);
            if (peerConnectionClient != null && !screencaptureEnabled) {
                peerConnectionClient.stopVideoSource();
            }
        }

        if (cpuMonitor != null) {
            cpuMonitor.pause();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        activityRunning = true;
        // Video is not paused for screencapture. See onPause.
        /**
         * 갖고 있는 모든 피어 비디오 재개
         */
        Iterator<String> keys = peerConnections.keySet().iterator();
        while(keys.hasNext()){
            String keyClientId = keys.next();
            PeerConnectionClient peerConnectionClient = peerConnections.get(keyClientId);
            if (peerConnectionClient != null && !screencaptureEnabled) {
                peerConnectionClient.startVideoSource();
            }
        }

        if (cpuMonitor != null) {
            cpuMonitor.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        String room = roomEditText.getText().toString();
//        String roomListJson = new JSONArray(roomList).toString();
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putString(keyprefRoom, room);
//        editor.putString(keyprefRoomList, roomListJson);
//        editor.commit();
    }

    @Override
    protected void onDestroy() {
        Thread.setDefaultUncaughtExceptionHandler(null);
        disconnect();
        if (logToast != null) {
            logToast.cancel();
        }
        activityRunning = false;
        super.onDestroy();
    }

    /**
     * call 시작
     */
    private void startCall() {
        if (appRtcClient == null) {
            Log.e(TAG, "AppRTC client is not allocated for a call.");
            return;
        }
        callStartedTimeMs = System.currentTimeMillis();

        /**
         * Room 연결 시작
         * appRtcClient 는 소켓 통신을 위한 객체
         */
        logAndToast(getString(R.string.connecting_to, roomConnectionParameters.roomUrl));
        appRtcClient.connectToRoom(roomConnectionParameters);

        audioManager = AppRTCAudioManager.create(getApplicationContext());
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(TAG, "Starting the audio manager...");
        audioManager.start(new AppRTCAudioManager.AudioManagerEvents() {
            /**
             * 이용 가능한 오디오 디바이스의 수가 바뀔때 마다 이 method가 호출될 것입니다.
             */
            @Override
            public void onAudioDeviceChanged(
                    AppRTCAudioManager.AudioDevice audioDevice, Set<AppRTCAudioManager.AudioDevice> availableAudioDevices) {
                onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
            }
        });
    }

    // Should be called from UI thread
    private void callConnected(PeerConnectionClient peerConnectionClient) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.i(TAG, "Call connected: delay=" + delta + "ms");
        if (peerConnectionClient == null || isError) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Enable statistics callback.
//        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
//        setSwappedFeeds(false /* isSwappedFeeds */);
    }

    // This method is called when the audio manager reports audio device change,
    // e.g. from wired headset to speakerphone.
    private void onAudioManagerDevicesChanged(
            final AppRTCAudioManager.AudioDevice device, final Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.d(TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", "
                + "selected: " + device);
        // TODO(henrika): add callback handler.
    }

    /**
     * Remote 리소스로 부터 연결해제
     * 로컬 리소스를 삭제
     * exit
     */
    private void disconnect() {
        activityRunning = false;
        remoteProxyRenderer.setTarget(null);
        localProxyVideoSink.setTarget(null);
        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }
        if (pipRenderer != null) {
            pipRenderer.release();
            pipRenderer = null;
        }
        if (videoFileRenderer != null) {
            videoFileRenderer.release();
            videoFileRenderer = null;
        }
        if (fullscreenRenderer != null) {
            fullscreenRenderer.release();
            fullscreenRenderer = null;
        }
        Iterator<String> keys = peerConnections.keySet().iterator();
        while(keys.hasNext()){
            String keyClientId = keys.next();
            PeerConnectionClient peerConnectionClient = peerConnections.get(keyClientId);
            peerConnectionClient.close();

            peerConnections.remove(keyClientId);
        }
//        if(peerConnectionClient != null){
//            peerConnectionClient.close();
//            peerConnections.remove(peerConnectionClient);
//        }


        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }
        if (connected && !isError) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    private void disconnectWithErrorMessage(final String errorMessage) {
        if (commandLineRun || !activityRunning) {
            Log.e(TAG, "Critical error: " + errorMessage);
            disconnect();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getText(R.string.channel_error_title))
                    .setMessage(errorMessage)
                    .setCancelable(false)
                    .setNeutralButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    disconnect();
                                }
                            })
                    .create()
                    .show();
        }
    }

    private void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

    private void reportError(final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError) {
                    isError = true;
                    disconnectWithErrorMessage(description);
                }
            }
        });
    }

    private @Nullable VideoCapturer createVideoCapturer() {
        final VideoCapturer videoCapturer;
        String videoFileAsCamera = getIntent().getStringExtra(EXTRA_VIDEO_FILE_AS_CAMERA);
        if (videoFileAsCamera != null) {
            try {
                videoCapturer = new FileVideoCapturer(videoFileAsCamera);
            } catch (IOException e) {
                reportError("Failed to open video file for emulated camera");
                return null;
            }
        } else if (screencaptureEnabled) {
            return createScreenCapturer();
        } else if (useCamera2()) {
            if (!captureToTexture()) {
                reportError(getString(R.string.camera2_texture_only_error));
                return null;
            }

            Logging.d(TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            Logging.d(TAG, "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            reportError("Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    private void setSwappedFeeds(boolean isSwappedFeeds) {
        Logging.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
        this.isSwappedFeeds = isSwappedFeeds;
        localProxyVideoSink.setTarget(isSwappedFeeds ? fullscreenRenderer : pipRenderer);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? pipRenderer : fullscreenRenderer);
        fullscreenRenderer.setMirror(isSwappedFeeds);
        pipRenderer.setMirror(!isSwappedFeeds);
    }

    // ----- AppRTCClient.AppRTCSignalingEvents 구현 ---------------
    /**
     * 모든 콜백들은 Websocket 시그널링 루퍼 스레드로부터 호출 됩니다.
     * 그리고 UI 스레드로 라우팅 됩니다.
     */
    private void createPeerConnectionInternal(Boolean isInitiator, String peerClientId, SessionDescription sdp,
                                              String senderId, String targetId, String type) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;

        logAndToast("Creating peer connection, delay=" + delta + "ms");

        /**
         * Peer 연결 클라이언트 생성
         */
        PeerConnectionClient peerConnectionClient = new PeerConnectionClient(
                getApplicationContext(), eglBase, MeetingRoomActivity.this, peerConnectionParameters.dataChannelParameters != null, peerConnectionParameters, factory, senderId, targetId);
        peerConnectionClient.createPeerConnection(
                localProxyVideoSink, remoteSinks, videoCapturer, isInitiator, peerClientId, sdp,
                senderId, targetId, type);



//        if (signalingParameters.initiator) {
//            logAndToast("Creating OFFER...");
//            // Create offer. Offer SDP will be sent to answering client in
//            // PeerConnectionEvents.onLocalDescription event.
//            peerConnectionClient.createOffer();
//        } else {
//            if (params.offerSdp != null) {
//                peerConnectionClient.setRemoteDescription(params.offerSdp);
//                logAndToast("Creating ANSWER...");
//                // Create answer. Answer SDP will be sent to offering client in
//                // PeerConnectionEvents.onLocalDescription event.
//                peerConnectionClient.createAnswer();
//            }
//            if (params.iceCandidates != null) {
//                // Add remote ICE candidates from room.
//                for (IceCandidate iceCandidate : params.iceCandidates) {
//                    peerConnectionClient.addRemoteIceCandidate(iceCandidate);
//                }
//            }
//        }
    }

    @Override
    public void onPeerCreated(PeerConnectionClient peerConnectionClient, PeerConnection peerConnection, Boolean isInitiator, String peerClientId, SessionDescription sdp,
                              String senderId, String targetId, String type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onPeerCreated");

                /**
                 * 피어 생성되면
                 * Map<String, PeerConnectionClient> PeerConnections 에서 관리 한다.
                 */
                if(peerConnections.get(peerClientId) == null){
                    Log.d(TAG, "[onPeerCreated] 추가 peerClientId:" + peerClientId);
                    Log.d(TAG, "[onPeerCreated] 추가 peerConnectionClient:" + peerConnectionClient);
                    peerConnections.put(peerClientId, peerConnectionClient);
                }
                Log.d(TAG, "[onPeerCreated] peerConnections:" + peerConnections);

                if(isInitiator){
                    logAndToast("Creating OFFER...");
                    // Create offer. Offer SDP will be sent to answering client in
                    // PeerConnectionEvents.onLocalDescription event.
                    peerConnectionClient.createOffer(clientId, peerClientId);
                }else{
                    /**
                     * 이곳에서 sdp 세팅
                     */
                    onRemoteDescription(sdp, senderId, targetId, type);

//                    logAndToast("Creating ANSWER...");
//                    peerConnectionClient.createAnswer();
                }
            }
        });
    }

    @Override
    public void onOffer(String senderId, String targetId, String type, SessionDescription sdp) {
        PeerConnectionClient peerConnectionclient =  peerConnections.get(senderId);
        /**
         * 해당 peer가 없을 경우에만 생성합니다.
         */
        if(peerConnectionclient == null){
            Log.d(TAG, "[onOffer] 서버로 부터 받은 senderId:" + senderId);
            Log.d(TAG, "[onOffer] 서버로 부터 받은 targetId:" + targetId);
            Log.d(TAG, "[onOffer] senderId => " + targetId);
            Log.d(TAG, "[onOffer] targetId => " + senderId);
            createPeerConnectionInternal(false, senderId, sdp, targetId, senderId, type);
        }
    }

    @Override
    public void onWebSocketConnected(String clientId) {
        this.clientId = clientId;
        Log.d(TAG, "[나의 아이디]:" + this.clientId);
        this.appRtcClient.joinRoom(roomId);
    }

    @Override
    public void onWebSocketJoined() {
        this.appRtcClient.sendReqUserList();
    }

    @Override
    public void onUserList(List<UserModel> userList, String initiator) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MeetingRoomActivity.this.userList = userList;
                // 이곳에서 User ClientID에 대한 Peer가 없다면, 생성시킨다.
                for (int i = 0; i < userList.size(); i++){
                    UserModel userModel = userList.get(i);
                    String peerClientId = userModel.clientId;
                    Log.d(TAG, "peerClientId:" + peerClientId);

                    // 자기 자신은 제외 시키자
                    if(userModel.clientId.equals(clientId)){
                       continue;
                    }

                    /**
                     * Socket ClientId 와 Initiator 가 같을 경우에만
                     * 존재하지 않는 피어들을 생성 시키자.
                     */
                    if(clientId.equals(initiator)){
                        // peer가 존재하지 않을 경우 생성
                        if(!checkExistPeer(peerClientId)){
                            Log.d(TAG, "Peer 생성시키자^^");
                            if(factory == null){
                                Log.d(TAG, "아직 팩토리가 생성되지 않았음");
                                return;
                            }
                            createPeerConnectionInternal(true, peerClientId , null, null, null, null);
                        }

                    }
                }
            }
        });
    }

    private boolean checkExistPeer(String clientId){
        Iterator<String> keys = peerConnections.keySet().iterator();
        boolean isExist = false;
        while(keys.hasNext()){
            String keyClientId = keys.next();
            if(clientId.equals(keyClientId)){
                isExist = true;
                return isExist;
            }
        }
        return isExist;
    }

    @Override
    public void onConnectedToRoom(final AppRTCClient.SignalingParameters params) {
        // work Thread에서 UI Thread로 동작 되게 시키는 것
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                createPeerConnectionInternal(params);
            }
        });
    }




    @Override
    public void onRemoteDescription(final SessionDescription desc, String senderId, String targetId, String type) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        /**
         * 해당 PeerConnectionClient 가져오기
         */
        PeerConnectionClient peerConnectionClient = getPeerConnectionClient(targetId);
        Log.d(TAG, "[onRemoteDescription] type:" + type);
        Log.d(TAG, "[onRemoteDescription] senderId:" + senderId);
        Log.d(TAG, "[onRemoteDescription] targetId:" + targetId);
        Log.d(TAG, "[onRemoteDescription] peerConnectionClient:" + peerConnectionClient);
        Log.d(TAG, "[onRemoteDescription] sdp.description" + desc.description);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                    return;
                }
                logAndToast("Received remote " + desc.type + ", delay=" + delta + "ms");
                peerConnectionClient.setRemoteDescription(desc);

                if ("offer".equals(type)) {
                    logAndToast("Creating ANSWER...");
                    // Create answer. Answer SDP will be sent to offering client in
                    // PeerConnectionEvents.onLocalDescription event.
                    peerConnectionClient.createAnswer();
                }
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(final IceCandidate candidate, String senderId, String targetId) {

        /**
         * 해당 PeerConnectionClient 가져오기
         */
        PeerConnectionClient peerConnectionClient = getPeerConnectionClient(senderId);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.addRemoteIceCandidate(candidate);
            }
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates, String senderId, String targetId) {

        /**
         * 해당 PeerConnectionClient 가져오기
         */
        PeerConnectionClient peerConnectionClient = getPeerConnectionClient(senderId);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.removeRemoteIceCandidates(candidates);
            }
        });
    }

    public PeerConnectionClient getPeerConnectionClient(String clientId){
        return peerConnections.get(clientId);
    }

    @Override
    public void onChannelClose() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("Remote end hung up; dropping PeerConnection");
                disconnect();
            }
        });
    }

    @Override
    public void onChannelError(final String description) {
        reportError(description);
    }



    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
    // Send local peer connection SDP and ICE candidates to remote party.
    // All callbacks are invoked from peer connection client looper thread and
    // are routed to UI thread.
    @Override
    public void onLocalDescription(final SessionDescription desc, boolean isInitiator, String senderId, String targetId, PeerConnectionClient peerConnectionClient) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    logAndToast("Sending " + desc.type + ", delay=" + delta + "ms");
                    if (isInitiator) {
                        appRtcClient.sendOfferSdp(desc, senderId, targetId);
                    } else {
                        appRtcClient.sendAnswerSdp(desc, senderId, targetId);
                    }
                }
                if (peerConnectionParameters.videoMaxBitrate > 0) {
                    Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                    peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
                }
            }
        });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate, String peerClientId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidate(candidate, clientId, peerClientId);
                }
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(final IceCandidate[] candidates, String peerClientId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidateRemovals(candidates, clientId, peerClientId);
                }
            }
        });
    }

    @Override
    public void onIceConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("ICE connected, delay=" + delta + "ms");
            }
        });
    }

    @Override
    public void onIceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("ICE disconnected");
            }
        });
    }

    @Override
    public void onConnected(PeerConnectionClient peerConnectionClient) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("DTLS connected, delay=" + delta + "ms");
                connected = true;
                callConnected(peerConnectionClient);
            }
        });
    }

    @Override
    public void onDisconnected(PeerConnectionClient peerConnectionClient) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("DTLS disconnected");
                connected = false;
                disconnect();
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {}

    @Override
    public void onPeerConnectionStatsReady(final RTCStatsReport report) {
    }

    @Override
    public void onPeerConnectionError(final String description) {
        reportError(description);
    }

}