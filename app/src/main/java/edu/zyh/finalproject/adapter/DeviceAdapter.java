package edu.zyh.finalproject.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.util.LogUtil;
import edu.zyh.finalproject.util.ProjectToken;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    private static final String TAG = "DEVICE_ADAPTER";
    private Context context;
    private final List<BluetoothDevice> deviceList;

    private Handler handler;

    private BluetoothServerSocket serverSocket;
    private BluetoothSocket clientSocket;
    private BluetoothAdapter mBluetoothAdapter;



    public DeviceAdapter(List<BluetoothDevice> deviceList, Handler mHandler) {
        this.deviceList = deviceList;
        initBluetooth();
        handler = mHandler;
    }

    private void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView deviceName;
        TextView deviceHardwareLocation;


        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            deviceName = view.findViewById(R.id.device_name);
            deviceHardwareLocation = view.findViewById(R.id.device_hardware_address);
        }

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.device_preview_layout, parent, false);

        DeviceAdapter.ViewHolder viewHolder = new DeviceAdapter.ViewHolder(view);
    
        return viewHolder;
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = deviceList.get(position);
        holder.deviceHardwareLocation.setText(device.getAddress());
        holder.deviceName.setText(device.getName());
        holder.cardView.setOnClickListener(view -> {
            int bindingAdapterPosition = holder.getBindingAdapterPosition();
            Toast.makeText(view.getContext(), "正在连接····", Toast.LENGTH_SHORT).show();
            BluetoothGatt bluetoothGatt = device.connectGatt(view.getContext(), false, myCallback);
        });

    }

    /**
     * 自定义蓝牙连接回调类
     */
    private BluetoothGattCallback myCallback = new BluetoothGattCallback(){
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        //主要连接类
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            LogUtil.info(TAG, "CONNECTING");
            super.onConnectionStateChange(gatt, status, newState);
            Message message = new Message();
            if (status == BluetoothGatt.GATT_SUCCESS){
                //连接成功
                LogUtil.info(TAG, "CONNECTED_SUCCESS");
                message.what = ProjectToken.BLUETOOTH_CONNECT_SUCCESS;
                handler.sendMessage(message);
            }else if(status == 133){
                gatt.disconnect();
                gatt.connect();
            }else if (status == BluetoothGatt.STATE_DISCONNECTING){
                message.what = ProjectToken.BLUETOOTH_DISCONNECTING;
                handler.sendMessage(message);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
        }



    };
    

    @Override
    public int getItemCount() {
        return deviceList.size();
    }





}
