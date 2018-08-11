package com.example.roren.auctioncast.UDP_Painting;

import android.graphics.Paint;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;

public class PaintingClientInitializer extends ChannelInitializer<DatagramChannel> {
    private PaintingReceiver paintingReceiver;

    public PaintingClientInitializer(PaintingReceiver paintingReceiver){
        this.paintingReceiver = paintingReceiver;
    }

    @Override
    protected void initChannel(DatagramChannel datagramChannel) throws Exception {
        ChannelPipeline pipeline = datagramChannel.pipeline();

        pipeline.addLast("echo", paintingReceiver);
    }
}
