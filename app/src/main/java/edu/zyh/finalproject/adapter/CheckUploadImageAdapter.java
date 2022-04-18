package edu.zyh.finalproject.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.data.CheckUploadImage;
import edu.zyh.finalproject.data.JSONData;
import edu.zyh.finalproject.ui.MainActivity;
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.util.JSONUtil;
import edu.zyh.finalproject.util.ProjectToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 查看已提交照片的用户adapter
 */
public class CheckUploadImageAdapter extends RecyclerView.Adapter<CheckUploadImageAdapter.ViewHolder> {
    private List<CheckUploadImage> mTaskImageList;
    private Context context;
    private Handler mHandler;


    public CheckUploadImageAdapter(List<CheckUploadImage> list, Handler handler) {
        mTaskImageList = list;
        mHandler = handler;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.check_upload_image_layout, parent, false);

        CheckUploadImageAdapter.ViewHolder viewHolder = new CheckUploadImageAdapter.ViewHolder(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final CheckUploadImage checkUploadImage = mTaskImageList.get(position);
        checkUploadImage.setPosition(position);
        holder.uploadUserName.setText(checkUploadImage.getUserName());
        holder.uploadDate.setText(formatDate(new Date()));


        holder.downloadImage.setOnClickListener(view -> {
            final Message message = new Message();
            message.what = ProjectToken.GET_UPLOAD_IMAGE_BY_USERID;
            message.obj = checkUploadImage;
            mHandler.sendMessage(message);
        });
        holder.finishTask.setOnClickListener(view -> {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
            builder1.setTitle("提示")
                    .setMessage("是否确定用户"+checkUploadImage.getUserName()+"完成任务？")
                    .setCancelable(true)
                    .setPositiveButton("确定", (dialogInterface, i) -> {
                        finishTaskFunction(checkUploadImage.getTaskId(), checkUploadImage.getUploadUserId());
                    }).setNegativeButton("取消", (dialogInterface, i) -> {
                    }).show();
        });
    }

    private void finishTaskFunction(int taskId, int userId) {
        String url = "/task/finishTask";
        final HashMap<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(userId));
        params.put("taskId", String.valueOf(taskId));
        HttpUtils.get(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String tmpResponse = response.body().string();
                if (response.isSuccessful()){
                    final JSONData responseData = JSONUtil.getResponseData(tmpResponse);
                    final int stateCode = responseData.getStateCode();
                    final Message message = new Message();
                    if (stateCode == 0){
                        message.what = ProjectToken.FINISH_TASK_SUCCESS;
                    }else {
                        message.what = ProjectToken.FINISH_TASK_FAIL;
                    }
                    mHandler.sendMessage(message);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mTaskImageList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView uploadUserName;
        TextView uploadDate;
        TextView uploadInfo;
        Button finishTask;
        Button downloadImage;


        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            uploadUserName = view.findViewById(R.id.upload_user_name);
            uploadInfo = view.findViewById(R.id.upload_info);
            uploadDate = view.findViewById(R.id.upload_date);
            finishTask = view.findViewById(R.id.finish_task);
            downloadImage = view.findViewById(R.id.download_image);
        }

    }


    private String formatDate(Date date) {
        DateFormat df4 = DateFormat.getDateInstance(DateFormat.LONG, Locale.CHINA);
        DateFormat df8 = DateFormat.getTimeInstance(DateFormat.LONG, Locale.CHINA);
        String date4 = df4.format(date);
        String time4 = df8.format(new Date());
        return date4 + " " + time4;
    }
}
