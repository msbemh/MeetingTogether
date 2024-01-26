package com.example.meetingtogether.common;

import static android.content.Context.TELEPHONY_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.meetingtogether.R;
import com.example.meetingtogether.dialogs.CustomDialog;
import com.example.meetingtogether.model.Contact;
import com.example.meetingtogether.model.ProfileMap;
import com.example.meetingtogether.model.User;
import com.example.meetingtogether.ui.users.ProfileActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    private static final String TAG = "TEST";

    public static int ACTION_CREATE = 0;
    public static int ACTION_EDIT = 1;
    public static int ACTION_DELETE = 2;

    public static User user = null;
    public static DateTimeFormatter calendarDateTimeFormatter = DateTimeFormatter.ofPattern("EEE'\n'dd MMM'\n'HH:mm");
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.KOREAN);
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final int SUCCESS = 0;
    public static final int FAIL = 1;
    public static final int DELETE_SUCCESS = 2;
    public static final int DELETE_FAIL = 3;
    public static final int READ_SUCCESS = 4;
    public static final int READ_FAIL = 5;
    public static final int UPDATE_SUCCESS = 6;
    public static final int UPDATE_FAIL = 7;
    public static final int CREATE_SUCCESS = 8;
    public static final int CREATE_FAIL = 9;
    public static final int PROCESS_SUCCESS = 10;
    public static final int PROCESS_FAIL = 11;
    public static final int DUP_CHECK_SUCCESS = 12;
    public static final int DUP_CHECK_FAIL = 13;
    public static final int AUTH_EMAIL_NUM_SUCCESS = 14;
    public static final int AUTH_EMAIL_NUM_FAIL = 15;
    public static final int LOGIN_SUCCESS = 16;
    public static final int LOGIN_FAIL = 17;
    public static final int ALREADY_EXIST = 18;
    public static final int GOOGLE_LOGIN_SUCCESS = 19;
    public static final int GOOGLE_LOGIN_FAIL = 20;

    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    public static List<Contact> contactList = new ArrayList<>();

    public static boolean isEmailValid(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }


    public static String[] numStringSplit(String str) {
        String[] strs = str.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        String letters = strs[0];
        String numbers = strs[1];
        return strs;
    }

    public static boolean isAlpha(String s) {
        return s != null && s.matches("^[a-zA-Z\\s]*$");
    }

    public static boolean isProcessService(Context context, String serviceName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String calcDateBeforeNow(Date givenDate) {
        Date now = new Date();

        StringBuilder stringBuilder = new StringBuilder();

        // 시간 차이 계산
        long diffInMsec = Math.abs(now.getTime() - givenDate.getTime());
        long diffInSec = TimeUnit.SECONDS.convert(diffInMsec, TimeUnit.MILLISECONDS);
        long diffInMinutes = TimeUnit.MINUTES.convert(diffInMsec, TimeUnit.MILLISECONDS);
        long diffInHour = TimeUnit.HOURS.convert(diffInMsec, TimeUnit.MILLISECONDS);

        if (diffInHour < 24) {
            int diffCalcSec = (int) diffInSec % 60;
            int diffCalcMinutes = (int) diffInMinutes % 60;
            int diffCalcHour = (int) diffInHour % 60;
            if (diffCalcHour > 0) {
                stringBuilder.append(diffCalcHour + "시간 전");
            } else if (diffCalcMinutes > 0) {
                stringBuilder.append(diffCalcMinutes + "분 전");
            } else if (diffCalcSec > 0) {
                stringBuilder.append(diffCalcSec + "초 전");
            }
        } else {
            return sdf.format(givenDate);
        }
        return stringBuilder.toString();
    }

    public static String calcDateBeforeNowAsMinSec(Date givenDate) {
        Date now = new Date();

        StringBuilder stringBuilder = new StringBuilder();

        // 시간 차이 계산
        long diffInMsec = Math.abs(now.getTime() - givenDate.getTime());
        long diffInSec = TimeUnit.SECONDS.convert(diffInMsec, TimeUnit.MILLISECONDS);
        long diffInMinutes = TimeUnit.MINUTES.convert(diffInMsec, TimeUnit.MILLISECONDS);
        long diffInHour = TimeUnit.HOURS.convert(diffInMsec, TimeUnit.MILLISECONDS);

        if (diffInHour < 24) {
            int diffCalcSec = (int) diffInSec % 60;
            int diffCalcMinutes = (int) diffInMinutes % 60;
            int diffCalcHour = (int) diffInHour % 60;
            if (diffCalcHour > 0) {
                stringBuilder.append(diffCalcHour + "시간 ");
            }
            if (diffCalcMinutes > 0) {
                stringBuilder.append(diffCalcMinutes + "분 ");
            }
            if (diffCalcSec > 0) {
                stringBuilder.append(diffCalcSec + "초 ");
            }
        } else {
            return sdf.format(givenDate);
        }
        return stringBuilder.toString();
    }

    public static String calcDateBeforeNowAsMinSec2(Date givenDate) {
        Date now = new Date();

        StringBuilder stringBuilder = new StringBuilder();

        // 시간 차이 계산
        long diffInMsec = Math.abs(now.getTime() - givenDate.getTime());
        long diffInSec = TimeUnit.SECONDS.convert(diffInMsec, TimeUnit.MILLISECONDS);
        long diffInMinutes = TimeUnit.MINUTES.convert(diffInMsec, TimeUnit.MILLISECONDS);
        long diffInHour = TimeUnit.HOURS.convert(diffInMsec, TimeUnit.MILLISECONDS);

        if (diffInHour < 24) {
            int diffCalcSec = (int) diffInSec % 60;
            int diffCalcMinutes = (int) diffInMinutes % 60;

            if(diffCalcMinutes < 10){
                stringBuilder.append("0" + diffCalcMinutes);
            }else{
                stringBuilder.append(diffCalcMinutes);
            }

            stringBuilder.append(":");

            if(diffCalcSec < 10){
                stringBuilder.append("0" + diffCalcSec);
            }else{
                stringBuilder.append(diffCalcSec);
            }
        } else {
            return sdf.format(givenDate);
        }
        return stringBuilder.toString();
    }

    public static boolean isEmpty(String str) {
        if ("".equals(str) || str == null) {
            return true;
        }
        return false;
    }

    public static String convertSHA256(String password) {

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return null;
        }

        try {
            // "SHA1PRNG"은 알고리즘 이름
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte[] bytes = new byte[16];
            random.nextBytes(bytes);
            // SALT 생성
//            String salt = new String(Base64.getEncoder().encode(bytes));
//            String rawAndSalt = password + salt;

            // 평문 암호화
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());
            String hex = String.format("%064x", new BigInteger(1, md.digest()));

            // 평문+salt 암호화
//            md.update(rawAndSalt.getBytes());
//            String hex = String.format("%064x", new BigInteger(1, md.digest()));

//            String[] result = new String[2];
//            result[0] = hex;
//            result[1] = salt;

            return hex;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
            return null;
        }

    }

    public static ProgressBar progressBar;

    public static void showDialog(Context context, ConstraintLayout constraintLayout){
        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(300, 300);
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        params.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;

        constraintLayout.addView(progressBar, params);
        progressBar.setVisibility(View.VISIBLE);
    }

    public static void hideDialog(){
        if(progressBar != null) progressBar.setVisibility(View.GONE);

    }

    public static ArrayList<Contact> getContactsString(Context context) {
        ArrayList<Contact> result = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();

        Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String[] projection = {
//        ContactsContract.CommonDataKinds.Phone.CONTACT_ID // 인덱스 값, 중복될 수 있음 -- 한 사람 번호가 여러개인 경우
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                ,ContactsContract.CommonDataKinds.Phone.NUMBER};

        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        Cursor cursor = resolver.query(phoneUri, projection, null, null, sortOrder);

        if (cursor != null) {
            while (cursor.moveToNext()) {
//        int idIndex = cursor.getColumnIndex(projection[0]); // 이름을 넣어주면 그 칼럼을 가져와준다.
                int nameIndex = cursor.getColumnIndex(projection[0]);
                int numberIndex = cursor.getColumnIndex(projection[1]);
//        String id = cursor.getString(idIndex);

                String name = cursor.getString(nameIndex);
                String number = cursor.getString(numberIndex);
                Contact contact = new Contact(name, number);
                result.add(contact);

            }
        }

        // 데이터 계열은 반드시 닫아줘야 한다.
        cursor.close();
        return result;
    }

    public static void loadProfile(Context context, ImageView imageView, ProfileMap profileMap, CustomDialog.Type type){
        RequestOptions requestOptions = null;

        if(type == CustomDialog.Type.PROFILE_IMAGE){
            requestOptions = new RequestOptions().circleCrop();
            if(profileMap == null){
                Glide
                    .with(context)
                    .load(R.mipmap.ic_launcher)
                    .apply(requestOptions)
                    /** Glide는 원본 비율을 유지한다. */
                    .override(500,500)
                    .into(imageView);
                return;
            }
        }else if(type == CustomDialog.Type.BACKGROUND_IMAGE){
            requestOptions = new RequestOptions().centerCrop();
            if(profileMap == null){
                Glide
                    .with(context)
                    .load("")
                    .placeholder(R.color.silver)
                    .apply(requestOptions)
                    /** Glide는 원본 비율을 유지한다. */
                    .override(500,500)
                    .into(imageView);
                return;
            }
        }

        String imgPath = profileMap.getProfileImgPath();

        Glide
            .with(context)
            .load("https://webrtc-sfu.kro.kr/" + imgPath)
            .apply(requestOptions)
            /** Glide는 원본 비율을 유지한다. */
            .override(500,500)
            .into(imageView);

    }

    public static String getBasicDateFormat(String dateString){

        // 날짜를 문자열로 변환
        String formattedDate = sdf.format(dateString);

        return formattedDate;
    }

    public static int MAX_HEIGHT = 500;
    public static int MAX_WIDTH = 500;

    // 리샘플링 값 계산 : 타겟 너비와 높이를 기준으로 2의 거듭제곱인 샘플 크기 값을 계산
    public static Bitmap resizeBitmapImage(Bitmap bitmap) {
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
        }

        return Bitmap.createScaledBitmap(bitmap, halfWidth / inSampleSize, halfHeight / inSampleSize, true);
    }

    public static File saveBitmapToJpeg(Bitmap bitmap, Context context) {

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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, out);
//            bitmap.recycle();

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

}
