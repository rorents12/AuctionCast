package com.example.roren.auctioncast.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.roren.auctioncast.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * 로그인 액티비티.
 *
 * 현재는 id를 입력하고 로그인 버튼을 누르면 바로 해당 아이디로 로그인 되는 형식이다.
 *
 * id를 한번 입력하면 sharedpreference를 통해 id를 저장하고, onCreate에서 id 저장 여부를 확인해 저장되어 있으면 로그인 절차를
 * 밟지 않고 바로 activity_broadcasting_list로 넘어간다.
 *
 */

public class activity_login extends AppCompatActivity implements View.OnClickListener{

    Button button_login;

    EditText editText_id;

    SharedPreferences sharedPreferences_save_id;

    public static String user_id;

    // recyclerview adpater 등의 non-activity class에서 context를 사용할 일이 있을 때 사용하기 위해 글로벌 컨텍스트 하나를 선언한다.
    static Context global_context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.INTERNET,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE},
                1
        );

        global_context = this;

        button_login = findViewById(R.id.activity_login_button_login);
        button_login.setOnClickListener(this);

        editText_id = findViewById(R.id.activity_login_editText_id);

        sharedPreferences_save_id  = getSharedPreferences("user_info", MODE_PRIVATE);

        //sharedpreference 체크 후, id가 저장되어 있으면 다음 액티비티로 이동.

        if(!sharedPreferences_save_id.getString("id", "").equals("")){

            user_id = sharedPreferences_save_id.getString("id", "");

            //액티비티 이동 메소드
            move_to_broadcasting_list();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.activity_login_button_login:

                //아이디를 입력하지 않으면 로그인 불가. 입력 시 sharedpreference에 저장.

                if(editText_id.getText().toString().length() == 0){

                    Toast.makeText(this, "아이디를 입력하세요", Toast.LENGTH_SHORT);

                }else{

                    SharedPreferences.Editor editor = sharedPreferences_save_id.edit();

                    editor.putString("id", editText_id.getText().toString());
                    editor.commit();

                    move_to_broadcasting_list();
                }
        }
    }

    //액티비티 이동 메소드

    public void move_to_broadcasting_list(){
        Intent intent = new Intent(this, activity_home.class);

        startActivity(intent);

        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 1:
                Log.e("퍼미션 테스트: ","tt");
        }
    }
}
