package com.example.meetingtogether.ui.meetings.fragments;

import static com.example.meetingtogether.MainActivity.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;

import com.example.meetingtogether.databinding.FragmentPeersBinding;

public class PeersFragment extends Fragment {

    FragmentPeersBinding binding;
    private CreateResultInterface createResultInterface;

    public PeersFragment(CreateResultInterface createResultInterface) {
        this.createResultInterface = createResultInterface;
    }

    public static PeersFragment newInstance(CreateResultInterface createResultInterface) {
        Log.d(TAG, "[PeerFragment] newInstance");
        PeersFragment fragment = new PeersFragment(createResultInterface);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "[PeerFragment] onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPeersBinding.inflate(inflater, container, false);
        Log.d(TAG, "[PeerFragment] onCreateView");

        /**
         * 메인 화면 비율을 1:1로 맞춘다.
         */
        ViewTreeObserver viewTreeObserver = binding.mainSurfaceView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // View의 크기가 변경되었을 때 실행할 작업을 여기에 구현합니다.
                    int width = binding.mainSurfaceView.getWidth();
                    int height = binding.mainSurfaceView.getHeight();

                    int min = Math.min(width, height);

                    // 리스너를 제거합니다.
                    binding.mainSurfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    FrameLayout.LayoutParams Layoutparams = (FrameLayout.LayoutParams) binding.mainSurfaceView.getLayoutParams();
                    Layoutparams.width = min;
                    Layoutparams.height = min;
                    Layoutparams.gravity = Gravity.CENTER;
                    binding.mainSurfaceView.setLayoutParams(Layoutparams);
                }
        });


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
    public void onStop() {
        super.onStop();
        Log.d(TAG, "[PeerFragment] onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "[PeerFragment] onPause");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "[PeerFragment] onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "[PeerFragment] onDestroy");
    }
}