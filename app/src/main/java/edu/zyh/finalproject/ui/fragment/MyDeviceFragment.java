package edu.zyh.finalproject.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.adapter.MyDeviceAdapter;
import edu.zyh.finalproject.data.DeviceForNewTask;
import edu.zyh.finalproject.data.JSONData;
import edu.zyh.finalproject.data.SurroundDevice;
import edu.zyh.finalproject.ui.DropDeviceInfo;
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.util.JSONUtil;
import edu.zyh.finalproject.util.ProjectToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 个人设备展示界面
 * @author zyhsna
 */
public class MyDeviceFragment extends Fragment {

    private View root;
    private SwipeRefreshLayout myDeviceRefreshList;
    private RecyclerView recyclerView;
    private int userId;
    private List<DeviceForNewTask> deviceList = new ArrayList<>();
    private MyDeviceAdapter deviceAdapter = null;
    private List<SurroundDevice> surroundDeviceList = new ArrayList<>();

    public MyDeviceFragment() {
        // Required empty public constructor
    }


    private void initLayout() {
        recyclerView = root.findViewById(R.id.my_device_recycler_view);
        myDeviceRefreshList = root.findViewById(R.id.my_device_refresh_list);
    }


    public static MyDeviceFragment newInstance() {
        MyDeviceFragment fragment = new MyDeviceFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_my_device, container, false);
        }
        initLayout();
        initGrid();
        initFunction();
        getBoundDevice();
        return root;
    }

    /**
     * 初始化控件功能
     */
    private void initFunction() {
        myDeviceRefreshList.setOnRefreshListener(this::getBoundDevice);
    }

    private void initGrid() {
        final GridLayoutManager manager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(manager);
        deviceAdapter = new MyDeviceAdapter(deviceList, handler);
        recyclerView.setAdapter(deviceAdapter);
    }

    public void getBoundDevice() {
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ProjectToken.DATA_NAME, getActivity().MODE_PRIVATE);
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

    private void userDropDevice(DeviceForNewTask device) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("提示")
                .setCancelable(true)
                .setMessage("确定将" + device.getDeviceName() + "挂失？")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        userDropDeviceHttp(device.getDeviceId());
                    }
                }).setNegativeButton("取消", null).show();
    }

    /**
     * 发送HTTP请求挂失设备
     * @param deviceId 设备ID
     */
    private void userDropDeviceHttp(int deviceId) {
        String url = "/device/dropDevice";
        final HashMap<String, String> params = new HashMap<>();
        params.put("deviceId", String.valueOf(deviceId));
        HttpUtils.get(url, params, new Callback() {
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
                    message.what = ProjectToken.SET_DEVICE_DROPPED_SUCCESS;
                    for (DeviceForNewTask device : deviceList) {
                        if (device.getDeviceId() == deviceId) {
                            device.setDropFlag(1);
                        }
                    }
                } else {
                    message.what = ProjectToken.SET_DEVICE_DROPPED_FAIL;
                }
                handler.sendMessage(message);
            }
        });
    }

    private void userJumpToDropDeviceInfo(int deviceId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("提示")
                .setMessage("即将跳转到详情界面，点击确认跳转")
                .setCancelable(true)
                .setPositiveButton("确认", (dialogInterface, i) -> {
                    final Intent intent = new Intent(getContext(), DropDeviceInfo.class);
                    intent.putExtra("deviceId",deviceId);
                    startActivity(intent);
                }).setNegativeButton("取消",null).show();

    }


    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ProjectToken.GET_BOUND_DEVICE_SUCCESS:
                    deviceAdapter.notifyDataSetChanged();
                    initGrid();
                    myDeviceRefreshList.setRefreshing(false);
                    break;
                case ProjectToken.SET_DEVICE_DROPPED_SUCCESS:
                    Toast.makeText(getContext(), "已成功挂失", Toast.LENGTH_SHORT).show();
                    deviceAdapter.notifyDataSetChanged();
                    initGrid();
                    break;
                case ProjectToken.SET_DEVICE_DROPPED_FAIL:
                    Toast.makeText(getContext(), "挂失失败", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.USER_DROP_DEVICE:
                    DeviceForNewTask device = (DeviceForNewTask) msg.obj;
                    userDropDevice(device);
                    break;
                case ProjectToken.CHECK_DETECTED_INFO:
                    int deviceId = (int)msg.obj;
                    userJumpToDropDeviceInfo(deviceId);
            }

        }
    };



}