package edu.zyh.finalproject.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.data.JSONData;
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.util.JSONUtil;
import edu.zyh.finalproject.util.MyApplication;
import edu.zyh.finalproject.util.ProjectToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class BindDeviceActivity extends AppCompatActivity {
    private EditText deviceName;
    private EditText devicePrice;
    private Button bindDevice;
    private EditText deviceId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_device);
        initLayout();
    }

    private void initLayout() {
        deviceName = findViewById(R.id.bind_device_name);
        devicePrice = findViewById(R.id.bind_device_price);
        deviceId = findViewById(R.id.device_id);
        final SharedPreferences sharedPreferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
        final String mac = sharedPreferences.getString(ProjectToken.MAC_ADDRESS, "");
        deviceId.setText(mac);
        bindDevice = findViewById(R.id.user_bind_device);
        bindDevice.setOnClickListener(view -> {
            showDialog();
        });
    }


    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(BindDeviceActivity.this);
        builder.setTitle("提示")
                .setMessage("是否绑定设备")
                .setCancelable(true)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences sharedPreferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
                        int userId = sharedPreferences.getInt("userId", 0);
                        Message message = new Message();
                        if (userId == 0) {
                            //用户未登录
                            Intent intent = new Intent(BindDeviceActivity.this, LoginActivity.class);
                            message.what = ProjectToken.USER_OFF_LINE;
                            handler.sendMessage(message);
                            startActivity(intent);
                        }
                        insertNewDevice(userId, deviceId.getText().toString(), deviceName.getText().toString(), devicePrice.getText().toString());
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();
    }

    private void insertNewDevice(int userId, String deviceUID, String deviceName, String devicePrice) {
        String url = "/device/insertNewDevice";
        HashMap<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(userId));
        params.put("hardwareAddress", deviceUID);
        params.put("deviceName",deviceName);
        params.put("devicePrice", devicePrice);
        HttpUtils.get(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String tmpResponse = response.body().string();
                if (response.isSuccessful()) {
                    JSONData responseData = JSONUtil.getResponseData(tmpResponse);
                    int stateCode = responseData.getStateCode();
                    Message message = new Message();
                    if (stateCode == 0) {
                        message.what = ProjectToken.BIND_DEVICE_SUCCESS;
                    } else if (stateCode == 19) {
                        message.what = ProjectToken.DEVICE_BINDED;
                    } else if (stateCode == 20) {
                        message.what = ProjectToken.BIND_DEVICE_FAIL;
                    }
                    handler.sendMessage(message);
                }
            }
        });
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ProjectToken.BIND_DEVICE_FAIL:
                    Toast.makeText(BindDeviceActivity.this, "绑定失败", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.BIND_DEVICE_SUCCESS:
                    Toast.makeText(BindDeviceActivity.this, "绑定成功", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.DEVICE_BINDED:
                    Toast.makeText(BindDeviceActivity.this, "设备已绑定,请勿重复绑定", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}