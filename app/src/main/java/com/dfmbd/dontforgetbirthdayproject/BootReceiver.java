package com.dfmbd.dontforgetbirthdayproject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.dfmbd.dontforgetbirthdayproject.activity.LoginActivity;
import com.dfmbd.dontforgetbirthdayproject.activity.MainActivity;
import com.dfmbd.dontforgetbirthdayproject.fragment.HomeFragment;

import java.time.LocalDate;

public class BootReceiver extends BroadcastReceiver {
    NotificationManager manager;
    NotificationCompat.Builder builder;
    MainActivity mainActivity ;
    private static String CHANNEL_ID = "channel1";
    private static String CHANNEL_NAME = "Channel1";
    @Override
    public void onReceive(Context context, Intent intent) {

            //오토 로그인 세팅 제거
            SharedPreferences auto = context.getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = auto.edit();
            editor.clear();
            editor.commit();
            //오레오 이상은 반드시 채널을 설정해줘야 Notification이 작동함
            builder = null;
            int requestCode = 0;
            int id = 0;

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
            Intent intent2 = new Intent(context, LoginActivity.class);

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
            builder.setContentTitle("재부팅 후 알림을 재설정하기 위해선 재로그인이 필요합니다."); //회의명노출
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

    }
}