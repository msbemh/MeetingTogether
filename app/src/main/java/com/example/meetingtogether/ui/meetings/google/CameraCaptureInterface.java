package com.example.meetingtogether.ui.meetings.google;

import org.webrtc.CapturerObserver;
import org.webrtc.VideoFrame;

public interface CameraCaptureInterface {
    void onBeforeCapture(CapturerObserver capturerObserver, VideoFrame frame);
}
