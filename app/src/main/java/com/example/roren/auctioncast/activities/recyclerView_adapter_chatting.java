package com.example.roren.auctioncast.activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import com.example.roren.auctioncast.R;

public class recyclerView_adapter_chatting extends RecyclerView.Adapter<recyclerView_adapter_chatting.ItemViewHolder>{

    ArrayList<recyclerView_item_chatting> items;
    Context context;

    public recyclerView_adapter_chatting(Context context){
        items = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_item_chatting, viewGroup, false);
        return new recyclerView_adapter_chatting.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int i) {
        final int position = i;

        holder.id.setText(items.get(i).getId());
        holder.text.setText(items.get(i).getText());

    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(recyclerView_item_chatting item){
        items.add(item);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView id;
        private TextView text;

        public ItemViewHolder(View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.recyclerView_item_broadcasting_chatting_textView_id);
            text = itemView.findViewById(R.id.recyclerView_item_broadcasting_chatting_textView_text);
        }
    }
}
