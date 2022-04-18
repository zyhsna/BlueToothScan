package edu.zyh.finalproject.util;

public class GetTaskLevel {
    public static String getTaskLevel(int taskLevel){
        switch (taskLevel){
            case 1:return "简单";
            case 2:return "较容易";
            case 3:return "一般";
            case 4:return "中等";
            case 5:return "较困难";
            case 6:return "困难";
            default:return "未知";
        }
    }
}
