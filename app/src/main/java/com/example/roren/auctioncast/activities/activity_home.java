package com.example.roren.auctioncast.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.roren.auctioncast.utility.utility_http_DBQuery;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import com.example.roren.auctioncast.R;

/**
 * 방송 목록을 보여주는 list와 왼쪽에서 오른쪽으로 슬라이드 되는 Navigation View로 이루어진 Activity이다.
 *
 * Navigation View
 *      1. 현재 로그인된 사용자의 프로필 사진과 아이디를 상단에 보여준다.
 *      2. 방송하기 버튼을 통해 방송을 시작할 수 있다.
 *
 * 방송 목록 list
 *      RecyclerView를 통해 list를 보여준다. 사용자는 http 통신을 통해 서버의 방송목록 정보를 받아오고, 해당 정보를 parsing하여
 *      RecyclerView에 세팅한다.
 */

public class activity_home extends AppCompatActivity implements View.OnClickListener {

    // 액티비티를 구성하는 View들을 위한 변수 선언

    Button button_start_broadcast;

    TextView textView_id;

    ImageView imageView_profile;

    RecyclerView recyclerView_list_broadcasting;

    recyclerView_adapter_castList recyclerView_adapter_castList;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        button_start_broadcast = findViewById(R.id.activity_broadcasting_list_button_start_broadcast);

        button_start_broadcast.setOnClickListener(this);

        textView_id = findViewById(R.id.activity_broadcasting_list_textView_id);
        textView_id.setText(activity_login.user_id);

        imageView_profile = findViewById(R.id.activity_broadcasting_list_imageView_profile);

        recyclerView_list_broadcasting = findViewById(R.id.recyclerView_container_list_broadcasting);

        // swipeRefreshLayout을 통해 위로 swipe 시 castList를 업데이트 하도록 함

        swipeRefreshLayout = findViewById(R.id.activity_broadcasting_list_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                set_recyclerView_list_broadcasting();

                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // castList를 위한 recyclerView 세팅
        set_recyclerView_list_broadcasting();

    }

    @Override
    protected void onResume() {
        super.onResume();
        set_recyclerView_list_broadcasting();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.activity_broadcasting_list_button_start_broadcast:
                // 방송 시작하기 버튼을 눌렀을 때, 현재 유저의 아이디를 intent에 포함시켜 publishVideo 액티비티를 실행한다.
                // activity_login.user_id 변수는 현재 유저의 아이디를 나타내는 static 변수이다.
                Intent intent = new Intent(this, activity_publishVideo.class);

                intent.putExtra("streamer_id", activity_login.user_id);

                startActivity(intent);

        }
    }

    // castList를 나타내기 위한 recyclerView를 세팅하는 메소드

    public void set_recyclerView_list_broadcasting(){
        try {

            recyclerView_list_broadcasting.setLayoutManager(new LinearLayoutManager(this));

            // recyclerView의 item을 담을 ArrayList
            ArrayList<recyclerView_item_castList> items = new ArrayList<>();

            // 서버의 DB로부터 방송목록에 대한 정보를 받아와 JSONArray 형태로 저장
            JSONArray json = new utility_http_DBQuery().execute("select * from table_broadcasting_list").get();

            // 방송 목록에 대한 정보를 parsing해서 각 방송정보를 하나의 아이템에 저장한 후, ArrayList에 추가
            for(int i = 0; i < json.length(); i++){
                JSONObject jsonObject = new JSONObject(json.get(i).toString());

                recyclerView_item_castList item = new recyclerView_item_castList();

                item.setId_broadcaster(jsonObject.getString("broadcaster_id").substring(0, jsonObject.getString("broadcaster_id").indexOf(" ")));
                item.setNum_viewer(jsonObject.getString("viewer_num"));
                item.setTitle_broadcasting(jsonObject.getString("title"));

                items.add(item);
            }

            // recyclerView를 위한 adapter
            recyclerView_adapter_castList = new recyclerView_adapter_castList(items, this);

            // 어댑터 설정
            recyclerView_list_broadcasting.setAdapter(recyclerView_adapter_castList);

            Log.e("어댑터 아이템 확인: " , Integer.toString(recyclerView_adapter_castList.getItemCount()));

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

}
