package com.example.kim.myapplication;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.graphics.*;
import android.view.*;

import java.util.ArrayList;

class GameActivity extends SurfaceView implements SurfaceHolder.Callback {
    public static int RUN = 1;                      // 옵션메뉴에서 사용
    public static int PAUSE = 2;
    public int mMode = RUN;
    public int t;
    public Paint paint;
    public GameData data;
    private SurfaceHolder mHolder;                // SurfaceHolder
    private MyThread mThread;      // Thread

    public BluetoothProcess bp;
    public static int Width, Height;                 // 화면의 전체 폭
    private int eWidth, eHeight;                              // 적기의 폭과 높이
    private Bitmap imgBack;
    private Bitmap[] life = new Bitmap[3];   // 배경 이미지
    private Bitmap gameLife;
    private boolean canRun = true,stopRun = true;                // 스레드 실행용 플래그
    private Bitmap Runner;
    private Bitmap Enemy, Enemy2;        // 적 이미지
    private ArrayList<MoveProcess> EnemyList;
    private ArrayList<LifeProcess> LifeList;
    public int charX = 250, charY = 150;
    int num = 0, Mdata = 10;
    String temp;
    public void ReadSprite(Context context) {
        // 화면 해상도 구하기
        Display display = ((WindowManager) context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();
        Width = display.getWidth();
        Height = display.getHeight();
        // 배경 읽기
        imgBack = BitmapFactory.decodeResource(context.getResources(), R.drawable.background2);
        imgBack = Bitmap.createScaledBitmap(imgBack, Width, Height, true);
        for (int i = 0; i < 3; i++) {
            life[i] = BitmapFactory.decodeResource(context.getResources(), R.drawable.life);
            life[i] = Bitmap.createScaledBitmap(life[i], 100, 100, true);
        }

        Runner = BitmapFactory.decodeResource(context.getResources(), R.drawable.me);
        Runner = Bitmap.createScaledBitmap(Runner, 200, 250, true);

        gameLife = BitmapFactory.decodeResource(context.getResources(), R.drawable.gamelife);
        gameLife = Bitmap.createScaledBitmap(gameLife, 170, 170, true);
        eWidth = 600;  // 건물의 폭
        eHeight = 1000;  //건물의 높이
        Enemy = BitmapFactory.decodeResource(context.getResources(), R.drawable.enemy1);
        Enemy = Bitmap.createScaledBitmap(Enemy, eWidth, eHeight, true);
        Enemy2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.enemy2);
        Enemy2 = Bitmap.createScaledBitmap(Enemy2, eWidth, eHeight, true);
    } // ReadSprite

    public GameActivity(Context context,BluetoothDevice mRemoteDevice) {
        super(context);
        bp = new BluetoothProcess();
        bp.mRemoteDevice=mRemoteDevice;
        mHolder = getHolder();
        mHolder.addCallback(this);
        ReadSprite(context);           // 비트맵 읽기
        mThread = new MyThread(mHolder, context, 1);
        setFocusable(true);
        LifeList = new ArrayList<LifeProcess>();
        EnemyList = new ArrayList<MoveProcess>();
        data = new GameData();
        paint = new Paint();
        bp.socket();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                mThread.join();
                retry = false;
            } catch (InterruptedException e) {
                //
            }
        } // while
    }

    // ---------------  여기서부터 스레드 영역이야. 넘보지 마!  -----------------------
    public class MyThread extends Thread {
        int who = 0;

        public MyThread(SurfaceHolder Holder, Context context, int who) {
            this.who = who;
            // nothing
        }

        public void run() {
            while (canRun) {
                Canvas canvas = null;
                try {
                    canvas = mHolder.lockCanvas(); //백그라운드에 그림을 그린다.
                    if(stopRun){
                        synchronized (mHolder) {
                            canvas.drawBitmap(imgBack, 0, 0, null);
                            RunnerDraw(canvas);
                            EnemyDraw(canvas);
                            LifeDraw(canvas);
                            LifeAndScore(canvas);
                            t++;
                        }
                    }else{
                        Thread.sleep(100);
                        canvas.drawBitmap(imgBack, 0, 0, null);
                        paint.setColor(Color.WHITE);
                        paint.setTextSize(300);
                        canvas.drawText("Game Over", (Width / 2) - 700, Height / 2, paint);
                    }
                        Thread.sleep(100);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                } finally {
                    if (canvas != null) mHolder.unlockCanvasAndPost(canvas);
                } // while
            }
        } // ru

       private  void RunnerDraw(Canvas canvas){
           Mdata = bp.sendData;
            if(Mdata<=num){ //아래로
                charY+=10;
            }else if(Mdata>num) { //위로
                if (charY  - 70 >= 0) charY -= 70;
            }
            canvas.drawBitmap(Runner, charX, charY, null);
            num = Mdata;
        }

        private void LifeAndScore(Canvas canvas) {
            int x = 2000;
            for (int i = 0; i < data.life; i++) {
                canvas.drawBitmap(life[i], x += 140, 50, null);
            }
            data.scoreProcess(t);
            paint.setColor(Color.BLACK);
            paint.setTextSize(100);

            canvas.drawText("SCORE: " + Integer.toString(data.score) , 1100, 140, paint);
        }

        private void LifeDraw(Canvas canvas) {
            if (t % 50 == 0) LifeList.add(new LifeProcess());
            if (mMode == PAUSE) return;
            for (int i = LifeList.size() - 1; i >= 0; --i) {      // LifeList 역순으로 검사
                if (LifeList.get(i).Move() == true)             // 적 이동후 화면을 벗어낫으면
                    LifeList.remove(i);                           // 리스트에서 삭제
             /*    if((LifeList.get(i).x-80<=(charX+70)||  //별충돌 프로세스 다시 시정하기!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
                        LifeList.get(i).x+80<=(charY+120)||LifeList.get(i).y+80<=(charY+120))){
                    data.life++;
                    LifeList.remove(i);
                }*/
            }
            if (t > 50) {
                for (LifeProcess addlife : LifeList) {// 리스트 처음부터 끝까지 canvas에 그리기
                    canvas.drawBitmap(gameLife, addlife.x, addlife.y, null);
                }
            }
        }

        private void EnemyDraw(Canvas canvas) {
            EnemyList.add(new MoveProcess());
            if (mMode == PAUSE) return;
            for (int i = EnemyList.size() - 1; i >= 0; i--) {      // EnemyList를 역순으로 검사
                if (EnemyList.get(i).Move() == true)             // 적 이동후 화면을 벗어낫으면
                    EnemyList.remove(i);
            }
            if( charY + 250 >= EnemyList.get(0).y) { // 적과 나의 충돌 범위 지정해주기!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                data.life--;
                if (data.life == 0) {   //건물과 나의 충돌시
                   stopRun=false;
                }else{
                    charX = 250; //내캐릭터의 시작위치
                    charY = 150;
                }
            }
            for (MoveProcess tEnemy : EnemyList) {// 리스트 처음부터 끝까지 canvas에 그리기
                canvas.drawBitmap(Enemy2, tEnemy.x, tEnemy.y, null);
            }
        } // EnemyDraw
    }
}


