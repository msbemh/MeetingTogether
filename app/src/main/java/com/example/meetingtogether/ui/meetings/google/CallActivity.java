/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.example.meetingtogether.ui.meetings.google;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.meetingtogether.R;
import com.example.meetingtogether.ui.meetings.UserModel;
import com.example.meetingtogether.ui.meetings.google.AppRTCAudioManager.AudioDevice;
import com.example.meetingtogether.ui.meetings.google.AppRTCAudioManager.AudioManagerEvents;
import com.example.meetingtogether.ui.meetings.google.AppRTCClient.RoomConnectionParameters;
import com.example.meetingtogether.ui.meetings.google.AppRTCClient.SignalingParameters;
import com.example.meetingtogether.ui.meetings.google.PeerConnectionClient.DataChannelParameters;
import com.example.meetingtogether.ui.meetings.google.PeerConnectionClient.PeerConnectionParameters;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.FileVideoCapturer;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RTCStatsReport;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFileRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Activity for peer connection call setup, call waiting
 * and call view.
 *
 * [AppRTCClient.SignalingEvents]
 * 시그널 채널에서 전송된 메시지에 대한 콜백 인터페이스
 *
 * [PeerConnectionClient.PeerConnectionEvents]
 * Peer 연결 인터페이스
 *
 * [CallFragment.OnCallEvents]
 * 특정 method에 대하여 override를 통해 프래그먼트에 있는 view의 동작을 조작
 */
public class CallActivity extends Activity implements AppRTCClient.SignalingEvents,
        PeerConnectionClient.PeerConnectionEvents,
                                                      CallFragment.OnCallEvents {
  private static final String TAG = "TEST";

  public static final String EXTRA_ROOMID = " com.example.meetingtogether.ROOMID";
  public static final String EXTRA_URLPARAMETERS = " com.example.meetingtogether.URLPARAMETERS";
  public static final String EXTRA_LOOPBACK = " com.example.meetingtogether.LOOPBACK";
  public static final String EXTRA_VIDEO_CALL = " com.example.meetingtogether.VIDEO_CALL";
  public static final String EXTRA_SCREENCAPTURE = " com.example.meetingtogether.SCREENCAPTURE";
  public static final String EXTRA_CAMERA2 = " com.example.meetingtogether.CAMERA2";
  public static final String EXTRA_VIDEO_WIDTH = " com.example.meetingtogether.VIDEO_WIDTH";
  public static final String EXTRA_VIDEO_HEIGHT = " com.example.meetingtogether.VIDEO_HEIGHT";
  public static final String EXTRA_VIDEO_FPS = " com.example.meetingtogether.VIDEO_FPS";
  public static final String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED =
      "org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER";
  public static final String EXTRA_VIDEO_BITRATE = " com.example.meetingtogether.VID`EO_BITRATE";
  public static final String EXTRA_VIDEOCODEC = " com.example.meetingtogether.VIDEOCODEC";
  public static final String EXTRA_HWCODEC_ENABLED = " com.example.meetingtogether.HWCODEC";
  public static final String EXTRA_CAPTURETOTEXTURE_ENABLED = " com.example.meetingtogether.CAPTURETOTEXTURE";
  public static final String EXTRA_FLEXFEC_ENABLED = " com.example.meetingtogether.FLEXFEC";
  public static final String EXTRA_AUDIO_BITRATE = " com.example.meetingtogether.AUDIO_BITRATE";
  public static final String EXTRA_AUDIOCODEC = " com.example.meetingtogether.AUDIOCODEC";
  public static final String EXTRA_NOAUDIOPROCESSING_ENABLED =
      " com.example.meetingtogether.NOAUDIOPROCESSING";
  public static final String EXTRA_AECDUMP_ENABLED = " com.example.meetingtogether.AECDUMP";
  public static final String EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED =
      " com.example.meetingtogether.SAVE_INPUT_AUDIO_TO_FILE";
  public static final String EXTRA_OPENSLES_ENABLED = " com.example.meetingtogether.OPENSLES";
  public static final String EXTRA_DISABLE_BUILT_IN_AEC = " com.example.meetingtogether.DISABLE_BUILT_IN_AEC";
  public static final String EXTRA_DISABLE_BUILT_IN_AGC = " com.example.meetingtogether.DISABLE_BUILT_IN_AGC";
  public static final String EXTRA_DISABLE_BUILT_IN_NS = " com.example.meetingtogether.DISABLE_BUILT_IN_NS";
  public static final String EXTRA_DISABLE_WEBRTC_AGC_AND_HPF =
      " com.example.meetingtogether.DISABLE_WEBRTC_GAIN_CONTROL";
  public static final String EXTRA_DISPLAY_HUD = " com.example.meetingtogether.DISPLAY_HUD";
  public static final String EXTRA_TRACING = " com.example.meetingtogether.TRACING";
  public static final String EXTRA_CMDLINE = " com.example.meetingtogether.CMDLINE";
  public static final String EXTRA_RUNTIME = " com.example.meetingtogether.RUNTIME";
  public static final String EXTRA_VIDEO_FILE_AS_CAMERA = " com.example.meetingtogether.VIDEO_FILE_AS_CAMERA";
  public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE =
      " com.example.meetingtogether.SAVE_REMOTE_VIDEO_TO_FILE";
  public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH =
      " com.example.meetingtogether.SAVE_REMOTE_VIDEO_TO_FILE_WIDTH";
  public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT =
      " com.example.meetingtogether.SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT";
  public static final String EXTRA_USE_VALUES_FROM_INTENT =
      " com.example.meetingtogether.USE_VALUES_FROM_INTENT";
  public static final String EXTRA_DATA_CHANNEL_ENABLED = " com.example.meetingtogether.DATA_CHANNEL_ENABLED";
  public static final String EXTRA_ORDERED = " com.example.meetingtogether.ORDERED";
  public static final String EXTRA_MAX_RETRANSMITS_MS = " com.example.meetingtogether.MAX_RETRANSMITS_MS";
  public static final String EXTRA_MAX_RETRANSMITS = " com.example.meetingtogether.MAX_RETRANSMITS";
  public static final String EXTRA_PROTOCOL = " com.example.meetingtogether.PROTOCOL";
  public static final String EXTRA_NEGOTIATED = " com.example.meetingtogether.NEGOTIATED";
  public static final String EXTRA_ID = " com.example.meetingtogether.ID";
  public static final String EXTRA_ENABLE_RTCEVENTLOG = " com.example.meetingtogether.ENABLE_RTCEVENTLOG";

  public static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;

  // List of mandatory application permissions.
  public static final String[] MANDATORY_PERMISSIONS = {"android.permission.MODIFY_AUDIO_SETTINGS",
      "android.permission.RECORD_AUDIO", "android.permission.INTERNET"};

  // Peer connection statistics callback period in ms.
  public static final int STAT_CALLBACK_PERIOD = 1000;

  /**
   * 비디오 싱크는 비디오 데이터를 받아 화면에 표시하는 역할을 하며
   */
  public static class ProxyVideoSink implements VideoSink {
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

  private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink();
  private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
  
  @Nullable private PeerConnectionClient peerConnectionClient;
  @Nullable
  private AppRTCClient appRtcClient;
  @Nullable
  private SignalingParameters signalingParameters;
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
  private RoomConnectionParameters roomConnectionParameters;
  @Nullable
  private PeerConnectionParameters peerConnectionParameters;
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
  private CallFragment callFragment;
  private HudFragment hudFragment;
  private CpuMonitor cpuMonitor;

  @Override
  // TODO(bugs.webrtc.org/8580): LayoutParams.FLAG_TURN_SCREEN_ON and
  // LayoutParams.FLAG_SHOW_WHEN_LOCKED are deprecated.
  @SuppressWarnings("deprecation")
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    /**
     * 스레드에서 발생한 예외가 처리되지 않을 경우 호출되는 기본 예외 처리기를 설정하는 데 사용됩니다.
     */
    Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));

    // setContentView 하기 전에 window에 관한 설정을 합니다.
    /**
     * 액티비티의 창 스타일과 기능을 지정할 수 있습니다.
     * Window.FEATURE_NO_TITLE : 타이틀바 숨기기
     * Window.FEATURE_ACTION_BAR : 액션바 호출
     */
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    /**
     * getWindow() : 현재 화면에 표시되는 UI 구성 요소들과 관련된 윈도우 객체를 반환합니다.
     * getWindow().addFlags : 액티비티의 창(Window)에 특정 플래그(Flags)를 추가할 때 사용됩니다.
     * 이를 통해 액티비티의 동작과 특성을 조정할 수 있습니다.
     *
     * LayoutParams.FLAG_FULLSCREEN : 액티비티나 뷰가 전체 화면(full screen)으로 표시되도록 지정
     * LayoutParams.FLAG_KEEP_SCREEN_ON : 이 창이 사용자에게 표시되는 동안에는 기기 화면을 켜고 밝게 유지하세요.
     */
    getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_KEEP_SCREEN_ON);

    /**
     * 활동이 재개될 때 화면을 켜야 하는지 여부를 지정합니다.
     * 일반적으로 화면이 꺼져 있는 상태에서 활동을 시작하면 활동이 중지된 상태로 전환되지만,
     * 이 설정을 true로 하면 화면이 켜져서 활동이 표시되고 화면이 켜져서 활동이 재개될 경우 화면이 켜지게 됩니다.
     */
    setTurnScreenOn(true);

    /**
     * 잠금 화면이 켜져 있고 활동이 재개될 때마다 활동을 잠금 화면 위에 표시할지 여부를 지정합니다.
     */
    setShowWhenLocked(true);

    /**
     * getWindow().getDecorView() : 현재 액티비티의 루트뷰를 반환합니다.
     * 디스플레이 설정
     */
    getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());

    setContentView(R.layout.activity_call);

    connected = false;
    signalingParameters = null;

    // Create UI controls.
    pipRenderer = findViewById(R.id.pip_video_view);
    fullscreenRenderer = findViewById(R.id.fullscreen_video_view);
    callFragment = new CallFragment();
    hudFragment = new HudFragment();

    /**
     * Call Control Fragment 토글
     */
    View.OnClickListener listener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        toggleCallControlFragmentVisibility();
      }
    };
    fullscreenRenderer.setOnClickListener(listener);

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

    final Intent intent = getIntent();

    /**
     * EglBase는 EGL(EGL, Embedded-System Graphics Library)과 관련이 있으며, OpenGL ES와 하드웨어 사이의 상호 작용을 관리합니다.
     */
    final EglBase eglBase = EglBase.create();

    /**
     * pip 비디오 렌더러 생성
     */
    pipRenderer.init(eglBase.getEglBaseContext(), null);
    pipRenderer.setScalingType(ScalingType.SCALE_ASPECT_FIT);
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
    fullscreenRenderer.setScalingType(ScalingType.SCALE_ASPECT_FILL);

    pipRenderer.setZOrderMediaOverlay(true);
    pipRenderer.setEnableHardwareScaler(true /* enabled */);
    fullscreenRenderer.setEnableHardwareScaler(false /* enabled */);

    /**
     * 로컬 Feed를 full screen으로 시작합니다.
     * 그리고 전화 연결이 됐을때, swap 됩니다.
     */
    setSwappedFeeds(true /* isSwappedFeeds */);

    /**
     * 필수적인 권한 확인
     */
    for (String permission : MANDATORY_PERMISSIONS) {
      if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
        logAndToast("Permission " + permission + " is not granted");
        setResult(RESULT_CANCELED);
        finish();
        return;
      }
    }

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
    DataChannelParameters dataChannelParameters = null;

    /**
     * 데이터 채널 설정
     */
    if (intent.getBooleanExtra(EXTRA_DATA_CHANNEL_ENABLED, false)) {
      dataChannelParameters = new DataChannelParameters(intent.getBooleanExtra(EXTRA_ORDERED, true),
          intent.getIntExtra(EXTRA_MAX_RETRANSMITS_MS, -1),
          intent.getIntExtra(EXTRA_MAX_RETRANSMITS, -1), intent.getStringExtra(EXTRA_PROTOCOL),
          intent.getBooleanExtra(EXTRA_NEGOTIATED, false), intent.getIntExtra(EXTRA_ID, -1));
    }

    /**
     * Peer 연결 파라미터 생성
     */
    peerConnectionParameters =
        new PeerConnectionParameters(intent.getBooleanExtra(EXTRA_VIDEO_CALL, true), loopback,
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
     * Room 명이 IP 형태라면 DirectRTCClient를 이용하고
     * 그렇지 않을 경우엔 표준 WebSocketRTCClient를 이용합니다.
     */
    if (loopback || !DirectRTCClient.IP_PATTERN.matcher(roomId).matches()) {
      appRtcClient = new WebSocketRTCClient(this, roomId);
    } else {
      Log.i(TAG, "Using DirectRTCClient because room name looks like an IP.");
      appRtcClient = new DirectRTCClient(this);
    }

    /**
     * Room 연결 파라미터 생성
     */
    String urlParameters = intent.getStringExtra(EXTRA_URLPARAMETERS);
    roomConnectionParameters =
        new RoomConnectionParameters(roomUri.toString(), roomId, loopback, urlParameters);

    /**
     * CPU 모니터 생성
     */
    if (CpuMonitor.isSupported()) {
      cpuMonitor = new CpuMonitor(this);
      hudFragment.setCpuMonitor(cpuMonitor);
    }

    /**
     * 프레그먼트로 Intent Arguments를 보낸다.
     */
    callFragment.setArguments(intent.getExtras());
    hudFragment.setArguments(intent.getExtras());

    /**
     * Call 프레그먼트, HUD 프레그먼트를 활성화 시킵니다.
     */
    FragmentTransaction ft = getFragmentManager().beginTransaction();
    ft.add(R.id.call_fragment_container, callFragment);
    ft.add(R.id.hud_fragment_container, hudFragment);
    ft.commit();

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
     * Peer 연결 클라이언트 생성
     */
    peerConnectionClient = new PeerConnectionClient(
        getApplicationContext(), eglBase, peerConnectionParameters, CallActivity.this);

    /**
     * Peer 팩토리 생성
     */
    PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
    if (loopback) {
      options.networkIgnoreMask = 0;
    }
    peerConnectionClient.createPeerConnectionFactory(options);

    /**
     * 스크린 캡처여부에 따라 다르게 start 한다.
     */
    if (screencaptureEnabled) {
      startScreenCapture();
    } else {
      startCall();
    }
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

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
      return;
    mediaProjectionPermissionResultCode = resultCode;
    mediaProjectionPermissionResultData = data;
    startCall();
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
    if (peerConnectionClient != null && !screencaptureEnabled) {
      peerConnectionClient.stopVideoSource();
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
    if (peerConnectionClient != null && !screencaptureEnabled) {
      peerConnectionClient.startVideoSource();
    }
    if (cpuMonitor != null) {
      cpuMonitor.resume();
    }
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

  // CallFragment.OnCallEvents interface implementation.
  @Override
  public void onCallHangUp() {
    disconnect();
  }

  @Override
  public void onCameraSwitch() {
    if (peerConnectionClient != null) {
      peerConnectionClient.switchCamera();
    }
  }

  @Override
  public void onVideoScalingSwitch(ScalingType scalingType) {
    fullscreenRenderer.setScalingType(scalingType);
  }

  @Override
  public void onCaptureFormatChange(int width, int height, int framerate) {
    if (peerConnectionClient != null) {
      peerConnectionClient.changeCaptureFormat(width, height, framerate);
    }
  }

  @Override
  public boolean onToggleMic() {
    if (peerConnectionClient != null) {
      micEnabled = !micEnabled;
      peerConnectionClient.setAudioEnabled(micEnabled);
    }
    return micEnabled;
  }

  // Helper functions.
  private void toggleCallControlFragmentVisibility() {
    if (!connected || !callFragment.isAdded()) {
      return;
    }
    // Show/hide call control fragment
    callControlFragmentVisible = !callControlFragmentVisible;
    FragmentTransaction ft = getFragmentManager().beginTransaction();
    if (callControlFragmentVisible) {
      ft.show(callFragment);
      ft.show(hudFragment);
    } else {
      ft.hide(callFragment);
      ft.hide(hudFragment);
    }
    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    ft.commit();
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
    audioManager.start(new AudioManagerEvents() {
      /**
       * 이용 가능한 오디오 디바이스의 수가 바뀔때 마다 이 method가 호출될 것입니다.
       */
      @Override
      public void onAudioDeviceChanged(
          AudioDevice audioDevice, Set<AudioDevice> availableAudioDevices) {
        onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
      }
    });
  }

  // Should be called from UI thread
  private void callConnected() {
    final long delta = System.currentTimeMillis() - callStartedTimeMs;
    Log.i(TAG, "Call connected: delay=" + delta + "ms");
    if (peerConnectionClient == null || isError) {
      Log.w(TAG, "Call is connected in closed or error state");
      return;
    }
    // Enable statistics callback.
    peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
    setSwappedFeeds(false /* isSwappedFeeds */);
  }

  // This method is called when the audio manager reports audio device change,
  // e.g. from wired headset to speakerphone.
  private void onAudioManagerDevicesChanged(
      final AudioDevice device, final Set<AudioDevice> availableDevices) {
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
    if (peerConnectionClient != null) {
      peerConnectionClient.close();
      peerConnectionClient = null;
    }
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
  private void onConnectedToRoomInternal(final SignalingParameters params) {
    final long delta = System.currentTimeMillis() - callStartedTimeMs;

    signalingParameters = params;
    logAndToast("Creating peer connection, delay=" + delta + "ms");
    VideoCapturer videoCapturer = null;
    if (peerConnectionParameters.videoCallEnabled) {
      videoCapturer = createVideoCapturer();
    }
    peerConnectionClient.createPeerConnection(
        localProxyVideoSink, remoteSinks, videoCapturer, false);

    if (signalingParameters.initiator) {
      logAndToast("Creating OFFER...");
      // Create offer. Offer SDP will be sent to answering client in
      // PeerConnectionEvents.onLocalDescription event.
      peerConnectionClient.createOffer();
    } else {
      if (params.offerSdp != null) {
        peerConnectionClient.setRemoteDescription(params.offerSdp);
        logAndToast("Creating ANSWER...");
        // Create answer. Answer SDP will be sent to offering client in
        // PeerConnectionEvents.onLocalDescription event.
        peerConnectionClient.createAnswer();
      }
      if (params.iceCandidates != null) {
        // Add remote ICE candidates from room.
        for (IceCandidate iceCandidate : params.iceCandidates) {
          peerConnectionClient.addRemoteIceCandidate(iceCandidate);
        }
      }
    }
  }

  @Override
  public void onWebSocketJoined() {

  }

  @Override
  public void onWebSocketConnected(String clientId) {

  }

  @Override
  public void onUserList(List<UserModel> userList, String initiato) {

  }

  @Override
  public void onConnectedToRoom(final SignalingParameters params) {
    // work Thread에서 UI Thread로 동작 되게 시키는 것
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        onConnectedToRoomInternal(params);
      }
    });
  }

  @Override
  public void onRemoteDescription(final SessionDescription desc) {
    final long delta = System.currentTimeMillis() - callStartedTimeMs;
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (peerConnectionClient == null) {
          Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
          return;
        }
        logAndToast("Received remote " + desc.type + ", delay=" + delta + "ms");
        peerConnectionClient.setRemoteDescription(desc);
        if (!signalingParameters.initiator) {
          logAndToast("Creating ANSWER...");
          // Create answer. Answer SDP will be sent to offering client in
          // PeerConnectionEvents.onLocalDescription event.
          peerConnectionClient.createAnswer();
        }
      }
    });
  }

  @Override
  public void onRemoteIceCandidate(final IceCandidate candidate) {
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
  public void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates) {
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

  @Override
  public void onPeerCreated(PeerConnection peerConnection, Boolean isInitiator) {

  }

  // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
  // Send local peer connection SDP and ICE candidates to remote party.
  // All callbacks are invoked from peer connection client looper thread and
  // are routed to UI thread.
  @Override
  public void onLocalDescription(final SessionDescription desc) {
    final long delta = System.currentTimeMillis() - callStartedTimeMs;
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (appRtcClient != null) {
          logAndToast("Sending " + desc.type + ", delay=" + delta + "ms");
          if (signalingParameters.initiator) {
            appRtcClient.sendOfferSdp(desc);
          } else {
            appRtcClient.sendAnswerSdp(desc);
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
  public void onIceCandidate(final IceCandidate candidate) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (appRtcClient != null) {
          appRtcClient.sendLocalIceCandidate(candidate);
        }
      }
    });
  }

  @Override
  public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (appRtcClient != null) {
          appRtcClient.sendLocalIceCandidateRemovals(candidates);
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
  public void onConnected() {
    final long delta = System.currentTimeMillis() - callStartedTimeMs;
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        logAndToast("DTLS connected, delay=" + delta + "ms");
        connected = true;
        callConnected();
      }
    });
  }

  @Override
  public void onDisconnected() {
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
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (!isError && connected) {
          hudFragment.updateEncoderStatistics(report);
        }
      }
    });
  }

  @Override
  public void onPeerConnectionError(final String description) {
    reportError(description);
  }
}
