package edu.zyh.finalproject.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.adapter.DeviceForNewTaskAdapter;
import edu.zyh.finalproject.data.DeviceForNewTask;
import edu.zyh.finalproject.data.JSONData;
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.util.JSONUtil;
import edu.zyh.finalproject.util.LogUtil;
import edu.zyh.finalproject.util.MyApplication;
import edu.zyh.finalproject.util.ProjectToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class NewTaskActivity extends AppCompatActivity {
    private EditText taskName;
    private EditText taskInfo;
    private Button userAddNewTask;
    private Spinner deviceListSelector;
    private EditText maxReceiverSelector;
    private List<DeviceForNewTask> deviceList;
    private DeviceForNewTask deviceForNewTask = null;
    private LinearLayout maxReceiverLinearLayout;
    private Boolean openToAllUser = false;

    private CheckBox openTask;
    private int userId;

    private DeviceForNewTaskAdapter deviceForNewTaskAdapter;
    private int scanUserId;
    private String hardwareAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        initLayout();
        final Intent intent = getIntent();
        scanUserId = intent.getIntExtra("scanUserId", 0);
        hardwareAddress = intent.getStringExtra("hardwareAddress");
        if (scanUserId != 0) {

        }

    }

    /**
     * 初始化控件绑定
     */
    private void initLayout() {
        taskName = findViewById(R.id.new_task_name);
        taskInfo = findViewById(R.id.new_task_info);
        userAddNewTask = findViewById(R.id.user_add_new_task);
        deviceListSelector = findViewById(R.id.device_list);
        maxReceiverSelector = findViewById(R.id.max_receiver_num);
        maxReceiverLinearLayout = findViewById(R.id.max_receiver_liner_layout);
        maxReceiverLinearLayout.setVisibility(View.INVISIBLE);
        openTask = findViewById(R.id.open_task);
        userAddNewTask.setOnClickListener(view -> {
            addNewTask();
        });
        openTask.setOnClickListener(view -> {
            openToAllUser = !openToAllUser;
            openTask.setChecked(openToAllUser);
            if (openToAllUser) {
                maxReceiverLinearLayout.setVisibility(View.VISIBLE);
            } else {
                maxReceiverLinearLayout.setVisibility(View.INVISIBLE);
            }
        });
        //获取绑定的设备
        getBoundDevice();


    }

    private void initSelector() {
        deviceForNewTaskAdapter = new DeviceForNewTaskAdapter(NewTaskActivity.this, deviceList);
        deviceListSelector.setAdapter(deviceForNewTaskAdapter);

        deviceListSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                deviceForNewTask = (DeviceForNewTask) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void addNewTask() {
        final String taskName = this.taskName.getText().toString();
        final String taskInfo = this.taskInfo.getText().toString();
        final String maxReceiver = maxReceiverSelector.getText().toString();
        if (deviceForNewTask == null) {
            Toast.makeText(this, "请选择设备", Toast.LENGTH_SHORT).show();
        } else {

            String url = "/task/pri/insertNewTask";
            final HashMap<String, String> params = new HashMap<>();
            LogUtil.info("ADD_NEW_TASK", "ADD_NEW_TASK");
            params.put("publisherId", String.valueOf(userId));
            params.put("deviceId", String.valueOf(deviceForNewTask.getDeviceId()));
            params.put("taskName", taskName);
            params.put("taskInfo", taskInfo);

            params.put("taskLevel", "1");
            final SharedPreferences sharedPreferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
            final String hardwareAddress = sharedPreferences.getString(ProjectToken.MAC_ADDRESS, "");
            params.put("currentDeviceHardwareAddress", hardwareAddress);
            params.put("receiverNum", "0");
            params.put("accomplishFlag", "0");
            params.put("receiveFlag", "0");
            if (!openToAllUser) {
                //不开放给其他人
                params.put("open", String.valueOf(0));
                params.put("targetUserId", String.valueOf(scanUserId));
                params.put("maxReceiver", String.valueOf(1));
            } else {
                params.put("open", String.valueOf(1));
                params.put("targetUserId", String.valueOf(0));
                params.put("maxReceiver", maxReceiver);
            }

            HttpUtils.post(url, params, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    final String tmpResponse = response.body().string();
                    if (response.isSuccessful()) {
                        final JSONData responseData = JSONUtil.getResponseData(tmpResponse);
                        final int stateCode = responseData.getStateCode();
                        final Message message = new Message();
                        if (stateCode == 0) {
                            message.what = ProjectToken.ADD_TASK_SUCCESS;
                        } else if (stateCode == 25) {
                            message.what = ProjectToken.USER_POINT_LACK;
                        } else {
                            message.what = ProjectToken.ADD_TASK_FAIL;
                        }
                        handler.sendMessage(message);
                    } else {

                    }
                }
            });
        }
    }

    public void getBoundDevice() {
        final SharedPreferences sharedPreferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", 0);
        if (userId == 0) {
            //TODO 用户未登录操作
        } else {
            String url = "/device/getDeviceListByUserId";
            final HashMap<String, String> params = new HashMap<>();
            params.put("userId", String.valueOf(userId));
            HttpUtils.get(url, params, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    final String tmpResponse = response.body().string();
                    if (response.isSuccessful()) {
                        final JSONData responseData = JSONUtil.getResponseData(tmpResponse);
                        final int stateCode = responseData.getStateCode();
                        final Message message = new Message();
                        if (stateCode == 0) {
                            final Gson gson = new Gson();
                            message.what = ProjectToken.GET_BOUND_DEVICE_SUCCESS;
                            deviceList = gson.fromJson(JSONUtil.objectToJson(responseData.getData()), new TypeToken<List<DeviceForNewTask>>() {
                            }.getType());

                        } else {
                            message.what = ProjectToken.GET_BOUND_DEVICE_FAIL;
                        }
                        handler.sendMessage(message);

                    } else {

                    }
                }
            });
        }
    }


    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ProjectToken.GET_BOUND_DEVICE_SUCCESS:
//                    deviceListSelector.notifyAll();
                    initSelector();
                    break;
                case ProjectToken.GET_BOUND_DEVICE_FAIL:
                    Toast.makeText(NewTaskActivity.this, "获取绑定设备失败", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.ADD_TASK_SUCCESS:
                    Toast.makeText(NewTaskActivity.this, "新建任务成功", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.USER_POINT_LACK:
                    Toast.makeText(NewTaskActivity.this, "积分不足", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.ADD_TASK_FAIL:
                    Toast.makeText(NewTaskActivity.this, "新建任务失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}