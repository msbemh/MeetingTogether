package com.example.meetingtogether.ui.chats;

import static com.example.meetingtogether.MainActivity.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.meetingtogether.R;
import com.example.meetingtogether.databinding.ActivityChatImgViewBinding;
import com.example.meetingtogether.databinding.ActivityProfileDetailBinding;
import com.example.meetingtogether.model.MessageDTO;
import com.example.meetingtogether.model.SliderItem;
import com.example.meetingtogether.retrofit.FileInfo;
import com.example.meetingtogether.retrofit.LocalDateTimeDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatImgViewActivity extends AppCompatActivity {

    private ActivityChatImgViewBinding binding;
    private Gson gson;

    private List<FileInfo> fileInfoList;
    private List<SliderItem> sliderItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatImgViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());
        gson = gsonBuilder.create();

        Intent intent = getIntent();
        String jsonString = intent.getStringExtra("json");
        MessageDTO messageDTO = gson.fromJson(jsonString, MessageDTO.class);
        Log.d(TAG, "messageDTO:" + messageDTO);

        fileInfoList = messageDTO.getFileInfoList();

        SliderView sliderView = binding.imageSliderContainer;
        ChatImgViewActivity.SliderAdapterExample adapter = new ChatImgViewActivity.SliderAdapterExample(this);
        sliderView.setSliderAdapter(adapter);

        sliderView.setAutoCycle(false);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using IndicatorAnimationType. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setIndicatorSelectedColor(Color.WHITE);
        sliderView.setIndicatorUnselectedColor(Color.GRAY);

        sliderItems = fileInfoList.stream().map(fileInfo -> new SliderItem("https://webrtc-sfu.kro.kr/" + fileInfo.getPath())).collect(Collectors.toList());

        adapter.renewItems(sliderItems);

        binding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public class SliderAdapterExample extends SliderViewAdapter<ChatImgViewActivity.SliderAdapterExample.SliderAdapterVH> {
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
        public ChatImgViewActivity.SliderAdapterExample.SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slider, null);
            return new ChatImgViewActivity.SliderAdapterExample.SliderAdapterVH(inflate);
        }
        @Override
        public void onBindViewHolder(ChatImgViewActivity.SliderAdapterExample.SliderAdapterVH viewHolder, final int position) {

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