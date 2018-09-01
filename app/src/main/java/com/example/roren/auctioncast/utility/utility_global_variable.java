package com.example.roren.auctioncast.utility;

import android.graphics.Bitmap;

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
    public static final int CODE_CHAT_START_PAINTING = 1008;
    public static final int CODE_CHAT_STOP_PAINTING = 1009;

    /**
     * 그림그리기 서버에 메시지를 보낼 때, 메시지의 타입을 구분하기 위해 설정한 변수들
     */
    public static final int CODE_PAINT_START = 2000;
    public static final int CODE_PAINT_PROGRESS = 2001;
    public static final int CODE_PAINT_BEFORE_PROGRESS = 2002;
    public static final int CODE_PAINT_UNDO = 2003;
    public static final int CODE_PAINT_CLEAR = 2004;
    public static final int CODE_PAINT_STOP = 2005;

    /**
     * netty 채팅 서버에 접속 할 때, 서버의 ip 주소와 port 번호
     */
    public static final String HOST = System.getProperty("host", "52.41.99.92");
//    public static final String HOST = System.getProperty("host", "192.168.0.2");
    public static final int PORT = Integer.parseInt(System.getProperty("port", "5001"));
    public static final int PORT_UDP = Integer.parseInt(System.getProperty("port_udp", "9999"));

    /**
     * Ethereum과 통신 시, 어떠한 메소드를 이용할 것인지 구분하기 위해 설정한 변수들
     */
    public static final int CODE_ETHER_GET_BALANCE = 2000;
    public static final int CODE_ETHER_SEND_TOKEN = 2001;
    public static final int CODE_ETHER_DEPLOY_TOKEN = 2010;

    /**
     * Ethereum Token 주소와 개인 wallet 주소
     */
    public static final String AUCTION_COIN_ADDRESS = "0x0970128d2A78906DdCEEA803290C90e9B4590F9b";
    public static String WALLET_ADDRESS;
    public static String WALLET_FILE_ADDRESS;

    /**
     * 얼굴인식 AR 카메라 마스크 인덱스
     */
    public static int AR_CAMERA_MASK_INDEX_DEFAULT = 10001;
    public static final int AR_CAMERA_MASK_INDEX_PICA = 10001;
    public static final int AR_CAMERA_MASK_INDEX_EVEE = 10002;
    public static final int AR_CAMERA_MASK_INDEX_NAON = 10003;
    public static final int AR_CAMERA_MASK_INDEX_THOR = 10004;
    public static final int AR_CAMERA_MASK_INDEX_MASK = 10005;

    /**
     * 프로필사진의 서버 URL 을 저장할 변수
     */
    public static String PROFILE_IMAGE_PATH = "";
    public static final String PROFILE_DEFAULT_IMAGE_PATH = "http://52.41.99.92/user-profile-images/profile.jpg";

    /**
     * Google Cloud Vision API KEY
     */
    public static final String GOOGLE_CLOUD_VISION_API_KEY = "AIzaSyAyuTdts3HlRvIB7YT-NKu6rGuDn8F5K6o";

    public static Bitmap bitmap_capture;
}
