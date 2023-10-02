package com.example.meetingtogether;

import android.app.Application;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.example.meetingtogether.common.ColorType;

import java.net.URISyntaxException;
import java.util.EnumMap;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MyApplication extends Application {

    private Socket mSocket;
    public static EnumMap<ColorType, Integer> colorTypeIntegerEnumMap;

    @Override
    public void onCreate() {
        super.onCreate();

        colorTypeIntegerEnumMap = new EnumMap<>(ColorType.class);
        colorTypeIntegerEnumMap.put(ColorType.BLACK, Color.BLACK);
        colorTypeIntegerEnumMap.put(ColorType.RED, Color.RED);

        int color = ContextCompat.getColor(this, R.color.orange);
        colorTypeIntegerEnumMap.put(ColorType.ORANGE, color);

        colorTypeIntegerEnumMap.put(ColorType.YELLOW, Color.YELLOW);
        colorTypeIntegerEnumMap.put(ColorType.GREEN, Color.GREEN);
        colorTypeIntegerEnumMap.put(ColorType.BLUE, Color.BLUE);

        color = ContextCompat.getColor(this, R.color.indigo);
        colorTypeIntegerEnumMap.put(ColorType.INDIGO, color);

        color = ContextCompat.getColor(this, R.color.purple);
        colorTypeIntegerEnumMap.put(ColorType.PURPLE, color);

        color = ContextCompat.getColor(this, R.color.white);
        colorTypeIntegerEnumMap.put(ColorType.ERASER, color);


        /**
         * 이곳에 메인 채팅 관련 서비스를 생성하고 바인딩 시키는게 좋을 듯 하다
         * TODO: 채팅 서비스 생성하고 바인딩 시키자^^
         */
//        try {
//            mSocket = IO.socket(Constants.CHAT_SERVER_URL);
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
    }

    public Socket getSocket() {
        try {
            return mSocket;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
