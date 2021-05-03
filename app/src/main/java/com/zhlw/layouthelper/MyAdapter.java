package com.zhlw.layouthelper;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private List<CharSequence> innerList;
    private String[] nameList;
    private Context context;

    public MyAdapter(Context context) {
        this.context = context;
        nameList = context.getResources().getStringArray(R.array.array_nodeinfo_name);
        innerList = new ArrayList<>();
    }

    public MyAdapter(List<CharSequence> innerList, Context context) {
        this.innerList = innerList;
        this.context = context;
        nameList = context.getResources().getStringArray(R.array.array_nodeinfo_name);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rv_main_item, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (!innerList.isEmpty()){
            String finishText = nameList[position].concat(" "+propertyName(position));
            holder.content.setText(finishText);
        }
    }

    @Override
    public int getItemCount() {
        return innerList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView content;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.text_rvmain_content);
        }
    }

    public void setInnerList(List<CharSequence> innerList) {
        this.innerList = innerList;
    }

    private String propertyName(int position){
        if (innerList.isEmpty()) return "";
        if (innerList.get(position) == null) {
            return context.getResources().getString(R.string.no_this_attr);
        } else {
            return innerList.get(position).toString();
        }
    }

}
