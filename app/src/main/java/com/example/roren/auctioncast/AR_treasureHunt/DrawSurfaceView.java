package com.example.roren.auctioncast.AR_treasureHunt;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.roren.auctioncast.R;
import com.example.roren.auctioncast.activities.activity_Ad;
import com.example.roren.auctioncast.activities.activity_contractInfo;
import com.example.roren.auctioncast.activities.activity_kakaopay;
import com.example.roren.auctioncast.activities.activity_login;
import com.example.roren.auctioncast.utility.utility_http_DBQuery;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *  위치정보를 이용하여 카메라뷰에 보물찾기 스팟을 AR 로 띄우고, 해당 스팟에 가까이 가면 해당 아이템을 얻을 수 있는
 *  액티비티이다.
 */

public class DrawSurfaceView extends View {
    Point me = new Point(37.477456, 126.955954, "me", "0", "me");
    Paint mPaint = new Paint();
    private double OFFSET = 0d; //we aren't using this yet, that will come in the next step
    private double OFFSET_Y = 0d;
    private double OFFSET_Z = 0d;
    private double screenWidth, screenHeight = 0d;

    private HashMap<String, ArrayList> hashMap = new HashMap();


    private RewardedVideoAd mRewardedVideoAd;
    private String adsID = "BACDAEBB29A89882B0C332DE9C2A8D9F";

    // AR 보물들의 정보를 저장할 HashMap 과 그 HashMap 의 key 값들을 관리할 ArrayList
    private HashMap<String, Point> hashMap_ARSpots = new HashMap<>();
    private ArrayList<String> ArrayList_ARKeys = new ArrayList<>();

    public DrawSurfaceView(Context c, Paint paint) {
        super(c);
    }

    public void setARSpots(){
        try{
            JSONArray queryResult = new utility_http_DBQuery().execute("select * from table_AR").get();

            hashMap_ARSpots.clear();
            ArrayList_ARKeys.clear();

            for(int i = 0; i < queryResult.length(); i++){
                JSONObject spot = new JSONObject(queryResult.get(i).toString());

                double latitude = spot.getDouble("latitude");
                double longitude = spot.getDouble("longitude");
                String type = spot.getString("type");
                String price = spot.getString("price");
                String key = spot.getString("pk");

                Point treasure = new Point(latitude, longitude, type, price, key);

                hashMap_ARSpots.put(key, treasure);
                ArrayList_ARKeys.add(key);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public DrawSurfaceView(Context context, AttributeSet set) {
        super(context, set);
        // 서버 DB 에서 AR 보물찾기의 보물 정보를 가져온다.

        setARSpots();

        mPaint.setColor(Color.GREEN);
        mPaint.setTextSize(50);
        mPaint.setStrokeWidth(DpiUtils.getPxFromDpi(getContext(), 2));
        mPaint.setAntiAlias(true);


        // 광고 세팅
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getContext());
        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {

            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {

                mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());

                final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog  .setTitle("광고시청 보상")
                        .setMessage("1000원의 옥션캐시를 받았습니다.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new utility_http_DBQuery().execute(
                                        "update table_user_membership set cash=cash-(-1000) where id='"+ activity_login.user_id+"';"
                                );
                                dialogInterface.dismiss();
                            }
                        });
                dialog.create();
                dialog.show();
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                Log.e("광고시청 완료", "ㄻㄴㅇㄹㄴ");
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {

            }
        });
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());



    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d("onSizeChanged", "in here w=" + w + " h=" + h);
        screenWidth = (double) w;
        screenHeight = (double) h;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                Bitmap treasure = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.treasure);

                double x = event.getX();
                double y = event.getY();
                double x1 = 0;
                double y1 = 0;


                for(int i = 0; i < ArrayList_ARKeys.size(); i++){
                    String key = ArrayList_ARKeys.get(i);

                    if(hashMap.get(key) != null){
                        x1 = (float)hashMap.get(key).get(0) + treasure.getWidth()/2;
                        y1 = (float)hashMap.get(key).get(1) + treasure.getHeight()/2;
                    }

                    if(x > x1 - (100) && x < x1 + (100)){
                        if(y > y1 - (100) && y < y1 + (100)){
                            Point p = hashMap_ARSpots.get(key);

                            switch (p.type){
                                case "coupon":
                                    new utility_http_DBQuery().execute(
                                            "delete from table_AR where pk='"+p.description+"';"
                                    );
                                    final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                                    dialog  .setTitle("쿠폰 발견!")
                                            .setMessage("500원의 옥션캐시를 받았습니다.")
                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    new utility_http_DBQuery().execute(
                                                            "update table_user_membership set cash=cash-(-500) where id='"+ activity_login.user_id+"';"
                                                    );
                                                    setARSpots();
                                                    dialogInterface.dismiss();
                                                }
                                            });
                                    dialog.create();
                                    dialog.show();
                                    break;

                                case "ad":
                                    if(mRewardedVideoAd.isLoaded()){
                                        new utility_http_DBQuery().execute(
                                                "delete from table_AR where pk='"+p.description+"';"
                                        );
                                        mRewardedVideoAd.show();
                                    }

                                    break;
                            }
                            break;
                        }
                    }
                }



                StringBuilder s = new StringBuilder();
                s
                        .append("\n")
                        .append("터치 x 좌표 = ").append(x).append("\n")
                        .append("터치 y 좌표 = ").append(y).append("\n")
                        .append("x 좌표 = ").append(x1).append("\n")
                        .append("y 좌표 = ").append(y1).append("\n");

                Log.e("위치 확인", s.toString());


        }

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        hashMap.clear();

        for (int i = 0; i < ArrayList_ARKeys.size(); i++) {
            Bitmap spot;
            Point u = hashMap_ARSpots.get(ArrayList_ARKeys.get(i));

            int dist = (int)distInMetres(me, u);

            if(dist > 1000){
                continue;
            }

            if(dist < 50){
                spot = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.treasure);
            }else{
                spot = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.blip);
            }

            double angle = bearing(me.latitude, me.longitude, u.latitude, u.longitude) - OFFSET;
            double angleY = bearing(me.latitude, me.longitude, u.latitude, u.longitude) - OFFSET_Y;
            double xPos, yPos;

            if(angle < 0)
                angle = (angle+360)%360;
            if(angleY < 0)
                angleY = (angleY+360)%360;

            if(angle > 360)
                angle = angle % 360;
            if(angleY > 360)
                angleY = angleY % 360;

//            if(angle > 45 && angle < 315 && angleY > 45 && angleY < 315){
//
//                if(angle > 135 && angle < 180)
//                    angle += 180;
//                if(angleY > 135 && angleY < 180)
//                    angleY += (180);
//
//                if(angle < 225 && angle > 180)
////                    angle -= (225 + 315);
//                    angle -= 180;
//                if(angleY < 225 && angleY > 180)
//                    angleY -= (180);
//
//                if(angle < 0)
//                    angle = -(angle);
//                if(angleY < 0)
//                    angleY = -(angleY);
//
//                if(angle > 360)
//                    angle = angle % 360;
//                if(angleY > 360)
//                    angleY = angleY % 360;
//            }

//            Log.e("각도", "X: " + String.valueOf(angle) + ", Y: " + String.valueOf(angleY));


            if(angle > 44 && angle < 180) {
                angle = 44;
            }else if(angle < 316 && angle >= 180){
                angle = 316;
            }

            if(angleY > 30 && angleY < 180){
                angleY = 30;
            }else if(angleY < 330 && angleY >= 180){
                angleY = 330;
            }

            double posInPx = angle * (screenWidth / 90d);
            double posInPy = angleY * (screenHeight / 90d);

            int spotCentreX = spot.getWidth() / 2;
            int spotCentreY = spot.getHeight() / 2;
            xPos = posInPx - spotCentreX;
            yPos = posInPy - spotCentreY;

            if (angle <= 45)
                u.x = (float) ((screenWidth / 2) + xPos);
            else if (angle >= 315)
                u.x = (float) ((screenWidth / 2) - ((screenWidth*4) - xPos));
            else
                u.x = (float) (float)(screenWidth*9); //somewhere off the screen

            if (angleY <= 45)
                u.y = (float) ((screenHeight / 2) + yPos);
            else if(angleY >= 315)
                u.y = (float) ((screenHeight / 2) - ((screenHeight*4) - yPos));
            else
                u.y = (float) (float)(screenHeight*9); //somewhere off the screen

            canvas.drawBitmap(spot, u.x, u.y, mPaint); //camera spot

            if(dist < 50){
                canvas.drawText("터치!", u.x, u.y, mPaint);
                ArrayList array = new ArrayList();

                array.add(u.x); array.add(u.y);
                hashMap.put(u.description, array);
            }else{
                canvas.drawText(String.valueOf(dist)+"m", u.x - (spot.getWidth()*2), u.y - spot.getHeight(), mPaint); //text
            }


//            StringBuilder s = new StringBuilder();
//            s
//                    .append("  ").append("\n")
//                    .append("현재위치 = ").append(me.latitude).append(", ").append(me.longitude).append("\n")
//                    .append("angleX = ").append(angle).append("\n")
//                    .append("angleY = ").append(angleY).append("\n")
//                    .append("화면의 x좌표 = ").append(u.x).append("  화면의 너비 = ").append(screenWidth).append("\n")
//                    .append("화면의 y좌표 = ").append(u.y).append("  화면의 높이 = ").append(screenHeight).append("\n")
//                    .append("OFFSET_X = ").append(OFFSET).append("\n")
//                    .append("OFFSET_Y = ").append(OFFSET_Y).append("\n")
//                    .append("OFFSET_Z = ").append(OFFSET_Z).append("\n");
//
//            Log.e("위치정보", s.toString());

        }
    }

    public void setOffset(float offset) {
        this.OFFSET = offset;
        if(OFFSET < 0){
            OFFSET = (OFFSET + 360)%360;
        }else if(OFFSET > 360){
            OFFSET = OFFSET%360;
        }
    }

    public void setOffset_Y(float offset_y){
        this.OFFSET_Y = offset_y;
        if(OFFSET_Y < 0){
            OFFSET_Y = (OFFSET_Y + 360)%360;
        }else if(OFFSET_Y > 360){
            OFFSET_Y = OFFSET_Y%360;
        }
    }

    public void setOffset_Z(float offset_z){
        this.OFFSET_Z = offset_z;
    }

    public void setMyLocation(double latitude, double longitude, double altitude) {
        me.latitude = latitude;
        me.longitude = longitude;

        Log.e("location switching", String.valueOf(latitude) + ", " + String.valueOf(longitude) + ", " + String.valueOf(altitude));
    }

    protected double distInMetres(Point me, Point u) {

        double lat1 = me.latitude;
        double lng1 = me.longitude;

        double lat2 = u.latitude;
        double lng2 = u.longitude;

        Location a = new Location("point A");
        Location b = new Location("point B");

        a.setLatitude(lat1);
        a.setLongitude(lng1);
        b.setLatitude(lat2);
        b.setLongitude(lng2);

        double distance = a.distanceTo(b);

        return distance;
//        double earthRadius = 6371;
//        double dLat = Math.toRadians(lat2 - lat1);
//        double dLng = Math.toRadians(lng2 - lng1);
//        double sindLat = Math.sin(dLat / 2);
//        double sindLng = Math.sin(dLng / 2);
//        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(lat1) * Math.cos(lat2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        double dist = earthRadius * c;
//
//        return dist * 1000;
    }

    protected static double bearing(double lat1, double lon1, double lat2, double lon2) {
        double longDiff = Math.toRadians(lon2 - lon1);
        double la1 = Math.toRadians(lat1);
        double la2 = Math.toRadians(lat2);
        double y = Math.sin(longDiff) * Math.cos(la2);
        double x = Math.cos(la1) * Math.sin(la2) - Math.sin(la1) * Math.cos(la2) * Math.cos(longDiff);

        double result = Math.toDegrees(Math.atan2(y, x));
        return (result+360.0d)%360.0d;
    }
}
