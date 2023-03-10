package com.example.dontforgetbirthdayproject.request;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class FindPwdIdExistRequest extends StringRequest {

    //서버 url 설정(php 파일 연동)
    final static private String URL = "http://dfmbd.ivyro.net/FindPwdIdExist.php";
    private Map<String,String> map;
    public FindPwdIdExistRequest(String userId, Response.Listener<String> listener){
        super(Method.POST, URL,listener,null);
        map = new HashMap<>();
        map.put("userId",userId);
    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}