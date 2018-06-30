//package com.example.roren.auctioncast.chatting;
//
//import org.json.JSONObject;
//
//public class chatting_messageCompressor {
//
//    public JSONObject getJSONObject(int type, String id,String text) throws Exception{
//
//        JSONObject message = new JSONObject();
//
//        message.put("type", type);
//        message.put("id", id);
//        message.put("text", text);
//
//        return message;
//    }
//
//    public String getJSONObjectToString(int type, String id, String text) throws Exception{
//        JSONObject message = new JSONObject();
//
//        message.put("type", type);
//        message.put("id", id);
//        message.put("text", text);
//
//        return message.toString();
//    }
//
//    public String getJSONObjectToString(int type, String id, String text, String roomCode) throws Exception{
//        JSONObject message = new JSONObject();
//
//        message.put("type", type);
//        message.put("id", id);
//        message.put("text", text);
//        message.put("roomCode", roomCode);
//
//        return message.toString();
//    }
//
//}
