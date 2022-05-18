package com.etranzact.pocketmoni.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.etranzact.pocketmoni.Model.ElectricityModel;
import java.util.List;

public class ElectricityViewModel extends ViewModel {
    private final MutableLiveData<List<ElectricityModel>> mutableLiveData = new MutableLiveData<>();

    public void setRecyclerData(List<ElectricityModel> data){
        mutableLiveData.setValue(data);
    }

    public void postRecyclerData(List<ElectricityModel> data){
        mutableLiveData.postValue(data);
    }

    public LiveData<List<ElectricityModel>> getRecyclerData(){
        return mutableLiveData;
    }
}
