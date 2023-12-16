package com.example.meetingtogether.model;

import com.example.meetingtogether.dialogs.CustomDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class User extends CommonModel{
    private String id;
    private String password;
    private String name;
    private String nickName;
    private String phoneNum;
    private String jwt;
    private List<ProfileMap> profileImgPaths;

    public User(String id, String password, String nickName, String phoneNum) {
        super();
        this.password = password;
        this.nickName = nickName;
        this.phoneNum = phoneNum;
    }

    public User(String id, String password) {
        super();
        this.id = id;
        this.password = password;
    }

    public List<ProfileMap> getProfileImgPaths() {
        return profileImgPaths;
    }

    public ProfileMap getMyProfileMap() {
        List<ProfileMap> list = null;
        try{
            list = profileImgPaths.stream().filter(profileMap -> profileMap.getType().equals(CustomDialog.Type.PROFILE_IMAGE.name())).collect(Collectors.toList());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        if(list.size() == 0) return null;

        return list.get(0);
    }

    public ProfileMap getMyBackgroundMap() {
        List<ProfileMap> list = null;
        try{
            list = profileImgPaths.stream().filter(profileMap -> profileMap.getType().equals(CustomDialog.Type.BACKGROUND_IMAGE.name())).collect(Collectors.toList());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        if(list.size() == 0) return null;

        return list.get(0);
    }

    public void setProfileImgPaths(List<ProfileMap> profileImgPaths) {
        this.profileImgPaths = profileImgPaths;
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

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getNickName() {
        return nickName;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

}
