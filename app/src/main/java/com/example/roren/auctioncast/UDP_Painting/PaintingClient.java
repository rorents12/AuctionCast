package com.example.roren.auctioncast.UDP_Painting;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class PaintingClient {

    private int port;
    private String host;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private PaintingReceiver paintingReceiver;

    public PaintingClient(String host, int port, PaintingReceiver paintingReceiver) {
        this.host = host;
        this.port = port;
        this.paintingReceiver = paintingReceiver;
    }

    public ChannelFuture start() throws InterruptedException {
        workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioDatagramChannel.class)
                .handler(new PaintingClientInitializer(paintingReceiver));

        ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(0));
        channelFuture.syncUninterruptibly();

        channel = channelFuture.channel();

        return channelFuture;
    }

    public ChannelFuture write(Object msg) throws InterruptedException {
        ChannelFuture channelFuture = channel.writeAndFlush(msg).sync();

        return channelFuture;
    }

    public void stop() {
        if (channel != null) {
            channel.close();
        }
        workerGroup.shutdownGracefully();
    }

}
