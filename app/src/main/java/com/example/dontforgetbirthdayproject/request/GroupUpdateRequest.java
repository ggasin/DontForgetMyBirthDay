package com.example.dontforgetbirthdayproject.request;

import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class GroupUpdateRequest extends StringRequest {

    //서버 url 설정(php 파일 연동)
    final static private String URL = "http://dfmbd.ivyro.net/GroupUpdate.php";
    private Map<String,String> map;
    public GroupUpdateRequest(String userId, String group ,String beforeGroupName, Response.Listener<String> listener){
        super(Request.Method.POST, URL,listener,null);
        map = new HashMap<>();
        map.put("userId",userId);
        map.put("group",group);
        map.put("beforeGroupName",beforeGroupName);

    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}
