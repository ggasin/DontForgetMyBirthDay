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
    @Override
    public void onStop() {
        mainActivity.beforeFragment = "캘린더 페이지";
        Log.d("beforeFragmentCal",mainActivity.beforeFragment);
        super.onStop();
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
        //db로부터 모든 양력 생일을 가져와서 점 찍기
        LoadAllSolarToDot(loadAllSolarToDot);

        //아이템 클릭 이벤트
        //추후 수정-> 여기서 아이템 클릭하고 홈으로 돌아가게 되면 캘린더 아이콘이 검은색임
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
                if(item.getItem_alarm_on()==1){
                    mainActivity.itemAlarmOnoff = true;
                } else{
                    mainActivity.itemAlarmOnoff =false;
                }
                mainActivity.onFragmentChange(2);
                Log.d("아이템 클릭","클릭");
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
                new Response.Listener<String>() {  //응답을 문자열로 받아서 여기다 넣어달란말임(응답을 성공적으로 받았을 떄 이메소드가 자동으로 호출됨
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(String response) {
                        itemList.clear();
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            itemRecyclerView.setAdapter(homeAdapter);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String name = jsonObject.getString("itemName"); //no가 문자열이라서 바꿔야함.
                                String so_birth = jsonObject.getString("itemSolarBirth");
                                String lu_birth = jsonObject.getString("itemLunarBirth");
                                String memo = jsonObject.getString("itemMemo");
                                String group = jsonObject.getString("itemGroup");
                                int is_alarm_on = jsonObject.getInt("itemAlarmOn");
                                String gender = jsonObject.getString("itemGender");
                                int itemRequestCode = jsonObject.getInt("itemRequestCode");
                                //생일 D-day 구하기
                                long solarDday = getBirthDday(so_birth);

                                if(gender.equals("남")){
                                    ItemData itemData= new ItemData(name, group,R.drawable.profile_man_icon,so_birth, lu_birth, memo, is_alarm_on,"D-"+solarDday,"",itemRequestCode); // 첫 번째 매개변수는 몇번째에 추가 될지, 제일 위에 오도록
                                    itemList.add(itemData);

                                } else {
                                    ItemData itemData= new ItemData(name, group,R.drawable.profile_woman_icon,so_birth, lu_birth, memo, is_alarm_on,"D-"+solarDday,"",itemRequestCode); // 첫 번째 매개변수는 몇번째에 추가 될지, 제일 위에 오도록
                                    itemList.add(itemData);

                                }
                                homeAdapter.notifyItemInserted(i);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){ //에러발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity().getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            //만약 POST 방식에서 전달할 요청 파라미터가 있다면 getParams 메소드에서 반환하는 HashMap 객체에 넣어줍니다.
            //이렇게 만든 요청 객체는 요청 큐에 넣어주는 것만 해주면 됩니다.
            //POST방식으로 안할거면 없어도 되는거같다.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("itemId", mainActivity.userId);
                params.put("itemSolar",solarBirth);
                return params;
            }
        };
        //실제 요청 작업을 수행해주는 요청큐 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity().getApplicationContext());
        //요청큐에 요청 객체 생성
        requestQueue.add(request);
    }
    public void LoadAllSolarToDot(String url){
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {  //응답을 문자열로 받아서 여기다 넣어달란말임(응답을 성공적으로 받았을 떄 이메소드가 자동으로 호출됨
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(String response) {
                        try {
                            LocalDate now = LocalDate.now(); //현재 날짜 가져오기
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
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
                new Response.ErrorListener(){ //에러발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity().getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            //만약 POST 방식에서 전달할 요청 파라미터가 있다면 getParams 메소드에서 반환하는 HashMap 객체에 넣어줍니다.
            //이렇게 만든 요청 객체는 요청 큐에 넣어주는 것만 해주면 됩니다.
            //POST방식으로 안할거면 없어도 되는거같다.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("itemId", mainActivity.userId);
                return params;
            }
        };
        //실제 요청 작업을 수행해주는 요청큐 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity().getApplicationContext());
        //요청큐에 요청 객체 생성
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
    //D-day 구하기 메소드
    @RequiresApi(api = Build.VERSION_CODES.O)
    public long getBirthDday(String birth){
        long Dday=0;
        LocalDate now = LocalDate.now();
        birth = now.getYear()+birth.substring(4,8);
        //dday 구하기
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