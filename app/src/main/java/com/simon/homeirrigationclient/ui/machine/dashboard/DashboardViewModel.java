package com.simon.homeirrigationclient.ui.machine.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simon.homeirrigationclient.HICApplication;
import com.simon.homeirrigationclient.model.DeviceInfo;

public class DashboardViewModel extends ViewModel {

    public int indexOfDeviceInfo;
    private MutableLiveData<String> mDeviceName;
    private MutableLiveData<String> mAddressPort;
    private MutableLiveData<String> mMode;
    private MutableLiveData<String> mScheduledFreqHint;
    private MutableLiveData<String> mScheduledTimeHint;

    /*
    public DashboardViewModel(String deviceName, String addressPort) {
        mDeviceName = new MutableLiveData<>();
        //mText = new MutableLiveData<>();
        //mText.setValue("This is home fragment");
        mDeviceName.setValue(deviceName);
    }
    */

    public DashboardViewModel() {
        mDeviceName = new MutableLiveData<>();
        mAddressPort = new MutableLiveData<>();
        mMode = new MutableLiveData<>();
        mScheduledFreqHint = new MutableLiveData<>();
        mScheduledTimeHint = new MutableLiveData<>();
    }

    public LiveData<String> getDeviceName() {
        return mDeviceName;
    }

    public LiveData<String> getAddressPort() {
        return mAddressPort;
    }

    public LiveData<String> getMode() {
        return mMode;
    }

    public LiveData<String> getScheduledFreqHint() {
        return mScheduledFreqHint;
    }

    public LiveData<String> getScheduledTimeHint() {
        return mScheduledTimeHint;
    }

    public void loadData() {
        DeviceInfo currentDeviceInfo = HICApplication.getInstance().servers.get(indexOfDeviceInfo);
        mDeviceName.setValue(currentDeviceInfo.name);
        mAddressPort.setValue(currentDeviceInfo.host + ": " + currentDeviceInfo.port);
        if(currentDeviceInfo.mode == 1) {   //Scheduled
            mMode.setValue("Scheduled");
            switch(currentDeviceInfo.scheduledFreq) {
                case 1: //Every day
                    mScheduledFreqHint.setValue("Scheduled Frequency: Every day");
                    break;
                case 2: //Every 2 days
                    mScheduledFreqHint.setValue("Scheduled Frequency: Every 2 days");
                    break;
                case 3: //Every three days
                    mScheduledFreqHint.setValue("Scheduled Frequency: Every 3 days");
                    break;
                case 4: //Every week
                    mScheduledFreqHint.setValue("Scheduled Frequency: Every week");
                    break;
            }
            mScheduledTimeHint.setValue("Scheduled Time: " + currentDeviceInfo.scheduledTime);
        } else if(currentDeviceInfo.mode == 2) {    //Manual
            mMode.setValue("Manual");
            mScheduledFreqHint.setValue("");
            mScheduledTimeHint.setValue("");
        } else {
            mMode.setValue("");
            mScheduledFreqHint.setValue("");
            mScheduledTimeHint.setValue("");
        }

    }


}