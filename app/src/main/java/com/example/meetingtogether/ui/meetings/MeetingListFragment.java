package com.example.meetingtogether.ui.meetings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.meetingtogether.databinding.FragmentMeetinglistBinding;
import com.example.meetingtogether.ui.chats.ChattingListFragment;
import com.example.meetingtogether.ui.users.UserListFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MeetingListFragment extends Fragment {

    private FragmentMeetinglistBinding binding;

    private MeetingListFragment.MyPagerAdapter mAdapter;
    private ViewPager2 mViewPager;
    private TabLayout mTabLayout;

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
            String title = "";
            switch(position){
                case  0:
                    title = "public";
                    break;
                case 1:
                    title = "private";
                    break;
                case 2:
                    title = "예약";
                    break;
            }
            tab.setText(title);
        }).attach();

        return root;
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
            return fragment;
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }

    }
}