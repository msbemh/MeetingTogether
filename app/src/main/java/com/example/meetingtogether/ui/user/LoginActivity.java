package com.example.meetingtogether.ui.user;

import static com.example.meetingtogether.MainActivity.TAG;
import static com.example.meetingtogether.common.Common.IS_ACTIVATE_CAMERA;
import static com.example.meetingtogether.common.Common.ROOMID;
import static com.example.meetingtogether.common.Common.ROOM_NAME;
import static com.example.meetingtogether.dialogs.CustomDialog.Type.PROFILE_IMAGE;
import static com.example.meetingtogether.sharedPreference.SharedPreferenceRepository.gson;
import static com.example.meetingtogether.sharedPreference.SharedPreferenceRepository.pref;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;
import com.appsflyer.deeplink.DeepLink;
import com.appsflyer.deeplink.DeepLinkListener;
import com.appsflyer.deeplink.DeepLinkResult;
import com.example.meetingtogether.MainActivity;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.ActivityLoginBinding;
import com.example.meetingtogether.dialogs.CustomDialog;
import com.example.meetingtogether.model.ProfileMap;
import com.example.meetingtogether.model.User;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.sharedPreference.SharedPreferenceRepository;
import com.example.meetingtogether.ui.meetings.DTO.MeetingDTO;
import com.example.meetingtogether.ui.meetings.MeetingRoomActivity;
import com.example.meetingtogether.ui.meetings.ParticipateMeetingActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private boolean deferred_deep_link_processed_flag = false;
    private final String YOUR_DEV_KEY = "F53Xt4VRqj3Nn9s27KD3eW";
    private String GOOGLE_LOGIN_TAG = "GOOGLE_LOGIN_TAG";
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO : 임시 테스트
//        Util.user = null;

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /**
         * Google Login API 설정
         */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Shared Preference 초기화
        if(SharedPreferenceRepository.gson == null){
            SharedPreferenceRepository.init();
        }
        if(SharedPreferenceRepository.pref == null){
            SharedPreferenceRepository.pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
            SharedPreferenceRepository.editor = pref.edit();
        }


        // 사용자 정보 불러오기
        Util.user = SharedPreferenceRepository.getUser();

        /**
         * 로그인 액티비티를 파라미터가 있는 상태로 진입했다는건 Deep Link로 접근했다 간주하고
         * 미팅 화면으로 리디렉트 시킨다.
         */
        Intent intent = getIntent();
        Uri data = intent.getData();

        if(Util.user != null){
            // 기본적인 접근
            // 메인 액티비티로 이동
            if (data == null) {
                goMain();
                finish();
                return;
            // Deep Link 접근
            }else{
                // 딥 링크에서 전달된 데이터를 처리
                String path = data.getPath(); // 경로 정보 가져오기
                Log.d(TAG, "path:" + path);
                // 추가적인 처리 로직 작성
                // URI에서 파라미터 추출
                int roomId = Integer.valueOf(data.getQueryParameter("roomId"));
                String roomName = data.getQueryParameter("roomName");
                Log.d(TAG, "roomId:" + roomId);
                Log.d(TAG, "roomName:" + roomName);
                goMeetingRoom(roomId, roomName);
                finish();
                return;
            }
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

        binding.googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this, "테스트", Toast.LENGTH_SHORT);
                signIn();
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

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null){
//            Toast.makeText(LoginActivity.this, "이미 구글 로그인 된 상태입니다.",Toast.LENGTH_SHORT).show();
        }
    }

    private void goMain(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void goMeetingRoom(int roomId, String roomName){
        // 서버에서 해당 방번호로 방 존재하는지 확인 및 정보 가져오기
        Call<MeetingDTO> call = RetrofitService.getInstance().getService().checkExistRoom(roomId);
        call.enqueue(new Callback<MeetingDTO>() {
            @Override
            public void onResponse(Call<MeetingDTO> call, Response<MeetingDTO> response) {
                MeetingDTO dMeetingDTO = response.body();
                int roomId = dMeetingDTO.getId();
                int maxClient = dMeetingDTO.getMaxClient();
                int currentClient = dMeetingDTO.getCurrentClient();
                String roomPassword = dMeetingDTO.getPassword();

                boolean isExistRoom = false;
                if(roomId != -1){
                    isExistRoom = true;
                }

                if(currentClient >= maxClient){
                    Toast.makeText(LoginActivity.this, "해당 방은 이미 가득 찼습니다.",Toast.LENGTH_SHORT).show();
                    goMain();
                    return;
                }

                if(isExistRoom){
                    Intent intent = new Intent(LoginActivity.this, MeetingRoomActivity.class);
                    intent.putExtra(ROOMID, roomId);
                    intent.putExtra(ROOM_NAME, roomName);
                    intent.putExtra(IS_ACTIVATE_CAMERA, true);
                    startActivity(intent);
                }else{
                    Toast.makeText(LoginActivity.this, "해당 방은 존재하지 않습니다.",Toast.LENGTH_SHORT).show();
                    goMain();
                }
            }

            @Override
            public void onFailure(Call<MeetingDTO> call, Throwable t) {
                // 오류 처리
                Log.d(TAG, t.getMessage());
                Toast.makeText(LoginActivity.this, "해당 방은 존재하지 않습니다.",Toast.LENGTH_SHORT).show();
                goMain();
            }
        });
    }

    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 1; /* unique request id */
    private static final int RC_SIGN_IN = 2;

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(GOOGLE_LOGIN_TAG, "account" + account);

            if (account != null) {
                String personName = account.getDisplayName();
                String personGivenName = account.getGivenName();
                String personFamilyName = account.getFamilyName();
                String personEmail = account.getEmail();
                String personId = account.getId();
                Uri personPhoto = account.getPhotoUrl();
                String phoneNum = Util.getPhoneNum();

                User user = new User(personEmail, null, personName, phoneNum);
                user.setName(personName);
                if(personPhoto != null){
                    List<ProfileMap> profileImgPaths = new ArrayList<>();
                    ProfileMap profileMap = new ProfileMap("https://" +personPhoto.getHost() + personPhoto.getPath(), CustomDialog.Type.PROFILE_IMAGE.name());
                    profileImgPaths.add(profileMap);
                    user.setProfileImgPaths(profileImgPaths);
                }

                /** 서버로 Google User Insert 요청. 만약 이미 있으면 그냥 패스 */
                Call<CommonRetrofitResponse> call = RetrofitService.getInstance().getService().postSignUp(user);
                call.enqueue(new Callback<CommonRetrofitResponse>() {
                    @Override
                    public void onResponse(Call<CommonRetrofitResponse> call, Response<CommonRetrofitResponse> response) {
                        // 응답 처리
                        if (response.isSuccessful() && response.body().isResult()) {
                            Log.d(GOOGLE_LOGIN_TAG, "구글 로그인에 성공했습니다.");

                            String jwt = (String) response.body().getData();
                            Util.user = user;
                            Util.user.setJwt(jwt);

                            SharedPreferenceRepository.saveUserForAutoLogin(Util.user);

                            Toast.makeText(LoginActivity.this, "구글 로그인에 성공했습니다.",Toast.LENGTH_SHORT).show();
                            goMain();
                            finish();
                            return;
                        }

                        Toast.makeText(LoginActivity.this, "구글 로그인에 실패 했습니다",Toast.LENGTH_SHORT).show();
                        Log.d(GOOGLE_LOGIN_TAG, response.message());
                    }
                    @Override
                    public void onFailure(Call<CommonRetrofitResponse> call, Throwable t) {
                        // 오류 처리
                        Log.d(GOOGLE_LOGIN_TAG, t.getMessage());
                        Toast.makeText(LoginActivity.this, "구글 로그인에 실패 했습니다.",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (ApiException e) {
            Log.w(GOOGLE_LOGIN_TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

}