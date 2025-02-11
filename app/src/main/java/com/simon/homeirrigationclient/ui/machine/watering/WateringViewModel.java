package com.simon.homeirrigationclient.ui.machine.watering;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WateringViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public WateringViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}