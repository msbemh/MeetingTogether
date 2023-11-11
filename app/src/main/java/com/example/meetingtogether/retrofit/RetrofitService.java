package com.example.meetingtogether.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {

    private Retrofit retrofit;
    private RetrofitInterface service;
    private static RetrofitService instance;

    public RetrofitService(){
        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl("https://webrtc-sfu.kro.kr")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        service = retrofit.create(RetrofitInterface.class);
    }

    public static RetrofitService getInstance(){
        if(instance == null){
            instance = new RetrofitService();
        }
        return instance;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public RetrofitInterface getService() {
        return service;
    }
}
