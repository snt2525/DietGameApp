package com.example.kim.myapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
    import android.os.Handler;

    import java.io.IOException;
    import java.io.InputStream;
    import java.io.OutputStream;
    import java.util.UUID;

    /**
     * Created by kim on 2017-12-07.
     */
    public class BluetoothProcess  {
        byte[] readBuffer;
        InputStream mInputStream = null;
        OutputStream mOutputStream = null;
        Thread mWorkerThread;
        int readBufferPosition;
        char mCharDelimiter = '\n';
        int sendData;
        int sd;
        BluetoothSocket mSocket = null;
        BluetoothDevice mRemoteDevice;

        void socket() {
            try {
                // 소켓 생성
               // sendData= "zzz";
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
                mSocket = mRemoteDevice.createRfcommSocketToServiceRecord(uuid);
                // RFCOMM 채널을 통한 연결
                mSocket.connect();
                mInputStream = mSocket.getInputStream();
                mOutputStream = mSocket.getOutputStream();
                beginListenForData(); //블루투스엑티비티에서 데이터 받기
            }catch(Exception e) {
            }
        }

        void beginListenForData(){
            final Handler handler = new Handler();
            readBuffer = new byte[1024];
            readBufferPosition = 0;
            mWorkerThread = new Thread(new Runnable() {
                @Override
                public void run(){
                    while(!Thread.currentThread().isInterrupted()){
                        try {
                            int bytesAvailable = mInputStream.available();
                            if(bytesAvailable>0){
                                byte[] packectBytes = new byte[bytesAvailable];
                                mInputStream.read(packectBytes);
                                for(int i=0;i<bytesAvailable;i++){
                                    byte b = packectBytes[i];
                                    if(b==mCharDelimiter) {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        final String data = new String(encodedBytes, "UTF-8"); //byte를 string으로 변환
                                        readBufferPosition = 0;
                                        handler.post(new Runnable() {
                                            public void run() {
                                                String a[] = data.trim().split("");
                                                sendData = Integer.parseInt(a[1]);
                                            }
                                        });
                                    }else{
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }
                        }catch(IOException e){

                        }
                    }
                }
            });
            mWorkerThread.start();
        }
}
