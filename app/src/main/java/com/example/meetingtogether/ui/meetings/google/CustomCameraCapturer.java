package com.example.meetingtogether.ui.meetings.google;

import android.content.Context;
import android.hardware.camera2.CameraManager;

import androidx.annotation.Nullable;

import org.webrtc.SurfaceTextureHelper;

public class CustomCameraCapturer extends Camera2Capturer{
    private final Context context;
    @Nullable
    private final CameraManager cameraManager;

    public CustomCameraCapturer(Context context, String cameraName, CameraEventsHandler eventsHandler, Context context1, @Nullable CameraManager cameraManager) {
        super(context, cameraName, eventsHandler);
        this.context = context1;
        this.cameraManager = cameraManager;
    }

    protected void createCameraSession(CameraSession.CreateSessionCallback createSessionCallback, CameraSession.Events events, Context applicationContext, SurfaceTextureHelper surfaceTextureHelper, String cameraName, int width, int height, int framerate) {
        Camera2Session.create(createSessionCallback, events, applicationContext, this.cameraManager, surfaceTextureHelper, cameraName, width, height, framerate);
    }
}
