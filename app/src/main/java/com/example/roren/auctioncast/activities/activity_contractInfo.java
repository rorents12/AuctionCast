package com.example.roren.auctioncast.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.roren.auctioncast.R;
import com.example.roren.auctioncast.utility.utility_http_DBQuery;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 *  이 액티비티는 사용자가 구매, 또는 판매한 상품에 대한 정보를  recyclerView 를 통해 보여준다.
 */

public class activity_contractInfo extends AppCompatActivity {

    /**
     *  recyclerView 와 adapter 변수 선언
     */
    RecyclerView recyclerView_contractInfo;

    recyclerView_adapter_contractInfo recyclerView_adapter_contractInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contractinfo);

        recyclerView_contractInfo = findViewById(R.id.activity_contracInfo_recyclerView);

        set_recyclerView_contractInfo();
    }

    /**
     *  recyclerView 에 사용자의 거래정보를 세팅하는 method.
     *
     *  하나의 recyclerView 에 판매 내역과 구매 내역을 구분하여 넣기 위해서, recyclerView 에 item 을 삽입할 때
     *  판매에 관한 item 을 먼저 모두 삽입한 뒤, 구매에 관한 item 을 삽입한다.
     */
    public void set_recyclerView_contractInfo(){
        try {

            recyclerView_contractInfo.setLayoutManager(new LinearLayoutManager(this));

            // recyclerView의 item을 담을 ArrayList
            ArrayList<recyclerView_item_contractInfo> items = new ArrayList<>();

            // 판매 구분자 삽입(아래의 item 들이 판매내역임을 보여준다)
            recyclerView_item_contractInfo item_sellFence = new recyclerView_item_contractInfo();
            item_sellFence.setSellFence("sellFence");
            items.add(item_sellFence);

            // 서버의 DB로부터 거래목록(사용자의 판매정보)에 대한 정보를 받아와 JSONArray 형태로 저장
            JSONArray jsonSell = new utility_http_DBQuery().execute(
                    "select * from table_contract_information where seller = '" + activity_login.user_id +
                            "' and complete = 'N'").get();

            // 판매 정보를 각각의 recyclerView item에 assign
            for(int i = 0; i < jsonSell.length(); i++){
                JSONObject jsonObject = new JSONObject(jsonSell.get(i).toString());

                recyclerView_item_contractInfo item = new recyclerView_item_contractInfo();

                item.setBuyer_id(jsonObject.getString("buyer"));
                item.setBuyPrice(jsonObject.getString("price"));
                item.setSellProduct(jsonObject.getString("title"));
                item.setContractNum(jsonObject.getString("pk"));

                items.add(item);
            }

            // 구매 구분자 삽입(아래의 item 들이 구매내역임을 보여준다)
            recyclerView_item_contractInfo item_buyFence = new recyclerView_item_contractInfo();
            item_buyFence.setBuyFence("buyFence");
            items.add(item_buyFence);

            // 서버의 DB로부터 거래목록(사용자의 구매정보)에 대한 정보를 받아와 JSONArray 형태로 저장
            JSONArray jsonBuy = new utility_http_DBQuery().execute(
                    "select * from table_contract_information where buyer = '" + activity_login.user_id +
                            "' and complete = 'N'").get();

            // 구매 정보를 각각의 recyclerView item에 assign
            for(int i = 0; i < jsonBuy.length(); i++){
                JSONObject jsonObject = new JSONObject(jsonBuy.get(i).toString());

                recyclerView_item_contractInfo item = new recyclerView_item_contractInfo();

                item.setSeller_id(jsonObject.getString("seller"));
                item.setSellPrice(jsonObject.getString("price"));
                item.setBuyProduct(jsonObject.getString("title"));
                item.setContractNum(jsonObject.getString("pk"));

                items.add(item);
            }

            // recyclerView를 위한 adapter
            recyclerView_adapter_contractInfo = new recyclerView_adapter_contractInfo(items, this);

            // 어댑터 설정
            recyclerView_contractInfo.setAdapter(recyclerView_adapter_contractInfo);

            Log.e("어댑터 아이템 확인: " , Integer.toString(recyclerView_adapter_contractInfo.getItemCount()));

        } catch (Exception e) {

            e.printStackTrace();
        }

    }
}
