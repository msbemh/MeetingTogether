package com.example.meetingtogether.ui.meetings;

import org.webrtc.AudioTrack;
import org.webrtc.VideoTrack;

public class CustomTrack {
    private VideoTrack videoTrack;
    private AudioTrack audioTrack;
    private String type;

    public CustomTrack(String type) {
        this.type = type;
    }

    public VideoTrack getVideoTrack() {
        return videoTrack;
    }

    public void setVideoTrack(VideoTrack videoTrack) {
        this.videoTrack = videoTrack;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AudioTrack getAudioTrack() {
        return audioTrack;
    }

    public void setAudioTrack(AudioTrack audioTrack) {
        this.audioTrack = audioTrack;
    }
}
