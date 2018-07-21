package com.example.roren.auctioncast.activities;

/**
 * 1. id - 채팅을 보낸 사용자의 id
 * 2. text - 채팅 내용
 * 3. timeStamp - 채팅을 보낸 당시의 시간
 * 4. roomCode - 채팅을 하고 있는 채팅방의 고유번호
 */

public class recyclerView_item_chatting {
    private String id;
    private String text;
    private String timeStamp;
    private String roomCode;

    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }

    public void setText(String text){
        this.text = text;
    }
    public String getText(){
        return this.text;
    }

    public void setTimeStamp(String timeStamp) { this.timeStamp = timeStamp; }
    public String getTimeStamp() { return this.timeStamp; }

    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    public String getRoomCode() { return this.roomCode; }

}
