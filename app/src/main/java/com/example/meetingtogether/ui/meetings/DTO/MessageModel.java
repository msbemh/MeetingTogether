package com.example.meetingtogether.ui.meetings.DTO;

import com.example.meetingtogether.retrofit.FileInfo;

import java.time.LocalDate;
import java.util.List;

public class MessageModel {

    public enum MessageType{
        RECEIVE(0),
        SEND(1);

        private Integer value;

        MessageType(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }


    private MessageType messageType;
    private String msg;
    private String filename;
    private UserModel sender;
    private LocalDate createDate;
    private String date;

    public MessageModel(MessageType messageType, String msg, String filename, UserModel sender, String date) {
        this.messageType = messageType;
        this.msg = msg;
        this.sender = sender;
        this.date = date;
        this.filename = filename;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public UserModel getSender() {
        return sender;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setSender(UserModel sender) {
        this.sender = sender;
    }

    public LocalDate getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDate createDate) {
        this.createDate = createDate;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
