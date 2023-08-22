/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.example.meetingtogether.ui.meetings.google;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.meetingtogether.common.Constants;
import com.example.meetingtogether.ui.meetings.RoomParametersFetcher;
import com.example.meetingtogether.ui.meetings.UserModel;
import com.example.meetingtogether.ui.meetings.google.util.AsyncHttpURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * Negotiates signaling for chatting with https://apprtc.webrtcserver.cn/ "rooms".
 * Uses the client<->server specifics of the apprtc AppEngine webapp.
 *
 * <p>To use: create an instance of this object (registering a message handler) and
 * call connectToRoom().  Once room connection is established
 * onConnectedToRoom() callback with room parameters is invoked.
 * Messages to other party (with local Ice candidates and answer SDP) can
 * be sent after WebSocket connection is established.
 */
public class WebSocketRTCClient implements AppRTCClient, WebSocketChannelClient.WebSocketChannelEvents {
  private static final String TAG = "TEST";
  private static final String ROOM_JOIN = "join";
  private static final String ROOM_MESSAGE = "message";
  private static final String ROOM_LEAVE = "leave";

  private enum ConnectionState { NEW, CONNECTED, CLOSED, ERROR }

  private enum MessageType { MESSAGE, LEAVE }

  private final Handler handler;
  private boolean initiator;
  private SignalingEvents events;
  private WebSocketChannelClient wsClient;
  private ConnectionState roomState;
  private RoomConnectionParameters connectionParameters;
  private String messageUrl;
  private String leaveUrl;
  private String roomId;

  public WebSocketRTCClient(SignalingEvents events, String roomId) {
    this.events = events;
    roomState = ConnectionState.NEW;
    this.roomId = roomId;

    /**
     * 이곳에서의 handler는 백그라운드 작업을 위해서 새로운 HandlerThread를 생성합니다.
     */
    final HandlerThread handlerThread = new HandlerThread(TAG);
    // 루퍼 시작
    handlerThread.start();
    // 해당 핸들러는 새로 생성한 thread의 루퍼와 연결됩니다.
    // 메인 스레드 아님!!
    handler = new Handler(handlerThread.getLooper());
  }

  // --------------------------------------------------------------------
  // AppRTCClient interface implementation.
  // Asynchronously connect to an AppRTC room URL using supplied connection
  // parameters, retrieves room parameters and connect to WebSocket server.
  @Override
  public void connectToRoom(RoomConnectionParameters connectionParameters) {
    this.connectionParameters = connectionParameters;
    handler.post(new Runnable() {
      // 백그라운드 작업 수행
      @Override
      public void run() {
        connectToRoomInternal();
      }
    });
  }

  @Override
  public void disconnectFromRoom() {
    handler.post(new Runnable() {
      @Override
      public void run() {
        disconnectFromRoomInternal();
        handler.getLooper().quit();
      }
    });
  }

  /**
   * Room 과 연결
   * 해당 function은 local looper thread 위에서 동작합니다.
   */
  private void connectToRoomInternal() {
    String connectionUrl = getConnectionUrl(connectionParameters);
    Log.d(TAG, "Connect to room: " + connectionUrl);

    roomState = ConnectionState.NEW;

    wsClient = new WebSocketChannelClient(handler, this, roomId);

    roomState = ConnectionState.CONNECTED;

    wsClient.connect(Constants.CHAT_SERVER_URL);
  }

  // Disconnect from room and send bye messages - runs on a local looper thread.
  private void disconnectFromRoomInternal() {
    Log.d(TAG, "Disconnect. Room state: " + roomState);
    if (roomState == ConnectionState.CONNECTED) {
      Log.d(TAG, "Closing room.");
//      sendPostMessage(MessageType.LEAVE, leaveUrl, null);
    }

    roomState = ConnectionState.CLOSED;
    if (wsClient != null) {
      wsClient.disconnect(true);
    }
  }

  // Helper functions to get connection, post message and leave message URLs
  private String getConnectionUrl(RoomConnectionParameters connectionParameters) {
    return connectionParameters.roomUrl + "/" + ROOM_JOIN + "/" + connectionParameters.roomId
        + getQueryString(connectionParameters);
  }

  private String getMessageUrl(
      RoomConnectionParameters connectionParameters, SignalingParameters signalingParameters) {
    return connectionParameters.roomUrl + "/" + ROOM_MESSAGE + "/" + connectionParameters.roomId
        + "/" + signalingParameters.clientId + getQueryString(connectionParameters);
  }

  private String getLeaveUrl(
      RoomConnectionParameters connectionParameters, SignalingParameters signalingParameters) {
    return connectionParameters.roomUrl + "/" + ROOM_LEAVE + "/" + connectionParameters.roomId + "/"
        + signalingParameters.clientId + getQueryString(connectionParameters);
  }

  private String getQueryString(RoomConnectionParameters connectionParameters) {
    if (connectionParameters.urlParameters != null) {
      return "?" + connectionParameters.urlParameters;
    } else {
      return "";
    }
  }

  // Callback issued when room parameters are extracted. Runs on local
  // looper thread.

  /**
   * Room 파라미터가 추출 되었을 때, 콜백이 호출 됩니다.
   * local looper thread 위에서 동작 됩니다.
   */
//  private void signalingParametersReady(final SignalingParameters signalingParameters) {
//    Log.d(TAG, "Room connection completed.");
//    if (connectionParameters.loopback
//        && (!signalingParameters.initiator || signalingParameters.offerSdp != null)) {
//      reportError("Loopback room is busy.");
//      return;
//    }
//    if (!connectionParameters.loopback && !signalingParameters.initiator
//        && signalingParameters.offerSdp == null) {
//      Log.w(TAG, "No offer SDP in room response.");
//    }
//    initiator = signalingParameters.initiator;
//    messageUrl = getMessageUrl(connectionParameters, signalingParameters);
//    leaveUrl = getLeaveUrl(connectionParameters, signalingParameters);
//    Log.d(TAG, "Message URL: " + messageUrl);
//    Log.d(TAG, "Leave URL: " + leaveUrl);
//    roomState = ConnectionState.CONNECTED;
//
//    // Fire connection and signaling parameters events.
//    events.onConnectedToRoom(signalingParameters);
//
//    // Connect and register WebSocket client.
////    wsClient.connect(signalingParameters.wssUrl, signalingParameters.wssPostUrl);
////    wsClient.register(connectionParameters.roomId, signalingParameters.clientId);
//  }

  // Send local offer SDP to the other participant.
  @Override
  public void sendOfferSdp(final SessionDescription sdp) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        if (roomState != ConnectionState.CONNECTED) {
          reportError("Sending offer SDP in non connected state.");
          return;
        }
        JSONObject json = new JSONObject();
        jsonPut(json, "sdp", sdp.description);
        jsonPut(json, "type", "offer");
//        sendPostMessage(MessageType.MESSAGE, messageUrl, json.toString());
        if (connectionParameters.loopback) {
          // In loopback mode rename this offer to answer and route it back.
          SessionDescription sdpAnswer = new SessionDescription(
              SessionDescription.Type.fromCanonicalForm("answer"), sdp.description);
          events.onRemoteDescription(sdpAnswer);
        }
      }
    });
  }

  // Send local answer SDP to the other participant.
  @Override
  public void sendAnswerSdp(final SessionDescription sdp) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        if (connectionParameters.loopback) {
          Log.e(TAG, "Sending answer in loopback mode.");
          return;
        }
        JSONObject json = new JSONObject();
        jsonPut(json, "sdp", sdp.description);
        jsonPut(json, "type", "answer");
        wsClient.send(json.toString());
      }
    });
  }

  // Send Ice candidate to the other participant.
  @Override
  public void sendLocalIceCandidate(final IceCandidate candidate) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        JSONObject json = new JSONObject();
        jsonPut(json, "type", "candidate");
        jsonPut(json, "label", candidate.sdpMLineIndex);
        jsonPut(json, "id", candidate.sdpMid);
        jsonPut(json, "candidate", candidate.sdp);
        if (initiator) {
          // Call initiator sends ice candidates to GAE server.
          if (roomState != ConnectionState.CONNECTED) {
            reportError("Sending ICE candidate in non connected state.");
            return;
          }
//          sendPostMessage(MessageType.MESSAGE, messageUrl, json.toString());
          if (connectionParameters.loopback) {
            events.onRemoteIceCandidate(candidate);
          }
        } else {
          // Call receiver sends ice candidates to websocket server.
          wsClient.send(json.toString());
        }
      }
    });
  }

  // Send removed Ice candidates to the other participant.
  @Override
  public void sendLocalIceCandidateRemovals(final IceCandidate[] candidates) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        JSONObject json = new JSONObject();
        jsonPut(json, "type", "remove-candidates");
        JSONArray jsonArray = new JSONArray();
        for (final IceCandidate candidate : candidates) {
          jsonArray.put(toJsonCandidate(candidate));
        }
        jsonPut(json, "candidates", jsonArray);
        if (initiator) {
          // Call initiator sends ice candidates to GAE server.
          if (roomState != ConnectionState.CONNECTED) {
            reportError("Sending ICE candidate removals in non connected state.");
            return;
          }
//          sendPostMessage(MessageType.MESSAGE, messageUrl, json.toString());
          if (connectionParameters.loopback) {
            events.onRemoteIceCandidatesRemoved(candidates);
          }
        } else {
          // Call receiver sends ice candidates to websocket server.
          wsClient.send(json.toString());
        }
      }
    });
  }

  // --------------------------------------------------------------------
  // WebSocketChannelEvents interface implementation.
  // All events are called by WebSocketChannelClient on a local looper thread
  // (passed to WebSocket client constructor).
  @Override
  public void onWebSocketMessage(final String msg) {

    /**
     * 웹소켓 연결 확인
     */
    if (wsClient.getState() != WebSocketChannelClient.WebSocketConnectionState.REGISTERED) {
      Log.e(TAG, "Got WebSocket message in non registered state.");
      return;
    }

    try {
      JSONObject json = new JSONObject(msg);
      String msgText = json.getString("msg");
      String errorText = json.optString("error");
      if (msgText.length() > 0) {
        json = new JSONObject(msgText);
        String type = json.optString("type");

        if(type.equals("userList")){

          List<UserModel> userList = new ArrayList<UserModel>();
          JSONArray jsonArray = json.getJSONArray("userList");

          for (int i = 0; i < jsonArray.length(); i++){
            JSONObject obj = jsonArray.getJSONObject(i);
            Log.d(TAG, "obj:"+obj);
            UserModel userModel = new UserModel(obj.getString("clientID"));

            userList.add(userModel);
          }

          events.onUserList(userList);
        }
        // type : candidate
        else if (type.equals("candidate")) {
          events.onRemoteIceCandidate(toJavaCandidate(json));

        // type : remove-candidates
        } else if (type.equals("remove-candidates")) {
          JSONArray candidateArray = json.getJSONArray("candidates");
          IceCandidate[] candidates = new IceCandidate[candidateArray.length()];
          for (int i = 0; i < candidateArray.length(); ++i) {
            candidates[i] = toJavaCandidate(candidateArray.getJSONObject(i));
          }
          events.onRemoteIceCandidatesRemoved(candidates);

        // type : answer
        } else if (type.equals("answer")) {
          if (initiator) {
            SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.fromCanonicalForm(type), json.getString("sdp"));
            events.onRemoteDescription(sdp);
          } else {
            reportError("Received answer for call initiator: " + msg);
          }

        // type : offer
        } else if (type.equals("offer")) {
          if (!initiator) {
            SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.fromCanonicalForm(type), json.getString("sdp"));
            events.onRemoteDescription(sdp);
          } else {
            reportError("Received offer for call receiver: " + msg);
          }

        // type : bye
        } else if (type.equals("bye")) {
          events.onChannelClose();
        } else {
          reportError("Unexpected WebSocket message: " + msg);
        }
      } else {
        if (errorText != null && errorText.length() > 0) {
          reportError("WebSocket error message: " + errorText);
        } else {
          reportError("Unexpected WebSocket message: " + msg);
        }
      }
    } catch (JSONException e) {
      reportError("WebSocket message JSON parsing error: " + e.toString());
    }
  }

  @Override
  public void onWebSocketClose() {
    events.onChannelClose();
  }

  @Override
  public void onWebSocketError(String description) {
    reportError("WebSocket error: " + description);
  }

  // --------------------------------------------------------------------
  // Helper functions.
  private void reportError(final String errorMessage) {
    Log.e(TAG, errorMessage);
    handler.post(new Runnable() {
      @Override
      public void run() {
        if (roomState != ConnectionState.ERROR) {
          roomState = ConnectionState.ERROR;
          events.onChannelError(errorMessage);
        }
      }
    });
  }

  // Put a `key`->`value` mapping in `json`.
  private static void jsonPut(JSONObject json, String key, Object value) {
    try {
      json.put(key, value);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * SDP 또는 ICE candidate를 room 서버로 보낸다.
   */
//  private void sendPostMessage(
//      final MessageType messageType, final String url, @Nullable final String message) {
//    String logInfo = url;
//    if (message != null) {
//      logInfo += ". Message: " + message;
//    }
//    Log.d(TAG, "C->GAE: " + logInfo);
//    AsyncHttpURLConnection httpConnection =
//        new AsyncHttpURLConnection("POST", url, message, new AsyncHttpURLConnection.AsyncHttpEvents() {
//          @Override
//          public void onHttpError(String errorMessage) {
//            reportError("GAE POST error: " + errorMessage);
//          }
//
//          @Override
//          public void onHttpComplete(String response) {
//            if (messageType == MessageType.MESSAGE) {
//              try {
//                JSONObject roomJson = new JSONObject(response);
//                String result = roomJson.getString("result");
//                if (!result.equals("SUCCESS")) {
//                  reportError("GAE POST error: " + result);
//                }
//              } catch (JSONException e) {
//                reportError("GAE POST JSON error: " + e.toString());
//              }
//            }
//          }
//        });
//    httpConnection.send();
//  }

  // Converts a Java candidate to a JSONObject.
  private JSONObject toJsonCandidate(final IceCandidate candidate) {
    JSONObject json = new JSONObject();
    jsonPut(json, "label", candidate.sdpMLineIndex);
    jsonPut(json, "id", candidate.sdpMid);
    jsonPut(json, "candidate", candidate.sdp);
    return json;
  }

  // Converts a JSON candidate to a Java object.
  IceCandidate toJavaCandidate(JSONObject json) throws JSONException {
    return new IceCandidate(
        json.getString("id"), json.getInt("label"), json.getString("candidate"));
  }
}