package com.example.roren.auctioncast.UDP_Painting;

import org.json.JSONObject;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

// UDP 통신을 위해 여러 parameter 를 받아 json 객체로 만들고, 해당 객체를 String 형태로
// UDP 서버로 보내주는 역할을 하는 클래스

/**
 *  UDP 통신을 위해 여러 parameter 를 받아 json 객체로 만들고, 해당 객체를 String 형태로 UDP 서버로 전송하는 역할을 하는 클래스.
 *
 *  받는 parameter 는 다음과 같다.
 *
 *  int type
 *      - 보내는 메시지의 타입. 타입에 따라 메시지를 받은 클라이언트가 어떤 행동을 할지 결정된다.
 *
 *  InetSocketAddress
 *      - 메시지를 보낼 서버의 주소를 저장한 클래스
 *
 *  PaintingClient client
 *      - 메시지를 보내는 사용자의 UDP client 클래스
 *
 *  float oldX, oldY, x, y
 *      - 그림을 그려낼 path 의 시작점 좌표값과 도착점 좌표값
 *
 *  int color
 *      - 그려낼 path 의 색깔 정보
 *
 *  String roomCode
 *      - UDP 메시지를 전송할 채팅방의 roomCode
 */

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

            // parameter 들을 JSON 객체로 압축
            JSONObject json = new JSONObject();

            json.put("type", type);
            json.put("x1", oldX);
            json.put("y1", oldY);
            json.put("x2", x);
            json.put("y2", y);
            json.put("color", color);
            json.put("roomCode", roomCode);

            ByteBuf byteBuf = Unpooled.copiedBuffer(json.toString(), CharsetUtil.UTF_8);

            // UDP 서버로 전송
            client.write(new DatagramPacket(byteBuf, address));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendMessage(int type, InetSocketAddress address, PaintingClient client, String roomCode){
        try{

            // parameter 들을 JSON 객체로 압축
            JSONObject json = new JSONObject();

            json.put("type", type);
            json.put("roomCode", roomCode);

            ByteBuf byteBuf = Unpooled.copiedBuffer(json.toString(), CharsetUtil.UTF_8);

            // UDP 서버로 전송
            client.write(new DatagramPacket(byteBuf, address));
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
