package com.example.meetingtogether;

import android.app.Application;

import com.example.meetingtogether.common.Constants;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MyApplication extends Application {

    private Socket mSocket;

    @Override
    public void onCreate() {
        super.onCreate();
//        try {
//            mSocket = IO.socket(Constants.CHAT_SERVER_URL);
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
    }

    public Socket getSocket() {
        try {
            if(mSocket == null) mSocket = IO.socket(Constants.CHAT_SERVER_URL);
            return mSocket;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
