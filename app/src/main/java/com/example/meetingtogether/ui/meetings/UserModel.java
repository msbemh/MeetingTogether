package com.example.meetingtogether.ui.meetings;

import androidx.annotation.NonNull;

public class UserModel {
    public String clientID;

    public UserModel(String clientID){
        this.clientID = clientID;
    }

    @NonNull
    @Override
    public String toString() {
        return "UserModel{" + "clientID=" + this.clientID + ", '}'";

    }
}
