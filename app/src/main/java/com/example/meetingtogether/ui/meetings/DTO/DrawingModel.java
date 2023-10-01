package com.example.meetingtogether.ui.meetings.DTO;

import android.graphics.Paint;
import android.graphics.Path;

import com.example.meetingtogether.common.ColorType;

public class DrawingModel {
    private Path path;
    private Paint paint;
    private ColorType colorType;

    public DrawingModel(Path path, Paint paint, ColorType colorType) {
        this.path = path;
        this.paint = paint;
        this.colorType = colorType;
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
}
