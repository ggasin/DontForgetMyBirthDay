package com.dfmbd.dontforgetbirthdayproject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;


import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.dfmbd.dontforgetbirthdayproject.activity.LoginActivity;
import com.dfmbd.dontforgetbirthdayproject.activity.MainActivity;
import com.dfmbd.dontforgetbirthdayproject.apiClass.SetAlarmApiManager;
import com.dfmbd.dontforgetbirthdayproject.fragment.HomeFragment;


import java.util.Calendar;


public class NotificationReceiver extends BroadcastReceiver {


    private String TAG = this.getClass().getSimpleName();

    NotificationManager manager;
    NotificationCompat.Builder builder;


    //오레오 이상은 반드시 채널을 설정해줘야 Notification이 작동함
    private static String CHANNEL_ID = "channel1";
    private static String CHANNEL_NAME = "Channel1";
    public static AlarmManager alarmManager;

    //수신되는 인텐트 - The Intent being received.
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {


        Log.e(TAG, "onReceive 알람이 들어옴!!");
        String contentValue ="";
        contentValue= intent.getStringExtra("content");
        int requestCode = intent.getIntExtra("requestCode",0);
        int id = intent.getIntExtra("id",0);
        int day = intent.getIntExtra("day",0);
        int year = intent.getIntExtra("year",0);
        int month = intent.getIntExtra("month",0);
        int hour = intent.getIntExtra("hour",0);
        int minute = intent.getIntExtra("minute",0);
        int alarmStartDay = intent.getIntExtra("alarmStartDay",0);
        //푸시알림을 보내기 위해, 시스템에서 알림 서비스를 생성해주는 코드

        Log.e(TAG, "onReceive 리시버 확인 : " + contentValue);

        builder = null;

        //푸시 알림을 보내기위해 시스템에 권한을 요청하여 생성
        manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        //안드로이드 오레오 버전 대응
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            manager.createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            );
            builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }

        //알림창 클릭 시 지정된 activity 화면으로 이동
        Intent intent2 = new Intent(context,HomeFragment.class);


        // FLAG_UPDATE_CURRENT ->
        // 설명된 PendingIntent가 이미 존재하는 경우 유지하되, 추가 데이터를 이 새 Intent에 있는 것으로 대체함을 나타내는 플래그입니다.
        // getActivity, getBroadcast 및 getService와 함께 사용
        PendingIntent pendingIntent;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            pendingIntent = PendingIntent.getActivity(context,requestCode,intent2,
                    PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context,requestCode,intent2,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }


            int dday = day-alarmStartDay; //생일 날짜 - 현재날 해서 생일로부터 남은 날 계산
            if(dday>0){
                //알림창 제목
                builder.setContentTitle(contentValue+"이 "+dday+"일 남았습니다."); //회의명노출
            } else {
                builder.setContentTitle(contentValue+"입니다! 축하해주세요!"); //회의명노출
            }

            //builder.setContentText(intent.getStringExtra("content")); //회의 내용
            //알림창 아이콘
            builder.setSmallIcon(R.drawable.cake_alarm_icon);
            //알림창 터치시 자동 삭제
            builder.setAutoCancel(true);

            builder.setContentIntent(pendingIntent);

            //푸시알림 빌드
            Notification notification = builder.build();

            //NotificationManager를 이용하여 푸시 알림 보내기
            manager.notify(id,notification);
            //생일날이 오기 전까지 다음날 알람 추가
            //처음엔 알림이 울린 뒤에 receiver에서 mainActivity에 신호를 줘서 mainActivity에서 다음 날짜 알림을 설정하는 방식으로 했는데
            //그렇게 하면 doze모드일때 background에서 작동을 안하는듯함. 그래서 reciever안에서 다음 날짜를 설정하는걸로 구현
            if(dday>0){
                Log.d("인수 확인",String.valueOf(year)+String.valueOf(month)+String.valueOf(day)+String.valueOf(hour)+String.valueOf(minute)
                        +String.valueOf(id)+contentValue+String.valueOf(requestCode));
                try{
                    Intent receiverIntent = new Intent(context, NotificationReceiver.class);
                    receiverIntent.putExtra("content", contentValue);
                    receiverIntent.putExtra("requestCode", requestCode);
                    receiverIntent.putExtra("id",id);
                    receiverIntent.putExtra("year",year);
                    receiverIntent.putExtra("month",month);
                    receiverIntent.putExtra("day",day);
                    receiverIntent.putExtra("alarmStartDay",alarmStartDay+1);
                    receiverIntent.putExtra("hour",hour);
                    receiverIntent.putExtra("minute",minute);
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        pendingIntent = PendingIntent.getBroadcast(context, requestCode, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
                    } else {
                        pendingIntent = PendingIntent.getBroadcast(context, requestCode, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    }
                    //date타입으로 변경된 알람시간을 캘린더 타임에 등록
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month - 1);
                    calendar.set(Calendar.DATE, alarmStartDay+1);
                    calendar.set(Calendar.HOUR_OF_DAY,hour);
                    calendar.set(Calendar.MINUTE,minute);

                    //알람시간 설정
                    //param 1)알람의 타입
                    //param 2)알람이 울려야 하는 시간(밀리초)을 나타낸다.
                    //param 3)알람이 울릴 때 수행할 작업을 나타냄
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);

                    } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);

                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);

                    }
                /*
                mainActivity에 신호를 주는 코드. 신호를 줄 필요가 없어져서 주석처리
                Intent i = new Intent("INTERNET_LOST");
                i.putExtra("year",year);
                i.putExtra("month",month);
                i.putExtra("alarmStartDay",alarmStartDay+1);
                i.putExtra("day",day);
                i.putExtra("hour",hour);
                i.putExtra("minute",minute);
                i.putExtra("id",id);
                i.putExtra("content",contentValue);
                i.putExtra("requestCode",requestCode);
                context.sendBroadcast(i); //푸시알림이 울리고나서 메인액티비티에 있는 메소드를 실행하기 위해 메인액티비티에 액션 전달
                 */
                    Log.d("리시버에서 setNotice 호출 성공","");
                }catch (NullPointerException e){
                    Log.d("리시버에서 setNotice 호출 오류","");

                }
            }




    }


}


