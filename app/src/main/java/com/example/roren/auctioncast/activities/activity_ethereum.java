package com.example.roren.auctioncast.activities;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.roren.auctioncast.R;
import com.example.roren.auctioncast.utility.AuctionCastToken_sol_AuctionCastToken;
import com.example.roren.auctioncast.utility.utility_ether_connectToken;
import com.example.roren.auctioncast.utility.utility_global_variable;
import com.example.roren.auctioncast.utility.utility_http_DBQuery;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.crypto.ContractUtils;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.AdminFactory;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class activity_ethereum extends AppCompatActivity implements View.OnClickListener{

    Button button_createWallet;

    TextView textView_wallet, textView_walletAddress, textView_auctionCoin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ethereum);

        button_createWallet = findViewById(R.id.activity_ethereum_button_createWallet);
        button_createWallet.setOnClickListener(this);

        textView_wallet = findViewById(R.id.activity_ethereum_textView_wallet);
        textView_walletAddress = findViewById(R.id.activity_ethereum_textView_walletAddress);
        textView_auctionCoin = findViewById(R.id.activity_ethereum_textView_auctionCoin);

//        // 서버 DB에서 계정의 지갑 정보를 불러온다.
//        try{
//            JSONArray json = new utility_http_DBQuery().execute("select * from table_user_membership where id = '" + activity_login.user_id + "';").get();
//            JSONObject j = json.getJSONObject(0);
//
//            walletAddress = j.getString("wallet_path");
//            walletFileAddress = j.getString("wallet_filepath");
//        }catch (Exception e){
//        }

        // 지갑을 가지고 있으면 지갑 정보를, 지갑을 가지고 있지 않으면 지갑 생성 버튼을 보여준다.
        Log.e("fsadfs", utility_global_variable.WALLET_ADDRESS);

        if(utility_global_variable.WALLET_ADDRESS.equals("null")){
            textView_auctionCoin.setVisibility(View.INVISIBLE);
            textView_walletAddress.setVisibility(View.GONE);
            textView_wallet.setVisibility(View.GONE);
        }else{
            button_createWallet.setVisibility(View.GONE);
            textView_walletAddress.setText(utility_global_variable.WALLET_ADDRESS);
            try{
                String coin = new utility_ether_connectToken(
                        utility_global_variable.CODE_ETHER_GET_BALANCE,
                        utility_global_variable.WALLET_ADDRESS,
                        utility_global_variable.WALLET_FILE_ADDRESS
                ).execute().get();
                textView_auctionCoin.setText("보유 옥션코인 - " + coin + " 개");
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.activity_ethereum_button_createWallet:
                // 지갑 생성 버튼을 눌렀을 때
                AlertDialog.Builder dialog = new AlertDialog.Builder(activity_ethereum.this);
                final EditText et = new EditText(activity_ethereum.this);
                dialog  .setTitle("생성할 지갑의 비밀번호를 입력하세요.")
                        .setView(et)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try{
                                    String[] s = createWallet(et.getText().toString());

                                    //지갑 주소와 지갑 파일주소 서버 DB에 저장
                                    new utility_http_DBQuery().execute(
                                            "update table_user_membership set wallet_path='" + s[1] + "', " +
                                                    "wallet_filePath='" + s[0] + "' " +
                                                    "where id = '" + activity_login.user_id + "';");

                                    Log.e("파일 주소: ", s[0]);
                                    Log.e("지갑 주소: ", s[1]);

                                }catch (Exception e){
                                    e.printStackTrace();
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
                break;


        }
    }

    public String[] createWallet(final String password){
        String[] result = new String[2];

        try{
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if(!path.exists()){
                path.mkdir();
            }
            String fileName = WalletUtils.generateLightNewWalletFile(password, new File(String.valueOf(path)));

            result[0] = path+"/"+fileName;

            Credentials credentials = WalletUtils.loadCredentials(password, result[0]);

            result[1] = credentials.getAddress();

            return result;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }



}
