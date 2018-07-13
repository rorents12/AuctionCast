package com.example.roren.auctioncast.utility;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import java.math.BigInteger;

public class utility_ether_connectToken extends AsyncTask<String, Void, String> {

        private int method;
        private String walletPath;
        private String walletFileAddress;
        private String walletPath_to;
        private int amount;

        public utility_ether_connectToken(int method, String walletPath, String walletFileAddress){
            this.method = method;
            this.walletPath = walletPath;
            this.walletFileAddress = walletFileAddress;
        }

        public utility_ether_connectToken(int method, String walletPath, String walletFileAddress, String to, String amount){
            this.method = method;
            this.walletPath = walletPath;
            this.walletFileAddress = walletFileAddress;
            this.walletPath_to = to;
            this.amount = Integer.parseInt(amount);
        }

        @Override
        protected String doInBackground(String... strings) {
            try{

                //토큰 사용 준비
                Web3j web3 = Web3jFactory.build(new HttpService("https://rinkeby.infura.io/klwpYHouwU7OfVRYqK3z"));

                Credentials credentials = WalletUtils.loadCredentials("1", walletFileAddress);

                AuctionCastToken_sol_AuctionCastToken contract = AuctionCastToken_sol_AuctionCastToken.load(
                        utility_global_variable.AUCTION_COIN_ADDRESS,
                        web3,
                        credentials,
                        ManagedTransaction.GAS_PRICE,
                        Contract.GAS_LIMIT);


                Log.e("지갑 주소" , walletPath);
                Log.e("파일 주소", walletFileAddress);

                switch (method){
                    // 토큰 잔액 조회
                    case utility_global_variable.CODE_ETHER_GET_BALANCE:
                        BigInteger result = contract.balanceOf(walletPath).send();

                        return result.toString();

                    // 토큰 발행
                    case utility_global_variable.CODE_ETHER_DEPLOY_TOKEN:
                        //토큰 발행 코드
                        AuctionCastToken_sol_AuctionCastToken newContract = AuctionCastToken_sol_AuctionCastToken.deploy(
                                web3, credentials, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT, BigInteger.valueOf(100000)
                        ).send();
                        break;

                    // 토큰 전송
                    case utility_global_variable.CODE_ETHER_SEND_TOKEN:

                        TransactionReceipt result2 = contract.transfer(walletPath_to, BigInteger.valueOf(amount)).send();

                        return result2.toString();

                }

            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }


}
