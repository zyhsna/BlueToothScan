package edu.zyh.finalproject.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.adapter.DropDeviceInfoAdapter;
import edu.zyh.finalproject.data.DeviceForNewTask;
import edu.zyh.finalproject.data.JSONData;
import edu.zyh.finalproject.data.SurroundDevice;
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.util.JSONUtil;
import edu.zyh.finalproject.util.ProjectToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DropDeviceInfo extends AppCompatActivity {
    private RecyclerView dropDeviceRecyclerView;
    private TextView textView;
    private int deviceId;
    private DropDeviceInfoAdapter dropDeviceInfoAdapter;
    private List<SurroundDevice> dropDeviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drop_device_info);
        final Intent intent = getIntent();
        deviceId = intent.getIntExtra("deviceId", 0);

        initLayout();
        getDroppedDeviceInfo(deviceId);
    }

    private void initGrid() {
        dropDeviceInfoAdapter = new DropDeviceInfoAdapter(dropDeviceList, handler);
        final GridLayoutManager manager = new GridLayoutManager(DropDeviceInfo.this, 1);
        dropDeviceRecyclerView.setAdapter(dropDeviceInfoAdapter);
        dropDeviceRecyclerView.setLayoutManager(manager);

    }

    private void getDroppedDeviceInfo(int deviceId) {
        String url = "/surroundDevice/getDropDeviceInfo";
        final HashMap<String, String> param = new HashMap<>();
        param.put("deviceId", String.valueOf(deviceId));
        HttpUtils.get(url, param, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String tmpResponse = response.body().string();
                final JSONData responseData = JSONUtil.getResponseData(tmpResponse);
                final int stateCode = responseData.getStateCode();
                final Message message = new Message();
                if (stateCode == 0) {
                    final Gson gson = new Gson();
                    message.what = ProjectToken.GET_DROP_DEVICE_INFO_SUCCESS;
                    dropDeviceList = gson.fromJson(JSONUtil.objectToJson(responseData.getData()), new TypeToken<List<SurroundDevice>>() {
                    }.getType());
                } else {
                    message.what = ProjectToken.GET_DROP_DEVICE_INFO_FAIL;
                }
                handler.sendMessage(message);
            }
        });
    }


    private void initLayout() {
        dropDeviceRecyclerView = findViewById(R.id.drop_device_recycle_view);
        textView = findViewById(R.id.text_view);
        textView.setVisibility(View.GONE);
        textView.setText("暂未有相关信息");
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ProjectToken.USER_ASK_FOR_HELP:
                    final String what = (String) msg.obj;
                    final String[] split = what.split("-");
                    userAskForHelp(Integer.parseInt(split[0]), split[1]);
                    break;
                case ProjectToken.USER_NAVI_TO_LOCATION_SELF:
                    String location = (String) msg.obj;
                    final String[] strings = location.split("-");
                    userNaviToLocationSelf(Double.parseDouble(strings[0]), Double.parseDouble(strings[1]));
                    break;
                case ProjectToken.GET_DROP_DEVICE_INFO_SUCCESS:
                    initGrid();
                    textView.setText("共有"+dropDeviceList.size()+"条信息");
                    textView.setVisibility(View.VISIBLE);
                    break;
                case ProjectToken.GET_DROP_DEVICE_INFO_FAIL:
                    textView.setVisibility(View.VISIBLE);
                    dropDeviceRecyclerView.setVisibility(View.GONE);
                    break;
            }
        }
    };

    private void userNaviToLocationSelf(double longitude, double latitude) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示")
                .setMessage("即将打开导航前往地点")
                .setCancelable(true)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final Intent intent = new Intent(DropDeviceInfo.this, MapActivity.class);
                        intent.putExtra("longitude", longitude);
                        intent.putExtra("latitude", latitude);
                        startActivity(intent);
                    }
                }).setNegativeButton("取消",null).show();
    }

    private void userAskForHelp(int scanUserId, String hardwareAddress) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示")
                .setMessage("确认请求帮助？")
                .setCancelable(true)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final Intent intent = new Intent(DropDeviceInfo.this, NewTaskActivity.class);
                        intent.putExtra("scanUserId", scanUserId);
                        intent.putExtra("hardwareAddress", hardwareAddress);
                        startActivity(intent);
                    }
                }).setNegativeButton("取消",null).show();
    }


}