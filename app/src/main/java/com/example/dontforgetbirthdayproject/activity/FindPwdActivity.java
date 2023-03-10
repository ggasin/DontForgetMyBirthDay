package com.example.dontforgetbirthdayproject.activity;

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
import com.example.dontforgetbirthdayproject.R;
import com.example.dontforgetbirthdayproject.request.FindPwdIdExistRequest;
import com.example.dontforgetbirthdayproject.request.FindPwdUpdatePwdRequest;

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



        //????????? ????????? ??????
        goFindIdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FindPwdActivity.this, FindIdActivity.class);
                startActivity(intent);
            }
        });
        //????????????
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FindPwdActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
        //?????? ?????? ?????????
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               userId = idEt.getText().toString();

                if(userId.equals("")){
                    AlertDialog.Builder builder=new AlertDialog.Builder( FindPwdActivity.this );
                    dialog=builder.setMessage("???????????? ????????? ??????????????????.")
                            .setPositiveButton("??????",null)
                            .create();
                    dialog.show();
                    return;
                }
                isIdExist(userId);

            }
        });
        //????????? editText
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
        //????????? ?????? ?????? ?????????
        sendEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userEmail.equals(userEmailInDB)){
                    //SMTP ?????? ?????? ????????? ?????? ?????? ??????
                    String adminEmail = "dfmbdmail@gmail.com";
                    String password =  "puyfrwayhizzwths";
                    Properties props = new Properties();
                    props.put("mail.smtp.host", "smtp.gmail.com");
                    props.put("mail.smtp.socketFactory.port", "465");
                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.port", "465");
                    //???????????? ??????
                    updatePwdNum = updatePwdRandomNum();
                    //?????? ?????? ????????? ?????? ?????? ??????
                    Session session = Session.getDefaultInstance(props,
                            new javax.mail.Authenticator() {
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(adminEmail,password);
                                }
                            });
                    //????????? ?????? ????????? ??????
                    sendEmailThread = new SendEmailThread(session,userEmail,adminEmail);
                    sendEmailThread.start();
                    //????????? ?????? ???????????? ????????? ?????? ????????? ??????
                    try{
                        sendEmailThread.join();
                    }catch (InterruptedException e)
                    {

                    }
                    if (isSuccessSendEmail){
                        updatePwdToDB(userId,String.valueOf(updatePwdNum));
                        AlertDialog.Builder builder=new AlertDialog.Builder( FindPwdActivity.this );
                        dialog=builder.setMessage("????????? ??????????????? ?????????????????????. ?????? ????????? ????????? ????????? ???????????? ???????????????.")
                                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(FindPwdActivity.this,LoginActivity.class);
                                        startActivity(intent);
                                    }
                                })
                                .create();
                        dialog.show();

                    } else if(!isSuccessSendEmail && emailValid == false){
                        Toast.makeText(getApplicationContext(), "???????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                        isSuccessSendEmail = false;
                        emailValid = false;
                    } else {
                        Toast.makeText(getApplicationContext(), "?????? ??????.", Toast.LENGTH_SHORT).show();
                        isSuccessSendEmail = false;
                        emailValid = false;
                    }
                } else {
                    AlertDialog.Builder builder=new AlertDialog.Builder( FindPwdActivity.this );
                    dialog=builder.setMessage("????????? ???????????? ????????? ???????????? ???????????? ????????????. ????????? ???????????? ???????????????.")
                            .setPositiveButton("??????", new DialogInterface.OnClickListener() {
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
    //-----------------------------?????????----------------------------------------

    //????????? id??? db??? ???????????? ??????????????? ??????. ???????????? ?????? ????????????
    public void isIdExist(String userId){
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse=new JSONObject(response);
                    boolean success=jsonResponse.getBoolean("success");
                    //???????????? ???????????? ????????? ?????? ????????????
                    if(success){
                        userEmailInDB = jsonResponse.getString("userEmail");
                        infoText.setText("???????????? ??????????????? ?????? ?????? ???????????? ??????????????????.");
                        idLy.setVisibility(View.GONE);
                        nextBtn.setVisibility(View.GONE);
                        goFindIdBtn.setVisibility(View.GONE);
                        emailLy.setVisibility(View.VISIBLE);
                        sendEmailBtn.setVisibility(View.VISIBLE);
                    }
                    else{
                        AlertDialog.Builder builder=new AlertDialog.Builder( FindPwdActivity.this );
                        dialog=builder.setMessage("???????????? ???????????? ????????????. ????????? ???????????? ???????????????.")
                                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
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

    //????????? ?????? ?????????
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

                    //????????? ????????? ?????? ??????
                    MimeMessage message = new MimeMessage(session);
                    //????????? ????????? ?????? ??????
                    message.setFrom(new InternetAddress(adminEmail));
                    //?????? ????????? ?????? ??????
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(userEmail));
                    //?????? ??????
                    message.setSubject("????????? ????????? ???????????? ?????????");
                    Log.d("userEmail in thre","");
                    //?????? ??????
                    message.setText("??????????????? "+String.valueOf(updatePwdNum)+"??? ????????? ???????????????.\n?????? ??????????????? ????????? ??? ??? ??????????????? ??????????????????.");
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

    //???????????? ????????? 6?????? ??????????????? ???????????? ??????????????? ????????????.
    public void updatePwdToDB(String userId,String userPwd){
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse=new JSONObject(response);
                    boolean success=jsonResponse.getBoolean("success");
                    if(success){
                       Log.d("???????????? ???????????? ??????","??????");
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"???????????? ???????????? ??????",Toast.LENGTH_SHORT).show();
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
    //???????????? 6??????
    public static int updatePwdRandomNum() {
        Random random = new Random();
        int randomNum = random.nextInt(888888) + 111111;
        return randomNum;
    }

}
