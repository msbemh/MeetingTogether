package com.example.meetingtogether.ui.chats;

import static android.app.Activity.RESULT_OK;
import static com.example.meetingtogether.common.Common.OTHER_USER_ID;
import static com.example.meetingtogether.common.Common.OTHER_USER_NAME;
import static com.example.meetingtogether.common.Common.ROOMID;
import static com.example.meetingtogether.common.Common.ROOM_NAME;
import static com.example.meetingtogether.common.Common.ROOM_TYPE_ID;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.meetingtogether.R;
import com.example.meetingtogether.common.CommonRecyclerView;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.ChatRoomRowItemBinding;
import com.example.meetingtogether.databinding.FragmentChattinglistBinding;
import com.example.meetingtogether.dialogs.CustomDialog;
import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.model.MessageDTO;
import com.example.meetingtogether.model.ProfileMap;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.services.ChatService;
import com.example.meetingtogether.ui.meetings.DTO.ChatRoomListDTO;
import com.example.meetingtogether.ui.user.SignUpActivity;
import com.example.meetingtogether.ui.users.ProfileActivity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChattingListFragment extends Fragment {

    private FragmentChattinglistBinding binding;
    private String TAG = "TEST";

    private ChatRoomRowItemBinding chatRoomRowItemBinding;
    private Handler handler;

    /**
     * 그룹 채팅방 생성 결과 받기
     */
    private ActivityResultLauncher groupRoomCreateLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            // 추가
            if(result.getResultCode() == RESULT_OK){

            // 취소
            }else {

            }
        }
    });

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChattinglistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        HandlerThread handlerChatThread = new HandlerThread("chatting-list-handler");
        handlerChatThread.start();
        handler = new Handler(handlerChatThread.getLooper());

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

                List<ChatRoomListDTO> dataList = (List<ChatRoomListDTO>) adapter.getFilteredList();

                ChatRoomListDTO chatRoomListDTO = dataList.get(position);

                itemBinding.profileName.setText(dataList.get(position).getFriendName());
                itemBinding.message.setText(dataList.get(position).getRecentMessage());

                int noReadCnt = dataList.get(position).getNoReadCnt();
                if(noReadCnt > 0){
                    itemBinding.noReadCnt.setVisibility(View.VISIBLE);
                    itemBinding.noReadCnt.setText(String.valueOf(noReadCnt));
                }else{
                    itemBinding.noReadCnt.setVisibility(View.GONE);
                }

                LocalDateTime recentMessageCreateDate = dataList.get(position).getRecentMessageCreateDate();
                if(recentMessageCreateDate != null){
                    itemBinding.messageDate.setText(recentMessageCreateDate.toLocalDate() + " " + recentMessageCreateDate.toLocalTime());
                }else{
                    itemBinding.messageDate.setVisibility(View.INVISIBLE);
                }

                // 채팅방 인원 수 표시
                int userCnt = chatRoomListDTO.getUserCnt();
                itemBinding.userCnt.setText(userCnt + "명");

                /**
                 * 채팅방 인원이 2명일 경우에는 그냥 그대로.
                 * 채팅방 인원이 3명일 경우,  2개 프로필 표현. profileArray 에 데이터 부족하면 기본 이미지로 채운다. 좌측상단, 우측하단에 배치
                 * 채팅방 인원이 4명일 경우, 3개 프로필 표현. profileArray 에 데이터 부족하면 기본 이미지로 채운다. 가운데 상단, 좌측하단, 우측하단 배치
                 * 채팅방 인원이 5명 이상일 경우, 4개 프로필 표현. profileArray 에 데이터 부족하면 기본 이미지로 채운다. 바둑판 모양 배치
                 */
                String[] profileArray = chatRoomListDTO.getFriendProfileImgPath().split(";");

                // bitmap 으로 변환은 느리기 때문에 별도의 스레드에서 동작 시킨다.
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        List<Bitmap> bitmapList = convertBitmap(profileArray);
                        // 없으면 기본 mimap ic_launcher bitmap으로 대체
                        while(bitmapList.size() < userCnt-1){
                            Bitmap basicBitmap = createBasicIconBitmap();
                            bitmapList.add(basicBitmap);
                        }
                        Bitmap combinedBitmap = combineBitmap(bitmapList);

                        if(userCnt == 2){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
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
                            });
                        }else if(userCnt >= 3){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    RequestOptions requestOptions = new RequestOptions().centerCrop();
                                    Glide
                                        .with(getActivity())
                                        .load(combinedBitmap)
                                        .apply(requestOptions)
                                        /** Glide는 원본 비율을 유지한다. */
                                        .override(100,100)
                                        .into(itemBinding.imageViewProfile);
                                }
                            });
                        }
                    }
                });
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
                CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.userListRecyclerview.getAdapter();
                List<ChatRoomListDTO> dataList = (List<ChatRoomListDTO>) adapter.getDataList();

                ChatRoomListDTO chatRoomListDTO = dataList.get(position);
                int roomId = chatRoomListDTO.getRoomId();
                String friendId = chatRoomListDTO.getFriendId();
                String friendName = chatRoomListDTO.getFriendName();

                Intent intent = new Intent(getActivity(), ChatRoomActivity.class);

                // 해당 방 번호, 방 유형, 상대방 Id 를 넘겨준다.
                intent.putExtra(ROOMID, roomId);
                intent.putExtra(ROOM_TYPE_ID, chatRoomListDTO.getRoomType());
                intent.putExtra(OTHER_USER_ID, friendId);
                intent.putExtra(OTHER_USER_NAME, friendName);
                intent.putExtra(ROOM_NAME, friendName);

                startActivity(intent);
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

        // 방 데이터 읽어오기
        getRoomList();

        /**
         * 채팅방에서 나왔을때 OutDate Update 가 완료된 시점에 방 리스트를 다시 읽는다.
         */
        ChatRoomActivity.setChatRoomEvent(new ChatRoomActivity.ChatRoomEvent() {
            @Override
            public void onOutRoomDateComplete() {
                // 방 데이터 읽어오기
                getRoomList();
            }

            @Override
            public void onInRoomDateComplete() {
                // 방 데이터 읽어오기
                getRoomList();
            }
        });

        binding.searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchText = binding.searchEdit.getText().toString();
                CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.userListRecyclerview.getAdapter();
                adapter.getFilter().filter(searchText);
            }
        });

        binding.addGroupChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), GroupChatCreateActivity.class);
                groupRoomCreateLauncher.launch(intent);
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void settingChatRoomList(List<ChatRoomListDTO> dtoList){
        if(binding != null){
            CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.userListRecyclerview.getAdapter();
            adapter.updateList(dtoList);

            String searchText = binding.searchEdit.getText().toString();
            if(!searchText.isEmpty()){
                adapter.getFilter().filter(searchText);
            }else{
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void getRoomList(){
        /** 최신 채팅방 리스트 받아온다. */
        Call<List<ChatRoomListDTO>> call = RetrofitService.getInstance().getService().getChatRoomList();
        call.enqueue(new Callback<List<ChatRoomListDTO>>() {
            @Override
            public void onResponse(Call<List<ChatRoomListDTO>> call, Response<List<ChatRoomListDTO>> response) {
                List<ChatRoomListDTO> dtoList = response.body();
                Log.d(TAG, "[전]dtoList:" + dtoList);
                settingChatRoomList(dtoList);
                Log.d(TAG, "[후]dtoList:" + dtoList);
            }
            @Override
            public void onFailure(Call<List<ChatRoomListDTO>> call, Throwable t) {
                // 오류 처리
                Log.d(TAG, t.getMessage());
                Toast.makeText(getActivity(), "채팅방 목록을 불러오는데 실패했습니다.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Bitmap> convertBitmap(String[] profileArray){
        List<Bitmap> bitmapList = new ArrayList<>();

        try {
            RequestOptions requestOptions = new RequestOptions().circleCrop();

            for(int i=0; i<profileArray.length; i++){
                String profileImgPath = profileArray[i];

                Bitmap bitmap = Glide.with(getActivity())
                        .asBitmap()
                        .load("https://webrtc-sfu.kro.kr/" + profileImgPath)
                        .apply(requestOptions)
                        .submit()
                        .get();

                bitmapList.add(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

        return bitmapList;
    }

    private Bitmap createBasicIconBitmap(){
        Bitmap bitmap = null;
        try{

            RequestOptions requestOptions = new RequestOptions().circleCrop();

            bitmap = Glide.with(getActivity())
                    .asBitmap()
                    .load(R.mipmap.ic_launcher)
                    .apply(requestOptions)
                    .submit()
                    .get();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return bitmap;
    }

    private Bitmap combineBitmap(List<Bitmap> bitmapList){
        int combinedWidth = bitmapList.get(0).getWidth();
        int combinedHeight = bitmapList.get(0).getHeight();
        Bitmap combinedBitmap = Bitmap.createBitmap(combinedWidth, combinedHeight, bitmapList.get(0).getConfig());
        Canvas canvas = new Canvas(combinedBitmap);

        if(bitmapList.size() == 1) {
            Bitmap bitmap = bitmapList.get(0);
            canvas.drawBitmap(bitmap, 0, 0, null);
        }else if(bitmapList.size() == 2){
            Bitmap bitmapLeft = bitmapList.get(0);
            bitmapLeft = Bitmap.createScaledBitmap(bitmapLeft, bitmapLeft.getWidth() / 2, bitmapLeft.getHeight() / 2, true);

            Bitmap bitmapRight = bitmapList.get(1);
            bitmapRight = Bitmap.createScaledBitmap(bitmapRight, bitmapRight.getWidth() / 2, bitmapRight.getHeight() / 2, true);

            canvas.drawBitmap(bitmapLeft, 0, 0, null);
            canvas.drawBitmap(bitmapRight, combinedWidth/2, 0, null);

        }else if(bitmapList.size() == 3){
            Bitmap bitmapLeft = bitmapList.get(0);
            bitmapLeft = Bitmap.createScaledBitmap(bitmapLeft, bitmapLeft.getWidth() / 2, bitmapLeft.getHeight()/2, true);

            Bitmap bitmapRight = bitmapList.get(1);
            bitmapRight = Bitmap.createScaledBitmap(bitmapRight, bitmapRight.getWidth() / 2, bitmapRight.getHeight()/2, true);

            Bitmap bitmapBottom = bitmapList.get(2);
            bitmapBottom = Bitmap.createScaledBitmap(bitmapBottom, bitmapBottom.getWidth() / 2, bitmapBottom.getHeight()/2, true);

            canvas.drawBitmap(bitmapLeft, 0, 0, null);
            canvas.drawBitmap(bitmapRight, combinedWidth/2, 0, null);
            canvas.drawBitmap(bitmapBottom, 0, combinedHeight/2, null);

        }else if(bitmapList.size() >= 4){
            Bitmap bitmapTopLeft = bitmapList.get(0);
            bitmapTopLeft = Bitmap.createScaledBitmap(bitmapTopLeft, bitmapTopLeft.getWidth() / 2, bitmapTopLeft.getHeight()/2, true);

            Bitmap bitmapTopRight = bitmapList.get(1);
            bitmapTopRight = Bitmap.createScaledBitmap(bitmapTopRight, bitmapTopRight.getWidth() / 2, bitmapTopRight.getHeight()/2, true);

            Bitmap bitmapBottomLeft = bitmapList.get(2);
            bitmapBottomLeft = Bitmap.createScaledBitmap(bitmapBottomLeft, bitmapBottomLeft.getWidth() / 2, bitmapBottomLeft.getHeight()/2, true);

            Bitmap bitmapBottomRight = bitmapList.get(3);
            bitmapBottomRight = Bitmap.createScaledBitmap(bitmapBottomRight, bitmapBottomRight.getWidth() / 2, bitmapBottomRight.getHeight()/2, true);

            canvas.drawBitmap(bitmapTopLeft, 0, 0, null);
            canvas.drawBitmap(bitmapTopRight, combinedWidth/2, 0, null);
            canvas.drawBitmap(bitmapBottomLeft, 0, combinedHeight/2, null);
            canvas.drawBitmap(bitmapBottomRight, combinedWidth/2, combinedHeight/2, null);
        }

//        for(Bitmap bitmap : bitmapList){
//            bitmap.recycle();
//        }
        return combinedBitmap;
    }
}