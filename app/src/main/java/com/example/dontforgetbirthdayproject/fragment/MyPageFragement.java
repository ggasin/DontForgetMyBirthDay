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

    //onAttach ??? fragment??? activity??? ????????? ??????
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


        //??????????????? ????????? ??????
        ArrayAdapter alarmStartDayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.setWhenAlarmStartArray, android.R.layout.simple_spinner_dropdown_item);
        alarmStartDayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        alarmStartDaySpinner.setAdapter(alarmStartDayAdapter); //???????????? ??????????????????.

        //?????????,????????? ?????? ?????????
        if(settingAlarm == null && whenAlarmStart == null){
            alarmStartDaySpinner.setSelection(2);
            settingAllAlarmResult = "?????? ?????? ??????";
        } else {
            switch (settingAlarm){
                case "?????? ?????? ??????":  allAlarmOffRadioBtn.setChecked(true);
                    settingAllAlarmResult = "?????? ?????? ??????";
                    break;
                case "?????? ?????? ??????":  allAlarmOnRadioBtn.setChecked(true);
                    settingAllAlarmResult = "?????? ?????? ??????";
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




        //???????????? ???????????? ??????
        goChangePwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.onFragmentChange(4);
            }
        });
        //?????? ?????? ??????
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
        //?????? ?????? ?????? ??????
        completeSettingAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences alarmSettingPreferences = getActivity().getSharedPreferences("alarmSetting",Context.MODE_PRIVATE);
                SharedPreferences.Editor alarmSetting = alarmSettingPreferences.edit();
                settingAlarmLy.setVisibility(View.GONE);
                onAlarmSettingMode = false;
                switch (alarmStartDaySpinner.getSelectedItem().toString()){
                    case "??????":
                        alarmSetting.putString("whenAlarmStart", "0");
                        break;
                    case "1????????????":
                        alarmSetting.putString("whenAlarmStart", "1");
                        break;
                    case "2????????????":
                        alarmSetting.putString("whenAlarmStart", "2");
                        break;
                    case "3????????????":
                        alarmSetting.putString("whenAlarmStart", "3");
                        break;
                    case "4????????????":
                        alarmSetting.putString("whenAlarmStart","4");
                        Log.d("sw 2","");
                        break;
                    case "5????????????":
                        alarmSetting.putString("whenAlarmStart", "5");
                        break;
                    case "6????????????":
                        alarmSetting.putString("whenAlarmStart", "6");
                        break;
                    case "7????????????":
                        alarmSetting.putString("whenAlarmStart", "7");
                        break;
                }
                switch (settingAllAlarmResult){
                    case "?????? ?????? ??????":
                        //?????? ?????? ??????
                        mainActivity.allCancelAlarm(AllAlarmOffURL,"??????");
                        alarmSetting.putString("settingAlarm", "?????? ?????? ??????");
                        Log.d("sw 1","");
                        break;
                    case "?????? ?????? ??????":
                        mainActivity.allAlarmStart( AllAlarmOnURL,"??????");
                        alarmSetting.putString("settingAlarm", "?????? ?????? ??????");
                        Log.d("sw 1","");
                        break;

                }
                alarmSetting.commit();
                Toast.makeText(getActivity().getApplicationContext(),"?????? ??????",Toast.LENGTH_SHORT).show();

            }
        });

        //?????? ????????? ?????? ?????? ?????????
        alarmSettingRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = group.findViewById(checkedId);
                settingAllAlarmResult = rb.getText().toString();
                Log.d("set radio",settingAllAlarmResult);

            }
        });

        //?????? ????????????
        loadMyInfo();

        //???????????? ?????? ?????????
        logoutTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //?????????????????? ?????? ????????? ????????? ?????? ?????? ??????
                mainActivity.allCancelAlarm(AllAlarmOffURL,"??????");
                Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                //?????? ????????? ?????? ??????
                SharedPreferences auto = mainActivity.getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = auto.edit();
                editor.clear();
                editor.commit();
                //?????? ?????? ??????
                SharedPreferences alarmsetting = mainActivity.getSharedPreferences("alarmSetting", Activity.MODE_PRIVATE);
                SharedPreferences.Editor alarmEditor = alarmsetting.edit();
                alarmEditor.clear();
                alarmEditor.commit();

                Toast.makeText(getActivity().getApplicationContext(), "????????????", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        });
        //???????????? ?????? ?????????
        deleteAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String warningInfo = "?????? ?????????????????????????\n????????? ???????????? ????????? ??????????????????.";
                //string ??? ??????
                SpannableString spannableString = new SpannableString(warningInfo);
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
                spannableString.setSpan(colorSpan, 0, warningInfo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                //????????? ????????? ??????????????? ??????
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(spannableString)
                        .setPositiveButton("???", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteAccount(deleteAccountURL);
                            }
                        })
                        .setNegativeButton("?????????", new DialogInterface.OnClickListener() {
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
                            Toast.makeText(getContext(),"?????? ???????????? ??????",Toast.LENGTH_SHORT).show();
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
                new Response.Listener<String>() {  //????????? ???????????? ????????? ????????? ??????????????????(????????? ??????????????? ????????? ??? ??????????????? ???????????? ?????????
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if(success){
                                Toast.makeText(getContext(),"?????? ??????",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            } else{
                                Toast.makeText(getContext(),"?????? ??????.",Toast.LENGTH_SHORT).show();
                                return;
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
                        Log.d("group load db : ","error");
                    }
                }
        ){
            //?????? POST ???????????? ????????? ?????? ??????????????? ????????? getParams ??????????????? ???????????? HashMap ????????? ???????????????.
            //????????? ?????? ?????? ????????? ?????? ?????? ???????????? ?????? ????????? ?????????.
            //POST???????????? ???????????? ????????? ???????????????.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userId", mainActivity.userId);
                return params;
            }
        };
        //?????? ?????? ????????? ??????????????? ????????? ?????? ??????
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity().getApplicationContext());
        //???????????? ?????? ?????? ??????
        requestQueue.add(request);
    }
}