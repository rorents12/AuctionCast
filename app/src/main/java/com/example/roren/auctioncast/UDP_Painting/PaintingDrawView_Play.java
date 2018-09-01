package com.example.roren.auctioncast.UDP_Painting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import com.example.roren.auctioncast.R;
import com.example.roren.auctioncast.utility.utility_Bitmap_Stack;
import com.example.roren.auctioncast.utility.utility_global_variable;

import java.net.InetSocketAddress;

/**
 *  시청자가 방송자로부터 그림그리기에 대한 UDP 패킷을 전달받아 그림을 그리는 View 클래스.
 */

public class PaintingDrawView_Play extends View
{
    // 현재 그리기 조건을 기억 하는 변수
    private Paint paint;

    // 그림을 그려낼 bitmap 클래스
    private Bitmap bitmap;
    // 실행취소 기능을 위해 새로운 그림을 그리기 전 이전의 bitmap 을 저장할 bitmap
    private Bitmap bitmap_previous;
    // 실행취소 기능을 위해 이전의 bitmap 들을 저장할 Stack. 최대 20개의 bitmap 을 저장할 수 있다.
    private utility_Bitmap_Stack history;

    // bitmap 에 그림을 그릴 canvas 클래스
    private Canvas canvas;

    // 그림을 그릴 경로를 저장할 path 클래스
    private Path path;

    // UDP 서버로의 소켓연결 역할을 하는 client
    private PaintingClient client;
    // UDP 서버의 주소
    private InetSocketAddress address;
    // 현재 사용자가 속한 UDP 서버 방의 roomCode
    private String roomCode;
    // UDP 서버로 메시지를 보낼 때 사용하는 util 클래스
    private Painting_sendMessageUtil sendMessageUtil;

    // 현재 사용자 기기 스크린의 가로 길이와 세로 길이를 저장할 변수
    private float height;
    private float width;

    // 지우개 기능을 사용하기 위해 정의한 Xfermode 클래스
    PorterDuffXfermode clear = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    public PaintingDrawView_Play(Context context, PaintingClient client, InetSocketAddress address, String roomCode){
        this(context);
        this.client = client;
        this.address = address;
        this.roomCode = roomCode;

        // 사용자 기기의 가로 길이와 세로 길이를 받아와 저장
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        width = dm.widthPixels;
        height = dm.heightPixels;

        // sendMessageUtil 클래스 초기화
        sendMessageUtil = new Painting_sendMessageUtil();

        // 그리기를 할 bitmap 객체 생성.
        bitmap = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);

        // 실행취소 기능을 위한 변수들의 초기화
        bitmap_previous = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
        history = new utility_Bitmap_Stack();

        // 그리기 bitmap 에서 canvas 를 받아와 저장
        canvas = new Canvas(bitmap);

        // paint 클래스 초기화
        paint = new Paint();

        //경로 초기화.
        path = new Path();
    }

//    @Override
//    protected void onDetachedFromWindow()
//    {
//        //앱 종료시 그리기 bitmap 초기화 시킴...
//        if(bitmap!= null) bitmap.recycle();
//        bitmap = null;
//
//        super.onDetachedFromWindow();
//    }

    /**
     *  invalidate() 함수가 호출되면 아래의 함수가 자동으로 호출된다.
     *  그림을 그려낸 bitmap 자체를 현재의 view 에 그려주는 역할을 한다.
     */
    @Override
    public void onDraw(Canvas canvas)
    {
        if(bitmap != null)
        {
            // bitmap 을 현재의 view 에 그려낸다.
            canvas.drawBitmap(bitmap, 0, 0, null);
        }

    }

    /**
     *  그림 그리기 메소드. color 와 시작점의 좌표값, 도착점의 좌표값을 parameter 로 받아
     *  bitmap 위에 그림을 그려낸다.
     */
    public void draw(int color, float x1, float y1, float x2, float y2){

        // 시작점과 도착점의 좌표값을 사용자의 기기에 맞게 normalize 한다.
        float oldX = width*x1;
        float oldY = height*y1;
        float x = width*x2;
        float y = height*y2;

        // path 를 reset 한다.
        path.reset();

        // path 의 시작점을 그려낼 시작점으로 이동하고 시작점과 도착점을 연결한다
        path.moveTo(oldX,oldY);
        path.quadTo(oldX, oldY, x, y);

        // paint 의 설정값을 설정한다
        paint.setColor(color);
        paint.setXfermode(null);
        paint.setAlpha(255);
        paint.setDither(true);
        paint.setStrokeWidth(10);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
        // color 값이 TRANSPARENT 일 경우, 지우개를 사용해야 하므로 StrokeWidth 를 늘리고
        // Xfermode 를 clear 로 설정한다
        if(color == Color.TRANSPARENT){
            paint.setStrokeWidth(80);
            paint.setXfermode(clear);
        }

        // bitmap 에 path 를 따라 그림을 그려낸다
        canvas.drawPath(path, paint);
    }

    /**
     *  현재의 bitmap 을 history 에 넣어 저장하는 메소드
     */
    public void savePainting(){
        bitmap_previous = Bitmap.createBitmap(bitmap);
        history.push(bitmap_previous);
    }

    /**
     *  history 에서 가장 최근의 bitmap 을 불러와 그림을 그릴 bitmap 으로 설정하는 메소드
     *  가장 최근에 실행된 그림그리기를 취소 할 수 있다.
     */
    public void undo(){
        try {
            bitmap = Bitmap.createBitmap(history.pop());
            canvas = new Canvas(bitmap);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *  현재 bitmap 에 그려진 그림을 모두 지우는 메소드
     */
    public void clear(){
        savePainting();
        bitmap = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    public PaintingDrawView_Play(Context context)
    {
        super(context);
    }

}
