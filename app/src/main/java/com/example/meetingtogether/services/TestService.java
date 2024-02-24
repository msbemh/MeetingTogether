package com.example.meetingtogether.services;

import static com.example.meetingtogether.MainActivity.TAG;
import static com.example.meetingtogether.common.Common.PEERS;
import static com.example.meetingtogether.common.Common.VIDEO;
import static com.example.meetingtogether.ui.meetings.MeetingRoomActivity.FPS;
import static com.example.meetingtogether.ui.meetings.MeetingRoomActivity.VIDEO_RESOLUTION_HEIGHT;
import static com.example.meetingtogether.ui.meetings.MeetingRoomActivity.VIDEO_RESOLUTION_WIDTH;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.meetingtogether.R;
import com.example.meetingtogether.common.Common;
import com.example.meetingtogether.common.MessageRecyclerView;
import com.example.meetingtogether.databinding.ActivityMeetingRoomBinding;
import com.example.meetingtogether.databinding.FragmentMeetingRoomChatBinding;
import com.example.meetingtogether.databinding.FragmentPeersBinding;
import com.example.meetingtogether.databinding.FragmentWhiteboardBinding;
import com.example.meetingtogether.retrofit.RetrofitInterface;
import com.example.meetingtogether.ui.meetings.CustomCapturer;
import com.example.meetingtogether.ui.meetings.CustomPeerConnection;
import com.example.meetingtogether.ui.meetings.CustomQueue;
import com.example.meetingtogether.ui.meetings.CustomTrack;
import com.example.meetingtogether.ui.meetings.DTO.MessageModel;
import com.example.meetingtogether.ui.meetings.DTO.UserModel;
import com.example.meetingtogether.ui.meetings.DrawingView;
import com.example.meetingtogether.ui.meetings.MeetingRoomActivity;
import com.example.meetingtogether.ui.meetings.ProxySink;
import com.example.meetingtogether.ui.meetings.fragments.MeetingRoomChatFragment;
import com.example.meetingtogether.ui.meetings.fragments.PeersFragment;
import com.example.meetingtogether.ui.meetings.fragments.WhiteboardFragment;
import com.example.meetingtogether.ui.meetings.google.Camera2Capturer;
import com.example.meetingtogether.ui.meetings.google.CameraCaptureInterface;
import com.google.mlkit.vision.face.FaceDetector;

import org.webrtc.AudioSource;
import org.webrtc.CapturerObserver;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.TextureBufferImpl;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSource;
import org.webrtc.YuvConverter;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import retrofit2.Retrofit;

public class TestService extends Service {
    private final IBinder mBinder = new TestServiceBinder();
    private NotificationCompat.Builder builder;
    private Notification notification;
    private TestServiceResult event;

    private Context context;

    /**
     * WebRTC / Camera / Retrofit / Socket 관련 모든 변수
     */

    public class TestServiceBinder extends Binder {
        public TestService getService() {
            return TestService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "서비스 생성");

        // 알림 표시 띄우기
        showNotification();
    }

    /** Activity 와 Service 와의 비동기 상호작용을 위한 인터페이스 */
    public interface TestServiceResult{
        void onLocalCapturerCreated(CustomCapturer customCapturer);
        void onError(String message);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }

    /** 비디오 캡처 - 여기서 안해도 될 듯하다. */
    public void startCapture(VideoCapturer videoCapturer){
        videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);
    }

    public void setInterface(TestServiceResult event){
        this.event = event;
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
            builder = new NotificationCompat.Builder(this, NotificationUtil.MEETING_CHANNEL_ID);
            builder.setContentTitle("화상채팅");
            builder.setContentText("미팅중...");
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setOngoing(true); // Swipe로 알림 삭제하는 기능 중지

            /**
             * Notification
             * 컨텐츠 영역 클릭 Pending Intent
             */
            Intent contentIntent = new Intent(this, MeetingRoomActivity.class);
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
            notificationManager.notify(NotificationUtil.NOTIFICATION_MEETING_ID, builder.build());

            notification = builder.build();

            /**
             * 포그라운드 서비스를 시작하면
             * 자동으로 Notification을 띄워준다.
             */
            startForeground(NotificationUtil.NOTIFICATION_MEETING_ID, notification);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NotificationUtil.MEETING_CHANNEL_ID,
                    NotificationUtil.MEETING_CHANNEL_ID,
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
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
