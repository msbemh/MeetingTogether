package com.example.meetingtogether.ui.meetings;

import static com.example.meetingtogether.MainActivity.TAG;
import static com.example.meetingtogether.common.Common.HOST;
import static com.example.meetingtogether.common.Common.IS_ACTIVATE_CAMERA;
import static com.example.meetingtogether.common.Common.ROOMID;
import static com.example.meetingtogether.common.Common.ROOM_NAME;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.meetingtogether.R;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.ActivityChatRoomBinding;
import com.example.meetingtogether.databinding.ActivityNewMeetingBinding;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.services.ChatService;
import com.example.meetingtogether.ui.meetings.DTO.MeetingDTO;
import com.example.meetingtogether.ui.user.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewMeetingActivity extends AppCompatActivity {

    private ActivityNewMeetingBinding binding;
    private boolean isActivateCamera = false;
    private MeetingDTO.TYPE type = MeetingDTO.TYPE.PUBLIC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewMeetingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        isActivateCamera = false;
        binding.videoSwitch.setText("비디오 꺼짐");
        binding.videoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    isActivateCamera = true;
                    binding.videoSwitch.setText("비디오 켜짐");
                }else{
                    isActivateCamera = false;
                    binding.videoSwitch.setText("비디오 꺼짐");
                }
            }
        });

        binding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 선택된 라디오 버튼 처리
                switch (checkedId) {
                    case R.id.radioPublic:
                        type = MeetingDTO.TYPE.PUBLIC;
                        break;
                    case R.id.radioPrivate:
                        type = MeetingDTO.TYPE.PRIVATE;
                        break;
                }
            }
        });

        binding.meetingStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = binding.meetingTitleEdit.getText().toString();
                String password = binding.editTextTextPassword.getText().toString();

                if("".equals(title) || title == null){
                    Toast.makeText(NewMeetingActivity.this, "제목을 입력하세요",Toast.LENGTH_LONG).show();
                    return;
                }

                MeetingDTO meetingDTO = new MeetingDTO();
                meetingDTO.setTitle(title);
                meetingDTO.setPassword(password);
                meetingDTO.setType(type);

                Call<CommonRetrofitResponse> call = RetrofitService.getInstance().getService().postCreateMeeting(meetingDTO);
                call.enqueue(new Callback<CommonRetrofitResponse>() {
                    @Override
                    public void onResponse(Call<CommonRetrofitResponse> call, Response<CommonRetrofitResponse> response) {
                        Log.d(TAG, "성공");
                        CommonRetrofitResponse commonRetrofitResponse = response.body();
                        boolean result = commonRetrofitResponse.isResult();

                        if(result){
                            int meetingId =  Integer.parseInt(commonRetrofitResponse.getData().toString());
                            Log.d(TAG, "meetingId:" + meetingId);

                            Intent intent = new Intent(NewMeetingActivity.this, MeetingRoomActivity.class);
                            intent.putExtra(ROOMID, meetingId);
                            intent.putExtra(ROOM_NAME, title);
                            intent.putExtra(IS_ACTIVATE_CAMERA, isActivateCamera);
                            intent.putExtra(HOST, Util.user.getId());

                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(NewMeetingActivity.this, "미팅 회의 방 생성에 실패했습니다.",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CommonRetrofitResponse> call, Throwable t) {
                        // 오류 처리
                        Log.d(TAG, t.getMessage());
                    }
                });
            }
        });
    }
}