package com.example.meetingtogether.ui.meetings.fragments;

import static com.example.meetingtogether.MainActivity.TAG;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePickerFragment extends TimePickerDialog implements TimePickerDialog.OnTimeSetListener {
    private Listener listener;

    public TimePickerFragment(Context context, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
        super(context, listener, hourOfDay, minute, is24HourView);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        Log.d(TAG, "TEST");
    }

    public interface Listener{
        void onTimeSet(TimePicker timePicker, int i, int i1);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}