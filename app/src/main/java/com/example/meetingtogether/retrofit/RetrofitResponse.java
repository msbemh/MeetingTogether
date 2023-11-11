package com.example.meetingtogether.retrofit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RetrofitResponse {

    @SerializedName("message")
    private String message;
    @SerializedName("fileInfos")
    private List<FileInfo> fileInfos;
    @SerializedName("fileInfo")
    private FileInfo fileInfo;

    public List<FileInfo> getFileInfos() {
        return fileInfos;
    }
    public FileInfo getFileInfo() {
        return fileInfo;
    }
}
