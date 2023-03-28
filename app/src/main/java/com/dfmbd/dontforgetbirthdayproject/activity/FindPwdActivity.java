package com.dfmbd.dontforgetbirthdayproject.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.dfmbd.dontforgetbirthdayproject.request.FindPwdIdExistRequest;
import com.dfmbd.dontforgetbirthdayproject.request.FindPwdUpdatePwdRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

public class FindPwdActivity extends AppCompatActivity {


    private Button nextBtn,sendEmailBtn;
    private ImageButton backBtn;
    private TextView goFindIdBtn , infoText;
    private EditText emailEt , idEt;
    private boolean isSuccessSendEmail=false;
    private static ArrayList userIdArr = new ArrayList();
    private String userEmail="",userId="",userEmailInDB="";
    private boolean emailValid = false;
    private int updatePwdNum=0;
    private LinearLayout certificationLy,emailLy,idLy;
    SendEmailThread sendEmailThread;
    private AlertDialog dialog;
    private AdView findPwdAdView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pwd);

        idEt = findViewById(R.id.find_pwd_input_id_et);
        idLy = findViewById(R.id.find_pwd_input_id_layout);
        nextBtn = findViewById(R.id.find_pwd_next_btn);
        emailEt = findViewById(R.id.find_pwd_email_et);
        emailLy = findViewById(R.id.find_pwd_email_layout);
        goFindIdBtn = findViewById(R.id.go_find_id_text_btn);
        sendEmailBtn = findViewById(R.id.find_pwd_email_send_btn);
        infoText = findViewById(R.id.find_pwd_info_t);
        backBtn = findViewById(R.id.find_pwd_back_btn);
        certificationLy = findViewById(R.id.find_pwd_certification_layout);

        findPwdAdView= findViewById(R.id.find_pwd_adView);

        //광고 띄우기
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        findPwdAdView.loadAd(adRequest);



        //아이디 찾기로 이동
        goFindIdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FindPwdActivity.this, FindIdActivity.class);
                startActivity(intent);
            }
        });
        //뒤로가기
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FindPwdActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
        //다음 버튼 이벤트
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               userId = idEt.getText().toString();

                if(userId.equals("")){
                    AlertDialog.Builder builder=new AlertDialog.Builder( FindPwdActivity.this );
                    dialog=builder.setMessage("아이디를 제대로 입력해주세요.")
                            .setPositiveButton("확인",null)
                            .create();
                    dialog.show();
                    return;
                }
                isIdExist(userId);

            }
        });
        //이메일 editText
        emailEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
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
        //이메일 전송 버튼 이벤트
        sendEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userEmail.equals(userEmailInDB)){
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
                    updatePwdNum = updatePwdRandomNum();
                    //메일 계정 정보를 담기 위한 객체
                    Session session = Session.getDefaultInstance(props,
                            new javax.mail.Authenticator() {
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(adminEmail,password);
                                }
                            });
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
                        updatePwdToDB(userId,String.valueOf(updatePwdNum));
                        AlertDialog.Builder builder=new AlertDialog.Builder( FindPwdActivity.this );
                        dialog=builder.setMessage("메일로 비밀번호가 전송되었습니다. 확인 버튼을 누르면 로그인 화면으로 돌아갑니다.")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(FindPwdActivity.this,LoginActivity.class);
                                        startActivity(intent);
                                    }
                                })
                                .create();
                        dialog.show();

                    } else if(!isSuccessSendEmail && emailValid == false){
                        Toast.makeText(getApplicationContext(), "이메일을 확인해주세요.", Toast.LENGTH_SHORT).show();
                        isSuccessSendEmail = false;
                        emailValid = false;
                    } else {
                        Toast.makeText(getApplicationContext(), "전송 실패.", Toast.LENGTH_SHORT).show();
                        isSuccessSendEmail = false;
                        emailValid = false;
                    }
                } else {
                    AlertDialog.Builder builder=new AlertDialog.Builder( FindPwdActivity.this );
                    dialog=builder.setMessage("등록된 아이디에 작성된 이메일과 일치하지 않습니다. 로그인 화면으로 돌아갑니다.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(FindPwdActivity.this,LoginActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .create();
                    dialog.show();

                }

            }
        });
    }
    //-----------------------------메소드----------------------------------------

    //입력한 id가 db에 존재하는 아이디인지 확인. 존재하면 다음 스탭으로
    public void isIdExist(String userId){
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse=new JSONObject(response);
                    boolean success=jsonResponse.getBoolean("success");
                    //아이디가 존재하면 이메일 받는 스탭으로
                    if(success){
                        userEmailInDB = jsonResponse.getString("userEmail");
                        infoText.setText("재설정된 비밀번호를 받기 위한 이메일을 입력해주세요.");
                        idLy.setVisibility(View.GONE);
                        nextBtn.setVisibility(View.GONE);
                        goFindIdBtn.setVisibility(View.GONE);
                        emailLy.setVisibility(View.VISIBLE);
                        sendEmailBtn.setVisibility(View.VISIBLE);
                    }
                    else{
                        AlertDialog.Builder builder=new AlertDialog.Builder( FindPwdActivity.this );
                        dialog=builder.setMessage("아이디가 존재하지 않습니다. 로그인 화면으로 돌아갑니다.")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(FindPwdActivity.this,LoginActivity.class);
                                        startActivity(intent);
                                    }
                                })
                                .create();
                        dialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        FindPwdIdExistRequest findPwdIdExistRequest = new FindPwdIdExistRequest(userId,responseListener);
        RequestQueue queue = Volley.newRequestQueue(FindPwdActivity.this);
        queue.add(findPwdIdExistRequest);

    }

    //이메일 전송 스레드
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

                    //메일을 보내기 위한 객체
                    MimeMessage message = new MimeMessage(session);
                    //보내는 사람의 메일 주소
                    message.setFrom(new InternetAddress(adminEmail));
                    //받는 사람의 메일 주소
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(userEmail));
                    //메일 제목
                    message.setSubject("잊지마 내생일 비밀번호 재설정");
                    Log.d("userEmail in thre","");
                    //메일 내용
                    message.setText("비밀번호는 "+String.valueOf(updatePwdNum)+"로 재설정 되었습니다.\n바뀐 비밀번호로 로그인 한 후 비밀번호를 변경해주세요.");
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

    //랜덤으로 배정된 6자리 비밀번호를 사용자의 비밀번호로 업데이트.
    public void updatePwdToDB(String userId,String userPwd){
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse=new JSONObject(response);
                    boolean success=jsonResponse.getBoolean("success");
                    if(success){
                       Log.d("비밀번호 업데이트 성공","성공");
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"비밀번호 업데이트 실패",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        FindPwdUpdatePwdRequest findPwdUpdatePwdRequest = new FindPwdUpdatePwdRequest(userId,userPwd,responseListener);
        RequestQueue queue = Volley.newRequestQueue(FindPwdActivity.this);
        queue.add(findPwdUpdatePwdRequest);

    }
    //랜덤으로 6자리
    public static int updatePwdRandomNum() {
        Random random = new Random();
        int randomNum = random.nextInt(888888) + 111111;
        return randomNum;
    }

}
