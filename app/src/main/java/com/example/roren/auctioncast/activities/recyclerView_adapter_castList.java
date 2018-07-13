package com.example.roren.auctioncast.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.roren.auctioncast.utility.utility_http_DBQuery;

import java.util.ArrayList;

import com.example.roren.auctioncast.R;

public class recyclerView_adapter_castList extends RecyclerView.Adapter<recyclerView_adapter_castList.ItemViewHolder>{

    ArrayList<recyclerView_item_castList> items;
    Context context;

    public recyclerView_adapter_castList(ArrayList<recyclerView_item_castList> items, Context context){
        this.context = context;
        this.items = items;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_item_castlist, viewGroup, false);
        return new ItemViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int i) {
        final int position = i;

        holder.thumbnail.setImageDrawable(activity_login.global_context.getResources().getDrawable(R.drawable.profile));

        holder.title_broadcasting.setText(items.get(i).getTitle_broadcasting());
        holder.id_broadcaster.setText("BJ " + items.get(i).getId_broadcaster());
        holder.num_viewer.setText(items.get(i).getNum_viewer() + "명 시청중");

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, activity_playVideo.class);

                intent.putExtra("streamer_id", items.get(position).getId_broadcaster());
                intent.putExtra("title", items.get(position).getTitle_broadcasting());

                new utility_http_DBQuery().execute("update table_broadcasting_list set viewer_num=viewer_num-(-1) where broadcaster_id = '" + items.get(position).getId_broadcaster() + "'");
//                new utility_http_DBQuery().execute("update table_broadcasting_list set viewer_num=viewer_num+1 where broadcaster_id = 'nova'");
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public static class ItemViewHolder extends RecyclerView.ViewHolder{
        private ImageView thumbnail;
        private TextView title_broadcasting;
        private TextView id_broadcaster;
        private TextView num_viewer;
        private LinearLayout container;

        public ItemViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.recyclerView_item_broadcasting_list_imageView_thumbnail);
            title_broadcasting = itemView.findViewById(R.id.recyclerView_item_broadcasting_list_textView_title_broadcast);
            id_broadcaster = itemView.findViewById(R.id.recyclerView_item_broadcasting_list_textView_id_broadcaster);
            num_viewer = itemView.findViewById(R.id.recyclerView_item_broadcasting_list_textView_num_viewer);
            container = itemView.findViewById(R.id.recyclerView_item_broadcasting_list_linearLayout_container);
        }
    }

}
