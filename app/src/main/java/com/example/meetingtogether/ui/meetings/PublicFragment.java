package com.example.meetingtogether.ui.meetings;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.meetingtogether.R;
import com.example.meetingtogether.common.CommonRecyclerView;
import com.example.meetingtogether.databinding.FragmentPublicBinding;
import com.example.meetingtogether.databinding.PlainRowItemBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicFragment extends Fragment {

    private FragmentPublicBinding binding;

    public PublicFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPublicBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 리사이클러뷰
        RecyclerView recyclerView = binding.publicRecyclerview;

        // 데이터 수동 설정
        List<Map> dataList = new ArrayList<>();
        for(int i=0; i<3; i++){
            Map map = new HashMap();
            map.put("room", i+1);
            dataList.add(map);
        }

        // 리사이클러뷰 바인드
        CommonRecyclerView commonRecyclerView = new CommonRecyclerView(new CommonRecyclerView.OnBind() {
            // TODO: ViewBind 변경
            // ViewBind 연동
            @Override
            public void onBindViewListener(CommonRecyclerView.MyRecyclerAdapter.ViewHolder viewHolder, View view) {
                viewHolder.setBinding(PlainRowItemBinding.bind(view));
            }
            // TODO: ViewBind 변경
            // 실제 View 와 데이터 연동
            @Override
            public void onBindViewHolderListener(CommonRecyclerView.MyRecyclerAdapter.ViewHolder holder, int position) {
                PlainRowItemBinding binding = (PlainRowItemBinding)(holder.binding);
                binding.textViewTitle.setText(dataList.get(position).get("room").toString() + "번 방");
                binding.textViewSubTitle.setText("테스트");
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
//                int room = Integer.parseInt(dataList.get(position).get("room").toString());
                String roomId = dataList.get(position).get("room").toString();

//                Intent intent = new Intent(getActivity(), MeetingRoomActivity.class);
//                Intent intent = new Intent(getActivity(), ConnectActivity.class);
//                intent.putExtra("roomId", roomId);

                MeetingListFragment meetingListFragment = (MeetingListFragment) getParentFragment();
                meetingListFragment.connectToRoom(roomId, false, false, false, 0);
//                getActivity().startActivity(intent);
            }
            @Override
            public void onItemLongClickListener(View view, int position) {
                Toast.makeText(getActivity(), position + " long 클릭",Toast.LENGTH_LONG).show();
            }
        });

        commonRecyclerView.setContext(getActivity());
        // TODO: 데이터 변경
        // 데이터 세팅
        commonRecyclerView.setDataList(dataList);
        commonRecyclerView.setRecyclerView(recyclerView);
        // TODO: row item 레이아웃 변경
        // row item 레이아웃 세팅
        commonRecyclerView.setRowItem(R.layout.plain_row_item);
        // 적용
        commonRecyclerView.adapt();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }


}