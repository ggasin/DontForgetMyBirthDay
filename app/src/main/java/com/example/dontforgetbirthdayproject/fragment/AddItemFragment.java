package com.example.dontforgetbirthdayproject.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
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
import com.example.dontforgetbirthdayproject.data.RequestCodeData;
import com.example.dontforgetbirthdayproject.request.ItemAddRequest;
import com.example.dontforgetbirthdayproject.R;
import com.example.dontforgetbirthdayproject.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
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

    private int userRequestCode;
    static int solarRequestCode;



    String selectedGroup,gender="남";
    String lunarBirth="--" , solarBirth;
    String requestCodeURL = "http://dfmbd.ivyro.net/loadTotalRequestCode.php";
    boolean isValidBirth = false; //유효한 생년월일인지 파악


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
                LocalDate now = LocalDate.now(); //현재 날짜 가져오기
                solarBirth = addSolarBirthEt.getText().toString();
                if(solarBirth.length()==8){
                    int solarBirthYear = Integer.parseInt(solarBirth.substring(0,4)); //생년 인트형 변환
                    int solarBirthMonth = Integer.parseInt(solarBirth.substring(4,6));
                    int solarBirthDay = Integer.parseInt(solarBirth.substring(6,8));
                    //유효한 생년월일인지 체크
                    //1850년보다 많고 오늘날의 연도보다 생년월일이 적어야하고, 달은 1보다 크고 12보다 작아야한다
                    if(solarBirthYear>1850 && solarBirthYear<=now.getYear() && solarBirthMonth>=1 && solarBirthMonth<=12){
                        //30일이 끝인 달은 생일이 30일보다 작아야 유효하다
                        if(solarBirthMonth==4 && solarBirthMonth==6 && solarBirthMonth==9 && solarBirthMonth==11 ){
                            if(solarBirthDay>=1 && solarBirthDay<=30){
                                ifSolarValidLoadRequestCode();
                            }
                        } else if(solarBirthMonth == 2){
                            //생일이 2월인데 윤년이면 29일까지 허용
                            if( ((solarBirthYear%4==0 && solarBirthYear %100 !=0)||solarBirthYear % 400==0) && (solarBirthDay<=29 && solarBirthDay>=1)){
                                ifSolarValidLoadRequestCode();
                            } else if(solarBirthDay<=28) {
                                ifSolarValidLoadRequestCode();
                            }
                        } else if(solarBirthDay<=31 && solarBirthDay>=1){
                            ifSolarValidLoadRequestCode();
                        }

                    } else {
                        isValidBirth = false;
                    }
                } else {
                    isValidBirth = false;
                }
                Log.d("양력입력 이후",String.valueOf(isValidBirth));
            }
        });

        addLunarChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked && isValidBirth){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ArrayList<String> lunarArr = getLunar(solarBirth.substring(0,4),solarBirth.substring(4,6),solarBirth.substring(6,8));
                                lunarBirth = lunarArr.get(2)+lunarArr.get(1)+lunarArr.get(0);
                            } catch (IOException e) {
                                e.printStackTrace();
                                lunarBirth = "&&";
                                Log.d("스레드 예외 발생","1");
                            }
                        }
                    }).start();
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

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if(!name.equals("") &&!group.equals("") && !solarBirth.equals("") && isValidBirth){ //공백이 없고 생년월일이 유효하면
                                if(success){
                                    LocalDate now = LocalDate.now();
                                    int month = Integer.parseInt(solarBirth.substring(4,6));
                                    int day = Integer.parseInt(solarBirth.substring(6,8));
                                    //추가하는 생일이 이미 지났다면 내년 생일 알림으로 등록
                                    if(now.getMonthValue()>month || (now.getMonthValue()==month && now.getDayOfMonth()>day)){
                                        //알람 추가(양력)
                                        mainActivity.setNotice(now.getYear()+1,month,day-2,day,0,0,solarRequestCode,
                                                name+"님의 생일",solarRequestCode);
                                    } else {
                                        //알람 추가(양력)
                                        mainActivity.setNotice(now.getYear(),month,day-2,day,0,0,solarRequestCode,
                                                name+"님의 생일",solarRequestCode);
                                    }
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
                ItemAddRequest itemAddRequest = new ItemAddRequest(id,group,name,solarBirth,lunarBirth,memo,1,gender,RequestCodeData.getRequestCode(),responseListener);
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
    //음력 받아오기
    public ArrayList<String> getLunar(String year,String month,String day) throws IOException {
        StringBuffer buffer=new StringBuffer();
        String key="%2FFuD9koHUvAqYGTL1DrKzBYNcJcVxuraDgxxPn5TpZr%2B5m6YCmEc1Bf%2BJB1tEUn5MqMMXXxtZ6wr9ngFMyJOoQ%3D%3D";
        ArrayList<String> arr = new ArrayList<>();
        String queryUrl="http://apis.data.go.kr/B090041/openapi/service/LrsrCldInfoService/getLunCalInfo?solYear="+year+"&solMonth="+month+"&solDay="+day+"&ServiceKey="+key;
        try{
            URL url= new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.
            InputStream is= url.openStream(); //url위치로 입력스트림 연결

            XmlPullParserFactory factory= XmlPullParserFactory.newInstance();//xml파싱을 위한
            XmlPullParser xpp= factory.newPullParser();
            xpp.setInput( new InputStreamReader(is, "UTF-8") ); //inputstream 으로부터 xml 입력받기

            String tag;

            xpp.next();
            int eventType= xpp.getEventType();
            while( eventType != XmlPullParser.END_DOCUMENT ){
                switch( eventType ){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        tag= xpp.getName();//테그 이름 얻어오기
                        if(tag.equals("item")) ;// 첫번째 검색결과
                        else if(tag.equals("lunYear")){
                            xpp.next();
                            arr.add(xpp.getText());//title 요소의 TEXT 읽어와서 문자열버퍼에 추가
                        }
                        else if(tag.equals("lunMonth")){
                            buffer.append("음력 달 : ");
                            xpp.next();
                            arr.add(xpp.getText());
                        } else if(tag.equals("lunDay")){
                            buffer.append("음력 일 : ");
                            xpp.next();
                            arr.add(xpp.getText());
                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }

                eventType= xpp.next();
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        return arr;

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


    //양력 받아오기기
    /*
    public ArrayList<String> getSolar(String year, String month, String day){
        StringBuffer buffer=new StringBuffer();
        String key="%2FFuD9koHUvAqYGTL1DrKzBYNcJcVxuraDgxxPn5TpZr%2B5m6YCmEc1Bf%2BJB1tEUn5MqMMXXxtZ6wr9ngFMyJOoQ%3D%3D";
        ArrayList<String> arr = new ArrayList<>();
        String queryUrl="http://apis.data.go.kr/B090041/openapi/service/LrsrCldInfoService/getSolCalInfo?lunYear="+year+"&lunMonth="+month+"&lunDay="+day+"&ServiceKey="+key;
        try{
            URL url= new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.
            InputStream is= url.openStream(); //url위치로 입력스트림 연결
            XmlPullParserFactory factory= XmlPullParserFactory.newInstance();//xml파싱을 위한
            XmlPullParser xpp= factory.newPullParser();
            xpp.setInput( new InputStreamReader(is, "UTF-8") ); //inputstream 으로부터 xml 입력받기

            String tag;

            xpp.next();
            int eventType= xpp.getEventType();
            while( eventType != XmlPullParser.END_DOCUMENT ){
                switch( eventType ){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        tag= xpp.getName();//테그 이름 얻어오기
                        if(tag.equals("item")) ;// 첫번째 검색결과
                        else if(tag.equals("solYear")){
                            xpp.next();
                            arr.add(xpp.getText());//title 요소의 TEXT 읽어와서 문자열버퍼에 추가
                        }
                        else if(tag.equals("solMonth")){
                            xpp.next();
                            arr.add(xpp.getText());
                        } else if(tag.equals("solDay")){
                            xpp.next();
                            arr.add(xpp.getText());
                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }

                eventType= xpp.next();
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        return arr;
    }
    */



}
