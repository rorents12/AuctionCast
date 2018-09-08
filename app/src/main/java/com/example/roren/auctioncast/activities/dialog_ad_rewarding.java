package com.example.roren.auctioncast.activities;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.roren.auctioncast.R;
import com.example.roren.auctioncast.utility.utility_http_DBQuery;

public class dialog_ad_rewarding extends Dialog {


    Button button_auctionCash;
    Button button_auctionCoin;

    Context context;

    public dialog_ad_rewarding(final Context context){
        super(context);

        this.context = context;

        getWindow().setGravity(Gravity.BOTTOM);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.layout_ad_reward_dialog);

        button_auctionCash = findViewById(R.id.ad_reward_dialog_button_auctionCash);
        button_auctionCoin = findViewById(R.id.ad_reward_dialog_button_auctionCoin);

        button_auctionCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new utility_http_DBQuery().execute("update table_user_membership set cash=cash-(-100) where id = '" + activity_login.user_id +"';");

                Toast.makeText(context, "광고 시청을 완료하여 100 옥션캐시를 지급받았습니다.", Toast.LENGTH_LONG).show();

                dismiss();
            }
        });

        button_auctionCoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                dismiss();
            }
        });
    }

}
