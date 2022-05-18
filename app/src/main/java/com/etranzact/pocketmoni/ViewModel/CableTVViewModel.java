package com.etranzact.pocketmoni.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.etranzact.pocketmoni.Model.CableTVModel;
import java.util.List;

public class CableTVViewModel extends ViewModel {
    private final MutableLiveData<List<CableTVModel>> mutableLiveData = new MutableLiveData<>();

    public void setRecyclerData(List<CableTVModel> data){
        mutableLiveData.setValue(data);
    }

    public void postRecyclerData(List<CableTVModel> data){
        mutableLiveData.postValue(data);
    }

    public LiveData<List<CableTVModel>> getRecyclerData(){
        return mutableLiveData;
    }
}
