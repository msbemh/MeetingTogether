package com.example.meetingtogether.retrofit;

import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.model.CreateRoomDTO;
import com.example.meetingtogether.model.MessageDTO;
import com.example.meetingtogether.model.User;
import com.example.meetingtogether.ui.meetings.DTO.ChatRoomListDTO;
import com.example.meetingtogether.ui.meetings.DTO.MeetingDTO;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

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

    @GET("get_chat_room_list")
    Call<List<ChatRoomListDTO>> getChatRoomList();

    @GET("get_room_all_message")
    Call<List<MessageDTO>> getRoomAllMessage(@Query(value = "roomId") int roomId);

    @FormUrlEncoded
    @POST("post_chat_renew_out_date")
    Call<CommonRetrofitResponse> postChatRenewOutDate(@Field("roomId") int roomId);

    @FormUrlEncoded
    @POST("post_chat_renew_in_date")
    Call<CommonRetrofitResponse> postChatRenewInDate(@Field("roomId") int roomId);

    @Multipart
    @POST("post_upload_images")
    Call<MessageDTO> postUploadImages(@Part List<MultipartBody.Part> imageList, @Part("info")RequestBody requestBody);

    @POST("post_create_group_room")
    Call<CommonRetrofitResponse> postCreateGroupRoom(@Body CreateRoomDTO createRoomDTO);

    @POST("post_chat_queue_and_delete")
    Call<List<MessageDTO>> postChatQueueAndDelete();

    @POST("post_create_meeting")
    Call<CommonRetrofitResponse> postCreateMeeting(@Body MeetingDTO meetingDTO);

    @POST("post_check_delete_meeting")
    Call<CommonRetrofitResponse> postCheckDeleteMeeting(@Body MeetingDTO meetingDTO);

    @POST("post_current_client_update")
    Call<CommonRetrofitResponse> postCurrentClientUpdate(@Body MeetingDTO meetingDTO);

    @GET("get_meeting_room_list")
    Call<List<MeetingDTO>> getMeetingRoomList();

    @GET("check_exist_room")
    Call<MeetingDTO> checkExistRoom(@Query(value = "roomId") int roomId);



}
