package com.example.meetingtogether.broadcast;

import static com.example.meetingtogether.MainActivity.TAG;
import static com.example.meetingtogether.MainActivity.TAG_INTERNET_RECEIVER;
import static com.example.meetingtogether.MainActivity.mChatService;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class InternetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG_INTERNET_RECEIVER, "[TEST] onReceive 동작 : " + intent.getAction().toString());
        Toast.makeText(context, "onReceive 동작", Toast.LENGTH_SHORT);

        String action = intent.getAction();
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            boolean isConnected = isNetworkAvailable(context);
            if (isConnected) {
                // 네트워크 연결됨
                if(mChatService != null){
                    mChatService.openSocket();
                }
                Toast.makeText(context, "인터넷 연결됨", Toast.LENGTH_SHORT).show();
            } else {
                // 네트워크 끊김
                Toast.makeText(context, "인터넷 연결 끊김", Toast.LENGTH_SHORT).show();
                if(mChatService != null){
                    mChatService.openSocket();
                }
            }
        }
    }

    private Boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network nw = connectivityManager.getActiveNetwork();
            if (nw == null) return false;
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
            return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
            return nwInfo != null && nwInfo.isConnected();
        }
    }
}
