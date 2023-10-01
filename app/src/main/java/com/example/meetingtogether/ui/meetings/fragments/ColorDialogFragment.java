package com.example.meetingtogether.ui.meetings.fragments;

import static com.example.meetingtogether.common.Constant.BLACK;
import static com.example.meetingtogether.common.Constant.BLUE;
import static com.example.meetingtogether.common.Constant.GREEN;
import static com.example.meetingtogether.common.Constant.INDIGO;
import static com.example.meetingtogether.common.Constant.ORANGE;
import static com.example.meetingtogether.common.Constant.PURPLE;
import static com.example.meetingtogether.common.Constant.RED;
import static com.example.meetingtogether.common.Constant.YELLOW;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.meetingtogether.R;
import com.example.meetingtogether.common.ColorType;
import com.example.meetingtogether.ui.meetings.DTO.ColorModel;

public class ColorDialogFragment extends DialogFragment {

    private ColorDialogListener colorDialogListener;
    private ColorModel colorModel;

    public interface ColorDialogListener{
        void onClick(ColorModel colorModel);
    }

    public ColorDialogFragment(ColorDialogListener colorDialogListener){
        this.colorDialogListener = colorDialogListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.fragment_color_dialog, null);
        dialogView.findViewById(R.id.black_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorDialogListener.onClick(new ColorModel(ColorType.BLACK));
            }
        });

        dialogView.findViewById(R.id.red_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorDialogListener.onClick(new ColorModel(ColorType.RED));
            }
        });
        dialogView.findViewById(R.id.orange_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = ContextCompat.getColor(getActivity(), R.color.orange);
                colorDialogListener.onClick(new ColorModel(ColorType.ORANGE));
            }
        });
        dialogView.findViewById(R.id.yellow_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = ContextCompat.getColor(getActivity(), R.color.yellow);
                colorDialogListener.onClick(new ColorModel(ColorType.YELLOW));
            }
        });
        dialogView.findViewById(R.id.green_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = ContextCompat.getColor(getActivity(), R.color.green);
                colorDialogListener.onClick(new ColorModel(ColorType.GREEN));
            }
        });
        dialogView.findViewById(R.id.blue_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = ContextCompat.getColor(getActivity(), R.color.blue);
                colorDialogListener.onClick(new ColorModel(ColorType.BLUE));
            }
        });
        dialogView.findViewById(R.id.indigo_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = ContextCompat.getColor(getActivity(), R.color.indigo);
                colorDialogListener.onClick(new ColorModel(ColorType.INDIGO));
            }
        });
        dialogView.findViewById(R.id.purple_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = ContextCompat.getColor(getActivity(), R.color.purple);
                colorDialogListener.onClick(new ColorModel(ColorType.PURPLE));
            }
        });

        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView);
                // Add action buttons
//                .setPositiveButton(R.string.check, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        Toast.makeText(getActivity(), "긍정 선택",Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        Toast.makeText(getActivity(), "취소 선택",Toast.LENGTH_SHORT).show();
//                    }
//                });

        return builder.create();
    }
}
