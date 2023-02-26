package com.example.dontforgetbirthdayproject.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.dontforgetbirthdayproject.request.ItemUpdateRequest;
import com.example.dontforgetbirthdayproject.R;
import com.example.dontforgetbirthdayproject.activity.MainActivity;
import com.example.dontforgetbirthdayproject.adapter.HomeAdapter;
import com.example.dontforgetbirthdayproject.data.ItemData;
import com.example.dontforgetbirthdayproject.request.ItemDeleteRequest;

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


public class ItemDetailFragment extends Fragment {

    MainActivity mainActivity;
    private ArrayList<ItemData> itemList;
    private HomeAdapter homeAdapter;
    private TextView itemDetailName,itemDetailSolar,itemDetailLunar,itemDetailGroup;
    private EditText itemDetailMemo,itemDetailEditSolar,itemDetailEditName;
    private ImageButton itemDetailCloseBtn,itemDetailDeleteBtn,itemDetailAlterBtn;
    private LinearLayout itemDetailMemoLy;
    private Button itemDetailCompleteBtn,itemDetailCalcelBtn;
    private LinearLayout itemDetailBtnLy;
    private ImageView itemDetailProfile;
    private CheckBox itemDetailLunarChkBox;

    boolean isValidBirth = false;
    String solarBirth,lunarBirth="";
    private int itemRequestCode,requestCode;
    private static int solarRequestCode;
    private String itemRequestCodeURL = "http://dfmbd.ivyro.net/LoadItemRequestCode.php";
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
    public ItemDetailFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_item_detail, container, false);
        itemDetailProfile = rootView.findViewById(R.id.item_detail_profile_iv);
        itemDetailName = rootView.findViewById(R.id.item_detail_name_tv);
        itemDetailSolar = rootView.findViewById(R.id.item_detail_solar_tv);
        itemDetailLunar = rootView.findViewById(R.id.item_detail_lunar_tv);
        itemDetailMemo = rootView.findViewById(R.id.item_detail_memo_et);
        itemDetailGroup = rootView.findViewById(R.id.item_detail_group_tv);
        itemDetailCloseBtn = rootView.findViewById(R.id.item_detail_close_btn);
        itemDetailMemoLy = rootView.findViewById(R.id.item_detail_memo_ly);
        itemDetailAlterBtn = rootView.findViewById(R.id.item_detail_alter_btn);
        itemDetailDeleteBtn = rootView.findViewById(R.id.item_detail_delete_btn);
        itemDetailCompleteBtn = rootView.findViewById(R.id.item_detail_complete_btn);
        itemDetailCalcelBtn = rootView.findViewById(R.id.item_detail_cancel_btn);
        itemDetailEditName = rootView.findViewById(R.id.item_detail_edit_name);
        itemDetailEditSolar = rootView.findViewById(R.id.item_detail_edit_solar);
        itemDetailLunarChkBox = rootView.findViewById(R.id.item_detail_lunar_chk_box);


        itemDetailBtnLy = rootView.findViewById(R.id.item_detail_btn_ly);
        itemDetailMemo = rootView.findViewById(R.id.item_detail_memo_et);

        //사용자 정보로 세팅
        itemDetailProfile.setImageResource(mainActivity.profile_id);
        itemDetailName.setText(mainActivity.itemName);
        itemDetailGroup.setText(mainActivity.itemGroup);
        itemDetailSolar.setText(mainActivity.itemSolarBirth);
        itemDetailLunar.setText(mainActivity.itemlunarBirth);
        requestCode = mainActivity.itemRequestCode;

        lunarBirth = mainActivity.itemlunarBirth;
        Log.d("itemRequestCode",String.valueOf(mainActivity.itemRequestCode));

        itemDetailMemoLy.setBackgroundResource(R.drawable.memo_cant_edit_border);//배경색 설정
        itemDetailMemo.setEnabled(false);

        itemList = new ArrayList<>();
        homeAdapter = new HomeAdapter(getActivity().getApplicationContext(),itemList);

        //양력 editText 입력 이벤트
        itemDetailEditSolar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
                itemDetailLunarChkBox.setChecked(false);
            }
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void afterTextChanged(Editable s) {
                solarValidCheck();
            }
        });

        //음력 체크박스
        itemDetailLunarChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked && isValidBirth){
                    Thread thread = new Thread(new Runnable() {
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
                    });
                    thread.start(); //음력 계산 스레드 시작
                    try{
                        thread.join();  //음력 계산이 끝나면 아래 코드를 시행하도록 하는 join 메소드
                    }catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    itemDetailLunar.setText(lunarBirth);

                } else if(isChecked && !isValidBirth) {
                    itemDetailLunarChkBox.setChecked(false);
                    Toast.makeText(getContext(), "양력을 제대로 입력했는지 확인해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    lunarBirth = "--";
                    itemDetailLunar.setText(lunarBirth);

                }
            }
        });

        //삭제 버튼
        itemDetailDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = itemDetailName.getText().toString();
                //안전을 위해서 다이얼로그 추가
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("정말 삭제하시겠습니까?")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                homeAdapter.remove(mainActivity.itemClickPosition);
                                Response.Listener<String> responseListener = new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(response);
                                            boolean success = jsonObject.getBoolean("success");
                                            if (success) {
                                                Toast.makeText(getContext(), "삭제 완료", Toast.LENGTH_SHORT).show();
                                                mainActivity.cancelAlarm(requestCode); //삭제된 아이템의 알림을 삭제
                                                editModeOff();
                                                mainActivity.onFragmentChange(0);

                                            } else {
                                                Toast.makeText(getContext(), "삭제 실패.", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (JSONException e) {
                                            Toast.makeText(getContext(),"삭제 오류",Toast.LENGTH_SHORT).show();
                                            StringWriter sw = new StringWriter();
                                            e.printStackTrace(new PrintWriter(sw));
                                            String exceptionAsStrting = sw.toString();
                                            Log.e("삭제 오류", exceptionAsStrting);
                                            e.printStackTrace();
                                        }
                                    }
                                };

                                ItemDeleteRequest itemDeleteRequest = new ItemDeleteRequest(mainActivity.userId,name,responseListener);
                                RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
                                queue.add(itemDeleteRequest);

                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d("Item delete event","cancel");

                            }
                        }).show();

            }
        });
        //수정 버튼
        itemDetailAlterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editModeOn();
            }
        });
        //완료 버튼
        itemDetailCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

               LocalDate now = LocalDate.now(); //현재 날짜 가져오기
               String name = itemDetailEditName.getText().toString();
               String group = itemDetailGroup.getText().toString();
               String beforeName = itemDetailName.getText().toString();
               String solar = itemDetailEditSolar.getText().toString();
               String lunar = itemDetailLunar.getText().toString();
               String memo = itemDetailMemo.getText().toString();

               solarRequestCode = RequestCodeData.getRequestCode();
               Log.d("Load reCode in soValid",String.valueOf(solarRequestCode));
               if(isValidBirth && !itemDetailEditName.equals("")){
                   Response.Listener<String> responseListener = new Response.Listener<String>() {
                       @Override
                       public void onResponse(String response) {
                           try {
                               JSONObject jsonObject = new JSONObject(response);
                               boolean success = jsonObject.getBoolean("success");
                               if (success) {
                                   LocalDate now = LocalDate.now();
                                   int month = Integer.parseInt(solarBirth.substring(4,6));
                                   int day = Integer.parseInt(solarBirth.substring(6,8));

                                   if(now.getMonthValue()>month || (now.getMonthValue()==month && now.getDayOfMonth()>day)){
                                       //알람 추가(양력)
                                       mainActivity.setNotice(now.getYear()+1,month,day-2,day,0,0,solarRequestCode,
                                               name+"님의 생일",solarRequestCode);
                                   } else {
                                       //알람 추가(양력)
                                       mainActivity.setNotice(now.getYear(),month,day-2,day,0,0,solarRequestCode,
                                               name+"님의 생일",solarRequestCode);
                                   }
                                   Toast.makeText(getContext(), "수정 완료", Toast.LENGTH_SHORT).show();
                                   editModeOff();

                                   if(mainActivity.ifTrueCalenderElseHome){ //캘린더에서 눌러서 상세화면으로 온거면 캘린더로 돌아가기
                                       mainActivity.onFragmentChange(3);
                                   } else {
                                       mainActivity.onFragmentChange(0);
                                   }


                               } else {
                                   Toast.makeText(getContext(), "이름이 중복됩니다. 중복되지 않는 이름을 기입해주세요.", Toast.LENGTH_SHORT).show();
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
                   ItemUpdateRequest itemUpdateRequest = new ItemUpdateRequest(mainActivity.userId, group,name,
                           beforeName,solar,lunar,memo,responseListener);
                   RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
                   queue.add(itemUpdateRequest);
               } else if(!isValidBirth){
                   Toast.makeText(getActivity().getApplicationContext(),"유효하지 않은 생년월일 입니다.",Toast.LENGTH_SHORT).show();
               } else {
                   Toast.makeText(getActivity().getApplicationContext(),"입력하지 않은 란이 있는지 확인해 주세요.",Toast.LENGTH_SHORT).show();
               }
           }
        });
        //취소 버튼
        itemDetailCalcelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               editModeOff();
               itemDetailMemo.setText(mainActivity.itemMemo); //취소일땐 원래 메모 상태로 돌려놔야함
            }
        });
        //닫기 버튼
        itemDetailCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mainActivity.ifTrueCalenderElseHome){ //캘린더에서 눌러서 상세화면으로 온거면 캘린더로 돌아가기
                    mainActivity.onFragmentChange(3);
                } else {
                    mainActivity.onFragmentChange(0);
                }
            }
        });


        return rootView;
    }
    @Override
    public void onStart(){
        super.onStart();
        //popBackStack으로 B에서 이전 프래그먼트 A로 돌아오면서
        // 현재 UI Thread가 A로 바뀌지 않은 상태에서 setText를 해서 안먹음.
        //이전 프래그먼트로 복귀할 때는 onCreate부터 플로우를 타는 것이 아니기 때문에 해당 메소드를 오버라이드하여 setText()를 호출
        itemDetailMemo.setText(mainActivity.itemMemo);
    }
    public void editModeOn(){
        //TextView 없애기
        itemDetailName.setVisibility(View.GONE);
        itemDetailSolar.setVisibility(View.GONE);
        //EditText와 완료,취소 버튼 보이기
        itemDetailEditName.setVisibility(View.VISIBLE);
        itemDetailLunarChkBox.setVisibility(View.VISIBLE);
        itemDetailEditName.setText(itemDetailName.getText().toString());
        itemDetailEditSolar.setVisibility(View.VISIBLE);
        itemDetailEditSolar.setText(itemDetailSolar.getText().toString());
        itemDetailBtnLy.setVisibility(View.VISIBLE);
        itemDetailMemoLy.setBackgroundResource(R.drawable.memo_can_edit_border);
        itemDetailMemo.setEnabled(true);
    }
    public void editModeOff(){
        itemDetailName.setVisibility(View.VISIBLE);
        itemDetailSolar.setVisibility(View.VISIBLE);
        itemDetailLunar.setVisibility(View.VISIBLE);
        itemDetailEditName.setVisibility(View.GONE);
        itemDetailEditSolar.setVisibility(View.GONE);
        itemDetailBtnLy.setVisibility(View.GONE);
        itemDetailLunarChkBox.setVisibility(View.GONE);
        itemDetailLunarChkBox.setChecked(false); //체크 안풀면 나중에 다시 상세정보 클릭하면 체크가 된 상태라서 음력계산을 맘대로 해버림.
        itemDetailMemoLy.setBackgroundResource(R.drawable.memo_cant_edit_border);
        itemDetailMemo.setEnabled(false);
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

    public void ifSolarValidLoadRequestCode(){
        isValidBirth = true;
        solarBirth = itemDetailEditSolar.getText().toString();
        loadItemRequestCode(itemRequestCodeURL);
    }
    public void loadItemRequestCode(String url){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if(success){
                                itemRequestCode = jsonObject.getInt("itemRequestCode");
                                RequestCodeData.setRequestCode(itemRequestCode);
                                Log.d("Load requestCode loadRe",String.valueOf(itemRequestCode));
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
                params.put("itemId", mainActivity.userId);
                params.put("itemName", mainActivity.itemName);
                Log.d("userId in Additem",mainActivity.userId);
                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity().getApplicationContext());
        //요청큐에 요청 객체 생성
        requestQueue.add(stringRequest);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void solarValidCheck(){
        LocalDate now = LocalDate.now(); //현재 날짜 가져오기
        solarBirth = itemDetailEditSolar.getText().toString();
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