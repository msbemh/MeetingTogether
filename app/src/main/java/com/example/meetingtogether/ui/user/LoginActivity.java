package com.example.meetingtogether.ui.user;

import static com.example.meetingtogether.sharedPreference.SharedPreferenceRepository.pref;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.meetingtogether.MainActivity;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.ActivityLoginBinding;
import com.example.meetingtogether.model.User;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.sharedPreference.SharedPreferenceRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private String TAG = "TEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Shared Preference 초기화
        SharedPreferenceRepository.pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        SharedPreferenceRepository.editor = pref.edit();

        // 사용자 정보 불러오기
        Util.user = SharedPreferenceRepository.getUser();
        if(Util.user != null){
            // 메인 액티비티로 이동
            goMain();
            finish();
            return;
        }

        // 회원 가입
        binding.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = binding.editEmail.getText().toString();
                String password = binding.editPassword.getText().toString();

                // 이메일 입력 필요
                if(Util.isEmpty(email)){
                    Toast.makeText(LoginActivity.this, "이메일을 입력하세요.",Toast.LENGTH_SHORT).show();
                    return;
                }

                // 비밀번호 입력 필요
                if(Util.isEmpty(password)){
                    Toast.makeText(LoginActivity.this, "비밀번호를 입력하세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                String cryptoPassword = Util.convertSHA256(password);

                Util.showDialog(LoginActivity.this, binding.loginConstraintLayout);

                /** 서버로 로그인 요청 */
                Call<CommonRetrofitResponse<User>> call = RetrofitService.getInstance().getService().postLogin(email, cryptoPassword);
                call.enqueue(new Callback<CommonRetrofitResponse<User>>() {
                    @Override
                    public void onResponse(Call<CommonRetrofitResponse<User>> call, Response<CommonRetrofitResponse<User>> response) {
                        // 응답 처리
                        if (response.isSuccessful() && response.body().isResult()) {

                            Util.user = response.body().getData();

                            SharedPreferenceRepository.saveUserForAutoLogin(Util.user);

                            Log.d(TAG, "로그인 완료");
                            Toast.makeText(LoginActivity.this, "로그인 완료 됐습니다.",Toast.LENGTH_SHORT).show();

                            // 메인 액티비티로 이동
                            goMain();

                            finish();
                            return;
                        }

                        Toast.makeText(LoginActivity.this, "로그인에 실패했습니다.",Toast.LENGTH_SHORT).show();
                        Log.d(TAG, response.message());
                        Util.hideDialog();
                    }

                    @Override
                    public void onFailure(Call<CommonRetrofitResponse<User>> call, Throwable t) {
                        // 오류 처리
                        Log.d(TAG, t.getMessage());
                        Util.hideDialog();
                        Toast.makeText(LoginActivity.this, "로그인에 실패했습니다.",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void goMain(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
}