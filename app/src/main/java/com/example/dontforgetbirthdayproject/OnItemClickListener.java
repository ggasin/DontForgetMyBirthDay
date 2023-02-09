package com.example.dontforgetbirthdayproject;

import android.view.View;

import com.example.dontforgetbirthdayproject.adapter.HomeAdapter;

public interface OnItemClickListener {
    public void onItemClick(HomeAdapter.CustomViewHolder holder, View view, int position);
}
