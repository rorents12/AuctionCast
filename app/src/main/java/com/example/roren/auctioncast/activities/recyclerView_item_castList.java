package com.example.roren.auctioncast.activities;

/**
 * activity_broadcasting_list 에 RecyclerView 로 List 를 띄우기 위해 만든 item.
 *
 * List의 아이템 하나가 가지는 변수는 다음과 같다.
 *
 * 1. String url_thumbnail --> 방송 썸네일의 url 값
 * 2. String title_boradcasting --> 방송 제목
 * 3. String id_broadcaster --> 방송자 아이디
 * 4. String num_viewer --> 시청자 수
 *
 * item의 변수는 외부에서 직접 접근이 불가능하고, 아래에 정의된 set/get 메소드를 통해 변수를 변경하고 수정할 수 있다.
 *
 *
 * 참고사항
 *
 * bool_broadcast_start와 bool_auctioin_start는 후에 방송 예약기능을 업데이트 할 시 필요한 변수들이다. 현재는 사용하지 않는다.
 */


public class recyclerView_item_castList {

    private String url_thumbnail;
    private String title_broadcasting;
    private String id_broadcaster;
    private String num_viewer;
    private String identity_num;
    private boolean bool_broadcast_start;
    private boolean bool_auction_start;

    public void setIdentity_num(String identity_num){
        this.identity_num = identity_num;
    }
    public String getIdentity_num(){
        return this.identity_num;
    }

    public void setUrl_thumbnail(String url_thumbnail){
        this.url_thumbnail = url_thumbnail;
    }
    public String getUrl_thumbnail(){
        return this.url_thumbnail;
    }


    public void setTitle_broadcasting(String title_broadcasting){
        this.title_broadcasting = title_broadcasting;
    }
    public String getTitle_broadcasting(){
        return this.title_broadcasting;
    }


    public void setId_broadcaster(String id_broadcaster){
        this.id_broadcaster = id_broadcaster;
    }
    public String getId_broadcaster(){
        return this.id_broadcaster;
    }


    public void setNum_viewer(String num_viewer){
        this.num_viewer = num_viewer;
    }
    public String getNum_viewer(){
        return this.num_viewer;
    }

}
