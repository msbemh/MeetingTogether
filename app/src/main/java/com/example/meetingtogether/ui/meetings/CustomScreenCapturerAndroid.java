package com.example.meetingtogether.ui.meetings;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.view.Surface;
import androidx.annotation.Nullable;

import org.webrtc.CapturerObserver;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.ThreadUtils;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

@TargetApi(21)
public class CustomScreenCapturerAndroid implements VideoCapturer, VideoSink {
    private static final int DISPLAY_FLAGS = 3;
    private static final int VIRTUAL_DISPLAY_DPI = 400;
    private final Intent mediaProjectionPermissionResultData;
    private final MediaProjection.Callback mediaProjectionCallback;
    private int width;
    private int height;
    @Nullable
    private VirtualDisplay virtualDisplay;
    @Nullable
    private SurfaceTextureHelper surfaceTextureHelper;
    @Nullable
    private CapturerObserver capturerObserver;
    private long numCapturedFrames;
    @Nullable
    private MediaProjection mediaProjection;
    private boolean isDisposed;
    @Nullable
    private MediaProjectionManager mediaProjectionManager;

    public CustomScreenCapturerAndroid(Intent mediaProjectionPermissionResultData, MediaProjection.Callback mediaProjectionCallback) {
        this.mediaProjectionPermissionResultData = mediaProjectionPermissionResultData;
        this.mediaProjectionCallback = mediaProjectionCallback;
    }

    private void checkNotDisposed() {
        if (this.isDisposed) {
            throw new RuntimeException("capturer is disposed.");
        }
    }

    @Nullable
    public MediaProjection getMediaProjection() {
        return this.mediaProjection;
    }

    public synchronized void initialize(SurfaceTextureHelper surfaceTextureHelper, Context applicationContext, CapturerObserver capturerObserver) {
        this.checkNotDisposed();
        if (capturerObserver == null) {
            throw new RuntimeException("capturerObserver not set.");
        } else {
            this.capturerObserver = capturerObserver;
            if (surfaceTextureHelper == null) {
                throw new RuntimeException("surfaceTextureHelper not set.");
            } else {
                this.surfaceTextureHelper = surfaceTextureHelper;
                this.mediaProjectionManager = (MediaProjectionManager)applicationContext.getSystemService("media_projection");
            }
        }
    }

    public synchronized void startCapture(int width, int height, int ignoredFramerate) {
        this.checkNotDisposed();
        this.width = width;
        this.height = height;
        this.mediaProjection = this.mediaProjectionManager.getMediaProjection(-1, this.mediaProjectionPermissionResultData);
        this.mediaProjection.registerCallback(this.mediaProjectionCallback, this.surfaceTextureHelper.getHandler());
        this.createVirtualDisplay();
        this.capturerObserver.onCapturerStarted(true);
        this.surfaceTextureHelper.startListening(this);
    }

    public synchronized void stopCapture() {
        this.checkNotDisposed();
        ThreadUtils.invokeAtFrontUninterruptibly(this.surfaceTextureHelper.getHandler(), new Runnable() {
            public void run() {
                CustomScreenCapturerAndroid.this.surfaceTextureHelper.stopListening();
                CustomScreenCapturerAndroid.this.capturerObserver.onCapturerStopped();
                if (CustomScreenCapturerAndroid.this.virtualDisplay != null) {
                    CustomScreenCapturerAndroid.this.virtualDisplay.release();
                    CustomScreenCapturerAndroid.this.virtualDisplay = null;
                }

                if (CustomScreenCapturerAndroid.this.mediaProjection != null) {
                    CustomScreenCapturerAndroid.this.mediaProjection.unregisterCallback(CustomScreenCapturerAndroid.this.mediaProjectionCallback);
                    CustomScreenCapturerAndroid.this.mediaProjection.stop();
                    CustomScreenCapturerAndroid.this.mediaProjection = null;
                }

            }
        });
    }

    public synchronized void dispose() {
        this.isDisposed = true;
    }

    public synchronized void changeCaptureFormat(int width, int height, int ignoredFramerate) {
        this.checkNotDisposed();
        this.width = width;
        this.height = height;
        if (this.virtualDisplay != null) {
            ThreadUtils.invokeAtFrontUninterruptibly(this.surfaceTextureHelper.getHandler(), new Runnable() {
                public void run() {
                    CustomScreenCapturerAndroid.this.virtualDisplay.release();
                    CustomScreenCapturerAndroid.this.createVirtualDisplay();
                }
            });
        }
    }

    private void createVirtualDisplay() {
        this.surfaceTextureHelper.setTextureSize(this.width, this.height);
        this.virtualDisplay = this.mediaProjection.createVirtualDisplay("WebRTC_ScreenCapture", this.width, this.height, 400, 3, new Surface(this.surfaceTextureHelper.getSurfaceTexture()), (VirtualDisplay.Callback)null, (Handler)null);
    }

    private FrameInterface frameInterface;

    public interface FrameInterface{
        void onFrame(VideoFrame frame);
    }

    public void setInterface(FrameInterface frameInterface){
        this.frameInterface = frameInterface;
    }

    public void onFrame(VideoFrame frame) {
        ++this.numCapturedFrames;
//        if(this.frameInterface != null){
//            this.frameInterface.onFrame(frame);
//        }else{
//            this.capturerObserver.onFrameCaptured(frame);
//        }
        this.capturerObserver.onFrameCaptured(frame);
    }

    public void onFrameCaptured(VideoFrame frame){
        this.capturerObserver.onFrameCaptured(frame);
    }

    public boolean isScreencast() {
        return true;
    }

    public long getNumCapturedFrames() {
        return this.numCapturedFrames;
    }
}
