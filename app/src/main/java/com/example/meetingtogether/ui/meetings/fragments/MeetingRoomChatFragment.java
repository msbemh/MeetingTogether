package com.example.meetingtogether.ui.meetings.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.meetingtogether.R;
import com.example.meetingtogether.databinding.FragmentMeetingRoomChatBinding;
import com.example.meetingtogether.databinding.FragmentWhiteboardBinding;
import com.example.meetingtogether.ui.meetings.DTO.ColorModel;


public class MeetingRoomChatFragment extends Fragment {

    private MeetingRoomChatFragment.CreateResultInterface createResultInterface;
    private FragmentMeetingRoomChatBinding binding;

    public MeetingRoomChatFragment(MeetingRoomChatFragment.CreateResultInterface createResultInterface) {
        this.createResultInterface = createResultInterface;
    }

    public static MeetingRoomChatFragment newInstance(MeetingRoomChatFragment.CreateResultInterface createResultInterface) {
        MeetingRoomChatFragment fragment = new MeetingRoomChatFragment(createResultInterface);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public interface CreateResultInterface{
        void onCreated(FragmentMeetingRoomChatBinding binding);
        void onError(Exception e);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMeetingRoomChatBinding.inflate(inflater, container, false);
        try{
            this.createResultInterface.onCreated(binding);
        }catch (Exception e){
            this.createResultInterface.onError(e);
        }

        return binding.getRoot();
    }
}