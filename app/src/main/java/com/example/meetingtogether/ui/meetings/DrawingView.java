package com.example.meetingtogether.ui.meetings;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.example.meetingtogether.MyApplication;
import com.example.meetingtogether.common.ColorType;
import com.example.meetingtogether.ui.meetings.DTO.ColorModel;
import com.example.meetingtogether.ui.meetings.DTO.DrawingModel;

import org.webrtc.DataChannel;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoFrame;
import static com.example.meetingtogether.MainActivity.TAG;
import static com.example.meetingtogether.common.Util.DRAW_TAG;
import static com.example.meetingtogether.common.Util.WEBRTC_WHITEBOARD;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class DrawingView extends SurfaceViewRenderer {
    private ArrayList<DrawingModel> drawingModelVector = new ArrayList<>();

    private Context context;
    private Bitmap bitmap;
    private Object rock = new Object();
    private ColorType colorType = ColorType.BLACK;
    private Handler handler;

    public DrawingView(Context context, AttributeSet attrs, ColorModel colorModel) {
        super(context, attrs);
        this.context = context;

        HandlerThread handlerChatThread = new HandlerThread("chat-thread");
        handlerChatThread.start();
        handler = new android.os.Handler(handlerChatThread.getLooper());

        DrawingModel drawingModel = createDrawingModel(colorModel.getColorType(), "local");
        synchronized (rock){
            drawingModelVector.add(drawingModel);
        }

        setWillNotDraw(false);
        setBackgroundColor(Color.WHITE);
    }

    private Paint createPaint(ColorModel colorModel){
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(MyApplication.colorTypeIntegerEnumMap.get(colorModel.getColorType()));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        if(colorModel.getColorType() == ColorType.ERASER) paint.setStrokeWidth(30f);
        else paint.setStrokeWidth(5f);

        return paint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(DRAW_TAG, "onDraw 메소드가 동작합니다.");

        if(bitmap != null){
            canvas.drawBitmap(bitmap, 0, 0, null);
        }

        synchronized (rock){
            for(DrawingModel drawingModel : drawingModelVector){
                Log.d(WEBRTC_WHITEBOARD, "drawingModel:" + drawingModel);
                Log.d(WEBRTC_WHITEBOARD, "path:" + drawingModel.getPath());
                Log.d(WEBRTC_WHITEBOARD, "paint:" + drawingModel.getPaint());
                Log.d(WEBRTC_WHITEBOARD, "color:" + drawingModel.getPaint().getColor());
                canvas.drawPath(drawingModel.getPath(), drawingModel.getPaint());
            }
        }

//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                synchronized (rock){
//                    for(DrawingModel drawingModel : drawingModelVector){
//                        Log.d(WEBRTC_WHITEBOARD, "drawingModel:" + drawingModel);
//                        Log.d(WEBRTC_WHITEBOARD, "path:" + drawingModel.getPath());
//                        Log.d(WEBRTC_WHITEBOARD, "paint:" + drawingModel.getPaint());
//                        Log.d(WEBRTC_WHITEBOARD, "color:" + drawingModel.getPaint().getColor());
//                        ((Activity)DrawingView.this.context).runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                canvas.drawPath(drawingModel.getPath(), drawingModel.getPaint());
//                            }
//                        });
//                    }
//                }
//
//            }
//        });

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        Log.d(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        super.surfaceChanged(holder, format, width, height);
        Log.d(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        Log.d(TAG, "surfaceDestroyed");
    }

    @Override
    public void onFrame(VideoFrame frame) {
        super.onFrame(frame);
    }


    /**
     * 이곳에서 Rock이 걸려버리면 ANR 에러가 발생한다.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent");
        Log.d(DRAW_TAG, "onTouchEvent 메소드가 동작합니다.");

        DrawingModel drawingModel = getLastDrawingModel("local");

        if(drawingModel == null || drawingModel.isEnd()){
            drawingModel = createDrawingModel(DrawingView.this.colorType, "local");
            synchronized (rock){
                drawingModelVector.add(drawingModel);
            }
        }

        // 마우스 이벤트 처리
        float x = event.getX();
        float y = event.getY();
//        DrawingModel currentLocalDrawingModel = currentDrawingMap.get("local");

        // 연결된 피어 모두애게 그리기 정보를 보낸다.
        for (int i = 0; i < MeetingRoomActivity.peerConnections.size(); i++){
            CustomPeerConnection customPeerConnection = MeetingRoomActivity.peerConnections.get(i);
            DataChannel dataChannel = customPeerConnection.getDataChannel();
            String cmd = "draw";
//            String data = cmd + ";" + x + ";" + y + ";" +  currentLocalDrawingModel.getColorType() + ";" + event.getAction();
            String data = cmd + ";" + x + ";" + y + ";" +  drawingModel.getColorType() + ";" + event.getAction();

            if(i == 0){
                Log.d(DRAW_TAG, data + " 데이터를 모든 피어에게 보냅니다.");
            }

            ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
            dataChannel.send(new DataChannel.Buffer(buffer, false));
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                currentLocalDrawingModel.getPath().moveTo(x, y);
                drawingModel.getPath().moveTo(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
//                currentLocalDrawingModel.getPath().lineTo(x, y);
                drawingModel.getPath().lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                // 그리기 완료
                drawingModel.setEnd(true);
                break;
            default:
                return false;
        }

        Log.d(DRAW_TAG, "화이트 보드 화면 렌더링을 시작합니다.");

        // 화면 다시 그리기
//        ((Activity)(DrawingView.this.context)).runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                invalidate();
//            }
//        });
        invalidate();
        return true;
    }

    // 비트맵 설정 메서드
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate(); // 뷰 다시 그리기 요청
    }

    public void fireDraw(float x, float y, ColorType colorType, String clientId, Integer motion) {
        Log.d(TAG, "fireDraw");
        Log.d(DRAW_TAG, "상대방(" + clientId + ")에 의해 motion : " + motion + ", colorType:" + colorType + " 정보로 그림을 그립니다.");

        DrawingModel drawingModel = getLastDrawingModel(clientId);

        if(drawingModel == null || drawingModel.isEnd()){
            drawingModel = createDrawingModel(x, y, colorType, clientId);
            synchronized (rock) {
                drawingModelVector.add(drawingModel);
            }
        }

        switch (motion) {
            case MotionEvent.ACTION_DOWN:
                //                opponentDrawingModel.getPath().moveTo(x, y);
                drawingModel.getPath().moveTo(x, y);
                Log.d(DRAW_TAG, "x:" + x + " y:" + y + "로 Path를 이동시킵니다.");
                return;
            case MotionEvent.ACTION_MOVE:
                //                opponentDrawingModel.getPath().lineTo(x, y);
                drawingModel.getPath().lineTo(x, y);
                Log.d(DRAW_TAG, "x:" + x + " y:" + y + "로 Line을 이어서 그립니다.");
                break;
            case MotionEvent.ACTION_UP:
                // 그리기 완료
                drawingModel.setEnd(true);
                return;
            default:
                return;
        }

        Log.d(DRAW_TAG, "상대방(" + clientId + ")에 의해 화면 렝더링을 시작합니다.");
        invalidate();
    }

    private DrawingModel createDrawingModel(ColorType colorType, String clientId){
        // 그리기에 사용할 Path와 Paint 객체 초기화
        Path path = new Path();
        Paint paint = createPaint(new ColorModel(colorType));

        DrawingModel drawingModel = new DrawingModel(path, paint, colorType, clientId);

        return drawingModel;
    }

    private DrawingModel createDrawingModel(float x, float y, ColorType colorType, String clientId){
        // 그리기에 사용할 Path와 Paint 객체 초기화
        Path path = new Path();
        path.moveTo(x, y);
        Paint paint = createPaint(new ColorModel(colorType));

        DrawingModel drawingModel = new DrawingModel(path, paint, colorType, clientId);

        return drawingModel;
    }

    // 로컬에서 호출
    public void setPaintColor(ColorModel colorModel) {
        DrawingModel drawingModel = createDrawingModel(colorModel.getColorType(), "local");
        Log.d(DRAW_TAG, "새로운 Color에 대한 DrawingModel을 생성합니다. => " + drawingModel);
        this.colorType = colorModel.getColorType();
        synchronized (rock){
            drawingModelVector.add(drawingModel);
        }
    }

    private DrawingModel getLastDrawingModel(String clientId){
//        if(drawingModelVector.size() > 0){
//            return drawingModelVector.get(drawingModelVector.size() -1);
//        }
//        return null;
        DrawingModel drawingModel = null;
        for(int i=drawingModelVector.size()-1; i>=0; i--){
            drawingModel = drawingModelVector.get(i);
            if(drawingModel.getClientId().equals(clientId)){
                return drawingModel;
            }
        }

        return null;
    }
}
