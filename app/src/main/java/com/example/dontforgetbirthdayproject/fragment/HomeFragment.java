package com.example.dontforgetbirthdayproject.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dontforgetbirthdayproject.GetGroupPositionListener;
import com.example.dontforgetbirthdayproject.OnGroupLongClickListener;
import com.example.dontforgetbirthdayproject.adapter.GroupAdapter;
import com.example.dontforgetbirthdayproject.request.GroupAddRequest;
import com.example.dontforgetbirthdayproject.data.GroupData;
import com.example.dontforgetbirthdayproject.adapter.HomeAdapter;
import com.example.dontforgetbirthdayproject.data.ItemData;
import com.example.dontforgetbirthdayproject.OnItemClickListener;
import com.example.dontforgetbirthdayproject.R;
import com.example.dontforgetbirthdayproject.activity.MainActivity;
import com.example.dontforgetbirthdayproject.request.GroupDeleteRequest;
import com.example.dontforgetbirthdayproject.request.ItemDeleteRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class HomeFragment extends Fragment  {
    MainActivity mainActivity;
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
    private ArrayList<ItemData> itemList;
    private ArrayList<GroupData> groupList;
    private HomeAdapter homeAdapter;
    private GroupAdapter groupAdapter;
    private RecyclerView itemRecyclerView,groupRecyclerView;
    private LinearLayoutManager ItemLinearLayoutManager,GroupLinearLayoutManager;
    private ImageButton itemAddBtn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_home, container, false);
        String loadItemURL = "http://dfmbd.ivyro.net/LoadItemDB.php";
        String loadGroupURL = "http://dfmbd.ivyro.net/LoadGroupDB.php";
        ItemLinearLayoutManager = new LinearLayoutManager(getActivity());
        itemRecyclerView = (RecyclerView)rootView.findViewById(R.id.home_birth_rv);
        itemRecyclerView.setHasFixedSize(true);
        itemRecyclerView.scrollToPosition(0);
        itemRecyclerView.setItemAnimator(new DefaultItemAnimator());
        itemRecyclerView.setLayoutManager(ItemLinearLayoutManager);

        GroupLinearLayoutManager = new LinearLayoutManager(getActivity(),RecyclerView.HORIZONTAL,false);
        groupRecyclerView = (RecyclerView)rootView.findViewById(R.id.group_rv);
        groupRecyclerView.setHasFixedSize(true);
        groupRecyclerView.scrollToPosition(0);
        groupRecyclerView.setItemAnimator(new DefaultItemAnimator());
        groupRecyclerView.setLayoutManager(GroupLinearLayoutManager);

        itemList = new ArrayList<>();
        groupList = new ArrayList<>();


        itemAddBtn = rootView.findViewById(R.id.item_add_btn);
        homeAdapter = new HomeAdapter(getActivity().getApplicationContext(),itemList);
        groupAdapter = new GroupAdapter(getActivity().getApplicationContext(),groupList,this);


        //db로부터 데이터를 가져와 recyclerView에 바인딩
        loadGroupFromDB(loadGroupURL);

        Log.d("home first Login",String.valueOf(mainActivity.firstLogin));
        //로그인을 처음 한 상태라면 로그아웃때 다 취소한 알람을 다시 설정해야함.
        if(mainActivity.firstLogin){
            allAlarmStart(loadItemURL,"전체");
            mainActivity.firstLogin=false;
        }



        //아이템 클릭 이벤트
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
                mainActivity.ifTrueCalenderElseHome = false;
                mainActivity.onFragmentChange(2);
                Log.d("아이템 클릭","클릭");
            }
        });


        groupAdapter.setOnGroupClicklistener(new GetGroupPositionListener() {
            @Override
            public void getGroupPosition(int position) {
                loadDB(loadItemURL,groupAdapter.getItem(position).getGroup());
                mainActivity.selectedGroup = groupAdapter.getItem(position).getGroup().toString();
                Log.d("선택된 그룹",groupAdapter.getItem(position).getGroup().toString());
            }
        });







        //그룹 꾹 누르면 삭제 다이얼로그 나옴.
        groupAdapter.setOnGroupLongCLickListener(new OnGroupLongClickListener() {
            @Override
            public void onGroupLongClick(GroupAdapter.CustomViewHolder holder, View view, int position) {
                Log.d("group LongCLick",groupAdapter.getItem(position).getGroup());
                String group = groupAdapter.getItem(position).getGroup();
                if(groupAdapter.getItem(position).getGroup().equals("전체")){
                    Toast.makeText(getContext(),"전체 그룹은 삭제할 수 없습니다.",Toast.LENGTH_SHORT).show();
                } else {
                    //안전을 위해서 다이얼로그 추가
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("정말 삭제하시겠습니까?\n그룹 내 데이터는 모두 삭제됩니다.")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mainActivity.allCancelAlarm("http://dfmbd.ivyro.net/LoadItemDB.php",group);
                                    groupAdapter.remove(position);
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Response.Listener<String> responseListener = new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    try {
                                                        JSONObject jsonObject = new JSONObject(response);
                                                        boolean success = jsonObject.getBoolean("success");
                                                        if (success) {
                                                            GroupLinearLayoutManager.scrollToPositionWithOffset(0,0); //그룹 리사이클러뷰 스크롤을 최상단으로 이동
                                                            Toast.makeText(getContext(), "삭제 완료", Toast.LENGTH_SHORT).show();
                                                            //첫번째 그룹 클릭
                                                            Handler handler = new Handler();
                                                            handler.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    GroupLinearLayoutManager.scrollToPositionWithOffset(0,0); //그룹 리사이클러뷰 스크롤을 최상단으로 이동
                                                                    //화면에 보이는 그룹 리사이클러뷰의 첫번째 클릭
                                                                    groupRecyclerView.findViewHolderForLayoutPosition(GroupLinearLayoutManager.findFirstCompletelyVisibleItemPosition()).itemView.performClick();

                                                                }
                                                            },20);

                                                        } else {
                                                            Toast.makeText(getContext(), "삭제 실패.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } catch (JSONException e) {
                                                        Toast.makeText(getContext(), "삭제 오류", Toast.LENGTH_SHORT).show();
                                                        StringWriter sw = new StringWriter();
                                                        e.printStackTrace(new PrintWriter(sw));
                                                        String exceptionAsStrting = sw.toString();
                                                        Log.e("삭제 오류", exceptionAsStrting);
                                                        e.printStackTrace();
                                                    }
                                                }
                                            };
                                            Log.d("group LongCLick1", String.valueOf(position));
                                            GroupDeleteRequest groupDeleteRequest = new GroupDeleteRequest(mainActivity.userId, group, responseListener);
                                            RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
                                            queue.add(groupDeleteRequest);
                                        }
                                    },700);
                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.d("Item delete event", "cancel");
                                }
                            }).show();
                }
            }
        });


        //추가 아이콘 클릭 이벤트
        itemAddBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                //팝업 메뉴 생성
                PopupMenu popup= new PopupMenu(getActivity().getApplicationContext(), view);//v는 클릭된 뷰를 의미
                popup.getMenuInflater().inflate(R.menu.add_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.add_group_menu:
                                final EditText groupEt = new EditText(getActivity());
                                groupEt.setTextColor(Color.BLACK);
                                FrameLayout container = new FrameLayout(getActivity());
                                FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                params.leftMargin = 60;
                                params.width = 1100;
                                groupEt.setLayoutParams(params);
                                container.addView(groupEt);
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("그룹 추가")
                                        .setMessage("\n추가할 그룹을 입력해주세요.(최대 6글자)")
                                        .setView(container)
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String group = groupEt.getText().toString();
                                                if(group.length() >6){
                                                    Toast.makeText(getActivity().getApplicationContext(),"6자 이내로 작성해주세요.",Toast.LENGTH_SHORT).show();
                                                } else {
                                                    groupRecyclerView.setAdapter(groupAdapter);
                                                    addGroupToDB(group);
                                                }

                                            }
                                        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        }).show();
                                break;
                            case R.id.add_item_menu:
                                mainActivity.onFragmentChange(1);
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
                popup.show();

            }
        });

        return rootView;
    }

    public void loadDB(String url,String group){
        StringRequest request = new StringRequest(
                            Request.Method.POST,
                            url,
                new Response.Listener<String>() {  //응답을 문자열로 받아서 여기다 넣어달란말임(응답을 성공적으로 받았을 떄 이메소드가 자동으로 호출됨
                        @Override
                        public void onResponse(String response) {
                            itemList.clear();
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                itemRecyclerView.setAdapter(homeAdapter);
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String name = jsonObject.getString("itemName"); //no가 문자열이라서 바꿔야함.
                                    String so_birth = jsonObject.getString("itemSolarBirth");
                                    String lu_birth = jsonObject.getString("itemLunarBirth");
                                    String memo = jsonObject.getString("itemMemo");
                                    String group = jsonObject.getString("itemGroup");
                                    int is_alarm_on = jsonObject.getInt("itemAlarmOn");
                                    String gender = jsonObject.getString("itemGender");
                                    int itemRequestCode = jsonObject.getInt("itemRequestCode");

                                    if(gender.equals("남")){
                                        ItemData itemData= new ItemData(name, group,R.drawable.profile_man_icon,so_birth, lu_birth, memo, is_alarm_on,"","",itemRequestCode); // 첫 번째 매개변수는 몇번째에 추가 될지, 제일 위에 오도록
                                        itemList.add(itemData);

                                    } else {
                                        ItemData itemData= new ItemData(name, group,R.drawable.profile_woman_icon,so_birth, lu_birth, memo, is_alarm_on,"","",itemRequestCode); // 첫 번째 매개변수는 몇번째에 추가 될지, 제일 위에 오도록
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
                params.put("itemGroup",group);
                Log.d("아이템아이디",mainActivity.userId);
                return params;
            }
        };
        //실제 요청 작업을 수행해주는 요청큐 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity().getApplicationContext());
        //요청큐에 요청 객체 생성
        requestQueue.add(request);
        Log.d("로드디비2","ㄱ");
    }
    public void addGroupToDB(String group){
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                    if(!group.equals("")){
                        if(success){
                            GroupData groupData= new GroupData(group); // 첫 번째 매개변수는 몇번째에 추가 될지, 제일 위에 오도록
                            groupList.add(groupData);
                            groupAdapter.notifyDataSetChanged();
                            Toast.makeText(getContext(),"추가 완료",Toast.LENGTH_SHORT).show();
                        } else{
                            Toast.makeText(getContext(),"그룹이 중복되지 않도록 기입해주세요.",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                       Toast.makeText(getContext(),"그룹을 입력하지 않았습니다.",Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(getContext(),"그룹 추가 오류",Toast.LENGTH_SHORT).show();
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String exceptionAsStrting = sw.toString();
                    Log.e("그룹 추가 오류", exceptionAsStrting);
                    e.printStackTrace();
                }
            }
        };
        GroupAddRequest groupAddRequest = new GroupAddRequest(mainActivity.userId,group,responseListener);
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        queue.add(groupAddRequest);
    }
    //그룹 불러올때 랜덤으로 불러와서 뒤죽박죽임 나중에 해결 ㄱㄱ 23.02.27
    public void loadGroupFromDB(String url){
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {  //응답을 문자열로 받아서 여기다 넣어달란말임(응답을 성공적으로 받았을 떄 이메소드가 자동으로 호출됨
                    @Override
                    public void onResponse(String response) {
                        groupList.clear();
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            groupRecyclerView.setAdapter(groupAdapter);
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String group = jsonObject.getString("myGroup"); //no가 문자열이라서 바꿔야함.

                                GroupData groupData= new GroupData(group); // 첫 번째 매개변수는 몇번째에 추가 될지, 제일 위에 오도록
                                if(group.equals("전체")){
                                    groupList.add(groupData);
                                    Collections.swap(groupList,0,i);
                                } else {
                                    groupList.add(groupData);
                                }

                                groupAdapter.notifyItemInserted(i);
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
                params.put("itemId", mainActivity.userId);
                return params;
            }
        };
        //실제 요청 작업을 수행해주는 요청큐 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity().getApplicationContext());
        //요청큐에 요청 객체 생성
        requestQueue.add(request);
        Log.d("로드디비2","ㄱ");
    }
    public void allAlarmStart(String url,String group){
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
                            LocalDate now = LocalDate.now();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String name = jsonObject.getString("itemName"); //no가 문자열이라서 바꿔야함.
                                String so_birth = jsonObject.getString("itemSolarBirth");
                                String lu_birth = jsonObject.getString("itemLunarBirth");
                                int itemRequestCode = jsonObject.getInt("itemRequestCode");
                                int month = Integer.parseInt(so_birth.substring(4,6));
                                int day = Integer.parseInt(so_birth.substring(6,8));

                                //생일이 이미 지났다면 다음년도로 알림설정
                                if(now.getMonthValue()>month || (now.getMonthValue()==month && now.getDayOfMonth()>day)){
                                    mainActivity.setNotice(now.getYear()+1,month,day-2
                                            ,day,0,0,itemRequestCode,name+"님의 생일",itemRequestCode);
                                } else {
                                    mainActivity.setNotice(now.getYear(),month,day-2
                                            ,day,0,0,itemRequestCode,name+"님의 생일",itemRequestCode);
                                }
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
                params.put("itemGroup",group);
                Log.d("아이템아이디",mainActivity.userId);
                return params;
            }
        };
        //실제 요청 작업을 수행해주는 요청큐 객체 생성
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity().getApplicationContext());
        //요청큐에 요청 객체 생성
        requestQueue.add(request);
        Log.d("로드디비2","ㄱ");
    }
    //그룹 첫번째를 클릭하는 메소드
    public void clickFirstGroup(){
        //딜레이를 살짝 주지 않으면 클릭할 그룹이 없어서 오류남
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GroupLinearLayoutManager.scrollToPositionWithOffset(0,0); //그룹 리사이클러뷰 스크롤을 최상단으로 이동
                //화면에 보이는 그룹 리사이클러뷰의 첫번째 클릭
                groupRecyclerView.findViewHolderForLayoutPosition(GroupLinearLayoutManager.findFirstCompletelyVisibleItemPosition()).itemView.performClick();

            }
        },10);
    }

}