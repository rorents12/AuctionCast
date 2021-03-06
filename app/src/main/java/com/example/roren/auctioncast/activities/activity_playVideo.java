package com.example.roren.auctioncast.activities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roren.auctioncast.UDP_Painting.PaintingClient;
import com.example.roren.auctioncast.UDP_Painting.PaintingDrawView_Play;
import com.example.roren.auctioncast.UDP_Painting.PaintingDrawView_Publish;
import com.example.roren.auctioncast.UDP_Painting.PaintingReceiver;
import com.example.roren.auctioncast.UDP_Painting.Painting_sendMessageUtil;
import com.example.roren.auctioncast.chatting.chatting_client_receiver;
import com.example.roren.auctioncast.chatting.chatting_client_initializer;
import com.example.roren.auctioncast.chatting.chatting_utility;
import com.example.roren.auctioncast.utility.utility_global_variable;
import com.example.roren.auctioncast.utility.utility_http_DBQuery;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.net.InetSocketAddress;
import java.util.ArrayList;

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

import org.json.JSONObject;

/**
 * 시청자가 방송을 볼 수 있는 액티비티.
 *
 * ExoPlayer를 이용하여 RTMP 프로토콜을 통해 동영상을 스트리밍 하는 부분과, netty 를 이용하여 채팅과 경매를 진행하는 부분으로 나뉜다.
 *
 * 1. RTMP 스트리밍
 * activity_home 에서 방송목록을 터치하면 해당 방송의 정보를 intent 를 통해 이 activity 로 넘기게 된다.
 * 넘겨받은 방송정보 중 방송의 고유번호를 통해 RTMP Streaming 의 url 을 세팅하고 스트리밍을 시작한다.
 *
 * 2. 채팅과 경매 시스템
 * netty 서버와의 소켓 연결을 통해 채팅과 경매를 진행한다.
 * 이 activity 에서의 여러 행동들은 sendMessage method 를 통해 각각 netty 서버로 message 를 보내거나 받게 된다.
 * 각 행동들은 고유의 code 를 포함한 message 를 전송하며, 고유 code 는 다음과 같다.
 *
 *      시청자가 서버로 보내는 message code
 *          1) 방송 참여
 *              code -> utility_global_variable.CODE_CHAT_ENTRANCE
 *              서버에서 해당 code 를 파싱하여 사용자를 채팅방 세션에 추가한다.
 *          2) 채팅 메시지 보내기
 *              code -> utility_global_variable.CODE_CHAT_MESSAGE_GENERAL
 *              서버에서 해당 code 를 파싱하여 사용자가 참여한 채팅방의 다른 사용자들에게 메시지를 전송한다.
 *          3) 경매 입찰
 *              code -> utility_global_variable.CODE_CHAT_PRICE_RAISE
 *              서버에서 해당 code 를 파싱하여 사용자가 참여한 채팅방의 다른 사용자들에게 경매 입찰 정보를 전송하고, 경매 정보를 업데이트한다.
 *
 *      시청자가 서버로부터 받는 message code
 *          chatting_client_receiver 에서 받은 message 를 이 activity 의 handler 로 보내고, handler 에서 receive_chatting method 를 이용하여
 *          각 code 에 맞는 처리를 진행한다.
 *
 *          1) 채팅 메시지 받기
 *              code -> utility_global_variable.CODE_CHAT_MESSAGE_GENERAL
 *          2) 경매 시작 신호
 *              code -> utility_global_variable.CODE_CHAT_AUCTION_START
 *          3) 경매 입찰 정보
 *              code -> utility_global_variable.CODE_CHAT_PRICE_RAISE
 *          4) 경매 종료 신호
 *              code -> utility_global_variable.CODE_CHAT_AUCTION_STOP
 *
 **/

public class activity_playVideo extends AppCompatActivity{

    // player 객체를 위한 변수 선언
    SimpleExoPlayer player;

    /**
     *  채팅을 위한 변수 선언
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

    // netty 채팅 서버에 접속할 때, 사용자가 속한 방에 대한 정보를 나타내는 변수들
    private String roomCode;
    private String streamer_id;
    private String title;

    /**
     * 경매 시스템을 위한 변수 선언
     */

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
     * 그림 그리기를 위한 변수 선언
     */

    // 그림을 그릴 수 있는 뷰 클래스. 그림을 그리기 위한 여러 메소드들이 정의되어 있다.
    private PaintingDrawView_Play paintingDrawView_play;

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playvideo);

        /**
         * onCreate 내의 그림그리기 부분
         */

        // PaintingDrawView_Play 뷰 클래스를 위치시킬 linearLayout 을 선언한다.
        linearLayout = findViewById(R.id.activity_player_LinearLayout);

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

        /**
         * onCreate 내의 채팅부분
         */
        // 채팅 방을 구분하기 위한 roomCode 선언
        roomCode = getIntent().getStringExtra("identity_num");
        streamer_id = getIntent().getStringExtra("streamer_id");
        title = getIntent().getStringExtra("title");

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

        // 채팅 메시지를 서버로부터 받아오는 receiver 선언
        receiver = new chatting_client_receiver(handler);

        // 채팅 메시지를 parsing / compressing / send 해주는 유틸리티 클래스 선언
        chattingUtility = new chatting_utility();

        // netty를 이용하여 서버 소켓에 연결 하고 연결을 지속해줄 channel 객체를 생성한다.
        EventLoopGroup group = new NioEventLoopGroup();

        try{
            final SslContext sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new chatting_client_initializer(sslCtx, receiver));

            channel = bootstrap.connect(utility_global_variable.HOST, utility_global_variable.PORT).sync().channel();

            //시청자 최초 연결 시 방 입장을 위해 서버에게 메시지를 보낸다
            chattingUtility.sendMessage(
                    utility_global_variable.CODE_CHAT_ENTRANCE,
                    channel,
                    activity_login.user_id,
                    "방 입장",
                    roomCode
            );

        }catch (Exception e){
            e.printStackTrace();
        }

        // 액티비티의 채팅 전송 UI 세팅(텍스트 입력란, 전송버튼)
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
                                roomCode
                        );
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
         *  onCreate 내의 경매 시스템 부분
         */

        textView_priceNow = findViewById(R.id.auction_textView_priceNow);
        textView_bidder = findViewById(R.id.auction_textView_bidder);
        textView_priceBid = findViewById(R.id.auction_textView_priceBid);

        layout_auction = findViewById(R.id.layout_auction);

        btnBid = findViewById(R.id.auction_button_bid);
        btnBidUp = findViewById(R.id.auction_button_bid_up);
        btnBidDown = findViewById(R.id.auction_button_bid_down);

        // 입찰 버튼 클릭 이벤트 정의
        // 현재 입찰가를 확인한 후, 사용자가 입찰 할 금액이 현재 입찰가보다 같거나 낮으면
        // 입찰 할 수 없다는 메시지를 띄워준다.
        btnBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 현재 입찰가 확인, 입찰할 금액과 비교
                int priceBid = Integer.parseInt(textView_priceBid.getText().toString());
                if(priceBid > priceNow){
                    try{
                        chattingUtility.sendMessage(
                                utility_global_variable.CODE_CHAT_PRICE_RAISE,
                                channel,
                                activity_login.user_id,
                                String.valueOf(priceBid),
                                roomCode
                        );
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(
                            activity_playVideo.this,
                            "현재 입찰가인 " + priceNow + "원 보다 낮은 금액으로는 입찰할 수 없습니다.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });

        // 입찰 금액 증가 버튼 클릭 이벤트 정의
        // 입찰할 금액을 1000원 올린다.
        btnBidUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int priceBid = Integer.parseInt(textView_priceBid.getText().toString());
                textView_priceBid.setText(String.valueOf(priceBid + 1000));
            }
        });

        // 입찰 금액 감소 버튼 클릭 이벤트 정의
        // 입찰할 금액을 1000원 내린다.
        btnBidDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int priceBid = Integer.parseInt(textView_priceBid.getText().toString());
                textView_priceBid.setText(String.valueOf(priceBid - 1000));
            }
        });

        /**
         *  onCreate 내의 RTMP 플레이어 부분
         */
        //initiate Player
        //Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        //Create the player
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        PlayerView playerView = findViewById(R.id.activity_player_playerView);
        playerView.setPlayer(player);
        playerView.hideController();

        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();

        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory)
                .createMediaSource(Uri.parse("rtmp://52.41.99.92/mytv/" + roomCode));

        // Prepare the player with the source.
        player.prepare(videoSource);
        //auto start playing
        player.setPlayWhenReady(true);
    }


    /**
     *  onResume, onPause 의 생명주기 콜백 메소드를 통해 사용자가 화면을 사용중이지 않을 때 동영상 재생을 끊거나
     *  다시 화면으로 돌아왔을 때 동영상을 재생해주는 기능을 구현.
     */

    @Override
    protected void onResume() {
        super.onResume();
        player.setPlayWhenReady(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.stop();
    }

    /**
     * onDestroy 콜백 메소드를 통해 사용자가 방송화면을 종료했을 시, netty 채팅 서버와의 연결을 끊고 서버 DB의 방송 시청자 수 를 수정한다.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        player.stop();

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

        // 시청자가 나갈 때, 서버 방송목록 정보의 시청자수에 -1 연산을 해준다.
        new utility_http_DBQuery()
                .execute("update table_broadcasting_list set viewer_num=viewer_num-1 where broadcaster_id = '"
                        + getIntent().getStringExtra("streamer_id") + "'");

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

                textView_priceNow.setText("경매 시작가 - " + priceNow + "원");
                textView_bidder.setText("입찰자 - ");
                textView_priceBid.setText(String.valueOf(priceNow + 1000));

                System.out.println("시작가는 " + priceNow);

                break;

            case utility_global_variable.CODE_CHAT_STOP_AUCTION:
                // 경매 종료 신호가 왔을 때
                JSONObject json = chattingUtility.getMessageAuctionInfo(string);

                // 경매 종료 시 텍스트 변경 애니메이션
                Animation alphaPriceBid = new AlphaAnimation(0, 1);
                alphaPriceBid.setDuration(500);
                Animation alphaVanish = new AlphaAnimation(1, 0);
                alphaVanish.setDuration(500);

                textView_priceBid.startAnimation(alphaPriceBid);

                textView_priceNow.setText("낙찰가 - " + json.getString("price") + "원");
                textView_bidder.setText("낙찰자 - " + json.getString("id"));

                btnBid.setVisibility(View.GONE);
                btnBidUp.setVisibility(View.GONE);
                btnBidDown.setVisibility(View.GONE);

                textView_priceBid.setText("경매 종료");

                // 경매 종료 시 낙찰자를 제외한 사용자들은 자동으로 퇴장
                if(!json.getString("id").equals(activity_login.user_id)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(activity_playVideo.this);
                    final EditText et = new EditText(activity_playVideo.this);
                    dialog.setTitle("자동 퇴장 알림")
                            .setMessage("경매가 종료되어 낙찰자를 제외한 시청자들은 자동으로 퇴장합니다.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            });

                    dialog.create();
                    dialog.show();
                }else{
                    // 경매 종료 시 낙찰자는 서버 DB에 거래 정보를 업로드한다.
                    new utility_http_DBQuery().execute(
                            "insert into table_contract_information (seller, buyer, price, title) values('"
                                    + streamer_id + "','" + activity_login.user_id + "'," + json.getString("price")
                                    + ",'" + title + "');").get();
                }

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
                    paintingDrawView_play = new PaintingDrawView_Play(this, paintingClient, UDP_server_address, roomCode);
                    linearLayout.addView(paintingDrawView_play);
                }else{

                    // 그림 그리기를 중단했다가 다시 활성화 했을 경우, linearLayout 의 Visibility 값만 VISIBLE 로 변경해준다.
                    linearLayout.setVisibility(View.VISIBLE);
                }
                break;

            case utility_global_variable.CODE_CHAT_STOP_PAINTING:

                // 그림그리기 중단 신호가 왔을 때
                // 그림 그리기 UI 의 Visibility 를 GONE 으로 처리한다.
                linearLayout.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * UDP 서버로부터 온 json 메시지를 파싱하여 상황에 맞게 처리하는 메소드
     */
    public void receive_painting(String string){
        try {

            // JSONObject 파싱하기
            JSONObject json = new JSONObject(string);

            int type = json.getInt("type");
            String roomCode = json.getString("roomCode");

            // 메시지의 타입에 따라 어떤 처리를 할것인지 결정한다.
            switch (type){

                // 메시지타입 - CODE_PAINT_PROGRESS
                // 시작점과 도착점의 x,y 좌표값과 color 값을 받아 paintingDrawView_play 의 draw 메소드에
                // 전달하여 그림을 그리도록 한다.
                case utility_global_variable.CODE_PAINT_PROGRESS:
                    float x1 = (float)json.getDouble("x1");
                    float y1 = (float)json.getDouble("y1");
                    float x2 = (float)json.getDouble("x2");
                    float y2 = (float)json.getDouble("y2");
                    int color = json.getInt("color");
                    paintingDrawView_play.draw(color, x1, y1, x2, y2);
                    paintingDrawView_play.invalidate();
                    break;

                // 메시지 타입 - CODE_PAINT_BEFORE_PROGRESS
                // 실행취소 기능을 위하여 paintingDrawView_play 의 savePainting 메소드를 호출한다.
                // 현재의 그림을 저장한다.
                case utility_global_variable.CODE_PAINT_BEFORE_PROGRESS:
                    paintingDrawView_play.savePainting();
                    break;

                // 메시지 타입 - CODE_PAINT_UNDO
                // 실행취소 기능을 실행한다.
                case utility_global_variable.CODE_PAINT_UNDO:
                    paintingDrawView_play.undo();
                    paintingDrawView_play.invalidate();
                    break;

                // 메시지 타입 - CODE_PAINT_CLEAR
                // 모두 지우기 기능을 실행한다.
                case utility_global_variable.CODE_PAINT_CLEAR:
                    paintingDrawView_play.clear();
                    paintingDrawView_play.invalidate();


            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
