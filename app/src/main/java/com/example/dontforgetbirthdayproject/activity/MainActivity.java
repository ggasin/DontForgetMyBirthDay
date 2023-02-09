package com.example.dontforgetbirthdayproject.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.RadioButton;

import com.example.dontforgetbirthdayproject.fragment.AddItemFragment;
import com.example.dontforgetbirthdayproject.BackKeyHandler;
import com.example.dontforgetbirthdayproject.fragment.CalendarFragment;
import com.example.dontforgetbirthdayproject.fragment.HomeFragment;
import com.example.dontforgetbirthdayproject.fragment.ItemDetailFragment;
import com.example.dontforgetbirthdayproject.fragment.MyPageFragement;
import com.example.dontforgetbirthdayproject.NotificationReceiver;
import com.example.dontforgetbirthdayproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    // 뒤로가기 이벤트 핸들러 변수
    private final BackKeyHandler backKeyHandler = new BackKeyHandler(this);
    //뒤로가기 두번 누르면 종료
    @Override
    public void onBackPressed() {
        backKeyHandler.onBackPressed("'뒤로' 버튼을 두 번 누르면 종료됩니다.");
    }
    //프래그먼트 매니저 획득
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private HomeFragment fragmentHome = new HomeFragment();
    private MyPageFragement fragmentMyPage = new MyPageFragement();
    private AddItemFragment fragmentAddItem = new AddItemFragment();
    private ItemDetailFragment fragmentItemDetail = new ItemDetailFragment();
    private CalendarFragment fragmentCalendar = new CalendarFragment();
    private RadioButton mpAlarmOne, mpAlarmThree, mpAlarmSeven;
    public String userId,selectedGroup,itemName,itemSolarBirth,itemlunarBirth,itemMemo,itemGroup;
    public int itemClickPosition;
    public int profile_id;
    //현재 시간,분 변수선언
    int currHour, currMinute;
    //시스템에서 알람 서비스를 제공하도록 도와주는 클래스
    //특정 시점에 알람이 울리도록 도와준다
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        userId=intent.getStringExtra("userId");
        //프래그먼트 Transaction 획득
        //프래그먼트를 올리거나 교체하는 작업을 Transaction이라고 합니다.
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        HomeFragment homeFragment = new HomeFragment();
        transaction.replace(R.id.frameLayout, fragmentHome).commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.menu_bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new ItemSelectedListener());
        bottomNavigationView.setSelectedItemId(R.id.home_menu);



        //푸시알림을 보내기 위해, 시스템에서 알림 서비스를 생성해주는 코드
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);



    }
    //프래그먼트 선택 리스너
    class ItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            switch (menuItem.getItemId()) {
                case R.id.calander_menu:
                    transaction.replace(R.id.frameLayout,fragmentCalendar).commit();
                    break;
                case R.id.home_menu:
                    transaction.replace(R.id.frameLayout, fragmentHome).commit();
                    break;
                case R.id.mypage_menu:
                    transaction.replace(R.id.frameLayout, fragmentMyPage).commitAllowingStateLoss();
                    break;

            }

            return true;
        }
    }
    //홈프래그먼트에서 다른 프래그먼트 불러올 때 이용
    public void onFragmentChange(int index){
        if(index==0){ //홈프레그먼트로 이동
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragmentHome).commit();
        }
        else if(index==1){ //아이템 추가 프레그먼트로 이동
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragmentAddItem).commit();
            Log.d("메인에서 선택된 그룹",selectedGroup);
        } else if(index==2){
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragmentItemDetail).commit();

        }
    }

    //알람매니저에 알람등록 처리
    public void setNotice(int year,int month,int day,int hour,int minute,int id,String content,int requestCode) {

        //알람을 수신할 수 있도록 하는 리시버로 인텐트 요청
        Intent receiverIntent = new Intent(this, NotificationReceiver.class);
        receiverIntent.putExtra("content", content);
        receiverIntent.putExtra("requestCode", requestCode);
        receiverIntent.putExtra("id",id);
        receiverIntent.putExtra("day",day+2);

        /**
         * PendingIntent란?
         * - Notification으로 작업을 수행할 때 인텐트가 실행되도록 합니다.
         * Notification은 안드로이드 시스템의 NotificationManager가 Intent를 실행합니다.
         * 즉 다른 프로세스에서 수행하기 때문에 Notification으로 Intent수행시 PendingIntent의 사용이 필수 입니다.
         */

        /**
         * 브로드캐스트로 실행될 pendingIntent선언 한다.
         * Intent가 새로 생성될때마다(알람을 등록할 때마다) intent값을 업데이트 시키기 위해, FLAG_UPDATE_CURRENT 플래그를 준다
         * 이전 알람을 취소시키지 않으려면 requestCode를 다르게 줘야 한다.
         * */

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(this, requestCode, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(this, requestCode, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        //date타입으로 변경된 알람시간을 캘린더 타임에 등록
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        //알람시간 설정
        //param 1)알람의 타입
        //param 2)알람이 울려야 하는 시간(밀리초)을 나타낸다.
        //param 3)알람이 울릴 때 수행할 작업을 나타냄
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * 3, pendingIntent);
    }

}
