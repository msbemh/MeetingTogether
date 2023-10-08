package com.example.meetingtogether.ui.meetings;


import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.Nullable;

import org.webrtc.EglBase;
import org.webrtc.EglRenderer;
import org.webrtc.GlRectDrawer;
import org.webrtc.GlTextureFrameBuffer;
import org.webrtc.GlUtil;
import org.webrtc.RendererCommon;
import org.webrtc.ThreadUtils;
import org.webrtc.VideoFrame;
import org.webrtc.VideoFrameDrawer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class CustomEglRenderer extends EglRenderer {

    public Matrix drawMatrix;
    public VideoFrameDrawer frameDrawer;
    public CustomVideoFrameDrawer customFrameDrawer;
    public GlTextureFrameBuffer bitmapTextureFramebuffer;
    public RendererCommon.GlDrawer drawer;
    public RendererCommon.GlDrawer drawer2;
    public Object handlerLock;
    public Handler renderThreadHandler;

    public CustomEglRenderer(String name) {
        this(name, new VideoFrameDrawer());
    }

    public CustomEglRenderer(String name, VideoFrameDrawer videoFrameDrawer) {
        super(name, videoFrameDrawer);

        this.drawMatrix = new Matrix();
        this.bitmapTextureFramebuffer = new GlTextureFrameBuffer(6408);
        this.frameDrawer = videoFrameDrawer;
        this.handlerLock = new Object();

        this.customFrameDrawer = new CustomVideoFrameDrawer();
    }

    public void init(@Nullable EglBase.Context sharedContext, int[] configAttributes, RendererCommon.GlDrawer drawer) {
        this.init(sharedContext, configAttributes, drawer, false);
    }

    public void init(@Nullable EglBase.Context sharedContext, int[] configAttributes, RendererCommon.GlDrawer drawer, boolean usePresentationTimeStamp) {
        super.init(sharedContext, configAttributes, drawer, false);
        synchronized(this.handlerLock) {
            if (this.renderThreadHandler != null) {
                throw new IllegalStateException(this.name + "Already initialized");
            } else {
                this.drawer = drawer;
                this.drawer2 = new GlRectDrawer();
            }
        }
    }


    public synchronized Bitmap convertVideoFrameToBitmap(VideoFrame frame){
        this.drawMatrix.reset();
        this.drawMatrix.preTranslate(0.5F, 0.5F);
        this.drawMatrix.preScale(1.0F , 1.0F);
        this.drawMatrix.preScale(1.0F, -1.0F);
        this.drawMatrix.preTranslate(-0.5F, -0.5F);

        int scaledWidth = (int)(1 * (float)frame.getRotatedWidth());
        int scaledHeight = (int)(1 * (float)frame.getRotatedHeight());
        if (scaledWidth != 0 && scaledHeight != 0) {
            this.bitmapTextureFramebuffer.setSize(scaledWidth, scaledHeight);
            GLES20.glBindFramebuffer(36160, this.bitmapTextureFramebuffer.getFrameBufferId());
            GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.bitmapTextureFramebuffer.getTextureId(), 0);
            GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            GLES20.glClear(16384);

            this.customFrameDrawer.drawFrame(frame, this.drawer2, this.drawMatrix, 0, 0, scaledWidth, scaledHeight);

            ByteBuffer bitmapBuffer = ByteBuffer.allocateDirect(scaledWidth * scaledHeight * 4);
            GLES20.glViewport(0, 0, scaledWidth, scaledHeight);
            GLES20.glReadPixels(0, 0, scaledWidth, scaledHeight, 6408, 5121, bitmapBuffer);
            GLES20.glBindFramebuffer(36160, 0);
            GlUtil.checkNoGLES2Error("EglRenderer.notifyCallbacks");
            Bitmap bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(bitmapBuffer);
            return bitmap;
        }

        return null;
    }

//    private void notifyCallbacks(VideoFrame frame, boolean wasRendered) {
//            this.drawMatrix.reset();
//            this.drawMatrix.preTranslate(0.5F, 0.5F);
//            this.drawMatrix.preScale(1.0F , 1.0F);
//            this.drawMatrix.preScale(1.0F, -1.0F);
//            this.drawMatrix.preTranslate(-0.5F, -0.5F);
//            Iterator<FrameListenerAndParams> it = this.frameListeners.iterator();
//
//            while(true) {
//                while(true) {
//                    FrameListenerAndParams listenerAndParams;
//                    do {
//                        if (!it.hasNext()) {
//                            return;
//                        }
//
//                        listenerAndParams = (FrameListenerAndParams)it.next();
//                    } while(!wasRendered && listenerAndParams.applyFpsReduction);
//
//                    it.remove();
//                    int scaledWidth = (int)(listenerAndParams.scale * (float)frame.getRotatedWidth());
//                    int scaledHeight = (int)(listenerAndParams.scale * (float)frame.getRotatedHeight());
//                    if (scaledWidth != 0 && scaledHeight != 0) {
//                        this.bitmapTextureFramebuffer.setSize(scaledWidth, scaledHeight);
//                        GLES20.glBindFramebuffer(36160, this.bitmapTextureFramebuffer.getFrameBufferId());
//                        GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.bitmapTextureFramebuffer.getTextureId(), 0);
//                        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
//                        GLES20.glClear(16384);
//                        this.frameDrawer.drawFrame(frame, listenerAndParams.drawer, this.drawMatrix, 0, 0, scaledWidth, scaledHeight);
//                        ByteBuffer bitmapBuffer = ByteBuffer.allocateDirect(scaledWidth * scaledHeight * 4);
//                        GLES20.glViewport(0, 0, scaledWidth, scaledHeight);
//                        GLES20.glReadPixels(0, 0, scaledWidth, scaledHeight, 6408, 5121, bitmapBuffer);
//                        GLES20.glBindFramebuffer(36160, 0);
//                        GlUtil.checkNoGLES2Error("EglRenderer.notifyCallbacks");
//                        Bitmap bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
//                        bitmap.copyPixelsFromBuffer(bitmapBuffer);
//                    }
//                }
//            }
//    }
}

