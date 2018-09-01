package com.example.roren.auctioncast.utility;

import android.graphics.Bitmap;
import java.util.ArrayList;

/**
 *  push, pop 메소드를 이용하여 Bitmap 을 ArrayList 에 넣고 뺄 수 있는 Stack 자료구조 클래스이다.
 *
 *  최대 20개의 bitmap 을 ArrayList 내에 저장할 수 있으며, push 메소드를 이용하여 ArrayList 가장 앞에
 *  bitmap 을 삽입할 수 있고, pop 메소드를 이용하여 ArrayList 의 가장 앞에 있는 bitmap 을 꺼낼 수 있다.
 */

public class utility_Bitmap_Stack {
    private ArrayList<Bitmap> array;

    public utility_Bitmap_Stack() {
        array = new ArrayList<Bitmap>();
    }

    // ArrayList 의 가장 앞에 bitmap 을 삽입. ArrayList 의 size 를 체크하여 bitmap 의 갯수가 20개를 넘으면
    // 맨 마지막의 bitmap 을 삭제한다.
    public void push(Bitmap data) {
        array.add(0, data);
        if(array.size() > 20){
            array.remove(20);
        }

    }

    // ArrayList 의 가장 앞에 위치한 bitmap 을 반환하고, 해당 bitmap 을 삭제한다.
    public Bitmap pop() {
        if(!array.isEmpty()){
            Bitmap b = array.get(0);
            array.remove(0);
            return b;
        }
        return null;
    }

    // ArrayList 초기화
    public void clear(){
        array.clear();
    }


}
