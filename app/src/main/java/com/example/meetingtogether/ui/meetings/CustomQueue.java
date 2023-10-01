package com.example.meetingtogether.ui.meetings;

import org.webrtc.IceCandidate;

import java.util.LinkedList;
import java.util.Queue;

public class CustomQueue {
    private Queue<IceCandidate> queue;
    private String type;
    private String clientId;
    private String TAG = "TEST";

    public CustomQueue(String clientId, String type) {
        this.queue = new LinkedList<>();
        this.type = type;
        this.clientId = clientId;
    }

    public void setQueue(Queue<IceCandidate> queue) {
        this.queue = queue;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Queue<IceCandidate> getQueue() {
        return queue;
    }

    public String getType() {
        return type;
    }

    public String getClientId() {
        return clientId;
    }
}
