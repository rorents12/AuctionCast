//package com.example.roren.auctioncast;
//
//import org.json.JSONObject;
//
//public class chatting_messageParser{
//
//        private JSONObject message;
//
//        public int getMessageType(String msg) throws Exception{
//            this.message = new JSONObject(msg);
//
//            return message.getInt("type");
//        }
//
//        public String getMessageId(String msg) throws Exception {
//            this.message = new JSONObject(msg);
//
//            return message.getString("id");
//        }
//
//        public String getMessageText(String msg) throws Exception{
//            this.message = new JSONObject(msg);
//
//            return message.getString("text");
//        }
//}
