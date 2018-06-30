package com.example.roren.auctioncast.chatting;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;

public class chatting_client_initializer extends ChannelInitializer<SocketChannel>{

    private final SslContext sslCtx;
    private final ChannelInboundHandlerAdapter receiver;

    public chatting_client_initializer(SslContext sslCtx, ChannelInboundHandlerAdapter receiver){
        this.sslCtx = sslCtx;
        this.receiver = receiver;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());
        pipeline.addLast(this.receiver);
    }

}
