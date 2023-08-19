package com.example.meetingtogether.ui.meetings;

import android.content.Intent;
import android.os.Bundle;

import com.example.meetingtogether.MyApplication;
import com.example.meetingtogether.databinding.ActivityMeetingRoomBinding;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.meetingtogether.R;

import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MeetingRoomActivity extends AppCompatActivity {

    private ActivityMeetingRoomBinding binding;

    private Socket mSocket;

    private int room = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMeetingRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /**
         * 전달된 Intent 받는 부분
         */
        Intent receiveIntent = getIntent();
        room = receiveIntent.getIntExtra("room", -1);

        if(room == -1){
            finish();
        }

        Toast.makeText(MeetingRoomActivity.this, room + "번방에 진입 했습니다.",Toast.LENGTH_SHORT).show();

        socketIoConn();

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.emit("message", "helloWorld");
            }
        });

    }

    private void socketIoConn(){
        MyApplication app = (MyApplication) getApplication();
        mSocket = app.getSocket();

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // 연결 성공 시 실행되는 코드
                Log.d("TEST", "소켓 연결");
                mSocket.emit("join", room);
            }
        });

        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("TEST", "소켓 종료");
            }
        });

        mSocket.on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try{
                    String message = args[0].toString();
                    JSONObject roomJson = new JSONObject(message);
                    Log.d("TEST", roomJson.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        mSocket.connect();


//        mSocket.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }
}