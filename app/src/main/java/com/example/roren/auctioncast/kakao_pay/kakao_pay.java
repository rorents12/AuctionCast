//package com.example.roren.auctioncast.kakao_pay;
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.AsyncTask;
//
//import com.example.roren.auctioncast.activities.activity_webView;
//
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.io.OutputStreamWriter;
//import java.net.URL;
//
//import javax.net.ssl.HttpsURLConnection;
//
//public class kakao_pay extends AsyncTask{
//
//    private Context context;
//
//    public kakao_pay(Context context, String API_CODE, String params){
//        this.context = context;
//    }
//
//    public enum HttpMethodType { POST, GET, DELETE }
//
//    private static final String API_SERVER_HOST  = "https://kapi.kakao.com";
//
//    public static final String API_CODE_READY = "/v1/payment/ready";
//    private static final String API_PARAM_CONTENT_TYPE = "application/x-www-from-urlencoded";
//    private static final String API_PARAM_CHARSET = "utf-8";
//
//    private String access_token = "8f5348d2f23ebb8b4ec72e8f8d61c68e";
//
//
//    @Override
//    protected Object doInBackground(Object[] objects) {
//
//        String requestURL = API_SERVER_HOST + API_CODE_READY;
//        HttpMethodType httpMethodType = HttpMethodType.POST;
//
//        HttpsURLConnection conn;
//        OutputStreamWriter writer = null;
//        BufferedReader reader = null;
//        InputStreamReader isr = null;
//
//        String params = "?cid=TC0ONETIME&partner_order_id=partner_order_id&partner_user_id=partner_user_id&" +
//                "item_name=초코파이&quantity=1&total_amount=2200&vat_amount=200&tax_free_amount=0&" +
//                "approval_url=https://developers.kakao.com/success&" +
//                "fail_url=https://developers.kakao.com/fail&" +
//                "cancel_url=https://developers.kakao.com/cancel";
//
//        if (params != null && params.length() > 0) {
//            requestURL += params;
//        }
//
//        try{
//            final URL url = new URL(requestURL);
//
//            conn = (HttpsURLConnection) url.openConnection();
//            conn.setRequestMethod(httpMethodType.toString());
//
//            conn.setRequestProperty("Authorization", "KakaoAK " + access_token);
//
//            conn.setRequestProperty("Content-Type", API_PARAM_CONTENT_TYPE);
//            conn.setRequestProperty("charset", API_PARAM_CHARSET);
//            conn.setRequestProperty("Content-Length", String.valueOf(params));
//
//            final int responseCode = conn.getResponseCode();
//            System.out.println(String.format("\nSending '%s' request to URL : %s", httpMethodType, requestURL));
//            System.out.println("Response Code : " + responseCode);
//
//            if (responseCode == 200)
//                isr = new InputStreamReader(conn.getInputStream());
//            else
//                isr = new InputStreamReader(conn.getErrorStream());
//
//            reader = new BufferedReader(isr);
//            final StringBuffer buffer = new StringBuffer();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                buffer.append(line);
//            }
//            System.out.println(buffer.toString());
//            return buffer.toString();
//
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            if (writer != null) try { writer.close(); } catch (Exception ignore) { }
//            if (reader != null) try { reader.close(); } catch (Exception ignore) { }
//            if (isr != null) try { isr.close(); } catch (Exception ignore) { }
//        }
//
//
//
//        return null;
//    }
//
//    @Override
//    protected void onPostExecute(Object o) {
//        super.onPostExecute(o);
//        try{
//            String s = o.toString();
//
//            JSONObject jsonObject = new JSONObject(s);
//
//            Intent intent = new Intent(context, activity_webView.class);
//
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//    }
//}
