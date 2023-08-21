package com.example.meetingtogether.ui.meetings;

import static com.example.meetingtogether.ui.meetings.PeerConfig.AUDIO_CODEC_ISAC;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_AECDUMP_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_AUDIOCODEC;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_AUDIO_BITRATE;
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
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_TRACING;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEOCODEC;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_BITRATE;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_CALL;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_FPS;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_HEIGHT;
import static com.example.meetingtogether.ui.meetings.PeerConfig.EXTRA_VIDEO_WIDTH;
import static com.example.meetingtogether.ui.meetings.PeerConfig.VIDEO_CODEC_H264_HIGH;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.example.meetingtogether.MyApplication;
import com.example.meetingtogether.common.Constants;
import com.example.meetingtogether.databinding.ActivityMeetingRoomBinding;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.meetingtogether.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RTCStatsReport;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MeetingRoomActivity extends AppCompatActivity implements AppRTCClient.SignalingEvents,
        PeerConnectionClient.PeerConnectionEvents{
    private static final String TAG = "TEST";
    private ActivityMeetingRoomBinding binding;

    private Socket mSocket;

    private String roomId;

    @Nullable
    private SurfaceViewRenderer fullscreenRenderer;

    private List<UserModel> userModelList = new ArrayList<>();

    private static final int CONNECTION_REQUEST = 1;
    private static final int PERMISSION_REQUEST = 2;
    private static final int REMOVE_FAVORITE_INDEX = 0;

    @Nullable private PeerConnectionClient peerConnectionClient;

    @Nullable
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;

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

    private SharedPreferences sharedPref;
    private String keyprefResolution;
    private String keyprefFps;
    private String keyprefVideoBitrateType;
    private String keyprefVideoBitrateValue;
    private String keyprefAudioBitrateType;
    private String keyprefAudioBitrateValue;
    private String keyprefRoomServerUrl;
    private String keyprefRoom;
    private String keyprefRoomList;

    private Intent receiveIntent;

    private VideoCapturer videoCapturer;

    /**
     * 필요한 권한
     */
    private static final String[] PERMISSIONS = {
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE
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
                    // 송신측에서 extra_loopback 값을 보냈다면 받고, 없으면 디폴트 false
                    boolean loopback = receiveIntent.getBooleanExtra(PeerConfig.EXTRA_LOOPBACK, false);
                    // 송신측에서 extra_runtime 값을 보냈다면 받고, 없으면 디폴트 0
                    int runTimeMs = receiveIntent.getIntExtra(PeerConfig.EXTRA_RUNTIME, 0);
                    // 송신측에서 extra_use_values_from_intent 값을 보냈다면 받고, 없으면 디폴트 false
                    boolean useValuesFromIntent =
                            receiveIntent.getBooleanExtra(PeerConfig.EXTRA_USE_VALUES_FROM_INTENT, false);

                    // 위의 정보를 바탕으로 room으로 연결하자
                    setting(true, loopback, useValuesFromIntent, runTimeMs);
                // 모든 권한에 동의 하지 않음
                }else{
                    finish();
                }
            }
        });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get setting keys.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        keyprefResolution = getString(R.string.pref_resolution_key);
        keyprefFps = getString(R.string.pref_fps_key);
        keyprefVideoBitrateType = getString(R.string.pref_maxvideobitrate_key);
        keyprefVideoBitrateValue = getString(R.string.pref_maxvideobitratevalue_key);
        keyprefAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
        keyprefAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
        keyprefRoomServerUrl = getString(R.string.pref_room_server_url_key);
        keyprefRoom = getString(R.string.pref_room_key);
        keyprefRoomList = getString(R.string.pref_room_list_key);

        binding = ActivityMeetingRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /**
         * 전달된 Intent 받는 부분
         */
        receiveIntent = getIntent();
        roomId = receiveIntent.getStringExtra("roomId");

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
                mSocket.emit("message", "helloWorld");
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
        // 송신측에서 extra_loopback 값을 보냈다면 받고, 없으면 디폴트 false
        boolean loopback = receiveIntent.getBooleanExtra(PeerConfig.EXTRA_LOOPBACK, false);
        // 송신측에서 extra_runtime 값을 보냈다면 받고, 없으면 디폴트 0
        int runTimeMs = receiveIntent.getIntExtra(PeerConfig.EXTRA_RUNTIME, 0);
        // 송신측에서 extra_use_values_from_intent 값을 보냈다면 받고, 없으면 디폴트 false
        boolean useValuesFromIntent =
                receiveIntent.getBooleanExtra(PeerConfig.EXTRA_USE_VALUES_FROM_INTENT, false);

        // 위의 정보를 바탕으로 room으로 연결하자
        setting(true, loopback, useValuesFromIntent, runTimeMs);
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

    private void roomIn(){
        Toast.makeText(MeetingRoomActivity.this, roomId + "번방에 진입 했습니다.",Toast.LENGTH_SHORT).show();

        /**
         * 소켓 연결
         */
        socketIoConn();

        /**
         * [SurfaceViewRenderer]
         * 비디오 프레임을 효율적으로 렌더링할 수 있도록 도와줍니다.
         * EglBase와 함께 사용합니다.
         * 비디오 프레임을 OpenGL ES 컨텍스트로 렌더링하며, 이를 통해 빠르고 부드러운 비디오 표시가 가능합니다.
         *
         * SurfaceViewRenderer를 사용하려면 적절한 EglBase 인스턴스를 생성하고
         * init 메서드를 호출하여 초기화해야 합니다.
         * 그런 다음 SurfaceViewRenderer 인스턴스를 생성하고,
         * setEglRenderer 메서드를 사용하여 렌더러를 설정하고
         * init 메서드를 호출하여 초기화합니다.
         *
         * 마지막으로 비디오 프레임을 받아와 renderFrame 메서드를 호출하여 화면에 렌더링합니다.
         */

        /**
         * EglBase는 EGL(EGL, Embedded-System Graphics Library)과 관련이 있으며, OpenGL ES와 하드웨어 사이의 상호 작용을 관리합니다.
         */
        eglBase = EglBase.create();

        fullscreenRenderer = binding.fullscreenVideoView;
        fullscreenRenderer.init(eglBase.getEglBaseContext(), null);
        fullscreenRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        fullscreenRenderer.setEnableHardwareScaler(true /* enabled */);
        fullscreenRenderer.setMirror(true);

        /**
         * Peer 연결 파라미터 설정 준비
         */
        boolean loopback = receiveIntent.getBooleanExtra(EXTRA_LOOPBACK, false);
        boolean tracing = receiveIntent.getBooleanExtra(EXTRA_TRACING, false);

        int videoWidth = receiveIntent.getIntExtra(EXTRA_VIDEO_WIDTH, 0);
        int videoHeight = receiveIntent.getIntExtra(EXTRA_VIDEO_HEIGHT, 0);

        PeerConnectionClient.DataChannelParameters dataChannelParameters = null;

        /**
         * 데이터 채널 설정
         */
        if (receiveIntent.getBooleanExtra(EXTRA_DATA_CHANNEL_ENABLED, false)) {
            dataChannelParameters = new PeerConnectionClient.DataChannelParameters(receiveIntent.getBooleanExtra(EXTRA_ORDERED, true),
                    receiveIntent.getIntExtra(EXTRA_MAX_RETRANSMITS_MS, -1),
                    receiveIntent.getIntExtra(EXTRA_MAX_RETRANSMITS, -1), receiveIntent.getStringExtra(EXTRA_PROTOCOL),
                    receiveIntent.getBooleanExtra(EXTRA_NEGOTIATED, false), receiveIntent.getIntExtra(EXTRA_ID, -1));
        }

        /**K
         * Peer 연결 파라미터 생성
         */
        peerConnectionParameters =
                new PeerConnectionClient.PeerConnectionParameters(receiveIntent.getBooleanExtra(EXTRA_VIDEO_CALL, true), loopback,
                        tracing, videoWidth, videoHeight, receiveIntent.getIntExtra(EXTRA_VIDEO_FPS, 0),
                        receiveIntent.getIntExtra(EXTRA_VIDEO_BITRATE, 0), receiveIntent.getStringExtra(EXTRA_VIDEOCODEC),
                        receiveIntent.getBooleanExtra(EXTRA_HWCODEC_ENABLED, true),
                        receiveIntent.getBooleanExtra(EXTRA_FLEXFEC_ENABLED, false),
                        receiveIntent.getIntExtra(EXTRA_AUDIO_BITRATE, 0), receiveIntent.getStringExtra(EXTRA_AUDIOCODEC),
                        receiveIntent.getBooleanExtra(EXTRA_NOAUDIOPROCESSING_ENABLED, false),
                        receiveIntent.getBooleanExtra(EXTRA_AECDUMP_ENABLED, false),
                        receiveIntent.getBooleanExtra(EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED, false),
                        receiveIntent.getBooleanExtra(EXTRA_OPENSLES_ENABLED, false),
                        receiveIntent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AEC, false),
                        receiveIntent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AGC, false),
                        receiveIntent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_NS, false),
                        receiveIntent.getBooleanExtra(EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, false),
                        receiveIntent.getBooleanExtra(EXTRA_ENABLE_RTCEVENTLOG, false), dataChannelParameters);

        /**
         * Peer 연결 클라이언트 생성
         */
        peerConnectionClient = new PeerConnectionClient(
                getApplicationContext(), eglBase, peerConnectionParameters, MeetingRoomActivity.this);

        /**
         * Peer 팩토리 생성
         */
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        if (loopback) {
            options.networkIgnoreMask = 0;
        }
        createPeerConnectionFactory(options);

        getVideoSource();
    }

    private void socketIoConn(){
//        MyApplication app = (MyApplication) getApplication();
//        mSocket = app.getSocket();

        try {
            mSocket = IO.socket(Constants.CHAT_SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // 연결 성공 시 실행되는 코드
                Log.d("TEST", "소켓 연결");
                mSocket.emit("join", roomId);
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("TEST", "소켓 종료");
            }
        }).on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try{
                    String message = args[0].toString();
                    JSONObject jsonObject = new JSONObject(message);
                    Log.d("TEST", jsonObject.toString());
                    String type = jsonObject.getString("type");
                    if("userList".equals(type)){
                        JSONArray jsonArray = (JSONArray) jsonObject.get("userList");
                        for (int i = 0; i < jsonArray.length(); i++){
                            JSONObject userObj = new JSONObject(jsonArray.get(i).toString());
                            String clientID = userObj.get("clientID").toString();
                            Log.d("TEST", "userObj:"+userObj);
                            Log.d("TEST", "clientID:"+clientID);

                            userModelList.add(new UserModel(clientID));
                        }
                        // 유저 리스트 로그
                        showUserList();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e("TEST", e.toString());
                }
            }
        });
        mSocket.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket = null;
    }

    private void showUserList(){
        Log.d("TEST", "==========[유저 리스트 시작]==========");
        for (int i = 0; i < userModelList.size(); i++){
            UserModel userModel = userModelList.get(i);
            Log.d("TEST", userModel.toString());
        }
        Log.d("TEST", "==========[유저 리스트 끝]==========");
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

    private void createPeerConnectionFactory(PeerConnectionFactory.Options options) {

        /**
         * webRTC 추적 기능 존재 하면 설정
         */
        if (peerConnectionParameters.tracing) {
            PeerConnectionFactory.startInternalTracingCapture(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                            + "webrtc-trace.txt");
        }

        /**
         * Isac 사용 여부 확인
         */
        preferIsac = peerConnectionParameters.audioCodec != null
                && peerConnectionParameters.audioCodec.equals(AUDIO_CODEC_ISAC);

        /**
         * 녹음 오디오를 파일로 저장
         */
        if (peerConnectionParameters.saveInputAudioToFile) {
            if (!peerConnectionParameters.useOpenSLES) {
                Log.d(TAG, "Enable recording of microphone input audio to file");
//        saveRecordedAudioToFile = new RecordedAudioToFileController(executor);
            } else {
                // TODO(henrika): ensure that the UI reflects that if OpenSL ES is selected,
                // then the "Save inut audio to file" option shall be grayed out.
                Log.e(TAG, "Recording of input audio is not supported for OpenSL ES");
            }
        }

        final AudioDeviceModule adm = createJavaAudioDevice();

        /**
         * Peer Connection 팩토리 생성
         */
        if (options != null) {
            Log.d(TAG, "Factory networkIgnoreMask option: " + options.networkIgnoreMask);
        }

        /**
         * 인코딩/디코딩 팩토리 생성
         */
        final boolean enableH264HighProfile =
                VIDEO_CODEC_H264_HIGH.equals(peerConnectionParameters.videoCodec);
        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;

        if (peerConnectionParameters.videoCodecHwAcceleration) {
            encoderFactory = new DefaultVideoEncoderFactory(
                    eglBase.getEglBaseContext(), true /* enableIntelVp8Encoder */, enableH264HighProfile);
            decoderFactory = new DefaultVideoDecoderFactory(eglBase.getEglBaseContext());
        } else {
            encoderFactory = new SoftwareVideoEncoderFactory();
            decoderFactory = new SoftwareVideoDecoderFactory();
        }

        /**
         * encryption 설정
         */
        if (peerConnectionParameters.loopback) {
            options.disableEncryption = true;
        }

        /**
         * 팩토리 생성
         */
        factory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(adm)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();

        Log.d(TAG, "Peer connection factory created.");

        adm.release();
    }

    /**
     * 비디오 소스 가져오기
     */
    private void getVideoSource() {
        /**
         * 비디오 캡처 생성
         */
        videoCapturer = createCameraCapturer(new Camera2Enumerator(this));

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

    AudioDeviceModule createJavaAudioDevice() {
        // Enable/disable OpenSL ES playback.
        if (!peerConnectionParameters.useOpenSLES) {
            Log.w(TAG, "External OpenSLES ADM not implemented yet.");
            // TODO(magjed): Add support for external OpenSLES ADM.
        }

        // Set audio record error callbacks.
        JavaAudioDeviceModule.AudioRecordErrorCallback audioRecordErrorCallback = new JavaAudioDeviceModule.AudioRecordErrorCallback() {
            @Override
            public void onWebRtcAudioRecordInitError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordInitError: " + errorMessage);
            }

            @Override
            public void onWebRtcAudioRecordStartError(
                    JavaAudioDeviceModule.AudioRecordStartErrorCode errorCode, String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordStartError: " + errorCode + ". " + errorMessage);
            }

            @Override
            public void onWebRtcAudioRecordError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordError: " + errorMessage);
            }
        };

        JavaAudioDeviceModule.AudioTrackErrorCallback audioTrackErrorCallback = new JavaAudioDeviceModule.AudioTrackErrorCallback() {
            @Override
            public void onWebRtcAudioTrackInitError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackInitError: " + errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackStartError(
                    JavaAudioDeviceModule.AudioTrackStartErrorCode errorCode, String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackStartError: " + errorCode + ". " + errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackError: " + errorMessage);
            }
        };

        // Set audio record state callbacks.
        JavaAudioDeviceModule.AudioRecordStateCallback audioRecordStateCallback = new JavaAudioDeviceModule.AudioRecordStateCallback() {
            @Override
            public void onWebRtcAudioRecordStart() {
                Log.i(TAG, "Audio recording starts");
            }

            @Override
            public void onWebRtcAudioRecordStop() {
                Log.i(TAG, "Audio recording stops");
            }
        };

        // Set audio track state callbacks.
        JavaAudioDeviceModule.AudioTrackStateCallback audioTrackStateCallback = new JavaAudioDeviceModule.AudioTrackStateCallback() {
            @Override
            public void onWebRtcAudioTrackStart() {
                Log.i(TAG, "Audio playout starts");
            }

            @Override
            public void onWebRtcAudioTrackStop() {
                Log.i(TAG, "Audio playout stops");
            }
        };

        return JavaAudioDeviceModule.builder(getApplicationContext())
//        .setSamplesReadyCallback(saveRecordedAudioToFile)
                .setUseHardwareAcousticEchoCanceler(!peerConnectionParameters.disableBuiltInAEC)
                .setUseHardwareNoiseSuppressor(!peerConnectionParameters.disableBuiltInNS)
                .setAudioRecordErrorCallback(audioRecordErrorCallback)
                .setAudioTrackErrorCallback(audioTrackErrorCallback)
                .setAudioRecordStateCallback(audioRecordStateCallback)
                .setAudioTrackStateCallback(audioTrackStateCallback)
                .createAudioDeviceModule();
    }

    private static boolean commandLineRun;

    private void setting(boolean commandLineRun, boolean loopback, boolean useValuesFromIntent, int runTimeMs){
        MeetingRoomActivity.commandLineRun = commandLineRun;

        // roomId is random for loopback.
        if (loopback) {
            roomId = Integer.toString((new Random()).nextInt(100000000));
        }

        // Video call enabled flag.
        boolean videoCallEnabled = sharedPrefGetBoolean(R.string.pref_videocall_key,
                PeerConfig.EXTRA_VIDEO_CALL, R.string.pref_videocall_default, useValuesFromIntent);

        // Use screencapture option.
        boolean useScreencapture = sharedPrefGetBoolean(R.string.pref_screencapture_key,
                PeerConfig.EXTRA_SCREENCAPTURE, R.string.pref_screencapture_default, useValuesFromIntent);

        // Use Camera2 option.
        boolean useCamera2 = sharedPrefGetBoolean(R.string.pref_camera2_key, PeerConfig.EXTRA_CAMERA2,
                R.string.pref_camera2_default, useValuesFromIntent);

        // Get default codecs.
        String videoCodec = sharedPrefGetString(R.string.pref_videocodec_key,
                PeerConfig.EXTRA_VIDEOCODEC, R.string.pref_videocodec_default, useValuesFromIntent);
        String audioCodec = sharedPrefGetString(R.string.pref_audiocodec_key,
                PeerConfig.EXTRA_AUDIOCODEC, R.string.pref_audiocodec_default, useValuesFromIntent);

        // Check HW codec flag.
        boolean hwCodec = sharedPrefGetBoolean(R.string.pref_hwcodec_key,
                PeerConfig.EXTRA_HWCODEC_ENABLED, R.string.pref_hwcodec_default, useValuesFromIntent);

        // Check Capture to texture.
        boolean captureToTexture = sharedPrefGetBoolean(R.string.pref_capturetotexture_key,
                PeerConfig.EXTRA_CAPTURETOTEXTURE_ENABLED, R.string.pref_capturetotexture_default,
                useValuesFromIntent);

        // Check FlexFEC.
        boolean flexfecEnabled = sharedPrefGetBoolean(R.string.pref_flexfec_key,
                PeerConfig.EXTRA_FLEXFEC_ENABLED, R.string.pref_flexfec_default, useValuesFromIntent);

        // Check Disable Audio Processing flag.
        boolean noAudioProcessing = sharedPrefGetBoolean(R.string.pref_noaudioprocessing_key,
                PeerConfig.EXTRA_NOAUDIOPROCESSING_ENABLED, R.string.pref_noaudioprocessing_default,
                useValuesFromIntent);

        boolean aecDump = sharedPrefGetBoolean(R.string.pref_aecdump_key,
                PeerConfig.EXTRA_AECDUMP_ENABLED, R.string.pref_aecdump_default, useValuesFromIntent);

        boolean saveInputAudioToFile =
                sharedPrefGetBoolean(R.string.pref_enable_save_input_audio_to_file_key,
                        PeerConfig.EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED,
                        R.string.pref_enable_save_input_audio_to_file_default, useValuesFromIntent);

        // Check OpenSL ES enabled flag.
        boolean useOpenSLES = sharedPrefGetBoolean(R.string.pref_opensles_key,
                PeerConfig.EXTRA_OPENSLES_ENABLED, R.string.pref_opensles_default, useValuesFromIntent);

        // Check Disable built-in AEC flag.
        boolean disableBuiltInAEC = sharedPrefGetBoolean(R.string.pref_disable_built_in_aec_key,
                PeerConfig.EXTRA_DISABLE_BUILT_IN_AEC, R.string.pref_disable_built_in_aec_default,
                useValuesFromIntent);

        // Check Disable built-in AGC flag.
        boolean disableBuiltInAGC = sharedPrefGetBoolean(R.string.pref_disable_built_in_agc_key,
                PeerConfig.EXTRA_DISABLE_BUILT_IN_AGC, R.string.pref_disable_built_in_agc_default,
                useValuesFromIntent);

        // Check Disable built-in NS flag.
        boolean disableBuiltInNS = sharedPrefGetBoolean(R.string.pref_disable_built_in_ns_key,
                PeerConfig.EXTRA_DISABLE_BUILT_IN_NS, R.string.pref_disable_built_in_ns_default,
                useValuesFromIntent);

        // Check Disable gain control
        boolean disableWebRtcAGCAndHPF = sharedPrefGetBoolean(
                R.string.pref_disable_webrtc_agc_and_hpf_key, PeerConfig.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF,
                R.string.pref_disable_webrtc_agc_and_hpf_key, useValuesFromIntent);

        // Get video resolution from settings.
        int videoWidth = 0;
        int videoHeight = 0;
        if (useValuesFromIntent) {
            videoWidth = this.getIntent().getIntExtra(PeerConfig.EXTRA_VIDEO_WIDTH, 0);
            videoHeight = this.getIntent().getIntExtra(PeerConfig.EXTRA_VIDEO_HEIGHT, 0);
        }
        if (videoWidth == 0 && videoHeight == 0) {
            String resolution =
                    sharedPref.getString(keyprefResolution, getString(R.string.pref_resolution_default));
            String[] dimensions = resolution.split("[ x]+");
            if (dimensions.length == 2) {
                try {
                    videoWidth = Integer.parseInt(dimensions[0]);
                    videoHeight = Integer.parseInt(dimensions[1]);
                } catch (NumberFormatException e) {
                    videoWidth = 0;
                    videoHeight = 0;
                    Log.e(TAG, "Wrong video resolution setting: " + resolution);
                }
            }
        }

        // Get camera fps from settings.
        int cameraFps = 0;
        if (useValuesFromIntent) {
            cameraFps = getIntent().getIntExtra(PeerConfig.EXTRA_VIDEO_FPS, 0);
        }
        if (cameraFps == 0) {
            String fps = sharedPref.getString(keyprefFps, getString(R.string.pref_fps_default));
            String[] fpsValues = fps.split("[ x]+");
            if (fpsValues.length == 2) {
                try {
                    cameraFps = Integer.parseInt(fpsValues[0]);
                } catch (NumberFormatException e) {
                    cameraFps = 0;
                    Log.e(TAG, "Wrong camera fps setting: " + fps);
                }
            }
        }

        // Check capture quality slider flag.
        boolean captureQualitySlider = sharedPrefGetBoolean(R.string.pref_capturequalityslider_key,
                PeerConfig.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED,
                R.string.pref_capturequalityslider_default, useValuesFromIntent);

        // Get video and audio start bitrate.
        int videoStartBitrate = 0;
        if (useValuesFromIntent) {
            videoStartBitrate = getIntent().getIntExtra(PeerConfig.EXTRA_VIDEO_BITRATE, 0);
        }
        if (videoStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_maxvideobitrate_default);
            String bitrateType = sharedPref.getString(keyprefVideoBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefVideoBitrateValue, getString(R.string.pref_maxvideobitratevalue_default));
                videoStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        int audioStartBitrate = 0;
        if (useValuesFromIntent) {
            audioStartBitrate = getIntent().getIntExtra(PeerConfig.EXTRA_AUDIO_BITRATE, 0);
        }
        if (audioStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
            String bitrateType = sharedPref.getString(keyprefAudioBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefAudioBitrateValue, getString(R.string.pref_startaudiobitratevalue_default));
                audioStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        // Check statistics display option.
        boolean displayHud = sharedPrefGetBoolean(R.string.pref_displayhud_key,
                PeerConfig.EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, useValuesFromIntent);

        boolean tracing = sharedPrefGetBoolean(R.string.pref_tracing_key, PeerConfig.EXTRA_TRACING,
                R.string.pref_tracing_default, useValuesFromIntent);

        // Check Enable RtcEventLog.
        boolean rtcEventLogEnabled = sharedPrefGetBoolean(R.string.pref_enable_rtceventlog_key,
                PeerConfig.EXTRA_ENABLE_RTCEVENTLOG, R.string.pref_enable_rtceventlog_default,
                useValuesFromIntent);

        // Get datachannel options
        boolean dataChannelEnabled = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key,
                PeerConfig.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default,
                useValuesFromIntent);
        boolean ordered = sharedPrefGetBoolean(R.string.pref_ordered_key, PeerConfig.EXTRA_ORDERED,
                R.string.pref_ordered_default, useValuesFromIntent);
        boolean negotiated = sharedPrefGetBoolean(R.string.pref_negotiated_key,
                PeerConfig.EXTRA_NEGOTIATED, R.string.pref_negotiated_default, useValuesFromIntent);
        int maxRetrMs = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key,
                PeerConfig.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default,
                useValuesFromIntent);
        int maxRetr =
                sharedPrefGetInteger(R.string.pref_max_retransmits_key, PeerConfig.EXTRA_MAX_RETRANSMITS,
                        R.string.pref_max_retransmits_default, useValuesFromIntent);
        int id = sharedPrefGetInteger(R.string.pref_data_id_key, PeerConfig.EXTRA_ID,
                R.string.pref_data_id_default, useValuesFromIntent);
        String protocol = sharedPrefGetString(R.string.pref_data_protocol_key,
                PeerConfig.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, useValuesFromIntent);

        // Start AppRTCMobile activity.
        Log.d(TAG, "Connecting to room " + roomId);
        if (roomId != null) {
            receiveIntent.putExtra(PeerConfig.EXTRA_ROOMID, roomId);
            receiveIntent.putExtra(PeerConfig.EXTRA_LOOPBACK, loopback);
            receiveIntent.putExtra(PeerConfig.EXTRA_VIDEO_CALL, videoCallEnabled);
            receiveIntent.putExtra(PeerConfig.EXTRA_SCREENCAPTURE, useScreencapture);
            receiveIntent.putExtra(PeerConfig.EXTRA_CAMERA2, useCamera2);
            receiveIntent.putExtra(PeerConfig.EXTRA_VIDEO_WIDTH, videoWidth);
            receiveIntent.putExtra(PeerConfig.EXTRA_VIDEO_HEIGHT, videoHeight);
            receiveIntent.putExtra(PeerConfig.EXTRA_VIDEO_FPS, cameraFps);
            receiveIntent.putExtra(PeerConfig.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
            receiveIntent.putExtra(PeerConfig.EXTRA_VIDEO_BITRATE, videoStartBitrate);
            receiveIntent.putExtra(PeerConfig.EXTRA_VIDEOCODEC, videoCodec);
            receiveIntent.putExtra(PeerConfig.EXTRA_HWCODEC_ENABLED, hwCodec);
            receiveIntent.putExtra(PeerConfig.EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
            receiveIntent.putExtra(PeerConfig.EXTRA_FLEXFEC_ENABLED, flexfecEnabled);
            receiveIntent.putExtra(PeerConfig.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
            receiveIntent.putExtra(PeerConfig.EXTRA_AECDUMP_ENABLED, aecDump);
            receiveIntent.putExtra(PeerConfig.EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED, saveInputAudioToFile);
            receiveIntent.putExtra(PeerConfig.EXTRA_OPENSLES_ENABLED, useOpenSLES);
            receiveIntent.putExtra(PeerConfig.EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
            receiveIntent.putExtra(PeerConfig.EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
            receiveIntent.putExtra(PeerConfig.EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
            receiveIntent.putExtra(PeerConfig.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, disableWebRtcAGCAndHPF);
            receiveIntent.putExtra(PeerConfig.EXTRA_AUDIO_BITRATE, audioStartBitrate);
            receiveIntent.putExtra(PeerConfig.EXTRA_AUDIOCODEC, audioCodec);
            receiveIntent.putExtra(PeerConfig.EXTRA_DISPLAY_HUD, displayHud);
            receiveIntent.putExtra(PeerConfig.EXTRA_TRACING, tracing);
            receiveIntent.putExtra(PeerConfig.EXTRA_ENABLE_RTCEVENTLOG, rtcEventLogEnabled);
            receiveIntent.putExtra(PeerConfig.EXTRA_CMDLINE, commandLineRun);
            receiveIntent.putExtra(PeerConfig.EXTRA_RUNTIME, runTimeMs);
            receiveIntent.putExtra(PeerConfig.EXTRA_DATA_CHANNEL_ENABLED, dataChannelEnabled);

            if (dataChannelEnabled) {
                receiveIntent.putExtra(PeerConfig.EXTRA_ORDERED, ordered);
                receiveIntent.putExtra(PeerConfig.EXTRA_MAX_RETRANSMITS_MS, maxRetrMs);
                receiveIntent.putExtra(PeerConfig.EXTRA_MAX_RETRANSMITS, maxRetr);
                receiveIntent.putExtra(PeerConfig.EXTRA_PROTOCOL, protocol);
                receiveIntent.putExtra(PeerConfig.EXTRA_NEGOTIATED, negotiated);
                receiveIntent.putExtra(PeerConfig.EXTRA_ID, id);
            }

            if (useValuesFromIntent) {
                if (getIntent().hasExtra(PeerConfig.EXTRA_VIDEO_FILE_AS_CAMERA)) {
                    String videoFileAsCamera =
                            getIntent().getStringExtra(PeerConfig.EXTRA_VIDEO_FILE_AS_CAMERA);
                    receiveIntent.putExtra(PeerConfig.EXTRA_VIDEO_FILE_AS_CAMERA, videoFileAsCamera);
                }

                if (getIntent().hasExtra(PeerConfig.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE)) {
                    String saveRemoteVideoToFile =
                            getIntent().getStringExtra(PeerConfig.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);
                    receiveIntent.putExtra(PeerConfig.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE, saveRemoteVideoToFile);
                }

                if (getIntent().hasExtra(PeerConfig.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH)) {
                    int videoOutWidth =
                            getIntent().getIntExtra(PeerConfig.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
                    receiveIntent.putExtra(PeerConfig.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, videoOutWidth);
                }

                if (getIntent().hasExtra(PeerConfig.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT)) {
                    int videoOutHeight =
                            getIntent().getIntExtra(PeerConfig.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
                    receiveIntent.putExtra(PeerConfig.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, videoOutHeight);
                }
            }
        }

        roomIn();
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    @Nullable
    private String sharedPrefGetString(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultValue = getString(defaultId);
        if (useFromIntent) {
            String value = getIntent().getStringExtra(intentName);
            if (value != null) {
                return value;
            }
            return defaultValue;
        } else {
            String attributeName = getString(attributeId);
            return sharedPref.getString(attributeName, defaultValue);
        }
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private boolean sharedPrefGetBoolean(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        boolean defaultValue = Boolean.parseBoolean(getString(defaultId));
        if (useFromIntent) {
            return getIntent().getBooleanExtra(intentName, defaultValue);
        } else {
            String attributeName = getString(attributeId);
            return sharedPref.getBoolean(attributeName, defaultValue);
        }
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private int sharedPrefGetInteger(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultString = getString(defaultId);
        int defaultValue = Integer.parseInt(defaultString);
        if (useFromIntent) {
            return getIntent().getIntExtra(intentName, defaultValue);
        } else {
            String attributeName = getString(attributeId);
            String value = sharedPref.getString(attributeName, defaultString);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Wrong setting for: " + attributeName + ":" + value);
                return defaultValue;
            }
        }
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



    @Override
    public void onConnectedToRoom(AppRTCClient.SignalingParameters params) {

    }

    @Override
    public void onRemoteDescription(SessionDescription sdp) {

    }

    @Override
    public void onRemoteIceCandidate(IceCandidate candidate) {

    }

    @Override
    public void onRemoteIceCandidatesRemoved(IceCandidate[] candidates) {

    }

    @Override
    public void onChannelClose() {

    }

    @Override
    public void onChannelError(String description) {

    }

    @Override
    public void onLocalDescription(SessionDescription sdp) {

    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {

    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {

    }

    @Override
    public void onIceConnected() {

    }

    @Override
    public void onIceDisconnected() {

    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onPeerConnectionClosed() {

    }

    @Override
    public void onPeerConnectionStatsReady(RTCStatsReport report) {

    }

    @Override
    public void onPeerConnectionError(String description) {

    }
}