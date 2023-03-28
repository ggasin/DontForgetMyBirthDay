package com.dfmbd.dontforgetbirthdayproject.data;

public class ItemData {
    private int iv_profile;
    private String tv_item_group;
    private String tv_item_name;
    private String tv_item_solar_birth;
    private String tv_item_lunar_birth;
    private String tv_item_memo;
    private String tv_item_so_dday;
    private String tv_item_lu_dday;
    private int item_request_code;
    private int item_alarm_on;
    public int getItem_request_code() {
        return item_request_code;
    }

    public void setItem_request_code(int item_request_code) {
        this.item_request_code = item_request_code;
    }



    public int getIv_profile() {
        return iv_profile;
    }

    public void setIv_profile(int iv_profile) {
        this.iv_profile = iv_profile;
    }


    public String getTv_item_so_dday() {
        return tv_item_so_dday;
    }

    public void setTv_item_so_dday(String tv_item_so_dday) {
        this.tv_item_so_dday = tv_item_so_dday;
    }

    public String getTv_item_lu_dday() {
        return tv_item_lu_dday;
    }

    public void setTv_item_lu_dday(String tv_item_lu_dday) {
        this.tv_item_lu_dday = tv_item_lu_dday;
    }



    public int getItem_alarm_on() {
        return item_alarm_on;
    }

    public void setItem_alarm_on(int item_alarm_on) {
        this.item_alarm_on = item_alarm_on;
    }

    public ItemData(String tv_item_name, String tv_item_group,int iv_profile ,String tv_item_solar_birth, String tv_item_lunar_birth, String tv_item_memo,
                    int item_alram_on,String tv_item_so_dday,String tv_item_lu_dday,int item_request_code) {
        this.iv_profile = iv_profile;
        this.tv_item_group = tv_item_group;
        this.tv_item_name = tv_item_name;
        this.tv_item_solar_birth = tv_item_solar_birth;
        this.tv_item_lunar_birth = tv_item_lunar_birth;
        this.tv_item_memo = tv_item_memo;
        this.item_alarm_on = item_alram_on;
        this.tv_item_so_dday = tv_item_so_dday;
        this.tv_item_lu_dday = tv_item_lu_dday;
        this.item_request_code = item_request_code;
    }

    public String getTv_item_group() {
        return tv_item_group;
    }

    public void setTv_item_group(String tv_item_group) {
        this.tv_item_group = tv_item_group;
    }

    public String getTv_item_name() {
        return tv_item_name;
    }

    public void setTv_item_name(String tv_item_name) {
        this.tv_item_name = tv_item_name;
    }

    public String getTv_item_solar_birth() {
        return tv_item_solar_birth;
    }

    public void setTv_item_solar_birth(String tv_item_solar_birth) {
        this.tv_item_solar_birth = tv_item_solar_birth;
    }

    public String getTv_item_lunar_birth() {
        return tv_item_lunar_birth;
    }

    public void setTv_item_lunar_birth(String tv_item_lunar_birth) {
        this.tv_item_lunar_birth = tv_item_lunar_birth;
    }

    public String getTv_item_memo() {
        return tv_item_memo;
    }

    public void setTv_item_memo(String tv_item_memo) {
        this.tv_item_memo = tv_item_memo;
    }


}
