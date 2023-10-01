package com.example.meetingtogether.ui.meetings.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.meetingtogether.databinding.FragmentWhiteboardBinding;
import com.example.meetingtogether.ui.meetings.DTO.ColorModel;
import com.example.meetingtogether.ui.meetings.MeetingRoomActivity;

public class WhiteboardFragment extends Fragment implements ColorDialogFragment.ColorDialogListener {

    private FragmentWhiteboardBinding binding;
    private CreateResultInterface createResultInterface;
    private ColorModel colorModel;
    private DialogFragment dialog;

    public WhiteboardFragment(CreateResultInterface createResultInterface) {
        this.createResultInterface = createResultInterface;
    }

    public static WhiteboardFragment newInstance(CreateResultInterface createResultInterface) {
        WhiteboardFragment fragment = new WhiteboardFragment(createResultInterface);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.colorModel = new ColorModel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWhiteboardBinding.inflate(inflater, container, false);
        try{
            this.createResultInterface.onCreated(binding, this.colorModel);
        }catch (Exception e){
            this.createResultInterface.onError(e);
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.drawToolButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showColorDialog();
            }
        });
    }

    public void showColorDialog() {
        dialog = new ColorDialogFragment(this);
        dialog.show(getActivity().getSupportFragmentManager(), "ColorDialogFragment");
    }

    @Override
    public void onClick(ColorModel colorModel) {
        this.colorModel = colorModel;
        ((MeetingRoomActivity)getActivity()).getDrawingView().setPaintColor(this.colorModel);
        dialog.dismiss();
    }

    public interface CreateResultInterface{
        void onCreated(FragmentWhiteboardBinding whiteboardBinding, ColorModel colorModel);
        void onError(Exception e);
    }

    public ColorModel getColorModel() {
        return colorModel;
    }

    public void setColorModel(ColorModel colorModel) {
        this.colorModel = colorModel;
    }
}