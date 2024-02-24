package com.example.meetingtogether.ui.meetings.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.meetingtogether.ui.meetings.CustomPeerConnection;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private Listener listener;
    private int year;
    private int month;
    private int day;

    public interface Listener{
        void onDateSet(DatePicker datePicker, int year, int month, int day);
    }

    public DatePickerFragment(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        return new DatePickerDialog(getActivity(), this, this.year, this.month -1, this.day);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        if(this.listener != null) listener.onDateSet(datePicker, year, month, day);
    }
}