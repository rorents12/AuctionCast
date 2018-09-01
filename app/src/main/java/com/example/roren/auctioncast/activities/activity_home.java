package com.example.roren.auctioncast.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.roren.auctioncast.utility.utility_ether_connectToken;
import com.example.roren.auctioncast.utility.utility_global_variable;
import com.example.roren.auctioncast.utility.utility_http_DBQuery;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import com.example.roren.auctioncast.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * 방송 목록을 보여주는 recyclerView 와 왼쪽에서 오른쪽으로 슬라이드 되는 Navigation View 로 이루어진 Activity 이다.
 *
 * Navigation View
 *      1. 현재 로그인된 사용자의 프로필 사진과 아이디를 상단에 보여준다.
 *      2. 방송하기 버튼을 통해 방송을 시작할 수 있다.
 *      3. 옥션캐시 - 충전 버튼을 통해 옥션캐시를 충전할 수 있다.
 *      4. 지갑 버튼을 통해 옥션코인의 정보를 확인할 수 있다.
 *      5. 거래정보 버튼을 통해 자신의 거래내역을 확인할 수 있다.
 *
 * 방송 목록 list
 *      RecyclerView 를 통해 list 를 보여준다. 사용자는 http 통신을 통해 서버의 방송목록 정보를 받아오고, 해당 정보를 parsing 하여
 *      RecyclerView 에 세팅한다.
 */

public class activity_home extends AppCompatActivity implements View.OnClickListener {

    /**
     * activity 의 view 들을 위한 변수 선언
     */
    // 방송 시작 버튼
    Button button_start_broadcast;
    // 옥션캐시 충전 버튼
    Button button_charge_cash;
    // 이더리움 지갑 버튼
    Button button_auction_coin;
    // 거래정보 조회 버튼
    Button button_contractInfo;
    // 보물찾기 버튼
    Button button_treasureHunt;

    // 사용자 id 를 표시하기 위한 textView
    TextView textView_id;
    // 사용자의 잔여 옥션캐시를 표시하기 위한 textView
    TextView textView_cash;
    // 사용자의 신뢰도 점수를 표시하기 위한 textView
    TextView textView_reliability;

    // 사용자의 프로필 사진을 보여주는 imageView
    ImageView imageView_profile;

    // 현재 진행중인 방속 목록을 보여주기 위한 recyclerView 와 그 adapter
    RecyclerView recyclerView_list_broadcasting;
    recyclerView_adapter_castList recyclerView_adapter_castList;

    // 방송 목록을 swipe 를 통해 업데이트 하기 위한 swipeRefreshLayout. 이 layout 안에 방송 목록이 들어간다.
    SwipeRefreshLayout swipeRefreshLayout;

    /**
     *  액티비티 내에서 사용하기 위한 변수
     */

    // 옥션캐시 충전 시, 사용자가 충전할 금액을 선택하게 되는데, 사용자가 선택한 금액을 저장해두는 변수.
    // 해당 변수에 금액을 저장해두었다가, activity_kakaopay 로 intent 를 넘겨줄 때 해당 변수를 같이 보내준다.
    private int total_amount;

    // activity_home 의 context
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        context = this;

        button_start_broadcast = findViewById(R.id.activity_broadcasting_list_button_start_broadcast);
        button_charge_cash = findViewById(R.id.activity_broadcasting_list_button_chargeCash);
        button_auction_coin = findViewById(R.id.activity_broadcasting_list_button_chargeCoin);
        button_contractInfo = findViewById(R.id.activity_broadcasting_list_button_contract_information);
        button_treasureHunt = findViewById(R.id.activity_broadcasting_list_button_treasureHunt);

        button_start_broadcast.setOnClickListener(this);
        button_charge_cash.setOnClickListener(this);
        button_auction_coin.setOnClickListener(this);
        button_contractInfo.setOnClickListener(this);
        button_treasureHunt.setOnClickListener(this);

        textView_id = findViewById(R.id.activity_broadcasting_list_textView_id);
        textView_cash = findViewById(R.id.activity_broadcasting_list_textView_cash);
        textView_reliability = findViewById(R.id.activity_broadcasting_list_textView_reliability);

        textView_id.setText(activity_login.user_id);

        imageView_profile = findViewById(R.id.activity_broadcasting_list_imageView_profile);
        imageView_profile.setOnClickListener(this);

        recyclerView_list_broadcasting = findViewById(R.id.recyclerView_container_list_broadcasting);


        // swipeRefreshLayout 을 통해 위로 swipe 시 방송목록을 업데이트 하도록 함
        swipeRefreshLayout = findViewById(R.id.activity_broadcasting_list_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                set_recyclerView_list_broadcasting();

                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // 네비게이션 뷰의 회원개인정보를 세팅
        set_membership_info();

        // 방송목록을 위한 recyclerView 세팅
        set_recyclerView_list_broadcasting();

    }

    /**
     * 다른 액티비티로 넘어갔다가 이 액티비티로 오거나, 잠시 다른 앱을 사용하다가 다시 해당 액티비티로 돌아왔을 경우,
     * onResume callback method 를 통해 사용자의 개인정보와 방송목록을 업데이트하여 보여준다.
     */
    @Override
    protected void onResume() {
        super.onResume();
        set_membership_info();
        set_recyclerView_list_broadcasting();
    }

    /**
     * activity 의 버튼들의 클릭 이벤트를 처리해주는 method
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){

            /**
             * 방송시작 버튼
             */
            case R.id.activity_broadcasting_list_button_start_broadcast:
                // 방송 시작하기 버튼을 눌렀을 때, 현재 유저의 아이디를 intent에 포함시켜 publishVideo 액티비티를 실행한다.
                // activity_login.user_id 변수는 현재 유저의 아이디를 나타내는 static 변수이다.
                Intent intent = new Intent(this, activity_publishVideo.class);

                intent.putExtra("streamer_id", activity_login.user_id);

                startActivity(intent);

                break;

            /**
             * 캐시충전 버튼
             */
            case R.id.activity_broadcasting_list_button_chargeCash:
                //캐시충전 버튼을 누르면 다이얼로그로 5천원, 1만원, 5만원, 10만원의 금액을 선택하고 확인을 눌러 결제를 할 수 있도록 한다.
                final String[] items = new String[] {"5000", "10000", "50000", "100000"};

                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog  .setTitle("충전할 금액을 선택하세요.")
                        .setSingleChoiceItems(
                                items,
                                0,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // 선택한 금액을 total_amount 변수에 저장
                                        total_amount = i;
                                    }
                                }
                        )
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(context, activity_kakaopay.class);

                                // 충전할 금액 정보를 intent 에 포함시켜 전송
                                intent.putExtra("total_amount", items[total_amount]);
                                startActivity(intent);
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
                break;

            /**
             * 지갑 버튼
             */
            case R.id.activity_broadcasting_list_button_chargeCoin:
                // 지갑 정보 조회를 위해 activity_ethereum 으로 이동
                Intent i = new Intent(context, activity_ethereum.class);

                startActivity(i);
                break;

            /**
             * 거래정보 버튼
             */
            case R.id.activity_broadcasting_list_button_contract_information:
                // 거래 정보 조회를 위해 activity_contractInfo 로 이동
                Intent intent2 = new Intent(context, activity_contractInfo.class);

                startActivity(intent2);
                break;

            /**
             * 보물찾기 버튼
             */
            case R.id.activity_broadcasting_list_button_treasureHunt:
                // 보물찾기 activity 로 이동
                Intent intent3 = new Intent(context, activity_AR_treasureHunt.class);

                startActivity(intent3);
                break;

            /**
             * 프로필사진 터치 시 이벤트(프로필사진 변경)
             */
            case R.id.activity_broadcasting_list_imageView_profile:
                // 프로필사진 촬영 activity 로 이동
                Intent intent4 = new Intent(context, activity_faceDetection.class);

                startActivity(intent4);
                break;
        }
    }

    /**
     * 방송목록 recyclerView 를 세팅하는 method
     */
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

                item.setId_broadcaster(jsonObject.getString("broadcaster_id"));
                item.setNum_viewer(jsonObject.getString("viewer_num"));
                item.setTitle_broadcasting(jsonObject.getString("title"));
                item.setIdentity_num(jsonObject.getString("identity_num"));

                items.add(item);
            }

            // recyclerView를 위한 adapter
            recyclerView_adapter_castList = new recyclerView_adapter_castList(items, this);

            // 어댑터 설정
            recyclerView_list_broadcasting.setAdapter(recyclerView_adapter_castList);

        } catch (Exception e) {

            e.printStackTrace();
        }

    }


    /**
     * 회원의 회원정보를 HTTP 통신을 통해 받아온 후 Navigation View 에 세팅해주는 메소드
     */
    public void set_membership_info(){
        try{
            // 자신의 회원정보 조회
            JSONArray json = new utility_http_DBQuery()
                    .execute("select * from table_user_membership where id='"
                            + activity_login.user_id
                            + "';")
                    .get();
            JSONObject jsonObject = json.getJSONObject(0);

            // 프로필 사진 세팅
            RequestOptions profileOptions = new RequestOptions()
                    .circleCropTransform()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true);

            if(!jsonObject.getString("profile_path").equals("null")){
                utility_global_variable.PROFILE_IMAGE_PATH = jsonObject.getString("profile_path");
                Glide
                        .with(this)
                        .load(jsonObject.getString("profile_path"))
                        .apply(profileOptions)
                        .into(imageView_profile);
            }else{
                Glide
                        .with(this)
                        .load(utility_global_variable.PROFILE_DEFAULT_IMAGE_PATH)
                        .apply(profileOptions)
                        .into(imageView_profile);
            }

            // 캐시 잔액 세팅
            textView_cash.setText(jsonObject.getString("cash") + " 원");

            // 신뢰도 점수 세팅
            textView_reliability.setText(jsonObject.getString("reliability"));

            // 자신의 지갑주소를 global_variable 에 저장
            utility_global_variable.WALLET_ADDRESS = jsonObject.getString("wallet_path");
            utility_global_variable.WALLET_FILE_ADDRESS = jsonObject.getString("wallet_filepath");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
