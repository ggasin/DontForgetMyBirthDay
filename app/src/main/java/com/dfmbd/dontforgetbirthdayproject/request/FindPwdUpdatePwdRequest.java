package com.dfmbd.dontforgetbirthdayproject.request;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class FindPwdUpdatePwdRequest extends StringRequest {

    //서버 url 설정(php 파일 연동)
    final static private String URL = "http://dfmbd.ivyro.net/FindPwdUpdatePwd.php";
    private Map<String,String> map;
    public FindPwdUpdatePwdRequest(String userId, String userPwd,Response.Listener<String> listener){
        super(Method.POST, URL,listener,null);
        map = new HashMap<>();
        map.put("userId",userId);
        map.put("userPassword",userPwd);
    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}