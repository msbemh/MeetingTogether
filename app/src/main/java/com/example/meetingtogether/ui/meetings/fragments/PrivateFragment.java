package com.example.meetingtogether.ui.meetings.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.meetingtogether.R;
import com.example.meetingtogether.common.CommonRecyclerView;
import com.example.meetingtogether.databinding.FragmentPrivateBinding;
import com.example.meetingtogether.databinding.FragmentPublicBinding;
import com.example.meetingtogether.databinding.MeetingRowItemBinding;
import com.example.meetingtogether.ui.meetings.DTO.MeetingDTO;

import java.util.ArrayList;
import java.util.List;

public class PrivateFragment extends Fragment {

    private FragmentPrivateBinding binding;
    private CommonRecyclerView.MyRecyclerAdapter adapter;

    public PrivateFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPrivateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 리사이클러뷰
        RecyclerView recyclerView = binding.privateRecyclerview;

        // 리사이클러뷰 바인드
        CommonRecyclerView commonRecyclerView = new CommonRecyclerView(new CommonRecyclerView.OnBind() {
            // TODO: ViewBind 변경
            // ViewBind 연동
            @Override
            public void onBindViewListener(CommonRecyclerView.MyRecyclerAdapter.ViewHolder viewHolder, View view) {
                viewHolder.setBinding(MeetingRowItemBinding.bind(view));
            }
            // TODO: ViewBind 변경
            // 실제 View 와 데이터 연동
            @Override
            public void onBindViewHolderListener(CommonRecyclerView.MyRecyclerAdapter.ViewHolder holder, int position) {
                MeetingRowItemBinding binding = (MeetingRowItemBinding)(holder.binding);
                List<MeetingDTO> meetingDTOS = (List<MeetingDTO>) adapter.getDataList();

                MeetingDTO meetingDTO = meetingDTOS.get(position);
                String password = meetingDTO.getPassword();

                /**
                 * 비밀 번호가 존재할 경우에만 열쇠 표시
                 */
                if(!"".equals(password) && password != null){
                    binding.passwordImage.setVisibility(View.VISIBLE);
                }else{
                    binding.passwordImage.setVisibility(View.GONE);
                }

                binding.textViewTitle.setText(meetingDTOS.get(position).getTitle());

                String subTitle = meetingDTO.getCurrentClient() + "/" + meetingDTO.getMaxClient();
                binding.textViewSubTitle.setText(subTitle);
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
                List<MeetingDTO> meetingDTOS = (List<MeetingDTO>) adapter.getDataList();
                MeetingDTO meetingDTO = meetingDTOS.get(position);

                MeetingListFragment meetingListFragment = (MeetingListFragment) getParentFragment();
                meetingListFragment.connectToRoom(meetingDTO, false);
            }
            @Override
            public void onItemLongClickListener(View view, int position) {
                Toast.makeText(getActivity(), position + " long 클릭",Toast.LENGTH_LONG).show();
            }
        });

        commonRecyclerView.setContext(getActivity());
        // TODO: 데이터 변경
        // 데이터 세팅
        commonRecyclerView.setDataList(new ArrayList<>());
        commonRecyclerView.setRecyclerView(recyclerView);
        // TODO: row item 레이아웃 변경
        // row item 레이아웃 세팅
        commonRecyclerView.setRowItem(R.layout.meeting_row_item);
        // 적용
        adapter = commonRecyclerView.adapt();
    }

    public void update(List<MeetingDTO> pMeetingDTOList){
        adapter.updateList(pMeetingDTOList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}