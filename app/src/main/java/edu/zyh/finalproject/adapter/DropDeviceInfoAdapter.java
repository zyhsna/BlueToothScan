package edu.zyh.finalproject.adapter;

import android.content.Context;
import android.content.Intent;
import android.nfc.cardemulation.HostNfcFService;
import android.os.Handler;
import android.os.Message;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.data.SurroundDevice;
import edu.zyh.finalproject.ui.DropDeviceInfo;
import edu.zyh.finalproject.util.ProjectToken;

public class DropDeviceInfoAdapter extends RecyclerView.Adapter<DropDeviceInfoAdapter.ViewHolder> {

    private Context context;

    private List<SurroundDevice> surroundDeviceList;
    private Handler handler;

    public DropDeviceInfoAdapter(List<SurroundDevice> surroundDeviceList, Handler handler) {
        this.surroundDeviceList = surroundDeviceList;
        this.handler = handler;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.drop_device_info, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final SurroundDevice surroundDevice = surroundDeviceList.get(position);
        holder.scanTime.setText(formatDate(new Date(Long.parseLong(surroundDevice.getScanTime()))));
        String location = "经度："+ surroundDevice.getLongitude() +"  " +"纬度："+surroundDevice.getLatitude();
        holder.scanLocation.setText(location);
        holder.scanUserName.setText("发现用户名："+surroundDevice.getScanUserId());

        holder.askForHelp.setOnClickListener(view -> {
            userAskForHelp(surroundDevice.getScanUserId(), surroundDevice.getHardwareAddress());
        });
        holder.naviToSelf.setOnClickListener(view -> {
            userNaviToLocationSelf(surroundDevice.getLongitude(),surroundDevice.getLatitude());
        });
    }

    private void userAskForHelp(int scanUserId, String macAddress) {
        final Message message = new Message();
        message.what = ProjectToken.USER_ASK_FOR_HELP;
        message.obj = scanUserId + "-" + macAddress;
        handler.sendMessage(message);
    }

    private void userNaviToLocationSelf(double longitude, double latitude) {
        final Message message = new Message();
        message.what = ProjectToken.USER_NAVI_TO_LOCATION_SELF;
        message.obj = longitude + "-" + latitude;
        handler.sendMessage(message);
    }

    @Override
    public int getItemCount() {
        return surroundDeviceList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        private TextView scanTime;
        private TextView scanUserName;
        private TextView scanLocation;
        private Button naviToSelf;
        private Button askForHelp;


        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            scanUserName = view.findViewById(R.id.scan_user_name);
            scanLocation = view.findViewById(R.id.scan_location);
            scanTime = view.findViewById(R.id.scan_time);
            naviToSelf = view.findViewById(R.id.navi_to_location_self);
            askForHelp = view.findViewById(R.id.send_task);
        }
    }
    private String formatDate(Date date) {
        DateFormat df4 = DateFormat.getDateInstance(DateFormat.LONG, Locale.CHINA);
        DateFormat df8 = DateFormat.getTimeInstance(DateFormat.LONG, Locale.CHINA);
        String date4 = df4.format(date);
        String time4 = df8.format(date);
        return date4 + " " + time4.split(" ")[1];
    }
}
