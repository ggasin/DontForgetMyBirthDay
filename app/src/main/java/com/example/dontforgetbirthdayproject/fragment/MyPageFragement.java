package com.example.dontforgetbirthdayproject.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dontforgetbirthdayproject.activity.PrivatePolicyActivity;
import com.example.dontforgetbirthdayproject.apiClass.GetLunarSolarApiManager;
import com.example.dontforgetbirthdayproject.R;
import com.example.dontforgetbirthdayproject.activity.LoginActivity;
import com.example.dontforgetbirthdayproject.activity.MainActivity;
import com.example.dontforgetbirthdayproject.apiClass.LoadingApiManager;
import com.example.dontforgetbirthdayproject.apiClass.SetAlarmApiManager;
import com.example.dontforgetbirthdayproject.request.MyPageRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


public class MyPageFragement extends Fragment {
    MainActivity mainActivity;

    private TextView logoutTextBtn,myName,myId,myEmail;
    private Button goChangePwdBtn,goSettingAlarm,completeSettingAlarm,setLunarNowYearBtn,goPrivatePolicy,deleteAccountBtn;
    private LinearLayout settingAlarmLy,progressBarLy;
    private FrameLayout mainLy;
    private RadioGroup alarmSettingRadioGroup;
    private RadioButton allAlarmOnRadioBtn,allAlarmOffRadioBtn;
    String settingAllAlarmResult="",settingAlarm=null,whenAlarmStart=null;
    private Spinner alarmStartDaySpinner;
    boolean onAlarmSettingMode = false;
    String AllAlarmOnURL = "http://dfmbd.ivyro.net/AllAlarmOn.php";
    String AllAlarmOffURL = "http://dfmbd.ivyro.net/AllAlarmOff.php";
    String deleteAccountURL = "http://dfmbd.ivyro.net/DeleteAccount.php";
    String haveLunarPersonURL = "http://dfmbd.ivyro.net/LoadHaveLunarPerson.php";
    String updateNowYearLunarURL = "http://dfmbd.ivyro.net/UpdateNowYearLunar.php";
    HashMap<String, String> lunarResultMap = new HashMap<String, String>();
    HashMap<String, String> haveLunarPersonSolarMap = new HashMap<String, String>();
    HashMap<String, Integer> haveLunarPersonRequestCodeMap = new HashMap<String, Integer>();
    ArrayList<String> haveLunarPersonName = new ArrayList<>();
    GetLunarSolarApiManager getLunarSolarApiManager = new GetLunarSolarApiManager();
    SetAlarmApiManager setAlarmApiManager = new SetAlarmApiManager();
    LoadingApiManager loadingApiManager = new LoadingApiManager();
    public static ArrayList<String> lunarArr = new ArrayList<String>();
    public static ArrayList<String> nowYearLunarArr = new ArrayList<>();
    public static ArrayList<String> solarArr = new ArrayList<>();
    String lunarBirth = "";
    private ProgressBar loadingSpinner;
    //onAttach 는 fragment가 activity에 올라온 순간
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
        mainActivity.beforeFragment = "마이페이지";
        Log.d("beforeFragmentMy",mainActivity.beforeFragment);
        super.onStop();
    }

    public MyPageFragement() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_my_page_fragement, container, false);
        logoutTextBtn = rootView.findViewById(R.id.mp_logout_text_btn);
        myId = rootView.findViewById(R.id.mp_id_v);
        myName = rootView.findViewById(R.id.mp_name_v);
        myEmail = rootView.findViewById(R.id.mp_email_v);
        goChangePwdBtn =rootView.findViewById(R.id.mp_go_change_pwd_btn);
        goSettingAlarm = rootView.findViewById(R.id.mp_go_setting_push_alarm_btn);
        goPrivatePolicy = rootView.findViewById(R.id.mp_go_privacy_policy);
        settingAlarmLy = rootView.findViewById(R.id.mp_alarm_setting_ly);
        completeSettingAlarm = rootView.findViewById(R.id.mp_complete_setting_alarm_btn);
        alarmSettingRadioGroup = rootView.findViewById(R.id.mp_alarm_setting_redio_group);
        allAlarmOnRadioBtn = rootView.findViewById(R.id.mp_all_alarm_on_radio_btn);
        allAlarmOffRadioBtn = rootView.findViewById(R.id.mp_all_alarm_off_radio_btn);
        alarmStartDaySpinner = rootView.findViewById(R.id.mp_set_alarm_start_spinner);
        deleteAccountBtn = rootView.findViewById(R.id.mp_delete_account_btn);
        setLunarNowYearBtn = rootView.findViewById(R.id.mp_set_lunar_now_year_btn);
        loadingSpinner = rootView.findViewById(R.id.mp_progress_bar);
        progressBarLy = rootView.findViewById(R.id.mp_progress_bar_ly);
        mainLy = rootView.findViewById(R.id.mp_);

        SharedPreferences alarmSettingPreferences = getActivity().getSharedPreferences("alarmSetting", Activity.MODE_PRIVATE);
        settingAlarm= alarmSettingPreferences.getString("settingAlarm", null);//
        whenAlarmStart = alarmSettingPreferences.getString("whenAlarmStart", null);


        //알람시작일 스피너 설정
        ArrayAdapter alarmStartDayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.setWhenAlarmStartArray, android.R.layout.simple_spinner_dropdown_item);
        alarmStartDayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        alarmStartDaySpinner.setAdapter(alarmStartDayAdapter); //어댑터에 연결해줍니다.

        //스피너,라디오 버튼 디폴트
        if(settingAlarm == null && whenAlarmStart == null){
            alarmStartDaySpinner.setSelection(2);
            settingAllAlarmResult = "전체 알람 켜기";
        } else {
            switch (settingAlarm){
                case "전체 알람 끄기":  allAlarmOffRadioBtn.setChecked(true);
                    settingAllAlarmResult = "전체 알람 끄기";
                    break;
                case "전체 알람 켜기":  allAlarmOnRadioBtn.setChecked(true);
                    settingAllAlarmResult = "전체 알람 켜기";
                    break;
                default: 
                    break;
            }
            switch (whenAlarmStart){
                case "0":  alarmStartDaySpinner.setSelection(0);
                    break;
                case "1":  alarmStartDaySpinner.setSelection(1);
                    break;
                case "2":  alarmStartDaySpinner.setSelection(2);
                    break;
                case "3":  alarmStartDaySpinner.setSelection(3);
                    break;
                case "4":  alarmStartDaySpinner.setSelection(4);
                    break;
                case "5":  alarmStartDaySpinner.setSelection(5);
                    break;
                case "6":  alarmStartDaySpinner.setSelection(6);
                    break;
                case "7":  alarmStartDaySpinner.setSelection(7);
                    break;
            }
        }




        //비밀번호 변경하기 버튼
        goChangePwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.onFragmentChange(4);
            }
        });
        //알람 세팅 버튼
        goSettingAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!onAlarmSettingMode){
                    settingAlarmLy.setVisibility(View.VISIBLE);
                    onAlarmSettingMode = true;
                } else {
                    settingAlarmLy.setVisibility(View.GONE);
                    onAlarmSettingMode = false;
                }

            }
        });
        //알람 세팅 완료 버튼
        completeSettingAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences alarmSettingPreferences = getActivity().getSharedPreferences("alarmSetting",Context.MODE_PRIVATE);
                SharedPreferences.Editor alarmSetting = alarmSettingPreferences.edit();
                settingAlarmLy.setVisibility(View.GONE);
                onAlarmSettingMode = false;
                int whenAlarmStart=0;
                switch (alarmStartDaySpinner.getSelectedItem().toString()){
                    case "당일":
                        alarmSetting.putString("whenAlarmStart", "0");
                        whenAlarmStart = 0;
                        break;
                    case "1일전부터":
                        alarmSetting.putString("whenAlarmStart", "1");
                        whenAlarmStart = 1;
                        break;
                    case "2일전부터":
                        alarmSetting.putString("whenAlarmStart", "2");
                        whenAlarmStart = 2;
                        break;
                    case "3일전부터":
                        alarmSetting.putString("whenAlarmStart", "3");
                        whenAlarmStart = 3;
                        break;
                    case "4일전부터":
                        alarmSetting.putString("whenAlarmStart","4");
                        whenAlarmStart = 4;
                        Log.d("sw 2","");
                        break;
                    case "5일전부터":
                        alarmSetting.putString("whenAlarmStart", "5");
                        whenAlarmStart = 5;
                        break;
                    case "6일전부터":
                        alarmSetting.putString("whenAlarmStart", "6");
                        whenAlarmStart = 6;
                        break;
                    case "7일전부터":
                        alarmSetting.putString("whenAlarmStart", "7");
                        whenAlarmStart = 7;
                        break;
                }
                switch (settingAllAlarmResult){
                    case "전체 알람 끄기":
                        //모든 알람 끄기
                        setAlarmApiManager.allCancelAlarm(getActivity().getApplicationContext(),mainActivity.alarmManager,AllAlarmOffURL,"전체", mainActivity.userId);
                        alarmSetting.putString("settingAlarm", "전체 알람 끄기");
                        Log.d("sw 1","");
                        break;
                    case "전체 알람 켜기":
                        setAlarmApiManager.allAlarmStart( getActivity().getApplicationContext(), mainActivity.alarmManager, AllAlarmOnURL,"전체",whenAlarmStart,mainActivity.userId);
                        alarmSetting.putString("settingAlarm", "전체 알람 켜기");
                        Log.d("sw 1","");
                        break;

                }
                alarmSetting.commit();
                Toast.makeText(getActivity().getApplicationContext(),"설정 완료",Toast.LENGTH_SHORT).show();

            }
        });

        //알람 라디오 그룹 체크 리스너
        alarmSettingRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = group.findViewById(checkedId);
                settingAllAlarmResult = rb.getText().toString();
                Log.d("set radio",settingAllAlarmResult);

            }
        });

        //정보 불러오기
        loadMyInfo();
        Handler handler = new Handler(Looper.getMainLooper());
        Handler handler1 = new Handler(Looper.getMainLooper());

        //올해 년도를 기준으로 음력 초기화 하는 버튼
        //0318 handler 사용하는 방법으로 추후에 교체하기
        setLunarNowYearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //안전을 위해서 다이얼로그 추가
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("올해로 음력생일들을 재설정 하시겠습니까?")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loadingApiManager.showLoading(progressBarLy,loadingSpinner,getActivity()); // 로딩 보여주기
                                mainLy.setBackgroundColor(Color.parseColor("#E0E0E0")); //배경 회색으로
                                //DB에서 유효한 음력생일을 가진 사람들 가져오기
                                loadHaveLunarPersonFromDB(haveLunarPersonURL, new Runnable() {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void run() {
                                        if (haveLunarPersonName.size() == 0) {
                                            Log.d("haveLunarPersonMap", "없음");
                                        } else {
                                            LocalDate now = LocalDate.now();
                                            Thread thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    for (int i=0; i < haveLunarPersonName.size(); i++) {
                                                        String solarBirth = haveLunarPersonSolarMap.get(haveLunarPersonName.get(i));
                                                        String name = haveLunarPersonName.get(i);
                                                        Log.d("lunarTask","");
                                                        //태어난 날 양력 기준으로 음력 구하기
                                                        lunarArr = getLunarSolarApiManager.getLunarInMp(solarBirth.substring(0, 4), solarBirth.substring(4, 6), solarBirth.substring(6, 8));
                                                        //구한 음력을 토대로 올해에서 해당하는 양력 구하기
                                                        solarArr = getLunarSolarApiManager.getSolar(String.valueOf(now.getYear()), lunarArr.get(2), lunarArr.get(0));
                                                        //양력으로 변환했을 때 계산값이 올해보다 다음년도의 결과값이 나오면 전년도로 계산해야됨
                                                        // 1월 초반 생일자가 음력체크를 할때 이번년도에 해당하는 음력이 없으면 양력으로 변환할때 다음년도로 넘어가서 계산하게 됨.
                                                        if (Integer.parseInt(solarArr.get(2)) > now.getYear()) {
                                                            solarArr = getLunarSolarApiManager.getSolar(String.valueOf(now.getYear() - 1), lunarArr.get(2), lunarArr.get(0));
                                                        }
                                                        //lunarBirth라는 변수에 계산된 음력 값 저장
                                                        lunarBirth = solarArr.get(2) + solarArr.get(1) + solarArr.get(0);
                                                        lunarResultMap.put(name,lunarBirth);
                                                        Log.d("lunarResult",lunarResultMap+"");
                                                    }
                                                }
                                            });
                                            thread.start();
                                            try {
                                                thread.join();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            for(int i=0;i<haveLunarPersonName.size();i++){
                                                String name = haveLunarPersonName.get(i);
                                                //계산된 음력 db에 업데이트
                                                updateNowYearLunarToDB(updateNowYearLunarURL,name,lunarResultMap.get(name));
                                                int lunarMonth = Integer.parseInt(lunarResultMap.get(name).substring(4,6));
                                                int lunarDay = Integer.parseInt(lunarResultMap.get(name).substring(6,8));
                                                int whenAlarmStart = mainActivity.getSharedWhenStartAlarm();
                                                //재설정된 음력으로 다시 알람 설정
                                                setAlarmApiManager.setLunarAlarm(getActivity().getApplicationContext(),mainActivity.alarmManager,lunarMonth,
                                                        whenAlarmStart,lunarDay,name,haveLunarPersonRequestCodeMap.get(name));
                                                Log.d("lunarTaskResult", name+lunarBirth);
                                            }
                                            //작업이 모두 완료되었으므로 로딩 숨기기
                                            loadingApiManager.hideLoading(progressBarLy,loadingSpinner,getActivity());
                                            mainLy.setBackgroundColor(Color.TRANSPARENT); //배경 되돌리기
                                            haveLunarPersonName.clear();
                                            haveLunarPersonSolarMap.clear();
                                            lunarResultMap.clear();
                                            haveLunarPersonRequestCodeMap.clear();
                                            nowYearLunarArr.clear();
                                            solarArr.clear();
                                            lunarArr.clear();
                                            Toast.makeText(getActivity().getApplicationContext(),"초기화가 완료되었습니다.",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).show();

            }
        });



        //로그아웃 버튼 이벤트
        logoutTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //로그아웃하면 알람 울리지 않도록 모든 알람 끄기
                setAlarmApiManager.allCancelAlarm(getActivity().getApplicationContext(),mainActivity.alarmManager,AllAlarmOffURL,"전체", mainActivity.userId);
                Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                //오토 로그인 세팅 제거
                SharedPreferences auto = mainActivity.getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = auto.edit();
                editor.clear();
                editor.commit();
                //알림 세팅 제거
                SharedPreferences alarmsetting = mainActivity.getSharedPreferences("alarmSetting", Activity.MODE_PRIVATE);
                SharedPreferences.Editor alarmEditor = alarmsetting.edit();
                alarmEditor.clear();
                alarmEditor.commit();

                Toast.makeText(getActivity().getApplicationContext(), "로그아웃", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        });
        //회원탈퇴 버튼 이벤트
        deleteAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String warningInfo = "정말 탈퇴하시겠습니까?\n기록된 데이터는 복구가 불가능합니다.";
                //string 색 변경
                SpannableString spannableString = new SpannableString(warningInfo);
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
                spannableString.setSpan(colorSpan, 0, warningInfo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                //안전을 위해서 다이얼로그 추가
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(spannableString)
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteAccount(deleteAccountURL);
                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).show();

            }
        });
        goPrivatePolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PrivatePolicyActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }
    public void loadMyInfo(){
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                        if(success){
                            String userName = jsonObject.getString("userName");
                            String userId = jsonObject.getString("userID");
                            String userEmail = jsonObject.getString("userEmail");
                            myId.setText(userId);
                            myEmail.setText(userEmail);
                            myName.setText(userName);
                        } else{
                            Toast.makeText(getContext(),"정보 불러오기 실패",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }

            }
        };
        MyPageRequest myPageRequest = new MyPageRequest(mainActivity.userId,responseListener);
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        queue.add(myPageRequest);
    }

    //회원 탈퇴하면 db에서 회원 정보 삭제
    public void deleteAccount(String url){
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {  //응답을 문자열로 받아서 여기다 넣어달란말임(응답을 성공적으로 받았을 떄 이메소드가 자동으로 호출됨
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if(success){
                                Toast.makeText(getContext(),"탈퇴 완료",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            } else{
                                Toast.makeText(getContext(),"탈퇴 실패.",Toast.LENGTH_SHORT).show();
                                return;
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
                        Log.d("group load db : ","error");
                    }
                }
        ){
            //만약 POST 방식에서 전달할 요청 파라미터가 있다면 getParams 메소드에서 반환하는 HashMap 객체에 넣어줍니다.
            //이렇게 만든 요청 객체는 요청 큐에 넣어주는 것만 해주면 됩니다.
            //POST방식으로 안할거면 없어도 되는거같다.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userId", mainActivity.userId);
                return params;
            }
        };
        //실제 요청 작업을 수행해주는 요청큐 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity().getApplicationContext());
        //요청큐에 요청 객체 생성
        requestQueue.add(request);
    }

    //음력 생일 가진사람 db로부터 정보 가져오기.
    //0314 아직 음력 초기화 후 음력 알림 추가는 미완
    public void loadHaveLunarPersonFromDB(String url, final Runnable callback) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {  //응답을 문자열로 받아서 여기다 넣어달란말임(응답을 성공적으로 받았을 떄 이메소드가 자동으로 호출됨
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String name = jsonObject.getString("itemName"); //no가 문자열이라서 바꿔야함.
                                String solarBirth = jsonObject.getString("itemSolarBirth");
                                int itemRequestCode = jsonObject.getInt("itemRequestCode");
                                haveLunarPersonName.add(i,name);
                                haveLunarPersonSolarMap.put(name,solarBirth);
                                Log.d("haveLunarSolar",haveLunarPersonSolarMap.get(name));
                                haveLunarPersonRequestCodeMap.put(name,itemRequestCode);
                                Log.d("haveLunarName",name);
                                Log.d("lunarSolar",solarBirth);


                            }
                            if (callback != null) {
                                callback.run();
                            }

                        } catch (JSONException e) {
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            String exceptionAsStrting = sw.toString();
                            Log.e("haveError", exceptionAsStrting);
                            e.printStackTrace();


                        }
                    }
                },
                new Response.ErrorListener() { //에러발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity().getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();

                    }
                }
        ) {
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
    //음력 초기화 후 db에 업데이트
    public void updateNowYearLunarToDB(String url,String name,String itemLunarBirth){
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {  //응답을 문자열로 받아서 여기다 넣어달란말임(응답을 성공적으로 받았을 떄 이메소드가 자동으로 호출됨
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if(success){
                                Log.d("올해 음력 업데이트","성공");
                            } else{
                                Toast.makeText(getContext(),"음력 업데이트 실패.",Toast.LENGTH_SHORT).show();
                                Log.d("올해 음력 업데이트","실패");
                                return;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){ //에러발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity().getApplicationContext(), "UPDATE ALARMON ERROR", Toast.LENGTH_SHORT).show();
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
                params.put("itemName", name);
                params.put("itemLunarBirth",itemLunarBirth);
                return params;
            }
        };
        //실제 요청 작업을 수행해주는 요청큐 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity().getApplicationContext());
        //요청큐에 요청 객체 생성
        requestQueue.add(request);
    }



    /*private void showLoading() {
        progressBarLy.setVisibility(View.VISIBLE);
        loadingSpinner.setVisibility(View.VISIBLE);
        mainLy.setBackgroundColor(Color.parseColor("#E0E0E0"));
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideLoading() {
        progressBarLy.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.GONE);
        mainLy.setBackgroundColor(Color.TRANSPARENT);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }*/

}