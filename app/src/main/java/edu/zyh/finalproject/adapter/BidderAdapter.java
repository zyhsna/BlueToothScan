package edu.zyh.finalproject.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import edu.zyh.finalproject.R;
import edu.zyh.finalproject.data.AuctionDetail;
import edu.zyh.finalproject.data.JSONData;
import edu.zyh.finalproject.util.HttpUtils;
import edu.zyh.finalproject.util.JSONUtil;
import edu.zyh.finalproject.util.ProjectToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class BidderAdapter extends RecyclerView.Adapter<BidderAdapter.ViewHolder> {
    private List<AuctionDetail> auctionDetailList;
    private Context context;
    private Handler handler;

    public BidderAdapter(List<AuctionDetail> auctionDetailList, Handler handler) {
        this.auctionDetailList = auctionDetailList;
        this.handler = handler;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.bidder_layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final AuctionDetail auctionDetail = auctionDetailList.get(position);
        holder.bidderId.setText("竞拍者ID：" + auctionDetail.getBidderId());
        holder.bid.setText("出价：" + auctionDetail.getBid());
        if (auctionDetail.getSelected() == 0) {
            holder.participate.setText("出价中");
        } else if (auctionDetail.getSelected() == 1 && auctionDetail.getParticipated() == 0) {
            holder.participate.setText("等待对方选择");
        }else if (auctionDetail.getParticipated() == 1){
            holder.participate.setText("参与中");
        }
        if (auctionDetail.getSelected() == 0) {
            holder.selectBidder.setVisibility(View.VISIBLE);
            holder.selectBidder.setOnClickListener(view -> {
                auctioneerSelectBidder(auctionDetail.getAuctionId(), auctionDetail.getBidderId());
            });

        } else {
            holder.selectBidder.setVisibility(View.GONE);
        }
        holder.bidTime.setText(formatDate(new Date(Long.parseLong(auctionDetail.getBidTime()))));
    }

    /**
     * 网络请求选择竞拍者
     *
     * @param auctionId 拍卖ID
     * @param bidderId  竞拍者ID
     */
    private void auctioneerSelectBidder(int auctionId, int bidderId) {
        String url = "/auction/selectWinner";
        final HashMap<String, String> params = new HashMap<>();
        params.put("auctionId", String.valueOf(auctionId));
        params.put("bidderId", String.valueOf(bidderId));
        HttpUtils.get(url, params, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String tmpResponse = response.body().string();
                final JSONData jsonData = JSONUtil.jsonToPojo(tmpResponse, JSONData.class);
                final int stateCode = jsonData.getStateCode();
                final Message message = new Message();
                if (stateCode == 0) {
                    message.what = ProjectToken.SELECT_WINNER_SUCCESS;
                } else {
                    message.what = ProjectToken.SELECT_WINNER_FAIL;
                }
                handler.sendMessage(message);
            }
        });
    }

    private String formatDate(Date date) {
        DateFormat df4 = DateFormat.getDateInstance(DateFormat.LONG, Locale.CHINA);
        DateFormat df8 = DateFormat.getTimeInstance(DateFormat.LONG, Locale.CHINA);
        String date4 = df4.format(date);
        String time4 = df8.format(date);
        return date4 + " " + time4.split(" ")[1];
    }

    @Override
    public int getItemCount() {
        return auctionDetailList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView bidderId;
        private TextView bid;
        private TextView participate;
        private Button selectBidder;
        private TextView bidTime;

        public ViewHolder(@NonNull View view) {
            super(view);
            cardView = (CardView) view;
            bidderId = view.findViewById(R.id.bidder_id);
            bid = view.findViewById(R.id.bid);
            participate = view.findViewById(R.id.participate);
            selectBidder = view.findViewById(R.id.select_bidder);
            bidTime = view.findViewById(R.id.bid_time);
        }
    }


}
