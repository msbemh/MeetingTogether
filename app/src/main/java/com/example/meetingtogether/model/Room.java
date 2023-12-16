package com.example.meetingtogether.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Room {
    private String uuid;
    private String name;
    private List<User> userList = new ArrayList<>();

    public Room(String name) {
        this.name = name;
        this.uuid = UUID.randomUUID().toString();
    }

    public void addUser(User user){
        userList.add(user);
    }

    public void removeUser(User user){
        this.userList = this.userList.stream().filter(user1 -> !user1.getId().equals(user.getId())).collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }
}
