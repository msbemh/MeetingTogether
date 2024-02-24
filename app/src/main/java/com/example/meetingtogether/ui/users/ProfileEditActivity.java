package com.example.meetingtogether.ui.users;

import static com.example.meetingtogether.MainActivity.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.meetingtogether.R;
import com.example.meetingtogether.common.Util;
import com.example.meetingtogether.databinding.ActivityProfileBinding;
import com.example.meetingtogether.databinding.ActivityProfileEditBinding;
import com.example.meetingtogether.dialogs.CustomDialog;
import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.model.ProfileMap;
import com.example.meetingtogether.retrofit.CommonRetrofitResponse;
import com.example.meetingtogether.retrofit.FileInfo;
import com.example.meetingtogether.retrofit.RetrofitResponse;
import com.example.meetingtogether.retrofit.RetrofitService;
import com.example.meetingtogether.sharedPreference.SharedPreferenceRepository;
import com.example.meetingtogether.ui.meetings.CustomPeerConnection;
import com.example.meetingtogether.ui.meetings.DTO.MessageModel;
import com.example.meetingtogether.ui.meetings.DTO.UserModel;
import com.example.meetingtogether.ui.meetings.MeetingRoomActivity;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import org.webrtc.DataChannel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileEditActivity extends AppCompatActivity {

    private ActivityProfileEditBinding binding;

    private CustomDialog customDialog;

    private Bitmap backgroundBitmap = null;
    private Bitmap profileBitmap = null;
    private CustomDialog.Type type = null;

    private String userId;
    private String friendId;
    private String profileImagePath;
    private String backgroundImagePath;

    private ProfileMap userProfileMap;
    private ProfileMap backgroundProfileMap;
    private Handler handler;

    /**
     * 필요한 권한
     */
    private final String[] PERMISSIONS = {
            Manifest.permission.CAMERA
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
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
        localBuilder.setTitle("권한 설정")
                .setMessage("권한 거절로 인해 일부기능이 제한됩니다.")
                .setPositiveButton("권한 설정하러 가기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(Uri.parse("package:" + getPackageName()));
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
                        Toast.makeText(ProfileEditActivity.this, "권한을 취소하셨습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .create()
                .show();
    }

    /**
     * Camera 에서 이미지를 선택 했을 경우
     * Intent Result Callback
     */
    private ActivityResultLauncher activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            if(result.getResultCode() == RESULT_OK){
                Intent intent = result.getData();
                Bundle extras = intent.getExtras();

                if(type == CustomDialog.Type.BACKGROUND_IMAGE) {
                    backgroundBitmap = (Bitmap) extras.get("data");
                    // 이미지 표시
                    if(backgroundBitmap != null) settingImage(backgroundBitmap);
                }else if(type == CustomDialog.Type.PROFILE_IMAGE) {
                    profileBitmap = (Bitmap) extras.get("data");
                    // 이미지 표시
                    if(profileBitmap != null) settingImage(profileBitmap);
                }
            // 취소
            }else {
                //Toast.makeText(ProfileEditActivity.this, "취소 됐습니다.", Toast.LENGTH_SHORT).show();
            }

            customDialog.dismiss();
        }
    });



    /**
     * 앨범에서 이미지를 선택 했을 경우
     * Intent Callback
     */
    private ActivityResultLauncher activityAlbumResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == RESULT_OK){
                Intent intent = result.getData();
                ClipData clipData = intent.getClipData();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri imageUri = clipData.getItemAt(i).getUri();  // 선택한 이미지들의 uri를 가져온다.
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(ProfileEditActivity.this.getContentResolver(), imageUri);

                                /** 사이즈 max 500 기준으로 리사이징 (비율 유지) */
                                bitmap = resizeBitmapImage(bitmap);

                                /** Width > Height 면, 이미지를 회전 시킨다. */
                                ExifInterface exifInterface = new ExifInterface(getContentResolver().openInputStream(imageUri));
                                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                                // 방향 정보에 따라 이미지를 회전 또는 뒤집을 수 있습니다.
                                Matrix rotateMatrix = new Matrix();
                                switch (orientation) {
                                    case ExifInterface.ORIENTATION_ROTATE_90:
                                        rotateMatrix.postRotate(90);
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_180:
                                        rotateMatrix.postRotate(180);
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_270:
                                        rotateMatrix.postRotate(270);
                                        break;
                                    case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                                        // 이미지를 수평으로 뒤집음
                                        break;
                                    case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                                        // 이미지를 수직으로 뒤집음
                                        break;
                                }

                                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotateMatrix, false);

                                if(type == CustomDialog.Type.BACKGROUND_IMAGE) {
                                    backgroundBitmap = bitmap;

                                }else if(type == CustomDialog.Type.PROFILE_IMAGE) {
                                    profileBitmap = bitmap;
                                }

                                if(bitmap != null) {
                                    Bitmap finalBitmap = bitmap;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            settingImage(finalBitmap);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "File select error", e);
                            }
                        }
                    }
                });

            // 취소
            }else {
                Toast.makeText(ProfileEditActivity.this, "취소 됐습니다.", Toast.LENGTH_SHORT).show();
            }

            customDialog.dismiss();
        }
    });

    private Bitmap getBitmapFromUri(Uri uri) {
        Bitmap bitmap = null;
        try{
            // URI를 비트맵으로 변환
            InputStream inputStream = getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        return bitmap;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        friendId = intent.getStringExtra("friendId");

        HandlerThread handlerChatThread = new HandlerThread("thread");
        handlerChatThread.start();
        handler = new Handler(handlerChatThread.getLooper());

        // 친구의 경우
        if(friendId != null){
            // 프로필 세팅
            Contact friendContact = Util.contactList.stream().filter(contact -> contact.getFriendId().equals(friendId)).findAny().orElse(null);
            if(friendContact != null) {
                List<ProfileMap> profileMapList = friendContact.getFriendImgPaths();
                if(profileMapList != null){
                    userProfileMap = profileMapList.stream().filter(profileMap -> profileMap.getType().equals(CustomDialog.Type.PROFILE_IMAGE.name())).findFirst().orElse(null);
                    backgroundProfileMap = profileMapList.stream().filter(profileMap -> profileMap.getType().equals(CustomDialog.Type.BACKGROUND_IMAGE.name())).findFirst().orElse(null);
                }
                // 이름 세팅
                binding.profileName.setText(friendContact.getFriendName());
            }
            // 사용자 본인의 경우
        }else if(userId != null){
            // 프로필 세팅
            List<ProfileMap> profileMapList = Util.user.getProfileImgPaths();
            if(profileMapList != null) {
                userProfileMap = profileMapList.stream().filter(profileMap -> profileMap.getType().equals(CustomDialog.Type.PROFILE_IMAGE.name())).findFirst().orElse(null);
                backgroundProfileMap = profileMapList.stream().filter(profileMap -> profileMap.getType().equals(CustomDialog.Type.BACKGROUND_IMAGE.name())).findFirst().orElse(null);
            }
            // 이름 세팅
            binding.profileName.setText(Util.user.getNickName());
        }

        customDialog = new CustomDialog(this);

        customDialog.setInterface(new CustomDialog.CustomDialogClickInterface() {
            @Override
            public void albumClick() {
                dispatchTakePictureAlbumIntentIntent();
            }

            @Override
            public void cameraClick() {
                dispatchTakePictureCameraIntent();
            }
        });

        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog.setTitle("프로필 이미지 가져오기");
                type = CustomDialog.Type.PROFILE_IMAGE;
                customDialog.show();
            }
        });
        binding.backgroundImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog.setTitle("배경 이미지 가져오기");
                type = CustomDialog.Type.BACKGROUND_IMAGE;
                customDialog.show();
            }
        });

        binding.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                if(backgroundBitmap == null && profileBitmap == null){
//                    Toast.makeText(ProfileEditActivity.this, "업데이트할 게 없습니다.", Toast.LENGTH_SHORT).show();
//                    return;
//                }

                try {
                    MultipartBody.Part profileImgPart = convertMultipartBody(profileBitmap, CustomDialog.Type.PROFILE_IMAGE);
                    MultipartBody.Part backgroundImgPart = convertMultipartBody(backgroundBitmap, CustomDialog.Type.BACKGROUND_IMAGE);

                    /** 사용자명 기타 정보 추가하기  */
                    Map map = new HashMap();
                    map.put("name", binding.profileName.getText().toString());
                    Gson gson = new Gson();
                    String jsonString = gson.toJson(map);

                    RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), jsonString);
                    sendProfile(profileImgPart, backgroundImgPart, requestBody);

//                    if(backgroundBitmap != null) {
//                        MultipartBody.Part part = convertMultipartBody(backgroundBitmap);
//                        sendProfile(part, CustomDialog.Type.BACKGROUND_IMAGE);
//                    }
//                    if(profileBitmap != null){
//                        MultipartBody.Part part = convertMultipartBody(profileBitmap);
//                        sendProfile(part, CustomDialog.Type.PROFILE_IMAGE);
//                    }
                } catch (Exception e) {
                    Log.e(TAG, "File select error", e);
                }

            }
        });

        // 프로필 이미지 로드
        Util.loadProfile(this, binding.profileImage, userProfileMap, CustomDialog.Type.PROFILE_IMAGE);

        // 배경 이미지 로드
        Util.loadProfile(this, binding.backgroundImage, backgroundProfileMap, CustomDialog.Type.BACKGROUND_IMAGE);

        permissionLauncher.launch(PERMISSIONS);
    }

    private MultipartBody.Part convertMultipartBody(Bitmap bitmap, CustomDialog.Type type){
        if(bitmap == null) return null;
        /** 사이즈 max 500 기준으로 리사이징 (비율 유지) */
        bitmap = resizeBitmapImage(bitmap);

        Log.d(TAG, "bitmap:" + bitmap);
        File file = saveBitmapToJpeg(bitmap, ProfileEditActivity.this);

        // MultipartBody.Part로 파일 생성
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        MultipartBody.Part part = null;
        /** 프로필 이미지 */
        if(type == CustomDialog.Type.PROFILE_IMAGE){
            part = MultipartBody.Part.createFormData("update-profile-image", file.getName(), requestBody);
        /** 백그라운드 이미지*/
        }else if(type == CustomDialog.Type.BACKGROUND_IMAGE){
            part = MultipartBody.Part.createFormData("update-background-image", file.getName(), requestBody);
        }

        return part;
    }

    private void sendProfile(MultipartBody.Part profileImgPart, MultipartBody.Part backgroundImgPart, RequestBody requestBody){
        Call<CommonRetrofitResponse> call = RetrofitService.getInstance().getService().postProfileImages(profileImgPart, backgroundImgPart, requestBody);
        call.enqueue(new Callback<CommonRetrofitResponse>() {
            @Override
            public void onResponse(Call<CommonRetrofitResponse> call, Response<CommonRetrofitResponse> response) {
                // 응답 처리
                if (response.isSuccessful()) {

                    LinkedTreeMap data = (LinkedTreeMap) response.body().getData();

                    LinkedTreeMap info = (LinkedTreeMap) data.get("info");
                    String userName = info.get("name").toString();
                    Util.user.setName(userName);
                    Util.user.setNickName(userName);

                    List<LinkedTreeMap> files = (List<LinkedTreeMap>) data.get("files");

                    for(LinkedTreeMap fileMap : files){
                        String type = fileMap.get("type").toString();
                        String profileImgPath = fileMap.get("profileImgPath").toString();

                        // 프로필 추가 (맨 앞에)
                        Util.user.getProfileImgPaths().add(0, new ProfileMap(profileImgPath, type));
                    }

                    // 쉐어드에 저장
                    SharedPreferenceRepository.saveUserForAutoLogin(Util.user);

                    Toast.makeText(ProfileEditActivity.this, "저장이 완료됐습니다.", Toast.LENGTH_SHORT).show();

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

    private void dispatchTakePictureCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            activityResultLauncher.launch(intent);
        }
    }

    private void settingImage(Bitmap bitmap){
        if(bitmap == null){
//            binding.imageViewProfile.setImageResource(R.drawable.calendar_manage_logo);
            return;
        }


        if(type == CustomDialog.Type.BACKGROUND_IMAGE){
            binding.backgroundImage.setImageBitmap(bitmap);
            return;
        }

        /**
         * 가져온 Bitmap 원형으로 자르기
         */

        // bitmap 의 width 와 height 중 작은 값을 선택한다.
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());

        /**
         * [Bitmap.createBitmap]
         * 비트맵 객체를 생성하는 기능
         *
         * [Bitmap.Config.ARGB_8888]
         * 비트맵의 픽셀 형식중 하나 입니다.
         * 각 픽셀을 8비트로 나타냄.
         * 투명도, 빨, 초, 파 채널 이 있기 때문에, 총 8 * 4 = 32비트(4바이트) 사용
         */
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        /**
         * Canvas 를 조작 하면 해당 픽셀들이 bitmap에 저장이 된다.
         */
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        /**
         * [BitmapShader]
         * 비트맵을 텍스처로 그리는데 사용
         *
         * TileMode
         * CLAMP : 무늬 끝을 계속 반복
         * MIRROR : 무늬를 반사 시켜 계속 반복
         * REPEAT : 똑같은 무늬를 계속 반복
         *
         * 이곳에서 가져온 bitmap 을 사용하도록 설정
         */
        Shader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);

        /**
         * [Canvas 클래스의 drawCircle()]
         * 지정된 좌표를 중심으로 원을 그리는 데 사용.
         * cx : 원의 중심 x 좌표
         * cy : 원의 중심 y 좌표
         * radius : 반지름
         * paint : 그리기를 사용할 Paint 객체
         */
        canvas.drawCircle(size / 2, size / 2, size / 2, paint);

        if(type == CustomDialog.Type.PROFILE_IMAGE){
            binding.profileImage.setImageBitmap(output);
        }

    }

    private String createImageFileName(){
        String fileName = null;
        try{
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            fileName = "JPEG_" + timeStamp + "_";
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

        return fileName;
    }

    private String saveImageFile(){
        String imageFileName = "";
        String relativePath = "Pictures/MeetingTogether";
        String fileName = createImageFileName();
        String type = "jpg";
        OutputStream outputStream = null;
        try {
            /**
             * [MediaStore]
             * 안드로이드에서 제공하는 멀티미디어 파일 정보를 저장하는 데이터베이스.
             * 이 데이터베이스를 이용하여 이미지, 비디오, 오디오 등의 파일 정보를 가져오거나 저장할 수 있습니다.
             *
             * [ContentValues]
             * 데이터 베이스에 저장할 데이터를 Key-Value 형태로 저장하는 클래스입니다.
             */
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, relativePath);
            //values.put(MediaStore.Images.Media.DATA, tempFile.getAbsolutePath());

            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            outputStream = getContentResolver().openOutputStream(uri);

//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            outputStream.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "파일을 찾을 수 없음");

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        } finally {
            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        imageFileName = fileName + "." + type;

        return imageFileName;
    }

    int MAX_HEIGHT = 500;
    int MAX_WIDTH = 500;

    // 리샘플링 값 계산 : 타겟 너비와 높이를 기준으로 2의 거듭제곱인 샘플 크기 값을 계산
    private Bitmap resizeBitmapImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int inSampleSize = 1;

        int halfHeight = 0;
        int halfWidth = 0;
        if (height > MAX_HEIGHT || width > MAX_WIDTH) {
            halfHeight = height / 2;
            halfWidth = width / 2;

            while (halfHeight / inSampleSize >= MAX_HEIGHT && halfWidth / inSampleSize >= MAX_WIDTH) {
                inSampleSize *= 2;
            }
        }else{
            halfWidth = width;
            halfHeight = height;
        }

        return Bitmap.createScaledBitmap(bitmap, halfWidth / inSampleSize, halfHeight / inSampleSize, true);
    }

    // 비트맵을 파일로 변환하는 메소드
    public File saveBitmapToJpeg(Bitmap bitmap, Context context) {

        //내부저장소 캐시 경로를 받아옵니다.
        File storage = context.getCacheDir();

        //저장할 파일 이름
        String fileName = String.valueOf(System.currentTimeMillis()) + ".jpg";

        //storage 에 파일 인스턴스를 생성합니다.
        File imgFile = new File(storage, fileName);

        try {

            // 자동으로 빈 파일을 생성합니다.
            imgFile.createNewFile();

            // 파일을 쓸 수 있는 스트림을 준비합니다.
            FileOutputStream out = new FileOutputStream(imgFile);

            // compress 함수를 사용해 스트림에 비트맵을 저장합니다.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            bitmap.recycle();

            // 스트림 사용후 닫아줍니다.
            out.close();

            return imgFile;

        } catch (FileNotFoundException e) {
            Log.e("MyTag", "FileNotFoundException : " + e.getMessage());
        } catch (IOException e) {
            Log.e("MyTag", "IOException : " + e.getMessage());
        }

        return imgFile;
    }

    private void dispatchTakePictureAlbumIntentIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);  // 1개 이미지를 가져올 수 있도록 세팅
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityAlbumResultLauncher.launch(intent);

    }


}