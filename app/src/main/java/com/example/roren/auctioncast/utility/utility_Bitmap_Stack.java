package com.example.roren.auctioncast.utility;

import android.graphics.Bitmap;

public class utility_Bitmap_Stack {
    private int MAX_SIZE;
    private Bitmap[] stack;
    private int top;

    public utility_Bitmap_Stack() {
        MAX_SIZE = 10;
        stack = new Bitmap[MAX_SIZE];
        top = -1;
    }

    private boolean isEmpty() {
        return top == -1 ? true : false;
    }
    private boolean isFull() {
        return (top + 1 == MAX_SIZE) ? true : false;
    }

    public void push(Bitmap data) {
        if (!isFull())
            stack[++top] = data;
    }

    public Bitmap pop() {
        if (!isEmpty())
            return stack[top--];
        return null;
    }

    public void clear(){
        stack = new Bitmap[MAX_SIZE];
    }

    public void display() {
        System.out.print("top : " + top + "\nstack : ");
        for (int idx = 0; idx <= top; idx++)
            System.out.print(stack[idx] + " ");
        System.out.println();
    }

}
