package com.example.dontforgetbirthdayproject;

import android.view.View;

import com.example.dontforgetbirthdayproject.adapter.HomeAdapter;

public interface SetAlarmListener {
    public void setAlarm(HomeAdapter.CustomViewHolder holder, View view, int position);
}
