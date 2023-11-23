package com.example.meetingtogether.ui.users;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.meetingtogether.MainActivity;
import com.example.meetingtogether.R;
import com.example.meetingtogether.common.CommonRecyclerView;
import com.example.meetingtogether.common.MessageRecyclerView;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.FragmentUserlistBinding;
import com.example.meetingtogether.databinding.ReceiveMessageRowItemBinding;
import com.example.meetingtogether.databinding.SendMessageRowItemBinding;
import com.example.meetingtogether.databinding.UserRowItemBinding;
import com.example.meetingtogether.dialogs.CustomDialog;
import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.model.ProfileMap;
import com.example.meetingtogether.model.User;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.sharedPreference.SharedPreferenceRepository;
import com.example.meetingtogether.ui.meetings.DTO.MessageModel;
import com.example.meetingtogether.ui.meetings.MeetingRoomActivity;
import com.example.meetingtogether.ui.user.LoginActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListFragment extends Fragment {

    private FragmentUserlistBinding binding;

    private String TAG = "TEST";

    private List<Contact> dataList;
    private UserRowItemBinding userRowItemBinding;

    /**
     * 필요한 권한
     */
    private final String[] PERMISSIONS = {
            android.Manifest.permission.READ_CONTACTS
    };

    /**
     * 권한 요청에 대한 Callback
     */
    private ActivityResultLauncher permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "" + result.toString());

                    Boolean allGranted = true;

                    // 모든 권한에 동의 했는지 확인
                    // 하나라도 false가 존재한다면 allGranted가 false 됨
                    Iterator iterator = result.keySet().iterator();
                    while (iterator.hasNext()) {
                        String permissionName = iterator.next().toString();

                        boolean isAllowed = result.get(permissionName);
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                            allGranted = allGranted && isAllowed;
                        } else {
                            allGranted = false;
                        }
                    }

                    // 모든 권한에 동의
                    if (allGranted) {
                    // 모든 권한에 동의 하지 않음
                    } else {
                        showPermissionDialog();
                    }
                }
            });

    private void showPermissionDialog() {
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(getActivity());
        localBuilder.setTitle("권한 설정")
                .setMessage("권한 거절로 인해 일부기능이 제한됩니다.")
                .setPositiveButton("권한 설정하러 가기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(Uri.parse("package:" + getActivity().getPackageName()));
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, e.toString());
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                            startActivity(intent);
                        }
                    }
                })
                .setNegativeButton("취소하기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                        Toast.makeText(getActivity(), "권한을 취소하셨습니다.", Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                })
                .create()
                .show();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserlistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.profileName.setText(Util.user.getNickName());

        binding.profileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("userId", Util.user.getId());
                startActivity(intent);
            }
        });

        permissionLauncher.launch(PERMISSIONS);

        binding.syncFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    // 주소록 동기화
                    syncContact();
                }
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
                searchFilter(searchText);

            }
        });

        Util.contactList = SharedPreferenceRepository.getFriendList();
        dataList = Util.contactList;

        // 리사이클러뷰
        RecyclerView userListRecyclerview = binding.userListRecyclerview;

        // 리사이클러뷰 바인드
        CommonRecyclerView commonRecyclerView = new CommonRecyclerView(new CommonRecyclerView.OnBind() {
            // TODO: ViewBind 변경
            // ViewBind 연동
            @Override
            public void onBindViewListener(CommonRecyclerView.MyRecyclerAdapter.ViewHolder viewHolder, View view) {
                viewHolder.setBinding(userRowItemBinding.bind(view));
            }
            // TODO: ViewBind 변경
            // 실제 View 와 데이터 연동
            @Override
            public void onBindViewHolderListener(CommonRecyclerView.MyRecyclerAdapter.ViewHolder holder, int position) {
                UserRowItemBinding binding = (UserRowItemBinding)(holder.binding);
                binding.profileName.setText(dataList.get(position).getFriendName());

                String imgPath = null;
                // 프로필 이미지만 필터링
                if(dataList.get(position).getFriendImgPaths().size() > 0) {
                    ProfileMap userProfileMap = dataList.get(position).getFriendImgPaths().stream().filter(profileMap -> profileMap.getType().equals(CustomDialog.Type.PROFILE_IMAGE.name())).findFirst().orElse(null);
                    if(userProfileMap != null){
                        imgPath = userProfileMap.getProfileImgPath();
                    }
                }

                RequestOptions  requestOptions = new RequestOptions().circleCrop();

                Glide
                    .with(getActivity())
                    .load(imgPath == null ? R.mipmap.ic_launcher : "https://webrtc-sfu.kro.kr/" + imgPath)
                    .apply(requestOptions)
                    /** Glide는 원본 비율을 유지한다. */
                    .override(500,500)
                    .into(binding.imageViewProfile);
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
            public void onItemClickListener(View view, int position) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                String friendId = Util.contactList.get(position).getFriendId();
                intent.putExtra("friendId", friendId);
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
        commonRecyclerView.setDataList(dataList);
        commonRecyclerView.setRecyclerView(userListRecyclerview);
        // TODO: row item 레이아웃 변경
        // row item 레이아웃 세팅
        commonRecyclerView.setRowItem(R.layout.user_row_item);
        // 적용
        commonRecyclerView.adapt();

    }

    @Override
    public void onStart() {
        super.onStart();

        // 프로필 이미지 로드
        ProfileMap profileMap = Util.user.getMyProfileMap();

        Util.loadProfile(getActivity(), binding.imageViewProfile, profileMap, CustomDialog.Type.PROFILE_IMAGE);
        binding.profileName.setText(Util.user.getName());
    }

    private List<Contact> filteredList = new ArrayList<>();

    private void searchFilter(String searchText){
        filteredList.clear();

        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getFriendName().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(dataList.get(i));
            }
        }

        CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.userListRecyclerview.getAdapter();
        adapter.updateList(filteredList);
        adapter.notifyDataSetChanged();
    }

    private void syncContact(){
        List<Contact> contactList = Util.getContactsString(getActivity());
        Log.d(TAG, contactList.toString());

        /** 서버로 친구목록 동기화 요청 */
        Call<CommonRetrofitResponse<List<Contact>>> call = RetrofitService.getInstance().getService().postSyncFriend(contactList);
        call.enqueue(new Callback<CommonRetrofitResponse<List<Contact>>>() {
            @Override
            public void onResponse(Call<CommonRetrofitResponse<List<Contact>>> call, Response<CommonRetrofitResponse<List<Contact>>> response) {
                // 응답 처리
                if (response.isSuccessful() && response.body().isResult()) {

                    Util.contactList = response.body().getData();

                    // 귀찮으니까 그냥 덮어 씌우자 ㅎㅎ
                    SharedPreferenceRepository.saveFriendList(Util.contactList);

                    CommonRecyclerView.MyRecyclerAdapter adapter = (CommonRecyclerView.MyRecyclerAdapter) binding.userListRecyclerview.getAdapter();

                    dataList = Util.contactList;
                    adapter.updateList(dataList);
                    adapter.notifyDataSetChanged();

                    Log.d(TAG, "친구목록 동기화가 완료");
                    Toast.makeText(getActivity(), "친구목록 동기화가 완료 됐습니다.",Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(getActivity(), "친구목록 동기화에 실패했습니다.",Toast.LENGTH_SHORT).show();
                Log.d(TAG, response.message());
                Util.hideDialog();
            }

            @Override
            public void onFailure(Call<CommonRetrofitResponse<List<Contact>>> call, Throwable t) {
                // 오류 처리
                Log.d(TAG, t.getMessage());
                Util.hideDialog();
                Toast.makeText(getActivity(), "로그인에 실패했습니다.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}