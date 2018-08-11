package com.example.roren.auctioncast.UDP_Painting;

import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import com.example.roren.auctioncast.chatting.chatting_utility;

import org.json.JSONObject;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

public class Painting_sendMessageUtil {

    public void sendMessage(
            int type,
            InetSocketAddress address,
            PaintingClient client,
            float oldX, float oldY, float x, float y, int color,
            String roomCode
    )
    {
        try{
            JSONObject json = new JSONObject();

            json.put("type", type);
            json.put("x1", oldX);
            json.put("y1", oldY);
            json.put("x2", x);
            json.put("y2", y);
            json.put("color", color);
            json.put("roomCode", roomCode);

            ByteBuf byteBuf = Unpooled.copiedBuffer(json.toString(), CharsetUtil.UTF_8);

            client.write(new DatagramPacket(byteBuf, address));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendMessage(int type, InetSocketAddress address, PaintingClient client, String roomCode){
        try{
            JSONObject json = new JSONObject();

            json.put("type", type);
            json.put("roomCode", roomCode);

            ByteBuf byteBuf = Unpooled.copiedBuffer(json.toString(), CharsetUtil.UTF_8);

            client.write(new DatagramPacket(byteBuf, address));
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
