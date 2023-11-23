package com.example.meetingtogether.retrofit;

import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.model.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
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

    @FormUrlEncoded
    @POST("login")
    Call<CommonRetrofitResponse<User>> postLogin(@Field("userId") String userId, @Field("password") String password);

    @POST("sync-friend")
    Call<CommonRetrofitResponse<List<Contact>>> postSyncFriend(@Body List<Contact> contactList);

    @POST("sign-up")
    Call<CommonRetrofitResponse> postSignUp(@Body User user);

    @FormUrlEncoded
    @POST("req-otp")
    Call<CommonRetrofitResponse> postReqOtp(@Field("phoneNum") String phoneNum);

    @FormUrlEncoded
    @POST("email-chk")
    Call<CommonRetrofitResponse> postEmailChk(@Field("userId") String userId);

    @Multipart
    @POST("update-profile-image")
    Call<CommonRetrofitResponse> postProfileImages(@Part MultipartBody.Part profileImg, @Part MultipartBody.Part backgroundImg, @Part("info")RequestBody requestBody);

    @FormUrlEncoded
    @POST("delete-profile-image")
    Call<CommonRetrofitResponse> postDeleteProfileImages(@Field("delete_file") String delete_file);
}
