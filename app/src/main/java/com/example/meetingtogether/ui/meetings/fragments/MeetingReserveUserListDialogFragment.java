package com.example.meetingtogether.ui.meetings.fragments;

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.example.meetingtogether.MainActivity.TAG;
import static com.example.meetingtogether.sharedPreference.SharedPreferenceRepository.gson;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.meetingtogether.R;
import com.example.meetingtogether.common.CommonRecyclerView;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.FragmentMeetingReserveUserListDialogBinding;
import com.example.meetingtogether.databinding.FragmentUserListDialogBinding;
import com.example.meetingtogether.databinding.UserRowItemBinding;
import com.example.meetingtogether.databinding.UserRowSelectItemBinding;
import com.example.meetingtogether.dialogs.CustomDialog;
import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.model.MessageDTO;
import com.example.meetingtogether.model.ProfileMap;
import com.example.meetingtogether.model.User;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.sharedPreference.SharedPreferenceRepository;
import com.example.meetingtogether.ui.chats.GroupChatCreateActivity;
import com.example.meetingtogether.ui.meetings.DTO.UserModel;
import com.example.meetingtogether.ui.meetings.MeetingRoomActivity;
import com.google.gson.internal.LinkedTreeMap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeetingReserveUserListDialogFragment extends DialogFragment {
    private MeetingReserveUserListDialogFragment.Listener listener;
    private FragmentMeetingReserveUserListDialogBinding binding;
    private List<Contact> selectedContactList = new ArrayList<>();

    public interface Listener{
        void onCompleteClick(List<Contact> selectedContactList);
    }

    public MeetingReserveUserListDialogFragment(MeetingReserveUserListDialogFragment.Listener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        binding = FragmentMeetingReserveUserListDialogBinding.inflate(inflater);

        binding.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        binding.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onCompleteClick(selectedContactList);
                dismiss();
            }
        });

        binding.searchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchText = binding.searchUser.getText().toString();
                CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.userListRecyclerview.getAdapter();
                adapter.getFilter().filter(searchText);
            }
        });

        // 리사이클러뷰
        RecyclerView recyclerView = binding.userListRecyclerview;

        // 리사이클러뷰 바인드
        CommonRecyclerView commonRecyclerView = new CommonRecyclerView(new CommonRecyclerView.OnBind() {
            // TODO: ViewBind 변경
            // ViewBind 연동
            @Override
            public void onBindViewListener(CommonRecyclerView.MyRecyclerAdapter.ViewHolder viewHolder, View view) {
                viewHolder.setBinding(UserRowSelectItemBinding.bind(view));
            }
            // TODO: ViewBind 변경
            // 실제 View 와 데이터 연동
            @Override
            public void onBindViewHolderListener(CommonRecyclerView.MyRecyclerAdapter.ViewHolder holder, int position) {
                CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.userListRecyclerview.getAdapter();
                List<Contact> filteredList = (List<Contact>) adapter.getFilteredList();

                UserRowSelectItemBinding binding = (UserRowSelectItemBinding)(holder.binding);
                Contact selectedContact = filteredList.get(position);
                binding.profileName.setText(selectedContact.getFriendName());

                String imgPath = null;
                // 프로필 이미지만 필터링
                if(selectedContact.getFriendImgPaths().size() > 0) {
                    ProfileMap userProfileMap = filteredList.get(position).getFriendImgPaths().stream().filter(profileMap -> profileMap.getType().equals(CustomDialog.Type.PROFILE_IMAGE.name())).findFirst().orElse(null);
                    if(userProfileMap != null){
                        imgPath = userProfileMap.getProfileImgPath();
                    }
                }

                RequestOptions requestOptions = new RequestOptions().circleCrop();

                Glide
                    .with(getActivity())
                    .load(imgPath == null ? R.mipmap.ic_launcher : "https://webrtc-sfu.kro.kr/" + imgPath)
                    .apply(requestOptions)
                    /** Glide는 원본 비율을 유지한다. */
                    .override(500,500)
                    .into(binding.imageViewProfile);

                boolean isExist = selectedContactList.stream().anyMatch(contact -> contact.getFriendId().equals(selectedContact.getFriendId()));
                if(isExist){
                    binding.userCheckBox.setChecked(true);
                }

                binding.userCheckBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isCheck = binding.userCheckBox.isChecked();

                        if(isCheck){
                            selectedContactList.add(selectedContact);
                        }else{
                            selectedContactList.remove(selectedContact);
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
                List<Contact> dataList = (List<Contact>) adapter.getDataList();
                Contact selectedContact = dataList.get(position);

                UserRowSelectItemBinding userRowSelectItemBinding = ((UserRowSelectItemBinding)pBinding);


                boolean isCheck = userRowSelectItemBinding.userCheckBox.isChecked();

                if(isCheck){
                    userRowSelectItemBinding.userCheckBox.setChecked(false);
                    selectedContactList.remove(selectedContact);
                }else{
                    userRowSelectItemBinding.userCheckBox.setChecked(true);
                    selectedContactList.add(selectedContact);
                }

                Log.d(TAG, "selectedContactList:" + selectedContactList);
            }
            @Override
            public void onItemLongClickListener(View view, int position) {
                Toast.makeText(getActivity(), position + " long 클릭",Toast.LENGTH_LONG).show();
            }
        });

        commonRecyclerView.setContext(getActivity());
        // TODO: 데이터 변경
        // 데이터 세팅
        commonRecyclerView.setDataList(Util.contactList);
        commonRecyclerView.setRecyclerView(recyclerView);
        // TODO: row item 레이아웃 변경
        // row item 레이아웃 세팅
        commonRecyclerView.setRowItem(R.layout.user_row_select_item);
        // 적용
        commonRecyclerView.adapt();

        CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.userListRecyclerview.getAdapter();
        adapter.notifyDataSetChanged();


        builder.setView(binding.userListDialog);
        return builder.create();
    }

    public void update(List<Contact> contactList){
        selectedContactList.clear();
        selectedContactList.addAll(contactList);
    }
}
