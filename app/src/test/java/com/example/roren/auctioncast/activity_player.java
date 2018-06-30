//package com.example.roren.auctioncast;
//
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.support.annotation.Nullable;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//
//import com.google.android.exoplayer2.ExoPlayerFactory;
//import com.google.android.exoplayer2.SimpleExoPlayer;
//import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
//import com.google.android.exoplayer2.source.ExtractorMediaSource;
//import com.google.android.exoplayer2.source.MediaSource;
//import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
//import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
//import com.google.android.exoplayer2.trackselection.TrackSelection;
//import com.google.android.exoplayer2.trackselection.TrackSelector;
//import com.google.android.exoplayer2.ui.PlayerView;
//import com.google.android.exoplayer2.upstream.BandwidthMeter;
//import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
//
//import java.util.ArrayList;
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
//public class activity_playvideo extends AppCompatActivity{
//
//    SimpleExoPlayer player;
//
//    ////////////////////////////////////////////////////////////
//    private EditText editText_chat;
//    private Button btnSend;
//
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
//        recyclerView_chatting.scrollToPosition(adapter_broadcasting_chatting.getItemCount() - 1);
//    }
//
//    ///////////////////////////////////////////////////////////
//
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_playvideo);
//
//        parser = new chatting_messageParser();
//        compressor = new chatting_messageCompressor();
//
//        // 소켓에 연결 후, 채널을 만든다.
//        EventLoopGroup group = new NioEventLoopGroup();
//
//        try{
//            final SslContext sslCtx = SslContextBuilder.forClient()
//                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
//
//            Bootstrap bootstrap = new Bootstrap();
//            bootstrap.group(group)
//                    .channel(NioSocketChannel.class)
//                    .handler(new chatting_client_initializer_viewer(sslCtx));
//
//            channel = bootstrap.connect(HOST, PORT).sync().channel();
//
//            //시청자 최초 연결 시 방 입장을 위해 서버에게 메시지를 보낸다
//            sendMessage("enterRoom");
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//
//        handler = new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//                switch (msg.what){
//                    case 1:
//                        try{
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
//                        String m = compressor.getJSONObjectToString(utility_global_variable.CODE_CHAT_MESSAGE_GENERAL, activity_login.user_id, editText_chat.getText().toString(), getIntent().getStringExtra("streamer_id"));
//                        editText_chat.setText("");
//                        receive_chatting(m);
//                        ChannelFuture lastWriteFuture = null;
//                        lastWriteFuture = channel.writeAndFlush(m);
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
//
//        //initiate Player
////Create a default TrackSelector
//        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
//        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
//        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
//
////Create the player
//        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
//        PlayerView playerView = findViewById(R.id.activity_player_playerView);
//        playerView.setPlayer(player);
//        playerView.hideController();
//
//        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
//
//// This is the MediaSource representing the media to be played.
//        MediaSource videoSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory)
//                .createMediaSource(Uri.parse("rtmp://52.41.99.92/mytv/" + getIntent().getStringExtra("streamer_id")));
//
//// Prepare the player with the source.
//        player.prepare(videoSource);
////auto start playing
//        player.setPlayWhenReady(true);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        player.setPlayWhenReady(true);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        player.stop();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        player.stop();
//
//        try{
//            sendMessage("exitRoom");
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        new utility_http_DBQuery().execute("update table_broadcasting_list set viewer_num=viewer_num-1 where broadcaster_id = '" + getIntent().getStringExtra("streamer_id") + "'");
//
//    }
//
//    public void sendMessage(String code) throws Exception{
//
//        switch(code){
//            case "enterRoom":
//                String m = compressor.getJSONObjectToString(utility_global_variable.CODE_CHAT_ENTRANCE, activity_login.user_id, "방 입장", getIntent().getStringExtra("streamer_id"));
//                channel.writeAndFlush(m);
//                break;
//            case "exitRoom":
//                String m2 = compressor.getJSONObjectToString(utility_global_variable.CODE_CHAT_EXIT, activity_login.user_id, "방나가기", getIntent().getStringExtra("streamer_id"));
//                channel.writeAndFlush(m2);
//                break;
//            case "chatting":
//                String m3 = compressor.getJSONObjectToString(utility_global_variable.CODE_CHAT_MESSAGE_GENERAL, activity_login.user_id, editText_chat.getText().toString(), getIntent().getStringExtra("streamer_id"));
//                editText_chat.setText("");
//                receive_chatting(m3);
//                channel.writeAndFlush(m3);
//                break;
//        }
//
//    }
//}
