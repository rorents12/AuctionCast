package com.example.roren.auctioncast.activities;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

    // netty 채팅 서버에 연결하기 위한 port 번호와 host ip
    static final String HOST = System.getProperty("host", "192.168.0.90");
    static final int PORT = Integer.parseInt(System.getProperty("port", "5001"));

    // netty 채팅 서버와 연결했을 때 해당 서버와 연결을 지속하게 해주고, 메시지를 주고 받을 수 있게 해주는 클래스.
    private Channel channel;

    // netty 채팅 서버에서 메시지가 왔을 때 해당 메시지를 받아주는 receiver, receiver로부터 메시지를 전달받아 recyclerView를 업데이트 하는 handler.
    private Handler handler;
    private chatting_client_receiver receiver;

    // 서버로부터 채팅메시지가 도착하고, receiver에서 handler로 메시지가 전달된 후 recyclerView를 업데이트 하여 새로운 채팅을 보여주는 method.
    public void receive_chatting(String string) throws Exception{

        recyclerView_item_chatting item = new recyclerView_item_chatting();

        item.setId(chattingUtility.getMessageId(string));
        item.setText(chattingUtility.getMessageText(string));

        adapter_broadcasting_chatting.addItem(item);
        adapter_broadcasting_chatting.notifyDataSetChanged();
        recyclerView_chatting.scrollToPosition(adapter_broadcasting_chatting.getItemCount() - 1);
    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playvideo);

        /**
         * onCreate 내의 채팅부분
         */
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

            channel = bootstrap.connect(HOST, PORT).sync().channel();

            //시청자 최초 연결 시 방 입장을 위해 서버에게 메시지를 보낸다
            chattingUtility.sendMessage(utility_global_variable.CODE_CHAT_ENTRANCE, channel, activity_login.user_id, "방 입장", getIntent().getStringExtra("streamer_id"));

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
                        String m = chattingUtility.getJSONObjectToString(utility_global_variable.CODE_CHAT_MESSAGE_GENERAL, activity_login.user_id, editText_chat.getText().toString(), getIntent().getStringExtra("streamer_id"));
                        receive_chatting(m);

                        // 서버에 채팅 메시지 전송
                        chattingUtility.sendMessage(utility_global_variable.CODE_CHAT_MESSAGE_GENERAL, channel,activity_login.user_id, editText_chat.getText().toString(), getIntent().getStringExtra("streamer_id"));

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

}