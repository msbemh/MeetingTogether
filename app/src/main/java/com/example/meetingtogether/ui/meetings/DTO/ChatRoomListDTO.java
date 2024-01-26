package com.example.meetingtogether.ui.meetings.DTO;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChatRoomListDTO {
    @SerializedName("friend_id")
    private String friendId;
    @SerializedName("friend_name")
    private String friendName;
    @SerializedName("friend_profile_img_path")
    private String friendProfileImgPath;
    @SerializedName("friend_phone_num")
    private String friendPhoneNum;
    @SerializedName("room_id")
    private int roomId;
    @SerializedName("recent_message")
    private String recentMessage;
    @SerializedName("recent_message_create_date")
    private LocalDateTime recentMessageCreateDate;
    @SerializedName("no_read_cnt")
    private int noReadCnt;
    @SerializedName("room_name")
    private String roomName;
    @SerializedName("room_type")
    private String roomType;
    @SerializedName("user_cnt")
    private int userCnt;


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

    public String getFriendProfileImgPath() {
        return friendProfileImgPath;
    }

    public void setFriendProfileImgPath(String friendProfileImgPath) {
        this.friendProfileImgPath = friendProfileImgPath;
    }

    public String getFriendPhoneNum() {
        return friendPhoneNum;
    }

    public void setFriendPhoneNum(String friendPhoneNum) {
        this.friendPhoneNum = friendPhoneNum;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRecentMessage() {
        return recentMessage;
    }

    public void setRecentMessage(String recentMessage) {
        this.recentMessage = recentMessage;
    }

    public LocalDateTime getRecentMessageCreateDate() {
        return recentMessageCreateDate;
    }

    public void setRecentMessageCreateDate(LocalDateTime recentMessageCreateDate) {
        this.recentMessageCreateDate = recentMessageCreateDate;
    }

    public int getNoReadCnt() {
        return noReadCnt;
    }

    public void setNoReadCnt(int noReadCnt) {
        this.noReadCnt = noReadCnt;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }
}
