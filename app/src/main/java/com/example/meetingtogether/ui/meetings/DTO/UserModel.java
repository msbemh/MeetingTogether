package com.example.meetingtogether.ui.meetings.DTO;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.meetingtogether.model.ProfileMap;

import java.util.List;

public class UserModel {
    public String clientId;
    public String name;
    public ImageView profile;
    private String imgPath;
    private String email;
    private String host;
    private boolean isHost = false;
    private List<ProfileMap> profileImgPaths;

    public UserModel(){
    }

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

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    public boolean isHost() {
        return isHost;
    }

    public List<ProfileMap> getProfileImgPaths() {
        return profileImgPaths;
    }

    public void setProfileImgPaths(List<ProfileMap> profileImgPaths) {
        this.profileImgPaths = profileImgPaths;
    }
}
