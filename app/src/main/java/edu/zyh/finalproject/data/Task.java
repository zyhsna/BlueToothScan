package edu.zyh.finalproject.data;

import java.util.Date;

/**
 * 任务类
 * @author zyhsna
 */
public class Task {
    private int taskId;
    private int publisherId;
    private int deviceId;
    private int receiveFlag;
    private int accomplishFlag;
    private int receiverNum;
    private int taskLevel;
    private int maxReceiver;

    private String lastDeviceLocation;
    private String taskInfo;
    private String taskName;
    private String taskDeadline;
    private String createTime;
    private String currentDeviceHardwareAddress;

    private int open;
    private int targetUserId;

    public String getCurrentDeviceHardwareAddress() {
        return currentDeviceHardwareAddress;
    }

    public void setCurrentDeviceHardwareAddress(String currentDeviceHardwareAddress) {
        this.currentDeviceHardwareAddress = currentDeviceHardwareAddress;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public int getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(int targetUserId) {
        this.targetUserId = targetUserId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(int publisherId) {
        this.publisherId = publisherId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getReceiveFlag() {
        return receiveFlag;
    }

    public void setReceiveFlag(int receiveFlag) {
        this.receiveFlag = receiveFlag;
    }

    public int getAccomplishFlag() {
        return accomplishFlag;
    }

    public void setAccomplishFlag(int accomplishFlag) {
        this.accomplishFlag = accomplishFlag;
    }

    public int getReceiverNum() {
        return receiverNum;
    }

    public void setReceiverNum(int receiverNum) {
        this.receiverNum = receiverNum;
    }

    public int getTaskLevel() {
        return taskLevel;
    }

    public void setTaskLevel(int taskLevel) {
        this.taskLevel = taskLevel;
    }

    public int getMaxReceiver() {
        return maxReceiver;
    }

    public void setMaxReceiver(int maxReceiver) {
        this.maxReceiver = maxReceiver;
    }

    public String getLastDeviceLocation() {
        return lastDeviceLocation;
    }

    public void setLastDeviceLocation(String lastDeviceLocation) {
        this.lastDeviceLocation = lastDeviceLocation;
    }

    public String getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(String taskInfo) {
        this.taskInfo = taskInfo;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDeadline() {
        return taskDeadline;
    }

    public void setTaskDeadline(String taskDeadline) {
        this.taskDeadline = taskDeadline;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", publisherId=" + publisherId +
                ", deviceId=" + deviceId +
                ", receiveFlag=" + receiveFlag +
                ", accomplishFlag=" + accomplishFlag +
                ", receiverNum=" + receiverNum +
                ", taskLevel=" + taskLevel +
                ", maxReceiver=" + maxReceiver +
                ", lastDeviceLocation='" + lastDeviceLocation + '\'' +
                ", taskInfo='" + taskInfo + '\'' +
                ", taskName='" + taskName + '\'' +
                ", taskDeadline='" + taskDeadline + '\'' +
                ", createTime='" + createTime + '\'' +
                ", currentDeviceHardwareAddress='" + currentDeviceHardwareAddress + '\'' +
                ", open=" + open +
                ", targetUserId=" + targetUserId +
                '}';
    }
}
