package com.example.meetingtogether.broadcast;

import static com.example.meetingtogether.MainActivity.TAG;
import static com.example.meetingtogether.sharedPreference.SharedPreferenceRepository.pref;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.services.ChatService;
import com.example.meetingtogether.sharedPreference.SharedPreferenceRepository;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "[TEST] onReceive 동작 : " + intent.getAction().toString());
        Toast.makeText(context, "onReceive 동작", Toast.LENGTH_SHORT);

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferenceRepository.init();
            SharedPreferenceRepository.pref =  context.getSharedPreferences("pref", Activity.MODE_PRIVATE);
            SharedPreferenceRepository.editor = pref.edit();

            Util.user = SharedPreferenceRepository.getUser();
            Log.d(TAG, "[TEST] Util.user : " + Util.user);

            Intent newIntent = new Intent(context, ChatService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(newIntent);
            } else {
                context.startService(newIntent);
            }
        }else if(intent.getAction().equals(Intent.ACTION_SHUTDOWN) || intent.getAction().equals(Intent.ACTION_REBOOT)){
            if(ChatService.socket != null) {
                try {

                    Call<CommonRetrofitResponse> call = RetrofitService.getInstance().getService().postChatRenewOutDate(ChatService.roomId);
                    call.enqueue(new Callback<CommonRetrofitResponse>() {
                        @Override
                        public void onResponse(Call<CommonRetrofitResponse> call, Response<CommonRetrofitResponse> response) {
                        }

                        @Override
                        public void onFailure(Call<CommonRetrofitResponse> call, Throwable t) {
                        }
                    });

                    ChatService.roomId = -1;
                    ChatService.socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
