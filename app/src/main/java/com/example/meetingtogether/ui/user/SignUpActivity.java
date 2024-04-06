package com.example.meetingtogether.ui.user;

import static com.example.meetingtogether.MainActivity.TAG;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.ActivitySignUpBinding;
import com.example.meetingtogether.model.User;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.ui.meetings.MeetingRoomActivity;
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    private boolean isCompleteDupCheck = false;
    private boolean isAuthNumCheck = false;
    private String emailAuthNumber;
    private Thread otpValidThread;
    private Object object = new Object();

    /**
     * 필요한 권한
     */
    private final String[] PERMISSIONS = {
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.READ_PHONE_STATE
    };

    /**
     * 권한 요청에 대한 Callback
     */
    private ActivityResultLauncher permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            Log.d(TAG, "" + result.toString());

            Boolean allGranted = true;

            // 모든 권한에 동의 했는지 확인
            // 하나라도 false가 존재한다면 allGranted가 false 됨
            Iterator iterator = result.keySet().iterator();
            while (iterator.hasNext()) {
                String permissionName = iterator.next().toString();
                
                boolean isAllowed = result.get(permissionName);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                    allGranted = allGranted && isAllowed;
                } else {
                    allGranted = false;
                }
            }

            // 모든 권한에 동의
            if (allGranted) {
            // 모든 권한에 동의 하지 않음
            } else {
                showPermissionDialog();
            }
        }
    });

    private void showPermissionDialog() {
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
        localBuilder.setTitle("권한 설정")
                .setMessage("권한 거절로 인해 일부기능이 제한됩니다.")
                .setPositiveButton("권한 설정하러 가기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, e.toString());
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                            startActivity(intent);
                        }
                    }
                })
                .setNegativeButton("취소하기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                        Toast.makeText(SignUpActivity.this, "권한을 취소하셨습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .create()
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // OTP 요청
        binding.authRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = binding.editEmail.getText().toString();

                // 이메일 입력 필요
                if(Util.isEmpty(userEmail)){
                    Toast.makeText(SignUpActivity.this, "이메일을 입력하세요.",Toast.LENGTH_SHORT).show();
                    return;
                }

                // 이메일 형식 검사
                boolean isValid = Util.isEmailValid(userEmail);
                if(!isValid){
                    Toast.makeText(SignUpActivity.this, "올바르지 않은 이메일 형식입니다.",Toast.LENGTH_SHORT).show();
                    return;
                }

                // 이메일 중복 검사
                if(!isCompleteDupCheck){
                    Toast.makeText(SignUpActivity.this, "이메일 중복 검사를 해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                /** 서버로 OTP 요청 */
                String phoneNum = Util.getPhoneNum();
                Call<CommonRetrofitResponse> call = RetrofitService.getInstance().getService().postReqOtp(phoneNum);
                call.enqueue(new Callback<CommonRetrofitResponse>() {
                    @Override
                    public void onResponse(Call<CommonRetrofitResponse> call, Response<CommonRetrofitResponse> response) {
                        // 응답 처리
                        if (response.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "OTP 요청 완료", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "OTP 요청 완료");

                            if(otpValidThread != null) otpValidThread.interrupt();

                            // 유효시간 카운트 시작
                            otpValidThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    // 현재시간에 3분 더하기
                                    Date date = new Date();
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(date);
                                    cal.add(Calendar.MINUTE, 3);
                                    Date validDate = new Date(cal.getTimeInMillis());

                                    while(Thread.currentThread().isInterrupted()){
                                        try {
                                            synchronized (object){
                                                // 시간초과
                                                int result = validDate.compareTo(new Date());
                                                if(result >= 0) break;

                                                // 얼마나 남았는지 계산
                                                String timeBeforeNow = Util.calcDateBeforeNowAsMinSec2(validDate);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        binding.validDate.setText(timeBeforeNow);
                                                    }
                                                });

                                                Thread.sleep(100);
                                            }
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                            Log.e(TAG, e.getMessage());
                                        }
                                    }
                                }
                            });
                            otpValidThread.start();

                        }
                        Log.d(TAG, response.message());
                    }

                    @Override
                    public void onFailure(Call<CommonRetrofitResponse> call, Throwable t) {
                        // 오류 처리
                        Log.d(TAG, t.getMessage());
                    }
                });
            }
        });

        // 취소 버튼
        binding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 이메일 변경시, 중복 체크 풀기
        binding.editEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 중복 체크 완료 풀기
                isCompleteDupCheck = false;
                binding.editEmail.setTextColor(Color.BLACK);
            }
        });

        // 중복 체크 클릭
        binding.emailDuplicationCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.editEmail.getText().toString();

                // 이메일 입력 필요
                if(Util.isEmpty(email)){
                    Toast.makeText(SignUpActivity.this, "이메일을 입력하세요.",Toast.LENGTH_SHORT).show();
                    return;
                }

                // 이메일 형식 검사
                boolean isValid = Util.isEmailValid(email);
                if(!isValid){
                    Toast.makeText(SignUpActivity.this, "올바르지 않은 이메일 형식입니다.",Toast.LENGTH_SHORT).show();
                    return;
                }

                /** 이메일 중복 검사 */
                Call<CommonRetrofitResponse> call = RetrofitService.getInstance().getService().postEmailChk(email);
                call.enqueue(new Callback<CommonRetrofitResponse>() {
                    @Override
                    public void onResponse(Call<CommonRetrofitResponse> call, Response<CommonRetrofitResponse> response) {
                        // 응답 처리
                        if (response.isSuccessful()) {
                            if(response.body().isResult()){
                                Toast.makeText(SignUpActivity.this, "사용할 수 있는 이메일 입니다.",Toast.LENGTH_SHORT).show();
                                isCompleteDupCheck = true;
                                binding.editEmail.setTextColor(Color.GREEN);
                            }else{
                                Toast.makeText(SignUpActivity.this, "중복된 이메일 입니다.",Toast.LENGTH_SHORT).show();
                                isCompleteDupCheck = false;
                                binding.editEmail.setTextColor(Color.RED);
                            }
                            Util.hideDialog();
                            return;
                        }

                        Toast.makeText(SignUpActivity.this, "요청에 실패했습니다.",Toast.LENGTH_SHORT).show();
                        Log.d(TAG, response.message());
                        Util.hideDialog();
                    }
                    @Override
                    public void onFailure(Call<CommonRetrofitResponse> call, Throwable t) {
                        // 오류 처리
                        Log.d(TAG, t.getMessage());
                        Util.hideDialog();
                        Toast.makeText(SignUpActivity.this, "요청에 실패했습니다.",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        // 회원 가입 클릭
        binding.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.editEmail.getText().toString();
                String name = binding.editName.getText().toString();
                String password = binding.editPassword.getText().toString();
                String passwordCheck = binding.editPasswordCheck.getText().toString();

                // 이메일 입력 필요
                if(Util.isEmpty(email)){
                    Toast.makeText(SignUpActivity.this, "이메일을 입력하세요.",Toast.LENGTH_SHORT).show();
                    return;
                }

                // 이메일 형식 검사
                boolean isValid = Util.isEmailValid(email);
                if(!isValid){
                    Toast.makeText(SignUpActivity.this, "올바르지 않은 이메일 형식입니다.",Toast.LENGTH_SHORT).show();
                    return;
                }

                // 이메일 중복 검사
                if(!isCompleteDupCheck){
                    Toast.makeText(SignUpActivity.this, "이메일 중복 검사를 해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                // 비밀번호 입력 필요
                if(Util.isEmpty(password)){
                    Toast.makeText(SignUpActivity.this, "비밀번호를 입력하세요",Toast.LENGTH_SHORT).show();
                    return;
                }
                // 비밀번호 확인 입력 필요
                if(Util.isEmpty(passwordCheck)){
                    Toast.makeText(SignUpActivity.this, "비밀번호 확인을 입력하세요",Toast.LENGTH_SHORT).show();
                    return;
                }
                // 비밀번호 일치 여부 확인
                if(!password.equals(passwordCheck)){
                    Toast.makeText(SignUpActivity.this, "비밀번호가 일치하지 않습니다",Toast.LENGTH_SHORT).show();
                    return;
                }
                // 닉네임 입력 여부 확인
                if(Util.isEmpty(name)){
                    Toast.makeText(SignUpActivity.this, "닉네임을 입력해 주세요",Toast.LENGTH_SHORT).show();
                    return;
                }

//                String editEmailAuthNum = binding.authNumEdit.getText().toString();
//                // 이메일 인증 번호 일치 여부 확인
//                if(emailAuthNumber == null || !emailAuthNumber.equals(editEmailAuthNum)){
//                    Toast.makeText(SignUpActivity.this, "인증번호가 맞지 않습니다",Toast.LENGTH_SHORT).show();
//                    return;
//                }

                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
                    Toast.makeText(SignUpActivity.this, "버전이 맞지 않습니다",Toast.LENGTH_SHORT).show();
                    return;
                }

                String cryptoPassword = Util.convertSHA256(password);
                Log.d(TAG, "cryptoPassword: " + cryptoPassword);

//                String cryptoPassword = cryptoInfo[0];
//                String salt = cryptoInfo[1];

                String phoneNum = Util.getPhoneNum();

                User user = new User(email, cryptoPassword, name, phoneNum);
                Util.showDialog(SignUpActivity.this, binding.signUpConstraintLayout);

                /** 서버로 User Insert 요청 */
                Call<CommonRetrofitResponse> call = RetrofitService.getInstance().getService().postSignUp(user);
                call.enqueue(new Callback<CommonRetrofitResponse>() {
                    @Override
                    public void onResponse(Call<CommonRetrofitResponse> call, Response<CommonRetrofitResponse> response) {
                        // 응답 처리
                        if (response.isSuccessful() && response.body().isResult()) {
                            Log.d(TAG, "회원가입 완료");
                            Toast.makeText(SignUpActivity.this, "회원가입 완료",Toast.LENGTH_SHORT).show();
                            Util.hideDialog();
                            finish();
                            return;
                        }

                        Toast.makeText(SignUpActivity.this, "회원가입에 실패했습니다",Toast.LENGTH_SHORT).show();
                        Log.d(TAG, response.message());
                        Util.hideDialog();
                    }
                    @Override
                    public void onFailure(Call<CommonRetrofitResponse> call, Throwable t) {
                        // 오류 처리
                        Log.d(TAG, t.getMessage());
                        Util.hideDialog();
                        Toast.makeText(SignUpActivity.this, "회원가입에 실패했습니다",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        permissionLauncher.launch(PERMISSIONS);
    }

//    private String getPhoneNum() {
//        String phoneNum = "";
//        TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "전화번호 읽기 권한이 필요합니다.ㅣ",Toast.LENGTH_SHORT).show();
//            return null;
//        }
//
//        // 전화번호 정보 가져오기
//        phoneNum = telManager.getLine1Number();
//        if(phoneNum.startsWith("+82")) {
//            phoneNum = phoneNum.replace("+82", "0");
//        }
//        return phoneNum;
//    }
}