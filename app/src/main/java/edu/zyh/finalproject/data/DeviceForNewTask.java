package edu.zyh.finalproject.data;

/**
 * 设备实体类,用于新建任务时候下拉菜单展示所用
 * @author zyhsna
 */
public class DeviceForNewTask {
    private int deviceId;
    private int ownerId;
    private double price;
    private int dropFlag;
    private double latitude;
    private double longitude;
    private String deviceName;
    private String hardwareAddress;

    @Override
    public String toString() {
        return "DeviceForNewTask{" +
                "deviceId=" + deviceId +
                ", ownerId=" + ownerId +
                ", price=" + price +
                ", dropFlag=" + dropFlag +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", deviceName='" + deviceName + '\'' +
                ", deviceUid='" + hardwareAddress + '\'' +
                '}';
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getDropFlag() {
        return dropFlag;
    }

    public void setDropFlag(int dropFlag) {
        this.dropFlag = dropFlag;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getHardwareAddress() {
        return hardwareAddress;
    }

    public void setHardwareAddress(String hardwareAddress) {
        this.hardwareAddress = hardwareAddress;
    }
}
