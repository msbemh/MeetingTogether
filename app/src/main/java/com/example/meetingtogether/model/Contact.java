package com.example.meetingtogether.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Contact extends CommonModel{
    @SerializedName("friendId")
    private String friendId;

    @SerializedName("friendName")
    private String friendName;

    @SerializedName("friendPhoneNum")
    private String friendPhoneNum;

    @SerializedName("friendImgPaths")
    private List<ProfileMap> friendImgPaths;

    public Contact(String friendName, String friendPhoneNum) {
        super();
        this.friendName = friendName;
        this.friendPhoneNum = friendPhoneNum;
    }

    public Contact(String friendId, String friendName, String friendPhoneNum, List<ProfileMap> friendImgPaths) {
        this.friendId = friendId;
        this.friendName = friendName;
        this.friendPhoneNum = friendPhoneNum;
        this.friendImgPaths = friendImgPaths;
    }

    public List<ProfileMap> getFriendImgPaths() {
        return friendImgPaths;
    }

    public void setFriendImgPaths(List<ProfileMap> friendImgPaths) {
        this.friendImgPaths = friendImgPaths;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendPhoneNum() {
        return friendPhoneNum;
    }

    public void setFriendPhoneNum(String friendPhoneNum) {
        this.friendPhoneNum = friendPhoneNum;
    }
}
