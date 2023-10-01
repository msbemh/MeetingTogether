package com.example.meetingtogether.ui.meetings;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.example.meetingtogether.MyApplication;
import com.example.meetingtogether.common.ColorType;
import com.example.meetingtogether.ui.meetings.DTO.ColorModel;
import com.example.meetingtogether.ui.meetings.DTO.DrawingModel;
import com.example.meetingtogether.ui.meetings.DTO.PeerDrawing;

import org.webrtc.DataChannel;
import org.webrtc.PeerConnection;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoFrame;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DrawingView extends SurfaceViewRenderer {
    private List<DrawingModel> drawingModelList = new ArrayList<>();

    private String TAG = "TEST";
    private Context context;
    private Bitmap bitmap;
    public DrawingModel currentDrawingModel;
    private List<PeerDrawing> peerDrawingList = new ArrayList<>();

    public DrawingView(Context context, AttributeSet attrs, ColorModel colorModel) {
        super(context, attrs);
        this.context = context;

        // 그리기에 사용할 Path와 Paint 객체 초기화
        Path path = new Path();
        Paint paint = createPaint(colorModel);

        currentDrawingModel = new DrawingModel(path, paint, colorModel.getColorType());
        drawingModelList.add(currentDrawingModel);

        for(CustomPeerConnection customPeerConnection : MeetingRoomActivity.peerConnections){
            peerDrawingList.add(new PeerDrawing(customPeerConnection.getClientId()));
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
        paint.setStrokeWidth(5f);

        return paint;
    }

    public PeerDrawing getPeerDrawing(String clientId){
        for(PeerDrawing peerDrawing : peerDrawingList){
            if(clientId.equals(peerDrawing.getClientId())){
                return peerDrawing;
            }
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw");

        if(bitmap != null){
            canvas.drawBitmap(bitmap, 0, 0, null);
//            bitmap = null;
        }

        // 로컬에 있는 페인팅 경로 그리기
        for(DrawingModel drawingModel : drawingModelList){
            canvas.drawPath(drawingModel.getPath(), drawingModel.getPaint());
        }

        // 상대방 피어에 있는 모든 페인팅 경로 그리기
        for(PeerDrawing peerDrawing : peerDrawingList){
            List<DrawingModel> drawingModelList = peerDrawing.getDrawingModelList();
            for(DrawingModel drawingModel : drawingModelList){
                canvas.drawPath(drawingModel.getPath(), drawingModel.getPaint());
            }
        }
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


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent");
        // 마우스 이벤트 처리
        float x = event.getX();
        float y = event.getY();


        for (int i = 0; i < MeetingRoomActivity.peerConnections.size(); i++){
            CustomPeerConnection customPeerConnection = MeetingRoomActivity.peerConnections.get(i);
            DataChannel dataChannel = customPeerConnection.getDataChannel();
            String cmd = "draw";
            String data = cmd + ";" + x + ";" + y + ";" +  currentDrawingModel.getColorType() + ";" + event.getAction();
            ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
            dataChannel.send(new DataChannel.Buffer(buffer, false));
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentDrawingModel.getPath().moveTo(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                currentDrawingModel.getPath().lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                // 그리기 완료
                break;
            default:
                return false;
        }

        // 화면 다시 그리기
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

        PeerDrawing peerDrawing = getPeerDrawing(clientId);

        if(peerDrawing.getCurrentDrawingModel().getColorType() != colorType){
            // 그리기에 사용할 Path와 Paint 객체 초기화
            Path path = new Path();
            Paint paint = createPaint(new ColorModel(colorType));

            currentDrawingModel = new DrawingModel(path, paint, colorType);
            peerDrawing.getDrawingModelList().add(currentDrawingModel);
        }

        switch (motion) {
            case MotionEvent.ACTION_DOWN:
                currentDrawingModel.getPath().moveTo(x, y);
                return;
            case MotionEvent.ACTION_MOVE:
                currentDrawingModel.getPath().lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                // 그리기 완료
                break;
            default:
                return;
        }

        // 화면 다시 그리기
        invalidate();
    }

    // 로컬에서 호출
    public void setPaintColor(ColorModel colorModel) {
        // 그리기에 사용할 Path와 Paint 객체 초기화
        Path path = new Path();
        Paint paint = createPaint(colorModel);

        currentDrawingModel = new DrawingModel(path, paint, colorModel.getColorType());
        drawingModelList.add(currentDrawingModel);
    }
}
