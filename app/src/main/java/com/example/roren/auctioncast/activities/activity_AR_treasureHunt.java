package com.example.roren.auctioncast.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.example.roren.auctioncast.AR_treasureHunt.LocationUtils;
import com.example.roren.auctioncast.AR_treasureHunt.DrawSurfaceView;
import com.example.roren.auctioncast.AR_treasureHunt.Point;
import com.example.roren.auctioncast.R;

/**
 *  위치정보를 이용하여 카메라뷰에 보물찾기 스팟을 AR 로 띄우고, 해당 스팟에 가까이 가면 해당 아이템을 얻을 수 있는
 *  액티비티이다.
 */

public class activity_AR_treasureHunt extends AppCompatActivity {


    // 안드로이드 기기 내의 Compass 에 접근하여 기기의 회전값이 변경될 때 해당 이벤트를 캐치하는 Listener.
    // 변경된 x축, y축, z축의 회전값을 mDrawView 로 보내고, DrawView 가 다시 View 들을 그리도록
    // invalidate 메소드를 호출한다.
    private final SensorEventListener mListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (mDrawView != null){
                mDrawView.setOffset(sensorEvent.values[0]);
                mDrawView.setOffset_Y(sensorEvent.values[1]);
                mDrawView.setOffset_Z(sensorEvent.values[2]);
                mDrawView.invalidate();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    // 사용자의 위치 변화를 GPS 를 통해 감지할 수 있는 location manager 클래스
    LocationManager loccationManager;

    // 사용자 기기의 회전값 변화를 감지할 수 있는 Sensor 클래스
    SensorManager mSensorManager;
    Sensor mSensor;

    // 사용자와 AR Spots 의 위치정보를 받아와 사용자로부터 AR Spots 까지의 거리와 대략적인 위치를
    // View 로 그려주는 역할을 하는 View 클래스
    private DrawSurfaceView mDrawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 회전감지 센서 등록
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        setContentView(R.layout.activity_ar_treasurehunt);

        // AR Spots 를 그릴 View 등록
        mDrawView = (DrawSurfaceView) findViewById(R.id.drawSurfaceView);

        // 위치감지 센서 등록
        loccationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        LocationProvider high = loccationManager.getProvider(
                loccationManager.getBestProvider(LocationUtils.createFineCriteria(), true)
        );

        try{
            // mDrawView 에 사용자의 최초 위치 전달
            Location location = loccationManager.getLastKnownLocation(high.getName());
            mDrawView.setMyLocation(location.getLatitude(), location.getLongitude(), location.getAltitude());

            // 위치감지 클래스에 위치변경 이벤트를 관리할 Listener 등록
            loccationManager.requestLocationUpdates(high.getName(), 10000, 10f, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // 사용자의 위치가 변경되었을 때 변경된 위치정보를 mDrawView 에 전달하고,
                    // AR Spots 를 새롭게 그리도록 함
                    mDrawView.setMyLocation(location.getLatitude(), location.getLongitude(), location.getAltitude());
                    mDrawView.invalidate();
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                }

                @Override
                public void onProviderEnabled(String s) {
                }

                @Override
                public void onProviderDisabled(String s) {
                }
            });
        }catch (SecurityException e){
            e.printStackTrace();
        }



    }

    // onResume 에서 등록해제된 센서를 다시 등록
    // 서버 DB 에서 변경된 AR Spots 의 정보를 다시 받아와 세팅
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
        mDrawView.setARSpots();
    }

    // onPause 에서 등록된 센서를 release 한다.
    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mListener);
        super.onPause();
    }
}
