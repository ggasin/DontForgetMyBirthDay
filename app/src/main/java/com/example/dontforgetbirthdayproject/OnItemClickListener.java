package com.example.dontforgetbirthdayproject;

import android.view.View;

import com.example.dontforgetbirthdayproject.adapter.HomeAdapter;


//아이템 클릭 리스너
public interface OnItemClickListener {
    public void onItemClick(HomeAdapter.CustomViewHolder holder, View view, int position);
}
