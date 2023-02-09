package com.example.dontforgetbirthdayproject.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.dontforgetbirthdayproject.BackKeyHandler;
import com.example.dontforgetbirthdayproject.request.LoginRequest;
import com.example.dontforgetbirthdayproject.R;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    // 뒤로가기 이벤트 핸들러 변수
    private final BackKeyHandler backKeyHandler = new BackKeyHandler(this);
    //뒤로가기 두번 누르면 종료
    @Override
    public void onBackPressed() {
        backKeyHandler.onBackPressed("'뒤로' 버튼을 두 번 누르면 종료됩니다.");
    }

    private Button loginBtn;
    private TextView goJoinBtn;
    private EditText et_id,et_pwd;
    private CheckBox auto_login_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        loginBtn = findViewById(R.id.login_btn);
        goJoinBtn = findViewById(R.id.go_join_text_btn);
        et_id = findViewById(R.id.login_edit_id);
        et_pwd = findViewById(R.id.login_edit_pwd);
        auto_login_btn = findViewById(R.id.auto_login_btn);
        //로그인 버튼 이벤트
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userID = et_id.getText().toString();
                String userPwd = et_pwd.getText().toString();

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if(!et_id.getText().toString().equals("") && !et_pwd.getText().toString().equals("")){ //공백이 아니면
                                if(success){
                                    if(auto_login_btn.isChecked()){
                                        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
                                        SharedPreferences.Editor autoLoginEdit = auto.edit();
                                        autoLoginEdit.putString("userID", userID);
                                        autoLoginEdit.putString("userPwd", userPwd);
                                        autoLoginEdit.commit();
                                        Log.d("userName",jsonObject.getString("userName"));
                                    }
                                    String UserID = jsonObject.getString("userID");
                                    String UserPwd = jsonObject.getString("userPassword");
                                    Log.d("userName",jsonObject.getString("userName"));
                                    Toast.makeText(getApplicationContext(),"로그인 성공",Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("userId",userID);
                                    intent.putExtra("userPwd",userPwd);
                                    startActivity(intent);

                                } else{
                                    Toast.makeText(getApplicationContext(),"로그인 실패",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else{
                                Toast.makeText(getApplicationContext(),"로그인 실패",Toast.LENGTH_SHORT).show();
                                return;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                LoginRequest loginRequest = new LoginRequest(userID,userPwd,responseListener);
                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                queue.add(loginRequest);
            }
        });

        //회원가입 버튼
        goJoinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }
}
