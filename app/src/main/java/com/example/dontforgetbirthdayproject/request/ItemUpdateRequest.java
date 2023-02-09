package com.example.dontforgetbirthdayproject.request;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class ItemUpdateRequest extends StringRequest {

    //서버 url 설정(php 파일 연동)
    final static private String URL = "http://dfmbd.ivyro.net/ItemUpdate.php";
    private Map<String,String> map;
    public ItemUpdateRequest(String itemId,String itemGroup ,String itemName, String itemBeforeName,String itemSolarBirth,String itemLunarBirth,String itemMemo,Response .Listener<String> listener){
        super(Method.POST, URL,listener,null);
        map = new HashMap<>();
        map.put("itemId",itemId);
        map.put("itemGroup",itemGroup);
        map.put("itemName",itemName);
        map.put("itemBeforeName",itemBeforeName);
        map.put("itemSolarBirth",itemSolarBirth);
        map.put("itemLunarBirth",itemLunarBirth);
        map.put("itemMemo",itemMemo);
    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}