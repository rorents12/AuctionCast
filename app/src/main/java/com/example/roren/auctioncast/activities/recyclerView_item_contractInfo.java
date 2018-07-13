package com.example.roren.auctioncast.activities;

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
