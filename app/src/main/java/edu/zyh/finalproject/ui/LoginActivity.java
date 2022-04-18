package edu.zyh.finalproject.ui;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.data.JSONData;
import edu.zyh.finalproject.util.JSONUtil;
import edu.zyh.finalproject.util.LogUtil;
import edu.zyh.finalproject.util.MyApplication;
import edu.zyh.finalproject.util.ProjectToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends BasicActivity {


    private final String TAG = "LOGIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button loginButton = findViewById(R.id.user_login);
        Button registerButton = findViewById(R.id.user_register);

        EditText userLoginPhone = findViewById(R.id.user_login_phone);
        EditText userLoginPassword = findViewById(R.id.user_login_password);


        checkLoginStatue();

        loginButton.setOnClickListener(view -> {
            String password = userLoginPassword.getText().toString();
            String telephone = userLoginPhone.getText().toString();
            //正则判断是否为空格
            if (!(telephone.matches("^\\\\s*$") || password.matches("^\\\\s*$"))) {
                LogUtil.info(TAG, "点击登录");
                login(telephone, password);

            } else {
                Toast.makeText(LoginActivity.this, "相关信息不能为空！", Toast.LENGTH_SHORT).show();
            }
        });

        registerButton.setOnClickListener(view -> {
            Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(registerIntent);
        });

    }

    /**
     * 判断之前是否登录过
     */
    private void checkLoginStatue() {
        SharedPreferences sharedPreferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", 0);
        if (userId!=0){
            Intent registerIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(registerIntent);
            finish();
        }
    }



    private void login(String telephone, String password) {
        HashMap<String, String> loginMap = new HashMap<>();
        loginMap.put("telephone", telephone);
        loginMap.put("password", password);
        HttpUtils.get("/user/login", loginMap, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.info(TAG, e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    //暂存返回数据，防止输入流关闭
                    String tempResponse = response.body().string();
                    JSONData responseData = JSONUtil.getResponseData(tempResponse);
                    LogUtil.info(TAG, tempResponse);
                    LogUtil.info(TAG, responseData.toString());
                    switch (responseData.getStateCode()) {
                        //登录成功
                        case 0:
                            Looper.prepare();
                            Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                            //保存用户ID
                            SharedPreferences.Editor editor = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE).edit();
                            int data = Double.valueOf((double) responseData.getData()).intValue();
                            editor.putInt("userId", data);
                            editor.apply();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                            startActivity(intent);
                            finish();
                            Looper.loop();
                            break;
                        case 1:
                            Looper.prepare();
                            Toast.makeText(LoginActivity.this, "手机号未注册！", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                            break;
                        case 2:
                            Looper.prepare();
                            Toast.makeText(LoginActivity.this, "手机号与密码不匹配！", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                            break;
                        case 3:
                            Looper.prepare();
                            Toast.makeText(LoginActivity.this, "手机号或密码为空！", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                            break;
                    }
                }

            }
        });
    }



}