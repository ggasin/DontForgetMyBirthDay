package com.example.dontforgetbirthdayproject.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.dontforgetbirthdayproject.R;
import com.example.dontforgetbirthdayproject.activity.LoginActivity;
import com.example.dontforgetbirthdayproject.activity.MainActivity;
import com.example.dontforgetbirthdayproject.request.LoginRequest;
import com.example.dontforgetbirthdayproject.request.MyPageRequest;

import org.json.JSONException;
import org.json.JSONObject;


public class MyPageFragement extends Fragment {
    MainActivity mainActivity;
    private TextView logoutTextBtn,myName,myId,myEmail;
    private Button goChangePwdBtn;

    //onAttach 는 fragment가 activity에 올라온 순간
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    public MyPageFragement() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_my_page_fragement, container, false);
        logoutTextBtn = rootView.findViewById(R.id.mp_logout_text_btn);
        myId = rootView.findViewById(R.id.mp_id_v);
        myName = rootView.findViewById(R.id.mp_name_v);
        myEmail = rootView.findViewById(R.id.mp_email_v);
        goChangePwdBtn =rootView.findViewById(R.id.mp_go_change_pwd_btn);
        goChangePwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.onFragmentChange(4);
            }
        });



        //정보 불러오기
        loadMyInfo();

        //로그아웃 버튼 이벤트
        logoutTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //로그아웃하면 알람 울리지 않도록 모든 알람 끄기
                mainActivity.allCancelAlarm("http://dfmbd.ivyro.net/LoadItemDB.php","전체");
                Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                SharedPreferences auto = mainActivity.getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = auto.edit();
                editor.clear();
                editor.commit();

                Toast.makeText(getActivity().getApplicationContext(), "로그아웃", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        });
        return rootView;
    }
    public void loadMyInfo(){
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                        if(success){
                            String userName = jsonObject.getString("userName");
                            String userId = jsonObject.getString("userID");
                            String userEmail = jsonObject.getString("userEmail");
                            myId.setText(userId);
                            myEmail.setText(userEmail);
                            myName.setText(userName);
                        } else{
                            Toast.makeText(getContext(),"정보 불러오기 실패",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }

            }
        };
        MyPageRequest myPageRequest = new MyPageRequest(mainActivity.userId,responseListener);
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        queue.add(myPageRequest);
    }
}