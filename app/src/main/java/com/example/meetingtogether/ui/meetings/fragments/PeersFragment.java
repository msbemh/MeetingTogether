package com.example.meetingtogether.ui.meetings.fragments;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.meetingtogether.databinding.FragmentPeersBinding;

public class PeersFragment extends Fragment {

    FragmentPeersBinding binding;
    private CreateResultInterface createResultInterface;

    public PeersFragment(CreateResultInterface createResultInterface) {
        this.createResultInterface = createResultInterface;
    }

    public static PeersFragment newInstance(CreateResultInterface createResultInterface) {
        PeersFragment fragment = new PeersFragment(createResultInterface);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPeersBinding.inflate(inflater, container, false);

        try{
            this.createResultInterface.onCreated(binding);
        }catch (Exception e){
            this.createResultInterface.onError(e);
        }

        return binding.getRoot();
    }

    public interface CreateResultInterface{
        void onCreated(FragmentPeersBinding peersBinding);
        void onError(Exception e);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}