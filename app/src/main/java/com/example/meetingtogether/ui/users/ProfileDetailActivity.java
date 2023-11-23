package com.example.meetingtogether.ui.users;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.meetingtogether.R;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.ActivityProfileBinding;
import com.example.meetingtogether.databinding.ActivityProfileDetailBinding;
import com.example.meetingtogether.databinding.ActivityProfileEditBinding;
import com.example.meetingtogether.dialogs.CustomDialog;
import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.model.ProfileMap;
import com.example.meetingtogether.model.SliderItem;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.sharedPreference.SharedPreferenceRepository;
import com.google.gson.internal.LinkedTreeMap;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.socket.client.Url;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileDetailActivity extends AppCompatActivity {

    private ActivityProfileDetailBinding binding;
    private String TAG = "TEST";

    private String userId;
    private String friendId;
    private String profileImagePath;
    private String backgroundImagePath;

    private ProfileMap userProfileMap;
    private ProfileMap backgroundProfileMap;
    private String type;
    private List<ProfileMap> profileMapList;
    private List<SliderItem> sliderItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        friendId = intent.getStringExtra("friendId");
        type = intent.getStringExtra("type");

        // 친구의 경우
        if(friendId != null){
            // 프로필 세팅
            Contact friendContact = Util.contactList.stream().filter(contact -> contact.getFriendId().equals(friendId)).findAny().orElse(null);
            if(friendContact != null) {
                List<ProfileMap> friendProfileMapList = friendContact.getFriendImgPaths();
                if(friendProfileMapList != null){
                    profileMapList = friendProfileMapList.stream().filter(profileMap -> profileMap.getType().equals(type)).collect(Collectors.toList());
                }
            }
            // 삭제 버튼 안보이게
            binding.deleteButton.setVisibility(View.INVISIBLE);
        // 사용자 본인의 경우
        }else if(userId != null){
            // 프로필 세팅
            List<ProfileMap> userProfileMapList = Util.user.getProfileImgPaths();
            if(userProfileMapList != null) {
                profileMapList = userProfileMapList.stream().filter(profileMap -> profileMap.getType().equals(type)).collect(Collectors.toList());
            }
            // 삭제 버튼 보이게
            binding.deleteButton.setVisibility(View.VISIBLE);
        }


        SliderView sliderView = binding.imageSliderContainer;

        SliderAdapterExample adapter = new SliderAdapterExample(this);

        sliderView.setSliderAdapter(adapter);

        sliderView.setAutoCycle(false);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using IndicatorAnimationType. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
//        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
        sliderView.setIndicatorSelectedColor(Color.WHITE);
        sliderView.setIndicatorUnselectedColor(Color.GRAY);
//        sliderView.setScrollTimeInSec(4); //set scroll delay in seconds :
//        sliderView.startAutoCycle();

        sliderItems = profileMapList.stream().map(profileMap -> new SliderItem("https://webrtc-sfu.kro.kr/" + profileMap.getProfileImgPath())).collect(Collectors.toList());

        adapter.renewItems(sliderItems);



        /** 삭제 */
        binding.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * [AlertDialog]
                 * 삭제, 확인 Dialog 띄우기
                 */
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileDetailActivity.this);
                builder.setMessage("정말 삭제 하시겠습니까?")
                        .setTitle("확인")
                        .setIcon(R.drawable.metting_together_logo);
                // 긍정 버튼
                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 삭제 시키기
                        deleteProfile(sliderView.getCurrentPagePosition());
                    }
                });
                // 부정 버튼
                builder.setNegativeButton("취소", null);
                builder.show();
            }
        });
    }

    private void deleteProfile(int position){
        String imgPath = sliderItems.get(position).getImagePath();
        try {
            URL url = new URL(imgPath);
            imgPath = url.getPath();
            imgPath = imgPath.substring(1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }


        // 서버로 삭제 요청
        Call<CommonRetrofitResponse> call = RetrofitService.getInstance().getService().postDeleteProfileImages(imgPath);
        String finalImgPath = imgPath;
        call.enqueue(new Callback<CommonRetrofitResponse>() {
            @Override
            public void onResponse(Call<CommonRetrofitResponse> call, Response<CommonRetrofitResponse> response) {
                // 응답 처리
                if (response.isSuccessful()) {

                    // 슬라이드 list 삭제
                    sliderItems.remove(position);

                    // 로컬 사용자 데이터 삭제
                    List<ProfileMap> filteredList = Util.user.getProfileImgPaths().stream().filter(profileMap -> !finalImgPath.equals(profileMap.getProfileImgPath())).collect(Collectors.toList());
                    Util.user.setProfileImgPaths(filteredList);

                    // 쉐어드에 저장
                    SharedPreferenceRepository.saveUserForAutoLogin(Util.user);

                    Toast.makeText(ProfileDetailActivity.this, "삭제가 완료됐습니다.", Toast.LENGTH_SHORT).show();

                    setResult(RESULT_OK);
                    finish();
                }
                Log.d(TAG, response.message());
            }
            @Override
            public void onFailure(Call<CommonRetrofitResponse> call, Throwable t) {
                // 오류 처리
                Log.d(TAG, t.getMessage());
            }
        });
    }

    public class SliderAdapterExample extends SliderViewAdapter<SliderAdapterExample.SliderAdapterVH> {
        private Context context;
        private List<SliderItem> mSliderItems = new ArrayList<>();
        public SliderAdapterExample(Context context) {
            this.context = context;
        }
        public void renewItems(List<SliderItem> sliderItems) {
            this.mSliderItems = sliderItems;
            notifyDataSetChanged();
        }
        public void deleteItem(int position) {
            this.mSliderItems.remove(position);
            notifyDataSetChanged();
        }
        public void addItem(SliderItem sliderItem) {
            this.mSliderItems.add(sliderItem);
            notifyDataSetChanged();
        }
        @Override
        public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slider, null);
            return new SliderAdapterVH(inflate);
        }
        @Override
        public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {

            SliderItem sliderItem = mSliderItems.get(position);

            Glide.with(viewHolder.itemView)
                    .load(sliderItem.getImagePath() != null ? sliderItem.getImagePath() : R.mipmap.ic_launcher)
                    .fitCenter()
                    .into(viewHolder.imageView);

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "This is item in position " + position, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getCount() {
            //slider view count could be dynamic size
            return mSliderItems.size();
        }

        class SliderAdapterVH extends SliderViewAdapter.ViewHolder {
            ImageView imageView;
            public SliderAdapterVH(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageSlider);
            }
        }

    }
}