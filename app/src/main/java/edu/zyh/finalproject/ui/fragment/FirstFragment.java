package edu.zyh.finalproject.ui.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.data.CustomThread;
import edu.zyh.finalproject.data.DeviceForNewTask;
import edu.zyh.finalproject.data.JSONData;
import edu.zyh.finalproject.ui.MainActivity;
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.util.JSONUtil;
import edu.zyh.finalproject.util.LogUtil;
import edu.zyh.finalproject.util.MyApplication;
import edu.zyh.finalproject.util.ProjectToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 扫描信息的fragment
 */
public class FirstFragment extends Fragment {

    private final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "FIRST_FRAGMENT";
    private View root;
    private int userId;
    private AMapLocationClient mLocationClient = null;
    private Button startBluetoothScan;
    private Button stopBluetoothScan;
    private TextView scanInfo;
    private boolean isFirstLocate = true;

    private double latitude;
    private double longitude;

    private BluetoothAdapter bluetoothAdapter;

    private List<BluetoothDevice> deviceList = new ArrayList<>();
    //已经链接的蓝牙设备
    Set<BluetoothDevice> pairedDevices = null;

    private CustomThread customThread;


    public FirstFragment() {

    }


    public static FirstFragment newInstance() {
        FirstFragment fragment = new FirstFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_first, container, false);
        }
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ProjectToken.DATA_NAME, Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", 0);

        initLayout();
        initButtonFunction();
        initBlueTooth();
        initLocationClient();


        return root;
    }





    /**
     * 初始化布局
     */
    private void initLayout() {
        startBluetoothScan = root.findViewById(R.id.startBluetoothScan);
        stopBluetoothScan = root.findViewById(R.id.stopBluetoothScan);
        scanInfo = root.findViewById(R.id.scanInfo);
    }

    /**
     * 初始化按键功能
     */
    private void initButtonFunction() {
        startBluetoothScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userStartBluetoothScan();
            }
        });

        stopBluetoothScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userStopBluetoothScan();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void userStartBluetoothScan() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
        builder1.setTitle("提示")
                .setMessage("是否确认开启扫描，将会造成电量损失！")
                .setCancelable(true)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //开始定位
                        mLocationClient.startLocation();
                        final Message message = new Message();
                        message.what = ProjectToken.START_BLUETOOTH_SCAN_SUCCESS;
                        handler.sendMessage(message);
                        customThread = new CustomThread(bluetoothAdapter, handler);

                        final Thread thread = new Thread(customThread);
                        thread.start();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();

    }

    private void userStopBluetoothScan() {
        if (customThread != null) {
            customThread.stop();
            final Message message = new Message();
            message.what = ProjectToken.END_BLUETOOTH_SCAN_SUCCESS;
            handler.sendMessage(message);
        }
    }


    /**
     * 初始化蓝牙相关操作
     */
    @SuppressLint("MissingPermission")
    private void initBlueTooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            //不支持蓝牙
            Message message = new Message();
            message.what = ProjectToken.BLUETOOTH_NULL;
        } else {
            //如果没有开启蓝牙
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            userFindSurroundBlueToothDevice();

        }
    }


    /**
     * 用户打开蓝牙发现周围蓝牙设备
     */
    private void userFindSurroundBlueToothDevice() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().getApplicationContext().registerReceiver(receiver, filter);
    }


    /**
     * 初始化client对象
     */
    private void initLocationClient() {
        try {
            mLocationClient = new AMapLocationClient(MyApplication.getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        final AMapLocationClientOption clientOption = new AMapLocationClientOption();
        //设置GPS高精度显示
        clientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //禁止模拟定位
        clientOption.setMockEnable(false);
        //设置5s定位一次
        clientOption.setInterval(10000);
        mLocationClient.setLocationOption(clientOption);


    }

    /**
     * 自定义位置监听回调类
     */
    private AMapLocationListener mLocationListener = mapLocation -> {
        if (mapLocation != null) {
            if (mapLocation.getErrorCode() == 0) {
                latitude = mapLocation.getLatitude();//获取纬度
                longitude = mapLocation.getLongitude();//获取经度
                final String city = mapLocation.getCity();//城市信息
                final String district = mapLocation.getDistrict();//城区信息
                final String street = mapLocation.getStreet();//街道信息
                final String streetNum = mapLocation.getStreetNum();//街道门牌号信息
                LogUtil.info(TAG, "经度:" + longitude + "纬度:" + latitude);
                LogUtil.info(TAG, city + district + street + streetNum);

                //首次打开应用更新定位
                if (isFirstLocate) {
                    updateLocation(longitude, latitude);
                    isFirstLocate = false;
                }
            } else {
                //说明出错了
                LogUtil.error(TAG, mapLocation.getErrorInfo());
            }
        }
    };


    //蓝牙发现类
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                @SuppressLint("MissingPermission") String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if (!deviceList.contains(device)) {
                    //没有则加入显示
                    deviceList.add(device);
                    LogUtil.info(TAG, "发现设备上传");
                    //更新到后端数据库当中
                    updateSurroundDevice(device.getAddress());
                }
                LogUtil.info(TAG, "设备名：" + deviceName + "  MAC地址: " + deviceHardwareAddress);
                Message message = new Message();
                message.what = ProjectToken.FIND_SURROUND_BLUETOOTH_DEVICE;
                message.obj = "设备名：" + deviceName + "  MAC地址: " + deviceHardwareAddress;
                handler.sendMessage(message);
            }
        }
    };


    /**
     * 用户更新位置
     *
     * @param longitude 经度
     * @param latitude  纬度
     */
    private void updateLocation(double longitude, double latitude) {
        SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", 0);
        final String mac = sharedPreferences.getString(ProjectToken.MAC_ADDRESS, "");
        String url = "/map/updateLocation";
        HashMap<String, String> params = new HashMap<>();
        params.put("hardwareAddress", mac);
        params.put("userId", String.valueOf(userId));
        params.put("longitude", String.valueOf(longitude));
        params.put("latitude", String.valueOf(latitude));
        if (!("".equals(mac) || userId == 0)) {
            HttpUtils.get(url, params, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    LogUtil.error(TAG, e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                }
            });
        }
    }

    /**
     * 将发现的周围设备信息更新到后端数据库中
     *
     * @param address 发现设备的MAC地址
     */
    private void updateSurroundDevice(String address) {
        String url = "/surroundDevice/updateSurroundDevice";
        final HashMap<String, String> params = new HashMap<>();
        params.put("scanTime", String.valueOf(new Date().getTime()));
        params.put("hardwareAddress", address);
        params.put("longitude", String.valueOf(longitude));
        params.put("latitude", String.valueOf(latitude));
        params.put("scanUserId", String.valueOf(userId));
        HttpUtils.post(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mLocationClient.startLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationClient.stopLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getApplicationContext().unregisterReceiver(receiver);
        mLocationClient.onDestroy();
    }


    private void increaseUserPoint(int userId) {
        String url = "/user/increaseUserPoint";
        HashMap<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(userId));
        params.put("point", String.valueOf(1));
        HttpUtils.get(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }



    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ProjectToken.END_BLUETOOTH_SCAN_SUCCESS:
                    Toast.makeText(getContext(), "停止扫描成功", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.START_BLUETOOTH_SCAN_SUCCESS:
                    Toast.makeText(getContext(), "开始扫描成功", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.INCREASE_USER_POINT:
                    increaseUserPoint(userId);
                    break;


            }
        }
    };


}