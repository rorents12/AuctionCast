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
    //현재 그리기 조건(색상, 굵기, 등등.)을 기억 하는 변수.
    private Paint paint = null;
    private Paint paint_eraser = null;

    //그리기를 할 bitmap 객체. -- 도화지라고 생각하면됨.
    private Bitmap bitmap = null;
    private Bitmap bitmap_previous = null;
    private utility_Bitmap_Stack history;

    //bitmap 객체의 canvas 객체. 실제로 그리기를 하기 위한 객체.. -- 붓이라고 생각하면됨.
    private Canvas canvas = null;

    //마우스 포인터(손가락)이 이동하는 경로 객체.
    private Path path;

    //마우스 포인터(손가락)이 가장 마지막에 위치한 x좌표값 기억용 변수.
    private float   oldX;

    //마우스 포인터(손가락)이 가장 마지막에 위치한 y좌표값 기억용 변수.
    private float   oldY;

    //지우개 선택여부
    private boolean bool_eraser = false;

    private PaintingClient client;
    private InetSocketAddress address;
    private String roomCode;
    private Painting_sendMessageUtil sendMessageUtil;

    private float height;
    private float width;

    PorterDuffXfermode clear = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    public PaintingDrawView_Publish(Context context, PaintingClient client, InetSocketAddress address, String roomCode){
//        super(context, set);
        this(context);
        this.client = client;
        this.address = address;
        this.roomCode = roomCode;

        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        width = dm.widthPixels;
        height = dm.heightPixels;

        sendMessageUtil = new Painting_sendMessageUtil();

        //그리기를 할 bitmap 객체 생성.
        bitmap = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
        bitmap_previous = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
        history = new utility_Bitmap_Stack();

        //그리기 bitmap에서 canvas를 알아옴.
        canvas = new Canvas(bitmap);

        //그림을 그리기 위한 펜의 Paint 객체 설정
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

        //그림을 지우기 위한 지우개의 Paint 객체 설정
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

    @Override
    protected void onDetachedFromWindow()
    {
        //앱 종료시 그리기 bitmap 초기화 시킴...
        if(bitmap!= null) bitmap.recycle();
        bitmap = null;

        super.onDetachedFromWindow();
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        //그리기 bitmap이 있으면 현재 화면에 bitmap을 그린다.
        //자바의 view는 onDraw할때 마다 화면을 싹 지우고 다시 그리게 됨.
        if(bitmap != null)
        {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
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


    //이벤트 처리용 함수..
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
            {
                bitmap_previous = Bitmap.createBitmap(bitmap);
                history.push(bitmap_previous);

                // 패킷 누락에 대비해 메시지를 2번 보냄
                try {
                    sendMessageUtil.sendMessage(
                            utility_global_variable.CODE_PAINT_BEFORE_PROGRESS, address, client, roomCode
                    );
                }catch (Exception e){
                    e.printStackTrace();
                }

                //최초 마우스를 눌렀을때(손가락을 댓을때) 경로를 초기화 시킨다.
                path.reset();

                //그다음.. 현재 경로로 경로를 이동 시킨다.
                path.moveTo(x, y);

                //포인터 위치값을 기억한다.
                oldX = x;
                oldY = y;

                invalidate();

                //계속 이벤트 처리를 하겠다는 의미.
                return true;
            }
            case MotionEvent.ACTION_MOVE:
            {

                //포인트가 이동될때 마다 두 좌표(이전에눌렀던 좌표와 현재 이동한 좌료)간의 간격을 구한다.
                float dx = Math.abs(x - oldX);
                float dy = Math.abs(y - oldY);

                //두 좌표간의 간격이 4px이상이면 (가로든, 세로든) 그리기 bitmap에 선을 그린다.
                if (dx >= 4 || dy >= 4)
                {
                    //path에 좌표의 이동 상황을 넣는다. 이전 좌표에서 신규 좌표로..
                    //lineTo를 쓸수 있지만.. 좀더 부드럽게 보이기 위해서 quadTo를 사용함.
                    path.quadTo(oldX, oldY, x, y);

                    try{
                        float x1 = oldX/width;
                        float y1 = oldY/height;
                        float x2 = x/width;
                        float y2 = y/height;

                        sendMessageUtil.sendMessage(
                                utility_global_variable.CODE_PAINT_PROGRESS, address, client,
                                x1, y1, x2, y2, paint.getColor(), roomCode
                        );
                    }catch (Exception e){
                        e.printStackTrace();
                    }



                    //포인터의 마지막 위치값을 기억한다.
                    oldX = x;
                    oldY = y;

                    //그리기 bitmap에 path를 따라서 선을 그린다.
                    canvas.drawPath(path, paint);
                }

                //화면을 갱신시킴.. 이 함수가 호출 되면 onDraw 함수가 실행됨.
                invalidate();

                //계속 이벤트 처리를 하겠다는 의미.
                return true;
            }
            case MotionEvent.ACTION_UP:
                oldX = 0;
                oldY = 0;

                invalidate();
                return false;
        }

        //더이상 이벤트 처리를 하지 않겠다는 의미.
        return false;
    }

    /**
     * 펜 색상 세팅
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

        if(color == Color.TRANSPARENT){
            paint.setXfermode(clear);
            paint.setStrokeWidth(80);
            paint.setColor(Color.TRANSPARENT);
        }
    }

    public int getLineColor(){
        return paint.getColor();
    }

    public void undo(){
        try {
            bitmap = Bitmap.createBitmap(history.pop());
            canvas = new Canvas(bitmap);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void clear(){
        history.clear();
        bitmap = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }



    public PaintingDrawView_Publish(Context context)
    {
        super(context);
    }
}
