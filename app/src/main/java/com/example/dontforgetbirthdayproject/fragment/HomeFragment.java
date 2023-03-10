package com.example.dontforgetbirthdayproject.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.example.dontforgetbirthdayproject.listener.GetGroupPositionListener;
import com.example.dontforgetbirthdayproject.listener.OnGroupLongClickListener;
import com.example.dontforgetbirthdayproject.adapter.GroupAdapter;
import com.example.dontforgetbirthdayproject.request.GroupAddRequest;
import com.example.dontforgetbirthdayproject.data.GroupData;
import com.example.dontforgetbirthdayproject.adapter.HomeAdapter;
import com.example.dontforgetbirthdayproject.data.ItemData;
import com.example.dontforgetbirthdayproject.listener.OnItemClickListener;
import com.example.dontforgetbirthdayproject.R;
import com.example.dontforgetbirthdayproject.activity.MainActivity;
import com.example.dontforgetbirthdayproject.request.GroupDeleteRequest;
import com.example.dontforgetbirthdayproject.request.GroupUpdateRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class HomeFragment extends Fragment  {

    MainActivity mainActivity;
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
    // search_list ????????? ?????? ????????? ?????? ???????????? ?????? ?????????
    private ArrayList<ItemData> itemList, searchList;
    private ArrayList<GroupData> groupList;
    //????????? ???????????? ???????????? ???????????? ?????? ???????????? ????????? spinner??? ????????? ????????? ?????? groupSpinnerArr??? group??? db????????? ???????????? ??????.
    public static ArrayList<String> groupSpinnerArr;
    private HomeAdapter homeAdapter;
    private GroupAdapter groupAdapter;
    private RecyclerView itemRecyclerView,groupRecyclerView;
    private LinearLayoutManager ItemLinearLayoutManager,GroupLinearLayoutManager;
    private LinearLayout searchEditLy,upsideBtnLy;
    private ImageButton itemAddBtn,searchBtn,searchCloseBtn,sortBtn;
    private EditText searchEt;
    private TextView solarDdayText;
    String loadItemURL = "http://dfmbd.ivyro.net/LoadItemDB.php";
    String loadGroupURL = "http://dfmbd.ivyro.net/LoadGroupDB.php";
    String groupUpdateURL = "http://dfmbd.ivyro.net/GroupUpdate.php";
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



        searchBtn = rootView.findViewById(R.id.home_search_btn);
        searchEt = rootView.findViewById(R.id.home_search_et);
        searchEditLy = rootView.findViewById(R.id.home_search_et_ly);
        searchCloseBtn = rootView.findViewById(R.id.home_search_close_btn);
        sortBtn = rootView.findViewById(R.id.home_sort_btn);
        solarDdayText = rootView.findViewById(R.id.recycler_item_so_dday_tv);
        upsideBtnLy = rootView.findViewById(R.id.home_btn_ly);

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


        //db????????? ???????????? ????????? recyclerView??? ?????????
        loadGroupFromDB(loadGroupURL);



        Log.d("home first Login",String.valueOf(mainActivity.firstLogin));
        //???????????? ?????? ??? ???????????? ??????????????? ??? ????????? ????????? ?????? ???????????????.
        if(mainActivity.firstLogin){
            SharedPreferences alarmSettingPreferences = getActivity().getSharedPreferences("alarmSetting", Activity.MODE_PRIVATE);
            String settingAlarm= alarmSettingPreferences.getString("settingAlarm", null);//
            String whenAlarmStart = alarmSettingPreferences.getString("whenAlarmStart", null);
            if(settingAlarm == null && whenAlarmStart ==null){
                mainActivity.allAlarmStart(allAlarmOnURL,"??????");
            } else {
                switch (settingAlarm){
                    case "?????? ?????? ??????":
                        mainActivity.allCancelAlarm(allAlarmOffURL,"??????");;
                        break;
                    case "?????? ?????? ??????":
                        mainActivity.allAlarmStart(allAlarmOnURL,"??????");;
                        break;
                    default:
                        break;
                }
            }
            mainActivity.firstLogin=false;
        }

        //?????? ?????? ?????? ?????????
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               upsideBtnLy.setVisibility(View.GONE);
               searchEditLy.setVisibility(View.VISIBLE);
            }
        });

        //????????? ?????? x ?????? ?????? ?????????
        searchCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upsideBtnLy.setVisibility(View.VISIBLE);
                searchEt.setText("");
                searchEditLy.setVisibility(View.GONE);
                loadDB(loadItemURL,mainActivity.selectedGroup);
                //??????????????? ????????? ????????? ??????
                InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            }
        });

        // ?????? ????????? ??????
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

        //?????? ?????? ??????
        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //?????? ?????? ??????
                PopupMenu popup= new PopupMenu(getActivity().getApplicationContext(), view);//v??? ????????? ?????? ??????
                popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            //?????? ????????????
                            case R.id.sort_asc_menu:
                                recyclerViewAcsSort(itemList);


                                break;
                                //?????? ????????????
                            case R.id.sort_des_menu:
                                recyclerViewDesSort(itemList);
                                break;
                                //????????? ?????? ???
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
                if(item.getItem_alarm_on()==1){
                    mainActivity.itemAlarmOnoff = true;
                } else{
                    mainActivity.itemAlarmOnoff =false;
                }
                mainActivity.ifTrueCalenderElseHome = false;
                searchEt.setText(""); //?????????????????? ?????? ???????????? ???????????? ??????????????? ????????? ?????? ???????????? ???????????? ??????edit??? ????????? ??????????????? ???????????? ????????? list??? ??????
                for(int i=0;i<groupSpinnerArr.size();i++){
                    Log.d("?????? ????????? ??????1",groupSpinnerArr.get(i));
                }
                mainActivity.groupArr = groupSpinnerArr;
                for(int i=0;i<mainActivity.groupArr.size();i++){
                    Log.d("?????? ????????? ??????2",mainActivity.groupArr.get(i));
                }
                mainActivity.onFragmentChange(2);
                Log.d("????????? ??????","??????");
            }
        });


        //?????? ?????? ???
        groupAdapter.setOnGroupClicklistener(new GetGroupPositionListener() {
            @Override
            public void getGroupPosition(int position) {
                loadDB(loadItemURL,groupAdapter.getItem(position).getGroup());
                mainActivity.selectedGroup = groupAdapter.getItem(position).getGroup().toString();
                Log.d("????????? ??????",groupAdapter.getItem(position).getGroup().toString());

            }
        });

        //?????? ??? ????????? ???
        groupAdapter.setOnGroupLongCLickListener(new OnGroupLongClickListener() {
            @Override
            public void onGroupLongClick(GroupAdapter.CustomViewHolder holder, View view, int position) {
                //????????? ????????? ??????
                String selectedGroup = groupAdapter.getItem(position).getGroup();
                //?????? ?????? ??????
                PopupMenu popup= new PopupMenu(getActivity().getApplicationContext(), view);//v??? ????????? ?????? ??????
                popup.getMenuInflater().inflate(R.menu.group_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            //?????? ?????? ??????
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
                                builder.setTitle("?????? ?????? ??????")
                                        .setMessage("\n??????????????? ?????? ????????? ????????? ??????????????????.(?????? 6??????)")
                                        .setView(container)
                                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String newGroupName = groupUpdateEt.getText().toString();
                                                if(newGroupName.length() >6){
                                                    Toast.makeText(getActivity().getApplicationContext(),"6??? ????????? ??????????????????.",Toast.LENGTH_SHORT).show();
                                                } else {
                                                    updateGroupName(newGroupName,selectedGroup);
                                                    groupRecyclerView.setAdapter(groupAdapter);
                                                }

                                            }
                                        }).setNegativeButton("??????", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        }).show();
                                break;
                            //?????? ??????
                            //2023-03-11 ?????? ????????? ????????? ??? ????????? ??????
                            case R.id.group_delete_menu:
                                Log.d("group LongCLick",groupAdapter.getItem(position).getGroup());
                                if(groupAdapter.getItem(position).getGroup().equals("??????")){
                                    Toast.makeText(getContext(),"?????? ????????? ????????? ??? ????????????.",Toast.LENGTH_SHORT).show();
                                } else {
                                    //????????? ????????? ??????????????? ??????
                                    AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(getActivity());
                                    deleteBuilder.setTitle("?????? ?????????????????????????\n?????? ??? ???????????? ?????? ???????????????.")
                                            .setPositiveButton("???", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    mainActivity.allCancelAlarm(loadItemURL,selectedGroup);
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
                                                                            GroupLinearLayoutManager.scrollToPositionWithOffset(0,0); //?????? ?????????????????? ???????????? ??????????????? ??????
                                                                            Toast.makeText(getContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                                                                            //????????? ?????? ??????
                                                                            Handler handler = new Handler();
                                                                            handler.postDelayed(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    GroupLinearLayoutManager.scrollToPositionWithOffset(0,0); //?????? ?????????????????? ???????????? ??????????????? ??????
                                                                                    //????????? ????????? ?????? ????????????????????? ????????? ??????
                                                                                    groupRecyclerView.findViewHolderForLayoutPosition(GroupLinearLayoutManager.findFirstCompletelyVisibleItemPosition()).itemView.performClick();
                                                                                }
                                                                            },15);

                                                                        } else {
                                                                            Toast.makeText(getContext(), "?????? ??????.", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    } catch (JSONException e) {
                                                                        Toast.makeText(getContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                                                                        StringWriter sw = new StringWriter();
                                                                        e.printStackTrace(new PrintWriter(sw));
                                                                        String exceptionAsStrting = sw.toString();
                                                                        Log.e("?????? ??????", exceptionAsStrting);
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
                                            .setNegativeButton("?????????", new DialogInterface.OnClickListener() {
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

        //?????? ????????? ?????? ?????????
        itemAddBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                //?????? ?????? ??????
                PopupMenu popup= new PopupMenu(getActivity().getApplicationContext(), view);//v??? ????????? ?????? ??????
                popup.getMenuInflater().inflate(R.menu.add_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            //?????? ??????
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
                                builder.setTitle("?????? ??????")
                                        .setMessage("\n????????? ????????? ??????????????????.(?????? 6??????)")
                                        .setView(container)
                                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String group = groupEt.getText().toString();
                                                if(group.length() >6){
                                                    Toast.makeText(getActivity().getApplicationContext(),"6??? ????????? ??????????????????.",Toast.LENGTH_SHORT).show();
                                                } else {
                                                    groupRecyclerView.setAdapter(groupAdapter);
                                                    addGroupToDB(group);
                                                }

                                            }
                                        }).setNegativeButton("??????", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        }).show();
                                break;
                                //????????? ??????
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
                new Response.Listener<String>() {  //????????? ???????????? ????????? ????????? ??????????????????(????????? ??????????????? ????????? ??? ??????????????? ???????????? ?????????
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onResponse(String response) {
                            itemList.clear();
                            searchList.clear();
                            LocalDate now = LocalDate.now();
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
                                        ItemData itemData= new ItemData(name, group,R.drawable.profile_man_icon,so_birth, lu_birth, memo,
                                                is_alarm_on,"D-"+solarDday,"",itemRequestCode); // ??? ?????? ??????????????? ???????????? ?????? ??????, ?????? ?????? ?????????
                                        itemList.add(itemData);
                                    } else {
                                        ItemData itemData= new ItemData(name, group,R.drawable.profile_woman_icon,so_birth, lu_birth, memo,
                                                is_alarm_on,"D-"+solarDday,"",itemRequestCode); // ??? ?????? ??????????????? ???????????? ?????? ??????, ?????? ?????? ?????????
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
                params.put("itemGroup",group);
                Log.d("??????????????????",mainActivity.userId);
                return params;
            }
        };
        //?????? ?????? ????????? ??????????????? ????????? ?????? ??????
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity().getApplicationContext());
        //???????????? ?????? ?????? ??????
        requestQueue.add(request);
        Log.d("????????????1","???");

    }
    //?????? DB??? ???????????? ?????????
    public void addGroupToDB(String group){
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                    if(!group.equals("")){
                        if(success){
                            GroupData groupData= new GroupData(group); // ??? ?????? ??????????????? ???????????? ?????? ??????, ?????? ?????? ?????????
                            groupList.add(groupData);
                            groupSpinnerArr.add(group);
                            groupAdapter.notifyDataSetChanged();
                            Toast.makeText(getContext(),"?????? ??????",Toast.LENGTH_SHORT).show();
                        } else{
                            Toast.makeText(getContext(),"????????? ???????????? ????????? ??????????????????.",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                       Toast.makeText(getContext(),"????????? ???????????? ???????????????.",Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(getContext(),"?????? ?????? ??????",Toast.LENGTH_SHORT).show();
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String exceptionAsStrting = sw.toString();
                    Log.e("?????? ?????? ??????", exceptionAsStrting);
                    e.printStackTrace();
                }
            }
        };
        GroupAddRequest groupAddRequest = new GroupAddRequest(mainActivity.userId,group,responseListener);
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        queue.add(groupAddRequest);
    }
    //????????? DB????????? ???????????? ?????????
    public void loadGroupFromDB(String url){
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {  //????????? ???????????? ????????? ????????? ??????????????????(????????? ??????????????? ????????? ??? ??????????????? ???????????? ?????????
                    @Override
                    public void onResponse(String response) {
                        groupList.clear();
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            groupRecyclerView.setAdapter(groupAdapter);
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String group = jsonObject.getString("myGroup"); //no??? ?????????????????? ????????????.
                                groupSpinnerArr.add(group);
                                GroupData groupData= new GroupData(group); // ??? ?????? ??????????????? ???????????? ?????? ??????, ?????? ?????? ?????????
                                if(group.equals("??????")){
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
                params.put("itemId", mainActivity.userId);
                return params;
            }
        };
        //?????? ?????? ????????? ??????????????? ????????? ?????? ??????
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity().getApplicationContext());
        //???????????? ?????? ?????? ??????
        requestQueue.add(request);
        Log.d("????????????2","???");
    }
    //?????? ?????? ?????? ?????????
    public void updateGroupName(String group, String beforeName){
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                    if(!group.equals("")){
                        if(success){
                            Toast.makeText(getContext(),"?????? ??????",Toast.LENGTH_SHORT).show();
                            loadGroupFromDB(loadGroupURL);
                        } else{
                            Toast.makeText(getContext(),"????????? ???????????? ????????? ??????????????????",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        Toast.makeText(getContext(),"????????? ???????????? ???????????????",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getContext(),"?????? ?????? ??????",Toast.LENGTH_SHORT).show();
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String exceptionAsStrting = sw.toString();
                    Log.e("?????? ?????? ??????", exceptionAsStrting);
                    e.printStackTrace();
                }
            }
        };
        GroupUpdateRequest groupUpdateRequest = new GroupUpdateRequest(mainActivity.userId,group,beforeName,responseListener);
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        queue.add(groupUpdateRequest);
    }



    //?????? ???????????? ???????????? ?????????. groupAdapter?????? recyclerview ???????????? ????????? ??? ????????? ??????.
    //??? ???????????? groupAdapter?????? ??????
    public void clickFirstGroup(){
        recyclerViewDaySort(itemList);
        //???????????? ?????? ?????? ????????? ????????? ????????? ????????? ?????????
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GroupLinearLayoutManager.scrollToPositionWithOffset(0,0); //?????? ?????????????????? ???????????? ??????????????? ??????
                //????????? ????????? ?????? ????????????????????? ????????? ??????
                groupRecyclerView.findViewHolderForLayoutPosition(GroupLinearLayoutManager.findFirstCompletelyVisibleItemPosition()).itemView.performClick();


            }
        },15);
    }
    //?????? ???????????? ??????
    public void recyclerViewAcsSort(ArrayList<ItemData> item){
        Collections.sort(item, new Comparator<ItemData>() {
            @Override
            public int compare(ItemData itemData, ItemData t1) {
                return itemData.getTv_item_name().compareTo(t1.getTv_item_name());
            }
        });
        itemRecyclerView.setAdapter(homeAdapter);
    }
    //?????? ???????????? ??????
    public void recyclerViewDesSort(ArrayList<ItemData> item){
        Collections.sort(item,new Comparator<ItemData>() {
            @Override
            public int compare(ItemData itemData, ItemData t1) {
                return t1.getTv_item_name().compareTo(itemData.getTv_item_name());
            }
        });
        itemRecyclerView.setAdapter(homeAdapter);
    }
    //????????? ?????? ????????? ??????
    public void recyclerViewDaySort(ArrayList<ItemData> item){
        Collections.sort(item,new Comparator<ItemData>() {
            @Override
            public int compare(ItemData itemData, ItemData t1) {
                int item1 = Integer.parseInt(itemData.getTv_item_so_dday().substring(2));
                int item2 = Integer.parseInt(t1.getTv_item_so_dday().substring(2));
                Log.d("?????? ??????",itemData.getTv_item_so_dday().substring(2));
                Log.d("?????? ??????1",t1.getTv_item_so_dday().substring(2));
                return item1>item2 ? 1 : item1<item2 ? -1 : 0;
                //int compare(T o1, T o2) ??? ??? ????????? ?????? ?????? ???????????? ????????? ?????????, o1??? ????????? ????????? ??????, ????????? ????????? o2??? ????????? ????????? ????????????.
            }
        });

        itemRecyclerView.setAdapter(homeAdapter);
        homeAdapter.notifyDataSetChanged();

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