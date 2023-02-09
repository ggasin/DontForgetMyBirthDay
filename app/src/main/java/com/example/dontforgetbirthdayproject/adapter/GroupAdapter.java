package com.example.dontforgetbirthdayproject.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dontforgetbirthdayproject.GetGroupPositionListener;
import com.example.dontforgetbirthdayproject.data.GroupData;
import com.example.dontforgetbirthdayproject.R;

import java.util.ArrayList;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.CustomViewHolder> implements GetGroupPositionListener {

    private Context context;
    private ArrayList<GroupData> arrayList;
    GetGroupPositionListener listener;
    private int selectedItemPosition = -1;
    boolean firstBind = true;


    public GroupAdapter(Context context,ArrayList<GroupData> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }


    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_recycler_item,parent,false);
        CustomViewHolder holder = new CustomViewHolder(view);



        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        holder.tv_group.setText(arrayList.get(position).getGroup());
        holder.itemView.setTag(position);
        Log.d("포지션",String.valueOf(position));
        if(selectedItemPosition==position){
            holder.groupLayout.setBackgroundResource(R.drawable.group_item_selected);
            getGroupPosition(position);
        } else {
            holder.groupLayout.setBackgroundResource(R.drawable.group_item_not_selected);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedItemPosition = holder.getBindingAdapterPosition();
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != arrayList ? arrayList.size() : 0);
    }
    public void remove(int position){
        try {
            arrayList.remove(position);
            notifyItemRemoved(position);
        } catch (IndexOutOfBoundsException e){
            e.printStackTrace();
        }
    }


    public void setOnGroupClicklistener(GetGroupPositionListener listener){
        this.listener = listener;
    }
    @Override
    public void getGroupPosition(int position) {
        if(listener != null){
            listener.getGroupPosition(position);
        }
    }

    public GroupData getItem(int position){
        return arrayList.get(position);
    }



    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView tv_group;
        public LinearLayout groupLayout;
        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            this.tv_group = (TextView)itemView.findViewById(R.id.group_recycler_group);
            this.groupLayout = (LinearLayout) itemView.findViewById(R.id.group_recycler_ly);

        }
    }
}

