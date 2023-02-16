package com.example.dontforgetbirthdayproject.data;

public class RequestCodeData {
    public static int requestCode;

    public RequestCodeData(int requestCode) {
        this.requestCode = requestCode;
    }

    public static int getRequestCode() {
        return requestCode;
    }

    public static void setRequestCode(int requestCode) {
        RequestCodeData.requestCode = requestCode;
    }
}
