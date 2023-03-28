package com.dfmbd.dontforgetbirthdayproject.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dfmbd.dontforgetbirthdayproject.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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


public class FindIdActivity extends AppCompatActivity {


    private Button sendEmailBtn,completeBtn,goLoginBtn;
    private ImageButton backBtn;
    private TextView goFindPwdBtn, timerText, timerTitleText,findUserIdValue , infoText;
    private EditText emailEt,certificationEt;
    private boolean isSuccessSendEmail=false;
    private ArrayList userIdArr = new ArrayList();
    private String userEmail="";
    private boolean emailValid = false;
    private int certificationNum=0;
    private LinearLayout certificationLy,emailLy;
    SendEmailThread sendEmailThread;
    private CountDownTimer timer;
    private AlertDialog dialog;
    private AdView findIdPwdAdView;
    String findIdUrl = "http://dfmbd.ivyro.net/FindId.php";

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
        setContentView(R.layout.activity_find_id_pwd);

        sendEmailBtn = findViewById(R.id.find_id_email_send_btn);
        goFindPwdBtn = findViewById(R.id.go_find_pwd_text_btn);
        backBtn = findViewById(R.id.find_id_pwd_back_btn);
        emailEt = findViewById(R.id.find_id_email_et);
        sendEmailBtn.setEnabled(false);
        timerText = findViewById(R.id.find_id_timer_t);
        timerTitleText = findViewById(R.id.find_id_timer_title_t);
        certificationEt = findViewById(R.id.find_id_certification_et);
        completeBtn = findViewById(R.id.find_id_complete_btn);
        certificationLy = findViewById(R.id.find_id_certification_layout);
        emailLy = findViewById(R.id.find_id_email_layout);
        findUserIdValue = findViewById(R.id.find_user_id_value_t);
        infoText = findViewById(R.id.find_id_info_t);
        goLoginBtn = findViewById(R.id.find_id_go_login_btn);


        findIdPwdAdView= findViewById(R.id.find_id_pwd_adView);

        //광고 띄우기
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        findIdPwdAdView.loadAd(adRequest);


        //이메일 editText
        emailEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Pattern pattern = android.util.Patterns.EMAIL_ADDRESS;
                if(pattern.matcher(emailEt.getText()).matches()){
                    userEmail = emailEt.getText().toString();
                    Log.d("userEmail first",userEmail);
                    emailValid = true;
                    sendEmailBtn.setEnabled(true);
                }

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FindIdActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });


        sendEmailBtn.setOnClickListener(new View.OnClickListener() {
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
                        AlertDialog.Builder builder=new AlertDialog.Builder( FindIdActivity.this );
                        certificationNum = 0;
                        emailEt.setEnabled(true);
                        sendEmailBtn.setEnabled(true);dialog=builder.setMessage("인증시간이 초과되었습니다. 다시 시도해주세요.")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(FindIdActivity.this,LoginActivity.class);
                                        startActivity(intent);
                                    }
                                })
                                .create();
                        dialog.show();
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
                    Toast.makeText(getApplicationContext(), "전송 완료", Toast.LENGTH_SHORT).show();
                    timer.start();
                    afterSendEmailUI();
                    FindIdFromDB(findIdUrl,userEmail,adminEmail);
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
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(String.valueOf(certificationNum).equals(certificationEt.getText().toString())){
                    Toast.makeText(getApplicationContext(),"인증 성공",Toast.LENGTH_SHORT).show();
                    try{
                        timer.cancel();
                    } catch (Exception e) {}
                    timer=null;
                    String id = "회원님의 아이디는 ";
                    if(userIdArr.size()>0){
                        for(int i=0;i<userIdArr.size();i++){
                            if(i == userIdArr.size()-1){
                                id = id+userIdArr.get(i).toString()+"입니다.";
                            } else {
                                id = id+userIdArr.get(i).toString()+" , ";
                            }
                        }
                    } else {
                        id = "존재하는 아이디가 없습니다.";
                    }
                    findUserIdValue.setText(id);
                    afterCompleteUI();
                    userIdArr.clear();
                } else {
                    Toast.makeText(getApplicationContext(),"인증번호를 확인해 주세요.",Toast.LENGTH_SHORT).show();
                }
            }
        });
        goLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FindIdActivity.this,LoginActivity.class);
                startActivity(intent);

            }
        });
        goFindPwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FindIdActivity.this,FindPwdActivity.class);
                startActivity(intent);
            }
        });
    }



    public class SendEmailThread extends Thread{

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
                    message.setSubject("잊지마 내생일 아이디 찾기 인증번호");
                    Log.d("userEmail in thre","");
                    //메일 내용
                    message.setText("인증번호는 " + content + " 입니다");
                    Transport.send(message);
                    isSuccessSendEmail = true;

                } else {

                }

            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void FindIdFromDB(String url,String userEmail,String adminEmail){
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {  //응답을 문자열로 받아서 여기다 넣어달란말임(응답을 성공적으로 받았을 떄 이메소드가 자동으로 호출됨
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                userIdArr.add(jsonObject.getString("userID")); //no가 문자열이라서 바꿔야함.
                            }
                            if(jsonArray.length()  >0){
                                Log.d("isSuccessSendEmail f1",userIdArr.get(0).toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){ //에러발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();

                    }
                }
        ){
            //만약 POST 방식에서 전달할 요청 파라미터가 있다면 getParams 메소드에서 반환하는 HashMap 객체에 넣어줍니다.
            //이렇게 만든 요청 객체는 요청 큐에 넣어주는 것만 해주면 됩니다.
            //POST방식으로 안할거면 없어도 되는거같다.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userEmail", userEmail);
                Log.d("userEmail",userEmail);
                return params;
            }
        };
        //실제 요청 작업을 수행해주는 요청큐 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        //요청큐에 요청 객체 생성
        requestQueue.add(request);

    }

    public static int ramdomSixNum() {
        Random random = new Random();
        int randomNum = random.nextInt(888888) + 111111;
        return randomNum;
    }
    public void afterSendEmailUI(){
        timerText.setVisibility(View.VISIBLE);
        timerTitleText.setVisibility(View.VISIBLE);
        certificationLy.setVisibility(View.VISIBLE);
        completeBtn.setVisibility(View.VISIBLE);
        goFindPwdBtn.setVisibility(View.GONE);
        emailEt.setEnabled(false);
        sendEmailBtn.setEnabled(false);
        isSuccessSendEmail = false;
        emailValid = false;
    }
    public void afterCompleteUI(){
        timerText.setVisibility(View.GONE);
        timerTitleText.setVisibility(View.GONE);
        certificationLy.setVisibility(View.GONE);
        completeBtn.setVisibility(View.GONE);
        emailLy.setVisibility(View.GONE);
        sendEmailBtn.setVisibility(View.GONE);
        infoText.setVisibility(View.GONE);
        findUserIdValue.setVisibility(View.VISIBLE);
        goLoginBtn.setVisibility(View.VISIBLE);
        emailEt.setEnabled(true);
        sendEmailBtn.setEnabled(true);
        emailValid = false;
    }




}
