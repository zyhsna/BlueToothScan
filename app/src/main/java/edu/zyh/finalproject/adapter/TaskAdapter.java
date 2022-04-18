package edu.zyh.finalproject.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.data.Task;
import edu.zyh.finalproject.ui.AuctionActivity;
import edu.zyh.finalproject.ui.TaskActivity;
import edu.zyh.finalproject.util.GetTaskLevel;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    private Context context;
    private final List<Task> mTaskList;
    private final int mod;


    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView taskName;
        TextView taskPublisherName;
        TextView taskCreateTime;
        TextView taskPreviewTaskLevel;
        TextView taskOpen;


        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            taskName = view.findViewById(R.id.task_name);
            taskCreateTime = view.findViewById(R.id.task_create_time);
            taskPublisherName = view.findViewById(R.id.task_publisher_name);
            taskPreviewTaskLevel = view.findViewById(R.id.task_preview_task_level);
            taskOpen = view.findViewById(R.id.task_open);
        }

    }

    public TaskAdapter(List<Task> taskList, int mod) {
        mTaskList = taskList;
        this.mod = mod;
    }

    public int getMod() {
        return mod;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.task_preview_layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = mTaskList.get(position);
        holder.taskName.setText(task.getTaskName());
        holder.taskCreateTime.setText(formatDate(new Date(Long.parseLong(task.getCreateTime()))));
        if (task.getOpen() == 1) {
            holder.taskOpen.setText("开放任务");
        }else {
            holder.taskOpen.setText("私有任务");
        }
        holder.taskPreviewTaskLevel.setText("难度：" + GetTaskLevel.getTaskLevel(task.getTaskLevel()));
        //设置点击列表展示详细任务信息
        holder.cardView.setOnClickListener(view -> {
            int position1 = holder.getBindingAdapterPosition();
            Task task1 = mTaskList.get(position1);
            Intent intent = new Intent(context, AuctionActivity.class);
            intent.putExtra("taskId", task1.getTaskId());
            intent.putExtra("taskName", task1.getTaskName());
            intent.putExtra("mod", mod);
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return mTaskList.size();
    }

    private String formatDate(Date date) {
        DateFormat df4 = DateFormat.getDateInstance(DateFormat.LONG, Locale.CHINA);
        DateFormat df8 = DateFormat.getTimeInstance(DateFormat.LONG, Locale.CHINA);
        String date4 = df4.format(date);
        String time4 = df8.format(date);
        return date4 + " " + time4.split(" ")[1];
    }

}
