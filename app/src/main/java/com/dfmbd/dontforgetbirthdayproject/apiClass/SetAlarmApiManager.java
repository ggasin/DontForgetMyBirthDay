package com.dfmbd.dontforgetbirthdayproject.apiClass;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.RequiresApi;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dfmbd.dontforgetbirthdayproject.NotificationReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SetAlarmApiManager {
    public static AlarmManager alarmManager;
    public static PendingIntent pendingIntent;


    //알람매니저에 알람등록 처리
    public void setNotice(Context context, AlarmManager alarmManager, int year, int month, int alarmStartDay, int day, int id, String content, int requestCode) {


        //알람을 수신할 수 있도록 하는 리시버로 인텐트 요청
        Intent receiverIntent = new Intent(context, NotificationReceiver.class);
        receiverIntent.putExtra("content", content);
        receiverIntent.putExtra("requestCode", requestCode);
        receiverIntent.putExtra("id",id);
        receiverIntent.putExtra("year",year);
        receiverIntent.putExtra("month",month);
        receiverIntent.putExtra("day",day);
        receiverIntent.putExtra("alarmStartDay",alarmStartDay);
        receiverIntent.putExtra("hour",0);
        receiverIntent.putExtra("minute",2);

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
            pendingIntent = PendingIntent.getBroadcast(context, requestCode, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, requestCode, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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
    public void cancelAlarm(Context context,AlarmManager alarmManager,int requestCode){
        Log.d("all cancel success2","cancel");
        Intent receiverIntent = new Intent(context, NotificationReceiver.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(context, requestCode, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, requestCode, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

    }
    public void allCancelAlarm(Context context,AlarmManager alarmManager,String url , String group ,String userId ){
        Log.d("all cancel success3","cancel");
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {  //응답을 문자열로 받아서 여기다 넣어달란말임(응답을 성공적으로 받았을 떄 이메소드가 자동으로 호출됨
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String lu_birth = jsonObject.getString("itemLunarBirth");
                                int itemRequestCode = jsonObject.getInt("itemRequestCode");
                                if(lu_birth.equals("--")){
                                    cancelAlarm(context,alarmManager,itemRequestCode);
                                    cancelAlarm(context,alarmManager,itemRequestCode+1);
                                    Log.d("all cancel success","cancel");
                                } else {
                                    cancelAlarm(context,alarmManager,itemRequestCode);
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
                        Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show();

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
        RequestQueue requestQueue= Volley.newRequestQueue(context);
        //요청큐에 요청 객체 생성
        requestQueue.add(request);
    }
    //모든 알람을 다시 설정하는 메소드(로그아웃시 다 없앴기 때문에 첫 로그인을 하면 이 메소드를 실행함)
    public void allAlarmStart(Context context,AlarmManager alarmManager,String url,String group,int whenAlarmStart ,String userId){
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {  //응답을 문자열로 받아서 여기다 넣어달란말임(응답을 성공적으로 받았을 떄 이메소드가 자동으로 호출됨
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            Calendar calendar = Calendar.getInstance();
                            int nowYear = calendar.get(Calendar.YEAR);
                            int nowMonth = calendar.get(Calendar.MONTH)+1;
                            int nowDay = calendar.get(Calendar.DATE);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String name = jsonObject.getString("itemName"); //no가 문자열이라서 바꿔야함.
                                String so_birth = jsonObject.getString("itemSolarBirth");
                                String lu_birth = jsonObject.getString("itemLunarBirth");
                                int itemRequestCode = jsonObject.getInt("itemRequestCode");
                                int month = Integer.parseInt(so_birth.substring(4,6));
                                int day = Integer.parseInt(so_birth.substring(6,8));
                                int lunarRequestCode = itemRequestCode+1;

                                if(lu_birth.equals("--") || lu_birth.equals("윤달")){
                                    if(nowMonth>month || (nowMonth==month && nowDay>day)){
                                        //알람 추가(양력)
                                        setNotice(context,alarmManager,nowYear+1,month,day-whenAlarmStart,day,itemRequestCode,
                                                name+"님의 생일",itemRequestCode);
                                    } else {
                                        //알람 추가(양력)
                                        setNotice(context,alarmManager,nowYear,month,day-whenAlarmStart,day,itemRequestCode,
                                                name+"님의 생일",itemRequestCode);
                                    }
                                } else {
                                    int lunarMonth = Integer.parseInt(lu_birth.substring(4,6));
                                    int lunarDay = Integer.parseInt(lu_birth.substring(6,8));
                                    //양력
                                    if(nowMonth>month || (nowMonth==month && nowDay>day)){
                                        setNotice(context,alarmManager,nowYear+1,month,day-whenAlarmStart,day,itemRequestCode,
                                                name+"님의 생일",itemRequestCode);
                                    } else {
                                        setNotice(context,alarmManager,nowYear,month,day-whenAlarmStart,day,itemRequestCode,
                                                name+"님의 생일",itemRequestCode);
                                    }
                                    //음력
                                    if(nowMonth>lunarMonth || (nowMonth==lunarMonth && nowDay>lunarDay )){
                                        setNotice(context,alarmManager,nowYear+1,lunarMonth,lunarDay-whenAlarmStart,lunarDay,lunarRequestCode,
                                                name+"님의 음력 생일",lunarRequestCode);
                                    } else {
                                        setNotice(context,alarmManager,nowYear,lunarMonth,lunarDay-whenAlarmStart,lunarDay,lunarRequestCode,
                                                name+"님의 음력 생일",lunarRequestCode);
                                    }
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
                        Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show();

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
        RequestQueue requestQueue= Volley.newRequestQueue(context.getApplicationContext());
        //요청큐에 요청 객체 생성
        requestQueue.add(request);
        Log.d("로드디비2","ㄱ");
    }
    //알림 설정
    public void whenAddOrUpdateItemSetNotice(Context context,AlarmManager alarmManager,boolean lunarIsChecked,int month, int day, int whenAlarmStart,String lunarBirth,String name,int solarRequestCode){
        int lunarRequestCode = solarRequestCode+1;
        Calendar calendar = Calendar.getInstance();
        int nowYear = calendar.get(Calendar.YEAR);
        int nowMonth = calendar.get(Calendar.MONTH)+1;
        int nowDay = calendar.get(Calendar.DATE);
        //음력 체크 되어있고 윤달이 아니라면 음력도 알림 설정
        if(lunarIsChecked && !lunarBirth.equals("윤달")){
            Log.d("mainLunarBirth3",lunarIsChecked+"");
            Log.d("mainLunarBirth33",lunarBirth.equals("윤달")+"");
            Log.d("mainLunarBirth33",lunarBirth+"");
            int lunarMonth = Integer.parseInt(lunarBirth.substring(4,6));
            int lunarDay = Integer.parseInt(lunarBirth.substring(6,8));
            //다음년도 양력 설정
            if(nowMonth>month || (nowMonth==month && nowDay>day)){
                setNotice(context,alarmManager,nowYear+1,month,day-whenAlarmStart,day,solarRequestCode,
                        name+"님의 생일",solarRequestCode);
            } else {
                //올해 양력 설정
                setNotice(context,alarmManager,nowYear,month,day-whenAlarmStart,day,solarRequestCode,
                        name+"님의 생일",solarRequestCode);
            }

            if(nowMonth>lunarMonth || (nowMonth==lunarMonth && nowDay>lunarDay )){
                //다음년도 음력 설정
                setNotice(context,alarmManager,nowYear+1,lunarMonth ,lunarDay-whenAlarmStart,lunarDay,lunarRequestCode,
                        name+"님의 음력 생일",lunarRequestCode);
            } else {
                //올해 음력 설정
                setNotice(context,alarmManager,nowYear,lunarMonth,lunarDay-whenAlarmStart,lunarDay,lunarRequestCode,
                        name+"님의 음력 생일",lunarRequestCode);
            }
        } else {
            Log.d("mainLunarBirth4","");
            if(nowMonth>month || (nowMonth==month && nowDay>day)){
                //다음년도 양력 설정
                setNotice(context,alarmManager,nowYear+1,month,day-whenAlarmStart,day,solarRequestCode,
                        name+"님의 생일",solarRequestCode);
            } else {
                //올해 양력 설정
                setNotice(context,alarmManager,nowYear,month,day-whenAlarmStart,day,solarRequestCode,
                        name+"님의 생일",solarRequestCode);
            }
        }
    }
    public void setLunarAlarm(Context context, AlarmManager alarmManager,  int lunarMonth, int alarmStartDay, int lunarDay, String name, int requestCode){
        Calendar calendar = Calendar.getInstance();
        int nowYear = calendar.get(Calendar.YEAR);
        int nowMonth = calendar.get(Calendar.MONTH)+1;
        int nowDay = calendar.get(Calendar.DATE);
        //음력
        if(nowMonth>lunarMonth || (nowMonth==lunarMonth && nowDay>lunarDay )){
            setNotice(context,alarmManager,nowYear+1,lunarMonth,lunarDay-alarmStartDay,lunarDay,requestCode+1,
                    name+"님의 음력 생일",requestCode+1);
            Log.d("setLunarAlarmRequest",name+"이름"+requestCode+"+"+lunarMonth+"월"+lunarDay+"일"+alarmStartDay+"시작일");

        } else {
            setNotice(context, alarmManager, nowYear , lunarMonth, lunarDay - alarmStartDay, lunarDay, requestCode + 1,
                    name + "님의 음력 생일", requestCode + 1);
            Log.d("setLunarAlarmRequest1",name+"이름"+requestCode+"+"+lunarMonth+"월"+lunarDay+"일"+alarmStartDay+"시작일");

        }
    }


}
