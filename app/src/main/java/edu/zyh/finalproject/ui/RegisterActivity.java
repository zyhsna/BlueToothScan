package edu.zyh.finalproject.ui;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.data.JSONData;
import edu.zyh.finalproject.util.JSONUtil;
import edu.zyh.finalproject.util.LogUtil;
import edu.zyh.finalproject.util.MyApplication;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RegisterActivity extends BasicActivity {

    private static final String TAG = "REGISTER_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //获取相关控件
        EditText registerUsername = findViewById(R.id.user_register_username);
        EditText registerPhone = findViewById(R.id.user_register_phone);
        EditText registerPass = findViewById(R.id.user_register_password);
        EditText registerPassCheck = findViewById(R.id.user_register_password_check);
        RadioGroup registerGender = findViewById(R.id.user_register_gender);
        RadioButton maleRadio = findViewById(R.id.male_radiobutton);
        RadioButton femaleRadio = findViewById(R.id.female_radiobutton);
        Button register = findViewById(R.id.user_register_confirm);
        maleRadio.setOnClickListener(view -> {
            if (femaleRadio.isChecked()) {
                femaleRadio.setChecked(false);
            }
        });

        femaleRadio.setOnClickListener(view -> {
            if (maleRadio.isChecked()) {
                maleRadio.setChecked(false);
            }
        });

        register.setOnClickListener(view -> {
            String username = registerUsername.getText().toString();
            String phone = registerPhone.getText().toString();
            String pass = registerPass.getText().toString();
            String passCheck = registerPassCheck.getText().toString();
            int gender = 2;
            if (femaleRadio.isChecked()) {
                gender = 1;
            } else if (maleRadio.isChecked()) {
                gender = 0;
            } else {
                Toast.makeText(RegisterActivity.this, "请选择性别", Toast.LENGTH_SHORT).show();
            }


            LogUtil.info(TAG, username + phone + pass + passCheck + gender);
            if (!pass.equals(passCheck)) {
                Toast.makeText(RegisterActivity.this, "两次密码输入不一致！", Toast.LENGTH_SHORT).show();

            } else {
                HashMap<String, String> registerInfo = new HashMap<>();
                registerInfo.put("userName", username);
                registerInfo.put("telephone", phone);
                registerInfo.put("password", pass);
                registerInfo.put("gender", String.valueOf(gender));
                registerInfo.put("userLevel", String.valueOf(1));
                registerInfo.put("userRole", String.valueOf(1));
                registerInfo.put("deviceUID", MyApplication.getDeviceUID());
                HttpUtils.post("/user/register", registerInfo, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        LogUtil.error(TAG, e.toString());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String tempResponse = response.body().string();
                        JSONData responseData = JSONUtil.getResponseData(tempResponse);
                        switch (responseData.getStateCode()) {
                            case 0:
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                                Looper.loop();
                                break;
                            case 4:
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this, "手机号已存在！", Toast.LENGTH_SHORT).show();
                                registerPhone.setText("");
                                Looper.loop();
                                break;
                            case 5:
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this, "注册失败！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                                break;
                        }

                    }
                });
            }
        });
    }
}