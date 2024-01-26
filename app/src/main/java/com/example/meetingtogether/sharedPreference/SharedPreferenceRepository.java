package com.example.meetingtogether.sharedPreference;

import android.content.SharedPreferences;
import android.util.Log;

import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.model.User;
import com.example.meetingtogether.retrofit.LocalDateTimeDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SharedPreferenceRepository {
    private static final String TAG = SharedPreferenceRepository.class.getSimpleName();
    public static SharedPreferences pref;                     // 프리퍼런스
    public static SharedPreferences.Editor editor;            // 에디터
    public static Gson gson;

    public static void init(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());
        gson = gsonBuilder.create();
    }

    /**
     * 자동 로그인을 위해서
     * 사용자 정보 및 jwt 저장
     */
    public static void saveUserForAutoLogin(User user){
        try {
            String json = gson.toJson(user);

            Log.d(TAG, "json:" + json);
            editor.putString("user", json);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    public static void saveFriendList(List<Contact> responseContactList){
        try {
            String json = gson.toJson(responseContactList);

            Log.d(TAG, "json:" + json);
            editor.putString("friendList", json);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    public static User getUser(){
        try {
            String userJson = pref.getString("user", null);
            if(userJson == null) return null;

            User user = gson.fromJson(userJson, User.class);

            Log.d(TAG, "user:" + user);
            return user;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        return null;
    }
    public static List<Contact> getFriendList(){
        try {
            String friendListJson = pref.getString("friendList", null);
            if(friendListJson == null) return new ArrayList<>();

            List<Contact> friendList = gson.fromJson(friendListJson, new TypeToken<ArrayList<Contact>>(){}.getType());

            Log.d(TAG, "friendList:" + friendList);
            return friendList;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 자동 로그인을 위해서
     * email, password 정보 삭제
     */
    public static void deleteUserForAutoLogin(){
        try {
            editor.remove("user");
            editor.apply();
        }catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    public static void clear(){
        try {
            editor.clear();
            editor.apply();
        }catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }


}
