//package com.example.roren.auctioncast;
//
//import android.app.Service;
//import android.content.Intent;
//import android.os.AsyncTask;
//import android.os.IBinder;
//import android.support.annotation.Nullable;
//
//import io.netty.bootstrap.Bootstrap;
//import io.netty.channel.Channel;
//import io.netty.channel.EventLoop;
//import io.netty.channel.EventLoopGroup;
//import io.netty.channel.nio.NioEventLoop;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import io.netty.handler.ssl.SslContext;
//import io.netty.handler.ssl.SslContextBuilder;
//import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
//
//public class utility_chatting_client extends Service{
//
//    public static String chat_mesasge;
//
//    static final String HOST = System.getProperty("host", "127.0.0.1");
//    static final int PORT = Integer.parseInt(System.getProperty("port", "5001"));
//
//    private Channel channel;
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
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
//                    .handler(new chatting_client_initializer(sslCtx));
//
//            channel = bootstrap.connect(HOST, PORT).sync().channel();
//
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//
//    private class receive_thread extends Thread{
//        @Override
//        public void run(){
//            EventLoopGroup group = new NioEventLoopGroup();
//
//            try{
//                final SslContext sslCtx = SslContextBuilder.forClient()
//                        .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
//
//                Bootstrap bootstrap = new Bootstrap();
//                bootstrap.group(group)
//                        .channel(NioSocketChannel.class)
//                        .handler(new chatting_client_initializer(sslCtx));
//
//                Channel channel = bootstrap.connect(HOST, PORT).sync().channel();
//
//
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private class
//}
