package com.example.roren.auctioncast.FaceDetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import com.example.roren.auctioncast.R;
import com.example.roren.auctioncast.utility.utility_global_variable;
import com.google.android.gms.vision.face.Face;

public class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float GENERIC_POS_OFFSET = 20.0f;
    private static final float GENERIC_NEG_OFFSET = -20.0f;

    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;
    private Bitmap bitmap;
    private Bitmap op;

    public FaceGraphic(GraphicOverlay overlay, int maskIndex) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

        switch (maskIndex){
            case utility_global_variable.AR_CAMERA_MASK_INDEX_PICA:
                bitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.pica);
                break;
            case utility_global_variable.AR_CAMERA_MASK_INDEX_EVEE:
                bitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.evee);
                break;
            case utility_global_variable.AR_CAMERA_MASK_INDEX_NAON:
                bitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.naon);
                break;
            case utility_global_variable.AR_CAMERA_MASK_INDEX_THOR:
                bitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.thor);
                break;
            case utility_global_variable.AR_CAMERA_MASK_INDEX_MASK:
                bitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.mask);
                break;
        }
//        bitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.pica);
        op = bitmap;
    }

    public void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    public void updateFace(Face face) {
        mFace = face;
        op = Bitmap.createScaledBitmap(bitmap, (int) scaleX(face.getWidth()),
                (int) scaleY(((bitmap.getHeight() * face.getWidth()) / bitmap.getWidth())), false);
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
//        canvas.drawRect(left, top, right, bottom, mBoxPaint);
        canvas.drawBitmap(op, left, top, new Paint());
    }

    private float getNoseAndMouthDistance(PointF nose, PointF mouth) {
        return (float) Math.hypot(mouth.x - nose.x, mouth.y - nose.y);
    }

    public void changeMask(int index){
        switch (index){
            case utility_global_variable.AR_CAMERA_MASK_INDEX_PICA:
                bitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.pica);
                break;
            case utility_global_variable.AR_CAMERA_MASK_INDEX_EVEE:
                bitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.evee);
                break;
            case utility_global_variable.AR_CAMERA_MASK_INDEX_NAON:
                bitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.naon);
                break;
            case utility_global_variable.AR_CAMERA_MASK_INDEX_THOR:
                bitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.thor);
                break;
            case utility_global_variable.AR_CAMERA_MASK_INDEX_MASK:
                bitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), R.drawable.mask);
                break;
        }
    }
}
