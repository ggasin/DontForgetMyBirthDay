package com.example.dontforgetbirthdayproject.activity;


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
import com.example.dontforgetbirthdayproject.R;

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


        //????????? editText
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
                certificationNum = ramdomSixNum();
                //?????? ?????? ????????? ?????? ?????? ??????
                Session session = Session.getDefaultInstance(props,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(adminEmail,password);
                            }
                        });
                //????????? ?????? 3???
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
                        sendEmailBtn.setEnabled(true);dialog=builder.setMessage("??????????????? ?????????????????????. ?????? ??????????????????.")
                                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
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
                    Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                    timer.start();
                    afterSendEmailUI();
                    FindIdFromDB(findIdUrl,userEmail,adminEmail);
                } else if(!isSuccessSendEmail && emailValid == false){
                    Toast.makeText(getApplicationContext(), "???????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    isSuccessSendEmail = false;
                    emailValid = false;
                } else {
                    Toast.makeText(getApplicationContext(), "?????? ??????.", Toast.LENGTH_SHORT).show();
                    isSuccessSendEmail = false;
                    emailValid = false;
                }



            }
        });
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(String.valueOf(certificationNum).equals(certificationEt.getText().toString())){
                    Toast.makeText(getApplicationContext(),"?????? ??????",Toast.LENGTH_SHORT).show();
                    try{
                        timer.cancel();
                    } catch (Exception e) {}
                    timer=null;
                    String id = "???????????? ???????????? ";
                    if(userIdArr.size()>0){
                        for(int i=0;i<userIdArr.size();i++){
                            if(i == userIdArr.size()-1){
                                id = id+userIdArr.get(i).toString()+"?????????.";
                            } else {
                                id = id+userIdArr.get(i).toString()+" , ";
                            }
                        }
                    } else {
                        id = "???????????? ???????????? ????????????.";
                    }
                    findUserIdValue.setText(id);
                    afterCompleteUI();
                    userIdArr.clear();
                } else {
                    Toast.makeText(getApplicationContext(),"??????????????? ????????? ?????????.",Toast.LENGTH_SHORT).show();
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
                    //????????? ????????? ?????? ??????
                    MimeMessage message = new MimeMessage(session);
                    //????????? ????????? ?????? ??????
                    message.setFrom(new InternetAddress(adminEmail));
                    //?????? ????????? ?????? ??????
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(userEmail));
                    //?????? ??????
                    message.setSubject("????????? ????????? ????????? ?????? ????????????");
                    Log.d("userEmail in thre","");
                    //?????? ??????
                    message.setText("??????????????? " + content + " ?????????");
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
                new Response.Listener<String>() {  //????????? ???????????? ????????? ????????? ??????????????????(????????? ??????????????? ????????? ??? ??????????????? ???????????? ?????????
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                userIdArr.add(jsonObject.getString("userID")); //no??? ?????????????????? ????????????.
                            }
                            if(response.length()  >0){
                                Log.d("isSuccessSendEmail f1",userIdArr.get(0).toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){ //??????????????? ????????? ????????? ??????
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();

                    }
                }
        ){
            //?????? POST ???????????? ????????? ?????? ??????????????? ????????? getParams ??????????????? ???????????? HashMap ????????? ???????????????.
            //????????? ?????? ?????? ????????? ?????? ?????? ???????????? ?????? ????????? ?????????.
            //POST???????????? ???????????? ????????? ???????????????.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userEmail", userEmail);
                Log.d("userEmail",userEmail);
                return params;
            }
        };
        //?????? ?????? ????????? ??????????????? ????????? ?????? ??????
        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        //???????????? ?????? ?????? ??????
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
