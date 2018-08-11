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
 *  activity_AR_treasureHunt 액티비티가 가지고 있는 View 이다.
 *  서버로부터 쿠폰들의 위치정보를 받아와 그것을 기반으로 화면에 그리는 역할을 한다.
 *
 */

public class DrawSurfaceView extends View {

    // 내 위치를 저장할 Point 클래스
    Point me = new Point(0, 0, "me", "0", "me");

    // AR Spots 을 나타내는 View 위에 Text View 를 넣어야 하는데, 해당 Text View 의
    // 설정값을 가지고 있을 Paint 클래스
    Paint mPaint = new Paint();

    // activity_AR_TreasureHunt 로 부터 회전값을 전달받아 저장할 double 변수.
    // 각각 x축, y축, z축 값을 저장한다.
    private double OFFSET = 0d;
    private double OFFSET_Y = 0d;
    private double OFFSET_Z = 0d;

    // 사용자 기기의 스크린 너비와 높이를 저장할 변수
    private double screenWidth, screenHeight = 0d;

    // 쿠폰들의 정보를 저장할 HashMap 과 그 HashMap 의 key 값들을 관리할 ArrayList
    // key 값은 각 쿠폰들이 가지고있는 고유번호이다.
    private HashMap<String, Point> hashMap_ARSpots = new HashMap<>();
    private ArrayList<String> ArrayList_ARKeys = new ArrayList<>();

    // 사용자가 쿠폰들의 위치로부터 100미터 내로 접근했을 때, 사용자는 해당 쿠폰을 화면에서 터치하여
    // 보상을 얻을 수 있다. 사용자로부터 100미터 내에 있는 쿠폰들이 표현된 View 의
    // 기기의 스크린 좌표를 저장하는 hashMap 변수. key 값은 각각의 쿠폰들이 가지고 있는 고유번호이다.
    private HashMap<String, ArrayList> hashMap_touchLocation = new HashMap();

    // 애드몹 광고 변수
    private RewardedVideoAd mRewardedVideoAd;
    private String adsID = "BACDAEBB29A89882B0C332DE9C2A8D9F";
    private Boolean ad_complete = false;


    // 서버로부터 쿠폰들의 위치를 받아와 해당 정보들을 관리할 hashMap_ARSpots, ArrayList_ARKeys 변수에
    // 세팅하는 메소드
    public void setARSpots(){
        try{
            // 서버로부터 쿠폰들의 정보를 받아온다.
            JSONArray queryResult = new utility_http_DBQuery().execute("select * from table_AR").get();

            // 세팅할 때 마다 hashMap 과 ArrayList 초기화
            hashMap_ARSpots.clear();
            ArrayList_ARKeys.clear();

            // hashMap 과 ArrayList 에 세팅
            // 위치정보를 받아와 정보들을 Point 클래스에 저장하고, 해당 Point 를 key 값과 함께
            // hashMap 에 저장 후, 해당 key 값들을 ArrayList 에 저장.
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

    // DrawSurfaceView 의 생성자.
    public DrawSurfaceView(Context context, AttributeSet set) {
        super(context, set);
        // 서버 DB 에서 AR 보물찾기의 보물 정보를 가져와 세팅
        setARSpots();

        // 출력할 Text View 의 설정값 설정
        mPaint.setColor(Color.GREEN);
        mPaint.setTextSize(50);
        mPaint.setStrokeWidth(DpiUtils.getPxFromDpi(getContext(), 2));
        mPaint.setAntiAlias(true);


        // 광고 세팅
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getContext());

        // 광고 시청도중 발생할 수 있는 이벤트들을 감지하는 eventListener
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
                // 광고창이 꺼졌을 때

                // 광고를 다시 Load
                mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());

                // 광고를 다 본 상태라면 500 옥션캐시를 지급, 그렇지 않으면 100 옥션캐시를 지급.
                if(ad_complete){

                    // 광고 시청완료를 알리는 boolean 변수를 다시 false 로 변경
                    ad_complete = false;

                    final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog  .setTitle("광고시청 보상")
                            .setMessage("500원의 옥션캐시를 받았습니다.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    new utility_http_DBQuery().execute(
                                            "update table_user_membership set cash=cash-(-500) where id='"+ activity_login.user_id+"';"
                                    );
                                    dialogInterface.dismiss();
                                }
                            });
                    dialog.create();
                    dialog.show();
                }else{
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog  .setTitle("광고를 끝까지 보지 않았군요!")
                            .setMessage("100원의 옥션캐시를 받았습니다.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    new utility_http_DBQuery().execute(
                                            "update table_user_membership set cash=cash-(-100) where id='"+ activity_login.user_id+"';"
                                    );
                                    setARSpots();
                                    dialogInterface.dismiss();
                                }
                            });
                    dialog.create();
                    dialog.show();
                }


            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                // 광고시청을 완료했을 시 해당 사실을 알려주는 boolean 변수를 true 로 설정.
                ad_complete = true;
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {

            }
        });

        // 광고 load
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());
    }


    // 화면 회전으로 Screen 의 width 와 height 가 변경될 때 해당 사실을 감지하여
    // screenWidth, screenHeight 변수에 변경된 값을 저장
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d("onSizeChanged", "in here w=" + w + " h=" + h);
        screenWidth = (double) w;
        screenHeight = (double) h;

    }

    /**
     * 화면을 터치했을 때 해당 좌표와 현재 화면에 표시되고 있는 쿠폰의 좌표를 비교하여
     * 100미터 이내에 있는 쿠폰을 터치했을 시 해당 쿠폰을 얻을 수 있도록 하는 메소드
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){

            // 터치 이벤트 발생 시
            case MotionEvent.ACTION_UP:

                // 쿠폰의 좌표로부터 얼마나 떨어져있어도 터치를 인식할 것인가를 결정하기 위해
                // 보물상자 모양의 bitmap 의 width 와 height 를 이용한다.
                Bitmap treasure = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.treasure);

                // 사용자의 터치좌표 x,y 와 쿠폰의 위치좌표를 저장할 x1,y1 변수
                double x = event.getX();
                double y = event.getY();
                double x1 = 0;
                double y1 = 0;

                // 현재 화면에 표시되고 있는 모든 쿠폰들의 key 값을 이용하여
                // hashMap_touchLocation 에 존재하는 쿠폰들을 꺼낸다.
                // 그리고 해당 쿠폰들의 좌표를 x1, y1 변수에 저장하고, 사용자의 터치 위치와 비교하여
                // 적정 위치를 터치했다면 터치이벤트가 실행되도록 한다.
                for(int i = 0; i < ArrayList_ARKeys.size(); i++){
                    String key = ArrayList_ARKeys.get(i);

                    if(hashMap_touchLocation.get(key) != null){
                        x1 = (float)hashMap_touchLocation.get(key).get(0) + treasure.getWidth()/2;
                        y1 = (float)hashMap_touchLocation.get(key).get(1) + treasure.getHeight()/2;
                    }

                    if(x > x1 - (100) && x < x1 + (100)){
                        if(y > y1 - (100) && y < y1 + (100)){
                            Point p = hashMap_ARSpots.get(key);

                            // 쿠폰의 타입에 따라 광고를 실행할 것인지, 바로 캐시 지급을 할것인지를 결정한다.
                            switch (p.type){

                                // 타입이 일반 쿠폰일경우
                                case "coupon":
                                    // 해당 쿠폰 서버 DB 에서 삭제
                                    new utility_http_DBQuery().execute(
                                            "delete from table_AR where pk='"+p.description+"';"
                                    );
                                    final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                                    dialog  .setTitle("쿠폰 발견!")
                                            .setMessage("100원의 옥션캐시를 받았습니다.")
                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    new utility_http_DBQuery().execute(
                                                            "update table_user_membership set cash=cash-(-100) where id='"+ activity_login.user_id+"';"
                                                    );
                                                    setARSpots();
                                                    dialogInterface.dismiss();
                                                }
                                            });
                                    dialog.create();
                                    dialog.show();
                                    break;

                                // 타입이 광고일 경우
                                case "ad":
                                    if(mRewardedVideoAd.isLoaded()){
                                        // 해당 쿠폰 서버 DB 에서 삭제
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
        }

        return true;
    }


    /**
     * DrawSurfaceView.invalidate() 메소드를 실행할 때마다 호출되는 함수.
     * 실제로 view 들을 그려내는 역할을 한다.
     */
    @Override
    protected void onDraw(Canvas canvas) {

        // 터치 가능한 쿠폰들의 정보를 그릴때마다 초기화 하고 다시 그려낸다.
        // 다시 그리게 될 때 터치 가능했던 쿠폰이 터치가 안되도록 변경될 수 있기 때문
        hashMap_touchLocation.clear();

        // 서버 DB 에서 가져온 모든 쿠폰들에 대해 거리를 체크하고 해당 거리에 맞도록 화면에 쿠폰의 View 를 그린다.
        for (int i = 0; i < ArrayList_ARKeys.size(); i++) {

            // 쿠폰을 나타낼 Bitmap
            Bitmap spot;

            // 쿠폰의 위치정보를 저장할 Point 클래스
            Point u = hashMap_ARSpots.get(ArrayList_ARKeys.get(i));

            // 사용자와 쿠폰 사이의 거리를 저장할 dist 변수
            int dist = (int)distInMetres(me, u);

            // 사용자와 쿠폰 사이의 거리가 1000미터 이상이면 화면에 그리지 않는다.
            if(dist > 1000){
                continue;
            }

            // 사용자와 쿠폰 사이의 거리가 100미터 이내이면 해당 쿠폰의 타입에 따라
            // spot 변수에 광고모양의 Bitmap 과 보물상자 모양의 bitmap 을 저장한다.
            // 거리가 100미터 보다 크다면 하얀 점으로 표시한다.
            if(dist < 200){
                switch (u.type){
                    default:
                        spot = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.treasure);
                        break;
                    case "ad":
                        spot = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ad);
                        break;
                }
            }else{
                spot = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.blip);
            }

            // 사용자가 향하고 있는 방향과 쿠폰의 위치 사이의 x축, y축 각도를 계산하여
            // 각각 angle, angleY 변수에 저장한다.
            double angle = bearing(me.latitude, me.longitude, u.latitude, u.longitude) - OFFSET;
//            double angleY = bearing(me.latitude, me.longitude, u.latitude, u.longitude) - OFFSET_Y;
            double xPos, yPos;

            // 각도가 0 보다 작거나, 360 보다 크면 모두 360 도 내로 들어오도록 각도를 normalize 한다.
            if(angle < 0)
                angle = (angle+360)%360;
//            if(angleY < 0)
//                angleY = (angleY+360)%360;
            if(angle > 360)
                angle = angle % 360;
//            if(angleY > 360)
//                angleY = angleY % 360;

            // 사용자의 화면에 쿠폰이 나타나기 위해서는 x축과 y축 각도가 0~45 도 또는 315~360도 사이의 값이어야 한다.
            // 그 밖의 값에 대해서는 화면에서 사라지지 않고 화면의 가장자리에 위치하도록 각도를 조절해준다.
            if(angle > 44 && angle < 180) {
                angle = 44;
            }else if(angle < 316 && angle >= 180){
                angle = 316;
            }
//            if(angleY > 15 && angleY < 180){
//                angleY = 15;
//            }else if(angleY < 345 && angleY >= 180){
//                angleY = 345;
//            }

            // 실제 화면에서 쿠폰을 그려낼 위치를 screenWidth, screenHeight 를 이용하여 결정한다.
            double posInPx = angle * (screenWidth / 90d);
//            double posInPy = angleY * (screenHeight / 90d);

            int spotCentreX = spot.getWidth() / 2;
            int spotCentreY = spot.getHeight() / 2;
            xPos = posInPx - spotCentreX;
//            yPos = posInPy - spotCentreY;

            if (angle <= 45)
                u.x = (float) ((screenWidth / 2) + xPos);
            else if (angle >= 315)
                u.x = (float) ((screenWidth / 2) - ((screenWidth*4) - xPos));
            else
                u.x = (float) (float)(screenWidth*9);

//            if (angleY <= 45)
//                u.y = (float) ((screenHeight / 2) + yPos);
//            else if(angleY >= 315)
//                u.y = (float) ((screenHeight / 2) - ((screenHeight*4) - yPos));
//            else
//                u.y = (float) (float)(screenHeight*9);

            String s = String.valueOf(u.latitude).substring(7,8);
            int a = Integer.parseInt(s);

            if(a > 70){
                a -= 30;
            }else if(a < 30){
                a += 30;
            }

            float b = ((float)a)/100;
            u.y = (float) screenHeight*b;

            // 해당 위치에 spot 에 저장된 bitmap 을 그려낸다.
            canvas.drawBitmap(spot, u.x, u.y, mPaint);

            // 쿠폰과 사용자의 거리가 100미터 이내이면 해당 쿠폰의 좌표정보를 hashMap_touchLocation 에 저장하고,
            // 거리를 나타내는 Text 를 "터치!" 로 변경한다.
            // 거리가 100미터 보다 크다면 해당 거리를 Text 로 표시해준다.
            if(dist < 200){
                canvas.drawText("터치!", u.x + (spot.getWidth()/4), u.y, mPaint);
                ArrayList array = new ArrayList();

                array.add(u.x); array.add(u.y);
                hashMap_touchLocation.put(u.description, array);
            }else{
                canvas.drawText(String.valueOf(dist)+"m", u.x - (spot.getWidth()*2), u.y - spot.getHeight(), mPaint);
            }

        }
    }

    // 사용자 기기의 방향전환을 감지하여 OFFSET 변수에 저장하는 메소드
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

    // 사용자의 위치정보가 변경되었을 때 사용자의 위치정보를 가지고 있는 Point 클래스의 정보를 수정하는 메소드
    public void setMyLocation(double latitude, double longitude, double altitude) {
        me.latitude = latitude;
        me.longitude = longitude;

        Log.e("location switching", String.valueOf(latitude) + ", " + String.valueOf(longitude));
    }

    // Point 클래스 두개의 위치정보를 가져와 서로의 거리를 측정해주는 메소드
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

    // 두 점의 위치정보를 받아와 두 위치 간의 각도를 계산해주는 메소드
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



//    public DrawSurfaceView(Context c, Paint paint) {
//        super(c);
//    }




//    StringBuilder s = new StringBuilder();
//                s
//                        .append("\n")
//                        .append("터치 x 좌표 = ").append(x).append("\n")
//                        .append("터치 y 좌표 = ").append(y).append("\n")
//                        .append("x 좌표 = ").append(x1).append("\n")
//                        .append("y 좌표 = ").append(y1).append("\n");
//
//                        Log.e("위치 확인", s.toString());
