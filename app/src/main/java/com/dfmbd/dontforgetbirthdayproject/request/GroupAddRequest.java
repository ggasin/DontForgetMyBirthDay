package com.dfmbd.dontforgetbirthdayproject.request;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class GroupAddRequest extends StringRequest {

    //서버 url 설정(php 파일 연동)
    final static private String URL = "http://dfmbd.ivyro.net/GroupAdd.php";
    private Map<String,String> map;
    public GroupAddRequest(String userId, String myGroup , Response.Listener<String> listener){
        super(Method.POST, URL,listener,null);
        map = new HashMap<>();
        map.put("userId",userId);
        map.put("myGroup",myGroup);

    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}