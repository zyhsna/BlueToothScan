package edu.zyh.finalproject.data;

import android.bluetooth.BluetoothGattCallback;

public class Device {
    private String deviceName;
    private String deviceHardwareAddress;
    private BluetoothGattCallback bluetoothGattCallback;

    public Device(BluetoothGattCallback bluetoothGattCallback) {
        this.bluetoothGattCallback = bluetoothGattCallback;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceHardwareAddress() {
        return deviceHardwareAddress;
    }

    public void setDeviceHardwareAddress(String deviceHardwareAddress) {
        this.deviceHardwareAddress = deviceHardwareAddress;
    }
}
