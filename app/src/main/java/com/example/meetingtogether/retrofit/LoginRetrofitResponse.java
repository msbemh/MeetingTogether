package com.example.meetingtogether.retrofit;

import com.example.meetingtogether.model.User;
import com.google.gson.annotations.SerializedName;

public class LoginRetrofitResponse {
    @SerializedName("result")
    private boolean result;

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private User user;

    public User getUser() {
        return user;
    }
    public boolean isResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }
}
