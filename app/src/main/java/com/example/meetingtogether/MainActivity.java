package com.example.meetingtogether;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.meetingtogether.databinding.ActivityMainBinding;
import com.example.meetingtogether.services.ChatService;
import com.example.meetingtogether.services.TestService;
import com.example.meetingtogether.ui.meetings.MeetingRoomActivity;
import com.example.meetingtogether.ui.user.SignUpActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Iterator;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private String TAG = "TEST";

    /**
     * service
     * 필요한 권한
     */
    private final String[] PERMISSIONS = {
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE
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
                    start();
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
                        Toast.makeText(MainActivity.this, "권한을 취소하셨습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .create()
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_user, R.id.navigation_chat, R.id.navigation_meeting)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        permissionLauncher.launch(PERMISSIONS);
    }

    private void start(){
        /** 채팅 서비스 시작 */
        // 테스트 서비스 바인드 시작
        Intent intent = new Intent(MainActivity.this, ChatService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}