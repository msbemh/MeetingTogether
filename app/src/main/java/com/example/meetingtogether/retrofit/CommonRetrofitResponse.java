package com.example.meetingtogether.retrofit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CommonRetrofitResponse {
    @SerializedName("result")
    private boolean result;

    @SerializedName("message")
    private String message;

    public boolean isResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }
}
