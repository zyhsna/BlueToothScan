package edu.zyh.finalproject.util;

import com.google.gson.Gson;

import edu.zyh.finalproject.data.JSONData;

/**
 * json转换工具类
 */
public class JSONUtil {
    private static final Gson gson = new Gson();

    public static Gson getGson() {
        return new Gson();
    }

    /**
     * 用于转换接受过来的自定义JSON数据
     *
     * @param responseData
     * @return
     */
    public static JSONData getResponseData(String responseData) {
        return gson.fromJson(responseData, JSONData.class);
    }

    public static <T> T jsonToPojo(String jsonData, Class<T> beanType) {
        return gson.fromJson(jsonData, beanType);
    }



    public static String objectToJson(Object data) {
        return gson.toJson(data);
    }
}
