//package com.example.roren.auctioncast;
//
//import android.content.Context;
//
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.ChannelPipeline;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.unix.Socket;
//import io.netty.handler.codec.DelimiterBasedFrameDecoder;
//import io.netty.handler.codec.Delimiters;
//import io.netty.handler.codec.string.StringDecoder;
//import io.netty.handler.codec.string.StringEncoder;
//import io.netty.handler.ssl.SslContext;
//
//public class chatting_client_initializer extends ChannelInitializer<SocketChannel>{
//
//    private final SslContext sslCtx;
////    private Context context;
//
//    public chatting_client_initializer(SslContext sslCtx){
//        this.sslCtx = sslCtx;
////        this.context = context;
//    }
//
//    @Override
//    protected void initChannel(SocketChannel socketChannel) throws Exception {
//        ChannelPipeline pipeline = socketChannel.pipeline();
//
//        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
//        pipeline.addLast(new StringDecoder());
//        pipeline.addLast(new StringEncoder());
//        pipeline.addLast(new chatting_client_receiver());
//    }
//
//}
