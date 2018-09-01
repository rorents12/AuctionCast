package com.example.roren.auctioncast.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.roren.auctioncast.FaceDetection.CameraSourcePreview;
import com.example.roren.auctioncast.FaceDetection.FaceGraphic;
import com.example.roren.auctioncast.FaceDetection.GraphicOverlay;
import com.example.roren.auctioncast.R;
import com.example.roren.auctioncast.utility.utility_global_variable;
import com.example.roren.auctioncast.utility.utility_http_uploadImage;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class activity_faceDetection extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "FaceTracker";

    // 카메라 화면을 관리하는 CameraSource 변수
    private CameraSource mCameraSource = null;
    // 카메라로부터 받아온 화면을 그려 사용자에게 보여주는 CameraSourcePreview 변수
    private CameraSourcePreview mPreview;
    // CameraSourcePreview 에서 인식한 얼굴 위에 마스크를 그려주는 GraphicOverlay 변수
    private GraphicOverlay mGraphicOverlay;

    // GMS 코드
    private static final int RC_HANDLE_GMS = 9001;

    // 사진 촬영 버튼 - 이 버튼을 통해 CameraSourcePreview 의 화면을 비트맵으로 변환한다.
    // 확인 버튼 - 이 버튼을 통해 찍은 사진을 서버로 전송하여 프로필 사진을 변경한다.
    private Button button_photo, button_confirm;

    // 사진을 촬영했을 때 찍힌 사진을 보여주는 ImageView
    private ImageView imageView_result;
    // 사진 촬영 버튼을 통해 변환된 비트맵을 저장하는 변수
    private Bitmap result;

    // 사진 촬영 버튼을 눌렀을 때 호출되는 callback method.
    // shutterCallback - 셔터 소리를 내주는 콜백메소드
    // pictureCallback - 찍힌 사진을 bytes array 로 변환하여 처리하는 콜백메소드
    private CameraSource.ShutterCallback shutterCallback;
    private CameraSource.PictureCallback pictureCallback;

    // 얼굴 인식을 한 후 해당 얼굴에 씌울 마스크를 나타내는 ImageView
    // 이들 중 하나를 터치하면 해당 마스크를 선택할 수 있다.
    private ImageView imageView_pica, imageView_evee, imageView_naon, imageView_thor, imageView_mask;
    // 특정 마스크를 선택하면 해당 마스크의 번호를 저장하는 변수
    // 이 변수를 통해 화면에 그릴 마스크를 결정한다.
    private int maskIndex;

    private String imagePath;

    /**--------------------------------------------------------------------------------------------
     * 각종 화면 UI의 변수를 초기화 하는 onCreate.
     ----------------------------------------------------------------------------------------------*/
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_facedetection);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        button_photo = findViewById(R.id.activity_faceDetection_button_photo);
        button_confirm = findViewById(R.id.activity_faceDetection_button_confirm);

        button_photo.setOnClickListener(this);
        button_confirm.setOnClickListener(this);

        imageView_result = findViewById(R.id.facedetect_image);
        imageView_pica = findViewById(R.id.activity_faceDetection_imageView_pica);
        imageView_evee = findViewById(R.id.activity_faceDetection_imageView_evee);
        imageView_naon = findViewById(R.id.activity_faceDetection_imageView_naon);
        imageView_thor = findViewById(R.id.activity_faceDetection_imageView_thor);
        imageView_mask = findViewById(R.id.activity_faceDetection_imageView_mask);

        imageView_pica.setOnClickListener(this);
        imageView_evee.setOnClickListener(this);
        imageView_naon.setOnClickListener(this);
        imageView_thor.setOnClickListener(this);
        imageView_mask.setOnClickListener(this);

        // ----------------------------------------------------------------------------------------
        // maskIndex 의 초기값은 global variable 에 정의되어 있고, 앱이 시작될 때 이 값은
        // AR_CAMERA_MASK_INDEX_PICA 와 같다.
        // ----------------------------------------------------------------------------------------
        maskIndex = utility_global_variable.AR_CAMERA_MASK_INDEX_DEFAULT;

        // ----------------------------------------------------------------------------------------
        // CameraSource 의 takePicture 함수를 호출하면 호출되는 shutterCallback 메소드
        // PictureCallback 메소드가 호출되기 전에 호출되므로, 찍힌 사진을 처리하기 전에 어떠한 행동을
        // 할것인지 정의한다. 기본적으로 셔텨 소리가 나도록 구현되어있다.
        // ----------------------------------------------------------------------------------------
        shutterCallback = new CameraSource.ShutterCallback() {
            @Override
            public void onShutter() {
            }
        };

        // ----------------------------------------------------------------------------------------
        // CameraSource 의 takePicture 함수를 호출하면 호출되는 pictureCallback 메소드
        // 내부의 onPictureTaken 이라는 메소드를 통해 byte 배열로 된 사진 이미지를 처리할 수 있다.
        //
        // 최종적으로 마스크가 씌워진 사진을 나타내려면, CameraSource 에서 가져온 사진과 Overlay 된
        // 사진을 겹쳐서 보여줘야 한다.
        // 따라서 byte 배열로 된 사진 위에 가면을 그려내어야 한다.
        // ----------------------------------------------------------------------------------------
        pictureCallback = new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes) {

                // byte 배열을 비트맵으로 변환
                result = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                // 사진의 회전값을 맞추기 위해 Matrix 변수 선언
                Matrix rotateMatrix = new Matrix();
                rotateMatrix.postRotate(-90);

                // 사진의 좌우반전을 맞추기 위해 Matrix 변수 선언
                Matrix sideInversionMatrix = new Matrix();
                sideInversionMatrix.setScale(-1,1);

                // 사진의 회전값과 좌우반전을 적용하여 새로운 비트맵 생성
                result = Bitmap.createBitmap(result, 0,0, result.getWidth(), result.getHeight(), rotateMatrix, false);
                result = Bitmap.createBitmap(result, 0,0, result.getWidth(), result.getHeight(), sideInversionMatrix, false);

                // 사진을 미리볼 수 있는 imageView 에 결과 비트맵을 삽입
                imageView_result.setImageBitmap(result);

                // CameraSourcePreView 의 캡쳐를 통해 현재 화면에 나타난 마스크를 비트맵으로 변환
                Bitmap mask = Bitmap.createScaledBitmap(mPreview.capture(), result.getWidth(), result.getHeight(), false);

                // CameraSource 에서 가져온 사진의 bitmap 위에 마스크를 그려낸다.
                Canvas canvas = new Canvas(result);
                Rect rect = new Rect(0,0,mask.getWidth(), mask.getHeight());
                canvas.drawBitmap(mask,null,rect, null);

                // 카메라 화면을 숨기고, 결과 이미지뷰의 Visibility 를 Visible 로 처리한다.
                mPreview.setVisibility(View.GONE);
                imageView_result.setVisibility(View.VISIBLE);
            }
        };

        // 카메라 화면을 초기화한다.
        createCameraSource();
    }

    /**--------------------------------------------------------------------------------------------
     * 촬영과 확인버튼, 그리고 각 마스크들을 나타내는 ImageView 들의 클릭이벤트를 정의한다.
     ----------------------------------------------------------------------------------------------*/
    @Override
    public void onClick(View view) {
        switch (view.getId()){

            // ------------------------------------------------------------------------------------
            // 촬영 버튼
            //
            // 버튼의 text 를 확인하여 '촬영' 일 때는 촬영을, '다시 찍기' 일때는 다시찍기 기능을
            // 수행한다.
            // ------------------------------------------------------------------------------------
            case R.id.activity_faceDetection_button_photo:

                // 촬영버튼의 text 확인
                String text = button_photo.getText().toString();

                if(text.equals("촬영")){
                    // 버튼의 text 가 '촬영' 일때

                    button_photo.setText("다시 찍기");
                    mCameraSource.takePicture(shutterCallback, pictureCallback);
                    button_confirm.setVisibility(View.VISIBLE);
                }else if(text.equals("다시 찍기")){
                    // 버튼의 text 가 '다시 찍기' 일때

                    // 현재 사용하고 있는 마스크의 번호를 global variable 에 저장
                    utility_global_variable.AR_CAMERA_MASK_INDEX_DEFAULT = maskIndex;

                    // 액티비티 재시작
                    this.recreate();
                }
                break;

            // ------------------------------------------------------------------------------------
            // 확인 버튼
            //
            // 버튼을 누르면, 찍힌 사진을 서버로 전송한 후 프로필사진을 변경한다.
            // ------------------------------------------------------------------------------------
            case R.id.activity_faceDetection_button_confirm:

                File storage = this.getFilesDir();
                String fileName = System.currentTimeMillis() + "tempCapture.jpg";
                File tempFile = new File(storage, fileName);

                try{
                    tempFile.createNewFile();
                    FileOutputStream out = new FileOutputStream(tempFile, false);
                    result.compress(Bitmap.CompressFormat.JPEG, 50, out);
                    out.close();

                }catch (Exception e){
                    e.printStackTrace();
                }

                imagePath = tempFile.getAbsolutePath();

                new utility_http_uploadImage(this, imagePath).execute();

                break;

            // ------------------------------------------------------------------------------------
            // 마스크 이미지뷰
            //
            // 각 마스크 이미지를 누르면 maskIndex 를 해당 마스크의 index 로 변경하고,
            // CameraSource 를 새로 시작한다.
            // ------------------------------------------------------------------------------------
            case R.id.activity_faceDetection_imageView_pica:
                maskIndex = utility_global_variable.AR_CAMERA_MASK_INDEX_PICA;
                createCameraSource();
                mPreview.stop();
                startCameraSource();
                break;
            case R.id.activity_faceDetection_imageView_evee:
                maskIndex = utility_global_variable.AR_CAMERA_MASK_INDEX_EVEE;
                createCameraSource();
                mPreview.stop();
                startCameraSource();
                break;
            case R.id.activity_faceDetection_imageView_naon:
                maskIndex = utility_global_variable.AR_CAMERA_MASK_INDEX_NAON;
                createCameraSource();
                mPreview.stop();
                startCameraSource();
                break;
            case R.id.activity_faceDetection_imageView_thor:
                maskIndex = utility_global_variable.AR_CAMERA_MASK_INDEX_THOR;
                createCameraSource();
                mPreview.stop();
                startCameraSource();
                break;
            case R.id.activity_faceDetection_imageView_mask:
                maskIndex = utility_global_variable.AR_CAMERA_MASK_INDEX_MASK;
                createCameraSource();
                mPreview.stop();
                startCameraSource();
                break;
        }
    }


    /**--------------------------------------------------------------------------------------------
     * CameraSource 를 만들고 시작한다.
     ----------------------------------------------------------------------------------------------*/
    private void createCameraSource() {

        Context context = getApplicationContext();

        // 얼굴 인식을 수행할 FaceDetector 생성
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();

        // FaceDetector 의 Processor 를 정의
        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        // detector 사용 가능여부 체크
        if (!detector.isOperational()) {
            // 이 앱이 처음 Face Detector API 를 사용한다면, GMS 가 face detection 에 필요한 native
            // library 를 다운로드 할 것이다. 보통은 앱이 실행되기 전에 이 다운로드가 끝날 것이지만,
            // 그렇지 않았을 경우에는 detector 가 얼굴 인식 기능을 수행할 수 없다. isOperational()
            // 함수를 통해 다운로드 완료 여부를 체크한다.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        // CameraSource 생성
        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }

    /**--------------------------------------------------------------------------------------------
     * onResume 콜백메소드 호출 시, CameraSource 를 재시작한다.
     ----------------------------------------------------------------------------------------------*/
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**--------------------------------------------------------------------------------------------
     * onPause 콜백메소드 호출 시, CameraSource 를 멈춘다.
     ----------------------------------------------------------------------------------------------*/
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**--------------------------------------------------------------------------------------------
     * onDestroy 콜백메소드 호출 시, CameraSource 를 release 한다.
     ----------------------------------------------------------------------------------------------*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**--------------------------------------------------------------------------------------------
     * 생성된 CameraSource 를 시작한다.
     * 카메라 화면을 CameraSourcePreview 에 출력한다.
     ----------------------------------------------------------------------------------------------*/
    private void startCameraSource() {

        // 디바이스가 google play services 를 사용가능한지 체크
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /**--------------------------------------------------------------------------------------------
     * 새로운 얼굴이 화면에 나타났을 때, 새로운 FaceTracker 를 생성해주는 FaceTracker Factory 클래스
     ----------------------------------------------------------------------------------------------*/
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay, maskIndex);
        }
    }

    /**--------------------------------------------------------------------------------------------
     * 인식된 얼굴을 추적하는 FaceTracker 클래스
     ----------------------------------------------------------------------------------------------*/
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay, int maskIndex) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay, maskIndex);
        }

        // ----------------------------------------------------------------------------------------
        // 얼굴을 찾아냈을 때, FaceGraphic 객체에 해당 얼굴을 등록한다.
        // ----------------------------------------------------------------------------------------
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        // ----------------------------------------------------------------------------------------
        // 얼굴 위치가 변경될 때 해당 변경사항을 FaceGraphic 객체에 업데이트 한다.
        // ----------------------------------------------------------------------------------------
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        // ----------------------------------------------------------------------------------------
        // Tracking 하던 얼굴이 사라졌을 때, 해당 얼굴 정보를 FaceGraphic 객체에서 삭제한다.
        // ----------------------------------------------------------------------------------------
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        // ----------------------------------------------------------------------------------------
        // Tracking 이 종료되면, 해당 얼굴 정보를 FaceGraphic 객체에서 삭제한다.
        // ----------------------------------------------------------------------------------------
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }

}
