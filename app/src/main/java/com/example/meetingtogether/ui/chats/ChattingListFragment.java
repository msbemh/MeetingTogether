package com.example.meetingtogether.ui.chats;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.meetingtogether.R;
import com.example.meetingtogether.common.CommonRecyclerView;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.ChatRoomRowItemBinding;
import com.example.meetingtogether.databinding.FragmentChattinglistBinding;
import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.ui.meetings.DTO.ChatRoomListDTO;
import com.example.meetingtogether.ui.user.SignUpActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChattingListFragment extends Fragment {

    private FragmentChattinglistBinding binding;
    private String TAG = "TEST";

    private ChatRoomRowItemBinding chatRoomRowItemBinding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChattinglistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 리사이클러뷰
        RecyclerView recyclerView = binding.userListRecyclerview;

        // 리사이클러뷰 바인드
        CommonRecyclerView commonRecyclerView = new CommonRecyclerView(new CommonRecyclerView.OnBind() {
            // TODO: ViewBind 변경
            // ViewBind 연동
            @Override
            public void onBindViewListener(CommonRecyclerView.MyRecyclerAdapter.ViewHolder viewHolder, View view) {
                viewHolder.setBinding(ChatRoomRowItemBinding.bind(view));
            }
            // TODO: ViewBind 변경
            // 실제 View 와 데이터 연동
            @Override
            public void onBindViewHolderListener(CommonRecyclerView.MyRecyclerAdapter.ViewHolder holder, int position) {
                ChatRoomRowItemBinding itemBinding = (ChatRoomRowItemBinding)(holder.binding);


                CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.userListRecyclerview.getAdapter();

                List<ChatRoomListDTO> dataList = (List<ChatRoomListDTO>) adapter.getDataList();

                itemBinding.profileName.setText(dataList.get(position).getFriendName());
                itemBinding.message.setText(dataList.get(position).getRecentMessage());

                int noReadCnt = dataList.get(position).getNoReadCnt();
                if(noReadCnt > 0){
                    itemBinding.noReadCnt.setVisibility(View.VISIBLE);
                    itemBinding.noReadCnt.setText(String.valueOf(noReadCnt));
                }else{
                    itemBinding.noReadCnt.setVisibility(View.GONE);
                }

                String friendProfileImgPath = dataList.get(position).getFriendProfileImgPath();
                RequestOptions requestOptions = new RequestOptions().circleCrop();
                Glide
                    .with(getActivity())
                    .load(friendProfileImgPath == null ? R.mipmap.ic_launcher : "https://webrtc-sfu.kro.kr/" + friendProfileImgPath)
                    .apply(requestOptions)
                    /** Glide는 원본 비율을 유지한다. */
                    .override(500,500)
                    .into(itemBinding.imageViewProfile);

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
            public void onItemClickListener(View view, int position) {
                Toast.makeText(getActivity(), position + " short 클릭",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onItemLongClickListener(View view, int position) {
                Toast.makeText(getActivity(), position + " long 클릭",Toast.LENGTH_LONG).show();
            }
        });

        commonRecyclerView.setContext(getActivity());
        // TODO: 데이터 변경
        // 데이터 세팅
        commonRecyclerView.setDataList(new ArrayList<ChatRoomListDTO>());
        commonRecyclerView.setRecyclerView(recyclerView);
        // TODO: row item 레이아웃 변경
        // row item 레이아웃 세팅
        commonRecyclerView.setRowItem(R.layout.chat_room_row_item);
        // 적용
        commonRecyclerView.adapt();

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        /** 최신 채팅방 리스트 받아온다. */
        Call<List<ChatRoomListDTO>> call = RetrofitService.getInstance().getService().getChatRoomList();
        call.enqueue(new Callback<List<ChatRoomListDTO>>() {
            @Override
            public void onResponse(Call<List<ChatRoomListDTO>> call, Response<List<ChatRoomListDTO>> response) {
                List<ChatRoomListDTO> dtoList = response.body();
                settingChatRoomList(dtoList);
                Log.d(TAG, "dtoList:" + dtoList);
            }
            @Override
            public void onFailure(Call<List<ChatRoomListDTO>> call, Throwable t) {
                // 오류 처리
                Log.d(TAG, t.getMessage());
                Toast.makeText(getActivity(), "채팅방 목록을 불러오는데 실패했습니다.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void settingChatRoomList(List<ChatRoomListDTO> dtoList){
        CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.userListRecyclerview.getAdapter();
        adapter.updateList(dtoList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}