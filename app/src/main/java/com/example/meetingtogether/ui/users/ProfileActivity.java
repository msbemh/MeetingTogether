package com.example.meetingtogether.ui.users;

import static com.example.meetingtogether.common.Common.OTHER_USER_ID;
import static com.example.meetingtogether.common.Common.OTHER_USER_NAME;
import static com.example.meetingtogether.common.Common.ROOMID;
import static com.example.meetingtogether.common.Common.ROOM_TYPE_ID;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.meetingtogether.R;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.ActivityLoginBinding;
import com.example.meetingtogether.databinding.ActivityProfileBinding;
import com.example.meetingtogether.dialogs.CustomDialog;
import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.model.MessageDTO;
import com.example.meetingtogether.model.ProfileMap;
import com.example.meetingtogether.sharedPreference.SharedPreferenceRepository;
import com.example.meetingtogether.ui.chats.ChatRoomActivity;
import com.example.meetingtogether.ui.meetings.MeetingRoomActivity;
import com.example.meetingtogether.ui.user.LoginActivity;

import java.util.List;
import java.util.stream.Collectors;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private AlertDialog.Builder builder;
    private String userId;
    private String friendId;
    private String friendName;
    private int roomId;
    private String profileImagePath;
    private String backgroundImagePath;

    private ProfileMap userProfileMap;
    private ProfileMap backgroundProfileMap;

    /**
     * 수정 결과 받기
     */
    private ActivityResultLauncher editLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            Intent intent = result.getData();

            // 추가
            if(result.getResultCode() == RESULT_OK){
                // 프로필 세팅
                List<ProfileMap> profileMapList = Util.user.getProfileImgPaths();
                if(profileMapList != null) {
                    userProfileMap = profileMapList.stream().filter(profileMap -> profileMap.getType().equals(CustomDialog.Type.PROFILE_IMAGE.name())).findFirst().orElse(null);
                    backgroundProfileMap = profileMapList.stream().filter(profileMap -> profileMap.getType().equals(CustomDialog.Type.BACKGROUND_IMAGE.name())).findFirst().orElse(null);
                }
                // 이름 세팅
                binding.profileName.setText(Util.user.getNickName());

                Util.loadProfile(ProfileActivity.this, binding.profileImage, userProfileMap, CustomDialog.Type.PROFILE_IMAGE);

                // 배경 이미지 로드
                Util.loadProfile(ProfileActivity.this, binding.backgroundImage, backgroundProfileMap, CustomDialog.Type.BACKGROUND_IMAGE);
            // 취소
            }else {

            }
        }
    });

    private ActivityResultLauncher detailLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            Intent intent = result.getData();

            // 삭제
            if(result.getResultCode() == RESULT_OK){
                // 프로필 세팅
                List<ProfileMap> profileMapList = Util.user.getProfileImgPaths();
                if(profileMapList != null) {
                    userProfileMap = profileMapList.stream().filter(profileMap -> profileMap.getType().equals(CustomDialog.Type.PROFILE_IMAGE.name())).findFirst().orElse(null);
                    backgroundProfileMap = profileMapList.stream().filter(profileMap -> profileMap.getType().equals(CustomDialog.Type.BACKGROUND_IMAGE.name())).findFirst().orElse(null);
                }
                // 이름 세팅
                binding.profileName.setText(Util.user.getNickName());

                Util.loadProfile(ProfileActivity.this, binding.profileImage, userProfileMap, CustomDialog.Type.PROFILE_IMAGE);

                // 배경 이미지 로드
                Util.loadProfile(ProfileActivity.this, binding.backgroundImage, backgroundProfileMap, CustomDialog.Type.BACKGROUND_IMAGE);
            // 취소
            }else {

            }
        }
    });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        roomId = intent.getIntExtra("roomId", -1);
        friendId = intent.getStringExtra("friendId");
        friendName = intent.getStringExtra("friendName");

        // 친구의 경우
        if(friendId != null){
            // 프로필 세팅
            Contact friendContact = Util.contactList.stream().filter(contact -> contact.getFriendId().equals(friendId)).findAny().orElse(null);
            if(friendContact != null) {
                List<ProfileMap> profileMapList = friendContact.getFriendImgPaths();
                if(profileMapList != null){
                    userProfileMap = profileMapList.stream().filter(profileMap -> {
                        if(profileMap == null || profileMap.getType() == null) return false;
                        return profileMap.getType().equals(CustomDialog.Type.PROFILE_IMAGE.name());
                    }).findFirst().orElse(null);

                    backgroundProfileMap = profileMapList.stream().filter(profileMap -> {
                        if(profileMap == null || profileMap.getType() == null) return false;
                        return profileMap.getType().equals(CustomDialog.Type.BACKGROUND_IMAGE.name());
                    }).findFirst().orElse(null);
                }
                // 이름 세팅
                binding.profileName.setText(friendContact.getFriendName());
            }
            // 로그아웃 가리기
            binding.logoutBtn.setVisibility(View.INVISIBLE);
            // 수정버튼 가리기
            binding.editContainer.setVisibility(View.GONE);
        // 사용자 본인의 경우
        }else if(userId != null){
            // 프로필 세팅
            List<ProfileMap> profileMapList = Util.user.getProfileImgPaths();
            if(profileMapList != null) {
                userProfileMap = profileMapList.stream().filter(profileMap -> {
                    if(profileMap == null || profileMap.getType() == null) return false;
                    return profileMap.getType().equals(CustomDialog.Type.PROFILE_IMAGE.name());
                }).findFirst().orElse(null);

                backgroundProfileMap = profileMapList.stream().filter(profileMap -> {
                    if(profileMap == null || profileMap.getType() == null) return false;
                    return profileMap.getType().equals(CustomDialog.Type.BACKGROUND_IMAGE.name());
                }).findFirst().orElse(null);
            }
            // 이름 세팅
            binding.profileName.setText(Util.user.getNickName());

            // 로그아웃 보이기
            binding.logoutBtn.setVisibility(View.VISIBLE);
            // 수정 버튼 보이기
            binding.editContainer.setVisibility(View.VISIBLE);
        }

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
                    // 그냥 모든 정보 초기화
                    SharedPreferenceRepository.clear();
//                    SharedPreferenceRepository.deleteUserForAutoLogin();

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

        binding.editContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
                if(userId != null){
                    intent.putExtra("userId", userId);
                }else if(friendId != null){
                    intent.putExtra("friendId", friendId);
                }


                editLauncher.launch(intent);
            }
        });

        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, ProfileDetailActivity.class);
                if(userId != null){
                    intent.putExtra("userId", userId);
                }else if(friendId != null){
                    intent.putExtra("friendId", friendId);
                }

                intent.putExtra("type", CustomDialog.Type.PROFILE_IMAGE.name());
                detailLauncher.launch(intent);
            }
        });

        binding.backgroundImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, ProfileDetailActivity.class);
                if(userId != null){
                    intent.putExtra("userId", userId);
                }else if(friendId != null){
                    intent.putExtra("friendId", friendId);
                }

                intent.putExtra("type", CustomDialog.Type.BACKGROUND_IMAGE.name());
                detailLauncher.launch(intent);
            }
        });

        binding.chatContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, ChatRoomActivity.class);

                // 해당 방 번호, 방 유형, 상대방 Id 를 넘겨준다.
                intent.putExtra(ROOMID, roomId);
                intent.putExtra(ROOM_TYPE_ID, MessageDTO.RoomType.INDIVIDUAL.name());
                intent.putExtra(OTHER_USER_ID, friendId);
                intent.putExtra(OTHER_USER_NAME, friendName);

                startActivity(intent);
            }
        });


        // 프로필 이미지 로드
//        ProfileMap profileMap = Util.user.getMyProfileMap();
        Util.loadProfile(this, binding.profileImage, userProfileMap, CustomDialog.Type.PROFILE_IMAGE);

        // 배경 이미지 로드
//        ProfileMap backgroundProfileMap = Util.user.getMyBackgroundMap();
        Util.loadProfile(this, binding.backgroundImage, backgroundProfileMap, CustomDialog.Type.BACKGROUND_IMAGE);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}