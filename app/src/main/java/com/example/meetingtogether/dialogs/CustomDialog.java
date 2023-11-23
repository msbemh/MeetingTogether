package com.example.meetingtogether.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.example.meetingtogether.R;

public class CustomDialog extends Dialog {

    private Context context;
    private TextView titleView;
    private Type type;

    public enum Type{
        BACKGROUND_IMAGE,
        PROFILE_IMAGE
    }

    private CustomDialogClickInterface customDialogClickInterface;

    public interface CustomDialogClickInterface{
        void albumClick();
        void cameraClick();
    }

    public void setInterface(CustomDialogClickInterface customDialogClickInterface){
        this.customDialogClickInterface = customDialogClickInterface;
    }

    public CustomDialog(@NonNull Context context) {
        super(context);

        setContentView(R.layout.custom_dialog);

        this.titleView = findViewById(R.id.title);

        CardView albumCard = findViewById(R.id.album_card);
        albumCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomDialog.this.customDialogClickInterface.albumClick();
            }
        });

        CardView cameraCard = findViewById(R.id.camera_card);
        cameraCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomDialog.this.customDialogClickInterface.cameraClick();
            }
        });
    }

    public void setTitle(String title){
        this.titleView.setText(title);
    }

    public void setType(Type type){
        this.type = type;
    }


}
