package com.example.roren.auctioncast.chatting;

import com.example.roren.auctioncast.UDP_Painting.PaintingClient;
import com.example.roren.auctioncast.activities.activity_login;
import com.example.roren.auctioncast.utility.utility_global_variable;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.netty.channel.Channel;

/**
 *  netty 채팅을 위해 보낼 message 를 compress 하거나, 받은 message 를 parsing 하는 method 들을 제공하는 class
 *
 *  1. message compressor
 *      서버로 채팅 메시지를 보낼 때, 양식에 맞게 보내기 위해 JSONObject 형식의 String 으로 여러 요소들을 compress
 *      해주는 method 들을 제공.
 *
 *  2. message parser
 *      서버에서 받은 메시지를 처리하고자 할 때, JSONObject 형식의 String 으로 되어있는 message 에서 원하는 정보들을
 *      빼서 String 으로 반환해주는 method 들을 제공.
 *
 *  3. message sender
 *      compressor 를 통해 만들어낸 message 를 서버로 보내는 역할을 하는 method 를 제공.
 */

public class chatting_utility {

    private JSONObject message;

    /**
     * messageCompressor 부분
     */

    public String getJSONObjectToString(int type, String id, String text, String roomCode) throws Exception{
        JSONObject message = new JSONObject();

        message.put("type", type);
        message.put("id", id);
        message.put("text", text);
        message.put("roomCode", roomCode);

        return message.toString();
    }

    public String getJSONObjectToString(int type, String id, String text, String roomCode, String timeStamp) throws Exception{
        JSONObject message = new JSONObject();

        message.put("type", type);
        message.put("id", id);
        message.put("text", text);
        message.put("roomCode", roomCode);
        message.put("timeStamp", timeStamp);

        return message.toString();
    }

    /**
     * messageParser 부분
     */

    public int getMessageType(String msg) throws Exception{
        this.message = new JSONObject(msg);

        return message.getInt("type");
    }

    public String getMessageId(String msg) throws Exception {
        this.message = new JSONObject(msg);

        return message.getString("id");
    }

    public String getMessageText(String msg) throws Exception{
        this.message = new JSONObject(msg);

        return message.getString("text");
    }

    public JSONObject getMessageAuctionInfo(String msg) throws Exception{
        this.message = new JSONObject(msg);

        return message.getJSONObject("auctionInfo");
    }

    public String getMessageTimeStamp(String msg) throws Exception{
        this.message = new JSONObject(msg);

        return message.getString("timeStamp");
    }

    /**
     * sendMessage 부분
     */

    public void sendMessage(int code, Channel channel, String id, String text, String roomCode) throws Exception{

        String timeStamp = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());

        String m = this.getJSONObjectToString(code, id, text, roomCode, timeStamp);
        channel.writeAndFlush(m + "\n");

    }

}
