//package com.example.roren.auctioncast.chatting;
//
//import android.os.Message;
//import android.util.Log;
//
//import com.example.roren.auctioncast.activities.activity_playvideo;
//
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//
//public class chatting_client_handler_viewer extends ChannelInboundHandlerAdapter {
//
//    private chatting_messageParser parser;
////    private Context context;
//
//    public chatting_client_handler_viewer(){
//        parser = new chatting_messageParser();
////        this.context = context;
//    }
//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        String message = null;
//        message = (String)msg;
//
//        Log.e("리시브 테스트", message);
//
////        utility_chatting_client.chat_mesasge = message;
//
//        Message m = new Message();
//        m.what = 1;
//        m.obj = message;
//
//        activity_playvideo.handler.sendMessage(m);
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        cause.printStackTrace();
//        ctx.close();
//    }
//}