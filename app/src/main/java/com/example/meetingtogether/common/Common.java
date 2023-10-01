package com.example.meetingtogether.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class Common {
    static public final String VIDEO = "Video";
    static public final String SCREEN = "Screen";
    static public final String WHITE_BOARD = "WhiteBoard";
    static public final String ROOMID = "RoomId";

    // Bitmap을 Byte로 변환
    static public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, stream) ;
        byte[] byteArray = stream.toByteArray() ;
        return byteArray ;
    }

    // Byte를 Bitmap으로 변환
    static public Bitmap byteArrayToBitmap(byte[] byteArray) {
        Bitmap bitmap = BitmapFactory.decodeByteArray( byteArray, 0, byteArray.length ) ;
        return bitmap ;
    }
}
