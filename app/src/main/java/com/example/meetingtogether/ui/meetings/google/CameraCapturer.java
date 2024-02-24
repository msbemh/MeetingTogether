package com.example.meetingtogether.ui.meetings.google;
import static com.example.meetingtogether.MainActivity.TAG;

import android.content.Context;
import android.graphics.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

import org.webrtc.CameraVideoCapturer;
import org.webrtc.CapturerObserver;
import org.webrtc.Logging;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoFrame;

abstract class CameraCapturer implements CameraVideoCapturer {
    private static final int MAX_OPEN_CAMERA_ATTEMPTS = 3;
    private static final int OPEN_CAMERA_DELAY_MS = 500;
    private static final int OPEN_CAMERA_TIMEOUT = 10000;
    private final CameraEnumerator cameraEnumerator;
    private final CameraEventsHandler eventsHandler;
    private final Handler uiThreadHandler;
    @Nullable
    private final CameraSession.CreateSessionCallback createSessionCallback = new CameraSession.CreateSessionCallback() {
        public void onDone(CameraSession session) {
            CameraCapturer.this.checkIsOnCameraThread();
            Log.d(TAG, "Create session done. Switch state: " + CameraCapturer.this.switchState);
            CameraCapturer.this.uiThreadHandler.removeCallbacks(CameraCapturer.this.openCameraTimeoutRunnable);
            synchronized(CameraCapturer.this.stateLock) {
                CameraCapturer.this.capturerObserver.onCapturerStarted(true);
                CameraCapturer.this.sessionOpening = false;
                CameraCapturer.this.currentSession = session;
                CameraCapturer.this.cameraStatistics = new CameraStatistics(CameraCapturer.this.surfaceHelper, CameraCapturer.this.eventsHandler);
                CameraCapturer.this.firstFrameObserved = false;
                CameraCapturer.this.stateLock.notifyAll();
                if (CameraCapturer.this.switchState == SwitchState.IN_PROGRESS) {
                    CameraCapturer.this.switchState = SwitchState.IDLE;
                    if (CameraCapturer.this.switchEventsHandler != null) {
                        CameraCapturer.this.switchEventsHandler.onCameraSwitchDone(CameraCapturer.this.cameraEnumerator.isFrontFacing(CameraCapturer.this.cameraName));
                        CameraCapturer.this.switchEventsHandler = null;
                    }
                } else if (CameraCapturer.this.switchState == SwitchState.PENDING) {
                    String selectedCameraName = CameraCapturer.this.pendingCameraName;
                    CameraCapturer.this.pendingCameraName = null;
                    CameraCapturer.this.switchState = SwitchState.IDLE;
                    CameraCapturer.this.switchCameraInternal(CameraCapturer.this.switchEventsHandler, selectedCameraName);
                }

            }
        }

        public void onFailure(CameraSession.FailureType failureType, String error) {
            CameraCapturer.this.checkIsOnCameraThread();
            CameraCapturer.this.uiThreadHandler.removeCallbacks(CameraCapturer.this.openCameraTimeoutRunnable);
            synchronized(CameraCapturer.this.stateLock) {
                CameraCapturer.this.capturerObserver.onCapturerStarted(false);
                CameraCapturer.this.openAttemptsRemaining--;
                if (CameraCapturer.this.openAttemptsRemaining <= 0) {
                    Logging.w(TAG, "Opening camera failed, passing: " + error);
                    CameraCapturer.this.sessionOpening = false;
                    CameraCapturer.this.stateLock.notifyAll();
                    if (CameraCapturer.this.switchState != SwitchState.IDLE) {
                        if (CameraCapturer.this.switchEventsHandler != null) {
                            CameraCapturer.this.switchEventsHandler.onCameraSwitchError(error);
                            CameraCapturer.this.switchEventsHandler = null;
                        }

                        CameraCapturer.this.switchState = SwitchState.IDLE;
                    }

                    if (failureType == CameraSession.FailureType.DISCONNECTED) {
                        CameraCapturer.this.eventsHandler.onCameraDisconnected();
                    } else {
                        CameraCapturer.this.eventsHandler.onCameraError(error);
                    }
                } else {
                    Logging.w(TAG, "Opening camera failed, retry: " + error);
                    CameraCapturer.this.createSessionInternal(500);
                }

            }
        }
    };
    @Nullable
    private final CameraSession.Events cameraSessionEventsHandler = new CameraSession.Events() {
        public void onCameraOpening() {
            CameraCapturer.this.checkIsOnCameraThread();
            synchronized(CameraCapturer.this.stateLock) {
                if (CameraCapturer.this.currentSession != null) {
                    Logging.w(TAG, "onCameraOpening while session was open.");
                } else {
                    CameraCapturer.this.eventsHandler.onCameraOpening(CameraCapturer.this.cameraName);
                }
            }
        }

        public void onCameraError(CameraSession session, String error) {
            CameraCapturer.this.checkIsOnCameraThread();
            synchronized(CameraCapturer.this.stateLock) {
                if (session != CameraCapturer.this.currentSession) {
                    Logging.w(TAG, "onCameraError from another session: " + error);
                } else {
                    CameraCapturer.this.eventsHandler.onCameraError(error);
                    CameraCapturer.this.stopCapture();
                }
            }
        }

        public void onCameraDisconnected(CameraSession session) {
            CameraCapturer.this.checkIsOnCameraThread();
            synchronized(CameraCapturer.this.stateLock) {
                if (session != CameraCapturer.this.currentSession) {
                    Logging.w(TAG, "onCameraDisconnected from another session.");
                } else {
                    CameraCapturer.this.eventsHandler.onCameraDisconnected();
                    CameraCapturer.this.stopCapture();
                }
            }
        }

        public void onCameraClosed(CameraSession session) {
            CameraCapturer.this.checkIsOnCameraThread();
            synchronized(CameraCapturer.this.stateLock) {
                if (session != CameraCapturer.this.currentSession && CameraCapturer.this.currentSession != null) {
                    Log.d(TAG, "onCameraClosed from another session.");
                } else {
                    CameraCapturer.this.eventsHandler.onCameraClosed();
                }
            }
        }

        public void onFrameCaptured(CameraSession session, VideoFrame frame) {
            CameraCapturer.this.checkIsOnCameraThread();
            synchronized(CameraCapturer.this.stateLock) {
                if (session != CameraCapturer.this.currentSession) {
                    Logging.w(TAG, "onFrameCaptured from another session.");
                } else {
                    if (!CameraCapturer.this.firstFrameObserved) {
                        CameraCapturer.this.eventsHandler.onFirstFrameAvailable();
                        CameraCapturer.this.firstFrameObserved = true;
                    }

                    CameraCapturer.this.cameraStatistics.addFrame();

                    CameraCapturer.this.cameraCaptureInterface.onBeforeCapture(CameraCapturer.this.capturerObserver, frame);
                }
            }
        }
    };
    private final Runnable openCameraTimeoutRunnable = new Runnable() {
        public void run() {
            CameraCapturer.this.eventsHandler.onCameraError("Camera failed to start within timeout.");
        }
    };
    private Handler cameraThreadHandler;
    private Context applicationContext;
    private CapturerObserver capturerObserver;
    private SurfaceTextureHelper surfaceHelper;
    private final Object stateLock = new Object();
    private boolean sessionOpening;
    @Nullable
    private CameraSession currentSession;
    private String cameraName;
    private String pendingCameraName;
    private int width;
    private int height;
    private int framerate;
    private int openAttemptsRemaining;
    private SwitchState switchState;
    @Nullable
    private CameraSwitchHandler switchEventsHandler;
    @Nullable
    private CameraStatistics cameraStatistics;
    private boolean firstFrameObserved;

    public CameraCapturer(String cameraName, @Nullable CameraEventsHandler eventsHandler, CameraEnumerator cameraEnumerator) {
        this.switchState = SwitchState.IDLE;
        if (eventsHandler == null) {
            eventsHandler = new CameraEventsHandler() {
                public void onCameraError(String errorDescription) {
                }

                public void onCameraDisconnected() {
                }

                public void onCameraFreezed(String errorDescription) {
                }

                public void onCameraOpening(String cameraName) {
                }

                public void onFirstFrameAvailable() {
                }

                public void onCameraClosed() {
                }
            };
        }

        this.eventsHandler = eventsHandler;
        this.cameraEnumerator = cameraEnumerator;
        this.cameraName = cameraName;
        List<String> deviceNames = Arrays.asList(cameraEnumerator.getDeviceNames());
        this.uiThreadHandler = new Handler(Looper.getMainLooper());
        if (deviceNames.isEmpty()) {
            throw new RuntimeException("No cameras attached.");
        } else if (!deviceNames.contains(this.cameraName)) {
            throw new IllegalArgumentException("Camera name " + this.cameraName + " does not match any known camera device.");
        }
    }

    public void initialize(SurfaceTextureHelper surfaceTextureHelper, Context applicationContext, CapturerObserver capturerObserver) {
        this.applicationContext = applicationContext;
        this.capturerObserver = capturerObserver;
        this.surfaceHelper = surfaceTextureHelper;
        this.cameraThreadHandler = surfaceTextureHelper.getHandler();
    }

    private CameraCaptureInterface cameraCaptureInterface;

    public void setCameraCaptureInterface(CameraCaptureInterface cameraCaptureInterface){
        this.cameraCaptureInterface = cameraCaptureInterface;
    }
    public void startCapture(int width, int height, int framerate) {
        Log.d(TAG, "startCapture: " + width + "x" + height + "@" + framerate);
        if (this.applicationContext == null) {
            throw new RuntimeException("CameraCapturer must be initialized before calling startCapture.");
        } else {
            synchronized(this.stateLock) {
                if (!this.sessionOpening && this.currentSession == null) {
                    this.width = width;
                    this.height = height;
                    this.framerate = framerate;
                    this.sessionOpening = true;
                    this.openAttemptsRemaining = 3;
                    this.createSessionInternal(0);
                } else {
                    Logging.w(TAG, "Session already open");
                }
            }
        }
    }

    private void createSessionInternal(int delayMs) {
        this.uiThreadHandler.postDelayed(this.openCameraTimeoutRunnable, (long)(delayMs + 10000));
        this.cameraThreadHandler.postDelayed(new Runnable() {
            public void run() {
                CameraCapturer.this.createCameraSession(CameraCapturer.this.createSessionCallback, CameraCapturer.this.cameraSessionEventsHandler, CameraCapturer.this.applicationContext, CameraCapturer.this.surfaceHelper, CameraCapturer.this.cameraName, CameraCapturer.this.width, CameraCapturer.this.height, CameraCapturer.this.framerate);
            }
        }, (long)delayMs);
    }

    public void stopCapture() {
        Log.d(TAG, "Stop capture");
        synchronized(this.stateLock) {
            while(this.sessionOpening) {
                Log.d(TAG, "Stop capture: Waiting for session to open");

                try {
                    this.stateLock.wait();
                } catch (InterruptedException var4) {
                    Logging.w(TAG, "Stop capture interrupted while waiting for the session to open.");
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            if (this.currentSession != null) {
                Log.d(TAG, "Stop capture: Nulling session");
                this.cameraStatistics.release();
                this.cameraStatistics = null;
                final CameraSession oldSession = this.currentSession;
                this.cameraThreadHandler.post(new Runnable() {
                    public void run() {
                        oldSession.stop();
                    }
                });
                this.currentSession = null;
                this.capturerObserver.onCapturerStopped();
            } else {
                Log.d(TAG, "Stop capture: No session open");
            }
        }

        Log.d(TAG, "Stop capture done");
    }

    public void changeCaptureFormat(int width, int height, int framerate) {
        Log.d(TAG, "changeCaptureFormat: " + width + "x" + height + "@" + framerate);
        synchronized(this.stateLock) {
            this.stopCapture();
            this.startCapture(width, height, framerate);
        }
    }

    public void dispose() {
        Log.d(TAG, "dispose");
        this.stopCapture();
    }

    public void switchCamera(final CameraSwitchHandler switchEventsHandler) {
        Log.d(TAG, "switchCamera");
        this.cameraThreadHandler.post(new Runnable() {
            public void run() {
                List<String> deviceNames = Arrays.asList(CameraCapturer.this.cameraEnumerator.getDeviceNames());
                if (deviceNames.size() < 2) {
                    CameraCapturer.this.reportCameraSwitchError("No camera to switch to.", switchEventsHandler);
                } else {
                    int cameraNameIndex = deviceNames.indexOf(CameraCapturer.this.cameraName);
                    String cameraName = (String)deviceNames.get((cameraNameIndex + 1) % deviceNames.size());
                    CameraCapturer.this.switchCameraInternal(switchEventsHandler, cameraName);
                }
            }
        });
    }

    public void switchCamera(final CameraSwitchHandler switchEventsHandler, final String cameraName) {
        Log.d(TAG, "switchCamera");
        this.cameraThreadHandler.post(new Runnable() {
            public void run() {
                CameraCapturer.this.switchCameraInternal(switchEventsHandler, cameraName);
            }
        });
    }

    public boolean isScreencast() {
        return false;
    }

    public void printStackTrace() {
        Thread cameraThread = null;
        if (this.cameraThreadHandler != null) {
            cameraThread = this.cameraThreadHandler.getLooper().getThread();
        }

        if (cameraThread != null) {
            StackTraceElement[] cameraStackTrace = cameraThread.getStackTrace();
            if (cameraStackTrace.length > 0) {
                Log.d(TAG, "CameraCapturer stack trace:");
                StackTraceElement[] var3 = cameraStackTrace;
                int var4 = cameraStackTrace.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    StackTraceElement traceElem = var3[var5];
                    Log.d(TAG, traceElem.toString());
                }
            }
        }

    }

    private void reportCameraSwitchError(String error, @Nullable CameraSwitchHandler switchEventsHandler) {
        Logging.e(TAG, error);
        if (switchEventsHandler != null) {
            switchEventsHandler.onCameraSwitchError(error);
        }

    }

    private void switchCameraInternal(@Nullable CameraSwitchHandler switchEventsHandler, String selectedCameraName) {
        Log.d(TAG, "switchCamera internal");
        List<String> deviceNames = Arrays.asList(this.cameraEnumerator.getDeviceNames());
        if (!deviceNames.contains(selectedCameraName)) {
            this.reportCameraSwitchError("Attempted to switch to unknown camera device " + selectedCameraName, switchEventsHandler);
        } else {
            synchronized(this.stateLock) {
                if (this.switchState != SwitchState.IDLE) {
                    this.reportCameraSwitchError("Camera switch already in progress.", switchEventsHandler);
                    return;
                }

                if (!this.sessionOpening && this.currentSession == null) {
                    this.reportCameraSwitchError("switchCamera: camera is not running.", switchEventsHandler);
                    return;
                }

                this.switchEventsHandler = switchEventsHandler;
                if (this.sessionOpening) {
                    this.switchState = SwitchState.PENDING;
                    this.pendingCameraName = selectedCameraName;
                    return;
                }

                this.switchState = SwitchState.IN_PROGRESS;
                Log.d(TAG, "switchCamera: Stopping session");
                this.cameraStatistics.release();
                this.cameraStatistics = null;
                final CameraSession oldSession = this.currentSession;
                this.cameraThreadHandler.post(new Runnable() {
                    public void run() {
                        oldSession.stop();
                    }
                });
                this.currentSession = null;
                this.cameraName = selectedCameraName;
                this.sessionOpening = true;
                this.openAttemptsRemaining = 1;
                this.createSessionInternal(0);
            }

            Log.d(TAG, "switchCamera done");
        }
    }

    private void checkIsOnCameraThread() {
        if (Thread.currentThread() != this.cameraThreadHandler.getLooper().getThread()) {
            Logging.e(TAG, "Check is on camera thread failed.");
            throw new RuntimeException("Not on camera thread.");
        }
    }

    protected String getCameraName() {
        synchronized(this.stateLock) {
            return this.cameraName;
        }
    }

    protected abstract void createCameraSession(CameraSession.CreateSessionCallback var1, CameraSession.Events var2, Context var3, SurfaceTextureHelper var4, String var5, int var6, int var7, int var8);

    static enum SwitchState {
        IDLE,
        PENDING,
        IN_PROGRESS;

        private SwitchState() {
        }
    }
}

