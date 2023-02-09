package com.example.dontforgetbirthdayproject.request;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class ItemDeleteRequest extends StringRequest {
    //서버 url 설정(php 파일 연동)
    final static private String URL = "http://dfmbd.ivyro.net/ItemDelete.php";
    private Map<String,String> map;
    public ItemDeleteRequest(String itemId, String itemName, Response.Listener<String> listener){
        super(Method.POST, URL,listener,null);
        map = new HashMap<>();
        map.put("itemId",itemId);
        map.put("itemName",itemName);
    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}
