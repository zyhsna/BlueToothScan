package edu.zyh.finalproject.data;

public class Auction {
    private int auctionId;
    private int taskId;
    private int auctioneerId;
    private int bidderNum;
    private int state;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Auction{" +
                "auctionId=" + auctionId +
                ", taskId=" + taskId +
                ", auctioneerId=" + auctioneerId +
                ", bidderNum=" + bidderNum +
                ", state=" + state +
                '}';
    }

    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getAuctioneerId() {
        return auctioneerId;
    }

    public void setAuctioneerId(int auctioneerId) {
        this.auctioneerId = auctioneerId;
    }

    public int getBidderNum() {
        return bidderNum;
    }

    public void setBidderNum(int bidderNum) {
        this.bidderNum = bidderNum;
    }
}
