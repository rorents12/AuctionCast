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
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.coremedia.iso.Hex;
import com.example.roren.auctioncast.R;
import com.example.roren.auctioncast.utility.utility_Bitmap_Stack;
import com.example.roren.auctioncast.utility.utility_global_variable;

import org.web3j.abi.datatypes.Array;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class PaintingDrawView_Publish extends View
{
    // 현재 그리기 조건을 기억 하는 변수
    private Paint paint = null;
    private Paint paint_eraser = null;

    // 그림을 그려낼 bitmap 클래스
    private Bitmap bitmap = null;
    // 실행취소 기능을 위해 새로운 그림을 그리기 전 이전의 bitmap 을 저장할 bitmap
    private Bitmap bitmap_previous = null;
    // 실행취소 기능을 위해 이전의 bitmap 들을 저장할 Stack. 최대 20개의 bitmap 을 저장할 수 있다.
    private utility_Bitmap_Stack history;

    // bitmap 에 그림을 그릴 canvas 클래스
    private Canvas canvas = null;

    // 그림을 그릴 경로를 저장할 path 클래스
    private Path path;

    // 마우스 포인터(손가락)이 가장 마지막에 위치한 x좌표값 기억용 변수.
    private float   oldX;
    // 마우스 포인터(손가락)이 가장 마지막에 위치한 y좌표값 기억용 변수.
    private float   oldY;


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

    public PaintingDrawView_Publish(Context context, PaintingClient client, InetSocketAddress address, String roomCode){
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

        // 그림을 그리기 위한 펜의 Paint 객체 설정
        paint = new Paint();
        paint.setColor(Color.BLACK);

        paint.setXfermode(null);

        paint.setAlpha(255);
        paint.setDither(true);
        paint.setStrokeWidth(10);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);

        // 그림을 지우기 위한 지우개의 Paint 객체 설정
        paint_eraser = new Paint();
        paint_eraser.setColor(Color.TRANSPARENT);

        paint_eraser.setXfermode(null);

        paint_eraser.setAlpha(255);
        paint_eraser.setDither(true);
        paint_eraser.setStrokeWidth(80);
        paint_eraser.setStrokeJoin(Paint.Join.ROUND);
        paint_eraser.setStyle(Paint.Style.STROKE);
        paint_eraser.setStrokeCap(Paint.Cap.ROUND);
        paint_eraser.setAntiAlias(true);

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
    public void onDraw(Canvas canvas) {
        if(bitmap != null)
        {
            // bitmap 을 현재의 view 에 그려낸다.
            canvas.drawBitmap(bitmap, 0, 0, null);
        }

        // 그림을 그리는 사용자가 사용하는 색상을 체크하고,
        // 펜을 사용하고 있다면 펜 아이콘을,
        // 지우개를 사용하고 있다면 지우개 아이콘을 띄워준다.
        if( oldX != 0 && oldY != 0){
            Bitmap b;
            if(paint.getColor() == Color.TRANSPARENT){
                b = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.eraser);
                canvas.drawBitmap(b, oldX - (b.getWidth()/4), oldY - b.getWidth() + (b.getWidth()/4), paint_eraser);
            }else {
                b = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.pen);
                canvas.drawBitmap(b, oldX - (b.getWidth()/8), oldY - b.getWidth(), paint);
            }
        }
    }


    /**
     *  사용자가 그림을 그리기 위하여 터치를 할 때, 해당 터치 이벤트를 감지해 상황에 따른 처리를 하는 eventListener
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

        // 사용자가 터치한 좌표 저장
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            // 사용자가 처음 화면에 손을 댔을 때
            case MotionEvent.ACTION_DOWN:
            {

                // 현재의 비트맵 정보를 history 에 저장
                bitmap_previous = Bitmap.createBitmap(bitmap);
                history.push(bitmap_previous);

                // 그림 그리기를 시작한다는 메시지를 UDP 서버로 전송
                try {
                    sendMessageUtil.sendMessage(
                            utility_global_variable.CODE_PAINT_BEFORE_PROGRESS, address, client, roomCode
                    );
                }catch (Exception e){
                    e.printStackTrace();
                }

                // 경로 초기화
                path.reset();

                // 해당 포인트로 path 시작점 이동
                path.moveTo(x, y);

                // 시작점 좌표값 저장
                oldX = x;
                oldY = y;

                invalidate();

                return true;
            }
            case MotionEvent.ACTION_MOVE:
            {

                // 포인트가 이동될때 마다 두 좌표간의 간격을 구한다.
                float dx = Math.abs(x - oldX);
                float dy = Math.abs(y - oldY);

                // 두 좌표간의 간격이 4px 이상이면 bitmap 에 선을 그린다.
                if (dx >= 4 || dy >= 4)
                {
                    // 시작점과 도착점을 연결
                    path.quadTo(oldX, oldY, x, y);

                    // 시작점과 도착점의 정보를 UDP 서버로 전송
                    try{

                        // 현재 사용자의 기기를 기준으로 좌표값을 normalize
                        float x1 = oldX/width;
                        float y1 = oldY/height;
                        float x2 = x/width;
                        float y2 = y/height;

                        // 메시지 전송
                        sendMessageUtil.sendMessage(
                                utility_global_variable.CODE_PAINT_PROGRESS, address, client,
                                x1, y1, x2, y2, paint.getColor(), roomCode
                        );
                    }catch (Exception e){
                        e.printStackTrace();
                    }



                    // 포인터의 마지막 위치값 저장
                    oldX = x;
                    oldY = y;

                    // bitmap 에 path 를 따라 그림을 그려낸다
                    canvas.drawPath(path, paint);
                }

                invalidate();

                return true;
            }
            case MotionEvent.ACTION_UP:
                oldX = 0;
                oldY = 0;

                invalidate();
                return false;
        }

        return false;
    }

    /**
     * 펜 색상, 굵기, 모양 세팅
     * @param color 색상
     */
    public void setLineColor(int color)
    {
        paint = new Paint();
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
            paint.setXfermode(clear);
            paint.setStrokeWidth(80);
            paint.setColor(Color.TRANSPARENT);
        }
    }

    /**
     *  현재 사용자가 사용하고 있는 색상의 번호를 반환하는 메소드
     */
    public int getLineColor(){
        return paint.getColor();
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
        bitmap_previous = Bitmap.createBitmap(bitmap);
        history.push(bitmap_previous);

        bitmap = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }



    public PaintingDrawView_Publish(Context context)
    {
        super(context);
    }
}
