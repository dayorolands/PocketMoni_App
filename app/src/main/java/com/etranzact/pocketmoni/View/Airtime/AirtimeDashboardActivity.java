package com.etranzact.pocketmoni.View.Airtime;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.etranzact.pocketmoni.Model.AirtimeModel;
import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.ViewAdapter.AirtimeRecyclerAdapter;
import com.etranzact.pocketmoni.ViewModel.AirtimeViewModel;
import java.util.ArrayList;
import java.util.List;
import Utils.PosHandler;

public class AirtimeDashboardActivity extends AppCompatActivity{

    RecyclerView recyclerView;
    AirtimeViewModel viewModel;
    AirtimeRecyclerAdapter adapter;
    List<AirtimeModel> recyclerData;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airtime_dashboard_activity);
        recyclerView = findViewById(R.id.select_discos_recycler_view);
        recyclerData = new ArrayList<>();
        recyclerData.add(new AirtimeModel("Network..",""));
        viewModel = new ViewModelProvider(this).get(AirtimeViewModel.class);
        viewModel.setRecyclerData(recyclerData);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        viewModel.getRecyclerData().observe(this, (cableTvModels -> {
            if(adapter != null){
                adapter.notifyDataSetChanged();
            }
        }));
        adapter = new AirtimeRecyclerAdapter(viewModel.getRecyclerData().getValue(),this);
        recyclerView.setAdapter(adapter);
        getRecyclerData();
    }


    AirtimeModel model;
    private void getRecyclerData(){
        model = new AirtimeModel(this,(response)->{
            recyclerData.clear();
            List<AirtimeModel.Plan> category = model.getCategories();
            for(AirtimeModel.Plan plan : category){
                recyclerData.add(new AirtimeModel(
                        plan.getBillsName(),
                        plan.getImage()
                ));
            }
            viewModel.setRecyclerData(recyclerData);
        });
    }
}