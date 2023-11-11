package com.example.meetingtogether.model;

import com.google.gson.annotations.SerializedName;

public class Contact extends CommonModel{
    @SerializedName("friendId")
    private String friendId;

    @SerializedName("friendName")
    private String friendName;

    @SerializedName("friendPhoneNum")
    private String friendPhoneNum;

    public Contact(String friendName, String friendPhoneNum) {
        super();
        this.friendName = friendName;
        this.friendPhoneNum = friendPhoneNum;
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
