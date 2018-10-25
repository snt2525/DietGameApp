package com.example.kim.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IntroActivity extends Activity {
    byte[] readBuffer;
    int readBufferPosition;
    char mCharDelimiter = '\n';
    String sendData;
    static final int REQUEST_ENABLE_BT=10;
    BluetoothAdapter mBluetoothAdapter;
    int mPairedDeviceCount=0;
    Set<BluetoothDevice> mDevices;
    InputStream mInputStream = null;
    OutputStream mOutputStream = null;
    Thread mWorkerThread;
    BluetoothSocket mSocket = null;
    BluetoothDevice mRemoteDevice;

    public BluetoothProcess bp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_intro);
        Button sBtn = (Button) findViewById(R.id.Startbtn);
        bp = new BluetoothProcess();
       sBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               checkBluetooth();
            }
        });
    }
    public void checkBluetooth() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> set = mBluetoothAdapter.getBondedDevices();
            if (set.size() <= 0) {
                askConnectBluetooth();
            }else {
                setContentView(new GameActivity(this,mRemoteDevice));
            }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode==RESULT_OK){
                    selectDevice();
                }else if(resultCode == RESULT_CANCELED){
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void askConnectBluetooth(){  //블루투스를 연결할까요? 하고 뜬다.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter==null){
            finish();
        }else{
            if(!mBluetoothAdapter.isEnabled()){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            }else if(mBluetoothAdapter.isEnabled()) {
                selectDevice();
            }
        }
    }
    void selectDevice(){
        mDevices = mBluetoothAdapter.getBondedDevices();
        mPairedDeviceCount = mDevices.size();

        if(mPairedDeviceCount == 0){
            //스위치 비활성화
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("블루투스 장치 선택");

        List<String> listItems = new ArrayList<String>();
        for(BluetoothDevice device : mDevices){
            listItems.add(device.getName());
        }
        listItems.add("취소");

        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);

        builder.setItems(items,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if(item == mPairedDeviceCount){
                    // 스위치 비활성화
                    Toast.makeText(getApplication(), "장치를 연결해 주세요", Toast.LENGTH_SHORT).show();
//                    sw1.setChecked(false);
                    //bluetooth
                }else{
                    connectToSelectdDevice(items[item].toString());
                }
            }
        });
        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }
    BluetoothDevice getDeviceFromBondedList(String name) {
        BluetoothDevice selectedDevice = null;

        for (BluetoothDevice device : mDevices) {
            if (name.equals(device.getName())) {
                selectedDevice = device;
                break;
            }
        }
        return selectedDevice;
    }
    @Override
    protected void onDestroy(){
        try{
            mWorkerThread.interrupt();
            mInputStream.close();
            mOutputStream.close();
            mSocket.close();
        }catch(Exception e){
            super.onDestroy();
        }
    }

    void connectToSelectdDevice(String selectedDeviceName) {
        mRemoteDevice = getDeviceFromBondedList(selectedDeviceName);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        //00001101-0000-1000-8000-00805f9b34fb
        setContentView(new GameActivity(this,mRemoteDevice));
    }
}

