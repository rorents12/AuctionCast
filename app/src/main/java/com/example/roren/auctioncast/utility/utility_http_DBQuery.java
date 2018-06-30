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

public class utility_http_DBQuery extends AsyncTask<String, Void, JSONArray> {
    @Override
    protected JSONArray doInBackground(String... strings) {

        JSONArray json = new JSONArray();

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

//            byte[] outputInBytes = strings[0].getBytes("UTF-8");
//            OutputStream os = conn.getOutputStream();
//            os.write(outputInBytes);
//            os.close();

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
