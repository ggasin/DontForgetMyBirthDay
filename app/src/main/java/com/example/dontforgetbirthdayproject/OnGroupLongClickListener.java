package com.example.dontforgetbirthdayproject;

import android.view.View;

import com.example.dontforgetbirthdayproject.adapter.GroupAdapter;

//그룹 꾹 누르는 이벤트 리스너
public interface OnGroupLongClickListener {
    public void onGroupLongClick(GroupAdapter.CustomViewHolder holder, View view, int position);
}
