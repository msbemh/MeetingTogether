package com.example.meetingtogether.model;

import java.util.List;

public class MessageDTO {
    private String message;
    private RequestType type;
    private User user;
    private int roomUuid;
    private String roomName;
    private Room room;
    private List<Room> roomList;
    private List<User> userList;
    private User receiveUser;
    private Status status;
    private RoomType roomType;

    public enum RoomType{
        INDIVIDUAL,
        GROUP
    }

    public enum Status{
        NONE_ROOM,
        SUCCESS,
        ALREADY_IN_ROOM
    }

    public enum RequestType{
        USER_ADD,
        ROOM_LIST,
        USER_LIST,
        MESSAGE,
        ROOM_ENTER,
        ROOM_OUT,
        ROOM_CREATE,
        EXIT
    }


    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Room> getRoomList() {
        return roomList;
    }

    public void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RequestType getType() {
        return type;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public User getReceiveUser() {
        return receiveUser;
    }

    public void setReceiveUser(User receiveUser) {
        this.receiveUser = receiveUser;
    }

    public int getRoomUuid() {
        return roomUuid;
    }

    public void setRoomUuid(int roomUuid) {
        this.roomUuid = roomUuid;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
