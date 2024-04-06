package com.example.meetingtogether.ui.meetings.DTO;

import android.graphics.Paint;
import android.graphics.Path;

import androidx.annotation.NonNull;

import com.example.meetingtogether.common.ColorType;

public class DrawingModel {
    private Path path;
    private Paint paint;
    private ColorType colorType;
    private String clientId;
    private boolean isEnd;

    @NonNull
    @Override
    public String toString() {
        return "DrawingModel(path:" + path + ", paint:" + paint.getColor() + ", colorType:" + colorType.name() + ", clientId:" + clientId + ")";
    }

    public DrawingModel(Path path, Paint paint, ColorType colorType) {
        this.path = path;
        this.paint = paint;
        this.colorType = colorType;
        this.isEnd = false;
    }

    public DrawingModel(Path path, Paint paint, ColorType colorType, String clientId) {
        this.path = path;
        this.paint = paint;
        this.colorType = colorType;
        this.clientId = clientId;
        this.isEnd = false;
    }

    public ColorType getColorType() {
        return colorType;
    }

    public void setColorType(ColorType colorType) {
        this.colorType = colorType;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }
}
