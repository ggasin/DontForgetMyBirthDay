package com.dfmbd.dontforgetbirthdayproject.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dfmbd.dontforgetbirthdayproject.apiClass.SetAlarmApiManager;
import com.dfmbd.dontforgetbirthdayproject.listener.GetGroupPositionListener;
import com.dfmbd.dontforgetbirthdayproject.listener.OnGroupLongClickListener;
import com.dfmbd.dontforgetbirthdayproject.adapter.GroupAdapter;
import com.dfmbd.dontforgetbirthdayproject.request.GroupAddRequest;
import com.dfmbd.dontforgetbirthdayproject.data.GroupData;
import com.dfmbd.dontforgetbirthdayproject.adapter.HomeAdapter;
import com.dfmbd.dontforgetbirthdayproject.data.ItemData;
import com.dfmbd.dontforgetbirthdayproject.listener.OnItemClickListener;
import com.dfmbd.dontforgetbirthdayproject.R;
import com.dfmbd.dontforgetbirthdayproject.activity.MainActivity;
import com.dfmbd.dontforgetbirthdayproject.request.GroupDeleteRequest;
import com.dfmbd.dontforgetbirthdayproject.request.GroupUpdateRequest;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
    // search_list 검색시 같은 이름이 있는 아이템이 담길 리스트
    private ArrayList<ItemData> itemList, searchList;
    private ArrayList<GroupData> groupList;
    //아이템 상세정보 들어가서 수정할때 현재 존재하는 그룹을 spinner에 바인딩 시키기 위해 groupSpinnerArr에 group을 db로부터 가져올때 추가.
    public static ArrayList<String> groupSpinnerArr;
    private HomeAdapter homeAdapter;
    private GroupAdapter groupAdapter;
    private RecyclerView itemRecyclerView,groupRecyclerView;
    private LinearLayoutManager ItemLinearLayoutManager,GroupLinearLayoutManager;
    private LinearLayout searchEditLy,upsideBtnLy,progressBarLy;
    private ProgressBar loadingSpinner;
    private ImageButton itemAddBtn,searchBtn,searchCloseBtn,sortBtn , helpBtn;
    private EditText searchEt;
    SetAlarmApiManager setAlarmApiManager = new SetAlarmApiManager();
    String loadItemURL = "http://dfmbd.ivyro.net/LoadItemDB.php";
    String loadGroupURL = "http://dfmbd.ivyro.net/LoadGroupDB.php";
    String allAlarmOnURL = "http://dfmbd.ivyro.net/AllAlarmOn.php";
    String allAlarmOffURL = "http://dfmbd.ivyro.net/AllAlarmOff.php";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_home, container, false);

        helpBtn = rootView.findViewById(R.id.home_help_btn);
        searchBtn = rootView.findViewById(R.id.home_search_btn);
        searchEt = rootView.findViewById(R.id.home_search_et);
        searchEditLy = rootView.findViewById(R.id.home_search_et_ly);
        searchCloseBtn = rootView.findViewById(R.id.home_search_close_btn);
        sortBtn = rootView.findViewById(R.id.home_sort_btn);
        upsideBtnLy = rootView.findViewById(R.id.home_btn_ly);
        progressBarLy = rootView.findViewById(R.id.home_progress_bar_ly);
        loadingSpinner = rootView.findViewById(R.id.home_progress_bar);

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
        searchList = new ArrayList<>();
        groupSpinnerArr = new ArrayList<>();


        itemAddBtn = rootView.findViewById(R.id.item_add_btn);
        homeAdapter = new HomeAdapter(getActivity().getApplicationContext(),itemList);
        groupAdapter = new GroupAdapter(getActivity().getApplicationContext(),groupList,this);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        Log.d("year",year+","+month+","+day+"");



        //db로부터 데이터를 가져와 recyclerView에 바인딩
        loadGroupFromDB(loadGroupURL, new Runnable() {
            @Override
            public void run() {
                clickFirstGroup();
                mainActivity.groupArr = groupSpinnerArr;
                hideLoading();
            }
        });
        Log.d("home first Login",String.valueOf(mainActivity.firstLogin));
        Log.d("before Fragment",mainActivity.beforeFragment);
        //로그인을 처음 한 상태라면 로그아웃때 다 취소한 알람을 다시 설정해야함.
        if(mainActivity.firstLogin){
            SharedPreferences alarmSettingPreferences = getActivity().getSharedPreferences("alarmSetting", Activity.MODE_PRIVATE);
            String settingAlarm= alarmSettingPreferences.getString("settingAlarm", null);//
            String whenAlarmStart = alarmSettingPreferences.getString("whenAlarmStart", null);
            if(settingAlarm == null && whenAlarmStart ==null){
                setAlarmApiManager.allAlarmStart(getActivity().getApplicationContext(),mainActivity.alarmManager,
                        allAlarmOnURL,"전체",mainActivity.getSharedWhenStartAlarm(),mainActivity.userId);
                //mainActivity.allAlarmStart(allAlarmOnURL,"전체");
            } else {
                switch (settingAlarm){
                    case "전체 알람 끄기":
                        setAlarmApiManager.allCancelAlarm(getActivity().getApplicationContext(),mainActivity.alarmManager,allAlarmOffURL,"전체",mainActivity.userId);;
                        break;
                    case "전체 알람 켜기":
                        setAlarmApiManager.allAlarmStart(getActivity().getApplicationContext(),mainActivity.alarmManager,
                                allAlarmOnURL,"전체",mainActivity.getSharedWhenStartAlarm(),mainActivity.userId);
                        break;
                    default:
                        break;
                }
            }
            mainActivity.firstLogin=false;
        }


        //검색 버튼 클릭 이벤트
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               upsideBtnLy.setVisibility(View.GONE);
               searchEditLy.setVisibility(View.VISIBLE);
            }
        });

        //검색창 안에 x 버튼 클릭 이벤트
        searchCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upsideBtnLy.setVisibility(View.VISIBLE);
                searchEt.setText("");
                searchEditLy.setVisibility(View.GONE);
                loadDB(loadItemURL,mainActivity.selectedGroup);
                //올라가있던 키보드 내리는 코드
                InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            }
        });

        // 검색 리스너 작성
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                String searchText = searchEt.getText().toString();
                searchList.clear();
                if(searchText.equals("")){
                    homeAdapter.filterList(itemList);
                } else {
                    for (int i = 0; i < itemList.size(); i++) {
                        if (itemList.get(i).getTv_item_name().toLowerCase().contains(searchText.toLowerCase())) {
                            searchList.add(itemList.get(i));
                        }
                    }
                    homeAdapter.filterList(searchList);
                }
            }
        });

        //도움말 버튼 클릭
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHelpDialog();

            }
        });

        //정렬 버튼 클릭
        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //팝업 메뉴 생성
                PopupMenu popup= new PopupMenu(getActivity().getApplicationContext(), view);//v는 클릭된 뷰를 의미
                popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            //이름 오름차순
                            case R.id.sort_asc_menu:
                                recyclerViewAcsSort(itemList);


                                break;
                                //이름 내림차순
                            case R.id.sort_des_menu:
                                recyclerViewDesSort(itemList);
                                break;
                                //임박한 생일 순
                            case R.id.sort_dday_menu:
                                recyclerViewDaySort(itemList);
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
                if(item.getItem_alarm_on()==1){
                    mainActivity.itemAlarmOnoff = true;
                } else{
                    mainActivity.itemAlarmOnoff =false;
                }
                mainActivity.ifTrueCalenderElseHome = false;
                searchEt.setText(""); //검색하고나서 나온 아이템을 클릭하고 상세정보에 갔다가 다시 메인으로 돌아오면 검색edit에 글씨가 남아있어서 어댑터가 엉뚱한 list를 잡음
                mainActivity.onFragmentChange(2);
                Log.d("아이템 클릭","클릭");
            }
        });

        //그룹 클릭 시
        groupAdapter.setOnGroupClicklistener(new GetGroupPositionListener() {
            @Override
            public void getGroupPosition(int position) {
                loadDB(loadItemURL,groupAdapter.getItem(position).getGroup());
                mainActivity.selectedGroup = groupAdapter.getItem(position).getGroup().toString();
                Log.d("선택된 그룹",groupAdapter.getItem(position).getGroup().toString());

            }
        });

        //그룹 꾹 눌렀을 때
        groupAdapter.setOnGroupLongCLickListener(new OnGroupLongClickListener() {
            @Override
            public void onGroupLongClick(GroupAdapter.CustomViewHolder holder, View view, int position) {
                //선택된 그룹의 이름
                String selectedGroup = groupAdapter.getItem(position).getGroup();
                //팝업 메뉴 생성
                PopupMenu popup= new PopupMenu(getActivity().getApplicationContext(), view);//v는 클릭된 뷰를 의미
                popup.getMenuInflater().inflate(R.menu.group_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            //그룹 이름 변경
                            case R.id.group_name_update_menu:
                                final EditText groupUpdateEt = new EditText(getActivity());
                                groupUpdateEt.setTextColor(Color.BLACK);
                                FrameLayout container = new FrameLayout(getActivity());
                                FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                params.leftMargin = 60;
                                params.width = 1100;
                                groupUpdateEt.setLayoutParams(params);
                                container.addView(groupUpdateEt);
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("그룹 이름 변경")
                                        .setMessage("\n변경하고자 하는 그룹의 이름을 작성해주세요.(최대 6글자)")
                                        .setView(container)
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String newGroupName = groupUpdateEt.getText().toString();
                                                if(newGroupName.length() >6){
                                                    Toast.makeText(getActivity().getApplicationContext(),"6자 이내로 작성해주세요.",Toast.LENGTH_SHORT).show();
                                                } else {
                                                    updateGroupName(newGroupName,selectedGroup);
                                                    groupRecyclerView.setAdapter(groupAdapter);
                                                }
                                            }
                                        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        }).show();
                                break;
                            //그룹 삭제
                            //2023-03-11 여유 생기면 딜레이 왜 줬는지 체크
                            case R.id.group_delete_menu:
                                Log.d("group LongCLick",groupAdapter.getItem(position).getGroup());
                                if(groupAdapter.getItem(position).getGroup().equals("전체")){
                                    Toast.makeText(getContext(),"전체 그룹은 삭제할 수 없습니다.",Toast.LENGTH_SHORT).show();
                                } else {
                                    //안전을 위해서 다이얼로그 추가
                                    AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(getActivity());
                                    deleteBuilder.setTitle("정말 삭제하시겠습니까?\n그룹 내 데이터는 모두 삭제됩니다.")
                                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    setAlarmApiManager.allCancelAlarm(getActivity().getApplicationContext(),mainActivity.alarmManager,loadItemURL,selectedGroup,mainActivity.userId);
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
                                                                            },15);

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
                                                            GroupDeleteRequest groupDeleteRequest = new GroupDeleteRequest(mainActivity.userId, selectedGroup, responseListener);
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
                            //그룹 추가
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
                                //아이템 추가
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
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onResponse(String response) {
                            itemList.clear();
                            searchList.clear();

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
                                        ItemData itemData= new ItemData(name, group,R.drawable.profile_man_icon,so_birth, lu_birth, memo,
                                                is_alarm_on,"D-"+solarDday,"",itemRequestCode); // 첫 번째 매개변수는 몇번째에 추가 될지, 제일 위에 오도록
                                        itemList.add(itemData);
                                    } else {
                                        ItemData itemData= new ItemData(name, group,R.drawable.profile_woman_icon,so_birth, lu_birth, memo,
                                                is_alarm_on,"D-"+solarDday,"",itemRequestCode); // 첫 번째 매개변수는 몇번째에 추가 될지, 제일 위에 오도록
                                        itemList.add(itemData);
                                    }
                                    homeAdapter.notifyItemInserted(i);

                                }
                            } catch (JSONException e) {
                                StringWriter sw = new StringWriter();
                                e.printStackTrace(new PrintWriter(sw));
                                String exceptionAsStrting = sw.toString();
                                Log.e("loadDBError", exceptionAsStrting);
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
        Log.d("로드디비1","ㄱ");

    }
    //그룹 DB로 추가하는 메소드
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
                            groupSpinnerArr.add(group);
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
    //그룹을 DB로부터 가져오는 메소드
    public void loadGroupFromDB(String url, final Runnable callback){
        showLoading();
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
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String group = jsonObject.getString("myGroup"); //no가 문자열이라서 바꿔야함.
                                groupSpinnerArr.add(group);
                                GroupData groupData= new GroupData(group); // 첫 번째 매개변수는 몇번째에 추가 될지, 제일 위에 오도록
                                if(group.equals("전체")){
                                    groupList.add(groupData);
                                    Collections.swap(groupList,0,i);
                                } else {
                                    groupList.add(groupData);
                                }
                                groupAdapter.notifyItemInserted(i);
                            }
                            if (callback != null) {
                                callback.run();
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
    //그룹 이름 변경 메소드
    public void updateGroupName(String group, String beforeName){
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                    if(!group.equals("")){
                        if(success){
                            Toast.makeText(getContext(),"수정 완료",Toast.LENGTH_SHORT).show();
                            loadGroupFromDB(loadGroupURL, new Runnable() {
                                @Override
                                public void run() {
                                    clickFirstGroup();
                                    mainActivity.groupArr = groupSpinnerArr;
                                    hideLoading();
                                }
                            });
                        } else{
                            Toast.makeText(getContext(),"그룹이 중복되지 않도록 기입해주세요",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        Toast.makeText(getContext(),"그룹을 입력하지 않았습니다",Toast.LENGTH_SHORT).show();
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
        GroupUpdateRequest groupUpdateRequest = new GroupUpdateRequest(mainActivity.userId,group,beforeName,responseListener);
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        queue.add(groupUpdateRequest);
    }



    //그룹 첫번째를 클릭하는 메소드. groupAdapter에서 recyclerview 바인딩이 끝나면 이 메소드 수행.
    //이 메소드는 groupAdapter에서 사용
    //가끔 에러남 수정 필요 (groupAdapter의 onAttachedToRecyclerView는 홀더의 생성때 호출되기 때문에 딜레이를 넣지 않으면 오류가 남.)
    public void clickFirstGroup(){
        //딜레이를 살짝 주지 않으면 클릭할 그룹이 없어서 오류남
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GroupLinearLayoutManager.scrollToPositionWithOffset(0,0); //그룹 리사이클러뷰 스크롤을 최상단으로 이동
                //화면에 보이는 그룹 리사이클러뷰의 첫번째 클릭
                groupRecyclerView.findViewHolderForLayoutPosition(GroupLinearLayoutManager.findFirstCompletelyVisibleItemPosition()).itemView.performClick();
                Log.d("click First on","");
            }
        },10);
    }
    //이름 오름차순 정렬
    public void recyclerViewAcsSort(ArrayList<ItemData> item){
        Collections.sort(item, new Comparator<ItemData>() {
            @Override
            public int compare(ItemData itemData, ItemData t1) {
                return itemData.getTv_item_name().compareTo(t1.getTv_item_name());
            }
        });
        itemRecyclerView.setAdapter(homeAdapter);
    }
    //이름 내림차순 정렬
    public void recyclerViewDesSort(ArrayList<ItemData> item){
        Collections.sort(item,new Comparator<ItemData>() {
            @Override
            public int compare(ItemData itemData, ItemData t1) {
                return t1.getTv_item_name().compareTo(itemData.getTv_item_name());
            }
        });
        itemRecyclerView.setAdapter(homeAdapter);
    }
    //임박한 생일 순으로 정렬
    public void recyclerViewDaySort(ArrayList<ItemData> item){
        Collections.sort(item,new Comparator<ItemData>() {
            @Override
            public int compare(ItemData itemData, ItemData t1) {
                int item1 = Integer.parseInt(itemData.getTv_item_so_dday().substring(2));
                int item2 = Integer.parseInt(t1.getTv_item_so_dday().substring(2));
                Log.d("임박 생일",itemData.getTv_item_so_dday().substring(2));
                Log.d("임박 생일1",t1.getTv_item_so_dday().substring(2));
                return item1>item2 ? 1 : item1<item2 ? -1 : 0;
                //int compare(T o1, T o2) 로 두 객체의 특정 값을 연산해서 음수가 나오면, o1의 객체가 작다고 판단, 양수가 나오면 o2의 객체가 작다고 판단한다.
            }
        });

        itemRecyclerView.setAdapter(homeAdapter);
        homeAdapter.notifyDataSetChanged();

    }
    //D-day 구하기 메소드
    public long getBirthDday(String birth){
        long Dday=0;
        Calendar calendar = Calendar.getInstance();
        int nowYear = calendar.get(Calendar.YEAR);
        int nowMonth = calendar.get(Calendar.MONTH)+1;
        int nowDate = calendar.get(Calendar.DATE);
        birth = nowYear+birth.substring(4,8);
        //dday 구하기
        String nowMon = nowMonth+"";
        String nowDay = nowDate+"";
        if(nowMonth<10){
            nowMon = "0"+nowMonth;
        }
        if(nowDate<10){
            nowDay = "0"+nowDate;
        }
        String today = nowYear+nowMon+nowDay;
        try {
            Date todayForm = new SimpleDateFormat("yyyyMMdd").parse(today);
            Date birthForm = new SimpleDateFormat("yyyyMMdd").parse(birth);
            Dday = (birthForm.getTime() - todayForm.getTime() ) / 1000 / (24*60*60);
            if(Dday<0){
                birth = (nowYear+1)+birth.substring(4,8);
                birthForm = new SimpleDateFormat("yyyyMMdd").parse(birth);
                Dday = (birthForm.getTime() - todayForm.getTime() ) / 1000 / (24*60*60);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return Dday;
    }
    private void showLoading() {
        progressBarLy.setVisibility(View.VISIBLE);
        loadingSpinner.setVisibility(View.VISIBLE);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideLoading() {
        progressBarLy.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.GONE);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void showHelpDialog() {
        // Dialog 빌더를 생성합니다.
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Dialog 레이아웃을 생성합니다.
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.help_dialog, null);

        // Dialog 빌더에 Dialog 레이아웃을 설정합니다.
        builder.setView(view);

        // Dialog 빌더에 버튼을 추가합니다.
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Dialog 빌더를 사용하여 Dialog 를 생성하고 띄웁니다.
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}