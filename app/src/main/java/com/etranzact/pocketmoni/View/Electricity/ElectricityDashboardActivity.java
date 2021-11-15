package com.etranzact.pocketmoni.View.Electricity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.etranzact.pocketmoni.Model.ElectricityModel;
import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.ViewAdapter.ElectricityRecyclerAdapter;
import com.etranzact.pocketmoni.ViewModel.ElectricityViewModel;
import java.util.ArrayList;
import java.util.List;
import Utils.PosHandler;

public class ElectricityDashboardActivity extends AppCompatActivity implements PosHandler {

    RecyclerView recyclerView;
    ElectricityViewModel viewModel;
    ElectricityRecyclerAdapter adapter;
    List<ElectricityModel> recyclerData;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.electricity_dashboard_activity);
        recyclerView = findViewById(R.id.select_discos_recycler_view);
        recyclerData = new ArrayList<>();
        recyclerData.add(new ElectricityModel("disco..",""));
        viewModel = new ViewModelProvider(this).get(ElectricityViewModel.class);
        viewModel.setRecyclerData(recyclerData);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        viewModel.getRecyclerData().observe(this, (electricityModels -> {
            if(adapter != null){
                adapter.notifyDataSetChanged();
            }
        }));
        adapter = new ElectricityRecyclerAdapter(viewModel.getRecyclerData().getValue(),this);
        recyclerView.setAdapter(adapter);
        getRecyclerData();
    }


    ElectricityModel model;
    private void getRecyclerData(){
        model = new ElectricityModel(this,(response)->{
            recyclerData.clear();
            List<String> category = model.getCategories();
            for(String cats : category ){
                String cat = cats.split("\\|")[0];
                String url = cats.split("\\|")[1];
                recyclerData.add(new ElectricityModel(
                    cat,
                    url
                ));
            }
            viewModel.setRecyclerData(recyclerData);
        });
    }
}