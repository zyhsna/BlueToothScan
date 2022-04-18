package edu.zyh.finalproject.util;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于方便管理所有活动
 */
public class ActivityCollector {
    private static final List<Activity> activityList = new ArrayList<>();

    public static void addActivity(Activity activity){
        activityList.add(activity);
    }

    public static void removeActivity(Activity activity){
        activityList.remove(activity);
    }

    public static void finishAll(){
        for (Activity activity : activityList) {
            if (!activity.isFinishing()){
                activity.finish();
            }
        }

    }
}
