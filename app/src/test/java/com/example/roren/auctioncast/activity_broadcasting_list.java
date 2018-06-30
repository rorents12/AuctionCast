//package com.example.roren.auctioncast;
//
//import android.content.Intent;
//import android.os.AsyncTask;
//import android.support.v4.widget.ImageViewCompat;
//import android.support.v4.widget.SwipeRefreshLayout;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.ArrayList;
//
//import Utility.utility_http_DBQuery;
//
///**
// * 방송 목록을 보여주는 list와 왼쪽에서 오른쪽으로 슬라이드 되는 Navigation View로 이루어진 Activity이다.
// *
// * Navigation View
// *      1. 현재 로그인된 사용자의 프로필 사진과 아이디를 상단에 보여준다.
// *      2. 방송하기 버튼을 통해 방송을 시작할 수 있다.
// *
// * 방송 목록 list
// *      RecyclerView를 통해 list를 보여준다. 사용자는 http 통신을 통해 서버의 방송목록 정보를 받아오고, 해당 정보를 parsing하여
// *      RecyclerView에 세팅한다.
// */
//
//public class activity_home extends AppCompatActivity implements View.OnClickListener {
//
//    Button button_start_broadcast;
//
//    TextView textView_id;
//
//    ImageView imageView_profile;
//
//    RecyclerView recyclerView_list_broadcasting;
//
//    recyclerView_adapter_castList recyclerView_adapter_castList;
//
//    SwipeRefreshLayout swipeRefreshLayout;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_home);
//
//        button_start_broadcast = findViewById(R.id.activity_broadcasting_list_button_start_broadcast);
//
//        button_start_broadcast.setOnClickListener(this);
//
//        textView_id = findViewById(R.id.activity_broadcasting_list_textView_id);
//        textView_id.setText(activity_login.user_id);
//
//        imageView_profile = findViewById(R.id.activity_broadcasting_list_imageView_profile);
//
//        recyclerView_list_broadcasting = findViewById(R.id.recyclerView_container_list_broadcasting);
//
//        swipeRefreshLayout = findViewById(R.id.activity_broadcasting_list_swipeRefreshLayout);
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                set_recyclerView_list_broadcasting();
//
//                swipeRefreshLayout.setRefreshing(false);
//            }
//        });
//
//        set_recyclerView_list_broadcasting();
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        set_recyclerView_list_broadcasting();
//    }
//
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()){
//            case R.id.activity_broadcasting_list_button_start_broadcast:
//                Intent intent = new Intent(this, activity_publishvideo.class);
//
//                intent.putExtra("streamer_id", activity_login.user_id);
//
//                startActivity(intent);
//
//        }
//    }
//
//    public void set_recyclerView_list_broadcasting(){
//        try {
//
//            recyclerView_list_broadcasting.setLayoutManager(new LinearLayoutManager(this));
//
////            JSONObject json = new utility_http_DBQuery().execute("select * from table_broadcasting_list").get();
//
//            ArrayList<recyclerView_item_castList> items = new ArrayList<>();
//
//            JSONArray json = new utility_http_DBQuery().execute("select * from table_broadcasting_list").get();
//
//            for(int i = 0; i < json.length(); i++){
//                JSONObject jsonObject = new JSONObject(json.get(i).toString());
//
//                recyclerView_item_castList item = new recyclerView_item_castList();
//
//                item.setId_broadcaster(jsonObject.getString("broadcaster_id").substring(0, jsonObject.getString("broadcaster_id").indexOf(" ")));
//                item.setNum_viewer(jsonObject.getString("viewer_num"));
//                item.setTitle_broadcasting(jsonObject.getString("title"));
//
//                items.add(item);
//            }
//
//            recyclerView_adapter_castList = new recyclerView_adapter_castList(items, this);
//
//            recyclerView_list_broadcasting.setAdapter(recyclerView_adapter_castList);
//
//            Log.e("어댑터 아이템 확인: " , Integer.toString(recyclerView_adapter_castList.getItemCount()));
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//        }
//
//    }
//
//}
