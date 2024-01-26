package com.example.meetingtogether.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CreateRoomDTO {
    @SerializedName("name")
    private String name;
    @SerializedName("contact_list")
    private List<Contact> contactList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Contact> getContactList() {
        return contactList;
    }

    public void setContactList(List<Contact> contactList) {
        this.contactList = contactList;
    }
}
