package com.example.meetingtogether.model;

public class CommonModel<T> {
    private T data;

    public CommonModel() {
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
