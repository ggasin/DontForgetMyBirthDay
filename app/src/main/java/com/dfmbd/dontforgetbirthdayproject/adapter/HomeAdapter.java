package com.dfmbd.dontforgetbirthdayproject.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dfmbd.dontforgetbirthdayproject.data.ItemData;
import com.dfmbd.dontforgetbirthdayproject.listener.OnItemClickListener;
import com.dfmbd.dontforgetbirthdayproject.R;

import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.CustomViewHolder> implements OnItemClickListener {


    private Context context;
    private ArrayList<ItemData> arrayList;
    OnItemClickListener listener;


    public HomeAdapter(Context context,ArrayList<ItemData> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.birth_recycler_item,parent,false);
        CustomViewHolder holder = new CustomViewHolder(view);

        return holder;
    }




    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        holder.iv_profile.setImageResource(arrayList.get(position).getIv_profile());
        holder.tv_group.setText(arrayList.get(position).getTv_item_group());
        holder.tv_name.setText(arrayList.get(position).getTv_item_name());
        holder.tv_so_birth.setText(arrayList.get(position).getTv_item_solar_birth().substring(0,4)+"년 "+
                arrayList.get(position).getTv_item_solar_birth().substring(4,6)+"월 "+arrayList.get(position).getTv_item_solar_birth().substring(6,8)+"일");
        if(arrayList.get(position).getTv_item_lunar_birth().equals("--") || arrayList.get(position).getTv_item_lunar_birth().equals("윤달") ){
            holder.tv_lu_birth.setText(arrayList.get(position).getTv_item_lunar_birth());
        } else {
            Log.d("음력", arrayList.get(position).getTv_item_lunar_birth());
            holder.tv_lu_birth.setText(arrayList.get(position).getTv_item_lunar_birth().substring(0,4)+"년 "+
                    arrayList.get(position).getTv_item_lunar_birth().substring(4,6)+"월 "+arrayList.get(position).getTv_item_lunar_birth().substring(6,8)+"일");
        }
        holder.tv_memo.setText(arrayList.get(position).getTv_item_memo());
        holder.tv_so_dday.setText(arrayList.get(position).getTv_item_so_dday());
        holder.tv_lu_dday.setText(arrayList.get(position).getTv_item_lu_dday());



        //dday가 7일보다 아래면
        if( Integer.parseInt(arrayList.get(position).getTv_item_so_dday().substring(2))<7){
            holder.tv_so_dday.setTextColor(Color.parseColor("#FF0000")); //빨강

        } else {
            holder.tv_so_dday.setTextColor(-1979711488); //현재 기본 텍스트 색상
        }





    }

    @Override
    public int getItemCount() {
        return (null != arrayList ? arrayList.size() : 0);
    }

    //아이템 제거
    public void remove(int position){
        try {
            arrayList.remove(position);
            notifyItemRemoved(position);
        } catch (IndexOutOfBoundsException e){
            e.printStackTrace();
        }
    }

    //검색 기능시 활용. 어댑터의 아이템을 인자로 받은 아이템으로 바꾸고 notify~를 통해 recyclerView에게 데이터가 변했다고 알림.
    public void filterList(ArrayList<ItemData> list){
        arrayList = list;
        notifyDataSetChanged();
    }

    //아이템 클릭 리스너
    public void setOnItemClicklistener(OnItemClickListener listener){
        this.listener = listener;
    }

    @Override
    public void onItemClick(CustomViewHolder holder, View view, int position) {
        if(listener != null){
            listener.onItemClick(holder,view,position);
        }
    }


    public ItemData getItem(int position){
        return arrayList.get(position);
    }



    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected ImageView iv_profile;
        protected TextView tv_group,tv_name,tv_memo,tv_so_birth,tv_lu_birth,tv_so_dday,tv_lu_dday;
        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.iv_profile = (ImageView) itemView.findViewById(R.id.recycler_item_profile_iv);
            this.tv_group = (TextView) itemView.findViewById(R.id.recycler_item_group_tv);
            this.tv_name = (TextView) itemView.findViewById(R.id.recycler_item_name_tv);
            this.tv_so_birth = (TextView) itemView.findViewById(R.id.recycler_item_so_birth_tv);
            this.tv_lu_birth = (TextView) itemView.findViewById(R.id.recycler_item_lu_birth_tv);
            this.tv_memo = (TextView) itemView.findViewById(R.id.recycler_item_memo_tv);
            this.tv_so_dday = (TextView) itemView.findViewById(R.id.recycler_item_so_dday_tv);
            this.tv_lu_dday = (TextView) itemView.findViewById(R.id.recycler_item_lu_dday_tv);


            // 아이템 클릭 이벤트 처리.
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAbsoluteAdapterPosition() ;
                    if(listener != null){
                        listener.onItemClick(CustomViewHolder.this, v, position);
                    }
                }
            });




        }
    }
}
