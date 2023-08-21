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
import android.util.Log;

import androidx.annotation.Nullable;


import com.example.meetingtogether.ui.meetings.google.util.AsyncHttpURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import de.tavendo.autobahn.WebSocket.WebSocketConnectionObserver;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * WebSocket client implementation.
 *
 * <p>All public methods should be called from a looper executor thread
 * passed in a constructor, otherwise exception will be thrown.
 * All events are dispatched on the same thread.
 */
public class WebSocketChannelClient {
  private static final String TAG = "TEST";
  private static final int CLOSE_TIMEOUT = 1000;
  private final WebSocketChannelEvents events;
  private final Handler handler;
  private Socket ws;
  private String wsServerUrl;
  private String postServerUrl;
  @Nullable
  private String roomID;
  @Nullable
  private String clientID;
  private WebSocketConnectionState state;
  private final Object closeEventLock = new Object();
  private boolean closeEvent;
  // WebSocket send queue. Messages are added to the queue when WebSocket
  // client is not registered and are consumed in register() call.
  private final List<String> wsSendQueue = new ArrayList<>();

  /**
   * Possible WebSocket connection states.
   */
  public enum WebSocketConnectionState { NEW, CONNECTED, REGISTERED, CLOSED, ERROR }

  /**
   * Callback interface for messages delivered on WebSocket.
   * All events are dispatched from a looper executor thread.
   */
  public interface WebSocketChannelEvents {
    void onWebSocketMessage(final String message);
    void onWebSocketClose();
    void onWebSocketError(final String description);
  }

  public WebSocketChannelClient(Handler handler, WebSocketChannelEvents events) {
    this.handler = handler;
    this.events = events;
    roomID = null;
    clientID = null;
    state = WebSocketConnectionState.NEW;
  }

  public WebSocketConnectionState getState() {
    return state;
  }

  public void connect(final String wsUrl) {
    checkIfCalledOnValidThread();
    if (state != WebSocketConnectionState.NEW) {
      Log.e(TAG, "웹소켓이 이미 연결 되어져 있습니다.");
      return;
    }
    wsServerUrl = wsUrl;
    closeEvent = false;

    Log.d(TAG, "Connecting WebSocket to: " + wsUrl);

    try {
      ws = IO.socket(wsServerUrl);

      /**
       * 웹소켓 이벤트 리스너 등록
       */
      ws.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
              String message = args[0].toString();

              Log.d(TAG, "WebSocket connection opened to: " + wsServerUrl);
              Log.d(TAG, "message: " + message);

              /**
               * 웹소켓 연결 완료
               */
              handler.post(new Runnable() {
                @Override
                public void run() {
                  state = WebSocketConnectionState.CONNECTED;
                }
              });
                // 연결 성공 시 실행되는 코드
//                Log.d("TEST", "소켓 연결");
//                mSocket.emit("join", roomId);
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
              String message = args[0].toString();
              Log.d(TAG, "WebSocket connection closed. message: " + message + ". State: " + state);

              synchronized (closeEventLock) {
                closeEvent = true;
                closeEventLock.notify();
              }

              /**
               * 상태를 close 로 변경하고
               * 인터페이스 close 호출
               */
              handler.post(new Runnable() {
                @Override
                public void run() {
                  if (state != WebSocketConnectionState.CLOSED) {
                    state = WebSocketConnectionState.CLOSED;
                    events.onWebSocketClose();
                  }
                }
              });
            }
        }).on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
              final String message = args[0].toString();
              Log.d(TAG, "WSS->C: " + message);

              /**
               * 메시지를 받습니다.
               */
              handler.post(new Runnable() {
                @Override
                public void run() {
                  if (state == WebSocketConnectionState.CONNECTED
                      || state == WebSocketConnectionState.REGISTERED) {
                    events.onWebSocketMessage(message);
                  }
                }
              });
            }
        });
      ws.connect();
    } catch (Exception e) {
      e.printStackTrace();
      reportError("URI error: " + e.getMessage());
    }
  }

//  public void register(final String roomID, final String clientID) {
//    checkIfCalledOnValidThread();
//    this.roomID = roomID;
//    this.clientID = clientID;
//    if (state != WebSocketConnectionState.CONNECTED) {
//      Log.w(TAG, "WebSocket register() in state " + state);
//      return;
//    }
//    Log.d(TAG, "Registering WebSocket for room " + roomID + ". ClientID: " + clientID);
//    JSONObject json = new JSONObject();
//    try {
//      json.put("cmd", "register");
//      json.put("roomid", roomID);
//      json.put("clientid", clientID);
//      Log.d(TAG, "C->WSS: " + json.toString());
//      ws.sendTextMessage(json.toString());
//      state = WebSocketConnectionState.REGISTERED;
//      // Send any previously accumulated messages.
//      for (String sendMessage : wsSendQueue) {
//        send(sendMessage);
//      }
//      wsSendQueue.clear();
//    } catch (JSONException e) {
//      reportError("WebSocket register JSON error: " + e.getMessage());
//    }
//  }

  public void send(String message) {
    checkIfCalledOnValidThread();
    switch (state) {
      case NEW:
      case CONNECTED:
        // Store outgoing messages and send them after websocket client
        // is registered.
        Log.d(TAG, "WS ACC: " + message);
        wsSendQueue.add(message);
        return;
      case ERROR:
      case CLOSED:
        Log.e(TAG, "WebSocket send() in error or closed state : " + message);
        return;
      case REGISTERED:
        JSONObject json = new JSONObject();
        try {
          json.put("cmd", "send");
          json.put("msg", message);
          message = json.toString();
          Log.d(TAG, "C->WSS: " + message);
          ws.emit("message", message);
        } catch (JSONException e) {
          reportError("WebSocket send JSON error: " + e.getMessage());
        }
        break;
    }
  }

  // This call can be used to send WebSocket messages before WebSocket
  // connection is opened.
//  public void post(String message) {
//    checkIfCalledOnValidThread();
//    sendWSSMessage("POST", message);
//  }

  public void disconnect(boolean waitForComplete) {
    checkIfCalledOnValidThread();
    Log.d(TAG, "Disconnect WebSocket. State: " + state);
    if (state == WebSocketConnectionState.REGISTERED) {
      /**
       * WebSocket 서버로 "bye"를 보냅니다.
       */
      send("{\"type\": \"bye\"}");
      state = WebSocketConnectionState.CONNECTED;
      // Send http DELETE to http WebSocket server.
//      sendWSSMessage("DELETE", "");
    }

    /**
     * 상태가 CONNECTED or ERROR 일 때에만 소켓을 닫습니다.
     */
    if (state == WebSocketConnectionState.CONNECTED || state == WebSocketConnectionState.ERROR) {
      ws.disconnect();
      state = WebSocketConnectionState.CLOSED;

      // Wait for websocket close event to prevent websocket library from
      // sending any pending messages to deleted looper thread.
      /**
       * 삭제된 루퍼 스레드로 보류중인 매시지를 보내는 것을 방지하기 위해서
       * WebSocket Close Event를 기다립니다.
       */
      if (waitForComplete) {
        synchronized (closeEventLock) {
          while (!closeEvent) {
            try {
              closeEventLock.wait(CLOSE_TIMEOUT);
              break;
            } catch (InterruptedException e) {
              Log.e(TAG, "Wait error: " + e.toString());
            }
          }
        }
      }
    }
    Log.d(TAG, "Disconnecting WebSocket done.");
  }

  private void reportError(final String errorMessage) {
    Log.e(TAG, errorMessage);
    handler.post(new Runnable() {
      @Override
      public void run() {
        if (state != WebSocketConnectionState.ERROR) {
          state = WebSocketConnectionState.ERROR;
          events.onWebSocketError(errorMessage);
        }
      }
    });
  }

  // Asynchronously send POST/DELETE to WebSocket server.
//  private void sendWSSMessage(final String method, final String message) {
//    String postUrl = postServerUrl + "/" + roomID + "/" + clientID;
//    Log.d(TAG, "WS " + method + " : " + postUrl + " : " + message);
//    AsyncHttpURLConnection httpConnection =
//        new AsyncHttpURLConnection(method, postUrl, message, new AsyncHttpURLConnection.AsyncHttpEvents() {
//          @Override
//          public void onHttpError(String errorMessage) {
//            reportError("WS " + method + " error: " + errorMessage);
//          }
//
//          @Override
//          public void onHttpComplete(String response) {}
//        });
//    httpConnection.send();
//  }

  // Helper method for debugging purposes. Ensures that WebSocket method is
  // called on a looper thread.
  private void checkIfCalledOnValidThread() {
    if (Thread.currentThread() != handler.getLooper().getThread()) {
      throw new IllegalStateException("WebSocket method is not called on valid thread");
    }
  }

//  private class WebSocketObserver implements WebSocketConnectionObserver {
//    @Override
//    public void onOpen() {
//      Log.d(TAG, "WebSocket connection opened to: " + wsServerUrl);
//      handler.post(new Runnable() {
//        @Override
//        public void run() {
//          state = WebSocketConnectionState.CONNECTED;
//          // Check if we have pending register request.
//          if (roomID != null && clientID != null) {
//            register(roomID, clientID);
//          }
//        }
//      });
//    }
//
//    @Override
//    public void onClose(WebSocketCloseNotification code, String reason) {
//      Log.d(TAG, "WebSocket connection closed. Code: " + code + ". Reason: " + reason + ". State: "
//              + state);
//      synchronized (closeEventLock) {
//        closeEvent = true;
//        closeEventLock.notify();
//      }
//      handler.post(new Runnable() {
//        @Override
//        public void run() {
//          if (state != WebSocketConnectionState.CLOSED) {
//            state = WebSocketConnectionState.CLOSED;
//            events.onWebSocketClose();
//          }
//        }
//      });
//    }
//
//    @Override
//    public void onTextMessage(String payload) {
//      Log.d(TAG, "WSS->C: " + payload);
//      final String message = payload;
//      handler.post(new Runnable() {
//        @Override
//        public void run() {
//          if (state == WebSocketConnectionState.CONNECTED
//              || state == WebSocketConnectionState.REGISTERED) {
//            events.onWebSocketMessage(message);
//          }
//        }
//      });
//    }
//
//    @Override
//    public void onRawTextMessage(byte[] payload) {}
//
//    @Override
//    public void onBinaryMessage(byte[] payload) {}
//  }
}
