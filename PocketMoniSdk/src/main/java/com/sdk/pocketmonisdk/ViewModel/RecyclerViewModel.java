package com.sdk.pocketmonisdk.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sdk.pocketmonisdk.Model.RecyclerModel;

import java.util.List;

public class RecyclerViewModel extends ViewModel {
    private MutableLiveData<List<RecyclerModel>> mutableLiveData = new MutableLiveData<>();

    public void setRecyclerData(List<RecyclerModel> data){
        mutableLiveData.setValue(data);
    }

    public void postRecyclerData(List<RecyclerModel> data){
        mutableLiveData.postValue(data);
    }

    public LiveData<List<RecyclerModel>> getRecyclerData(){
        return mutableLiveData;
    }
}
