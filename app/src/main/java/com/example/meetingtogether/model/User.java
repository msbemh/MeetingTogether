package com.example.meetingtogether.model;

public class User {

    private String id;
    private String password;
    private String name;
    private String nickName;
    private String phoneNum;
    private String jwt;

    public User(String id, String password, String nickName, String phoneNum) {
        this.id = id;
        this.password = password;
        this.nickName = nickName;
        this.phoneNum = phoneNum;
    }

    public User(String id, String password) {
        this.id = id;
        this.password = password;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getNickName() {
        return nickName;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public String getJwt() {
        return jwt;
    }
}
