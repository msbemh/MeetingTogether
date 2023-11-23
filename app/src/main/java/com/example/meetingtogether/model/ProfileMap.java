package com.example.meetingtogether.model;

public class ProfileMap {
    private String profileImgPath;
    private String type;

    public ProfileMap(String profileImgPath, String type) {
        this.profileImgPath = profileImgPath;
        this.type = type;
    }

    public String getProfileImgPath() {
        return profileImgPath;
    }

    public void setProfileImgPath(String profileImgPath) {
        this.profileImgPath = profileImgPath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
