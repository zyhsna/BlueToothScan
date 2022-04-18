package edu.zyh.finalproject.data;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Message;



import edu.zyh.finalproject.util.LogUtil;
import edu.zyh.finalproject.util.ProjectToken;

public class CustomThread implements Runnable {
    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private volatile boolean exit = false;
    private final Message message = new Message();

    public CustomThread(BluetoothAdapter bluetoothAdapter, Handler handler) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.handler = handler;
    }

    public void stop() {
        exit = true;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        while (!exit) {
            LogUtil.info("test", "开始一次扫描");
            bluetoothAdapter.startDiscovery();
//            message.what = ProjectToken.INCREASE_USER_POINT;
            final Message message1 = handler.obtainMessage(48);
            handler.sendMessage(message1);
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
