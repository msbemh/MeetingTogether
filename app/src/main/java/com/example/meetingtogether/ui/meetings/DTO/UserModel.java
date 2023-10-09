package com.example.meetingtogether.ui.meetings.DTO;

import android.widget.ImageView;

import androidx.annotation.NonNull;

public class UserModel {
    public String clientId;
    public String name;
    public ImageView profile;

    public UserModel(String clientId){
        this.clientId = clientId;
    }

    @NonNull
    @Override
    public String toString() {
        return "UserModel{" + "clientID=" + this.clientId + ", '}'";
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ImageView getProfile() {
        return profile;
    }

    public void setProfile(ImageView profile) {
        this.profile = profile;
    }
}
