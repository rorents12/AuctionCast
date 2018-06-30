package com.example.roren.auctioncast.chatting;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.roren.auctioncast.activities.activity_publishVideo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class chatting_client_receiver extends ChannelInboundHandlerAdapter{

    private Handler handler;

    public chatting_client_receiver(Handler handler){
        this.handler = handler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = null;
        message = (String)msg;

        Message m = new Message();
        m.what = 1;
        m.obj = message;

        this.handler.sendMessage(m);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
