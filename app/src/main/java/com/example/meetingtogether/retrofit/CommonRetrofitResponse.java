package com.example.meetingtogether.retrofit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CommonRetrofitResponse<T> {
    @SerializedName("result")
    private boolean result;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    public T getData() {
        return data;
    }

    public boolean isResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }
}
