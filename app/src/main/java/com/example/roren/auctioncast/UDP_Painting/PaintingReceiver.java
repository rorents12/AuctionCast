package com.example.roren.auctioncast.UDP_Painting;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

public class PaintingReceiver extends SimpleChannelInboundHandler<DatagramPacket>{

    private Handler handler;

    public PaintingReceiver(Handler handler){
        this.handler = handler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {


        ByteBuf buf = msg.content();

        String message = buf.toString(CharsetUtil.UTF_8);

        Log.e("리시브 테스트: ", message);

        Message m = new Message();
        m.what = 1;
        m.obj = message;

        this.handler.sendMessage(m);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println(cause.getMessage());
        ctx.close();
    }
}
