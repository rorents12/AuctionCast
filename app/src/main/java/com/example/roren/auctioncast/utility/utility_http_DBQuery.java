package com.example.roren.auctioncast.utility;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * http 통신을 통해 서버 DB 에 접근하고자 할 때, 쿼리문을 execute method 의 parameter 로 넣고 실행하면
 * 해당 쿼리문의 결과값을 JSONArray 형태로 return 해주는 AsyncTask class 이다.
 *
 * new utility_http_DBQuery().execute("query").get 을 통해 손쉽게 쿼리의 결과를 JSONArray 로 받아올 수 있다.
 */

public class utility_http_DBQuery extends AsyncTask<String, Void, JSONArray> {
    @Override
    protected JSONArray doInBackground(String... strings) {

        JSONArray json = new JSONArray();

        Log.e("쿼리확인", strings[0]);

        try{

            String url = "http://52.41.99.92/HttpUtil.php";
            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            StringBuffer buffer = new StringBuffer();

            buffer.append("query").append("=").append(strings[0]);

            OutputStreamWriter outStream = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            PrintWriter writer = new PrintWriter(outStream);
            writer.write(buffer.toString());
            writer.flush();

            int retCode = conn.getResponseCode();

            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = br.readLine()) != null){
                response.append(line);
                response.append("\r");
            }
            br.close();

            String res = response.toString();

            Log.e("http 통신 확인: ", res);

            json = new JSONArray(res);

        }catch (Exception e){
            e.printStackTrace();
        }

        return json;
    }
}
