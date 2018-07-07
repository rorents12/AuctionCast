package com.example.roren.auctioncast.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.roren.auctioncast.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class activity_webView extends AppCompatActivity {

    // 카카오페이 REST API를 이용할 시, redirect url을 받아 웹 화면을 띄워주는 역할.
    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = findViewById(R.id.activity_webView_webView);

        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);

        // 웹 뷰의 loadurl 함수를 실행했을 때, url을 파라미터로 가져와서 아래에 overide 된 콜백함수가 실행된다.
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {

                    //카카오페이에서 제공하는 uri는 custom_scheme이기 때문에 일반 웹뷰에서는 열수 없고, 카카오톡의 인앱브라우저로 연결하여
                    //해당 액티비티를 실행해주어야 한다.
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());

                    if(existPackage != null){
                        startActivity(intent);
                    }else{
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                        startActivity(marketIntent);
                    }

                    return true;
                }catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            }
        });

        //카카오페이 REST API 호출

        new kakao_pay(this, null, getIntent().getStringExtra("total_amount")).execute();
    }


    /**
     * 카카오페이 REST api를 호출해주는 AsyncTask
     */
    private class kakao_pay extends AsyncTask{

        // REST API를 제공하는 URL
        private static final String API_SERVER_HOST  = "https://kapi.kakao.com";

        // POST 방식으로 API 호출
        private static final String httpMethodType = "POST";

        // 호출할 API를 결정하는 URL 경로
        public static final String API_CODE_READY = "/v1/payment/ready";

        // HTTP 요청 시 헤더에 포함할 정보
        private static final String API_PARAM_CONTENT_TYPE = "application/x-www-from-urlencoded";
        private static final String API_PARAM_CHARSET = "utf-8";

        // HTTP 요청 시 POST로 보낼 정보
        private HashMap<String, String> map_params;

        private Context context;

        public kakao_pay(Context context, String API_CODE, String total_amount){
            this.map_params = new HashMap<>();

            map_params.put("cid", "TC0ONETIME");
            map_params.put("partner_order_id", "partner_order_id");
            map_params.put("partner_user_id", "partner_user_id");
            map_params.put("item_name", "옥션캐시");
            map_params.put("quantity", "1");
            map_params.put("total_amount", total_amount);
            map_params.put("vat_amount", "0");
            map_params.put("tax_free_amount", "0");
            map_params.put("approval_url", "http://52.41.99.92/kakao-pay/success.php?id=" + activity_login.user_id);
            map_params.put("fail_url", "http://52.41.99.92/kakao-pay/fail.php?id=" + activity_login.user_id);
            map_params.put("cancel_url", "http://52.41.99.92/kakao-pay/cancel.php?id=" + activity_login.user_id);
            map_params.put("user_id", activity_login.user_id);

            this.context = context;
        }


//        private String access_token = "8f5348d2f23ebb8b4ec72e8f8d61c68e";


        @Override
        protected Object doInBackground(Object[] objects) {

            String requestURL = "http://52.41.99.92/kakao-pay/ready.php";
//            String requestURL = API_SERVER_HOST + API_CODE_READY;

            HttpURLConnection conn;
            OutputStreamWriter writer = null;
            BufferedReader reader = null;
            InputStreamReader isr = null;

            StringBuilder sb = new StringBuilder();
            sb.append("?");
            for (String key : map_params.keySet()) {
                sb.append(sb.length() > 0 ? "&" : "");
                sb.append(String.format("%s=%s", key, map_params.get(key).toString()));
            }

            String params = sb.toString();

//            String params = "?cid=TC0ONETIME&partner_order_id=partner_order_id&partner_user_id=partner_user_id&" +
//                    "item_name=초코파이&quantity=1&total_amount=2200&vat_amount=200&tax_free_amount=0&" +
//                    "approval_url=http://52.41.99.92/kakao-pay/success.php?id=" + activity_login.user_id +
//                    "&fail_url=http://52.41.99.92/kakao-pay/fail.php?id=" + activity_login.user_id +
//                    "&cancel_url=http://52.41.99.92/kakao-pay/cancel.php?id=" + activity_login.user_id +
//                    "&user_id=" + activity_login.user_id;

//            String params = "?cid=TC0ONETIME&partner_order_id=partner_order_id&partner_user_id=partner_user_id&" +
//                    "item_name=초코파이&quantity=1&total_amount=2200&vat_amount=200&tax_free_amount=0&" +
//                    "approval_url=https://developers.kakao.com/payment/mobile/approve/"+ access_token +
//                    "&fail_url=https://developers.kakao.com/payment/mobile/fail"+
//                    "&cancel_url=https://developers.kakao.com/payment/mobile/cancel";

            if (params != null && params.length() > 0) {
                requestURL += params;
            }

            try{
                final URL url = new URL(requestURL);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(httpMethodType);

//                conn.setRequestProperty("Authorization", "KakaoAK " + access_token);

                conn.setRequestProperty("Content-Type", API_PARAM_CONTENT_TYPE);
                conn.setRequestProperty("charset", API_PARAM_CHARSET);
                conn.setRequestProperty("Content-Length", String.valueOf(params));

                final int responseCode = conn.getResponseCode();
                System.out.println(String.format("\nSending '%s' request to URL : %s", httpMethodType, requestURL));
                System.out.println("Response Code : " + responseCode);

                if (responseCode == 200)
                    isr = new InputStreamReader(conn.getInputStream());
                else
                    isr = new InputStreamReader(conn.getErrorStream());

                reader = new BufferedReader(isr);
                final StringBuffer buffer = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                System.out.println(buffer.toString());

                return buffer.toString();


            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (writer != null) try { writer.close(); } catch (Exception ignore) { }
                if (reader != null) try { reader.close(); } catch (Exception ignore) { }
                if (isr != null) try { isr.close(); } catch (Exception ignore) { }
            }



            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            try{
                String s = o.toString();

                JSONObject jsonObject = new JSONObject(s);

                webView.loadUrl(jsonObject.getString("next_redirect_app_url"));

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
