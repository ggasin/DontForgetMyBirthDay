package com.dfmbd.dontforgetbirthdayproject.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.dfmbd.dontforgetbirthdayproject.R;
import com.dfmbd.dontforgetbirthdayproject.request.RegisterRequest;
import com.dfmbd.dontforgetbirthdayproject.request.IdCheckRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Properties;
import java.util.Random;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class RegisterActivity extends AppCompatActivity {

    private Button joinCompleteBtn, idCheckBtn , sendEmailbtn , certificationBtn ;
    private ImageButton backBtn;
    private EditText etId, etPwd, etName, etEmail, etPwdChk , etCertification;
    private TextView timerText,timerTitleText;
    private boolean isIdOk, isPwdOk, isEmailOk,emailValid= false;
    private LinearLayout certificationLy;
    private AlertDialog dialog; // 알림화면 띄우기
    private int certificationNum=0;
    private boolean isSuccessSendEmail=false;
    private String userEmail="";
    private CountDownTimer timer;


    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
            timer.cancel();
        } catch (Exception e) {}
        timer=null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etId = findViewById(R.id.rg_id_et);
        etPwd = findViewById(R.id.rg_pwd_et);
        etName = findViewById(R.id.rg_nickname_et);
        etPwdChk = findViewById(R.id.rg_check_pwd_et);
        etEmail = findViewById(R.id.rg_email_et);
        joinCompleteBtn = findViewById(R.id.rg_join_btn);
        idCheckBtn = findViewById(R.id.rg_id_check_btn);
        sendEmailbtn = findViewById(R.id.rg_send_email_btn);
        certificationBtn = findViewById(R.id.rg_certification_btn);
        etCertification = findViewById(R.id.rg_email_certification_et);
        timerText = findViewById(R.id.rg_timer_value_text);
        timerTitleText = findViewById(R.id.rg_timer_title_text);
        certificationLy = findViewById(R.id.rg_email_certification_ly);
        backBtn = findViewById(R.id.rg_back_btn);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });



        //아이디 중복 체크 버튼 이벤트
        idCheckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userID = etId.getText().toString();
                if(isIdOk){
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
                                isIdOk =true;
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
        etId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                isIdOk =false;
            }
        });
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Pattern pattern = android.util.Patterns.EMAIL_ADDRESS;
                if(pattern.matcher(etEmail.getText()).matches()){
                    userEmail = etEmail.getText().toString();
                    emailValid = true;
                    //sendEmailbtn.setEnabled(true);
                } else {
                    emailValid = false;
                }
            }
        });

       /* sendEmailbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //SMTP 서버 사용 정보를 담기 위한 객체
                String adminEmail = "dfmbdmail@gmail.com";
                String password =  "puyfrwayhizzwths";
                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", "465");
                //인증번호 생성
                certificationNum = ramdomSixNum();
                //메일 계정 정보를 담기 위한 객체
                Session session = Session.getDefaultInstance(props,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(adminEmail,password);
                            }
                        });
                //타이머 생성 3분
                timer = new CountDownTimer(300000, 1000) {
                    @Override
                    public void onTick(long l) {
                        timerText.setText(l / 1000 /60 + ":" + l / 1000 % 60);
                    }
                    @Override
                    public void onFinish() {
                        AlertDialog.Builder builder=new AlertDialog.Builder( RegisterActivity.this );
                        certificationNum = 0;
                        etEmail.setEnabled(true);
                        sendEmailbtn.setEnabled(true);
                        timerText.setText("인증시간이 초과됐습니다. 다시 시도해주세요.");
                    }
                };
                //이메일 전송 스레드 시작
                sendEmailThread = new SendEmailThread(session,userEmail,adminEmail);
                sendEmailThread.start();
                //이메일 전송 스레드가 끝나면 아래 코드를 실행
                try{
                    sendEmailThread.join();
                }catch (InterruptedException e)
                {

                }
                if (isSuccessSendEmail){
                    timer.start();
                    sendEmailbtn.setText("재전송");
                    etEmail.setEnabled(false);
                    certificationLy.setVisibility(View.VISIBLE);
                    timerText.setVisibility(View.VISIBLE);
                    timerTitleText.setVisibility(View.VISIBLE);
                    isSuccessSendEmail = false;
                } else if(!isSuccessSendEmail && emailValid == false){
                    Toast.makeText(getApplicationContext(), "이메일을 확인해주세요.", Toast.LENGTH_SHORT).show();
                    isSuccessSendEmail = false;
                    emailValid = false;
                } else {
                    Toast.makeText(getApplicationContext(), "전송 실패.", Toast.LENGTH_SHORT).show();
                    isSuccessSendEmail = false;
                    emailValid = false;
                }
            }
        });
        //인증번호 입력 후 확인 버튼
        certificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(String.valueOf(certificationNum).equals(etCertification.getText().toString())){
                    Toast.makeText(getApplicationContext(),"인증 성공",Toast.LENGTH_SHORT).show();
                    timerText.setVisibility(View.GONE);
                    timerTitleText.setText("인증 성공!");
                    timerTitleText.setTextColor(Color.parseColor("#1DDB16")); //초록색
                    certificationLy.setVisibility(View.GONE);
                    etEmail.setEnabled(false);
                    sendEmailbtn.setVisibility(View.GONE);

                    isEmailOk = true;
                    try{
                        timer.cancel();
                    } catch (Exception e) {}
                    timer=null;
                } else {
                    Toast.makeText(getApplicationContext(),"인증번호를 확인해 주세요.",Toast.LENGTH_SHORT).show();
                }
            }
        });*/


        etPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isPwdOk =false;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (etPwd.getText().length() < 6 || etPwd.getText().length()>20) {
                    isPwdOk = false;
                } else {
                    isPwdOk = true;
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if (etPwd.getText().toString().equals("")) {
                    isPwdOk = false;
                }
            }
        });
        //가입버튼 이벤트
        joinCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userID = etId.getText().toString();
                String userPwd = etPwd.getText().toString();
                String userName = etName.getText().toString();
                String userPwdCheck = etPwdChk.getText().toString();
                String userEmail = etEmail.getText().toString();
                String group = "전체";

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if(isIdOk && emailValid &&userPwd.equals(userPwdCheck) && isPwdOk){
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
                                Toast.makeText(getApplicationContext(),"누락된 정보가 있는지 확인해주세요.",Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Log.d("emailValid",emailValid+"");
                RegisterRequest registerRequest = new RegisterRequest(userID,userPwd,userName,userEmail,group,emailValid,responseListener);
                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                queue.add(registerRequest);

            }
        });
    }
    /*public class SendEmailThread extends Thread{

        private Session session;
        private String userEmail;
        private String adminEmail;
        public SendEmailThread(Session session, String userEmail, String adminEmail){
            this.session = session;
            this.userEmail = userEmail;
            this.adminEmail = adminEmail;
        };
        public void run() {
            try {
                if(emailValid){
                    String content = String.valueOf(certificationNum);
                    //메일을 보내기 위한 객체
                    MimeMessage message = new MimeMessage(session);
                    //보내는 사람의 메일 주소
                    message.setFrom(new InternetAddress(adminEmail));
                    //받는 사람의 메일 주소
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(userEmail));
                    //메일 제목
                    message.setSubject("잊지마 내생일 회원가입 인증번호");
                    Log.d("userEmail in thre","");
                    //메일 내용
                    message.setText("인증번호는 " + content + " 입니다");
                    Transport.send(message);
                    isSuccessSendEmail = true;
                    Log.d("isSuccessSendEmail",String.valueOf(isSuccessSendEmail));
                } else {

                }

            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static int ramdomSixNum() {
        Random random = new Random();
        int randomNum = random.nextInt(888888) + 111111;
        return randomNum;
    }*/
}
