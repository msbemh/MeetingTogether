package com.example.meetingtogether.ui.meetings.DTO;

import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.model.User;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.util.List;

public class MeetingDTO {

    @SerializedName("id")
    private int id;
    @SerializedName("title")
    private String title;

    @SerializedName("type")
    private TYPE type;

    @SerializedName("password")
    private String password;

    @SerializedName("max_client")
    private int maxClient;

    @SerializedName("current_client")
    private int currentClient;

    @SerializedName("reserve_start_date")
    private LocalDateTime reserve_start_date;

    @SerializedName("reserve_end_date")
    private LocalDateTime reserve_end_date;

    @SerializedName("host")
    private String host;

    @SerializedName("current_client_update_type")
    private CURRENT_CLIENT_UPDATE_TYPE currentClientUpdateType;

    @SerializedName("is_activate_camera")
    private boolean isActivateCamera;

    @SerializedName("user")
    private User user;

    @SerializedName("reserve_contact_list")
    private List<Contact> reserveContactList;

    public enum TYPE{
        PUBLIC,
        PRIVATE,
        RESERVE
    }

    public enum CURRENT_CLIENT_UPDATE_TYPE{
        INCREASE,
        DECREASE
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getReserve_start_date() {
        return reserve_start_date;
    }

    public void setReserve_start_date(LocalDateTime reserve_start_date) {
        this.reserve_start_date = reserve_start_date;
    }

    public LocalDateTime getReserve_end_date() {
        return reserve_end_date;
    }

    public void setReserve_end_date(LocalDateTime reserve_end_date) {
        this.reserve_end_date = reserve_end_date;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public CURRENT_CLIENT_UPDATE_TYPE getCurrentClientUpdateType() {
        return currentClientUpdateType;
    }

    public void setCurrentClientUpdateType(CURRENT_CLIENT_UPDATE_TYPE currentClientUpdateType) {
        this.currentClientUpdateType = currentClientUpdateType;
    }

    public int getMaxClient() {
        return maxClient;
    }

    public void setMaxClient(int maxClient) {
        this.maxClient = maxClient;
    }

    public int getCurrentClient() {
        return currentClient;
    }

    public void setCurrentClient(int currentClient) {
        this.currentClient = currentClient;
    }

    public boolean isActivateCamera() {
        return isActivateCamera;
    }

    public void setActivateCamera(boolean activateCamera) {
        isActivateCamera = activateCamera;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Contact> getReserveContactList() {
        return reserveContactList;
    }

    public void setReserveContactList(List<Contact> reserveContactList) {
        this.reserveContactList = reserveContactList;
    }
}
