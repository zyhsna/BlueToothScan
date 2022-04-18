package edu.zyh.finalproject.ui.fragment;

import android.content.Context;
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
import android.widget.Button;
import android.widget.LinearLayout;

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
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.util.JSONUtil;
import edu.zyh.finalproject.util.LogUtil;
import edu.zyh.finalproject.util.ProjectToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
public class PersonTaskFragment extends Fragment {
    private static final String TAG = "PERSON_TASK_FRAGMENT";
    private LinearLayout receiveTaskLayout;
    private LinearLayout publishTaskLayout;
    private Button receiveTaskButton;
    private Button publishTaskButton;
    private SwipeRefreshLayout refreshReceiveTask;
    private SwipeRefreshLayout refreshPublishTask;
    private RecyclerView receiveTaskView;
    private RecyclerView publishTaskView;

    private List<Task> receiveTaskList = new ArrayList<>();
    private List<Task> publishTaskList = new ArrayList<>();

    private TaskAdapter receiveTaskAdapter;
    private TaskAdapter publishTaskAdapter;
    private int userId;

    private View root;

    public PersonTaskFragment() {

    }


    public static PersonTaskFragment newInstance() {
        PersonTaskFragment fragment = new PersonTaskFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }


    private void initRefreshFunction() {
        refreshPublishTask.setColorSchemeResources(R.color.design_default_color_primary);
        refreshReceiveTask.setColorSchemeResources(R.color.design_default_color_primary);
        new Thread(() -> {
            refreshPublishTask.setOnRefreshListener(() -> {
                initTaskList(ProjectToken.REQUEST_PUBLISH_TASK, userId);
            });
            refreshReceiveTask.setOnRefreshListener(() -> {
                initTaskList(ProjectToken.REQUEST_RECEIVE_TASK, userId);
            });
        }).start();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_person_task, container, false);
        }
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ProjectToken.DATA_NAME, Context.MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", 0);
        initLayout();

        initTaskList(ProjectToken.REQUEST_PUBLISH_TASK, userId);
        initTaskList(ProjectToken.REQUEST_RECEIVE_TASK, userId);

        initReceiveLayout();
        initPublishLayout();
        initButtonFunction();
        initRefreshFunction();
        return root;
    }

    /**
     * 初始化相关控件
     */
    private void initLayout() {
        receiveTaskButton = root.findViewById(R.id.show_receive_task);
        receiveTaskLayout = root.findViewById(R.id.receive_task_layout);
        receiveTaskView = root.findViewById(R.id.receive_task_view);
        refreshReceiveTask = root.findViewById(R.id.refresh_receive_task_list);
        receiveTaskLayout.setVisibility(View.GONE);

        publishTaskButton = root.findViewById(R.id.show_publish_task);
        publishTaskLayout = root.findViewById(R.id.publish_task_layout);
        publishTaskView = root.findViewById(R.id.publish_task_view);
        refreshPublishTask = root.findViewById(R.id.refresh_publish_task_list);
    }

    /**
     * 初始化接取到的任务控件
     */
    private void initReceiveLayout() {
        final GridLayoutManager manager = new GridLayoutManager(getContext(), 1);
        receiveTaskView.setLayoutManager(manager);
        receiveTaskAdapter = new TaskAdapter(receiveTaskList, ProjectToken.MOD_TASK_RECEIVE);
        receiveTaskView.setAdapter(receiveTaskAdapter);
    }

    /**
     * 初始化已经发布的任务控件
     */
    private void initPublishLayout() {
        final GridLayoutManager manager = new GridLayoutManager(getContext(), 1);
        publishTaskView.setLayoutManager(manager);
        publishTaskAdapter = new TaskAdapter(publishTaskList, ProjectToken.MOD_TASK_PUBLISH);
        publishTaskView.setAdapter(publishTaskAdapter);
    }

    /**
     * 初始化按钮功能
     */
    private void initButtonFunction() {
        publishTaskButton.setOnClickListener(view -> {
            publishTaskLayout.setVisibility(View.VISIBLE);
            receiveTaskLayout.setVisibility(View.GONE);
        });

        receiveTaskButton.setOnClickListener(view -> {
            receiveTaskLayout.setVisibility(View.VISIBLE);
            publishTaskLayout.setVisibility(View.GONE);
        });
    }


    private void initTaskList(String mod, int userId) {
        String url = null;
        HashMap<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(userId));

        if (mod.equals(ProjectToken.REQUEST_RECEIVE_TASK)) {
            url = "/task/getReceiveTaskByUserId";
        } else if (mod.equals(ProjectToken.REQUEST_PUBLISH_TASK)) {
            url = "/task/getPublishTaskByUserId";
        }
        if (url != null) {
            HttpUtils.get(url, params, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    LogUtil.error(TAG, "HTTP REQUEST ERROR   " + e);
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
                            Message message = new Message();
                            if (mod.equals(ProjectToken.REQUEST_RECEIVE_TASK)) {
                                receiveTaskList = gson.fromJson(JSONUtil.objectToJson(responseData.getData()), new TypeToken<List<Task>>() {
                                }.getType());
                                message.what = ProjectToken.UPDATE_RECEIVE_TASK_LIST;
                            } else {
                                publishTaskList = gson.fromJson(JSONUtil.objectToJson(responseData.getData()), new TypeToken<List<Task>>() {
                                }.getType());
                                message.what = ProjectToken.UPDATE_PUBLISH_TASK_LIST;
                            }
                            handler.sendMessage(message);
                        }
                    } else {
                        LogUtil.error(TAG, "HTTP REQUEST ERROR   " + tempResponse);
                    }
                }
            });
        }
    }


    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {//如果是更新操作，就设置文本
                case ProjectToken.UPDATE_RECEIVE_TASK_LIST:
                    initReceiveLayout();
                    receiveTaskAdapter.notifyDataSetChanged();
                    refreshReceiveTask.setRefreshing(false);
                    break;
                case ProjectToken.UPDATE_PUBLISH_TASK_LIST:
                    initPublishLayout();
                    publishTaskAdapter.notifyDataSetChanged();
                    refreshPublishTask.setRefreshing(false);
                    break;
                default:
                    break;
            }
        }
    };

}