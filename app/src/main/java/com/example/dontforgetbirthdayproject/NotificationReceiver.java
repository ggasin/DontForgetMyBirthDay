package com.example.dontforgetbirthdayproject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.dontforgetbirthdayproject.activity.MainActivity;
import com.example.dontforgetbirthdayproject.fragment.HomeFragment;

import java.time.LocalDate;

public class NotificationReceiver extends BroadcastReceiver {

    private String TAG = this.getClass().getSimpleName();

    NotificationManager manager;
    NotificationCompat.Builder builder;

    //오레오 이상은 반드시 채널을 설정해줘야 Notification이 작동함
    private static String CHANNEL_ID = "channel1";
    private static String CHANNEL_NAME = "Channel1";

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

        Log.e(TAG, "onReceive contentValue값 확인 : " + contentValue);

        builder = null;

        //푸시 알림을 보내기위해 시스템에 권한을 요청하여 생성
        manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        //안드로이드 오레오 버전 대응
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            manager.createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            );
            builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }

        //알림창 클릭 시 지정된 activity 화면으로 이동
        Intent intent2 = new Intent(context, HomeFragment.class);

        // FLAG_UPDATE_CURRENT ->
        // 설명된 PendingIntent가 이미 존재하는 경우 유지하되, 추가 데이터를 이 새 Intent에 있는 것으로 대체함을 나타내는 플래그입니다.
        // getActivity, getBroadcast 및 getService와 함께 사용
        PendingIntent pendingIntent;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            pendingIntent = PendingIntent.getActivity(context,requestCode,intent2,
                    PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context,requestCode,intent2,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        LocalDate now = LocalDate.now(); //현재 날짜 가져오기
        int dday = day-alarmStartDay; //생일 날짜 - 현재날 해서 생일로부터 남은 날 계산
        //알림창 제목
        builder.setContentTitle(contentValue+dday+"일 남았습니다."); //회의명노출
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
        if(day+2- now.getDayOfMonth()>=0){
            Log.d("인수 확인",String.valueOf(year)+String.valueOf(month)+String.valueOf(day)+String.valueOf(hour)+String.valueOf(minute)
                    +String.valueOf(id)+contentValue+String.valueOf(requestCode));
            MainActivity.isPushAlarmSend = true;
        }
    }
}
