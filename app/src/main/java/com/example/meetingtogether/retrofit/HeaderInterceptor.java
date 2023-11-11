package com.example.meetingtogether.retrofit;

import com.example.meetingtogether.common.Util;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HeaderInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        // 현재의 요청을 가져옵니다.
        Request originalRequest = chain.request();

        String jwt = "";
        String id = "";
        if(Util.user != null) {
            jwt = Util.user.getJwt();
            id = Util.user.getId();
        }

        // 새로운 헤더를 추가하여 수정된 요청을 생성합니다.
        Request newRequest = originalRequest.newBuilder()
                .header("Authorization", jwt)
                .header("id", id)
                .build();

        // 수정된 요청을 사용하여 체인을 계속 진행합니다.
        return chain.proceed(newRequest);
    }
}
