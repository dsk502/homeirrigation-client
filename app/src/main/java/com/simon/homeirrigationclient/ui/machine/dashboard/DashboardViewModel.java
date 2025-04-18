package com.simon.homeirrigationclient.ui.machine.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simon.homeirrigationclient.HICApplication;
import com.simon.homeirrigationclient.model.DeviceInfo;

public class DashboardViewModel extends ViewModel {

    public int indexOfDeviceInfo;
    private MutableLiveData<String> mDeviceName;
    private MutableLiveData<String> mHost;

    private MutableLiveData<String> mPort;
    private MutableLiveData<Integer> mMode;

    private MutableLiveData<Double> mWaterAmount;
    private MutableLiveData<Integer> mScheduledFreq;
    private MutableLiveData<String> mScheduledTime;

    public DashboardViewModel() {
        mDeviceName = new MutableLiveData<>();
        mHost = new MutableLiveData<>();
        mPort = new MutableLiveData<>();
        mMode = new MutableLiveData<>();
        mWaterAmount = new MutableLiveData<>();
        mScheduledFreq = new MutableLiveData<>();
        mScheduledTime = new MutableLiveData<>();
    }

    public LiveData<String> getDeviceName() {
        return mDeviceName;
    }

    public LiveData<String> getHost() {
        return mHost;
    }
    public LiveData<String> getPort() {
        return mPort;
    }

    public LiveData<Integer> getMode() {
        return mMode;
    }

    public LiveData<Double> getWaterAmount() {
        return mWaterAmount;
    }
    public LiveData<Integer> getScheduledFreq() {
        return mScheduledFreq;
    }

    public LiveData<String> getScheduledTime() {
        return mScheduledTime;
    }

    public void loadData() {
        DeviceInfo currentDeviceInfo = HICApplication.getInstance().servers.get(indexOfDeviceInfo);
        mDeviceName.setValue(currentDeviceInfo.name);
        mHost.setValue(currentDeviceInfo.host);
        mPort.setValue(String.valueOf(currentDeviceInfo.port));
        mMode.setValue(currentDeviceInfo.mode);
        mWaterAmount.setValue(currentDeviceInfo.waterAmount);
        mScheduledFreq.setValue(currentDeviceInfo.scheduledFreq);
        mScheduledTime.setValue(currentDeviceInfo.scheduledTime);

    }


}