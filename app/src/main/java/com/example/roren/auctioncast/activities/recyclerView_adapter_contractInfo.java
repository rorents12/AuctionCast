package com.example.roren.auctioncast.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roren.auctioncast.R;
import com.example.roren.auctioncast.utility.utility_ether_connectToken;
import com.example.roren.auctioncast.utility.utility_global_variable;
import com.example.roren.auctioncast.utility.utility_http_DBQuery;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 *  activity_contractInfo 에서 거래내역을 보여주기 위한 recyclerView 에 적용되는 recyclerView Adapter 이다.
 *
 *  item 하나의 layout 에 판매 구분자, 구매 구분자, 판매 내역, 구매 내역을 위한 View 들이 모두 함께 있다.
 *
 *  1. 판매 구분자와 구매 구분자
 *  판매 구분자와 구매 구분자는 판매 내역과 구매 내역들의 최상단에 삽입하는 item 이다.
 *  이을 처리하기 위하여 item 의 buyFence / sellFence 변수가 null 인지 체크하고, null 이 아니라면 해당 구분자의 View 들만 보이도록 처리한다.
 *
 *  2. 판매 내역과 구매 내역의 구분
 *  item 하나의 layout 에 구매 정보를 위한 View 와 판매 정보를 위한 View 가 함께 있고,
 *  item 의 seller_id 변수가 null 이 아니라면 구매 정보이므로, 판매에 관한 View 들의 visibility 를  gone 으로 처리한다.
 *  만약 item 의 buyer_id 변수가 null 이 아니라면 판매 정보이므로, 위와 반대로 처리한다.
 *
 *  3. 구매 내역 item 을 클릭 이벤트 발생 시 송금 기능
 *  구매 내역을 터치하여 판매자에게 낙찰금액을 송금할 수 있다.
 *  송금은 내부 화폐인 옥션캐시 또는 이더리움 토큰인 옥션코인을 이용하여 진행할 수 있다.
 *
 */

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

    /**
     *
     * 이 method 를 통해 판매구분자와 구매구분자, 판매내역과 구매내역을 구분하여 각 item 에 보여준다.
     * 또한 구매 내역 item 에 onClickListener 를 통하여 송금기능을 구현하였다.
     *
     * @param holder
     *      recyclerView item 의 layout
     * @param i
     *      현재 선택된 recyclerView item 의 index(recyclerView 에서 몇번째 아이템인지를 나타냄)
     */
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int i) {
        final recyclerView_item_contractInfo item = items.get(i);

        if(item.getBuyFence() != null){
            // 판매 구분자 삽입
            holder.sellFence.setVisibility(View.GONE);
            holder.buyInfo.setVisibility(View.GONE);
            holder.sellInfo.setVisibility(View.GONE);
        }else if(item.getSellFence() != null){
            // 구매 구분자 삽입
            holder.buyFence.setVisibility(View.GONE);
            holder.buyInfo.setVisibility(View.GONE);
            holder.sellInfo.setVisibility(View.GONE);
        }else if(item.getSeller_id() != null){
            // 구매 정보 표시
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
            // 판매 정보 표시
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

    /**
     *  contractInfo recyclerView 의 item layout 이 가지고 있는 view 들을 변수와 연결해주는 class
     */
    public static class ItemViewHolder extends RecyclerView.ViewHolder{

        // seller_id, buyPrice, buyProduct 는 각각 판매자 이름, 구매가격, 구매상품을 뜻한다.
        // 이들은 구매정보를 표시할 때 사용되는 view 이다.
        private TextView seller_id;
        private TextView buyPrice;
        private TextView buyProduct;

        // buyer_id, sellPrice, sellProduct 는 각가 구매자 이름, 판매가격, 판매상품을 뜻한다.
        // 이들은 판매정보를 표시할 때 사용되는 view 이다.
        private TextView sellPrice;
        private TextView buyer_id;
        private TextView sellProduct;

        // sellInfo, buyInfo 는 위의 판매 정보, 구매 정보를 표시할 view 들을 담고 있는 LinearLayout 이다.
        private LinearLayout sellInfo;
        private LinearLayout buyInfo;

        // sellFence, buyFence 는 각각 구매 구분자, 판매 구분자를 나타내는 LinearLayout 이다.
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
     * 필요한 parameter 들을 받아서 서버 DB에 query 를 보내 옥션 캐시를 이용한 송금처리를 구현한 method.
     *
     * @param from
     *      옥션캐시를 이용하여 송금을 하는 구매자 id.
     * @param to
     *      옥션캐시를 받을 판매자 id.
     * @param amount
     *      보낼 옥션캐시의 양(낙찰가)
     * @param contractNum
     *      진행되고 있는 거래의 고유번호
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
                            " when '" + to + "' then cash-(-" + amount + ") end where id in ('" + from + "','" + to +"');"
            ).get();

            // 거래 완료 여부 'Y'로 변경
            new utility_http_DBQuery().execute(
                    "update table_contract_information set complete = 'Y' where pk =" + contractNum + ";"
            ).get();

            Toast.makeText(context, "송금을 완료했습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 필요한 parameter 들을 받아서 ethereum 통신을 통해 토큰을 전송해주는 method
     * parameter 로 코인을 전송받을 id 를 받아오고, method 내부에서 해당 id 를 통해 서버 DB 에 저장되어 있는
     * 상대방의 이더리움 지갑 주소를 받아온다.
     *
     * @param from
     *      코인을 보낼 사람의 id
     * @param to
     *      코인을 받을 사람의 id
     * @param amount
     *      보낼 코인의 갯수(낙찰가)
     * @param contractNum
     *      진행되고 있는 거래의 고유번호
     */
    public void send_auctionCoin(String from, String to, String amount, String contractNum) throws Exception{

        // 내 지갑주소 가져오기
        String walletAddress_mine = utility_global_variable.WALLET_ADDRESS;
        String walletFileAddress_mine = utility_global_variable.WALLET_FILE_ADDRESS;
        String walletAddress_to;

        // 코인을 받을사람의 지갑주소 서버 DB 에서 가져오기.
        JSONArray json = new utility_http_DBQuery().execute("select * from table_user_membership where id='" + to + "';").get();
        JSONObject jsonObject = json.getJSONObject(0);
        walletAddress_to = jsonObject.getString("wallet_path");

        // 코인을 보내기 전, 내 지갑의 코인 잔액을 조회
        String balance = new utility_ether_connectToken(
                utility_global_variable.CODE_ETHER_GET_BALANCE,
                walletAddress_mine,
                walletFileAddress_mine).execute().get();

        // 사용자의 보유 옥션코인이 모자라면 전송되지 않는다.
        if(Integer.parseInt(balance) < Integer.parseInt(amount)) {
            Log.e("분기 1", "");
            Toast.makeText(
                    context,
                    "옥션코인이 부족합니다.\n 현재 보유 옥션코인 - " + balance + " 개",
                    Toast.LENGTH_SHORT).show();
        }else{
            // 옥션코인 전송
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
