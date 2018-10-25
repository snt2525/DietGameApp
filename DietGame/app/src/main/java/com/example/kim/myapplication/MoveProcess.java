package com.example.kim.myapplication;

public class MoveProcess{
    public  int x, y;              // 적기의 현재 위치
    public int weight;       // 난수 발생용

    public MoveProcess() {
        x = 2500; //위치 x,
        y = (int)(Math.random()*700)+620; //위치y
        weight = 600; //너비랑 같아야한다
    }
    public boolean Move() {
        x -= weight;                 // Vec에 정의한 방향으로 이동한다
        if (y > GameActivity.Height || y < 0 || x > GameActivity.Width || x < 0) //범위 벗어나면 list에서 제거
            return true;
        else
            return false;
    } // Move
} // MyEnemy