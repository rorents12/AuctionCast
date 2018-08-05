package com.example.roren.auctioncast.AR_treasureHunt;

public class Point {
    public double longitude = 0f;
    public double latitude = 0f;
    public String type;
    public String price;
    public String description;
    public float x, y;

    public Point(double lat, double lon, String type, String price, String desc) {
        this.latitude = lat;
        this.longitude = lon;
        this.type = type;
        this.price = price;
        this.description = desc;
    }
}
