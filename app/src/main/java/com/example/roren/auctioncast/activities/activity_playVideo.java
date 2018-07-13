package com.example.roren.auctioncast.activities;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.util.ArrayList;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import com.example.roren.auctioncast.R;

import org.json.JSONObject;

/**
 * 시청자가 방송을 볼 수 있는 액티비티.
 *
 * ExoPlayer를 이용하여 RTMP 프로토콜을 통해 동영상을 스트리밍 하는 부분과, netty를 이용하여 채팅을 하는 부분으로 나뉜다.
 *
 * 1. RTMP 스트리밍
 *
 * 2. 채팅
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

    // netty 채팅 서버에 접속할 때, 사용자가 속한 방에 대한 정보를 나타내는 roomCode
    private String roomCode;
    private String title;

    /**
     * 경매 시스템을 위한 변수 선언
     */

    private Button btnBid;
    private ImageButton btnBidUp;
    private ImageButton btnBidDown;
    private TextView textView_priceNow;
    private TextView textView_bidder;
    private TextView textView_priceBid;
    private LinearLayout layout_auction;

    private int priceNow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playvideo);

        /**
         * onCreate 내의 채팅부분
         */
        // 채팅 방을 구분하기 위한 roomCode 선언
        roomCode = getIntent().getStringExtra("streamer_id");
        title = getIntent().getStringExtra("title");

        // 채팅 메시지 업데이트를 위한 handler 선언
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
                    "방 입장", roomCode
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

        btnBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                        System.out.println("확인했습니다.");
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

        btnBidUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int priceBid = Integer.parseInt(textView_priceBid.getText().toString());
                textView_priceBid.setText(String.valueOf(priceBid + 1000));
            }
        });

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
                .createMediaSource(Uri.parse("rtmp://52.41.99.92/mytv/" + getIntent().getStringExtra("streamer_id")));

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
            chattingUtility.sendMessage(utility_global_variable.CODE_CHAT_EXIT, channel, activity_login.user_id, "방 나가기", getIntent().getStringExtra("streamer_id"));
        }catch (Exception e){
            e.printStackTrace();
        }

        new utility_http_DBQuery().execute("update table_broadcasting_list set viewer_num=viewer_num-1 where broadcaster_id = '" + getIntent().getStringExtra("streamer_id") + "'");

    }

    /**
     * 서버로부터 채팅메시지가 도착하고, receiver에서 handler로 메시지가 전달된 후 메시지를 처리하는 method.
     */
    public void receive_chatting(String string) throws Exception{
        int messageType = chattingUtility.getMessageType(string);

        switch (messageType){

            case utility_global_variable.CODE_CHAT_MESSAGE_GENERAL:
                //채팅 메시지가 왔을 때
                recyclerView_item_chatting item = new recyclerView_item_chatting();

                item.setId(chattingUtility.getMessageId(string));
                item.setText(chattingUtility.getMessageText(string));

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
                                    + roomCode + "','" + activity_login.user_id + "'," + json.getString("price")
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
        }
    }

    public class Thread_stopBroadcast extends Thread{

        private Handler handler;

        Thread_stopBroadcast(Handler handler){
            this.handler = handler;
        }

        @Override
        public void run() {

            try{
                Message m = new Message();

                this.sleep(1000);

                m.what = 5;
                this.handler.sendMessage(m);
                this.sleep(1000);

                Message m2 = new Message();
                m2.what = 4;
                this.handler.sendMessage(m2);
                this.sleep(1000);

                Message m3 = new Message();
                m3.what = 3;
                this.handler.sendMessage(m3);
                this.sleep(1000);

                Message m4 = new Message();
                m4.what = 2;
                this.handler.sendMessage(m4);
                this.sleep(1000);

                Message m5 = new Message();
                m5.what = 1;
                this.handler.sendMessage(m5);
                this.sleep(1000);

                Message m6 = new Message();
                m6.what = 0;
                this.handler.sendMessage(m6);
                this.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public class Handler_stopBroadcast extends Handler{


        Animation alphaPriceBid = new AlphaAnimation(0, 1);

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 5:
                    alphaPriceBid.setDuration(500);
                    textView_priceBid.setText("방송 종료 5초전...");
                    textView_priceBid.setAnimation(alphaPriceBid);
                    break;
                case 4:
                    alphaPriceBid.setDuration(500);
                    textView_priceBid.setText("방송 종료 4초전...");
                    textView_priceBid.setAnimation(alphaPriceBid);
                    break;
                case 3:
                    alphaPriceBid.setDuration(500);
                    textView_priceBid.setText("방송 종료 3초전...");
                    textView_priceBid.setAnimation(alphaPriceBid);
                    break;
                case 2:
                    alphaPriceBid.setDuration(500);
                    textView_priceBid.setText("방송 종료 2초전...");
                    textView_priceBid.setAnimation(alphaPriceBid);
                    break;
                case 1:
                    alphaPriceBid.setDuration(500);
                    textView_priceBid.setText("방송 종료 1초전...");
                    textView_priceBid.setAnimation(alphaPriceBid);
                    break;
                case 0:
                    //방송 종료
                    break;
            }
        }
    }
}
