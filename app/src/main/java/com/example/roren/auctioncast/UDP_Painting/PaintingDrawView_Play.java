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
import com.example.roren.auctioncast.R;
import com.example.roren.auctioncast.utility.utility_Bitmap_Stack;
import com.example.roren.auctioncast.utility.utility_global_variable;

import java.net.InetSocketAddress;

public class PaintingDrawView_Play extends View
{
    //현재 그리기 조건(색상, 굵기, 등등.)을 기억 하는 변수.
    private Paint paint = null;

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

    private boolean bool_eraser = false;

    private PaintingClient client;
    private InetSocketAddress address;
    private String roomCode;
    private Painting_sendMessageUtil sendMessageUtil;

    private float height;
    private float width;

    PorterDuffXfermode clear = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    public PaintingDrawView_Play(Context context, PaintingClient client, InetSocketAddress address, String roomCode){
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
        paint = new Paint();

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
        // 그림그리기용 투명 비트맵이 있을 때, 해당 비트맵을 그려준다.
        if(bitmap != null)
        {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }

    }


    public void draw(int color, float x1, float y1, float x2, float y2){

        float oldX = width*x1;
        float oldY = height*y1;
        float x = width*x2;
        float y = height*y2;

        path.reset();

        path.moveTo(oldX,oldY);

        path.quadTo(oldX, oldY, x, y);

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
            paint.setStrokeWidth(80);
            paint.setXfermode(clear);
        }

        canvas.drawPath(path, paint);
    }

    public void savePainting(){
        bitmap_previous = Bitmap.createBitmap(bitmap);
        history.push(bitmap_previous);
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

    public PaintingDrawView_Play(Context context)
    {
        super(context);
    }
}
