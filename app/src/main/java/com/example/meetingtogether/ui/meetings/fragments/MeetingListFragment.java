package com.example.meetingtogether.ui.meetings.fragments;

import static android.app.Activity.RESULT_OK;
import static com.example.meetingtogether.MainActivity.TAG;
import static com.example.meetingtogether.MainActivity.TAG_LIFE;
import static com.example.meetingtogether.common.Common.HOST;
import static com.example.meetingtogether.common.Common.IS_ACTIVATE_CAMERA;
import static com.example.meetingtogether.common.Common.ROOMID;
import static com.example.meetingtogether.common.Common.ROOM_NAME;
import static com.example.meetingtogether.sharedPreference.SharedPreferenceRepository.gson;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.meetingtogether.MainActivity;
import com.example.meetingtogether.R;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.FragmentMeetinglistBinding;
import com.example.meetingtogether.model.MessageDTO;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.ui.chats.ChatImgViewActivity;
import com.example.meetingtogether.ui.chats.ChatRoomActivity;
import com.example.meetingtogether.ui.meetings.DTO.MeetingDTO;
import com.example.meetingtogether.ui.meetings.MeetingRoomActivity;
import com.example.meetingtogether.ui.meetings.NewMeetingActivity;
import com.example.meetingtogether.ui.meetings.NewReserveMeetingActivity;
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
//    public static List<Fragment> fragmentList = new ArrayList<>();

    private String title = "public";
    private List<MeetingDTO> meetingDTOList;
    private Handler handler;


    /**
     * 채팅 예약방 생성 완료 콜백
     */
    private ActivityResultLauncher reserveCompleteLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            // 추가
            if(result.getResultCode() == RESULT_OK){
                MessageDTO messageDTO = new MessageDTO();
                messageDTO.setType(MessageDTO.RequestType.NOTIFY_MEETING_RESERVE_CREATED);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            Thread.sleep(1000);
                            if(MainActivity.mChatService != null) MainActivity.mChatService.sendMsg(messageDTO);
                        }catch (Exception e){
                            Log.d(TAG, "예약 회의방이 생성이 완료되어 소켓서버로 데이터를 송신 중 에러가 발생헀습니다. e:" + e.getMessage());
                        }
                    }
                });
            // 취소
            }else {

            }
        }
    });


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG_LIFE, getClass().getSimpleName() + " onCreate");
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG_LIFE, getClass().getSimpleName() + " onCreateView");

        binding = FragmentMeetinglistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        HandlerThread handlerChatThread = new HandlerThread("wait-thread");
        handlerChatThread.start();
        handler = new Handler(handlerChatThread.getLooper());

        /**
         * ViewPager 에 어댑터 세팅
         */
        mViewPager = binding.viewPager;
        mAdapter = new MeetingListFragment.MyPagerAdapter(getActivity());
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
                // 방 번호 입력 Dialog show
                showParticipateDialog();
            }
        });

        /**
         * 예약
         */
        binding.reserveContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NewReserveMeetingActivity.class);
                reserveCompleteLauncher.launch(intent);
            }
        });

        mAdapter.notifyDataSetChanged();

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

//                Log.d(TAG, "fragmentList:" + fragmentList);
//                if(fragmentList.size() == 0) return;

                ViewPager2 viewPager = binding.viewPager;
                FragmentStateAdapter adapter = (FragmentStateAdapter) viewPager.getAdapter();
                int currentPosition = viewPager.getCurrentItem();

                if (mViewPager.getCurrentItem() == 0) {
                    PublicFragment publicFragment = (PublicFragment) getActivity().getSupportFragmentManager().findFragmentByTag("f" + currentPosition);
                    meetingDTOList = meetingDTOList.stream().filter(meetingDTO -> meetingDTO.getType() == MeetingDTO.TYPE.PUBLIC).collect(Collectors.toList());
                    publicFragment.update(meetingDTOList);
                } else if (mViewPager.getCurrentItem() == 1) {
                    PrivateFragment privateFragment = (PrivateFragment) getActivity().getSupportFragmentManager().findFragmentByTag("f" + currentPosition);
                    meetingDTOList = meetingDTOList.stream().filter(meetingDTO -> meetingDTO.getType() == MeetingDTO.TYPE.PRIVATE).collect(Collectors.toList());
                    privateFragment.update(meetingDTOList);
                } else if (mViewPager.getCurrentItem() == 2) {
                    ReserveFragment reserveFragment = (ReserveFragment) getActivity().getSupportFragmentManager().findFragmentByTag("f" + currentPosition);
                    meetingDTOList = meetingDTOList.stream().filter(meetingDTO -> meetingDTO.getType() == MeetingDTO.TYPE.RESERVE).collect(Collectors.toList());
                    reserveFragment.update(meetingDTOList);
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

        Log.d(TAG_LIFE, getClass().getSimpleName() + " onDestroyView");

//        fragmentList.clear();

        binding = null;
    }

    /**
     * Pager Adapter Class 선언
     */
    public class MyPagerAdapter extends FragmentStateAdapter {
        private static final int NUM_PAGES = 3;

        public MyPagerAdapter(FragmentActivity FragmentActivity) {
            super(FragmentActivity);
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

//            fragmentList.add(fragment);
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

    private void showParticipateDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("참가 방번호 입력");

        // EditText를 추가하여 입력을 받을 수 있도록 설정
        EditText input = new EditText(getActivity());
        input.setHint("회의방 번호를 입력하세요");
        builder.setView(input);

        // 확인 버튼 설정
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String roomId = input.getText().toString();
                if(!isNumeric(roomId)){
                    Toast.makeText(getActivity(), "숫자를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                int roomIdInt = Integer.valueOf(roomId);


                // 서버에서 해당 방번호로 방 존재하는지 확인 및 정보 가져오기
                Call<MeetingDTO> call = RetrofitService.getInstance().getService().checkExistRoom(roomIdInt);
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
                            Toast.makeText(getActivity(), "해당 방은 이미 가득 찼습니다.",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(isExistRoom){
                            // 비밀번호가 없을 때
                            if("".equals(roomPassword) || roomPassword == null){
                                /** 회의 참가 전 화면 이동 */
                                Intent intent = new Intent(getActivity(), ParticipateMeetingActivity.class);
                                String jsonString = gson.toJson(dMeetingDTO);
                                intent.putExtra("jsonString", jsonString);
                                startActivity(intent);
                            // 비밀번호가 있을 때
                            }else{
                                showPasswordAlert(dMeetingDTO, false);
                            }
                        }else{
                            Toast.makeText(getActivity(), "해당 방은 존재하지 않습니다.",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<MeetingDTO> call, Throwable t) {
                        // 오류 처리
                        Log.d(TAG, t.getMessage());
                        Toast.makeText(getActivity(), "해당 방은 존재하지 않습니다.",Toast.LENGTH_SHORT).show();
                    }
                });
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

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);  // Double 혹은 Integer 등 사용 가능
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}


