package com.example.dontforgetbirthdayproject.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.example.dontforgetbirthdayproject.R;
import com.example.dontforgetbirthdayproject.activity.LoginActivity;
import com.example.dontforgetbirthdayproject.activity.MainActivity;
import com.example.dontforgetbirthdayproject.activity.RegisterActivity;
import com.example.dontforgetbirthdayproject.request.ItemDeleteRequest;
import com.example.dontforgetbirthdayproject.request.MyPageRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;


public class MyPageFragement extends Fragment {
    MainActivity mainActivity;
    private TextView logoutTextBtn,deleteAccountBtn,myName,myId,myEmail;
    private Button goChangePwdBtn,goSettingAlarm,completeSettingAlarm;
    private LinearLayout settingAlarmLy;
    private RadioGroup alarmSettingRadioGroup;
    private RadioButton allAlarmOnRadioBtn,allAlarmOffRadioBtn;
    String settingAllAlarmResult="",settingAlarm=null,whenAlarmStart=null;
    private Spinner alarmStartDaySpinner;
    boolean onAlarmSettingMode = false;
    String AllAlarmOnURL = "http://dfmbd.ivyro.net/AllAlarmOn.php";
    String AllAlarmOffURL = "http://dfmbd.ivyro.net/AllAlarmOff.php";
    String deleteAccountURL = "http://dfmbd.ivyro.net/DeleteAccount.php";

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
        settingAlarmLy = rootView.findViewById(R.id.mp_alarm_setting_ly);
        completeSettingAlarm = rootView.findViewById(R.id.mp_complete_setting_alarm_btn);
        alarmSettingRadioGroup = rootView.findViewById(R.id.mp_alarm_setting_redio_group);
        allAlarmOnRadioBtn = rootView.findViewById(R.id.mp_all_alarm_on_radio_btn);
        allAlarmOffRadioBtn = rootView.findViewById(R.id.mp_all_alarm_off_radio_btn);
        alarmStartDaySpinner = rootView.findViewById(R.id.mp_set_alarm_start_spinner);
        deleteAccountBtn = rootView.findViewById(R.id.mp_delete_account_text_btn);

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
                switch (alarmStartDaySpinner.getSelectedItem().toString()){
                    case "당일":
                        alarmSetting.putString("whenAlarmStart", "0");
                        break;
                    case "1일전부터":
                        alarmSetting.putString("whenAlarmStart", "1");
                        break;
                    case "2일전부터":
                        alarmSetting.putString("whenAlarmStart", "2");
                        break;
                    case "3일전부터":
                        alarmSetting.putString("whenAlarmStart", "3");
                        break;
                    case "4일전부터":
                        alarmSetting.putString("whenAlarmStart","4");
                        Log.d("sw 2","");
                        break;
                    case "5일전부터":
                        alarmSetting.putString("whenAlarmStart", "5");
                        break;
                    case "6일전부터":
                        alarmSetting.putString("whenAlarmStart", "6");
                        break;
                    case "7일전부터":
                        alarmSetting.putString("whenAlarmStart", "7");
                        break;
                }
                switch (settingAllAlarmResult){
                    case "전체 알람 끄기":
                        //모든 알람 끄기
                        mainActivity.allCancelAlarm(AllAlarmOffURL,"전체");
                        alarmSetting.putString("settingAlarm", "전체 알람 끄기");
                        Log.d("sw 1","");
                        break;
                    case "전체 알람 켜기":
                        mainActivity.allAlarmStart( AllAlarmOnURL,"전체");
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

        //로그아웃 버튼 이벤트
        logoutTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //로그아웃하면 알람 울리지 않도록 모든 알람 끄기
                mainActivity.allCancelAlarm(AllAlarmOffURL,"전체");
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
}