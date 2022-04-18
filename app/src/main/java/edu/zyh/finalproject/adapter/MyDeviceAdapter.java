package edu.zyh.finalproject.adapter;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.data.DeviceForNewTask;
import edu.zyh.finalproject.data.JSONData;
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.util.JSONUtil;
import edu.zyh.finalproject.util.ProjectToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * MyDeviceFragment的adapter
 */
public class MyDeviceAdapter extends RecyclerView.Adapter<MyDeviceAdapter.ViewHolder> {
    private Context context;
    private final List<DeviceForNewTask> deviceList;

    private Handler handler;

    public MyDeviceAdapter(List<DeviceForNewTask> deviceList, Handler handler) {
        this.deviceList = deviceList;
        this.handler = handler;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.my_device_fragment_layout, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final DeviceForNewTask device = deviceList.get(position);
        holder.deviceName.setText(device.getDeviceName());
        if (device.getDropFlag() == 0) {
            holder.deviceDropFlag.setText("当前状态: 正常");
        } else {
            holder.deviceDropFlag.setText("当前状态: 丢失");
        }
        holder.cardView.setOnClickListener(view -> {
            final Message message = new Message();
            if (device.getDropFlag() == 0) {
                message.what = ProjectToken.USER_DROP_DEVICE;
                message.obj = device;
            } else {
                //跳转到显示有多少人检测的状态
                message.what = ProjectToken.CHECK_DETECTED_INFO;
                message.obj = device.getDeviceId();
            }
            handler.sendMessage(message);
        });
    }


    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView deviceName;
        TextView deviceDropFlag;


        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            deviceName = view.findViewById(R.id.my_device_device_name);
            deviceDropFlag = view.findViewById(R.id.my_device_device_drop_flag);
        }
    }

}
