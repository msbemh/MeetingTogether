package com.example.meetingtogether.model;

public class User extends CommonModel{
    private String id;
    private String password;
    private String name;
    private String nickName;
    private String phoneNum;
    private String jwt;

    public User(String id, String password, String nickName, String phoneNum) {
        super();
        this.password = password;
        this.nickName = nickName;
        this.phoneNum = phoneNum;
    }

    public User(String id, String password) {
        super();
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
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

}
