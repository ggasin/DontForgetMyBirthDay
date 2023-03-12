package com.example.dontforgetbirthdayproject.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dontforgetbirthdayproject.data.GroupData;
import com.example.dontforgetbirthdayproject.fragment.AddItemFragment;
import com.example.dontforgetbirthdayproject.BackKeyHandler;
import com.example.dontforgetbirthdayproject.fragment.CalendarFragment;
import com.example.dontforgetbirthdayproject.fragment.ChangePwdFragment;
import com.example.dontforgetbirthdayproject.fragment.HomeFragment;
import com.example.dontforgetbirthdayproject.fragment.ItemDetailFragment;
import com.example.dontforgetbirthdayproject.fragment.MyPageFragement;
import com.example.dontforgetbirthdayproject.NotificationReceiver;
import com.example.dontforgetbirthdayproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ChangePwdFragment fragmentChangePwd = new ChangePwdFragment();
    public String userId,selectedGroup,itemName,itemSolarBirth,itemlunarBirth,itemMemo,itemGroup;
    public ArrayList<String> groupArr;
    public int itemClickPosition,itemRequestCode;
    public int profile_id;

    public boolean firstLogin = false , itemAlarmOnoff;
    public boolean ifTrueCalenderElseHome = true;

    //시스템에서 알람 서비스를 제공하도록 도와주는 클래스
    //특정 시점에 알람이 울리도록 도와준다
    public static AlarmManager alarmManager;
    public static PendingIntent pendingIntent;


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


        //하단바 설정
        BottomNavigationView bottomNavigationView = findViewById(R.id.menu_bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new ItemSelectedListener());
        bottomNavigationView.setSelectedItemId(R.id.home_menu);

        //푸시알림을 보내기 위해, 시스템에서 알림 서비스를 생성해주는 코드
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        firstLogin = intent.getBooleanExtra("firstLogin",false);
        Log.d("first Login",String.valueOf(firstLogin));


        //NotificationReceiver로부터 액션 등록(푸시알림이 울리고 나서 메인액티비티에서 다시 setNotice를 실행하기 위함)
        /*
        registerReceiver(notificationReceiver,new IntentFilter("INTERNET_LOST"));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("INTERNET_LOST");
        */
        /*
        //배터리 절전모드 해제(알림이 백그라운드에서 작동하지 않는것 같아 추가했는데 필요 없어져서 주석처리)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && firstLogin) {
            Intent wakeSleepModeIntent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                wakeSleepModeIntent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                wakeSleepModeIntent.setData(Uri.parse("package:" + packageName));
                startActivity(wakeSleepModeIntent);
            }
        }
         */



    }
    //푸시알림이 울리고 난 뒤 액션을 받고나서 실행되는 부분. setNotice 실행(receiver에서 알림을 다시 실행시키는걸로 바꿈)
    /*
    BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int year = intent.getIntExtra("year",0);
            int month = intent.getIntExtra("month",0);
            int day = intent.getIntExtra("day",0);
            int alarmStartDay = intent.getIntExtra("alarmStartDay",0);
            int hour = intent.getIntExtra("hour",0);
            int minute = intent.getIntExtra("minute",0);
            String content = intent.getStringExtra("content");
            int id = intent.getIntExtra("id",0);
            int requestCode = intent.getIntExtra("requestCode",0);
            setNotice(year,month,alarmStartDay,day,hour,minute,id,content,requestCode);
            Log.d("year",String.valueOf(year));
        }
    };
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        //unregisterReceiver(notificationReceiver);
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
        } else if(index==3){
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragmentCalendar).commit();
        } else if(index==4){
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragmentChangePwd).commit();
        } else if(index==5){
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragmentMyPage).commit();
        }
    }

    //알람매니저에 알람등록 처리
    public void setNotice(int year, int month, int alarmStartDay,int day, int id, String content, int requestCode) {

        //알람을 수신할 수 있도록 하는 리시버로 인텐트 요청
        Intent receiverIntent = new Intent(this, NotificationReceiver.class);
        receiverIntent.putExtra("content", content);
        receiverIntent.putExtra("requestCode", requestCode);
        receiverIntent.putExtra("id",id);
        receiverIntent.putExtra("year",year);
        receiverIntent.putExtra("month",month);
        receiverIntent.putExtra("day",day);
        receiverIntent.putExtra("alarmStartDay",alarmStartDay);
        receiverIntent.putExtra("hour",0);
        receiverIntent.putExtra("minute",1);

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


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(this, requestCode, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(this, requestCode, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        //date타입으로 변경된 알람시간을 캘린더 타임에 등록
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DATE, alarmStartDay);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,1);


        //알람시간 설정
        //param 1)알람의 타입
        //param 2)알람이 울려야 하는 시간(밀리초)을 나타낸다.
        //param 3)알람이 울릴 때 수행할 작업을 나타냄


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
    public void cancelAlarm(int requestCode){
        Log.d("all cancel success2","cancel");
        Intent receiverIntent = new Intent(this, NotificationReceiver.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(this, requestCode, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(this, requestCode, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

    }
    public void allCancelAlarm(String url , String group){
        Log.d("all cancel success3","cancel");
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {  //응답을 문자열로 받아서 여기다 넣어달란말임(응답을 성공적으로 받았을 떄 이메소드가 자동으로 호출됨
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            LocalDate now = LocalDate.now();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String lu_birth = jsonObject.getString("itemLunarBirth");
                                int itemRequestCode = jsonObject.getInt("itemRequestCode");
                                if(lu_birth.equals("--")){
                                    cancelAlarm(itemRequestCode);
                                    cancelAlarm(itemRequestCode+1);
                                    Log.d("all cancel success","cancel");
                                } else {
                                    cancelAlarm(itemRequestCode);
                                    Log.d("all cancel success1","cancel");
                                }
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
                params.put("itemId", userId);
                params.put("itemGroup",group);
                Log.d("아이템아이디",userId);
                return params;
            }
        };
        //실제 요청 작업을 수행해주는 요청큐 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        //요청큐에 요청 객체 생성
        requestQueue.add(request);
    }
    //모든 알람을 다시 설정하는 메소드(로그아웃시 다 없앴기 때문에 첫 로그인을 하면 이 메소드를 실행함)
    public void allAlarmStart(String url,String group){
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {  //응답을 문자열로 받아서 여기다 넣어달란말임(응답을 성공적으로 받았을 떄 이메소드가 자동으로 호출됨
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            LocalDate now = LocalDate.now();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String name = jsonObject.getString("itemName"); //no가 문자열이라서 바꿔야함.
                                String so_birth = jsonObject.getString("itemSolarBirth");
                                String lu_birth = jsonObject.getString("itemLunarBirth");
                                int itemRequestCode = jsonObject.getInt("itemRequestCode");
                                int month = Integer.parseInt(so_birth.substring(4,6));
                                int day = Integer.parseInt(so_birth.substring(6,8));

                                //생일이 이미 지났다면 다음년도로 알림설정
                                if(now.getMonthValue()>month || (now.getMonthValue()==month && now.getDayOfMonth()>day)){
                                    setNotice(now.getYear()+1,month
                                            ,day-getSharedWhenStartAlarm(),day,itemRequestCode,name+"님의 생일",itemRequestCode);
                                } else {
                                    setNotice(now.getYear(),month
                                            ,day-getSharedWhenStartAlarm(),day,itemRequestCode,name+"님의 생일",itemRequestCode);
                                }
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
                params.put("itemId", userId);
                params.put("itemGroup",group);
                Log.d("아이템아이디",userId);
                return params;
            }
        };
        //실제 요청 작업을 수행해주는 요청큐 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext().getApplicationContext());
        //요청큐에 요청 객체 생성
        requestQueue.add(request);
        Log.d("로드디비2","ㄱ");
    }

    //sharedPreference에 저장된 알림 시작 날짜 가져오기
    public int getSharedWhenStartAlarm(){
        //알람을 며칠전부터 울리는지 사용자가 설정한 대로 main에 있는 변수에 선언해두기
        SharedPreferences alarmSettingPreferences = getSharedPreferences("alarmSetting", Activity.MODE_PRIVATE);
        String whenAlarmStart = alarmSettingPreferences.getString("whenAlarmStart", null);
        if(whenAlarmStart == null){
            return 2;
        } else {
            return Integer.parseInt(whenAlarmStart);
        }
    }




}
