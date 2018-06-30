package com.example.roren.auctioncast.chatting;

import com.example.roren.auctioncast.activities.activity_login;
import com.example.roren.auctioncast.utility.utility_global_variable;

import org.json.JSONObject;

import io.netty.channel.Channel;

public class chatting_utility {

    private JSONObject message;

    /**
     * messageCompressor 부분
     */

    public JSONObject getJSONObject(int type, String id, String text, String roomCode) throws Exception{

        JSONObject message = new JSONObject();

        message.put("type", type);
        message.put("id", id);
        message.put("text", text);
        message.put("roomCode", roomCode);

        return message;
    }

    public String getJSONObjectToString(int type, String id, String text, String roomCode) throws Exception{
        JSONObject message = new JSONObject();

        message.put("type", type);
        message.put("id", id);
        message.put("text", text);
        message.put("roomCode", roomCode);

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

    /**
     * sendMessage 부분
     */

    public void sendMessage(int code, Channel channel, String id, String text, String roomCode) throws Exception{

        String m = this.getJSONObjectToString(code, id, text, roomCode);
        channel.writeAndFlush(m);

    }

}
