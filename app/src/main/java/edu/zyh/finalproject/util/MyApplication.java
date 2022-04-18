package edu.zyh.finalproject.util;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.util.UUID;

/**
 * 全局获取context
 */
public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }


    public static Context getContext(){
        return context;
    }

    public static String getDeviceUID(){
        String device_model = Build.MODEL;
        String device = Build.DEVICE;
        String board = Build.BOARD;
        String id = Build.ID;
        return device_model+"-" +device+"-"+board+"-"+id;
    }
}
