package com.example.meetingtogether.ui.meetings;

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

import org.webrtc.DataChannel;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoFrame;
import static com.example.meetingtogether.MainActivity.TAG;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class DrawingView extends SurfaceViewRenderer {
    private Vector<DrawingModel> drawingModelVector = new Vector<>();

    private Map<String, DrawingModel> currentDrawingMap = new HashMap<>();

    private Context context;
    private Bitmap bitmap;


    public DrawingView(Context context, AttributeSet attrs, ColorModel colorModel) {
        super(context, attrs);
        this.context = context;

        // 그리기에 사용할 Path와 Paint 객체 초기화
        Path path = new Path();
        Paint paint = createPaint(colorModel);

        DrawingModel drawingModel = new DrawingModel(path, paint, colorModel.getColorType(), "local");
        drawingModelVector.add(drawingModel);
        currentDrawingMap.put("local", drawingModel);

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
        Log.d(TAG, "onDraw");

        if(bitmap != null){
            canvas.drawBitmap(bitmap, 0, 0, null);
        }

        for(DrawingModel drawingModel : drawingModelVector){
            Log.d(TAG, "drawingModel:" + drawingModel);
            Log.d(TAG, "paint:" + drawingModel.getPaint());
            Log.d(TAG, "color:" + drawingModel.getPaint().getColor());
            canvas.drawPath(drawingModel.getPath(), drawingModel.getPaint());
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
        DrawingModel currentLocalDrawingModel = currentDrawingMap.get("local");

        // 연결된 피어 모두애게 그리기 정보를 보낸다.
        for (int i = 0; i < MeetingRoomActivity.peerConnections.size(); i++){
            CustomPeerConnection customPeerConnection = MeetingRoomActivity.peerConnections.get(i);
            DataChannel dataChannel = customPeerConnection.getDataChannel();
            String cmd = "draw";
            String data = cmd + ";" + x + ";" + y + ";" +  currentLocalDrawingModel.getColorType() + ";" + event.getAction();
            ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
            dataChannel.send(new DataChannel.Buffer(buffer, false));
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentLocalDrawingModel.getPath().moveTo(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                currentLocalDrawingModel.getPath().lineTo(x, y);
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

        DrawingModel opponentDrawingModel = currentDrawingMap.get(clientId);
        if(opponentDrawingModel == null){
            // 그리기에 사용할 Path와 Paint 객체 초기화
            Path path = new Path();
            Paint paint = createPaint(new ColorModel(colorType));

            opponentDrawingModel = new DrawingModel(path, paint, colorType, clientId);
            currentDrawingMap.put(clientId, opponentDrawingModel);
            drawingModelVector.add(opponentDrawingModel);
        }else{
            if(!opponentDrawingModel.getColorType().equals(colorType)){
                Path path = new Path();
                Paint paint = createPaint(new ColorModel(colorType));

                opponentDrawingModel = new DrawingModel(path, paint, colorType, clientId);
                currentDrawingMap.put(clientId, opponentDrawingModel);
                drawingModelVector.add(opponentDrawingModel);
            }
        }

        switch (motion) {
            case MotionEvent.ACTION_DOWN:
                opponentDrawingModel.getPath().moveTo(x, y);
                return;
            case MotionEvent.ACTION_MOVE:
                opponentDrawingModel.getPath().lineTo(x, y);
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

        DrawingModel drawingModel = new DrawingModel(path, paint, colorModel.getColorType(), "local");
        currentDrawingMap.put("local", drawingModel);
        drawingModelVector.add(drawingModel);
    }
}
