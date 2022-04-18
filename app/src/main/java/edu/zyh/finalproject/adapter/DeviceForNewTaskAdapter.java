package edu.zyh.finalproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.data.DeviceForNewTask;

/**
 * new
 */
public class DeviceForNewTaskAdapter extends BaseAdapter {
    private final List<DeviceForNewTask> mList;
    private Context mContext;

    public DeviceForNewTaskAdapter(Context context, List<DeviceForNewTask> deviceForNewTaskList) {
        mContext = context;
        mList = deviceForNewTaskList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (mContext == null) {
            mContext = viewGroup.getContext();
        }
        final View root = LayoutInflater.from(mContext).inflate(R.layout.device_for_new_task_layout, null);

        if (root != null) {
//            TextView deviceUid = root.findViewById(R.id.bound_device_uid);
            TextView deviceName = root.findViewById(R.id.bound_device_name);
            deviceName.setText(mList.get(position).getDeviceName());
//            deviceUid.setText(mList.get(position).getHardwareAddress());
        }
        root.setPadding(0, 15, 15, 0);
        return root;
    }
}
