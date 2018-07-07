package com.example.roren.auctioncast.utility;

public class utility_global_variable {

    /**
     * netty 채팅 서버에 메시지를 보낼 때, 메시지의 타입을 구분하기 위해 설정한 변수들
     */
    public static final int CODE_CHAT_MAKEROOM = 1000;
    public static final int CODE_CHAT_ENTRANCE = 1001;
    public static final int CODE_CHAT_MESSAGE_GENERAL = 1002;
    public static final int CODE_CHAT_EXIT = 1003;
    public static final int CODE_CHAT_START_AUCTION = 1004;
    public static final int CODE_CHAT_STOP_AUCTION = 1005;
    public static final int CODE_CHAT_PRICE_RAISE = 1007;

    /**
     * netty 채팅 서버에 접속 할 때, 서버의 ip 주소와 port 번호
     */
    public static final String HOST = System.getProperty("host", "52.41.99.92");
    public static final int PORT = Integer.parseInt(System.getProperty("port", "5001"));

}
