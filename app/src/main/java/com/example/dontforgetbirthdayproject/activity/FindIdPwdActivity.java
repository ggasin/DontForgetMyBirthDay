package com.example.dontforgetbirthdayproject.activity;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dontforgetbirthdayproject.BackKeyHandler;
import com.example.dontforgetbirthdayproject.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;


import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class FindIdPwdActivity extends AppCompatActivity {


    private Button sendEmailBtn;
    private ImageButton backBtn;
    private TextView goFindPwdBtn;
    private EditText emailEt;
    private static boolean isSuccessSendEmail=false , isDBConnet = false;
    private static ArrayList userIdArr = new ArrayList();
    private String userEmail="";
    private boolean emailValid = false;
    SendEmailThread sendEmailThread;
    String findIdUrl = "http://dfmbd.ivyro.net/FindId.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_id_pwd);

        sendEmailBtn = findViewById(R.id.find_id_email_send_btn);
        goFindPwdBtn = findViewById(R.id.go_find_pwd_text_btn);
        backBtn = findViewById(R.id.find_id_pwd_back_btn);
        emailEt = findViewById(R.id.find_id_et);
        sendEmailBtn.setEnabled(false);
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
                Intent intent = new Intent(FindIdPwdActivity.this,LoginActivity.class);
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
                //메일 계정 정보를 담기 위한 객체
                Session session = Session.getDefaultInstance(props,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(adminEmail,password);
                            }
                        });


                FindIdFromDB(findIdUrl,userEmail,adminEmail);

                sendEmailThread = new SendEmailThread(session,userEmail,adminEmail);
                sendEmailThread.setPriority(Thread.MIN_PRIORITY);
                sendEmailThread.start();
                try{
                    sendEmailThread.join();
                }catch (InterruptedException e)
                {

                }

                Log.d("isSuccessSendEmail 1",String.valueOf(isSuccessSendEmail));
                if (isSuccessSendEmail){
                    Toast.makeText(getApplicationContext(), "전송 완료", Toast.LENGTH_SHORT).show();
                    isSuccessSendEmail = false;
                    emailValid = false;
                    userIdArr.clear();
                } else if(!isSuccessSendEmail && emailValid == false){
                    Toast.makeText(getApplicationContext(), "이메일을 확인해주세요.", Toast.LENGTH_SHORT).show();
                    isSuccessSendEmail = false;
                    emailValid = false;
                    userIdArr.clear();
                } else {
                    Toast.makeText(getApplicationContext(), "전송 실패.", Toast.LENGTH_SHORT).show();
                    isSuccessSendEmail = false;
                    emailValid = false;
                    userIdArr.clear();
                }


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
                    String content = "";
                    //메일을 보내기 위한 객체
                    MimeMessage message = new MimeMessage(session);
                    //보내는 사람의 메일 주소
                    message.setFrom(new InternetAddress(adminEmail));
                    //받는 사람의 메일 주소
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(userEmail));
                    //메일 제목
                    message.setSubject("잊지마 내생일 아이디 찾기");
                    Log.d("userEmail in thre","");
                    if(userIdArr.size()>0){
                        content = "사용자님의 ID는 ";
                        for(int i=0;i<userIdArr.size();i++){
                            if(i==userIdArr.size()-1){
                                content = content + userIdArr.get(i).toString() + "입니다.";
                            } else {
                                content = content + userIdArr.get(i).toString() + ",";
                            }
                        }
                    } else {
                        content = "존재하는 아이디가 없습니다.";
                    }
                    //메일 내용
                    message.setText(content);
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
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                userIdArr.add(jsonObject.getString("userID")); //no가 문자열이라서 바꿔야함.
                                Log.d("isSuccessSendEmail f","");
                            }

                            if(response.length()  >0){
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




}
