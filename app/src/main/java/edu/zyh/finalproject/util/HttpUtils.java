package edu.zyh.finalproject.util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import edu.zyh.finalproject.ui.RegisterActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * http工具类，用来发送Http请求
 */
public class HttpUtils {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");


    /**
     * 发送get请求
     *
     * @param url    地址 例如 http://127.0.0.0:8090/user/login 只需要传入/user/login
     * @param params 参数
     * @return 请求结果
     */
    public static void get(String url, Map<String, String> params, Callback callback) {
        request("get", url, params, callback);
    }

    public static void downloadImage(int userId, int taskId, String imagePath, String imageName, Handler handler) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://192.168.31.13:8081/api/v1/photo/pri/downloadImage";
        final HashMap<String, String> params = new HashMap<>();
        params.put("taskId",String.valueOf(taskId));
        params.put("userId",String.valueOf(userId));
        Gson gson = new Gson();
        String data = gson.toJson(params);
        RequestBody body = RequestBody.Companion.create(data, JSON);
        client.newCall(new Request.Builder().post(body).url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody tmpResponse = response.body();
                final Message message = new Message();
                message.what = ProjectToken.DOWNLOAD_IMAGE_FAIL;
                if (response.isSuccessful()) {
                    if (tmpResponse != null) {
                        File file = new File(imagePath + "/" + imageName);
                        if (file.exists()) {
                            final boolean delete = file.delete();
                        }
                        try {
                            InputStream inputStream = tmpResponse.byteStream();
                            final FileOutputStream outputStream = new FileOutputStream(file);
                            final byte[] bytes = new byte[4096];
                            int n;
                            if ((n = inputStream.read(bytes)) != -1) {
                                outputStream.write(bytes, 0, n);
                                while ((n = inputStream.read(bytes)) != -1) {
                                    outputStream.write(bytes, 0, n);
                                }
                            }
                            message.what = ProjectToken.DOWNLOAD_IMAGE_SUCCESS;
                        } catch (Exception e) {
                            LogUtil.error("HTTP_DOWNLOAD", e.toString());
                        }
                    } else {
                        LogUtil.debug("HTTP_DOWNLOAD", tmpResponse.string());
                    }
                }
                handler.sendMessage(message);
            }
        });
    }

    public static void upload(String url, String imgPath, int uploadUserId, int taskId, Callback callback) {
        url = "http://192.168.31.13:8081/api/v1" + url;
        File file = new File(imgPath);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse("multipart/form-data")))//文件
                .addFormDataPart("userId", String.valueOf(uploadUserId)).addFormDataPart("taskId",String.valueOf(taskId))
                .build();
        Request request = new Request.Builder()
                .url(url).post(requestBody)
                .build();
        LogUtil.info("HTTP_UPLOAD", request.toString());
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(callback);
    }

    /**
     * 发送post请求
     *
     * @param url    地址
     * @param params 参数
     */
    public static void post(String url, Map<String, String> params, Callback callback) {
        request("post", url, params, callback);
    }


    /**
     * 发送http请求
     *
     * @param method 请求方法
     * @param url    地址
     * @param params 参数
     * @return 请求结果
     */
    public static void request(String method, String url, Map<String, String> params, Callback callback) {

        if (method == null) {
            throw new RuntimeException("请求方法不能为空");
        }

        if (url == null) {
            throw new RuntimeException("url不能为空");
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        HttpUrl.Builder httpBuilder = Objects.requireNonNull(HttpUrl.parse("http://192.168.31.13:8081/api/v1" + url)).newBuilder();
        if (method.equals("get")) {
            if (params != null) {
                for (Map.Entry<String, String> param : params.entrySet()) {
                    httpBuilder.addQueryParameter(param.getKey(), param.getValue());
                }
            }
            LogUtil.info("HTTP_INFO_URL_GET", httpBuilder.toString());
            Request request = new Request.Builder()
                    .url(httpBuilder.build())
                    .get()
                    .build();
            client.newCall(request).enqueue(callback);
        }
        if (method.equals("post")) {
            Gson gson = new Gson();
            String data = gson.toJson(params);
            RequestBody body = RequestBody.Companion.create(data, JSON);
            LogUtil.info("HTTP_INFO_URL_POST", httpBuilder.toString() + "\n" + body);
            Request request = new Request
                    .Builder()
                    .post(body)
                    .url(httpBuilder.build())
                    .build();
            client.newCall(request).enqueue(callback);
        }
    }


}
