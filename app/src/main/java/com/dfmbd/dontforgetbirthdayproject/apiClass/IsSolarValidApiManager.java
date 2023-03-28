package com.dfmbd.dontforgetbirthdayproject.apiClass;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.util.Calendar;

public class IsSolarValidApiManager {

    public boolean solarValidCheck(String solarBirth,int solarBirthYear, int solarBirthMonth, int solarBirthDay){
        boolean isValidBirth = false;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        //유효한 생년월일인지 체크
        //1850년보다 많고 오늘날의 연도보다 생년월일이 적어야하고, 달은 1보다 크고 12보다 작아야한다
        if (solarBirthYear > 1850 && solarBirthYear <= year && solarBirthMonth >= 1 && solarBirthMonth <= 12) {
            //30일이 끝인 달은 생일이 30일보다 작아야 유효하다
            if ((solarBirthMonth==4) || (solarBirthMonth==6) || (solarBirthMonth==9) || (solarBirthMonth==11)) {
                if (solarBirthDay >= 1 && solarBirthDay <= 30) {
                    isValidBirth = true;
                } else {
                    isValidBirth = false;
                }
            } else if (solarBirthMonth == 2) {
                //생일이 2월인데 윤년이면 29일까지 허용
                if (((solarBirthYear % 4 == 0 && solarBirthYear % 100 != 0) || solarBirthYear % 400 == 0) && (solarBirthDay <= 29 && solarBirthDay >= 1)) {
                    isValidBirth = true;
                } else if (solarBirthDay <= 28) {
                    isValidBirth = true;
                } else {
                    isValidBirth = false;
                }
            } else if (solarBirthDay <= 31 && solarBirthDay >= 1) {
                Log.d("solarBirthMonth 30??",solarBirthMonth+"");
                isValidBirth = true;
            } else {
                isValidBirth = false;
            }
        } else {
            isValidBirth = false;
        }

        return isValidBirth;
    }
}
