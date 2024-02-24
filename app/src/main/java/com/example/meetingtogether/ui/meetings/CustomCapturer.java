package com.example.meetingtogether.ui.meetings;

import static com.example.meetingtogether.MainActivity.TAG;

import android.content.Context;
import android.util.Log;

import com.example.meetingtogether.common.Common;
import com.example.meetingtogether.ui.meetings.google.Camera2Enumerator;
import com.example.meetingtogether.ui.meetings.google.Camera2Enumerator;

import org.webrtc.AudioSource;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;

public class CustomCapturer {
    private VideoCapturer videoCapturer;
    private String type;
    private VideoSource videoSource;
    private AudioSource audioSource;
    private Context context;

    public CustomCapturer(String type, Context context) {
        this.type = type;
        this.context = context;

        if(Common.VIDEO.equals(type)){
            this.videoCapturer = createVideoCapturer(true);
        }
    }

    public CustomCapturer(String type, Context context, boolean isFront) {
        this.type = type;
        this.context = context;

        if(Common.VIDEO.equals(type)){
            this.videoCapturer = createVideoCapturer(isFront);
        }
    }

    public VideoCapturer createVideoCapturer(boolean isFront) {
        VideoCapturer videoCapturer;
        videoCapturer = createCameraCapturer(new Camera2Enumerator(context), isFront);

//        if (useCamera2()) {
//            videoCapturer = createCameraCapturer(new CustomCamera2Enumerator(context));
//        } else {
////            videoCapturer = createCameraCapturer((CameraEnumerator) new Camera1Enumerator(true));
//        }
        return videoCapturer;
    }

    private VideoCapturer createCameraCapturer(Camera2Enumerator enumerator, boolean isFront) {
        final String[] deviceNames = enumerator.getDeviceNames();

        for (String deviceName : deviceNames) {
            if (isFront ? enumerator.isFrontFacing(deviceName) : enumerator.isBackFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, new CameraVideoCapturer.CameraEventsHandler() {

                    @Override
                    public void onCameraError(String s) {
                        Log.d(TAG, "[onCameraError]:" + s);
                    }

                    @Override
                    public void onCameraDisconnected() {
                        Log.d(TAG, "[onCameraDisconnected]");
                    }

                    @Override
                    public void onCameraFreezed(String s) {
                        Log.d(TAG, "[onCameraFreezed]:" + s);
                    }

                    @Override
                    public void onCameraOpening(String s) {
                        Log.d(TAG, "[onCameraOpening]:" + s);
                    }

                    @Override
                    public void onFirstFrameAvailable() {
                        Log.d(TAG, "[onFirstFrameAvailable]");
                    }

                    @Override
                    public void onCameraClosed() {
                        Log.d(TAG, "[onCameraClosed]");
                    }
                });

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, new CameraVideoCapturer.CameraEventsHandler() {

                    @Override
                    public void onCameraError(String s) {
                        Log.d(TAG, "[onCameraError]:" + s);
                    }

                    @Override
                    public void onCameraDisconnected() {
                        Log.d(TAG, "[onCameraDisconnected]");
                    }

                    @Override
                    public void onCameraFreezed(String s) {
                        Log.d(TAG, "[onCameraFreezed]:" + s);
                    }

                    @Override
                    public void onCameraOpening(String s) {
                        Log.d(TAG, "[onCameraOpening]:" + s);
                    }

                    @Override
                    public void onFirstFrameAvailable() {
                        Log.d(TAG, "[onFirstFrameAvailable]");
                    }

                    @Override
                    public void onCameraClosed() {
                        Log.d(TAG, "[onCameraClosed]");
                    }
                });

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(context);
    }

    public void setVideoCapturer(VideoCapturer videoCapturer) {
        this.videoCapturer = videoCapturer;
    }

    public void setType(String type) {
        this.type = type;
    }

    public VideoCapturer getVideoCapturer() {
        return videoCapturer;
    }

    public String getType() {
        return type;
    }

    public VideoSource getVideoSource() {
        return videoSource;
    }

    public void setVideoSource(VideoSource videoSource) {
        this.videoSource = videoSource;
    }

    public AudioSource getAudioSource() {
        return audioSource;
    }

    public void setAudioSource(AudioSource audioSource) {
        this.audioSource = audioSource;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
