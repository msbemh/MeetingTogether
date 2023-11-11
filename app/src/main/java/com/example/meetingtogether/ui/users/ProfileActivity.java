package com.example.meetingtogether.ui.users;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.meetingtogether.R;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.ActivityLoginBinding;
import com.example.meetingtogether.databinding.ActivityProfileBinding;
import com.example.meetingtogether.sharedPreference.SharedPreferenceRepository;
import com.example.meetingtogether.ui.user.LoginActivity;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private String TAG = "TEST";
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.profileName.setText(Util.user.getNickName());


        builder = new AlertDialog.Builder(this);
        builder
            .setTitle("로그아웃")
            .setIcon(R.drawable.metting_together_logo)
            .setMessage("정말 로그아웃 하시겠습니까?")
            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // 앱에서 관리하는 사용자 정보 초기화
                    Util.user = null;

                    // 구글로 로그인 했을 경우, 로그인 정보 초기화
//                    GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), LoginActivity.gso);
//                    mGoogleSignInClient.signOut();

                    // Shared에 자동로그인을 위해서 저장한 정보 삭제
                    SharedPreferenceRepository.deleteUserForAutoLogin();

                    /**
                     * 영어 단어가 이미 존재하거나
                     * 새롭게 생성이 완료된 경우
                     * MainActivity 로 이동 한다
                     */
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    /**
                     * FLAG_ACTIVITY_CLEAR_TASK : 현재 스택에 있는 모든 액티비티들을 모두 제거
                     * FLAG_ACTIVITY_NEW_TASK : 새로운 Task를 시작 합니다
                     */
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            })
            .setNegativeButton("취소", null);

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.show();
            }
        });

        binding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}