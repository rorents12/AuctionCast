package com.example.roren.auctioncast.activities;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roren.auctioncast.UDP_Painting.PaintingClient;
import com.example.roren.auctioncast.UDP_Painting.PaintingDrawView_Publish;
import com.example.roren.auctioncast.UDP_Painting.PaintingReceiver;
import com.example.roren.auctioncast.UDP_Painting.Painting_sendMessageUtil;
import com.example.roren.auctioncast.chatting.chatting_client_receiver;
import com.example.roren.auctioncast.chatting.chatting_client_initializer;
//import com.example.roren.auctioncast.chatting.chatting_messageCompressor;
//import com.example.roren.auctioncast.chatting.chatting_messageParser;
import com.example.roren.auctioncast.chatting.chatting_utility;
import com.example.roren.auctioncast.utility.utility_global_variable;
import com.example.roren.auctioncast.utility.utility_http_DBQuery;
import com.github.faucamp.simplertmp.RtmpHandler;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.seu.magicfilter.utils.MagicFilterType;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.CharsetUtil;

import com.example.roren.auctioncast.R;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 방송자가 방송을 시작할 수 있는 액티비티
 *
 * RTMP 프로토콜을 통해 동영상을 스트리밍 서버로 전송하는 부분과, netty 를 이용하여 채팅과 경매를 진행하는 부분으로 나뉜다.
 *
 * 1. RTMP 스트리밍
 * 방송 시작 버튼을 누르면 서버 DB 에 방송목록 정보를 생성하고, 해당 정보에서 방송 고유번호를 가져와 RTMP streaming 의 url 로 설정한다.
 *
 * 2. 채팅과 경매 시스템
 * netty 서버와의 소켓 연결을 통해 채팅과 경매를 진행한다.
 * 이 activity 에서의 여러 행동들은 sendMessage method 를 통해 각각 netty 서버로 message 를 보내거나 받게 된다.
 * 각 행동들은 고유의 code 를 포함한 message 를 전송하며, 고유 code 는 다음과 같다.
 *
 *      방송자가 서버로 보내는 message code
 *          1) 방송 시작
 *              code -> utility_global_variable.CODE_CHAT_MAKEROOM
 *              서버에서 해당 code 를 파싱하여 새로운 채팅방 세션을 만든다.
 *          2) 채팅 메시지 보내기
 *              code -> utility_global_variable.CODE_CHAT_MESSAGE_GENERAL
 *              서버에서 해당 code 를 파싱하여 사용자가 참여한 채팅방의 다른 사용자들에게 메시지를 전송한다.
 *          3) 경매 시작
 *              code -> utility_global_variable.CODE_CHAT_AUCTION_START
 *              서버에서 해당 code 를 파싱하여 사용자가 참여한 채팅방의 다른 사용자들에게 경매 시작 신호를 전송한다.
 *          4) 경매 종료
 *              code -> utility_global_variable.CODE_CHAT_AUCTION_STOP
 *              서버에서 해당 code 를 파싱하여 사용자가 참여한 채팅방의 다른 사용자들에게 경매 종료 신호를 전송한다.
 *
 *      방송자가 서버로부터 받는 message code
 *          chatting_client_receiver 에서 받은 message 를 이 activity 의 handler 로 보내고, handler 에서 receive_chatting method 를 이용하여
 *          각 code 에 맞는 처리를 진행한다.
 *
 *          1) 채팅 메시지 받기
 *              code -> utility_global_variable.CODE_CHAT_MESSAGE_GENERAL
 *          2) 경매 입찰 정보
 *              code -> utility_global_variable.CODE_CHAT_PRICE_RAISE
 *
 **/

public class activity_publishVideo extends AppCompatActivity implements RtmpHandler.RtmpListener, SrsRecordHandler.SrsRecordListener, SrsEncodeHandler.SrsEncodeListener{

    /**
     * RTMP 송출을 위한 변수
     */
    private static final String TAG = "Yasea";

    private Button btnPublish;
    private Button btnSwitchCamera;

    private String rtmpUrl;

    private SrsPublisher mPublisher;

    /**
     * 채팅을 위한 변수
     */

    // 채팅 UI를 위한 View 변수
    private EditText editText_chat;
    private Button btnSend;

    // 채팅을 표시하는 recyclerView의 변수
    private RecyclerView recyclerView_chatting;
    private recyclerView_adapter_chatting adapter_broadcasting_chatting;
    private ArrayList<recyclerView_item_chatting> items;

    // 채팅 메시지를 parsing, compressing, sending 하는 클래스.
    private chatting_utility chattingUtility;

    // netty 채팅 서버와 연결했을 때 해당 서버와 연결을 지속하게 해주고, 메시지를 주고 받을 수 있게 해주는 클래스.
    private Channel channel;

    // netty 채팅 서버에서 메시지가 왔을 때 해당 메시지를 받아주는 receiver, receiver로부터 메시지를 전달받아 recyclerView를 업데이트 하는 handler.
    private Handler handler;
    private chatting_client_receiver receiver;

    // netty 채팅 서버에 접속할 때, 사용자가 속한 방에 대한 정보를 나타내는 변수
    private String roomCode;

    /**
     * 경매 시스템을 위한 변수
     */
    // 경매 시작 버튼
    private Button btnStartAuction;
    // 입찰 버튼
    private Button btnBid;

    // 호가를 1000원 올릴 수 있는 호가 조정 버튼
    private ImageButton btnBidUp;
    // 호가를 1000원 내릴 수 있는 호가 조정 버튼
    private ImageButton btnBidDown;

    // 현재 입찰가를 나타내는 텍스트뷰
    private TextView textView_priceNow;
    // 현재 입찰자를 나타내는 텍스트뷰
    private TextView textView_bidder;
    // 현재 사용자가 입찰할 호가를 나타내는 텍스트뷰
    private TextView textView_priceBid;

    // 경매 UI 를 담고있는 linearLayout. 경매가 시작되면 Visibility 를 VISIBLE 로 변경하여 화면에 나타낸다
    private LinearLayout layout_auction;

    // 현재 입찰가를 저장하는 변수. 사용자가 입찰 버튼을 눌렀을 때, 사용자가 결정한 호가가 현재 입찰가보다
    // 작은지 큰지를 비교할 때 사용한다.
    private int priceNow;

    /**
     * 그림 그리기를 위한 변수
     */
    // 그림을 그릴 수 있는 뷰 클래스. 그림을 그리기 위한 여러 메소드들이 정의되어 있다.
    private PaintingDrawView_Publish paintingDrawView;

    // 그림 그리기 시작 버튼
    private Button btnPainting;

    // 색상 변경 버튼. 버튼을 클릭하면 현재 사용하는 색깔로 버튼의 색상이 변경되며,
    // 색상은 검정, 노랑, 빨강, 초록, 파랑 순으로 변경된다.
    private Button btnChangeColor;

    // 지우개 버튼. 이 버튼을 터치하면 그리기 도구가 지우개로 변경된다.
    private Button btnEraser;

    // 실행취소 버튼. 최근에 실행한 그림에 관한 행동들을 취소할 수 있다. 최근 20번의 행동까지 취소가 가능하다.
    private Button btnUndo;
    // 모두 지우기 버튼. 그림을 초기화한다.
    private Button btnClear;

    // 현재 사용자가 사용하고 있는 색상의 번호를 저장하는 변수. 색상을 변경할 때, 현재 사용하고 있는 색상이
    // 무엇인지를 확인하기 위해 사용한다.
    private int colorNum;

    // 서버와 UDP 통신을 하기 위한 UDP client 클래스
    private PaintingClient paintingClient;
    private ChannelFuture paintingChannelFuture;

    // 서버로부터 온 UDP 패킷을 받아 처리하는 Receiver 클래스와 해당 패킷의 종류에 따라
    // paintingDrawView_play 와 상호작용하는 handler 클래스
    private Handler paintingHandler;
    private PaintingReceiver paintingReceiver;

    // UDP 통신을 위해 여러 parameter 를 받아 json 객체로 만들고, 해당 객체를 String 형태로
    // UDP 서버로 보내주는 역할을 하는 클래스
    private Painting_sendMessageUtil painting_sendMessageUtil;

    // UDP 서버의 주소 정보를 저장하는 클래스
    private InetSocketAddress UDP_server_address;

    // PaintingDrawView_Play 뷰 클래스를 위치시킬 linearLayout.
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_publishvideo);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        /**
         * 그림 그리기를 위한 세팅
         */

        // PaintingDrawView_Play 뷰 클래스를 위치시킬 linearLayout 을 선언한다.
        linearLayout = findViewById(R.id.activity_broadcasting_LinearLayout);

        // painting_sendMessageUtil 클래스 초기화
        painting_sendMessageUtil = new Painting_sendMessageUtil();

        // UDP 서버의 주소 정보를 저장
        UDP_server_address = new InetSocketAddress(utility_global_variable.HOST, utility_global_variable.PORT_UDP);

        // UDP 서버로부터 온 정보를 처리하는 핸들러. receive_painting 메소드를 이용하여 메시지를 파싱하여
        // 상황에 맞게 처리한다.
        paintingHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 1:
                        try{
                            receive_painting(msg.obj.toString());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                }
            }
        };

        // UDP 서버로부터 온 패킷을 처리하는 리시버
        paintingReceiver = new PaintingReceiver(paintingHandler);

        // 그림그리기 UI 의 버튼들을 초기화
        btnPainting = findViewById(R.id.activity_broadcasting_button_painting);
        btnChangeColor = findViewById(R.id.activity_broadcasting_button_colorChange);
        btnEraser = findViewById(R.id.activity_broadcasting_button_eraser);
        btnUndo = findViewById(R.id.activity_broadcasting_button_undo);
        btnClear = findViewById(R.id.activity_broadcasting_button_clear);

        // 그림 그리기 UI 의 버튼들의 Visibility 를 GONE 으로 변경한다.
        // 그림 그리기 버튼을 누르면 해당 UI 들의 Visibility 를 VISIBLE 로 변경한다.
        btnPainting.setVisibility(View.GONE);
        btnEraser.setVisibility(View.GONE);
        btnChangeColor.setVisibility(View.GONE);
        btnChangeColor.setBackgroundColor(Color.BLACK);
        btnUndo.setVisibility(View.GONE);
        btnClear.setVisibility(View.GONE);

        // 사용 색상의 초기값을 BLACK 으로 설정.
        colorNum = Color.BLACK;

        // 그림 그리기 버튼에 OnClickListener 를 세팅한다.
        // 그림그리기 버튼을 클릭하면 TCP 소켓을 이용해 채팅방에 있는 인원에게 그림그리기 시작 알림을 보내고,
        // 버튼의 text 를 "중단" 으로 변경한다.
        // 만약 버튼의 text 가 "중단" 일 때 버튼을 클릭하면 TCP 소켓을 이용해 채팅방의 인원들에게 그림그리기 중단 알림을 보낸다.
        btnPainting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnPainting.getText().toString().equals("그림그리기")) {
                    try {
                        btnPainting.setText("중단");
                        chattingUtility.sendMessage(
                                utility_global_variable.CODE_CHAT_START_PAINTING,
                                channel,
                                activity_login.user_id,
                                "그림그리기 시작",
                                roomCode
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    //그림그리기 중단
                    btnPainting.setText("그림그리기");
                    try{
                        chattingUtility.sendMessage(
                                utility_global_variable.CODE_CHAT_STOP_PAINTING,
                                channel,
                                activity_login.user_id,
                                "그림그리기 중단",
                                roomCode
                        );
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        // 지우개 버튼의 OnClickListener.
        // paintingDrawView 의 색상변경 메소드를 이용하여 색상을 TRANSPARENT 로 변경.
        btnEraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paintingDrawView.setLineColor(Color.TRANSPARENT);
            }
        });

        // 색상 변경 버튼의 OnClickListener.
        // 버튼을 클릭할 때마다 현재 색상의 다음 색상으로 변경한다.
        // 순서는 검정 - 노랑 - 빨강 - 초록 - 파랑 순이다.
        btnChangeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(paintingDrawView.getLineColor() == Color.TRANSPARENT){
                    paintingDrawView.setLineColor(colorNum);
                }else{
                    switch (colorNum){
                        case Color.BLACK:
                            colorNum = Color.YELLOW;
                            btnChangeColor.setBackgroundColor(colorNum);
                            btnChangeColor.setTextColor(Color.BLACK);
                            paintingDrawView.setLineColor(colorNum);
                            break;
                        case Color.YELLOW:
                            colorNum = Color.RED;
                            btnChangeColor.setBackgroundColor(colorNum);
                            btnChangeColor.setTextColor(Color.BLACK);
                            paintingDrawView.setLineColor(colorNum);
                            break;
                        case Color.RED:
                            colorNum = Color.GREEN;
                            btnChangeColor.setBackgroundColor(colorNum);
                            btnChangeColor.setTextColor(Color.BLACK);
                            paintingDrawView.setLineColor(colorNum);
                            break;
                        case Color.GREEN:
                            colorNum = Color.BLUE;
                            btnChangeColor.setBackgroundColor(colorNum);
                            btnChangeColor.setTextColor(Color.BLACK);
                            paintingDrawView.setLineColor(colorNum);
                            break;

                        case Color.BLUE:
                            colorNum = Color.BLACK;
                            btnChangeColor.setBackgroundColor(colorNum);
                            btnChangeColor.setTextColor(Color.WHITE);
                            paintingDrawView.setLineColor(colorNum);
                            break;
                    }
                }
            }
        });

        // 실행취소 버튼을 누르면 paintingDrawView 의 실행취소 메소드를 실행하고,
        // UDP 통신을 통해 실행취소 기능이 실행되었다는 메시지를 보낸다.
        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 실행취소 메소드 실행
                paintingDrawView.undo();
                paintingDrawView.invalidate();
                try{
                    // UDP 메시지 전송
                    painting_sendMessageUtil.sendMessage(
                            utility_global_variable.CODE_PAINT_UNDO, UDP_server_address, paintingClient, roomCode
                    );
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        // 모두지우기 버튼을 누르면 paintingDrawView 의 모두지우기 메소드를 실행하고,
        // UDP 통신을 통해 모두지우기 기능이 실행되었다는 메시지를 보낸다.
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 모두지우기 메소드 실행
                paintingDrawView.clear();
                paintingDrawView.invalidate();
                try{
                    // UDP 메시지 전송
                    painting_sendMessageUtil.sendMessage(
                            utility_global_variable.CODE_PAINT_CLEAR, UDP_server_address, paintingClient, roomCode
                    );
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        /**
         * 경매 시스템을 위한 세팅
         */
        textView_priceNow = findViewById(R.id.auction_textView_priceNow);
        textView_bidder = findViewById(R.id.auction_textView_bidder);
        textView_priceBid = findViewById(R.id.auction_textView_priceBid);

        layout_auction = findViewById(R.id.layout_auction);

        btnStartAuction = findViewById(R.id.activity_broadcasting_button_startAuction);
        btnStartAuction.setVisibility(View.GONE);
        btnBid = findViewById(R.id.auction_button_bid);
        btnBidUp = findViewById(R.id.auction_button_bid_up);
        btnBidDown = findViewById(R.id.auction_button_bid_down);

        // 경매 시작 버튼을 눌렀을 시
        // 다이얼로그를 통해 경매 시작가를 입력하고, 채팅 서버에 경매 시작 code 를 포함한 메시지를 전송하여
        // 시청자들에게 경매가 시작되었음을 알린다.
        // 경매 시작을 누름과 동시에 버튼은 '경매 종료' 버튼으로 변하게 된다.
        // 경매 종료 버튼을 누르면 시작때와 마찬가지로 다른 사용자들에게 경매 종료 code 를 포함한 메시지를 전송하여
        // 시청자들에게 경매가 종료되었음을 알린다.
        btnStartAuction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (btnStartAuction.getText().toString()){
                    case "경매 시작":
                        AlertDialog.Builder dialog = new AlertDialog.Builder(activity_publishVideo.this);
                        final EditText et = new EditText(activity_publishVideo.this);
                        dialog  .setTitle("경매 시작가를 입력하세요.")
                                .setView(et)
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        try{
                                            chattingUtility.sendMessage(
                                                    utility_global_variable.CODE_CHAT_START_AUCTION,
                                                    channel,
                                                    activity_login.user_id,
                                                    et.getText().toString(),
                                                    roomCode
                                            );
                                            btnStartAuction.setText("경매 종료");
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                })
                                .setNeutralButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });

                        dialog.create();
                        dialog.show();
                        break;
                    case "경매 종료":
                        try{
                            chattingUtility.sendMessage(
                                    utility_global_variable.CODE_CHAT_STOP_AUCTION,
                                    channel,
                                    activity_login.user_id,
                                    "경매 종료",
                                    roomCode
                            );
                            btnStartAuction.setVisibility(View.GONE);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                }

                try{

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        /**
         * 채팅을 위한 세팅
         */

        // 채팅 메시지 업데이트를 위한 handler 선언
        // chatting_client_receiver 를 통해 받은 메시지를 해당 handler 로 전송하여 처리한다.
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 1:
                        try{
                            receive_chatting(msg.obj.toString());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                }
            }
        };

        receiver = new chatting_client_receiver(handler);
        chattingUtility = new chatting_utility();

        editText_chat = findViewById(R.id.activity_broadcasting_editText_chatText);
        btnSend = (Button) findViewById(R.id.activity_broadcasting_button_send);

        // 전송 버튼을 눌렀을 때 동작 정의
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    // 전송할 텍스트가 비어있지 않을 때만 메시지를 전송
                    if(!editText_chat.getText().toString().equals("")) {
                        // 자신의 화면에 채팅 업데이트
                        String m = chattingUtility.getJSONObjectToString(
                                utility_global_variable.CODE_CHAT_MESSAGE_GENERAL,
                                activity_login.user_id,
                                editText_chat.getText().toString(),
                                activity_login.user_id);
                        receive_chatting(m);

                        // 서버에 채팅 메시지 전송
                        chattingUtility.sendMessage(
                                utility_global_variable.CODE_CHAT_MESSAGE_GENERAL,
                                channel,
                                activity_login.user_id,
                                editText_chat.getText().toString(),
                                roomCode
                        );

                        editText_chat.setText("");

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        recyclerView_chatting = findViewById(R.id.recyclerView_container_broadcasting_chatting);
        recyclerView_chatting.setLayoutManager(new LinearLayoutManager(this));

        items = new ArrayList<>();
        adapter_broadcasting_chatting = new recyclerView_adapter_chatting(this);

        recyclerView_chatting.setAdapter(adapter_broadcasting_chatting);

        /**
         * RTMP 송출을 위한 세팅
         */

        // initialize url.

        btnPublish = (Button) findViewById(R.id.publish);
        btnSwitchCamera = (Button) findViewById(R.id.swCam);

        mPublisher = new SrsPublisher((SrsCameraView) findViewById(R.id.glsurfaceview_camera));
        mPublisher.setEncodeHandler(new SrsEncodeHandler(this));
        mPublisher.setRtmpHandler(new RtmpHandler(this));
        mPublisher.setRecordHandler(new SrsRecordHandler(this));
        mPublisher.setPreviewResolution(640, 360);
        mPublisher.setOutputResolution(360, 640);
        mPublisher.setVideoHDMode();
        mPublisher.startCamera();

        // 방송 시작 버튼 클릭 이벤트 시
        // 방송 제목을 다이얼로그로 입력 받고, 해당 방송제목으로 서버 DB 에 방송 정보를 생성한다.
        // 생성한 방송 정보로부터 방송 고유번호를 받아와 rtmp url 을 생성하고, 스트리밍을 시작한다.
        // 또한 방송을 시작함과 동시에 netty 서버와 연결하여 채팅 세션을 만든다.
        // 방송 시작 후에는 버튼이 'stop'으로 표기되며, 이 때 버튼을 클릭하면 방송을 종료하게 된다.
        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnPublish.getText().toString().contentEquals("publish")) {

                    // 방송제목 입력 다이얼로그 띄우기
                    AlertDialog.Builder ad = new AlertDialog.Builder(activity_publishVideo.this);

                    ad.setTitle("방송 제목");
                    ad.setMessage("방송의 제목을 입력하세요.");

                    final EditText et = new EditText(activity_publishVideo.this);
                    ad.setView(et);

                    ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(et.getText().toString().equals("")){
                                Toast.makeText(activity_publishVideo.this, "방송 제목을 입력해야합니다.", Toast.LENGTH_SHORT);
                            }else{

                                // 방송 송출을 시작하면 소켓에 연결 후, 채팅 채널을 만든다.
                                EventLoopGroup group = new NioEventLoopGroup();

                                try{
                                    // http 통신을 통해 방송정보 업로드
                                    new utility_http_DBQuery()
                                            .execute("insert into table_broadcasting_list (title, broadcaster_id, broadcast_start_time)"
                                                    + " values ('"
                                                    + et.getText().toString()
                                                    + "','"
                                                    + getIntent().getStringExtra("streamer_id")
                                                    + "','a');").get();

                                    // http 통신을 통해 방송의 고유번호 획득
                                    JSONArray jsonArray = new utility_http_DBQuery()
                                            .execute("select * from table_broadcasting_list where broadcaster_id = '"
                                                    + activity_login.user_id
                                                    + "';").get();
                                    roomCode = jsonArray.getJSONObject(0).getString("identity_num");

                                    // 채팅 세션 생성
                                    final SslContext sslCtx = SslContextBuilder.forClient()
                                            .trustManager(InsecureTrustManagerFactory.INSTANCE).build();

                                    Bootstrap bootstrap = new Bootstrap();
                                    bootstrap.group(group)
                                            .channel(NioSocketChannel.class)
                                            .handler(new chatting_client_initializer(sslCtx, receiver));

                                    channel = bootstrap.connect(
                                            utility_global_variable.HOST,
                                            utility_global_variable.PORT
                                    ).sync().channel();

                                    // 방송자 최초 연결 시 방만들기를 위해 서버에게 메시지를 보낸다
                                    chattingUtility.sendMessage(
                                            utility_global_variable.CODE_CHAT_MAKEROOM,
                                            channel,
                                            activity_login.user_id,
                                            "방 만들기",
                                            roomCode
                                    );

                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                                // 방송 송출 코드
                                rtmpUrl  = "rtmp://52.41.99.92/mytv/" + roomCode;

                                mPublisher.startPublish(rtmpUrl);
                                mPublisher.startCamera();

                                btnPublish.setText("stop");

                                // 방송 시작 시 경매시작, 그림그리기 버튼이 보이게 됨
                                btnStartAuction.setVisibility(View.VISIBLE);
                                btnPainting.setVisibility(View.VISIBLE);
                            }
                        }
                    });

                    ad.setNeutralButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    ad.show();

                } else if (btnPublish.getText().toString().contentEquals("stop")) {
                    // 방송 중지 버튼을 눌렀을 때
                    mPublisher.stopPublish();
                    mPublisher.stopRecord();
                    btnPublish.setText("publish");
                    btnStartAuction.setVisibility(View.GONE);

                    try{
                        chattingUtility.sendMessage(
                                utility_global_variable.CODE_CHAT_EXIT,
                                channel,
                                activity_login.user_id,
                                "방 나가기",
                                roomCode
                        );
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    new utility_http_DBQuery()
                            .execute("delete from table_broadcasting_list where broadcaster_id = '"
                                    + getIntent().getStringExtra("streamer_id")
                                    + "';");
                }
            }
        });

        btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPublisher.switchCameraFace((mPublisher.getCamraId() + 1) % Camera.getNumberOfCameras());
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else {
            switch (id) {
                case R.id.cool_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.COOL);
                    break;
                case R.id.beauty_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.BEAUTY);
                    break;
                case R.id.early_bird_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.EARLYBIRD);
                    break;
                case R.id.evergreen_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.EVERGREEN);
                    break;
                case R.id.n1977_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.N1977);
                    break;
                case R.id.nostalgia_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.NOSTALGIA);
                    break;
                case R.id.romance_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.ROMANCE);
                    break;
                case R.id.sunrise_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.SUNRISE);
                    break;
                case R.id.sunset_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.SUNSET);
                    break;
                case R.id.tender_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.TENDER);
                    break;
                case R.id.toast_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.TOASTER2);
                    break;
                case R.id.valencia_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.VALENCIA);
                    break;
                case R.id.walden_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.WALDEN);
                    break;
                case R.id.warm_filter:
                    mPublisher.switchCameraFilter(MagicFilterType.WARM);
                    break;
                case R.id.original_filter:
                default:
                    mPublisher.switchCameraFilter(MagicFilterType.NONE);
                    break;
            }
        }
        setTitle(item.getTitle());

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Button btn = (Button) findViewById(R.id.publish);
        btn.setEnabled(true);
        mPublisher.resumeRecord();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPublisher.pauseRecord();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mPublisher.stopPublish();
        mPublisher.stopRecord();

        try{
            chattingUtility.sendMessage(
                    utility_global_variable.CODE_CHAT_EXIT,
                    channel,
                    activity_login.user_id,
                    "방 나가기",
                    roomCode);
        }catch (Exception e){
            e.printStackTrace();
        }
        new utility_http_DBQuery()
                .execute("delete from table_broadcasting_list where broadcaster_id = '"
                        + getIntent().getStringExtra("streamer_id")
                        + "';");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mPublisher.stopEncode();
        mPublisher.stopRecord();
//        btnRecord.setText("record");
        mPublisher.setScreenOrientation(newConfig.orientation);
        if (btnPublish.getText().toString().contentEquals("stop")) {
            mPublisher.startEncode();
        }
        mPublisher.startCamera();
    }

    private static String getRandomAlphaString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    private static String getRandomAlphaDigitString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    private void handleException(Exception e) {
        try {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            mPublisher.stopPublish();
            mPublisher.stopRecord();
            btnPublish.setText("publish");
//            btnRecord.setText("record");
//            btnSwitchEncoder.setEnabled(true);
        } catch (Exception e1) {
            //
        }
    }

    // Implementation of SrsRtmpListener.

    @Override
    public void onRtmpConnecting(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpConnected(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpVideoStreaming() {
    }

    @Override
    public void onRtmpAudioStreaming() {
    }

    @Override
    public void onRtmpStopped() {
        Toast.makeText(getApplicationContext(), "방송 종료", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpDisconnected() {
//        Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRtmpVideoFpsChanged(double fps) {
        Log.i(TAG, String.format("Output Fps: %f", fps));
    }

    @Override
    public void onRtmpVideoBitrateChanged(double bitrate) {
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            Log.i(TAG, String.format("Video bitrate: %f kbps", bitrate / 1000));
        } else {
            Log.i(TAG, String.format("Video bitrate: %d bps", rate));
        }
    }

    @Override
    public void onRtmpAudioBitrateChanged(double bitrate) {
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            Log.i(TAG, String.format("Audio bitrate: %f kbps", bitrate / 1000));
        } else {
            Log.i(TAG, String.format("Audio bitrate: %d bps", rate));
        }
    }

    @Override
    public void onRtmpSocketException(SocketException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIOException(IOException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalStateException(IllegalStateException e) {
        handleException(e);
    }

    // Implementation of SrsRecordHandler.

    @Override
    public void onRecordPause() {
        Toast.makeText(getApplicationContext(), "Record paused", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordResume() {
        Toast.makeText(getApplicationContext(), "Record resumed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordStarted(String msg) {
        Toast.makeText(getApplicationContext(), "Recording file: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordFinished(String msg) {
        Toast.makeText(getApplicationContext(), "MP4 file saved: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordIOException(IOException e) {
        handleException(e);
    }

    @Override
    public void onRecordIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    // Implementation of SrsEncodeHandler.

    @Override
    public void onNetworkWeak() {
        Toast.makeText(getApplicationContext(), "네트워크 상태가 불안정합니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNetworkResume() {
        Toast.makeText(getApplicationContext(), "네트워크가 다시 연결되었습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    /**
     * 서버로부터 채팅메시지가 도착하고, receiver 에서 handler 로 메시지가 전달된 후 메시지를 처리하는 method.
     * chatting_utility class 를 이용하여 message 로 부터 messageType(Code)를 파싱하여 어떤 message 인지 파악하고,
     * switch 문을 통해 각 Type 의 message 를 처리한다.
     */
    public void receive_chatting(String string) throws Exception{

        int messageType = chattingUtility.getMessageType(string);

        switch (messageType){

            case utility_global_variable.CODE_CHAT_MESSAGE_GENERAL:
                //채팅 메시지가 왔을 때
                recyclerView_item_chatting item = new recyclerView_item_chatting();

                item.setId(chattingUtility.getMessageId(string));
                item.setText(chattingUtility.getMessageText(string));
                item.setRoomCode(roomCode);

                if(!chattingUtility.getMessageId(string).equals(activity_login.user_id)){
                    item.setTimeStamp(chattingUtility.getMessageTimeStamp(string));
                }

                adapter_broadcasting_chatting.addItem(item);
                adapter_broadcasting_chatting.notifyDataSetChanged();

                recyclerView_chatting.scrollToPosition(adapter_broadcasting_chatting.getItemCount() - 1);
                break;

            case utility_global_variable.CODE_CHAT_START_AUCTION:
                //경매 시작 신호가 왔을 때
                priceNow = Integer.parseInt(chattingUtility.getMessageText(string));

                layout_auction.setVisibility(View.VISIBLE);

                // 경매 시스템 UI 등장 애니메이션
                Animation alphaUI = new AlphaAnimation(0, 1);
                alphaUI.setDuration(500);

                layout_auction.startAnimation(alphaUI);

                Toast.makeText(this, "경매가 시작되었습니다. 경매 시작가는 " + priceNow + "원 입니다.", Toast.LENGTH_LONG).show();

                // 경매 시스템 UI 세팅
                textView_priceNow.setText("경매 시작가 - " + priceNow + "원");
                textView_bidder.setText("입찰자 - ");

                btnBid.setVisibility(View.GONE);
                btnBidUp.setVisibility(View.GONE);
                btnBidDown.setVisibility(View.GONE);
                textView_priceBid.setText("경매 중...");
                break;

            case utility_global_variable.CODE_CHAT_STOP_AUCTION:
                // 경매 종료 신호가 왔을 때
                JSONObject json = chattingUtility.getMessageAuctionInfo(string);

                // 경매 종료 시 텍스트 변경 애니메이션
                Animation alphaPriceBid = new AlphaAnimation(0, 1);
                alphaPriceBid.setDuration(500);

                textView_priceBid.startAnimation(alphaPriceBid);

                // 경매 종료 시 텍스트 변경
                textView_priceNow.setText("낙찰가 - " + json.getString("price") + "원");
                textView_bidder.setText("낙찰자 - " + json.getString("id"));

                textView_priceBid.setText("경매 종료");
                break;

            case utility_global_variable.CODE_CHAT_PRICE_RAISE:
                // 입찰가 갱신 신호가 왔을 때
                priceNow = Integer.parseInt(chattingUtility.getMessageText(string));

                textView_priceNow.setText("입찰가 - " + String.valueOf(priceNow) + "원");
                textView_bidder.setText("입찰자 - " + chattingUtility.getMessageId(string));

                // 입찰가 갱신 시 입찰자, 입찰가 항목 변경 애니메이션
                Animation translateFromRight = new TranslateAnimation(1000, 0, 0, 0);
                Animation translateFromLeft = new TranslateAnimation(-1000, 0, 0, 0);

                translateFromRight.setDuration(250);
                translateFromLeft.setDuration(250);

                textView_priceNow.startAnimation(translateFromRight);
                textView_bidder.startAnimation(translateFromLeft);
                break;

            case utility_global_variable.CODE_CHAT_START_PAINTING:
                // 그림그리기 시작 신호가 왔을 때

                // UDP 소켓에 연결
                InetSocketAddress remoteAddress = new InetSocketAddress(utility_global_variable.HOST, utility_global_variable.PORT_UDP);

                paintingClient = new PaintingClient(utility_global_variable.HOST, utility_global_variable.PORT_UDP, paintingReceiver);

                try{
                    paintingChannelFuture = paintingClient.start();

                    // UDP 서버에 그림그리기가 시작됨을 알림
                    painting_sendMessageUtil.sendMessage(utility_global_variable.CODE_PAINT_START, UDP_server_address, paintingClient, roomCode);
                }catch (Exception e){
                    e.printStackTrace();
                }

                // 그림판 UI 들의 Visibility 를 VISIBLE 로 변경
                if(linearLayout.getVisibility() == View.VISIBLE){

                    // 방송 도중 최초로 그림 그리기를 시작하면, linearLayout 의 Visibility 가 VISIBLE 상태이므로,
                    // 아래의 코드는 방송 도중 최초로 그림그리기를 시작했을 때 실행된다.
                    // paintingDrawView_Play 클래스를 생성하고, linearLayout 에 addView 를 통해 삽입한다.
                    paintingDrawView = new PaintingDrawView_Publish(this, paintingClient, UDP_server_address, roomCode);
                    linearLayout.addView(paintingDrawView);
                }else{

                    // 그림 그리기를 중단했다가 다시 활성화 했을 경우, linearLayout 의 Visibility 값만 VISIBLE 로 변경해준다.
                    linearLayout.setVisibility(View.VISIBLE);
                }

                // 그림 그리기 UI 들의 Visibility 를 VISIBLE 로 변경
                btnChangeColor.setVisibility(View.VISIBLE);
                btnEraser.setVisibility(View.VISIBLE);
                btnUndo.setVisibility(View.VISIBLE);
                btnClear.setVisibility(View.VISIBLE);
                break;

            case utility_global_variable.CODE_CHAT_STOP_PAINTING:

                // 그림그리기 중단 신호가 왔을 때
                // 그림 그리기 UI 의 Visibility 를 GONE 으로 처리한다.
                linearLayout.setVisibility(View.GONE);
                btnChangeColor.setVisibility(View.GONE);
                btnEraser.setVisibility(View.GONE);
                btnUndo.setVisibility(View.GONE);
                btnClear.setVisibility(View.GONE);
                break;

        }
    }

    /**
     * UDP 서버로부터 온 json 메시지를 파싱하여 상황에 맞게 처리하는 메소드
     */
    public void receive_painting(String string) {

        // 이 액티비티에서는 UDP 패킷을 보내기만 하고, 받을 일은 없기 때문에 이 메소드가 딱히 하는일은 없다.

        try {
            JSONObject json = new JSONObject(string);

            int type = json.getInt("type");
            float x1 = (float)json.getDouble("x1");
            float y1 = (float)json.getDouble("y1");
            float x2 = (float)json.getDouble("x2");
            float y2 = (float)json.getDouble("y2");
            int color = json.getInt("color");
            String roomCode = json.getString("roomCode");


            switch (type){
                case utility_global_variable.CODE_PAINT_PROGRESS:
                    break;

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
