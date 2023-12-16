package com.example.meetingtogether.ui.chats;

import static com.example.meetingtogether.common.Common.ROOMID;
import static com.example.meetingtogether.common.Common.ROOM_TYPE_ID;
import static com.example.meetingtogether.common.Common.VIDEO;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.ActivityChatRoomBinding;
import com.example.meetingtogether.databinding.ActivityMeetingRoomBinding;
import com.example.meetingtogether.model.MessageDTO;
import com.example.meetingtogether.model.User;
import com.example.meetingtogether.services.ChatService;
import com.example.meetingtogether.services.TestService;
import com.example.meetingtogether.ui.meetings.CustomCapturer;
import com.example.meetingtogether.ui.meetings.DTO.MessageModel;
import com.example.meetingtogether.ui.meetings.MeetingRoomActivity;

public class ChatRoomActivity extends AppCompatActivity {

    public static final String TAG = "TEST";

    private ActivityChatRoomBinding binding;

    public static ChatService chatService;

    private boolean isChatServiceBound = false;
    private int roomId = -1;

    public ServiceConnection chatServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ChatService.ChatServiceBinder binder = (ChatService.ChatServiceBinder) service;
            chatService = binder.getService();
            isChatServiceBound = true;
            Log.d(TAG, "바인드 받아옴");

            chatService.setContext(ChatRoomActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isChatServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 방 번호 가져오기
        roomId = getIntent().getIntExtra(ROOMID, -1);

        // 해당 방에 있는 다른 사용자 정보 가져오기
        User otherUser = new User("test@naver.com", "123");
        otherUser.setName("테스트");

        // 개인 채팅방 or 단체 채팅방 구분 가져오기
        MessageDTO.RoomType roomType = MessageDTO.RoomType.valueOf(getIntent().getStringExtra(ROOM_TYPE_ID));

        binding.chatSendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = binding.messageText.getText().toString();

                Toast.makeText(ChatRoomActivity.this, msg, Toast.LENGTH_SHORT).show();

                // 채팅 서버로 메시지 전송
                MessageDTO sendMsgDTO = new MessageDTO();
                sendMsgDTO.setRoomUuid(roomId);
                sendMsgDTO.setRoomType(roomType);
                sendMsgDTO.setType(MessageDTO.RequestType.MESSAGE);
                sendMsgDTO.setMessage(msg);
                sendMsgDTO.setReceiveUser(otherUser);
                chatService.sendMsg(sendMsgDTO);

                // 에디터 초기화
                binding.messageText.setText("");
            }
        });

        binding.imageAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        binding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 채팅 서비스와 바인딩
        Intent intent = new Intent(ChatRoomActivity.this, ChatService.class);
        bindService(intent, chatServiceConn, Context.BIND_AUTO_CREATE);
    }
}