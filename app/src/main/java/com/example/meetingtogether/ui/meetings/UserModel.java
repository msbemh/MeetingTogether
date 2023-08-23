package com.example.meetingtogether.ui.meetings;

import androidx.annotation.NonNull;

public class UserModel {
    public String clientId;

    public UserModel(String clientId){
        this.clientId = clientId;
    }

    @NonNull
    @Override
    public String toString() {
        return "UserModel{" + "clientID=" + this.clientId + ", '}'";

    }
}
