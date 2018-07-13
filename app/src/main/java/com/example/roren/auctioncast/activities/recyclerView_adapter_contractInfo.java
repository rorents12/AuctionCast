package com.example.roren.auctioncast.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roren.auctioncast.R;
import com.example.roren.auctioncast.utility.utility_ether_connectToken;
import com.example.roren.auctioncast.utility.utility_global_variable;
import com.example.roren.auctioncast.utility.utility_http_DBQuery;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;

public class recyclerView_adapter_contractInfo extends RecyclerView.Adapter<recyclerView_adapter_contractInfo.ItemViewHolder>{

    ArrayList<recyclerView_item_contractInfo> items;
    Context context;

    String pay_method = "";

    public recyclerView_adapter_contractInfo(ArrayList<recyclerView_item_contractInfo> items, Context context){
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_item_contractinfo, viewGroup, false);
        return new recyclerView_adapter_contractInfo.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int i) {
        final recyclerView_item_contractInfo item = items.get(i);

        if(item.getBuyFence() != null){
            Log.e("판매 펜스!", "판매 펜스 세우기");
            holder.sellFence.setVisibility(View.GONE);
            holder.buyInfo.setVisibility(View.GONE);
            holder.sellInfo.setVisibility(View.GONE);
        }else if(item.getSellFence() != null){
            Log.e("구매 펜스!", "구매 펜스 세우기");
            holder.buyFence.setVisibility(View.GONE);
            holder.buyInfo.setVisibility(View.GONE);
            holder.sellInfo.setVisibility(View.GONE);
        }else if(item.getSeller_id() != null){
            Log.e("구매 목록!", "구매목록 채우기");
            holder.sellFence.setVisibility(View.GONE);
            holder.buyFence.setVisibility(View.GONE);
            holder.sellInfo.setVisibility(View.GONE);

            holder.buyProduct.setText("방송제목 - " + item.getBuyProduct());
            holder.seller_id.setText("판매자 - " + item.getSeller_id());
            holder.sellPrice.setText("낙찰가 - " + item.getSellPrice());

            // 구매 목록 아이템을 터치했을 때, 바로 낙찰금액을 판매자에게 송금할 수 있도록 clickListener 등록
            holder.buyInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    // 옥션 캐시를 이용하여 결제할 것인지, 옥션 코인을 이용하여 결제할 것인지 선택한다.
                    final String[] items = new String[] {
                            "옥션캐시 - " + item.getSellPrice() + "원",
                            "옥션코인 - " + String.valueOf(Integer.parseInt(item.getSellPrice())/1000) + "개"};

//                    final ProgressBar progressBar = new ProgressBar(context);
//                    progressBar.setVisibility(View.GONE);

                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog  .setTitle("송금할 화폐를 선택하세요.")

                            .setSingleChoiceItems(
                                    items,
                                    -1,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            pay_method = items[i].substring(0,4);
                                            Log.e("dfadsf", pay_method);
                                        }
                                    }
                            )
//                            .setView(progressBar)

                            .setPositiveButton("송금", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if(pay_method.equals("")){
                                        Toast.makeText(
                                                context,
                                                "송금할 화폐를 선택하세요.",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                    }else if(pay_method.equals("옥션캐시")){
                                        // 옥션캐시 전송
                                        try{
                                            send_auctionCash(
                                                    activity_login.user_id,
                                                    item.getSeller_id(),
                                                    item.getSellPrice(),
                                                    item.getContractNum()
                                            );
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }

                                    }else{
                                        // 옥션코인 전송
                                        try{
//                                            progressBar.setVisibility(View.VISIBLE);
                                            send_auctionCoin(
                                                    activity_login.user_id,
                                                    item.getSeller_id(),
                                                    String.valueOf(Integer.parseInt(item.getSellPrice())/1000),
                                                    item.getContractNum()
                                            );
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
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
                }
            });

        }else{
            Log.e("판매 목록!", "판매목록 채우기");
            holder.sellFence.setVisibility(View.GONE);
            holder.buyFence.setVisibility(View.GONE);
            holder.buyInfo.setVisibility(View.GONE);

            holder.sellProduct.setText("방송제목 - " + item.getSellProduct());
            holder.buyer_id.setText("구매자 - " + item.getBuyer_id());
            holder.buyPrice.setText("낙찰가 - " + item.getBuyPrice());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView seller_id;
        private TextView sellPrice;
        private TextView buyer_id;
        private TextView buyPrice;
        private TextView sellProduct;
        private TextView buyProduct;

        private LinearLayout sellInfo;
        private LinearLayout buyInfo;
        private LinearLayout sellFence;
        private LinearLayout buyFence;

        public ItemViewHolder(View itemView) {
            super(itemView);
            seller_id = itemView.findViewById(R.id.recyclerView_item_contractInfo_textView_seller);
            sellPrice = itemView.findViewById(R.id.recyclerView_item_contractInfo_textView_sellPrice);
            buyer_id = itemView.findViewById(R.id.recyclerView_item_contractInfo_textView_buyer);
            buyPrice = itemView.findViewById(R.id.recyclerView_item_contractInfo_textView_buyPrice);
            sellProduct = itemView.findViewById(R.id.recyclerView_item_contractInfo_textView_sellProduct);
            buyProduct = itemView.findViewById(R.id.recyclerView_item_contractInfo_textView_buyProduct);

            sellInfo = itemView.findViewById(R.id.recyclerView_item_contractInfo_linearLayout_sellInfo);
            buyInfo = itemView.findViewById(R.id.recyclerView_item_contractInfo_linearLayout_buyInfo);
            sellFence = itemView.findViewById(R.id.recyclerView_item_contractInfo_linearLayout_sellFence);
            buyFence = itemView.findViewById(R.id.recyclerView_item_contractInfo_linearLayout_buyFence);
        }
    }

    /**
     *  옥션캐시 송금 메소드
     */
    public void send_auctionCash(String from, String to, String amount, String contractNum) throws Exception{

        // 서버 DB에 저장되어 있는 사용자의 캐시정보를 받아온다.
        JSONArray json = new utility_http_DBQuery().execute("select * from table_user_membership where id='" + from + "';").get();

        JSONObject jsonObject = json.getJSONObject(0);

        // 사용자의 옥션캐시가 부족하면 송금이 완료되지 않는다.
        if(Integer.parseInt(jsonObject.getString("cash")) < Integer.parseInt(amount)){
            Toast.makeText(
                    context,
                    "옥션캐시가 부족합니다.\n 현재 보유 옥션캐시 - " + jsonObject.getString("cash") + " 원",
                    Toast.LENGTH_SHORT).show();
        }else{

            // 송금 처리
            new utility_http_DBQuery().execute(
                    "update table_user_membership set cash = case id when '" + from + "' then cash-" + amount +
                            " when '" + to + "' then cash+" + amount + " end where id in ('" + from + "','" + to +"');").get();

            // 거래 완료 여부 'Y'로 변경
            new utility_http_DBQuery().execute(
                    "update tabe_contract_information set complete = 'Y' where pk =" + contractNum + ";").get();

            Toast.makeText(context, "송금을 완료했습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     *  옥션코인 송금 메소드
     */
    public void send_auctionCoin(String from, String to, String amount, String contractNum) throws Exception{

        String walletAddress_mine = utility_global_variable.WALLET_ADDRESS;
        String walletFileAddress_mine = utility_global_variable.WALLET_FILE_ADDRESS;
        String walletAddress_to;


        Log.e("분기 2", "");

        JSONArray json = new utility_http_DBQuery().execute("select * from table_user_membership where id='" + to + "';").get();

        JSONObject jsonObject = json.getJSONObject(0);

        walletAddress_to = jsonObject.getString("wallet_path");

        Log.e("보낼 지갑 주소: ", walletAddress_to);

        String balance = new utility_ether_connectToken(
                utility_global_variable.CODE_ETHER_GET_BALANCE,
                walletAddress_mine,
                walletFileAddress_mine).execute().get();

        Log.e("잔액조회 결과: ", balance);


        // 사용자의 보유 옥션코인이 모자라면 전송되지 않는다.
        if(Integer.parseInt(balance) < Integer.parseInt(amount)) {
            Log.e("분기 1", "");
            Toast.makeText(
                    context,
                    "옥션코인이 부족합니다.\n 현재 보유 옥션코인 - " + balance + " 개",
                    Toast.LENGTH_SHORT).show();
        }else{
            String result = new utility_ether_connectToken(
                    utility_global_variable.CODE_ETHER_SEND_TOKEN,
                    walletAddress_mine,
                    walletFileAddress_mine,
                    walletAddress_to,
                    amount).execute().get();

            Log.e("결과값", result);

            // 거래 완료 여부 'Y'로 변경
            new utility_http_DBQuery().execute(
                    "update table_contract_information set complete = 'Y' where pk =" + contractNum + ";").get();

            Toast.makeText(context, "송금을 완료했습니다.", Toast.LENGTH_SHORT).show();
        }

    }
}
