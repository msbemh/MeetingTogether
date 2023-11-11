package com.example.meetingtogether.sharedPreference;

import android.content.SharedPreferences;
import android.util.Log;

import com.example.meetingtogether.model.User;
import com.google.gson.Gson;

import org.json.JSONObject;

public class SharedPreferenceRepository {
    private static final String TAG = SharedPreferenceRepository.class.getSimpleName();
    public static SharedPreferences pref;                     // 프리퍼런스
    public static SharedPreferences.Editor editor;            // 에디터

    /**
     * 자동 로그인을 위해서
     * 사용자 정보 및 jwt 저장
     */
    public static void saveUserForAutoLogin(User user){
        try {
            Gson gson = new Gson();
            String json = gson.toJson(user);

            Log.d(TAG, "json:" + json);
            editor.putString("user", json);
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


            Gson gson = new Gson();
            User user = gson.fromJson(userJson, User.class);

            Log.d(TAG, "user:" + user);
            return user;
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


}
