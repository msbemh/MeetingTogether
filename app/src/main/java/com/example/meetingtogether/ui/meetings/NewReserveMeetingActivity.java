package com.example.meetingtogether.ui.meetings;

import static com.example.meetingtogether.MainActivity.TAG;
import static com.example.meetingtogether.common.Common.HOST;
import static com.example.meetingtogether.common.Common.IS_ACTIVATE_CAMERA;
import static com.example.meetingtogether.common.Common.ROOMID;
import static com.example.meetingtogether.common.Common.ROOM_NAME;
import static com.example.meetingtogether.sharedPreference.SharedPreferenceRepository.gson;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.meetingtogether.MainActivity;
import com.example.meetingtogether.R;
import com.example.meetingtogether.common.CommonRecyclerView;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.ActivityNewReserveMeetingBinding;
import com.example.meetingtogether.databinding.UserRowSelectBorderItemBinding;
import com.example.meetingtogether.databinding.UserRowSelectItemBinding;
import com.example.meetingtogether.dialogs.CustomDialog;
import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.model.ProfileMap;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.ui.chats.GroupChatCreateActivity;
import com.example.meetingtogether.ui.meetings.DTO.MeetingDTO;
import com.example.meetingtogether.ui.meetings.DTO.UserModel;
import com.example.meetingtogether.ui.meetings.fragments.DatePickerFragment;
import com.example.meetingtogether.ui.meetings.fragments.MeetingReserveUserListDialogFragment;
import com.example.meetingtogether.ui.meetings.fragments.MeetingUserListDialogFragment;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewReserveMeetingActivity extends AppCompatActivity {
    private ActivityNewReserveMeetingBinding binding;

    private SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm");

    private SimpleDateFormat dateTimeSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private MeetingReserveUserListDialogFragment meetingResrMeetingReserveUserListDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewReserveMeetingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 현재 시간을 가져옵니다.
        LocalDateTime now = LocalDateTime.now();

        // LocalDateTime을 UTC로 변환
        ZonedDateTime utcTime = now.atZone(ZoneId.of("UTC"));

        // UTC 시간을 한국 표준시(KST)로 변환
//        ZonedDateTime kstTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        ZonedDateTime kstTime = now.atZone(ZoneId.of("Asia/Seoul"));

        // 1시간 이후로 설정합니다.
        ZonedDateTime endKstTime = kstTime.plus(1, ChronoUnit.HOURS);

        /** 현재 날짜로 세팅 */
        binding.reserveStartDateView.setText(kstTime.format(dateFormatter));
        binding.reserveStartTimeView.setText(kstTime.format(timeFormatter));
        binding.reserveEndDateView.setText(endKstTime.format(dateFormatter));
        binding.reserveEndTimeView.setText(endKstTime.format(timeFormatter));

        // 회의 시작 년월일 선택
        binding.reserveStartDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDate(binding.reserveStartDateView);
            }
        });

        binding.reserveStartTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime(binding.reserveStartTimeView);
            }
        });

        binding.reserveEndDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDate(binding.reserveEndDateView);
            }
        });

        binding.reserveEndTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime(binding.reserveEndTimeView);
            }
        });

        binding.participateAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserListDialog();
            }
        });

        binding.meetingReserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.reserveUserListRecyclerview.getAdapter();
                List<Contact> contactList = (List<Contact>) adapter.getFilteredList();
                String title = binding.meetingTitleEdit.getText().toString();

                String startDateString = binding.reserveStartDateView.getText() + " " + binding.reserveStartTimeView.getText();
                String endDateString = binding.reserveEndDateView.getText() + " " + binding.reserveEndTimeView.getText();

                LocalDateTime startDate = LocalDateTime.parse(startDateString, dateTimeFormatter);
                LocalDateTime endDate = LocalDateTime.parse(endDateString, dateTimeFormatter);

                if(title == null || "".equals(title)){
                    Toast.makeText(NewReserveMeetingActivity.this, "제목을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                MeetingDTO meetingDTO = new MeetingDTO();
                meetingDTO.setTitle(title);
                meetingDTO.setReserve_start_date(startDate);
                meetingDTO.setReserve_end_date(endDate);
                meetingDTO.setReserveContactList(contactList);

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
                            Toast.makeText(NewReserveMeetingActivity.this, "미팅 회의 방 예약에 성공했습니다.",Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }else{
                            Toast.makeText(NewReserveMeetingActivity.this, "미팅 회의 방 예약에 실패했습니다.",Toast.LENGTH_SHORT).show();
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

        // 리사이클러뷰
        RecyclerView recyclerView = binding.reserveUserListRecyclerview;

        // 리사이클러뷰 바인드
        CommonRecyclerView commonRecyclerView = new CommonRecyclerView(new CommonRecyclerView.OnBind() {
            // TODO: ViewBind 변경
            // ViewBind 연동
            @Override
            public void onBindViewListener(CommonRecyclerView.MyRecyclerAdapter.ViewHolder viewHolder, View view) {
                viewHolder.setBinding(UserRowSelectBorderItemBinding.bind(view));
            }
            // TODO: ViewBind 변경
            // 실제 View 와 데이터 연동
            @Override
            public void onBindViewHolderListener(CommonRecyclerView.MyRecyclerAdapter.ViewHolder holder, int position) {
                CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.reserveUserListRecyclerview.getAdapter();
                List<Contact> filteredList = (List<Contact>) adapter.getFilteredList();

                UserRowSelectBorderItemBinding binding = (UserRowSelectBorderItemBinding)(holder.binding);
                Contact selectedContact = filteredList.get(position);
                binding.profileName.setText(selectedContact.getFriendName());

                binding.removeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        filteredList.remove(selectedContact);
                        adapter.notifyItemRemoved(position);
                    }
                });
            }

            // TODO: 레이아웃 변경
            // 레이아웃 설정
            @Override
            public void onLayout(Context context, RecyclerView recyclerView) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                recyclerView.setLayoutManager(linearLayoutManager);
            }
        });

        // TODO: 클릭 이벤트 변경
        // 리사이클러뷰 클릭 이벤트
        commonRecyclerView.setOnItemClickListener(new CommonRecyclerView.OnItemClickInterface() {
            @Override
            public void onItemClickListener(View view, int position, ViewBinding pBinding) {
            }
            @Override
            public void onItemLongClickListener(View view, int position) {
            }
        });

        commonRecyclerView.setContext(this);
        // TODO: 데이터 변경
        // 데이터 세팅
        commonRecyclerView.setDataList(new ArrayList<Contact>());
        commonRecyclerView.setRecyclerView(recyclerView);
        // TODO: row item 레이아웃 변경
        // row item 레이아웃 세팅
        commonRecyclerView.setRowItem(R.layout.user_row_select_border_item);
        // 적용
        commonRecyclerView.adapt();

        CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.reserveUserListRecyclerview.getAdapter();
        adapter.notifyDataSetChanged();
    }

    private void setDate(TextView textView){
        String before = textView.getText().toString();
        LocalDate beforeDate = LocalDate.parse(before, dateFormatter);

        DatePickerFragment dialogFragment = new DatePickerFragment(beforeDate.getYear(), beforeDate.getMonthValue(), beforeDate.getDayOfMonth());
        dialogFragment.setListener(new DatePickerFragment.Listener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                LocalDate dateTime = LocalDate.of(year, month+1, day);

                textView.setText(dateFormatter.format(dateTime));
                if(!validateDate()){
                    textView.setText(before);
                }
            }
        });
        dialogFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private void setTime(TextView textView){
        String before = textView.getText().toString();
        LocalTime beforeTime = LocalTime.parse(before, timeFormatter);

        // TimePickerDialog 생성 및 설정
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (timePicker, selectedHour, selectedMinute) -> {
                    LocalTime dateTime = LocalTime.of(selectedHour, selectedMinute, 0);

                    textView.setText(timeFormatter.format(dateTime));
                    if(!validateDate()){
                        textView.setText(before);
                    }
                }, beforeTime.getHour(), beforeTime.getMinute(), true);

        // TimePickerDialog 보여주기
        timePickerDialog.show();
    }


    private boolean validateDate(){

        String startDateString = binding.reserveStartDateView.getText() + " " + binding.reserveStartTimeView.getText();
        String endDateString = binding.reserveEndDateView.getText() + " " + binding.reserveEndTimeView.getText();

        LocalDateTime startDate = LocalDateTime.parse(startDateString, dateTimeFormatter);
        LocalDateTime endDate = LocalDateTime.parse(endDateString, dateTimeFormatter);

        if(startDate.isAfter(endDate)){
            Toast.makeText(NewReserveMeetingActivity.this, "시작날짜는 종료날짜보다 전날이어야 합니다.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void showUserListDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(meetingResrMeetingReserveUserListDialogFragment == null){
            meetingResrMeetingReserveUserListDialogFragment = new MeetingReserveUserListDialogFragment(new MeetingReserveUserListDialogFragment.Listener() {
                @Override
                public void onCompleteClick(List<Contact> selectedContactList) {
                    Log.d(TAG, "selectedContactList:" + selectedContactList);
                    CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.reserveUserListRecyclerview.getAdapter();
                    /** 크기 변경 (삭제, 추가) 동시에 이루어 지므로 notifyDataSetChanged를 이용하자 */
                    List<Contact> contactList = (List<Contact>) adapter.getFilteredList();
                    contactList.clear();

                    contactList.addAll(selectedContactList);

                    adapter.notifyDataSetChanged();
                }
            });
        }
        // 프래그먼트가 생성 되어져 있고, 아직 isAdded 상태가 아닐 때만 show 시킨다
        if(meetingResrMeetingReserveUserListDialogFragment != null && !meetingResrMeetingReserveUserListDialogFragment.isAdded()) {
            CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.reserveUserListRecyclerview.getAdapter();
            List<Contact> contactList = (List<Contact>) adapter.getFilteredList();
            meetingResrMeetingReserveUserListDialogFragment.update(contactList);

            meetingResrMeetingReserveUserListDialogFragment.show(fragmentManager, "reserveUserListDialog");
        }

    }
}