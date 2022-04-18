package edu.zyh.finalproject.data;

public class AuctionDetail {
    private int auctionDetailId;
    private int auctionId;
    private int bidderId;
    private double bid;
    private String bidTime;
    private int selected;
    private int participated;

    public int getParticipated() {
        return participated;
    }

    public void setParticipated(int participated) {
        this.participated = participated;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    @Override
    public String toString() {
        return "AuctionDetail{" +
                "auctionDetailId=" + auctionDetailId +
                ", auctionId=" + auctionId +
                ", bidderId=" + bidderId +
                ", bid=" + bid +
                ", bidTime='" + bidTime + '\'' +
                ", selected=" + selected +
                ", participated=" + participated +
                '}';
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public int getAuctionDetailId() {
        return auctionDetailId;
    }

    public void setAuctionDetailId(int auctionDetailId) {
        this.auctionDetailId = auctionDetailId;
    }

    public int getBidderId() {
        return bidderId;
    }

    public void setBidderId(int bidderId) {
        this.bidderId = bidderId;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public String getBidTime() {
        return bidTime;
    }

    public void setBidTime(String bidTime) {
        this.bidTime = bidTime;
    }
}
