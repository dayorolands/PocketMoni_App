package com.sdk.pocketmonisdk.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import Utils.AidClass;

public class AppSelectViewModel extends ViewModel {
    private MutableLiveData<List<AidClass>> mutableLiveData = new MutableLiveData<>();

    public void setAppSelectData(List<AidClass> data){
        mutableLiveData.setValue(data);
    }

    public LiveData<List<AidClass>> getAppSelect(){
        return mutableLiveData;
    }
}
