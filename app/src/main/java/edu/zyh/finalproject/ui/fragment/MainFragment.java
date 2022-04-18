package edu.zyh.finalproject.ui.fragment;

import android.content.Context;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.adapter.TaskAdapter;
import edu.zyh.finalproject.data.JSONData;
import edu.zyh.finalproject.data.Task;
import edu.zyh.finalproject.ui.MainActivity;
import edu.zyh.finalproject.ui.NewTaskActivity;
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.util.JSONUtil;
import edu.zyh.finalproject.util.LogUtil;
import edu.zyh.finalproject.util.ProjectToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class MainFragment extends Fragment {
    private static final String TAG = "MAIN_FRAGMENT";
    private SwipeRefreshLayout swipeRefreshLayout;

    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private FloatingActionButton addNewTask;

    private View root;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_main, container, false);
        }
        initTaskList();

        setTaskPreviewGrid();
        setSwipeRefresh();
        initFloatButton();

        return root;
    }

    private void initFloatButton() {
        addNewTask = root.findViewById(R.id.add_new_task);
        addNewTask.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), NewTaskActivity.class);
            startActivity(intent);
        });
    }


    public static MainFragment newInstance() {

        Bundle args = new Bundle();

        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * 初始化任务展示列表
     */
    private void setTaskPreviewGrid() {

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        GridLayoutManager manager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(manager);
        taskAdapter = new TaskAdapter(taskList, ProjectToken.MOD_TASK_LIST);
        recyclerView.setAdapter(taskAdapter);
    }

    /**
     * 设置下拉刷新
     */
    private void setSwipeRefresh() {
        swipeRefreshLayout = root.findViewById(R.id.refresh_list);
        swipeRefreshLayout.setColorSchemeResources(R.color.design_default_color_primary);
        //重新刷新数据
        swipeRefreshLayout.setOnRefreshListener(this::initTaskList);
    }

    /**
     * 网络请求数据获得任务列表
     */
    private void initTaskList() {
        String url = "/task/getUnFinishedTask";
        HashMap<String, String> params = new HashMap<>();
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ProjectToken.DATA_NAME, Context.MODE_PRIVATE);
        final int userId = sharedPreferences.getInt("userId", 0);
        params.put("userId",String.valueOf(userId));
        HttpUtils.get(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.error(TAG, "HTTP REQUEST ERROR   " + e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String tempResponse = response.body().string();
                if (response.isSuccessful()) {

                    JSONData responseData = JSONUtil.getResponseData(tempResponse);
                    int stateCode = responseData.getStateCode();
                    LogUtil.info(TAG, tempResponse);
                    if (stateCode == 0) {
                        Gson gson = new Gson();
                        taskList = gson.fromJson(JSONUtil.objectToJson(responseData.getData()), new TypeToken<List<Task>>() {
                        }.getType());
                        Message message = new Message();
                        message.what = ProjectToken.UPDATE_TASK_LIST;
                        handler.sendMessage(message);
                    }
                } else {
                    LogUtil.error(TAG, "HTTP REQUEST ERROR   " + tempResponse);
                }
            }
        });
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {//如果是更新操作，就设置文本
                case ProjectToken.UPDATE_TASK_LIST:
                    //初始化相关任务列表
                    setTaskPreviewGrid();
                    taskAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                    break;
            }
        }
    };

}