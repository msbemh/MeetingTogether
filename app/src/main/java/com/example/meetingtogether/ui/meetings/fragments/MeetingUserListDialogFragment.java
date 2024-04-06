package com.example.meetingtogether.ui.meetings.fragments;

import static android.content.Context.CLIPBOARD_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import static com.example.meetingtogether.MainActivity.TAG;
import static com.example.meetingtogether.sharedPreference.SharedPreferenceRepository.gson;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.meetingtogether.R;
import com.example.meetingtogether.common.ColorType;
import com.example.meetingtogether.common.CommonRecyclerView;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.FragmentMeetinglistBinding;
import com.example.meetingtogether.databinding.FragmentUserListDialogBinding;
import com.example.meetingtogether.databinding.UserRowItemBinding;
import com.example.meetingtogether.dialogs.CustomDialog;
import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.model.MessageDTO;
import com.example.meetingtogether.model.ProfileMap;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.FileInfo;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.ui.chats.ChatRoomActivity;
import com.example.meetingtogether.ui.meetings.DTO.ColorModel;
import com.example.meetingtogether.ui.meetings.DTO.UserModel;
import com.example.meetingtogether.ui.meetings.MeetingRoomActivity;
import com.example.meetingtogether.ui.users.ProfileActivity;
import com.google.gson.internal.LinkedTreeMap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeetingUserListDialogFragment extends DialogFragment {
    private MeetingUserListDialogFragment.Listener listener;
    private FragmentUserListDialogBinding binding;
    private List<UserModel> userList;
    private Handler qrCodeHandler;
    private String roomId = null;

    public interface Listener{
        void onClick(UserModel selectedUserModel);
    }

    public MeetingUserListDialogFragment(List<UserModel> userList, MeetingUserListDialogFragment.Listener listener){
        this.userList = userList;
        this.listener = listener;
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {F
//        binding = FragmentUserListDialogBinding.inflate(inflater, container, false);
//        View root = binding.getRoot();
//        return root;
//    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        HandlerThread handlerChatThread = new HandlerThread("qrCodeHandler");
        handlerChatThread.start();
        qrCodeHandler = new Handler(handlerChatThread.getLooper());


        LayoutInflater inflater = requireActivity().getLayoutInflater();
        binding = FragmentUserListDialogBinding.inflate(inflater);

        binding.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        binding.inviteLinkCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                roomId = ((MeetingRoomActivity)getActivity()).roomId;
                String roomName = ((MeetingRoomActivity)getActivity()).roomName;

                // 복사할 텍스트 생성
                String meetingUrl = "https://song-one-link.onelink.me/VZvV/er7m9da1?roomId=" + roomId + "&roomName=" + roomName;

                // 클립보드 매니저 가져오기
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);

                // 클립 데이터 생성
                ClipData clip = ClipData.newPlainText("meetingUrl", meetingUrl);

                // 클립보드에 데이터 설정
                clipboard.setPrimaryClip(clip);

            }
        });

        binding.qrCodeCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                roomId = ((MeetingRoomActivity)getActivity()).roomId;
                String roomName = ((MeetingRoomActivity)getActivity()).roomName;

                // 복사할 텍스트 생성
                String meetingUrl = "https://song-one-link.onelink.me/VZvV/er7m9da1?roomId=" + roomId + "&roomName=" + roomName;

                try {
                    Bitmap bitmap = encodeAsBitmap(meetingUrl);
                    qrCodeImageUpload(bitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }


            }
        });

        // 리사이클러뷰
        RecyclerView recyclerView = binding.recyclerView;

        // 리사이클러뷰 바인드
        CommonRecyclerView commonRecyclerView = new CommonRecyclerView(new CommonRecyclerView.OnBind() {
            // TODO: ViewBind 변경
            // ViewBind 연동
            @Override
            public void onBindViewListener(CommonRecyclerView.MyRecyclerAdapter.ViewHolder viewHolder, View view) {
                viewHolder.setBinding(UserRowItemBinding.bind(view));
            }
            // TODO: ViewBind 변경
            // 실제 View 와 데이터 연동
            @Override
            public void onBindViewHolderListener(CommonRecyclerView.MyRecyclerAdapter.ViewHolder holder, int position) {
                CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.recyclerView.getAdapter();
                List<UserModel> filteredList = (List<UserModel>) adapter.getFilteredList();
                UserModel userModel = filteredList.get(position);

                String email = userModel.getEmail();
                boolean isHost = userModel.isHost();

                UserRowItemBinding binding = (UserRowItemBinding)(holder.binding);
                binding.profileName.setText(userModel.getName());

                binding.profileMessage.setText(userModel.getEmail());

                if(isHost){
                    binding.hostImageView.setVisibility(View.VISIBLE);
                }else{
                    binding.hostImageView.setVisibility(View.GONE);
                }

                String imgPath = userModel.getImgPath();
                Util.loadProfile(getActivity(), binding.imageViewProfile, imgPath, CustomDialog.Type.PROFILE_IMAGE);
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
                CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.recyclerView.getAdapter();
                List<UserModel> dataList = (List<UserModel>) adapter.getDataList();
                UserModel selectedUserModel = dataList.get(position);
                MeetingUserListDialogFragment.this.listener.onClick(selectedUserModel);
            }
            @Override
            public void onItemLongClickListener(View view, int position) {
                Toast.makeText(getActivity(), position + " long 클릭",Toast.LENGTH_LONG).show();
            }
        });

        commonRecyclerView.setContext(getActivity());
        // TODO: 데이터 변경
        // 데이터 세팅
        commonRecyclerView.setDataList(userList);
        commonRecyclerView.setRecyclerView(recyclerView);
        // TODO: row item 레이아웃 변경
        // row item 레이아웃 세팅
        commonRecyclerView.setRowItem(R.layout.user_row_item);
        // 적용
        commonRecyclerView.adapt();

        CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.recyclerView.getAdapter();
        adapter.notifyDataSetChanged();


        builder.setView(binding.userListDialog);
        return builder.create();
    }

    private Bitmap encodeAsBitmap(String text) throws WriterException {
        Bitmap bitmap = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try{
            BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE,200,200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
        }catch (Exception e){}
        return bitmap;
    }

    private void qrCodeImageUpload(Bitmap bitmap){

        qrCodeHandler.post(new Runnable() {
            @Override
            public void run() {
                File file = Util.saveBitmapToJpeg(bitmap, getActivity());
                // MultipartBody.Part로 파일 생성
                RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), file);
                MultipartBody.Part part = MultipartBody.Part.createFormData("photo" + 0, file.getName(), requestBody);

                ArrayList<MultipartBody.Part> files = new ArrayList<>();
                files.add(part);

                MessageDTO sendMsgDTO = new MessageDTO();
                sendMsgDTO.setRoomUuid(Integer.valueOf(roomId));
                String jsonString = gson.toJson(sendMsgDTO);
                RequestBody info = RequestBody.create(MediaType.parse("text/plain"), jsonString);

                Call<CommonRetrofitResponse> call = RetrofitService.getInstance().getService().postPureUploadImages(files, info);
                call.enqueue(new Callback<CommonRetrofitResponse>() {
                    @Override
                    public void onResponse(Call<CommonRetrofitResponse> call, Response<CommonRetrofitResponse> response) {
                        // 응답 처리
                        if (response.isSuccessful()) {
                            CommonRetrofitResponse commonRetrofitResponse = response.body();
                            LinkedTreeMap dataMap = (LinkedTreeMap) commonRetrofitResponse.getData();
                            List<LinkedTreeMap> fileInfoList = (List<LinkedTreeMap>) dataMap.get("file_info_list");
                            LinkedTreeMap fileInfo = (LinkedTreeMap) fileInfoList.get(0);
                            String qrCodeFilePath = "https://webrtc-sfu.kro.kr/" + fileInfo.get("path").toString();
;
                            // 클립보드 매니저 가져오기
                            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);

                            // 클립 데이터 생성
                            ClipData clip = ClipData.newPlainText("meetingUrl", qrCodeFilePath);

                            // 클립보드에 데이터 설정
                            clipboard.setPrimaryClip(clip);
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
        });
    }

    public void update(List<UserModel> pUserList){
        MeetingUserListDialogFragment.this.userList = pUserList;
        CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.recyclerView.getAdapter();
        adapter.updateList(pUserList);
        adapter.notifyDataSetChanged();
    }

}
