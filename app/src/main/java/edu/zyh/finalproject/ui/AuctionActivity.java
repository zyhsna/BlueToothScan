package edu.zyh.finalproject.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.adapter.BidderAdapter;
import edu.zyh.finalproject.data.Auction;
import edu.zyh.finalproject.data.AuctionDetail;
import edu.zyh.finalproject.data.JSONData;
import edu.zyh.finalproject.data.Task;
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.util.JSONUtil;
import edu.zyh.finalproject.util.LogUtil;
import edu.zyh.finalproject.util.ProjectToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AuctionActivity extends AppCompatActivity {
    private static final String TAG = "AUCTION_ACTIVITY";


    //控件
    private TextView auctionTaskDetail;
    private TextView auctionTaskTitle;
    private TextView auctionBidderNum;

    private LinearLayout auctioneer;
    private LinearLayout bidder;

    private Button finishAuction;
    private Button auctioneerJumpToTask;
    private Button commitBid;
    private Button commitParticipate;
    private Button bidderJumpToTask;

    private EditText bidderBid;

    private RecyclerView biddersInfoRecyclerView;

    //变量
    private int taskId;
    private int userId;
    private Auction auction = null;
    private AuctionDetail auctionDetail = null;
    private List<AuctionDetail> auctionDetailList;

    private BidderAdapter bidderAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auction);
        initInfo();
        initLayout();
        initButtonFunction();
    }

    /**
     * 初始化相关信息
     */
    private void initInfo() {
        final Intent intent = getIntent();
        taskId = intent.getIntExtra("taskId", 0);
        final SharedPreferences sharedPreferences = getSharedPreferences(ProjectToken.DATA_NAME, MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", 0);
        if (taskId != 0) {
            initTaskInfo();
            initBidInfo();
        }
    }

    /**
     * 控件初始化
     */
    private void initLayout() {
        auctionBidderNum = findViewById(R.id.auction_bidder_num);
        auctionTaskDetail = findViewById(R.id.auction_task_info);
        auctionTaskTitle = findViewById(R.id.auction_task_title);

        auctioneer = findViewById(R.id.auctioneer);
        bidder = findViewById(R.id.bidder);
        finishAuction = findViewById(R.id.finish_auction);
        auctioneerJumpToTask = findViewById(R.id.auctioneer_jump_to_task);
        commitBid = findViewById(R.id.commit_bid);
        commitParticipate = findViewById(R.id.commit_participate);
        bidderJumpToTask = findViewById(R.id.bidder_jump_to_task);
        bidderBid = findViewById(R.id.bidder_bid);
        biddersInfoRecyclerView = findViewById(R.id.bidders_info);
    }

    /**
     * 初始化bidder的布局
     */
    private void initBidderLayout() {

        auctionBidderNum.setText("当前状态：");
        final double bid = auctionDetail.getBid();
        bidderBid.setText(String.valueOf(bid));
        if (auctionDetail.getSelected() == 0) {
            auctionBidderNum.append("未被选中");
        } else if (auctionDetail.getSelected() == 1 && auctionDetail.getParticipated() != 1) {
            //被选中
            commitBid.setVisibility(View.GONE);
            commitParticipate.setVisibility(View.VISIBLE);
            bidderBid.setFocusable(false);
            auctionBidderNum.append("被选中，请选择是否参与");
        } else if (auctionDetail.getSelected() == 1 && auctionDetail.getParticipated() == 1) {
            //用户确定参加
            commitBid.setVisibility(View.GONE);
            commitParticipate.setVisibility(View.GONE);
            bidderJumpToTask.setVisibility(View.VISIBLE);
            auctionBidderNum.append("确认参与");
        }
    }


/*------------------------------------------------------------------------
按钮功能部分

--------------------------------------------------------------------------*/

    /**
     * 初始化竞标信息展示
     */
    private void initGrid() {
        bidderAdapter = new BidderAdapter(auctionDetailList, auctionHandler);
        final GridLayoutManager manager = new GridLayoutManager(AuctionActivity.this, 1);
        biddersInfoRecyclerView.setAdapter(bidderAdapter);
        biddersInfoRecyclerView.setLayoutManager(manager);
    }

    /**
     * 初始化按钮功能
     */
    private void initButtonFunction() {
        commitBid.setOnClickListener(view -> {
            bidderCommitBid();
        });
        bidderJumpToTask.setOnClickListener(view -> {
            jumpToTask(1);
        });
        commitParticipate.setOnClickListener(view -> {
            bidderCommitParticipate();
        });
        auctioneerJumpToTask.setOnClickListener(view -> {
            jumpToTask(0);
        });
    }



/*------------------------------------------------------------------------
网络请求部分

--------------------------------------------------------------------------*/

    /**
     * 根据用户不同决定跳转至任务详情界面的不同
     *
     * @param mod 1 bidder 0 auctioneer
     */
    private void jumpToTask(int mod) {
        final Intent intent = new Intent(AuctionActivity.this, TaskActivity.class);
        intent.putExtra("taskId", auction.getTaskId());
        if (mod == 0) {
            //auctioneer
            intent.putExtra("mod", ProjectToken.MOD_TASK_PUBLISH);
        } else {
            //bidder
            intent.putExtra("mod", ProjectToken.MOD_TASK_RECEIVE);
        }
        startActivity(intent);
    }

    /**
     * 初始化任务信息
     */
    private void initTaskInfo() {
        String url = "/task/getTaskByTaskId";
        HashMap<String, String> params = new HashMap<>();
        params.put("taskId", String.valueOf(taskId));
        HttpUtils.get(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.error(TAG, e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String tempResponse = Objects.requireNonNull(response.body()).string();
                if (response.isSuccessful()) {
                    JSONData responseData = JSONUtil.getResponseData(tempResponse);
                    int stateCode = responseData.getStateCode();
                    if (stateCode == 0) {
                        Task task = JSONUtil.jsonToPojo(JSONUtil.objectToJson(responseData.getData()), Task.class);
                        LogUtil.info(TAG, task.toString());
                        Message message = new Message();
                        message.what = ProjectToken.UPDATE_TASK_INFO;
                        message.obj = task;
                        auctionHandler.sendMessage(message);
                    }
                } else {
                    LogUtil.info(TAG, tempResponse);
                }
            }
        });
    }

    /**
     * 初始化竞标信息
     */
    private void initBidInfo() {
        String url = "/auction/queryAuctionByTaskId";
        final HashMap<String, String> params = new HashMap<>(1);
        params.put("taskId", String.valueOf(taskId));
        HttpUtils.get(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String tempResponse = Objects.requireNonNull(response.body()).string();
                if (response.isSuccessful()) {
                    JSONData responseData = JSONUtil.getResponseData(tempResponse);
                    int stateCode = responseData.getStateCode();
                    final Message message = new Message();
                    if (stateCode == 0) {
                        final Auction auction = JSONUtil.jsonToPojo(JSONUtil.objectToJson(responseData.getData()), Auction.class);
                        message.what = ProjectToken.QUERY_AUCTION_SUCCESS;
                        message.obj = auction;
                    } else {
                        message.what = ProjectToken.QUERY_AUCTION_FAIL;
                    }
                    auctionHandler.sendMessage(message);
                } else {
                    LogUtil.info(TAG, tempResponse);
                }
            }
        });
    }

    /**
     * 拍卖者获取拍卖信息信息，例如竞拍者的竞价
     *
     * @param auctionId 拍卖ID
     */
    private void queryBidder(int auctionId) {
        String url = "/auction/queryBidder";
        final HashMap<String, String> params = new HashMap<>(1);
        params.put("auctionId", String.valueOf(auctionId));
        HttpUtils.get(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String tempResponse = Objects.requireNonNull(response.body()).string();
                if (response.isSuccessful()) {
                    JSONData responseData = JSONUtil.getResponseData(tempResponse);
                    int stateCode = responseData.getStateCode();
                    final Message message = new Message();
                    if (stateCode == 0) {
                        Gson gson = new Gson();
                        message.what = ProjectToken.QUERY_BIDDERS_SUCCESS;
                        auctionDetailList = gson.fromJson(JSONUtil.objectToJson(responseData.getData()), new TypeToken<List<AuctionDetail>>() {
                        }.getType());
                    } else {
                        message.what = ProjectToken.QUERY_BIDDERS_FAIL;
                    }
                    auctionHandler.sendMessage(message);
                } else {
                    LogUtil.info(TAG, tempResponse);
                }
            }
        });
    }

    /**
     * 竞标者获取竞标信息，例如自己的出价，是否被选上以及是否确认参与
     *
     * @param userId    出价的用户ID
     * @param auctionId 拍卖ID
     */
    private void queryBidInfo(int userId, int auctionId) {
        String url = "/auction/queryAuctionDetail";
        final HashMap<String, String> params = new HashMap<>(2);
        params.put("auctionId", String.valueOf(auctionId));
        params.put("userId", String.valueOf(userId));
        HttpUtils.get(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String tempResponse = Objects.requireNonNull(response.body()).string();
                if (response.isSuccessful()) {
                    JSONData responseData = JSONUtil.getResponseData(tempResponse);
                    int stateCode = responseData.getStateCode();
                    final Message message = new Message();
                    if (stateCode == 0) {

                        message.obj = JSONUtil.jsonToPojo(JSONUtil.objectToJson(responseData.getData()), AuctionDetail.class);
                        message.what = ProjectToken.QUERY_BIDDER_DETAIL_SUCCESS;


                    } else {
                        message.what = ProjectToken.QUERY_BIDDER_DETAIL_FAIL;
                    }
                    auctionHandler.sendMessage(message);
                } else {
                    LogUtil.info(TAG, tempResponse);
                }
            }
        });

    }

    /**
     * bidder提交报价
     */
    private void bidderCommitBid() {
        final String bid = bidderBid.getText().toString();
        final HashMap<String, String> params = new HashMap<>();
        String url = "/auction/updateBid";
        params.put("bid", bid);
        params.put("bidTime", String.valueOf(new Date().getTime()));
        params.put("bidderId", String.valueOf(userId));
        params.put("auctionId", String.valueOf(auction.getAuctionId()));
        HttpUtils.post(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String tempResponse = Objects.requireNonNull(response.body()).string();
                if (response.isSuccessful()) {
                    JSONData responseData = JSONUtil.getResponseData(tempResponse);
                    int stateCode = responseData.getStateCode();
                    final Message message = new Message();
                    if (stateCode == 0) {
                        message.what = ProjectToken.UPDATE_BID_SUCCESS;
                    } else {
                        message.what = ProjectToken.UPDATE_BID_FAIL;
                    }
                    auctionHandler.sendMessage(message);
                } else {

                    LogUtil.info(TAG, tempResponse);
                }
            }
        });
    }

    private void bidderCommitParticipate() {
        String url = "/auction/bidderParticipate";
        final HashMap<String, String> params = new HashMap<>(2);
        params.put("auctionId", String.valueOf(auction.getAuctionId()));
        params.put("bidderId", String.valueOf(userId));
        HttpUtils.get(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String tempResponse = Objects.requireNonNull(response.body()).string();
                if (response.isSuccessful()) {
                    JSONData responseData = JSONUtil.getResponseData(tempResponse);
                    int stateCode = responseData.getStateCode();
                    final Message message = new Message();
                    if (stateCode == 0) {
                        message.what = ProjectToken.BIDDER_PARTICIPATE_SUCCESS;
                    } else {
                        message.what = ProjectToken.BIDDER_PARTICIPATE_FAIL;
                    }
                    auctionHandler.sendMessage(message);
                } else {

                    LogUtil.info(TAG, tempResponse);
                }
            }

        });
    }



/*------------------------------------------------------------------------
handler 数据信息处理

--------------------------------------------------------------------------*/


    private final Handler auctionHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ProjectToken.UPDATE_TASK_INFO:
                    final Task task = (Task) msg.obj;
                    auctionTaskDetail.setText(task.getTaskInfo());
//                    auctionTaskTitle.setText(task.getTaskName());
                    break;
                case ProjectToken.QUERY_AUCTION_SUCCESS:
                    auction = (Auction) msg.obj;
                    final int auctioneerId = auction.getAuctioneerId();
                    if (auction.getState() == 0) {
                        //当前拍卖未完成
                        if (auctioneerId == userId) {
                            //如果当前是拍卖者
                            String bidderNum = "当前竞标人数：" + auction.getBidderNum();
                            auctionBidderNum.setText(bidderNum);
                            // 展示竞标信息
                            auctioneer.setVisibility(View.VISIBLE);
                            bidder.setVisibility(View.GONE);
                            //查询出价信息
                            queryBidder(auction.getAuctionId());
                        } else {
                            // 当前是竞标者
                            auctioneer.setVisibility(View.GONE);
                            bidder.setVisibility(View.VISIBLE);
                            queryBidInfo(userId, auction.getAuctionId());
                        }
                    } else {
                        //当前拍卖已完成
                        auctioneer.setVisibility(View.GONE);
                        bidder.setVisibility(View.GONE);
                        auctionBidderNum.setText("当前任务已结束！");
                    }
                    break;
                case ProjectToken.QUERY_AUCTION_FAIL:
                    //TODO 查询竞拍信息失败
                    break;
                case ProjectToken.QUERY_BIDDERS_SUCCESS:
                    initGrid();
                    break;
                case ProjectToken.QUERY_BIDDERS_FAIL:
                    Toast.makeText(AuctionActivity.this, "查询所有出价信息失败", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.QUERY_BIDDER_DETAIL_SUCCESS:
                    auctionDetail = (AuctionDetail) msg.obj;
                    initBidderLayout();
                    break;
                case ProjectToken.QUERY_BIDDER_DETAIL_FAIL:

                    Toast.makeText(AuctionActivity.this, "未出价或者出价失败", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.UPDATE_BID_SUCCESS:
                    Toast.makeText(AuctionActivity.this, "更新报价成功！", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.UPDATE_BID_FAIL:
                    Toast.makeText(AuctionActivity.this, "更新报价失败！", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.BIDDER_PARTICIPATE_SUCCESS:
                    Toast.makeText(AuctionActivity.this, "参与任务成功！", Toast.LENGTH_SHORT).show();
                    //显示参与任务按钮，隐去确认参与按钮，报价不可编辑
                    bidderJumpToTask.setVisibility(View.VISIBLE);
                    commitParticipate.setVisibility(View.GONE);
                    bidderBid.setFocusable(false);
                    break;
                case ProjectToken.BIDDER_PARTICIPATE_FAIL:
                    Toast.makeText(AuctionActivity.this, "参与任务失败！", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.SELECT_WINNER_FAIL:
                    Toast.makeText(AuctionActivity.this, "选择竞价失败！", Toast.LENGTH_SHORT).show();
                    break;
                case ProjectToken.SELECT_WINNER_SUCCESS:
                    Toast.makeText(AuctionActivity.this, "选择竞价成功！", Toast.LENGTH_SHORT).show();
                    queryBidder(auction.getAuctionId());
                    break;
            }
        }
    };
}