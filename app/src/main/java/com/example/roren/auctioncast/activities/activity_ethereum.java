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

/**
 *  사용자 자신의 이더리움 지갑 주소와, 현재 보유하고 있는 옥션코인(토큰)의 갯수를 보여주거나
 *  사용자가 이더리움 지갑을 생성할 수 있는 액티비티
 *
 *  1. 사용자가 지갑을 보유하고 있을 경우
 *  사용자의 이더리움 지갑 주소와 보유중인 옥션코인의 갯수를 보여준다.
 *
 *  2. 사용자가 지갑을 보유하고 있지 않을 경우
 *  지갑 생성 버튼을 통해 이더리움 지갑을 생성할 수 있다.
 *
 */

public class activity_ethereum extends AppCompatActivity implements View.OnClickListener{

    // 지갑 생성 버튼
    Button button_createWallet;

    // 지갑주소, 옥션코인 갯수를 나타내는 textView
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


        // 지갑을 가지고 있으면 지갑 정보를, 지갑을 가지고 있지 않으면 지갑 생성 버튼을 보여준다.
        if(utility_global_variable.WALLET_ADDRESS.equals("null")){
            // 지갑 정보가 없으므로, 지갑 생성 버튼을 제외한 view 들을 보이지 않게 처리
            textView_auctionCoin.setVisibility(View.INVISIBLE);
            textView_walletAddress.setVisibility(View.GONE);
            textView_wallet.setVisibility(View.GONE);
        }else{
            // 지갑 정보가 있으므로, 지갑 생성 버튼의 visibility 를 gone 으로 처리
            button_createWallet.setVisibility(View.GONE);
            textView_walletAddress.setText(utility_global_variable.WALLET_ADDRESS);
            try{
                // 옥션코인 잔액 조회
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

    /**
     *  지갑 생성 버튼을 눌렀을 때, 클릭 이벤트 처리
     *  클릭 이벤트 발생 시, 다이얼로그의 editText 를 통해 지갑의 비밀번호를 입력할 수 있다.
     *  비밀번호를 입력할 시 해당 비밀번호를 가진 이더리움 지갑 파일을 로컬 저장소에 생성하고,
     *  해당 파일의 주소와 지갑 주소를 서버 DB 의 user_membership table 에 저장한다.
     */
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

    /**
     * 이더리움 지갑을 생성해주는 method. parameter 로 비밀번호를 받아오고, 해당 비밀번호를 가진 이더리움 지갑 파일을
     * 로컬 저장소에 생성한다. 지갑 파일 저장경로와 지갑 주소를 return 한다.
     *
     * @param password
     *      지갑의 비밀번호
     * @return
     *      지갑 파일의 로컬 저장소 주소(filePath)와, 생성된 지갑의 주소를 담고 있는 String Array 를 return 한다.
     */
    public String[] createWallet(final String password){
        String[] result = new String[2];

        try{
            // 지갑 생성
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if(!path.exists()){
                path.mkdir();
            }
            String fileName = WalletUtils.generateLightNewWalletFile(password, new File(String.valueOf(path)));

            // 지갑 파일 경로 획득
            result[0] = path+"/"+fileName;

            // 지갑 주소 획득
            Credentials credentials = WalletUtils.loadCredentials(password, result[0]);
            result[1] = credentials.getAddress();

            return result;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }



}
