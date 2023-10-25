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
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.meetingtogether.R;
import com.example.meetingtogether.common.Common;
import com.example.meetingtogether.ui.meetings.CustomCapturer;
import com.example.meetingtogether.ui.meetings.MeetingRoomActivity;

import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.VideoCapturer;

public class MeetingService extends Service {
    private String TAG = "TEST";
    private MediaProjectionManager mediaProjectionManager;
    private final IBinder mBinder = new MeetingServiceBinder();
    private NotificationCompat.Builder builder;
    private Notification notification;
    private int resultCode;
    private Intent resultData;
    private MeetingServiceResult event;

    private ImageView rectangleView;

    private Context context;

    public class MeetingServiceBinder extends Binder {
        public MeetingService getService() {
            return MeetingService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "서비스 생성");

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 알림 표시 띄우기
        showNotification();
    }

    public interface MeetingServiceResult{
        void onScreenCapturerCreated(CustomCapturer customCapturer);
        void onError(String message);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }

    public void createCapturer(String type){
        // 커스텀 비디오 캡처러 생성
        CustomCapturer customCapturer = new CustomCapturer(type, context);

        // 화면 캡처러 생성
        VideoCapturer screenCapturer = new ScreenCapturerAndroid(
                resultData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                Log.e(TAG, "화면 캡처를 위해서 권한 재요청");
            }
        });

        // type 설정
        customCapturer.setType(Common.SCREEN);
        // Video Capturer 설정
        customCapturer.setVideoCapturer(screenCapturer);

        if(event != null) event.onScreenCapturerCreated(customCapturer);
        else event.onError("아직 인터페이스가 세팅되지 않았습니다.");

    }

    public void setInterface(MeetingServiceResult event){
        this.event = event;
    }

    public void setMediaProjection(int resultCode, Intent resultData, MediaProjectionManager mediaProjectionManager){
        this.mediaProjectionManager = mediaProjectionManager;
        this.resultCode = resultCode;
        this.resultData = resultData;
    }

    private WindowManager.LayoutParams windowManagerParams;
    private WindowManager windowManager;

    public void enableCaptureDisplayRectangle(Boolean isEnableRectangle){
        if(isEnableRectangle){
            rectangleView = new ImageView(this);
            rectangleView.setBackgroundResource(R.drawable.rectangle);

            Point point = getDisplayDimensions();
            int width = point.x;
            int height = point.y;

            // Add the view to the window.
            windowManagerParams = new WindowManager.LayoutParams(
                    width,     // 가로
                    height,    // 세로
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,    //  뷰가 다른 앱 위에 그려지도록 하는 윈도우 타입
                    // FLAG_NOT_FOCUSABLE : 포커스를 받지 않도록 설정
                    // FLAG_NOT_TOUCHABLE : 클릭 비활성화
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN,          // 외부 터치 가능
                    PixelFormat.TRANSLUCENT);

            windowManagerParams.gravity = Gravity.CENTER;
            windowManager.addView(rectangleView, windowManagerParams);
        }else{
            rectangleRemoveView();
        }
    }

    private Point getDisplayDimensions() {
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            windowManager.getDefaultDisplay().getSize(size);
        } else {
            Display display = windowManager.getDefaultDisplay();
            size.x = display.getWidth();
            size.y = display.getHeight();
        }
        return size;
    }

    private void rectangleRemoveView(){
        synchronized (rectangleView){
            if(rectangleView != null){
                windowManager.removeView(rectangleView);  //추가했던 뷰 삭제
                rectangleView = null;
            }
        }
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
            builder = new NotificationCompat.Builder(this, NotificationUtil.SHARE_CHANNEL_ID);
            builder.setContentTitle("화면공유");
            builder.setContentText("화면공유중...");
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setOngoing(true); // Swipe로 알림 삭제하는 기능 중지

            /**
             * Notification
             * 컨텐츠 영역 클릭 Pending Intent
             */
            Intent contentIntent = new Intent(this, MeetingRoomActivity.class);
//            contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
            notificationManager.notify(NotificationUtil.NOTIFICATION_SHARE_ID, builder.build());

            notification = builder.build();

            /**
             * 포그라운드 서비스를 시작하면
             * 자동으로 Notification을 띄워준다.
             */
            startForeground(NotificationUtil.NOTIFICATION_SHARE_ID, notification);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NotificationUtil.SHARE_CHANNEL_ID,
                    NotificationUtil.SHARE_CHANNEL_ID,
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
        rectangleRemoveView();
    }


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
