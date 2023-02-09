package com.example.dontforgetbirthdayproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.dontforgetbirthdayproject.R;
import com.example.dontforgetbirthdayproject.request.RegisterRequest;
import com.example.dontforgetbirthdayproject.request.IdCheckRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private Button join_complete_btn, id_check_btn;
    private EditText et_id,et_pwd,et_name, et_email,et_pwd_chk;
    private boolean is_id_ok, is_pwd_ok , is_email_ok = false;
    private AlertDialog dialog; // 알림화면 띄우기

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        et_id = findViewById(R.id.rg_edit_id);
        et_pwd = findViewById(R.id.rg_edit_pwd);
        et_name = findViewById(R.id.rg_edit_nickname);
        et_pwd_chk = findViewById(R.id.rg_edit_check_pwd);
        et_email = findViewById(R.id.rg_edit_email);
        join_complete_btn = findViewById(R.id.rg_join_btn);
        id_check_btn = findViewById(R.id.rg_id_check_btn);


        //아이디 중복 체크 버튼 이벤트
        id_check_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userID = et_id.getText().toString();
                if(is_id_ok){
                    return;
                }
                if(userID.equals("")){
                    AlertDialog.Builder builder=new AlertDialog.Builder( RegisterActivity.this );
                    dialog=builder.setMessage("아이디를 입력해주세요.")
                            .setPositiveButton("확인",null)
                            .create();
                    dialog.show();
                    return;
                }
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse=new JSONObject(response);
                            boolean success=jsonResponse.getBoolean("success");
                            if(success){
                                AlertDialog.Builder builder=new AlertDialog.Builder( RegisterActivity.this );
                                dialog=builder.setMessage("사용할 수 있는 아이디입니다.")
                                        .setPositiveButton("확인",null)
                                        .create();
                                dialog.show();
                                is_id_ok =true;
                            }
                            else{
                                AlertDialog.Builder builder=new AlertDialog.Builder( RegisterActivity.this );
                                dialog=builder.setMessage("사용할 수 없는 아이디입니다.")
                                        .setNegativeButton("확인",null)
                                        .create();
                                dialog.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                IdCheckRequest idCheckRequest = new IdCheckRequest(userID,responseListener);
                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                queue.add(idCheckRequest);
            }
        });
        et_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                is_id_ok=false;
            }
        });
        et_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Pattern pattern = android.util.Patterns.EMAIL_ADDRESS;
                if(pattern.matcher(et_email.getText()).matches()){
                    is_email_ok = true;
                }
            }
        });
        et_pwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                is_pwd_ok =false;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (et_pwd.getText().length() < 6 || et_pwd.getText().length()>20) {
                    is_pwd_ok = false;
                } else {
                    is_pwd_ok = true;
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if (et_pwd.getText().toString().equals("")) {
                    is_pwd_ok = false;
                }
            }
        });
        //가입버튼 이벤트
        join_complete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userID = et_id.getText().toString();
                String userPwd = et_pwd.getText().toString();
                String userName = et_name.getText().toString();
                String userPwdCheck = et_pwd_chk.getText().toString();
                String userEmail = et_email.getText().toString();
                String group = "전체";

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if(is_id_ok && is_email_ok &&userPwd.equals(userPwdCheck) && is_pwd_ok){
                                if(success){
                                    Toast.makeText(getApplicationContext(),"가입 완료",Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else{
                                    Toast.makeText(getApplicationContext(),"가입 실패",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else {
                                Toast.makeText(getApplicationContext(),"아이디 중복확인 또는 비밀번호확인란을 확인해주세요",Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };

                RegisterRequest registerRequest = new RegisterRequest(userID,userPwd,userName,userEmail,group,responseListener);
                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                queue.add(registerRequest);

            }
        });
    }
}
