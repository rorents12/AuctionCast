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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.example.roren.auctioncast.AR_treasureHunt.LocationUtils;
import com.example.roren.auctioncast.AR_treasureHunt.DrawSurfaceView;
import com.example.roren.auctioncast.AR_treasureHunt.Point;
import com.example.roren.auctioncast.R;

public class activity_AR_treasureHunt extends AppCompatActivity {

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

    LocationManager loccationManager;
    SensorManager mSensorManager;
    Sensor mSensor;

    private DrawSurfaceView mDrawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        setContentView(R.layout.activity_ar_treasurehunt);

        mDrawView = (DrawSurfaceView) findViewById(R.id.drawSurfaceView);

        loccationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        LocationProvider high = loccationManager.getProvider(
                loccationManager.getBestProvider(LocationUtils.createFineCriteria(), true)
        );

        try{
            Location location = loccationManager.getLastKnownLocation(high.getName());
            mDrawView.setMyLocation(location.getLatitude(), location.getLongitude(), location.getAltitude());

            loccationManager.requestLocationUpdates(high.getName(), 10000, 10f, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // do something here to save this new location
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

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
        mDrawView.setARSpots();
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mListener);
        super.onPause();
    }
}
