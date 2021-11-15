package com.etranzact.pocketmoni.View.DataTopup;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.etranzact.pocketmoni.Model.AirtimeModel;
import com.etranzact.pocketmoni.Model.DataTopUpModel;
import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.ViewAdapter.AirtimeRecyclerAdapter;
import com.etranzact.pocketmoni.ViewAdapter.DataTopUpRecyclerAdapter;
import com.etranzact.pocketmoni.ViewModel.AirtimeViewModel;
import com.etranzact.pocketmoni.ViewModel.DataTopUpViewModel;

import java.util.ArrayList;
import java.util.List;

import Utils.PosHandler;

public class DataDashboardActivity extends AppCompatActivity implements PosHandler {
    RecyclerView recyclerView;
    DataTopUpViewModel viewModel;
    DataTopUpRecyclerAdapter adapter;
    List<DataTopUpModel> recyclerData;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.data_dashboard_activity);
        setContentView(R.layout.fragment_data);
//        recyclerView = findViewById(R.id.select_discos_recycler_view);
//        recyclerData = new ArrayList<>();
//        recyclerData.add(new DataTopUpModel("Network..",""));
//        viewModel = new ViewModelProvider(this).get(DataTopUpViewModel.class);
//        viewModel.setRecyclerData(recyclerData);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setHasFixedSize(true);

//        viewModel.getRecyclerData().observe(this, (dataTopUpModels -> {
//            if(adapter != null){
//                adapter.notifyDataSetChanged();
//            }
//        }));
//        adapter = new DataTopUpRecyclerAdapter(viewModel.getRecyclerData().getValue(),this);
//        recyclerView.setAdapter(adapter);
//        getRecyclerData();
    }


//    DataTopUpModel model;
//    private void getRecyclerData(){
//        model = new DataTopUpModel(this,(response)->{
//            recyclerData.clear();
//            List<DataTopUpModel.Plan> category = model.getCategories();
//            for(DataTopUpModel.Plan plan : category){
//                recyclerData.add(new DataTopUpModel(
//                        plan.getBillsName(),
//                        plan.getImage()
//                ));
//            }
//            viewModel.setRecyclerData(recyclerData);
//        });
//    }
}
