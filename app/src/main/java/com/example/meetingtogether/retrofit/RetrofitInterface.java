package com.example.meetingtogether.retrofit;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

// https://webrtc-sfu.kro.kr:3030/images/mask1-1697346399407.png
public interface RetrofitInterface {
    @Multipart
    @POST("profile")
    Call<RetrofitResponse> postProfile(@Part MultipartBody.Part image);

    @Multipart
    @POST("photo")
    Call<RetrofitResponse> postImage(@Part MultipartBody.Part image);

    @Multipart
    @POST("photos")
    Call<RetrofitResponse> postImages(@Part List<MultipartBody.Part> images);
}
