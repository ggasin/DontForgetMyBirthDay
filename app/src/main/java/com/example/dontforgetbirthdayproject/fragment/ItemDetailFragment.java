package com.example.dontforgetbirthdayproject.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.dontforgetbirthdayproject.request.ItemUpdateRequest;
import com.example.dontforgetbirthdayproject.R;
import com.example.dontforgetbirthdayproject.activity.MainActivity;
import com.example.dontforgetbirthdayproject.adapter.HomeAdapter;
import com.example.dontforgetbirthdayproject.data.ItemData;
import com.example.dontforgetbirthdayproject.request.ItemDeleteRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;


public class ItemDetailFragment extends Fragment {

    MainActivity mainActivity;
    private ArrayList<ItemData> itemList;
    private HomeAdapter homeAdapter;
    private TextView itemDetailName,itemDetailSolar,itemDetailLunar,itemDetailGroup;
    private EditText itemDetailMemo,itemDetailEditSolar,itemDetailEditLunar,itemDetailEditName;
    private ImageButton itemDetailCloseBtn,itemDetailDeleteBtn,itemDetailAlterBtn;
    private LinearLayout itemDetailMemoLy;
    private Button itemDetailCompleteBtn,itemDetailCalcelBtn;
    private LinearLayout itemDetailBtnLy;
    private ImageView itemDetailProfile;
    int requestCode;

    boolean isValidBirth = false;
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
        itemDetailEditLunar = rootView.findViewById(R.id.item_detail_edit_lunar);

        itemDetailBtnLy = rootView.findViewById(R.id.item_detail_btn_ly);
        itemDetailMemo = rootView.findViewById(R.id.item_detail_memo_et);

        //사용자 정보로 세팅
        itemDetailProfile.setImageResource(mainActivity.profile_id);
        itemDetailName.setText(mainActivity.itemName);
        itemDetailGroup.setText(mainActivity.itemGroup);
        itemDetailSolar.setText(mainActivity.itemSolarBirth);
        itemDetailLunar.setText(mainActivity.itemlunarBirth);
        requestCode = mainActivity.itemRequestCode;
        Log.d("itemRequestCode",String.valueOf(mainActivity.itemRequestCode));

        itemDetailMemoLy.setBackgroundResource(R.drawable.memo_cant_edit_border);//배경색 설정
        itemDetailMemo.setEnabled(false);

        itemList = new ArrayList<>();
        homeAdapter = new HomeAdapter(getActivity().getApplicationContext(),itemList);


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
               String lunar = itemDetailEditLunar.getText().toString();
               String memo = itemDetailMemo.getText().toString();
               if(solar.length()==8){
                   int solarBirthYear = Integer.parseInt(solar.substring(0,4)); //생년 인트형 변환
                   //유효한 생년월일인지 체크
                   if(solarBirthYear>1900 && solarBirthYear<=now.getYear()){
                       isValidBirth = true;
                   }
               } else {
                   isValidBirth = false;
               }
               Response.Listener<String> responseListener = new Response.Listener<String>() {
                   @Override
                   public void onResponse(String response) {
                       try {
                           JSONObject jsonObject = new JSONObject(response);
                           boolean success = jsonObject.getBoolean("success");
                           if(!itemDetailEditName.equals("")&&!itemDetailEditSolar.equals("")&&!itemDetailEditLunar.equals("")&&isValidBirth){
                               if (success) {
                                   Toast.makeText(getContext(), "수정 완료", Toast.LENGTH_SHORT).show();
                                   editModeOff();
                                   mainActivity.onFragmentChange(0);

                               } else {
                                   Toast.makeText(getContext(), "이름이 중복됩니다. 중복되지 않는 이름을 기입해주세요.", Toast.LENGTH_SHORT).show();
                               }

                           } else {
                               if(!isValidBirth){
                                   Toast.makeText(getActivity().getApplicationContext(),"유효하지 않은 생년월일 입니다.",Toast.LENGTH_SHORT).show();
                               } else {
                                   Toast.makeText(getActivity().getApplicationContext(),"입력하지 않은 란이 있는지 확인해 주세요.",Toast.LENGTH_SHORT).show();
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

               ItemUpdateRequest itemUpdateRequest = new ItemUpdateRequest(mainActivity.userId, group,name,
                       beforeName,solar,lunar,memo,responseListener);
               RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
               queue.add(itemUpdateRequest);

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
                mainActivity.onFragmentChange(0);
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
        itemDetailLunar.setVisibility(View.GONE);
        //EditText와 완료,취소 버튼 보이기
        itemDetailEditName.setVisibility(View.VISIBLE);
        itemDetailEditName.setText(itemDetailName.getText().toString());
        itemDetailEditSolar.setVisibility(View.VISIBLE);
        itemDetailEditSolar.setText(itemDetailSolar.getText().toString());
        itemDetailEditLunar.setVisibility(View.VISIBLE);
        itemDetailEditLunar.setText(itemDetailLunar.getText().toString());
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
        itemDetailEditLunar.setVisibility(View.GONE);
        itemDetailBtnLy.setVisibility(View.GONE);
        itemDetailMemoLy.setBackgroundResource(R.drawable.memo_cant_edit_border);
        itemDetailMemo.setEnabled(false);
    }
}