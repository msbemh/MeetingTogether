package com.example.meetingtogether.ui.meetings;

import android.util.Log;

import org.webrtc.CandidatePairChangeEvent;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;

import java.util.ArrayList;

public class CustomPeerConnection {

    private String TAG = "TEST";
    private Observer observer;
    private PeerConnection peerConnection;
    private String clientId;
    private String type;
    private DataChannel dataChannel;

    public CustomPeerConnection(PeerConnectionFactory factory, String clientId, String type, Observer observer){
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();

        String STUN_URL = "stun:stun.l.google.com:19302";
        iceServers.add(new PeerConnection.IceServer(STUN_URL));

        this.observer = observer;
        this.clientId = clientId;
        this.type = type;

        String TURN_URL = "turn:webrtc-sfu.kro.kr?transport=tcp";
        String userName = "song";
        String password = "Alshalsh92@";
        iceServers.add(new PeerConnection.IceServer(TURN_URL, userName, password));

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
//        MediaConstraints pcConstraints = new MediaConstraints();

        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                CustomPeerConnection.this.observer.onSignalingChange(signalingState);
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                CustomPeerConnection.this.observer.onIceConnectionChange(iceConnectionState, CustomPeerConnection.this);
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                CustomPeerConnection.this.observer.onIceConnectionReceivingChange(b);
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                CustomPeerConnection.this.observer.onIceGatheringChange(iceGatheringState);
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                CustomPeerConnection.this.observer.onIceCandidate(iceCandidate);
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                CustomPeerConnection.this.observer.onIceCandidatesRemoved(iceCandidates);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                CustomPeerConnection.this.observer.onAddStream(mediaStream);
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                CustomPeerConnection.this.observer.onRemoveStream(mediaStream);
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                CustomPeerConnection.this.observer.onDataChannel(dataChannel);
            }

            @Override
            public void onRenegotiationNeeded() {
                CustomPeerConnection.this.observer.onRenegotiationNeeded();
            }

            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                CustomPeerConnection.this.observer.onAddTrack(rtpReceiver, mediaStreams);
            }
        };

        this.peerConnection = factory.createPeerConnection(rtcConfig, pcObserver);

        DataChannel.Init init = new DataChannel.Init();
        init.ordered = true;
        init.negotiated = false;
        init.maxRetransmits = -1;
        init.maxRetransmitTimeMs = 0;
        init.id = -1;
        init.protocol = "";

        DataChannel dataChannel = this.peerConnection.createDataChannel(clientId + "(" + type + ")", init);
        setDataChannel(dataChannel);
        Log.d(TAG, "데이터 채널 생성 완료");
    }

    public PeerConnection getPeerConnection(){
        return this.peerConnection;
    }

    public interface Observer {
        void onSignalingChange(PeerConnection.SignalingState var1);

        void onIceConnectionChange(PeerConnection.IceConnectionState var1, CustomPeerConnection customPeerConnection);

        default void onStandardizedIceConnectionChange(PeerConnection.IceConnectionState newState) {}

        default void onConnectionChange(PeerConnection.PeerConnectionState newState) {}

        void onIceConnectionReceivingChange(boolean var1);

        void onIceGatheringChange(PeerConnection.IceGatheringState var1);

        void onIceCandidate(IceCandidate var1);

        void onIceCandidatesRemoved(IceCandidate[] var1);

        default void onSelectedCandidatePairChanged(CandidatePairChangeEvent event) {}

        void onAddStream(MediaStream var1);

        void onRemoveStream(MediaStream var1);

        void onDataChannel(DataChannel var1);

        void onRenegotiationNeeded();

        void onAddTrack(RtpReceiver var1, MediaStream[] var2);

        default void onTrack(RtpTransceiver transceiver) {}
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClientId() {
        return clientId;
    }

    public String getType() {
        return type;
    }
    public void setDataChannel(DataChannel dataChannel){
        this.dataChannel = dataChannel;
    }

    public DataChannel getDataChannel(){
        return this.dataChannel;
    }
}

