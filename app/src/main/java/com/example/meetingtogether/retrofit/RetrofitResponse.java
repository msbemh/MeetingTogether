package com.example.meetingtogether.retrofit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RetrofitResponse {
    @SerializedName("message")
    private String message;
    @SerializedName("fileInfo")
    private List<FileInfo> fileInfo;

    public List<FileInfo> getFileInfo() {
        return fileInfo;
    }
}
