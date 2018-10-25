package com.example.kim.myapplication;

public class GameData {
    public int score,life;
    public GameData(){
        score = 0;
        life  = 3;
    }

    public void scoreProcess(int t){
        if(t%3==0)
            score += 10;
    }
}
