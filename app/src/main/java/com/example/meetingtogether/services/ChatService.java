package com.example.meetingtogether.services;

import static com.example.meetingtogether.common.Common.IS_PENDING;
import static com.example.meetingtogether.common.Common.OTHER_USER_ID;
import static com.example.meetingtogether.common.Common.OTHER_USER_NAME;
import static com.example.meetingtogether.common.Common.ROOMID;
import static com.example.meetingtogether.common.Common.ROOM_NAME;
import static com.example.meetingtogether.common.Common.ROOM_TYPE_ID;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavDeepLinkBuilder;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.meetingtogether.MainActivity;
import com.example.meetingtogether.R;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.model.MessageDTO;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.LocalDateTimeDeserializer;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.ui.chats.ChatRoomActivity;
import com.example.meetingtogether.ui.chats.GroupChatCreateActivity;
import com.example.meetingtogether.ui.meetings.CustomCapturer;
import com.example.meetingtogether.ui.meetings.MeetingRoomActivity;
import com.example.meetingtogether.ui.user.LoginActivity;
import com.example.meetingtogether.ui.users.ProfileEditActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatService extends Service {

    private String TAG = "TEST";
    private final IBinder mBinder = new ChatService.ChatServiceBinder();
    private NotificationCompat.Builder builder;
    private Notification notification;
    private ChatService.ChatServiceInterface roomListEvent;
    private ChatService.ChatServiceInterface roomEvent;

    private Context context;

    private Handler handler;
    public static Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Gson gson;
    private final String SERVER_HOST = "34.64.140.200";
    private final int SERVER_PORT = 11002;
    public static int roomId = -1;

    public class ChatServiceBinder extends Binder {
        public ChatService getService() {
            return ChatService.this;
        }
    }

    public ChatService() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());
        gson = gsonBuilder.create();
    }

    /** Activity 와 Service 와의 비동기 상호작용을 위한 인터페이스 */
    public interface ChatServiceInterface{
        void onReceived();
        void onError(String message);
        void onUserAdd();
//        void onUserList(MessageDTO messageDTO);
//        void onRoomList(MessageDTO messageDTO);
//        void onRoomAdd(MessageDTO messageDTO);
//        void onRoomEnter(MessageDTO messageDTO);
//        void onRoomOut(MessageDTO messageDTO);
        void onMessage(MessageDTO messageDTO);
//        void onExit(MessageDTO messageDTO);

        void onAlertMsg(String msg);
    }

    public void setRoomListInterface(ChatService.ChatServiceInterface roomListEvent){
        this.roomListEvent = roomListEvent;
//        if(roomListEvent != null) openSocket();
    }

    public void setRoomInterface(ChatService.ChatServiceInterface roomEvent){
        this.roomEvent = roomEvent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "서비스 생성");

        // 알림 표시 띄우기
        // 포그라운드 채팅 채널
        // 채팅 메시지 채널
        showNotification();

        HandlerThread handlerChatThread = new HandlerThread("chat-thread");
        handlerChatThread.start();
        handler = new Handler(handlerChatThread.getLooper());
        // 소켓 연결
        openSocket();

        // 아직 전달 받지 못한 메시지 가져오기
        Call<List<MessageDTO>> call = RetrofitService.getInstance().getService().postChatQueueAndDelete();
        call.enqueue(new Callback<List<MessageDTO>>() {
            @Override
            public void onResponse(Call<List<MessageDTO>> call, Response<List<MessageDTO>> response) {
                List<MessageDTO> messageDTOList = response.body();
                Log.d(TAG, "messageDTOList:" + messageDTOList);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for(MessageDTO messageDTO : messageDTOList){
                            showMessageNotification(messageDTO);
                        }
                    }
                });

            }

            @Override
            public void onFailure(Call<List<MessageDTO>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void openSocket(){
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 서버로 부터 메시지 받는 곳
                    try {
                        socket = new Socket(SERVER_HOST, SERVER_PORT);

                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.flush();
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

                        writer = new PrintWriter(outputStreamWriter, true);

                        // Create input stream to receive data from the server
                        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                        // 사용자 추가 요청
                        MessageDTO sendMsgDTO = new MessageDTO();
                        sendMsgDTO.setUser(Util.user);
                        sendMsgDTO.setType(MessageDTO.RequestType.USER_ADD);
                        sendMsg(sendMsgDTO);

                        /** 수신 대기 */
                        String jsonString;
                        /** 무한 츠쿠요미 */
                        while ((jsonString = reader.readLine()) != null) {

                            MessageDTO receiveMsgDTO = gson.fromJson(jsonString, MessageDTO.class);

                            MessageDTO.RequestType type = receiveMsgDTO.getType();

                            // 사용자 추가
                            if(type == MessageDTO.RequestType.USER_ADD){
                                Log.d(TAG, "프로그램 접속에 완료 됐습니다.");
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(roomListEvent != null) roomListEvent.onUserAdd();
                                        if(roomEvent != null) roomEvent.onUserAdd();
                                    }
                                });
                            // 방 리스트
                            }else if(type == MessageDTO.RequestType.ROOM_LIST){
//                        clientInterface.onRoomList(receiveMsgDTO);
                            // 사용자 리스트
                            }else if(type == MessageDTO.RequestType.USER_LIST){
//                        clientInterface.onUserList(receiveMsgDTO);
                            // 텍스트 메시지
                            }else if(type == MessageDTO.RequestType.MESSAGE || type == MessageDTO.RequestType.IMAGE){
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(roomListEvent != null) roomListEvent.onMessage(receiveMsgDTO);
                                        if(roomEvent != null) roomEvent.onMessage(receiveMsgDTO);
                                    }
                                });

                                /** 노티피케이션 */
                                Log.d(TAG, "[TEST]receiveMsgDTO.getRoomUuid():" + receiveMsgDTO.getRoomUuid());
                                Log.d(TAG, "[TEST]roomId:" + roomId);
                                if(receiveMsgDTO.getRoomUuid() != roomId || roomId == -1) showMessageNotification(receiveMsgDTO);

//                        clientInterface.onMessage(receiveMsgDTO);
                            // 나의 메시지를 갱신 시킨다.
                            }else if(type == MessageDTO.RequestType.OTHER_USER_MSG_RENEW){
                                if(roomEvent != null) roomEvent.onMessage(receiveMsgDTO);
                            // 방에 입장
                            }else if(type == MessageDTO.RequestType.ROOM_ENTER){
//                        clientInterface.onRoomEnter(receiveMsgDTO);
                            // 방에서 나가기
                            }else if(type == MessageDTO.RequestType.ROOM_OUT){
//                        clientInterface.onRoomOut(receiveMsgDTO);
                            // 방 생성
                            }else if(type == MessageDTO.RequestType.ROOM_CREATE){
//                        clientInterface.onRoomAdd(receiveMsgDTO);
                            // 아예 나가기
                            }else if(type == MessageDTO.RequestType.EXIT){
//                        clientInterface.onExit(receiveMsgDTO);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            if(socket != null) socket.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    Log.d(TAG, "소켓 종료");
                    /** 소켓이 종료 되면 서비스도 죽이자.  */
                    stopForeground(true);
                    stopSelf();
                    showMessage("소켓연결에 실패하여 채팅 서비스는 종료됩니다.");
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    public void close(){
        try {
            if(this.socket != null ){
                this.socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    public void sendMsg(MessageDTO messageDTO){
        /**
         * Json String으로 변환과
         * Socket Send 가 느릴 수 있으니까 비동기적으로 보내주자
         */
        handler.post(new Runnable() {
            @Override
            public void run() {
                String jsonString = gson.toJson(messageDTO);
                Log.d(TAG, "[TEST] 서버로 보내는 JSON 메시지:" + jsonString);
                if(writer != null) writer.println(jsonString);
            }
        });
    }

    private void showNotification(){
        //안드로이드 O버전 이상에서는 알림창을 띄워야 포그라운드 사용 가능
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 포그라운드 채널 생성
            createNotificationChannel(NotificationUtil.CHAT_CHANNEL_ID);

            // 메시지 채널 생성
            createNotificationChannel(NotificationUtil.CHAT_MESSAGE_CHANNEL_ID);

            /**
             * [NotificationCompat.Builder]
             * 알림을 만들기 위해 사용되는 클래스
             */
            builder = new NotificationCompat.Builder(this, NotificationUtil.CHAT_CHANNEL_ID);
            builder.setContentTitle("채팅 서비스");
            builder.setContentText("동작중 ... ");
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setOngoing(true); // Swipe로 알림 삭제하는 기능 중지

            /**
             * Notification
             * 컨텐츠 영역 클릭 Pending Intent
             */
            Intent contentIntent = new Intent(this, MainActivity.class);
            contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent =
                    PendingIntent.getActivity(this, 0, contentIntent,
                            PendingIntent.FLAG_IMMUTABLE);

//            PendingIntent pendingIntent = new NavDeepLinkBuilder(this)
//                    .setGraph(R.navigation.mobile_navigation)
//                    .setDestination(R.id.navigation_chat)
//                    .createPendingIntent();

            builder.setContentIntent(pendingIntent);

            /**
             * 권한 체크
             */
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "권한 없음");
                return;
            }

            /**
             * 알림 표시
             */
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NotificationUtil.NOTIFICATION_CHAT_ID, builder.build());

            notification = builder.build();

            /**
             * 포그라운드 서비스를 시작하면
             * 자동으로 Notification을 띄워준다.
             */
            startForeground(NotificationUtil.NOTIFICATION_CHAT_ID, notification);
        }
    }

    private void createNotificationChannel(String channel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    channel,
                    channel,
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ChatService onDestroy");
        roomId = -1;
        close();
        // 사용자 추가 요청
//        MessageDTO sendMsgDTO = new MessageDTO();
//        sendMsgDTO.setUser(Util.user);
//        sendMsgDTO.setType(MessageDTO.RequestType.EXIT);
//        sendMsg(sendMsgDTO);
    }

    public void showMessageNotification(MessageDTO messageDTO){

        // Glide를 사용하여 이미지를 비트맵으로 변환
        try {
            RequestOptions requestOptions = new RequestOptions().centerCrop();
            Bitmap bitmap = null;
            try{
                bitmap = Glide.with(ChatService.this)
                        .asBitmap()
                        .load("https://webrtc-sfu.kro.kr/" + messageDTO.getProfileImgPath())
                        .apply(requestOptions)
                        .submit()
                        .get();
            }catch (Exception e){
                e.printStackTrace();
            }


//            @SuppressLint("RemoteViewLayout")
//            RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.notification_message_notification);
//            @SuppressLint("RemoteViewLayout")
//            RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_message_notification);
//
//            remoteView.setTextViewText(R.id.name, messageDTO.getSenderName().toString());
//            remoteView.setTextViewText(R.id.message, messageDTO.getMessage().toString());
//            remoteView.setTextViewText(R.id.date, messageDTO.getCreateDate().toLocalDate() + " " + messageDTO.getCreateDate().toLocalTime());
//            remoteView.setImageViewBitmap(R.id.profile, bitmap);

            /**
             * [NotificationCompat.Builder]
             * 알림을 만들기 위해 사용되는 클래스
             */
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationUtil.CHAT_MESSAGE_CHANNEL_ID);
            if(messageDTO.getRoomType() == MessageDTO.RoomType.INDIVIDUAL){
                builder.setContentTitle(messageDTO.getSenderName());
            }else if(messageDTO.getRoomType() == MessageDTO.RoomType.GROUP){
                builder.setContentTitle(messageDTO.getRoomName());
            }

            // 메시지 또는 이미지 구분 하여 표현
            // messageType2 는 받지 못한 메시지 조회에서 가져옴
            if(messageDTO.getMessage_type2() == null){
                if(messageDTO.getFileInfoList() != null && messageDTO.getFileInfoList().size() > 0){
                    builder.setContentText("(image) " + messageDTO.getFileInfoList().size());
                }else{
                    builder.setContentText(messageDTO.getMessage());
                }
            }else{
                if(messageDTO.getMessage_type2() == MessageDTO.MessageType2.MESSAGE){
                    builder.setContentText(messageDTO.getMessage());
                }else if(messageDTO.getMessage_type2() == MessageDTO.MessageType2.IMAGE){
                    builder.setContentText(messageDTO.getFileCnt());
                }
            }


            builder.setSmallIcon(R.mipmap.ic_launcher);
            if(bitmap != null) builder.setLargeIcon(bitmap);
//            builder.setCustomContentView(remoteView);
//            builder.setCustomBigContentView(notificationLayoutExpanded);
            builder.setOngoing(false); // Swipe로 알림 삭제하는 기능 사용

            /**
             * Notification
             * 컨텐츠 영역 클릭 Pending Intent
             */
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

            Intent firstIntent = new Intent(this, MainActivity.class);
            stackBuilder.addNextIntent(firstIntent);

            Intent contentIntent = new Intent(this, ChatRoomActivity.class);
//            contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            contentIntent.putExtra(ROOMID, messageDTO.getRoomUuid());
            contentIntent.putExtra(ROOM_NAME, messageDTO.getRoomName());
            contentIntent.putExtra(ROOM_TYPE_ID, messageDTO.getRoomType().name());
            contentIntent.putExtra(OTHER_USER_ID, messageDTO.getSenderId());
            contentIntent.putExtra(OTHER_USER_NAME, messageDTO.getSenderName());
            contentIntent.putExtra(IS_PENDING, true);

            stackBuilder.addNextIntent(contentIntent);

            /**
             * FLAG_UPDATE_CURRENT : 이미 존재하는 PendingIntent를 업데이트하는 데 사용되는 플래그입니다.
             * 이걸 해야 putExtra가 먹힌다.
             *
             * FLAG_IMMUTABLE: PendingIntent를 생성한 후에는 해당 PendingIntent의 내용을 변경할 수 없습니다.
             */
//            PendingIntent pendingIntent =
//                    PendingIntent.getActivity(this, 0, contentIntent,
//                            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            // PendingIntent 생성
            PendingIntent pendingIntent =
                    stackBuilder.getPendingIntent(messageDTO.getId(), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);

            /**
             * 권한 체크
             */
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "권한 없음");
                return;
            }

            /**
             * 알림 표시
             */
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(messageDTO.getId(), builder.build());

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private void showMessage(String msg){
        if(this.roomListEvent != null) this.roomListEvent.onAlertMsg(msg);
        if(this.roomEvent != null) this.roomEvent.onAlertMsg(msg);
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        Log.d(TAG, "[서비스] roomId를" + roomId + "로 업데이트");
        this.roomId = roomId;
    }
}