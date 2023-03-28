package com.dfmbd.dontforgetbirthdayproject.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.dfmbd.dontforgetbirthdayproject.R;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //다크모드 무시
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ImageView gif_cake = (ImageView) findViewById(R.id.gift_gif_icon);
        Glide.with(this).load(R.drawable.gift_gif_icon).into(gif_cake); // gif
        Handler handler = new Handler();
        //자동로그인 구현을 위한 SharedPreferences. 앱 파일에 저장되는 데이터를 다룰 수 있는 인터페이스
        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
        String userID = auto.getString("userID", null);//
        String userPwd = auto.getString("userPwd", null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //인터넷 연결이 됐는지 확인
                boolean isConnected = isNetworkConnected(SplashActivity.this);
                if (!isConnected)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                    builder.setMessage("인터넷 연결이 원활하지 않습니다")
                            .setCancelable(false)
                            .setPositiveButton("종료", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    finishAffinity();
                                }
                            }).show();
                }
                else{ //인터넷이 연결 됐으면
                    if(userID!=null && userPwd!=null){
                        Toast.makeText(getApplicationContext(),"로그인 성공",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        intent.putExtra("userId",userID);
                        intent.putExtra("userPwd",userPwd);
                        startActivity(intent);
                        finish();
                    } else{
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                }
            }
        },1500);
    }
    //인터넷 연결 확인 메소드
    public boolean isNetworkConnected(Context context)
    {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo wimax = manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
        boolean bwimax = false;
        if (wimax != null)
        {
            bwimax = wimax.isConnected();
        }
        if (mobile != null)
        {
            if (mobile.isConnected() || wifi.isConnected() || bwimax)
            {
                return true;
            }
        }
        else
        {
            if (wifi.isConnected() || bwimax)
            {
                return true;
            }
        }
        return false;
    }
}