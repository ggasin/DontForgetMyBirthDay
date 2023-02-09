package com.example.dontforgetbirthdayproject.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

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
import com.example.dontforgetbirthdayproject.adapter.GroupAdapter;
import com.example.dontforgetbirthdayproject.request.GroupAddRequest;
import com.example.dontforgetbirthdayproject.data.GroupData;
import com.example.dontforgetbirthdayproject.adapter.HomeAdapter;
import com.example.dontforgetbirthdayproject.data.ItemData;
import com.example.dontforgetbirthdayproject.OnItemClickListener;
import com.example.dontforgetbirthdayproject.R;
import com.example.dontforgetbirthdayproject.activity.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
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


    private Button item_add_btn;
    private ImageButton itemAddBtn;

    String userId,selectedGroup;


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
        groupAdapter = new GroupAdapter(getActivity().getApplicationContext(),groupList);


        //db로부터 데이터를 가져와 recyclerView에 바인딩


        loadGroupFromDB(loadGroupURL);



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

        //그룹의 첫번째가 선택되도록 함. findViewHolderForAdapterPosition을 이용하려는데 자꾸 안됨.
        //이유는 groupAdapter에서 아직 recyclerview에 attach 되어있지 않아서.
        //이 기능에 살짝의 딜레이를 넣어줬더니 작동함.
        //23-01-09 아직 문제 해결 x findViewHolderForAdapterPosition으로 하면 선택됐던 아이템이 화면에서 사라진상태로 다른 화면으로 갔다오면
        //어플 꺼짐.
        GroupLinearLayoutManager.scrollToPositionWithOffset(0,0); //그룹 리사이클러뷰 스크롤을 최상단으로 이동
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //화면에 보이는 그룹 리사이클러뷰의 첫번째 클릭
                groupRecyclerView.findViewHolderForLayoutPosition(GroupLinearLayoutManager.findFirstCompletelyVisibleItemPosition()).itemView.performClick();
            }
        },700);




        //추가 아이콘 클릭 이벤트
        itemAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.setNotice(02,8,17,25,1,"안녕",1);
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
                                    Log.d("젠더","----------------------------");
                                    Log.d("젠더",gender);
                                    if(gender.equals("남")){
                                        ItemData itemData= new ItemData(name, group,R.drawable.profile_man_icon,so_birth, lu_birth, memo, is_alarm_on,"",""); // 첫 번째 매개변수는 몇번째에 추가 될지, 제일 위에 오도록
                                        itemList.add(itemData);
                                        Log.d("젠더남",gender);
                                    } else {
                                        ItemData itemData= new ItemData(name, group,R.drawable.profile_woman_icon,so_birth, lu_birth, memo, is_alarm_on,"",""); // 첫 번째 매개변수는 몇번째에 추가 될지, 제일 위에 오도록
                                        itemList.add(itemData);
                                        Log.d("젠더여",gender);
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
                        Log.d("로드디비","ㄱ");
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
                                groupList.add(groupData);
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

}