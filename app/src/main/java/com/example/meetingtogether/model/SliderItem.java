package com.example.meetingtogether.model;

public class SliderItem {

    private String imagePath = "";

    public SliderItem(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
