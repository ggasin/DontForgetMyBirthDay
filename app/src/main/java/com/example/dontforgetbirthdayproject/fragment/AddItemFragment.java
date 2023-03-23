package com.example.dontforgetbirthdayproject.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dontforgetbirthdayproject.apiClass.GetLunarSolarApiManager;
import com.example.dontforgetbirthdayproject.apiClass.IsSolarValidApiManager;
import com.example.dontforgetbirthdayproject.apiClass.LoadingApiManager;
import com.example.dontforgetbirthdayproject.apiClass.SetAlarmApiManager;
import com.example.dontforgetbirthdayproject.data.RequestCodeData;
import com.example.dontforgetbirthdayproject.request.ItemAddRequest;
import com.example.dontforgetbirthdayproject.R;
import com.example.dontforgetbirthdayproject.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


//아이템 추가 화면 fragment
public class AddItemFragment extends Fragment {
    MainActivity mainActivity;

    private Button addCompleteBtn, addCloseBtn;
    private EditText addNameEt, addSolarBirthEt, addMemoEt;
    private TextView addGroupT, requestCodeT;
    private CheckBox addLunarChk;
    private RadioGroup addGenderGroup;
    private ProgressBar loadingSpinner;
    private LinearLayout progressBarLy;


    private int userRequestCode;
    static int solarRequestCode;

    String selectedGroup,gender="남";
    String lunarBirth="--" , solarBirth;
    String requestCodeURL = "http://dfmbd.ivyro.net/loadTotalRequestCode.php";
    boolean isValidBirth = false; //유효한 생년월일인지 파악
    ArrayList<String> lunarArr,solarArr;
    GetLunarSolarApiManager getLunarSolarApiManager = new GetLunarSolarApiManager();
    SetAlarmApiManager setAlarmApiManager = new SetAlarmApiManager();
    IsSolarValidApiManager isSolarValidApiManager = new IsSolarValidApiManager();
    LoadingApiManager loadingApiManager = new LoadingApiManager();

    //onAttach 는 fragment가 activity에 올라온 순간
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
        selectedGroup = mainActivity.selectedGroup;
        Log.d("selectedGroup on Add",selectedGroup);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_add_item, container, false);
        addCompleteBtn = rootView.findViewById(R.id.add_complete_btn);
        addCloseBtn = rootView.findViewById(R.id.add_close_btn);
        addNameEt = rootView.findViewById(R.id.add_name_et);
        addGroupT = rootView.findViewById(R.id.add_group_t);
        addSolarBirthEt = rootView.findViewById(R.id.add_solar_birth_et);
        addMemoEt = rootView.findViewById(R.id.add_memo_et);
        addLunarChk = rootView.findViewById(R.id.add_lunar_check_box);
        addGenderGroup = rootView.findViewById(R.id.add_gender_radio_group);
        requestCodeT = rootView.findViewById(R.id.requestCode_text);

        loadingSpinner =rootView.findViewById(R.id.add_progress_bar);
        progressBarLy = rootView.findViewById(R.id.add_progress_bar_ly);



        //선택된 그룹 이름으로 group text 초기화
        addGroupT.setText(selectedGroup);
        Log.d("selectedGroup on Add1",selectedGroup);

        //라디오그룹 체크 이벤트
        addGenderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i==R.id.add_gender_man){
                    gender = "남";
                } else {
                    gender = "여";
                }
            }
        });
        //양력 edittext 값 변화 이벤트
        addSolarBirthEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                addLunarChk.setChecked(false);
            }
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void afterTextChanged(Editable editable) {
                solarBirth = addSolarBirthEt.getText().toString();
                if(solarBirth.length()==8){
                    int solarBirthYear = Integer.parseInt(solarBirth.substring(0,4)); //생년 인트형 변환
                    int solarBirthMonth = Integer.parseInt(solarBirth.substring(4,6));
                    int solarBirthDay = Integer.parseInt(solarBirth.substring(6,8));
                    isValidBirth = isSolarValidApiManager.solarValidCheck(solarBirth,solarBirthYear,solarBirthMonth,solarBirthDay);
                    if(isValidBirth){
                        loadTotalRequestCode(requestCodeURL);
                    }
                } else {
                    isValidBirth = false;
                }
            }
        });

        //핸들러 정의
        Handler lunarHandler = new Handler(Looper.getMainLooper());
        Handler solarHandler = new Handler(Looper.getMainLooper());
        //음력 계산 이벤트
        addLunarChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked && isValidBirth){
                    loadingApiManager.showLoading(progressBarLy,loadingSpinner,getActivity());
                    LocalDate now = LocalDate.now();
                    //음력 계산 스레드
                    Thread lunarThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getLunarSolarApiManager.getLunar(solarBirth.substring(0,4), solarBirth.substring(4,6), solarBirth.substring(6,8), new GetLunarSolarApiManager.LunarCallback() {
                                @Override
                                public void onSuccess(ArrayList<String> result) {
                                    // 결과값 처리
                                    lunarArr = result; // 일 , 윤달여부, 월 , 년 순으로 저장되어있음
                                    Log.d("addLunarArr",lunarArr.get(1)+"+"+lunarArr.get(1).equals("윤"));
                                    if(lunarArr.get(1).equals("윤")){
                                        lunarBirth = "윤달";
                                        Log.d("addLunarYun",lunarBirth);
                                        mainActivity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                loadingApiManager.hideLoading(progressBarLy, loadingSpinner, getActivity());
                                            }
                                        });
                                    } else{
                                        //양력 계산 스레드
                                        Thread solarThread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                solarArr = getLunarSolarApiManager.getSolar(String.valueOf(now.getYear()),lunarArr.get(2),lunarArr.get(0));
                                                // 1월 초반 생일자가 음력체크를 할때 이번년도에 해당하는 음력이 없으면 양력으로 변환할때 다음년도로 넘어가서 계산하게 됨.
                                                if (Integer.parseInt(solarArr.get(2)) > now.getYear()) {
                                                    solarArr = getLunarSolarApiManager.getSolar(String.valueOf(now.getYear() - 1), lunarArr.get(2), lunarArr.get(0));
                                                }
                                                solarHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        lunarBirth = solarArr.get(2)+solarArr.get(1)+solarArr.get(0);
                                                        Log.d("addLunar",solarArr+"");
                                                        loadingApiManager.hideLoading(progressBarLy,loadingSpinner,getActivity());
                                                    }
                                                });
                                            }
                                        });
                                        solarThread.start(); // 양력 계산 스레드 시작
                                    }
                                }
                                @Override
                                public void onFailure(Exception e) {
                                    // 에러 처리
                                    Log.e("GetLunarSolarApiManager", "음력 조회 실패: " + e.getMessage());
                                }
                            });
                        }
                    });
                    lunarThread.start(); // 음력 계산 스레드 시작

                } else if(isChecked && !isValidBirth) {
                    addLunarChk.setChecked(false);
                    Toast.makeText(getContext(), "양력을 제대로 입력했는지 확인해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    lunarBirth = "--";
                }
            }
        });

        //완료 버튼 이벤트
        addCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                String id = mainActivity.userId;
                String name = addNameEt.getText().toString();
                String group = addGroupT.getText().toString();
                String memo = addMemoEt.getText().toString() ;

                //db로부터 가져온 requestCode를 저장
                solarRequestCode = RequestCodeData.getRequestCode();
                Log.d("addItemIsValidBirth",isValidBirth+"");
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");

                            if(!name.equals("") &&!group.equals("") && !solarBirth.equals("") && isValidBirth){ //공백이 없고 생년월일이 유효하면
                                if(success){
                                    int month = Integer.parseInt(solarBirth.substring(4,6));
                                    int day = Integer.parseInt(solarBirth.substring(6,8));
                                    //음력체크를 하지 않았어도 나중에 사용자가 아이템 수정시 음력을 체크하면 그때 쓸 requestCode가 없기때문에 미리 자리를 마련해두기 위해 solarRequestCode에 1을 더한 값을 공란으로 두기 위함.
                                    loadTotalRequestCode(requestCodeURL);
                                    //알림 추가 메소드
                                    setAlarmApiManager.whenAddOrUpdateItemSetNotice(getActivity().getApplicationContext(),mainActivity.alarmManager,addLunarChk.isChecked(),
                                            month,day,mainActivity.getSharedWhenStartAlarm(),lunarBirth,name,solarRequestCode);

                                    Toast.makeText(getContext(),"추가 완료",Toast.LENGTH_SHORT).show();
                                    addNameEt.setText("");
                                    addSolarBirthEt.setText("");
                                    addMemoEt.setText("");
                                    mainActivity.onFragmentChange(0);
                                } else{
                                    Toast.makeText(getContext(),"이름이 중복되지 않도록 기입해주세요.",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else {
                                if(!isValidBirth){
                                    Toast.makeText(getContext(),"유효한 생년월일인지 확인해주세요.",Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(),"모든 항목에 입력값을 넣었는지 확인해주세요.",Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(),"오류",Toast.LENGTH_SHORT).show();
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            String exceptionAsStrting = sw.toString();
                            Log.e("오류", exceptionAsStrting);
                            e.printStackTrace();
                        }
                    }
                };
                ItemAddRequest itemAddRequest = new ItemAddRequest(id,group,name,solarBirth,lunarBirth,memo,1,gender,RequestCodeData.getRequestCode(),isValidBirth,responseListener);
                RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
                queue.add(itemAddRequest);

            }
        });
        //닫기 버튼 이벤트
        addCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNameEt.setText("");
                addSolarBirthEt.setText("");
                addMemoEt.setText("");
                mainActivity.onFragmentChange(0);

            }
        });
        return rootView;
    }


    //양력이 유효하면 db로부터 requestCode 가져옴
    public void ifSolarValidLoadRequestCode(){
        isValidBirth = true;
        loadTotalRequestCode(requestCodeURL);
    }

    public void loadTotalRequestCode(String url){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if(success){
                                userRequestCode = jsonObject.getInt("userRequestCode");
                                RequestCodeData.setRequestCode(userRequestCode);
                                Log.d("Load requestCode loadRe",String.valueOf(userRequestCode));
                            } else {
                                Log.d("Load requestCode1",String.valueOf(String.valueOf(success)));
                            }
                        } catch (JSONException e) {
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            String exceptionAsStrting = sw.toString();
                            Log.e("Load request 오류", exceptionAsStrting);
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

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
                Log.d("userId in Additem",mainActivity.userId);
                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity().getApplicationContext());
        //요청큐에 요청 객체 생성
        requestQueue.add(stringRequest);


    }
    class MyRunnable implements Runnable {
        private Thread otherThread;

        public MyRunnable(Thread otherThread) {
            this.otherThread = otherThread;
        }

        @Override
        public void run() {
            // 다른 스레드 실행
            otherThread.start();
            // 이 스레드 실행
            // ...
        }
    }
    //알림 설정
    /*
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void whenAddItemSetNotice(int month,int day,String name){
        LocalDate now = LocalDate.now();
        lunarRequestCode = solarRequestCode+1;
        //음력체크를 하지 않았어도 나중에 사용자가 아이템 수정시 음력을 체크하면 그때 쓸 requestCode가 없기때문에 미리 자리를 마련해두기 위해 solarRequestCode에 1을 더한 값을 공란으로 두기 위함.
        loadTotalRequestCode(requestCodeURL);
        //음력 체크 되어있고 윤달이 아니라면 음력도 알림 설정
        if(addLunarChk.isChecked() && !lunarBirth.equals("윤달")){
            int lunarMonth = Integer.parseInt(lunarBirth.substring(4,6));
            int lunarDay = Integer.parseInt(lunarBirth.substring(6,8));
            //양력
            if(now.getMonthValue()>month || (now.getMonthValue()==month && now.getDayOfMonth()>day)){
                mainActivity.setNotice(now.getYear()+1,month,day-mainActivity.getSharedWhenStartAlarm(),day,solarRequestCode,
                        name+"님의 생일",solarRequestCode);
            } else {
                mainActivity.setNotice(now.getYear(),month,day-mainActivity.getSharedWhenStartAlarm(),day,solarRequestCode,
                        name+"님의 생일",solarRequestCode);
            }
            //음력
            if(now.getMonthValue()>lunarMonth || (now.getMonthValue()==lunarMonth && now.getDayOfMonth()>lunarDay )){
                mainActivity.setNotice(now.getYear()+1,lunarMonth,lunarDay-mainActivity.getSharedWhenStartAlarm(),lunarDay,lunarRequestCode,
                        name+"님의 음력 생일",lunarRequestCode);
            } else {
                mainActivity.setNotice(now.getYear(),lunarMonth,lunarDay-mainActivity.getSharedWhenStartAlarm(),lunarDay,lunarRequestCode,
                        name+"님의 음력 생일",lunarRequestCode);
            }
        } else {
            if(now.getMonthValue()>month || (now.getMonthValue()==month && now.getDayOfMonth()>day)){
                //알람 추가(양력)
                mainActivity.setNotice(now.getYear()+1,month,day-mainActivity.getSharedWhenStartAlarm(),day,solarRequestCode,
                        name+"님의 생일",solarRequestCode);
            } else {
                //알람 추가(양력)
                mainActivity.setNotice(now.getYear(),month,day-mainActivity.getSharedWhenStartAlarm(),day,solarRequestCode,
                        name+"님의 생일",solarRequestCode);
            }
        }
    }

     */





}
