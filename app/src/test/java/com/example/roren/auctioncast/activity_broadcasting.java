//package com.example.roren.auctioncast;
//
//import android.Manifest;
//import android.content.DialogInterface;
//import android.content.SharedPreferences;
//import android.content.pm.ActivityInfo;
//import android.content.pm.PackageManager;
//import android.content.res.Configuration;
//import android.hardware.Camera;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Environment;
//import android.os.Handler;
//import android.os.Message;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import com.github.faucamp.simplertmp.RtmpHandler;
//import com.seu.magicfilter.utils.MagicFilterType;
//
//import net.ossrs.yasea.SrsCameraView;
//import net.ossrs.yasea.SrsEncodeHandler;
//import net.ossrs.yasea.SrsPublisher;
//import net.ossrs.yasea.SrsRecordHandler;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.IOException;
//import java.net.SocketException;
//import java.util.ArrayList;
//import java.util.Random;
//
//import Utility.utility_http_DBQuery;
//import io.netty.bootstrap.Bootstrap;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelFuture;
//import io.netty.channel.EventLoopGroup;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import io.netty.handler.ssl.SslContext;
//import io.netty.handler.ssl.SslContextBuilder;
//import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
//
//public class activity_publishvideo extends AppCompatActivity implements RtmpHandler.RtmpListener, SrsRecordHandler.SrsRecordListener, SrsEncodeHandler.SrsEncodeListener{
//    private static final String TAG = "Yasea";
//
//    private int REQUEST_CAMERA;
//
//    private Button btnPublish;
//    private Button btnSwitchCamera;
//    private Button btnRecord;
//    private Button btnSwitchEncoder;
//    private Button btnSend;
//
//    private EditText editText_chat;
//
//    private String rtmpUrl;
//    private String recPath = Environment.getExternalStorageDirectory().getPath() + "/test.mp4";
//
//    private SrsPublisher mPublisher;
//
//    ////////////////////////////////////////////////////////////
//    private RecyclerView recyclerView_chatting;
//    private recyclerView_adapter_chatting adapter_broadcasting_chatting;
//    private ArrayList<recyclerView_item_chatting> items;
//    private chatting_messageParser parser;
//    private chatting_messageCompressor compressor;
//
//    static final String HOST = System.getProperty("host", "192.168.0.90");
//    static final int PORT = Integer.parseInt(System.getProperty("port", "5001"));
//
//    private Channel channel;
//
//    public static Handler handler;
//
//    public void receive_chatting(String string) throws Exception{
//
//        recyclerView_item_chatting item = new recyclerView_item_chatting();
//
//        item.setId(parser.getMessageId(string));
//        item.setText(parser.getMessageText(string));
//
//        adapter_broadcasting_chatting.addItem(item);
//        adapter_broadcasting_chatting.notifyDataSetChanged();
//
////        recyclerView_chatting.setAdapter(adapter_broadcasting_chatting);
//        recyclerView_chatting.scrollToPosition(adapter_broadcasting_chatting.getItemCount() - 1);
//
//        Log.e("채팅 개수", String.valueOf(adapter_broadcasting_chatting.getItemCount()));
//    }
//
//    ///////////////////////////////////////////////////////////
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
//            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
//        }else{
//            finish();
//        }
//
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        setContentView(R.layout.activity_publishvideo);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
//
//        /////////////////////////////////////////////////////////////////////////////////////
//
//        parser = new chatting_messageParser();
//        compressor = new chatting_messageCompressor();
//
//        handler = new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//                switch (msg.what){
//                    case 1:
//                        try{
//                            Log.e("핸들러 테스트", msg.obj.toString());
//                            receive_chatting(msg.obj.toString());
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//                }
//            }
//        };
//
//        editText_chat = findViewById(R.id.activity_broadcasting_editText_chatText);
//
//        btnSend = (Button) findViewById(R.id.activity_broadcasting_button_send);
//
//        btnSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try{
//                    if(!editText_chat.getText().toString().equals("")) {
//                        // 전송 버튼을 누르면 해당 텍스트를 서버로 전송
//                        sendMessage("chatting");
//                    }
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        recyclerView_chatting = findViewById(R.id.recyclerView_container_broadcasting_chatting);
//        recyclerView_chatting.setLayoutManager(new LinearLayoutManager(this));
//
//        items = new ArrayList<>();
//        adapter_broadcasting_chatting = new recyclerView_adapter_chatting(this);
//
//        recyclerView_chatting.setAdapter(adapter_broadcasting_chatting);
//        //////////////////////////////////////////////////////////////////////////////////////
//
//        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
//
//        if(permissionCheck == PackageManager.PERMISSION_DENIED){
//            Log.e("퍼미션퍼미션!", "퍼미션 허용 안돼있음");
//
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA);
//
//        }else{
//            Log.e("퍼미션퍼미션!", "퍼미션 허용 돼있음");
//
////            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
////            setContentView(R.layout.activity_publishvideo);
//
//            // response screen rotation event
////            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
//
//            rtmpUrl  = "rtmp://52.41.99.92/mytv/" + getIntent().getStringExtra("streamer_id");
//
//            // initialize url.
//            final EditText efu = (EditText) findViewById(R.id.url);
//            efu.setText(rtmpUrl);
//
//            btnPublish = (Button) findViewById(R.id.publish);
//            btnSwitchCamera = (Button) findViewById(R.id.swCam);
//            btnRecord = (Button) findViewById(R.id.record);
//            btnSwitchEncoder = (Button) findViewById(R.id.swEnc);
//
//            btnRecord.setVisibility(View.GONE);
//            btnSwitchEncoder.setVisibility(View.GONE);
//            efu.setVisibility(View.GONE);
//
//            mPublisher = new SrsPublisher((SrsCameraView) findViewById(R.id.glsurfaceview_camera));
//            mPublisher.setEncodeHandler(new SrsEncodeHandler(this));
//            mPublisher.setRtmpHandler(new RtmpHandler(this));
//            mPublisher.setRecordHandler(new SrsRecordHandler(this));
//            mPublisher.setPreviewResolution(640, 360);
//            mPublisher.setOutputResolution(360, 640);
//            mPublisher.setVideoHDMode();
//            mPublisher.startCamera();
//
//            btnPublish.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (btnPublish.getText().toString().contentEquals("publish")) {
//
//                        // 방송제목 입력 다이얼로그 띄우기
//
//                        AlertDialog.Builder ad = new AlertDialog.Builder(activity_publishvideo.this);
//
//                        ad.setTitle("방송 제목");
//                        ad.setMessage("방송의 제목을 입력하세요.");
//
//                        final EditText et = new EditText(activity_publishvideo.this);
//                        ad.setView(et);
//
//                        ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                   if(et.getText().toString().equals("")){
//                                      Toast.makeText(activity_publishvideo.this, "방송 제목을 입력해야합니다.", Toast.LENGTH_SHORT);
//                                   }else{
//                                       // 방송 송출을 시작하면 소켓에 연결 후, 채팅 채널을 만든다.
//                                       EventLoopGroup group = new NioEventLoopGroup();
//
//                                       try{
//                                           final SslContext sslCtx = SslContextBuilder.forClient()
//                                                   .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
//
//                                           Bootstrap bootstrap = new Bootstrap();
//                                           bootstrap.group(group)
//                                                   .channel(NioSocketChannel.class)
//                                                   .handler(new chatting_client_initializer(sslCtx));
//
//                                           channel = bootstrap.connect(HOST, PORT).sync().channel();
//
//                                           //방송자 최초 연결 시 방만들기를 위해 서버에게 메시지를 보낸다
//                                           sendMessage("makeRoom");
//
//                                       }catch (Exception e){
//                                           e.printStackTrace();
//                                       }
//
//                                       // 방송 송출 코드
//                                       rtmpUrl = efu.getText().toString();
//
//                                       mPublisher.startPublish(rtmpUrl);
//                                       mPublisher.startCamera();
//
//                                       if (btnSwitchEncoder.getText().toString().contentEquals("soft encoder")) {
//                                           Toast.makeText(getApplicationContext(), "Use hard encoder", Toast.LENGTH_SHORT).show();
//                                       } else {
//                                           Toast.makeText(getApplicationContext(), "Use soft encoder", Toast.LENGTH_SHORT).show();
//                                       }
//                                       btnPublish.setText("stop");
//
//                                       //http 통신을 통해 방송정보 업로드
//
//                                        new utility_http_DBQuery().execute("insert into table_broadcasting_list (title, broadcaster_id, broadcast_start_time)" +
//                                                " values ('" + et.getText().toString() + "','" + getIntent().getStringExtra("streamer_id") + "','" + "a" + "');");
//
//                                       btnSwitchEncoder.setEnabled(false);
//                                   }
//                            }
//                        });
//
//                        ad.setNeutralButton("취소", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                dialogInterface.dismiss();
//                            }
//                        });
//
//                        ad.show();
//
//                    } else if (btnPublish.getText().toString().contentEquals("stop")) {
//                        mPublisher.stopPublish();
//                        mPublisher.stopRecord();
//                        btnPublish.setText("publish");
//                        btnRecord.setText("record");
//                        btnSwitchEncoder.setEnabled(true);
//
//                        try{
//                            sendMessage("exitRoom");
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//
//                        new utility_http_DBQuery().execute("delete from table_broadcasting_list where broadcaster_id = '" + getIntent().getStringExtra("streamer_id") + "'");
//                    }
//                }
//            });
//
//            btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mPublisher.switchCameraFace((mPublisher.getCamraId() + 1) % Camera.getNumberOfCameras());
//                }
//            });
//
//            btnRecord.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (btnRecord.getText().toString().contentEquals("record")) {
//                        if (mPublisher.startRecord(recPath)) {
//                            btnRecord.setText("pause");
//                        }
//                    } else if (btnRecord.getText().toString().contentEquals("pause")) {
//                        mPublisher.pauseRecord();
//                        btnRecord.setText("resume");
//                    } else if (btnRecord.getText().toString().contentEquals("resume")) {
//                        mPublisher.resumeRecord();
//                        btnRecord.setText("pause");
//                    }
//                }
//            });
//
//            btnSwitchEncoder.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (btnSwitchEncoder.getText().toString().contentEquals("soft encoder")) {
//                        mPublisher.switchToSoftEncoder();
//                        btnSwitchEncoder.setText("hard encoder");
//                    } else if (btnSwitchEncoder.getText().toString().contentEquals("hard encoder")) {
//                        mPublisher.switchToHardEncoder();
//                        btnSwitchEncoder.setText("soft encoder");
//                    }
//                }
//            });
//        }
//
//
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        } else {
//            switch (id) {
//                case R.id.cool_filter:
//                    mPublisher.switchCameraFilter(MagicFilterType.COOL);
//                    break;
//                case R.id.beauty_filter:
//                    mPublisher.switchCameraFilter(MagicFilterType.BEAUTY);
//                    break;
//                case R.id.early_bird_filter:
//                    mPublisher.switchCameraFilter(MagicFilterType.EARLYBIRD);
//                    break;
//                case R.id.evergreen_filter:
//                    mPublisher.switchCameraFilter(MagicFilterType.EVERGREEN);
//                    break;
//                case R.id.n1977_filter:
//                    mPublisher.switchCameraFilter(MagicFilterType.N1977);
//                    break;
//                case R.id.nostalgia_filter:
//                    mPublisher.switchCameraFilter(MagicFilterType.NOSTALGIA);
//                    break;
//                case R.id.romance_filter:
//                    mPublisher.switchCameraFilter(MagicFilterType.ROMANCE);
//                    break;
//                case R.id.sunrise_filter:
//                    mPublisher.switchCameraFilter(MagicFilterType.SUNRISE);
//                    break;
//                case R.id.sunset_filter:
//                    mPublisher.switchCameraFilter(MagicFilterType.SUNSET);
//                    break;
//                case R.id.tender_filter:
//                    mPublisher.switchCameraFilter(MagicFilterType.TENDER);
//                    break;
//                case R.id.toast_filter:
//                    mPublisher.switchCameraFilter(MagicFilterType.TOASTER2);
//                    break;
//                case R.id.valencia_filter:
//                    mPublisher.switchCameraFilter(MagicFilterType.VALENCIA);
//                    break;
//                case R.id.walden_filter:
//                    mPublisher.switchCameraFilter(MagicFilterType.WALDEN);
//                    break;
//                case R.id.warm_filter:
//                    mPublisher.switchCameraFilter(MagicFilterType.WARM);
//                    break;
//                case R.id.original_filter:
//                default:
//                    mPublisher.switchCameraFilter(MagicFilterType.NONE);
//                    break;
//            }
//        }
//        setTitle(item.getTitle());
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        final Button btn = (Button) findViewById(R.id.publish);
//        btn.setEnabled(true);
//        mPublisher.resumeRecord();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mPublisher.pauseRecord();
//    }
//
//    @Override
//    protected void onDestroy(){
//        super.onDestroy();
//        mPublisher.stopPublish();
//        mPublisher.stopRecord();
//
//        try{
//            sendMessage("exitRoom");
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        new utility_http_DBQuery().execute("delete from table_broadcasting_list where broadcaster_id = '" + getIntent().getStringExtra("streamer_id") + "'");
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        mPublisher.stopEncode();
//        mPublisher.stopRecord();
//        btnRecord.setText("record");
//        mPublisher.setScreenOrientation(newConfig.orientation);
//        if (btnPublish.getText().toString().contentEquals("stop")) {
//            mPublisher.startEncode();
//        }
//        mPublisher.startCamera();
//    }
//
//    private static String getRandomAlphaString(int length) {
//        String base = "abcdefghijklmnopqrstuvwxyz";
//        Random random = new Random();
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < length; i++) {
//            int number = random.nextInt(base.length());
//            sb.append(base.charAt(number));
//        }
//        return sb.toString();
//    }
//
//    private static String getRandomAlphaDigitString(int length) {
//        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
//        Random random = new Random();
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < length; i++) {
//            int number = random.nextInt(base.length());
//            sb.append(base.charAt(number));
//        }
//        return sb.toString();
//    }
//
//    private void handleException(Exception e) {
//        try {
//            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
//            mPublisher.stopPublish();
//            mPublisher.stopRecord();
//            btnPublish.setText("publish");
//            btnRecord.setText("record");
//            btnSwitchEncoder.setEnabled(true);
//        } catch (Exception e1) {
//            //
//        }
//    }
//
//    // Implementation of SrsRtmpListener.
//
//    @Override
//    public void onRtmpConnecting(String msg) {
//        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onRtmpConnected(String msg) {
//        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onRtmpVideoStreaming() {
//    }
//
//    @Override
//    public void onRtmpAudioStreaming() {
//    }
//
//    @Override
//    public void onRtmpStopped() {
//        Toast.makeText(getApplicationContext(), "방송 종료", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onRtmpDisconnected() {
////        Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onRtmpVideoFpsChanged(double fps) {
//        Log.i(TAG, String.format("Output Fps: %f", fps));
//    }
//
//    @Override
//    public void onRtmpVideoBitrateChanged(double bitrate) {
//        int rate = (int) bitrate;
//        if (rate / 1000 > 0) {
//            Log.i(TAG, String.format("Video bitrate: %f kbps", bitrate / 1000));
//        } else {
//            Log.i(TAG, String.format("Video bitrate: %d bps", rate));
//        }
//    }
//
//    @Override
//    public void onRtmpAudioBitrateChanged(double bitrate) {
//        int rate = (int) bitrate;
//        if (rate / 1000 > 0) {
//            Log.i(TAG, String.format("Audio bitrate: %f kbps", bitrate / 1000));
//        } else {
//            Log.i(TAG, String.format("Audio bitrate: %d bps", rate));
//        }
//    }
//
//    @Override
//    public void onRtmpSocketException(SocketException e) {
//        handleException(e);
//    }
//
//    @Override
//    public void onRtmpIOException(IOException e) {
//        handleException(e);
//    }
//
//    @Override
//    public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
//        handleException(e);
//    }
//
//    @Override
//    public void onRtmpIllegalStateException(IllegalStateException e) {
//        handleException(e);
//    }
//
//    // Implementation of SrsRecordHandler.
//
//    @Override
//    public void onRecordPause() {
//        Toast.makeText(getApplicationContext(), "Record paused", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onRecordResume() {
//        Toast.makeText(getApplicationContext(), "Record resumed", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onRecordStarted(String msg) {
//        Toast.makeText(getApplicationContext(), "Recording file: " + msg, Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onRecordFinished(String msg) {
//        Toast.makeText(getApplicationContext(), "MP4 file saved: " + msg, Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onRecordIOException(IOException e) {
//        handleException(e);
//    }
//
//    @Override
//    public void onRecordIllegalArgumentException(IllegalArgumentException e) {
//        handleException(e);
//    }
//
//    // Implementation of SrsEncodeHandler.
//
//    @Override
//    public void onNetworkWeak() {
//        Toast.makeText(getApplicationContext(), "네트워크 상태가 불안정합니다.", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onNetworkResume() {
//        Toast.makeText(getApplicationContext(), "네트워크가 다시 연결되었습니다.", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {
//        handleException(e);
//    }
//
//    public void sendMessage(String code) throws Exception{
//
//        switch(code){
//            case "makeRoom":
//                String m1 = compressor.getJSONObjectToString(utility_global_variable.CODE_CHAT_MAKEROOM, activity_login.user_id, "방만들기");
//                channel.writeAndFlush(m1);
//                break;
//            case "exitRoom":
//                String m2 = compressor.getJSONObjectToString(utility_global_variable.CODE_CHAT_EXIT, activity_login.user_id, "방나가기", activity_login.user_id);
//                channel.writeAndFlush(m2);
//                break;
//            case "chatting":
//                String m3 = compressor.getJSONObjectToString(utility_global_variable.CODE_CHAT_MESSAGE_GENERAL, activity_login.user_id, editText_chat.getText().toString(), activity_login.user_id);
//                editText_chat.setText("");
//                receive_chatting(m3);
//                channel.writeAndFlush(m3);
//                break;
//        }
//
//    }
//}
