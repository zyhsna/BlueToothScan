package edu.zyh.finalproject.util;

import android.util.Log;

/**
 * 自定义日志工具
 */
public class LogUtil {
    private static final int VERBOSE = 1;
    private static final int DEBUG = 2;
    private static final int INFO = 3;
    private static final int WARN = 4;
    private static final int ERROR = 5;
    private static final int NOTING = 6;

    private static int level = VERBOSE;

    public static void verbose(String tag, String msg){
        if (level<= VERBOSE){
            Log.v(tag, "v: " +msg);
        }
    }
    public static void debug(String tag, String msg){
        if (level<= DEBUG){
            Log.d(tag, "d: " +msg);
        }
    }
    public static void info(String tag, String msg){
        if (level<= INFO){
            Log.i(tag, "i: " +msg);
        }
    }
    public static void warn(String tag, String msg){
        if (level<= WARN){
            Log.w(tag, "w: " +msg);
        }
    }
    public static void error(String tag, String msg){
        if (level<= ERROR){
            Log.e(tag, "e: " +msg);
        }
    }
}
