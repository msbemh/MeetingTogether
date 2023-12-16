package com.example.meetingtogether.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {

    private Retrofit retrofit;
    private RetrofitInterface service;
    private static RetrofitService instance;

    public RetrofitService(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .create();

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HeaderInterceptor())  // 여기에서 Interceptor를 추가합니다.
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://webrtc-sfu.kro.kr")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
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
