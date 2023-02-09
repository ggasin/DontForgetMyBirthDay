package com.example.dontforgetbirthdayproject;

import android.app.Activity;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

//뒤로가기 한번 누르면 두번 눌러야 종료라고 알려주는 이벤트
public class BackKeyHandler {
    private long backKeyPressedTime = 0;
    private final Activity activity;
    private Toast toast;

    public BackKeyHandler(Activity activity) {
        this.activity=activity;
    }
    private void showGuide() {
        toast = Toast.makeText(activity, "'뒤로' 버튼을 한번 더 누르시면 종료됩니다", Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showGuide(String msg) {
        toast = Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
        toast.show();
    }
    public void onBackPressed(String msg) {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide(msg);
            return;
        }

        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            ActivityCompat.finishAffinity(activity);
            toast.cancel();
        }
    }
}
