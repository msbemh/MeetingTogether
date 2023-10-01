package com.example.meetingtogether.ui.meetings.DTO;

import static com.example.meetingtogether.common.Constant.BLACK;

import android.graphics.Color;

import com.example.meetingtogether.common.ColorType;

public class ColorModel {
    private ColorType colorType;

    public ColorModel() {
        this.colorType = ColorType.BLACK;
    }

    public ColorModel(ColorType colorType) {
        this.colorType = colorType;
    }

    public ColorType getColorType() {
        return colorType;
    }

    public void setColorType(ColorType colorType) {
        this.colorType = colorType;
    }

}
