package com.etranzact.pocketmoni.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.etranzact.pocketmoni.Model.AirtimeModel;
import com.etranzact.pocketmoni.Model.DataTopUpModel;

import java.util.List;

public class DataTopUpViewModel extends ViewModel {
    private final MutableLiveData<List<DataTopUpModel>> mutableLiveData = new MutableLiveData<>();

    public void setRecyclerData(List<DataTopUpModel> data){
        mutableLiveData.setValue(data);
    }

    public void postRecyclerData(List<DataTopUpModel> data){
        mutableLiveData.postValue(data);
    }

    public LiveData<List<DataTopUpModel>> getRecyclerData(){
        return mutableLiveData;
    }
}
