package com.example.meetingtogether.ui.meetings.fragments;

import static com.example.meetingtogether.common.Common.HOST;
import static com.example.meetingtogether.common.Common.IS_ACTIVATE_CAMERA;
import static com.example.meetingtogether.common.Common.ROOMID;
import static com.example.meetingtogether.common.Common.ROOM_NAME;
import static com.example.meetingtogether.sharedPreference.SharedPreferenceRepository.gson;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.meetingtogether.R;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.FragmentMeetinglistBinding;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.ui.chats.ChatImgViewActivity;
import com.example.meetingtogether.ui.chats.ChatRoomActivity;
import com.example.meetingtogether.ui.meetings.DTO.MeetingDTO;
import com.example.meetingtogether.ui.meetings.MeetingRoomActivity;
import com.example.meetingtogether.ui.meetings.NewMeetingActivity;
import com.example.meetingtogether.ui.meetings.ParticipateMeetingActivity;
import com.example.meetingtogether.ui.user.LoginActivity;
import com.example.meetingtogether.ui.user.SignUpActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeetingListFragment extends Fragment {

    private FragmentMeetinglistBinding binding;

    private MeetingListFragment.MyPagerAdapter mAdapter;
    private ViewPager2 mViewPager;
    private TabLayout mTabLayout;
    private List<Fragment> fragmentList = new ArrayList<>();

    private static final String TAG = "TEST";
    private String title = "public";
    private List<MeetingDTO> meetingDTOList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMeetinglistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        /**
         * ViewPager 에 어댑터 세팅
         */
        mViewPager = binding.viewPager;
        mAdapter = new MeetingListFragment.MyPagerAdapter(this);
        mViewPager.setAdapter(mAdapter);

        /**
         * TabLayout 을 ViewPager 와 연결
         */
        mTabLayout = binding.tabLayout;
        new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
            switch(position){
                case  0:
                    title = "public";
                    break;
                case 1:
                    title = "private";
                    break;
                case 2:
                    title = "reserve";
                    break;
            }
            tab.setText(title);
        }).attach();

        // Tab 클릭 이벤트 처리
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                getMeetingList();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 탭이 선택 해제될 때 실행할 코드 추가
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 이미 선택된 탭을 다시 선택할 때 실행할 코드 추가
            }
        });

        /**
         * 새 회의
         */
        binding.newMeetingContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NewMeetingActivity.class);
                startActivity(intent);
            }
        });

        /**
         * 참가
         */
        binding.participateContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1. 방번호 입력 팝업 창 띄워주기

                // 2. 로컬 데이터에 해당 방이 존재 한다면 방 진입

                // 3. 해당 방에 비밀번호가 존재하는지 체크
                // 비밀번호가 존재한다면, 비밀번호 입력 팝업창 띄우기 => 비밀번호가 맞으면 "방 참가전 미리보기 화면" 으로 이동
                // 비밀번호가 존재하지 않는다면, 바로 "방 참가전 미리보기 화면" 으로 이동

            }
        });

        /**
         * 예약
         */
        binding.reserveContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        getMeetingList();
    }

//    private void update(){
//        if (mViewPager.getCurrentItem() == 0) {
//            PublicFragment publicFragment = (PublicFragment) fragmentList.get(mViewPager.getCurrentItem());
//            meetingDTOList = meetingDTOList.stream().filter(meetingDTO -> meetingDTO.getType() == MeetingDTO.TYPE.PUBLIC).collect(Collectors.toList());
//            publicFragment.update(meetingDTOList);
//        } else if (mViewPager.getCurrentItem() == 1) {
//            PrivateFragment privateFragment = (PrivateFragment) fragmentList.get(mViewPager.getCurrentItem());
//            meetingDTOList = meetingDTOList.stream().filter(meetingDTO -> meetingDTO.getType() == MeetingDTO.TYPE.PRIVATE).collect(Collectors.toList());
//            privateFragment.update(meetingDTOList);
//        } else if (mViewPager.getCurrentItem() == 2) {
//            ReserveFragment reserveFragment = (ReserveFragment) fragmentList.get(mViewPager.getCurrentItem());
//        }
//    }

    private void getMeetingList(){
        Call<List<MeetingDTO>> call = RetrofitService.getInstance().getService().getMeetingRoomList();
        call.enqueue(new Callback<List<MeetingDTO>>() {
            @Override
            public void onResponse(Call<List<MeetingDTO>> call, Response<List<MeetingDTO>> response) {
                meetingDTOList = response.body();

                if(fragmentList.size() == 0) return;

                if (mViewPager.getCurrentItem() == 0) {
                    PublicFragment publicFragment = (PublicFragment) fragmentList.get(mViewPager.getCurrentItem());
                    meetingDTOList = meetingDTOList.stream().filter(meetingDTO -> meetingDTO.getType() == MeetingDTO.TYPE.PUBLIC).collect(Collectors.toList());
                    publicFragment.update(meetingDTOList);
                } else if (mViewPager.getCurrentItem() == 1) {
                    PrivateFragment privateFragment = (PrivateFragment) fragmentList.get(mViewPager.getCurrentItem());
                    meetingDTOList = meetingDTOList.stream().filter(meetingDTO -> meetingDTO.getType() == MeetingDTO.TYPE.PRIVATE).collect(Collectors.toList());
                    privateFragment.update(meetingDTOList);
                } else if (mViewPager.getCurrentItem() == 2) {
                    ReserveFragment reserveFragment = (ReserveFragment) fragmentList.get(mViewPager.getCurrentItem());
                }
            }

            @Override
            public void onFailure(Call<List<MeetingDTO>> call, Throwable t) {
                // 오류 처리
                Log.d(TAG, t.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Pager Adapter Class 선언
     */
    public class MyPagerAdapter extends FragmentStateAdapter {
        private static final int NUM_PAGES = 3;

        public MyPagerAdapter(Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment = null;
            switch(position){
                case 0 :
                    fragment = new PublicFragment();
                    break;
                case 1 :
                    fragment = new PrivateFragment();
                    break;
                case 2 :
                    fragment = new ReserveFragment();
                    break;
            }

            fragmentList.add(fragment);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }

    }

    /**
     * 회의 방으로 진입
     */
    public void connectToRoom(MeetingDTO meetingDTO, boolean is_activate_camera) {
        int roomId = meetingDTO.getId();
        String roomName = meetingDTO.getTitle();
        String password = meetingDTO.getPassword();
        meetingDTO.setActivateCamera(is_activate_camera);

        Call<MeetingDTO> call = RetrofitService.getInstance().getService().checkExistRoom(roomId);
        call.enqueue(new Callback<MeetingDTO>() {
            @Override
            public void onResponse(Call<MeetingDTO> call, Response<MeetingDTO> response) {
                MeetingDTO dMeetingDTO = response.body();
                int roomId = dMeetingDTO.getId();
                int maxClient = dMeetingDTO.getMaxClient();
                int currentClient = dMeetingDTO.getCurrentClient();

                boolean isExistRoom = false;
                if(roomId != -1){
                    isExistRoom = true;
                }

                if(currentClient >= maxClient){
                    Toast.makeText(getActivity(), "해당 방은 이미 가득 찼습니다.",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(isExistRoom){
                    /** 비밀번호가 존재할 경우 맞춰야 한다. */
                    if(!"".equals(password) && password != null){
                        showPasswordAlert(meetingDTO, is_activate_camera);
                        return;
                    }

                    /** 회의 참가 전 화면 이동 */
                    Intent intent = new Intent(getActivity(), ParticipateMeetingActivity.class);
                    String jsonString = gson.toJson(dMeetingDTO);
                    intent.putExtra("jsonString", jsonString);
                    startActivity(intent);
                }else{
                    Toast.makeText(getActivity(), "해당 방은 존재하지 않습니다.",Toast.LENGTH_SHORT).show();
                    getMeetingList();
                }
            }

            @Override
            public void onFailure(Call<MeetingDTO> call, Throwable t) {
                // 오류 처리
                Log.d(TAG, t.getMessage());
            }
        });
    }
    private void showPasswordAlert(MeetingDTO meetingDTO, boolean is_activate_camera){
        // AlertDialog를 사용하는 예제
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("비밀번호 입력");

        // EditText를 추가하여 입력을 받을 수 있도록 설정
        final EditText input = new EditText(getActivity());
        input.setHint("회의방 비밀번호를 입력하세요.");
        builder.setView(input);

        // 확인 버튼 설정
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String meetingPassword = meetingDTO.getPassword();

                // 입력값 처리
                String inputValue = input.getText().toString();
                if(meetingPassword.equals(inputValue)){
                    /** 회의 참가 전 화면 이동 */
                    Intent intent = new Intent(getActivity(), ParticipateMeetingActivity.class);
                    String jsonString = gson.toJson(meetingDTO);
                    intent.putExtra("jsonString", jsonString);
                    startActivity(intent);
                }else{
                    Toast.makeText(getActivity(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // 취소 버튼 설정
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // AlertDialog 보이기
        builder.show();
    }
}