package com.example.roren.auctioncast.utility;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import com.example.roren.auctioncast.activities.activity_home;
import com.example.roren.auctioncast.activities.activity_login;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class utility_http_uploadImage extends AsyncTask{
    String url = "";
    Context context;

    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";

    String imagePath;
    String fileName;

    ProgressDialog progressDialog;

    public utility_http_uploadImage(Context context, String imagePath){
        this.url = "http://52.41.99.92/upload_userProfile.php";
        this.context = context;
        this.imagePath = imagePath;
        this.fileName = activity_login.user_id + "_profile.jpg";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progressDialog = ProgressDialog.show(context, "사진 업로드 중...", null, true, true);
    }

    @Override
    protected Object doInBackground(Object[] objects) {

        try{

            URL urlConn = new URL(url);

            HttpURLConnection conn = (HttpURLConnection)urlConn.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            // 회원정보 전송 한글깨짐문제 해결 --> 스트링을 바이트로 변환(UTF-8이용) 후 writeBytes가 아닌 write 사용
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition:form-data; name=\"" + "id" + "\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.write(activity_login.user_id.getBytes("UTF-8"));
            dos.writeBytes(lineEnd);

            // 이미지 전송
            FileInputStream mfileInputStream = null;

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + fileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            mfileInputStream = new FileInputStream(imagePath);

            int bytesAvailable = mfileInputStream.available();
            int maxBufferSize = 2048;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            byte[] buffer = new byte[bufferSize];
            int bytesRead = mfileInputStream.read(buffer, 0, bufferSize);

            while(bytesRead > 0){
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = mfileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = mfileInputStream.read(buffer, 0, bufferSize);
            }

            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            mfileInputStream.close();

            dos.flush();

            // ------------------------------------------------------------------------------------
            // 이게... getInputStream 에서 계속 fileNotFoundException 이 발생했다.
            // php 파일은 분명히 존재함에도 불구하고 이게 떴었는데... 아무래도 해당 url 에서 input 이
            // 발생하지 않아 생긴 Exception 인듯.
            //
            // getInputStream 을 getErrorStream 으로 변경하니 해당 error 를 받아오더라고. error 메시지는
            // 보내는 이미지의 용량이 너무 커서.. Request too large..? 였나 여튼 이 메시지가 떴음.
            //
            // 보니까 이게 웹서버, 즉 nginx 에서 에러가 난거라 php 파일까지 접근하지 못해 getInputStream 이
            // 안된 것 같다. getErrorStream 은 웹서버 프로그램에서 발생한 에러를 감지하여 해당 로그를
            // 받아올 수 있는 듯.
            //
            // 보내는 이미지의 용량을 줄이고 나니 getInputStream 이 정상적으로 작동하는 걸 확인할 수 있었다.
            // ------------------------------------------------------------------------------------
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

            Log.e("이미지 업로드 확인: ", res);

            dos.close();

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        progressDialog.dismiss();
        Intent intent = new Intent(context, activity_home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
}
