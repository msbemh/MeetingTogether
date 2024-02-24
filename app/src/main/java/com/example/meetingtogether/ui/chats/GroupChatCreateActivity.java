package com.example.meetingtogether.ui.chats;

import static com.example.meetingtogether.MainActivity.TAG;
import static com.example.meetingtogether.common.Common.OTHER_USER_ID;
import static com.example.meetingtogether.common.Common.OTHER_USER_NAME;
import static com.example.meetingtogether.common.Common.ROOMID;
import static com.example.meetingtogether.common.Common.ROOM_TYPE_ID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.meetingtogether.MainActivity;
import com.example.meetingtogether.R;
import com.example.meetingtogether.common.CommonRecyclerView;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.ActivityChatRoomBinding;
import com.example.meetingtogether.databinding.ActivityGroupChatCreateBinding;
import com.example.meetingtogether.databinding.UserRowItemBinding;
import com.example.meetingtogether.databinding.UserRowSelectItemBinding;
import com.example.meetingtogether.dialogs.CustomDialog;
import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.model.CreateRoomDTO;
import com.example.meetingtogether.model.MessageDTO;
import com.example.meetingtogether.model.ProfileMap;
import com.example.meetingtogether.model.User;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.sharedPreference.SharedPreferenceRepository;
import com.example.meetingtogether.ui.user.LoginActivity;
import com.example.meetingtogether.ui.users.ProfileActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kotlin.collections.Grouping;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupChatCreateActivity extends AppCompatActivity {

    private ActivityGroupChatCreateBinding binding;
    private List<Contact> dataList;
    private UserRowSelectItemBinding userRowSelectItemBinding;
    private List<Contact> addDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGroupChatCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String roomName = binding.roomName.getText().toString();

                if(roomName == null || roomName.isEmpty()){
                    Toast.makeText(GroupChatCreateActivity.this, "방 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(addDataList.size() == 0){
                    Toast.makeText(GroupChatCreateActivity.this, "초대할 인원을 1명 이상 선택하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                CreateRoomDTO createRoomDTO = new CreateRoomDTO();
                createRoomDTO.setContactList(addDataList);
                createRoomDTO.setName(roomName);

                Call<CommonRetrofitResponse> call = RetrofitService.getInstance().getService().postCreateGroupRoom(createRoomDTO);
                call.enqueue(new Callback<CommonRetrofitResponse>() {
                    @Override
                    public void onResponse(Call<CommonRetrofitResponse> call, Response<CommonRetrofitResponse> response) {
                        CommonRetrofitResponse commonRetrofitResponse = response.body();
                        int roomId = Integer.parseInt(commonRetrofitResponse.getData().toString());
                        Log.d(TAG, "roomId:" + roomId);

                        Intent intent = new Intent(GroupChatCreateActivity.this, ChatRoomActivity.class);
                        intent.putExtra(ROOMID, roomId);
                        intent.putExtra(ROOM_TYPE_ID, MessageDTO.RoomType.GROUP.name());

                        String groupRoomName = "";
                        String groupRoomFriendId = "";
                        for(int i=0; i<addDataList.size(); i++){
                            Contact contact = addDataList.get(i);
                            String friendName = contact.getFriendName();
                            String friendId = contact.getFriendId();

                            if(i != addDataList.size() -1){
                                friendName += ", ";
                                friendId += ";";
                            }
                            groupRoomName += friendName;
                            groupRoomFriendId += friendId;
                        }

                        intent.putExtra(OTHER_USER_NAME, groupRoomName);
                        intent.putExtra(OTHER_USER_ID, groupRoomFriendId);

                        startActivity(intent);

                        GroupChatCreateActivity.this.finish();
                    }

                    @Override
                    public void onFailure(Call<CommonRetrofitResponse> call, Throwable t) {
                        Log.d(TAG, t.getMessage());
                    }
                });
            }
        });

        Util.contactList = SharedPreferenceRepository.getFriendList();
        dataList = Util.contactList;

        // 리사이클러뷰
        RecyclerView userListRecyclerview = binding.userListRecyclerview;

        // 리사이클러뷰 바인드
        CommonRecyclerView commonRecyclerView = new CommonRecyclerView(new CommonRecyclerView.OnBind() {
            // TODO: ViewBind 변경
            // ViewBind 연동
            @Override
            public void onBindViewListener(CommonRecyclerView.MyRecyclerAdapter.ViewHolder viewHolder, View view) {
                viewHolder.setBinding(userRowSelectItemBinding.bind(view));
            }
            // TODO: ViewBind 변경
            // 실제 View 와 데이터 연동
            @Override
            public void onBindViewHolderListener(CommonRecyclerView.MyRecyclerAdapter.ViewHolder holder, int position) {
                CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.userListRecyclerview.getAdapter();
                List<Contact> filteredList = (List<Contact>) adapter.getFilteredList();

                UserRowSelectItemBinding binding = (UserRowSelectItemBinding)(holder.binding);
                binding.profileName.setText(filteredList.get(position).getFriendName());

                String imgPath = null;
                // 프로필 이미지만 필터링
                if(filteredList.get(position).getFriendImgPaths().size() > 0) {
                    ProfileMap userProfileMap = filteredList.get(position).getFriendImgPaths().stream().filter(profileMap -> profileMap.getType().equals(CustomDialog.Type.PROFILE_IMAGE.name())).findFirst().orElse(null);
                    if(userProfileMap != null){
                        imgPath = userProfileMap.getProfileImgPath();
                    }
                }

                RequestOptions requestOptions = new RequestOptions().circleCrop();

                Glide
                    .with(GroupChatCreateActivity.this)
                    .load(imgPath == null ? R.mipmap.ic_launcher : "https://webrtc-sfu.kro.kr/" + imgPath)
                    .apply(requestOptions)
                    /** Glide는 원본 비율을 유지한다. */
                    .override(500,500)
                    .into(binding.imageViewProfile);
            }

            // TODO: 레이아웃 변경
            // 레이아웃 설정
            @Override
            public void onLayout(Context context, RecyclerView recyclerView) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(linearLayoutManager);
            }
        });

        // TODO: 클릭 이벤트 변경
        // 리사이클러뷰 클릭 이벤트
        commonRecyclerView.setOnItemClickListener(new CommonRecyclerView.OnItemClickInterface() {
            @Override
            public void onItemClickListener(View view, int position, ViewBinding pBinding) {
                UserRowSelectItemBinding userRowSelectItemBinding = (UserRowSelectItemBinding) pBinding;
                Contact selectedContact = dataList.get(position);

                boolean isChecked = userRowSelectItemBinding.userCheckBox.isChecked();
                if(isChecked){
                    userRowSelectItemBinding.userCheckBox.setChecked(false);
                    // 추가 리스트에서 해당 유저를 뺀다.
                    addDataList = addDataList.stream().filter(contact -> !contact.getFriendId().equals(selectedContact.getFriendId())).collect(Collectors.toList());
                }else{
                    userRowSelectItemBinding.userCheckBox.setChecked(true);
                    // 추가 리스트에서 해당 유저를 넣는다.
                    addDataList.add(selectedContact);
                }
            }
            @Override
            public void onItemLongClickListener(View view, int position) {
                Toast.makeText(GroupChatCreateActivity.this, position + " long 클릭",Toast.LENGTH_LONG).show();
            }
        });

        commonRecyclerView.setContext(GroupChatCreateActivity.this);
        // TODO: 데이터 변경
        // 데이터 세팅
        commonRecyclerView.setDataList(dataList);
        commonRecyclerView.setRecyclerView(userListRecyclerview);
        // TODO: row item 레이아웃 변경
        // row item 레이아웃 세팅
        commonRecyclerView.setRowItem(R.layout.user_row_select_item);
        // 적용
        commonRecyclerView.adapt();
    }
}