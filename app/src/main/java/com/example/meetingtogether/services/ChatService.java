package com.example.meetingtogether.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.meetingtogether.R;
import com.example.meetingtogether.ui.chats.ChatRoomActivity;
import com.example.meetingtogether.ui.meetings.CustomCapturer;
import com.example.meetingtogether.ui.meetings.MeetingRoomActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class ChatService extends Service {

    private String TAG = "TEST";
    private final IBinder mBinder = new ChatService.ChatServiceBinder();
    private NotificationCompat.Builder builder;
    private Notification notification;
    private ChatService.ChatServiceInterface event;

    private Context context;

    private Handler handler;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public class ChatServiceBinder extends Binder {
        public ChatService getService() {
            return ChatService.this;
        }
    }
    public ChatService() {
    }

    /** Activity 와 Service 와의 비동기 상호작용을 위한 인터페이스 */
    public interface ChatServiceInterface{
        void onReceived();
        void onError(String message);
    }

    public void setInterface(ChatService.ChatServiceInterface event){
        this.event = event;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "서비스 생성");

        // 알림 표시 띄우기
        showNotification();

        HandlerThread handlerChatThread = new HandlerThread("chat-thread");
        handlerChatThread.start();
        handler = new Handler(handlerChatThread.getLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                openSocket();
            }
        });

    }

    private void openSocket(){
        try {
            //소켓 생성
            socket = new Socket();
            //주소 생성
            SocketAddress address = new InetSocketAddress("webrtc-sfu.kro.kr", 11000);
            //주소에 해당하는 서버랑 연결
            socket.connect(address);

            // Create output stream to send data to the server
            out = new PrintWriter(socket.getOutputStream(), true);
            // Create input stream to receive data from the server
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
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

    public void sendMsg(String msg){
        handler.post(new Runnable() {
            @Override
            public void run() {
                out.println(msg);
            }
        });
    }

    private void showNotification(){
        //안드로이드 O버전 이상에서는 알림창을 띄워야 포그라운드 사용 가능
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 채널 생성
            createNotificationChannel();

            /**
             * [NotificationCompat.Builder]
             * 알림을 만들기 위해 사용되는 클래스
             */
            builder = new NotificationCompat.Builder(this, NotificationUtil.CHAT_CHANNEL_ID);
            builder.setContentTitle("채팅 서비스");
            builder.setContentText("동장중 ... ");
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setOngoing(false); // Swipe로 알림 삭제하는 기능 중지

            /**
             * Notification
             * 컨텐츠 영역 클릭 Pending Intent
             */
            Intent contentIntent = new Intent(this, ChatRoomActivity.class);
            contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(this, 0, contentIntent,
                            PendingIntent.FLAG_IMMUTABLE);
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NotificationUtil.CHAT_CHANNEL_ID,
                    NotificationUtil.CHAT_CHANNEL_ID,
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}