package com.example.dontforgetbirthdayproject.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dontforgetbirthdayproject.listener.OnItemClickListener;
import com.example.dontforgetbirthdayproject.R;
import com.example.dontforgetbirthdayproject.activity.MainActivity;
import com.example.dontforgetbirthdayproject.adapter.HomeAdapter;
import com.example.dontforgetbirthdayproject.calenderDecorators.EventDecorator;
import com.example.dontforgetbirthdayproject.calenderDecorators.SaturdayDecorator;
import com.example.dontforgetbirthdayproject.calenderDecorators.SundayDecorator;
import com.example.dontforgetbirthdayproject.data.ItemData;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class CalendarFragment extends Fragment {
    MainActivity mainActivity;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    private MaterialCalendarView calendarView;
    private ArrayList<ItemData> itemList;
    private HomeAdapter homeAdapter;
    private RecyclerView itemRecyclerView;
    private LinearLayoutManager ItemLinearLayoutManager;
    String loadMatchBirthItemUrl = "http://dfmbd.ivyro.net/LoadItemToCalender.php";
    String loadAllSolarToDot = "http://dfmbd.ivyro.net/LoadAllSolarToDot.php";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_calendar, container, false);
        calendarView = rootView.findViewById(R.id.calendarView);

        ItemLinearLayoutManager = new LinearLayoutManager(getActivity());
        itemRecyclerView = (RecyclerView)rootView.findViewById(R.id.calender_birth_rv);
        itemRecyclerView.setHasFixedSize(true);
        itemRecyclerView.scrollToPosition(0);
        itemRecyclerView.setItemAnimator(new DefaultItemAnimator());
        itemRecyclerView.setLayoutManager(ItemLinearLayoutManager);
        itemList = new ArrayList<>();
        homeAdapter = new HomeAdapter(getActivity().getApplicationContext(),itemList);



        calendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator()
                );
        //db????????? ?????? ?????? ????????? ???????????? ??? ??????
        LoadAllSolarToDot(loadAllSolarToDot);

        //????????? ?????? ?????????
        homeAdapter.setOnItemClicklistener(new OnItemClickListener() {
            @Override
            public void onItemClick(HomeAdapter.CustomViewHolder holder, View view, int position) {
                ItemData item = homeAdapter.getItem(position);
                mainActivity.itemName = item.getTv_item_name();
                mainActivity.itemGroup = item.getTv_item_group();
                mainActivity.itemSolarBirth = item.getTv_item_solar_birth();
                mainActivity.itemlunarBirth = item.getTv_item_lunar_birth();
                mainActivity.itemMemo = item.getTv_item_memo();
                mainActivity.profile_id = item.getIv_profile();
                mainActivity.itemClickPosition = position;
                mainActivity.itemRequestCode = item.getItem_request_code();
                mainActivity.ifTrueCalenderElseHome = true;
                mainActivity.onFragmentChange(2);
                Log.d("????????? ??????","??????");
            }
        });

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

                String solarBirth = intMonthDayToString((date.getMonth()+1),date.getDay());
                LoadMatchBirthItem(loadMatchBirthItemUrl,solarBirth);

            }
        });



        return rootView;
    }
    public void LoadMatchBirthItem(String url,String solarBirth){
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {  //????????? ???????????? ????????? ????????? ??????????????????(????????? ??????????????? ????????? ??? ??????????????? ???????????? ?????????
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(String response) {
                        itemList.clear();
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            itemRecyclerView.setAdapter(homeAdapter);
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String name = jsonObject.getString("itemName"); //no??? ?????????????????? ????????????.
                                String so_birth = jsonObject.getString("itemSolarBirth");
                                String lu_birth = jsonObject.getString("itemLunarBirth");
                                String memo = jsonObject.getString("itemMemo");
                                String group = jsonObject.getString("itemGroup");
                                int is_alarm_on = jsonObject.getInt("itemAlarmOn");
                                String gender = jsonObject.getString("itemGender");
                                int itemRequestCode = jsonObject.getInt("itemRequestCode");
                                //?????? D-day ?????????
                                long solarDday = getBirthDday(so_birth);

                                if(gender.equals("???")){
                                    ItemData itemData= new ItemData(name, group,R.drawable.profile_man_icon,so_birth, lu_birth, memo, is_alarm_on,"D-"+solarDday,"",itemRequestCode); // ??? ?????? ??????????????? ???????????? ?????? ??????, ?????? ?????? ?????????
                                    itemList.add(itemData);

                                } else {
                                    ItemData itemData= new ItemData(name, group,R.drawable.profile_woman_icon,so_birth, lu_birth, memo, is_alarm_on,"D-"+solarDday,"",itemRequestCode); // ??? ?????? ??????????????? ???????????? ?????? ??????, ?????? ?????? ?????????
                                    itemList.add(itemData);

                                }
                                homeAdapter.notifyItemInserted(i);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){ //??????????????? ????????? ????????? ??????
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity().getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            //?????? POST ???????????? ????????? ?????? ??????????????? ????????? getParams ??????????????? ???????????? HashMap ????????? ???????????????.
            //????????? ?????? ?????? ????????? ?????? ?????? ???????????? ?????? ????????? ?????????.
            //POST???????????? ???????????? ????????? ???????????????.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("itemId", mainActivity.userId);
                params.put("itemSolar",solarBirth);
                return params;
            }
        };
        //?????? ?????? ????????? ??????????????? ????????? ?????? ??????
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity().getApplicationContext());
        //???????????? ?????? ?????? ??????
        requestQueue.add(request);
    }
    public void LoadAllSolarToDot(String url){
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {  //????????? ???????????? ????????? ????????? ??????????????????(????????? ??????????????? ????????? ??? ??????????????? ???????????? ?????????
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(String response) {
                        try {
                            LocalDate now = LocalDate.now(); //?????? ?????? ????????????
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String solarBirth = jsonObject.getString("itemSolarBirth");
                                int month = Integer.parseInt(solarBirth.substring(4,6))-1;
                                int day = Integer.parseInt(solarBirth.substring(6,8));
                                calendarView.addDecorator(new EventDecorator(Color.RED, Collections.singleton(CalendarDay.from(now.getYear(),month,day))));

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){ //??????????????? ????????? ????????? ??????
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity().getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            //?????? POST ???????????? ????????? ?????? ??????????????? ????????? getParams ??????????????? ???????????? HashMap ????????? ???????????????.
            //????????? ?????? ?????? ????????? ?????? ?????? ???????????? ?????? ????????? ?????????.
            //POST???????????? ???????????? ????????? ???????????????.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("itemId", mainActivity.userId);
                return params;
            }
        };
        //?????? ?????? ????????? ??????????????? ????????? ?????? ??????
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity().getApplicationContext());
        //???????????? ?????? ?????? ??????
        requestQueue.add(request);
    }
    public String intMonthDayToString(int month,int day){
        String monthDay = "";
        if(month<10){
            if(day<10){
                monthDay = "0"+month+"0"+day;
            } else {
                monthDay = "0"+month+day;
            }
        } else {
            if(day<10){
                monthDay = month+"0"+day;
            } else {
                monthDay = month+day+"";
            }
        }
        return monthDay;
    }
    //D-day ????????? ?????????
    @RequiresApi(api = Build.VERSION_CODES.O)
    public long getBirthDday(String birth){
        long Dday=0;
        LocalDate now = LocalDate.now();
        birth = now.getYear()+birth.substring(4,8);
        //dday ?????????
        String nowMon = now.getDayOfMonth()+"";
        String nowDay = now.getDayOfMonth()+"";
        if(now.getMonthValue()<10){
            nowMon = "0"+now.getMonthValue();
        }
        if(now.getDayOfMonth()<10){
            nowDay = "0"+now.getDayOfMonth();
        }
        String today = now.getYear()+nowMon+nowDay;
        try {
            Date todayForm = new SimpleDateFormat("yyyyMMdd").parse(today);
            Date birthForm = new SimpleDateFormat("yyyyMMdd").parse(birth);
            Dday = (birthForm.getTime() - todayForm.getTime() ) / 1000 / (24*60*60);
            if(Dday<0){
                birth = (now.getYear()+1)+birth.substring(4,8);
                birthForm = new SimpleDateFormat("yyyyMMdd").parse(birth);
                Dday = (birthForm.getTime() - todayForm.getTime() ) / 1000 / (24*60*60);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return Dday;
    }

}