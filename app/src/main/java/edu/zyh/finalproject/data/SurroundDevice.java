package edu.zyh.finalproject.data;

import androidx.annotation.NonNull;

public class SurroundDevice {
    private int sdId;
    private String scanTime;
    private double longitude;
    private double latitude;
    private int scanUserId;
    private String hardwareAddress;

    public String getHardwareAddress() {
        return hardwareAddress;
    }

    public void setHardwareAddress(String hardwareAddress) {
        this.hardwareAddress = hardwareAddress;
    }

    public int getSdId() {
        return sdId;
    }

    public void setSdId(int sdId) {
        this.sdId = sdId;
    }


    public String getScanTime() {
        return scanTime;
    }

    public void setScanTime(String scanTime) {
        this.scanTime = scanTime;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getScanUserId() {
        return scanUserId;
    }

    public void setScanUserId(int scanUserId) {
        this.scanUserId = scanUserId;
    }


    @Override
    public String toString() {
        return "SurroundDevice{" +
                "sdId=" + sdId +
                ", scanTime='" + scanTime + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", scanUserId=" + scanUserId +
                ", hardwareAddress='" + hardwareAddress + '\'' +
                '}';
    }
}
