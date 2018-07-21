package com.example.roren.auctioncast.activities;

/**
 *  recyclerView_item_contractInfo 는 하나의 아이템이 구매정보, 판매정보, 구매구분자, 판매구분자에 대한 변수를 모두 가지고 있다.
 *
 *  1. 구매 구분자 변수
 *  String buyFence --> 이 변수가 null 이 아니라면, adapter 는 구매 구분자를 item 에 setting 한다.
 *
 *  2. 판매 구분자 변수
 *  String sellFence --> 이 변수가 null 이 아니라면, adapter 는 판매 구분자를 item 에 setting 한다.
 *
 *  3. 구매 정보 변수
 *  String seller_id, buyPrice, buyProduct
 *      위의 변수들은 구매 정보를 나타낼 때 사용된다.
 *      seller_id 가 null 이 아니라면, adapter 는 구매 정보를 item 에 setting 한다.
 *
 *  4. 판매 정보 변수
 *  String buyer_id, sellPrice, sellProduct
 *      위의 변수들은 판매 정보를 나타낼 때 사용된다.
 *      buyer_id 가 null 이 아니라면, adapter 는 판매 정보를 item 에 setting 한다.
 *
 *  5. 거래 고유번호
 *  String contractNum --> 이 변수는 거래목록에서 해당 거래에 대한 송금처리를 할 때 사용된다.
 */

public class recyclerView_item_contractInfo {
    private String seller_id;
    private String sellPrice;
    private String buyer_id;
    private String buyPrice;
    private String sellFence;
    private String buyFence;

    private String sellProduct;
    private String buyProduct;

    private String contractNum;

    public void setContractNum(String contractNum){
        this.contractNum = contractNum;
    }
    public String getContractNum(){
        return this.contractNum;
    }

    public void setSellFence(String sellFence){
        this.sellFence = sellFence;
    }
    public String getSellFence(){
        return this.sellFence;
    }

    public void setBuyFence(String buyFence){
        this.buyFence = buyFence;
    }
    public String getBuyFence(){
        return this.buyFence;
    }

    public void setSellProduct(String sellProduct){
        this.sellProduct = sellProduct;
    }
    public String getSellProduct(){
        return  this.sellProduct;
    }

    public void setBuyProduct(String buyProduct){
        this.buyProduct = buyProduct;
    }
    public String getBuyProduct(){
        return this.buyProduct;
    }

    public void setSeller_id(String id){
        this.seller_id = id;
    }
    public String getSeller_id(){
        return this.seller_id;
    }

    public void setSellPrice(String price){
        this.sellPrice = price;
    }
    public String getSellPrice(){
        return this.sellPrice;
    }

    public void setBuyer_id(String id){
        this.buyer_id = id;
    }
    public String getBuyer_id(){
        return this.buyer_id;
    }

    public void setBuyPrice(String price){
        this.buyPrice = price;
    }
    public String getBuyPrice(){
        return this.buyPrice;
    }

}
