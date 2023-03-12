package com.example.dontforgetbirthdayproject.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.dontforgetbirthdayproject.R;
import com.example.dontforgetbirthdayproject.activity.FindPwdActivity;
import com.example.dontforgetbirthdayproject.activity.LoginActivity;
import com.example.dontforgetbirthdayproject.activity.MainActivity;
import com.example.dontforgetbirthdayproject.activity.RegisterActivity;
import com.example.dontforgetbirthdayproject.data.GroupData;
import com.example.dontforgetbirthdayproject.request.ChangePwdRequest;
import com.example.dontforgetbirthdayproject.request.FindPwdUpdatePwdRequest;
import com.example.dontforgetbirthdayproject.request.GroupAddRequest;
import com.example.dontforgetbirthdayproject.request.LoginRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;


public class ChangePwdFragment extends Fragment {
    MainActivity mainActivity;
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
    private LinearLayout inputIdLy,inputPwdLy,inputNewPwdLy,inputNewPwdCheckLy;
    private Button nextBtn,completeBtn;
    private ImageButton backBtn;
    private EditText inputIdEt,inputPwdEt,inputNewPwdEt,inputNewPwdCheckEt;
    private TextView infoText;
    private AlertDialog dialog; // 알림화면 띄우기



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_change_pwd, container, false);
        inputIdLy = rootView.findViewById(R.id.change_pwd_input_id_layout);
        inputPwdLy = rootView.findViewById(R.id.change_pwd_input_pwd_layout);
        inputNewPwdLy = rootView.findViewById(R.id.change_pwd_input_new_pwd_layout);
        inputNewPwdCheckLy = rootView.findViewById(R.id.change_pwd_input_new_pwd_check_layout);
        nextBtn = rootView.findViewById(R.id.change_pwd_next_btn);
        backBtn = rootView.findViewById(R.id.change_pwd_back_btn);
        completeBtn = rootView.findViewById(R.id.change_pwd_complete_btn);
        inputIdEt = rootView.findViewById(R.id.change_pwd_input_id_et);
        inputPwdEt = rootView.findViewById(R.id.change_pwd_input_pwd_et);
        inputNewPwdEt = rootView.findViewById(R.id.change_pwd_input_new_pwd_et);
        inputNewPwdCheckEt = rootView.findViewById(R.id.change_pwd_input_new_pwd_check_et);
        infoText = rootView.findViewById(R.id.change_pwd_info_t);

        //패스워드 입력 변화 리스너
        inputPwdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(inputPwdEt.getText().length()>=6){
                    nextBtn.setVisibility(View.VISIBLE);
                }
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.onFragmentChange(5); //마이페이지로 돌아가기
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mainActivity.userId.equals(inputIdEt.getText().toString())){
                    checkId(inputIdEt.getText().toString(),inputPwdEt.getText().toString());
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),"본인의 아이디와 일치하지 않습니다.",Toast.LENGTH_SHORT).show();
                }

            }
        });
        inputNewPwdCheckEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(inputNewPwdCheckEt.getText().toString().length()>=6){
                    completeBtn.setVisibility(View.VISIBLE);
                }

            }
        });
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputNewPwdCheckEt.getText().toString().length()>=6 && inputNewPwdCheckEt.getText().toString().equals(inputNewPwdEt.getText().toString())){
                    changePwd(inputNewPwdEt.getText().toString());

                } else {
                    Toast.makeText(getActivity().getApplicationContext(),"비밀번호를 다시 확인해주세요..",Toast.LENGTH_SHORT).show();
                }

            }
        });


        return rootView;
    }
    public void checkId(String id ,String pwd){
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                        if(success){
                            inputIdLy.setVisibility(View.GONE);
                            inputPwdLy.setVisibility(View.GONE);
                            inputNewPwdLy.setVisibility(View.VISIBLE);
                            inputNewPwdCheckLy.setVisibility(View.VISIBLE);
                            nextBtn.setVisibility(View.GONE);
                            infoText.setText("새로 바꿀 비밀번호를 입력해주세요.(6자-20자)");
                        } else{
                            Toast.makeText(getActivity().getApplicationContext(),"아이디가 존재하지 않거나 비밀번호가 일치하지 않습니다.",Toast.LENGTH_SHORT).show();
                            return;
                        }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        //로그인 하는 과정과 똑같아서 loginRequest 재탕
        LoginRequest loginRequest = new LoginRequest(id,pwd,responseListener);
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        queue.add(loginRequest);
    }
    public void changePwd(String pwd){
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                        if(success){
                            AlertDialog.Builder builder=new AlertDialog.Builder( getContext() );
                            dialog=builder.setMessage("로그인 화면으로 돌아가서 바뀐 비밀번호로 로그인 해 주세요.")
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(getActivity().getApplicationContext(),LoginActivity.class);
                                            startActivity(intent);
                                        }
                                    })
                                    .create();
                            dialog.show();
                        } else{
                            Toast.makeText(getContext(),"비밀번호 변경 실패",Toast.LENGTH_SHORT).show();
                            return;
                        }
                } catch (JSONException e) {
                    Toast.makeText(getContext(),"오류",Toast.LENGTH_SHORT).show();
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String exceptionAsStrting = sw.toString();
                    Log.e("오류", exceptionAsStrting);
                    e.printStackTrace();
                }
            }
        };
        FindPwdUpdatePwdRequest findPwdUpdatePwdRequest = new FindPwdUpdatePwdRequest(mainActivity.userId,pwd,responseListener);
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        queue.add(findPwdUpdatePwdRequest);


    }
}