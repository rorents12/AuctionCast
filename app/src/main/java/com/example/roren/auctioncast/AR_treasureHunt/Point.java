package com.example.roren.auctioncast.AR_treasureHunt;

/*
 * Portions (c) 2009 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Coby Plain coby.plain@gmail.com, Ali Muzaffar ali@muzaffar.me
 */

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
