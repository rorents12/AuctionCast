package com.example.roren.auctioncast.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import com.example.roren.auctioncast.R;
import com.example.roren.auctioncast.utility.utility_global_variable;
import com.example.roren.auctioncast.utility.utility_http_DBQuery;

/**
 * 방송 화면에서 채팅을 출력하기 위한 recyclerView 에 setting 하는 adapter 이다.
 *
 * 채팅을 보낸 사용자의 id 와 채팅 내용을 item 에 담아 onBindViewHolder method 를 통해 view 에 setting 한다.
 * 또한 item 에 LongClickListener 를 setting 하여 채팅 메시지를 롱클릭 할 경우 해당 채팅을 보낸 사용자를 신고할 수 있다.
 */

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
        final String timeStamp = items.get(i).getTimeStamp();
        final String id = items.get(i).getId();
        final String roomCode = items.get(i).getRoomCode();

        holder.id.setText(id);
        holder.text.setText(items.get(i).getText());

        // 해당 채팅 메시지가 다른 사용자가 보낸 메시지일 때, 롱 터치 시 해당 사용자를 신고할 수 있다.
        if(!items.get(i).getId().equals(activity_login.user_id)){
            holder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog  .setTitle("사용자 신고")
                            .setMessage("해당 채팅을 신고하시겠습니까?")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // 채팅 신고 접수
                                    new utility_http_DBQuery()
                                            .execute("insert into table_user_reporting (user_reporting, user_reported, roomCode, timeStamp) values('"
                                                    + activity_login.user_id
                                                    + "','"
                                                    + id
                                                    + "','"
                                                    + roomCode
                                                    + "','"
                                                    + timeStamp
                                                    + "');");

                                    Toast.makeText(context, "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNeutralButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });

                    dialog.create();
                    dialog.show();

                    return false;
                }
            });
        }

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
        private LinearLayout container;

        public ItemViewHolder(View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.recyclerView_item_broadcasting_chatting_textView_id);
            text = itemView.findViewById(R.id.recyclerView_item_broadcasting_chatting_textView_text);
            container = itemView.findViewById(R.id.recyclerView_item_broadcasting_chatting_container);
        }
    }
}
