package com.newchar.deviceview.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author newChar
 * date 2023/1/8
 * @since 设备信息
 * @since 迭代版本，（以及描述）
 */
public class DevicesInfo {

    private String mBoard;
    private String mBrand;
    private String mAndroidVersion;
    private String mHardware;
    private int mSDKVersion;
    private String mModel;

    private static volatile DevicesInfo mDevicesInfo;

    public static DevicesInfo getInstance(){
        if (mDevicesInfo == null) {
            mDevicesInfo = new DevicesInfo();
        }

        return mDevicesInfo;
    }

    public String getBrand() {
        return mBrand;
    }

    public void setBrand(String brand) {
        this.mBrand = brand;
    }

    public String getModel() {
        return mModel;
    }

    public void setModel(String model) {
        this.mModel = model;
    }

    public String getBoard() {
        return mBoard;
    }

    public void setBoard(String board) {
        this.mBoard = board;
    }

    public String getHardware() {
        return mHardware;
    }

    public void setHardware(String hardware) {
        this.mHardware = hardware;
    }

    public String getAndroidVersion() {
        return mAndroidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.mAndroidVersion = androidVersion;
    }

    public int getSDKVersion() {
        return mSDKVersion;
    }

    public void setSDKVersion(int sdkVersion) {
        this.mSDKVersion = sdkVersion;
    }

    public String toJson(){
        JSONObject rootJson = new JSONObject();
        try {
            rootJson.put("品牌", getBrand())
            .put("芯片型号", getBoard())
            .put("硬件", getHardware())
            .put("Android版本", getAndroidVersion())
            .put("Android-SDK", getSDKVersion())
            .put("设备型号", getModel());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rootJson.toString();
    }

}
