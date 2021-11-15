package com.etranzact.pocketmoni.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.etranzact.pocketmoni.Model.AirtimeModel;

import java.util.List;

public class AirtimeViewModel extends ViewModel {
    private final MutableLiveData<List<AirtimeModel>> mutableLiveData = new MutableLiveData<>();

    public void setRecyclerData(List<AirtimeModel> data){
        mutableLiveData.setValue(data);
    }

    public void postRecyclerData(List<AirtimeModel> data){
        mutableLiveData.postValue(data);
    }

    public LiveData<List<AirtimeModel>> getRecyclerData(){
        return mutableLiveData;
    }
}
