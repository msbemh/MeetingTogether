package com.example.meetingtogether.ui.meetings;

import static com.example.meetingtogether.MainActivity.TAG;
import static com.example.meetingtogether.common.Util.WEBRTC_PEER;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;

import com.example.meetingtogether.R;

import org.webrtc.Logging;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;
import org.webrtc.VideoTrack;

public class  ProxySink implements VideoSink {
    private String type;
    private String clientId;
    private VideoTrack videoTrack;
    private FrameLayout frameLayout;


    public ProxySink(String clientId, String type, FrameLayout frameLayout, Context context, int size) {
        this.type = type;
        this.clientId = clientId;
        this.frameLayout = frameLayout;
    }

    public ProxySink(String clientId, String type, Context context, int size) {
        this.type = type;
        this.clientId = clientId;
        this.frameLayout = new FrameLayout(context);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                size,
                size);

        this.frameLayout.setPadding(10, 10, 10, 10);
        this.frameLayout.setLayoutParams(layoutParams);
    }

    public VideoTrack getVideoTrack() {
        return videoTrack;
    }

    public void setVideoTrack(VideoTrack videoTrack) {
        this.videoTrack = videoTrack;
    }

    @Override
    public void onFrame(VideoFrame videoFrame) {
        SurfaceViewRenderer alreadySurfaceViewRenderer = (SurfaceViewRenderer) frameLayout.getChildAt(0);

        if (alreadySurfaceViewRenderer == null) {
            Logging.d(WEBRTC_PEER, "Dropping frame in proxy because target is null.");
            return;
        }

        alreadySurfaceViewRenderer.onFrame(videoFrame);
    }

    synchronized public void setTarget(VideoSink target) {
        SurfaceViewRenderer alreadySurfaceViewRenderer = (SurfaceViewRenderer) frameLayout.getChildAt(0);
        SurfaceViewRenderer targetSurfaceViewRenderer = (SurfaceViewRenderer) target;
        if(alreadySurfaceViewRenderer == null){
            frameLayout.addView(targetSurfaceViewRenderer, 0);
        }
    }
    synchronized public void setType(String type) {
        this.type = type;
    }
    synchronized public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public VideoSink getTarget() {
        SurfaceViewRenderer alreadySurfaceViewRenderer = (SurfaceViewRenderer) frameLayout.getChildAt(0);
        return alreadySurfaceViewRenderer;
    }

    public String getType() {
        return type;
    }

    public String getClientId() {
        return clientId;
    }

    public FrameLayout getFrameLayout() {
        return frameLayout;
    }

    public void setFrameLayout(FrameLayout frameLayout){
        this.frameLayout = frameLayout;
    }

}
