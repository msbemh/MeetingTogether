package com.example.meetingtogether.ui.meetings;

import org.webrtc.Logging;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

public class  ProxySink implements VideoSink {
    private VideoSink target;
    private String type;
    private String clientId;
    private String TAG = "TEST";

    public ProxySink(String clientId, String type) {
        this.type = type;
        this.clientId = clientId;
    }

    @Override
    public void onFrame(VideoFrame videoFrame) {
        if (target == null) {
            Logging.d(TAG, "Dropping frame in proxy because target is null.");
            return;
        }

        target.onFrame(videoFrame);
    }

    synchronized public void setTarget(VideoSink target) {
        this.target = target;
    }
    synchronized public void setType(String type) {
        this.type = type;
    }
    synchronized public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public VideoSink getTarget() {
        return target;
    }

    public String getType() {
        return type;
    }

    public String getClientId() {
        return clientId;
    }


}
