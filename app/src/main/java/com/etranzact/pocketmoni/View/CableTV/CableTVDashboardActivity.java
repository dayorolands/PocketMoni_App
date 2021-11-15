package com.etranzact.pocketmoni.View.CableTV;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.etranzact.pocketmoni.Model.CableTVModel;
import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.ViewAdapter.CableTVRecyclerAdapter;
import com.etranzact.pocketmoni.ViewModel.CableTVViewModel;
import java.util.ArrayList;
import java.util.List;
import Utils.PosHandler;

public class CableTVDashboardActivity extends AppCompatActivity implements PosHandler {

    RecyclerView recyclerView;
    CableTVViewModel viewModel;
    CableTVRecyclerAdapter adapter;
    List<CableTVModel> recyclerData;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cable_tv_dashboard_activity);
        recyclerView = findViewById(R.id.select_discos_recycler_view);
        recyclerData = new ArrayList<>();
        recyclerData.add(new CableTVModel("Billers..",""));
        viewModel = new ViewModelProvider(this).get(CableTVViewModel.class);
        viewModel.setRecyclerData(recyclerData);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        viewModel.getRecyclerData().observe(this, (cableTvModels -> {
            if(adapter != null){
                adapter.notifyDataSetChanged();
            }
        }));
        adapter = new CableTVRecyclerAdapter(viewModel.getRecyclerData().getValue(),this);
        recyclerView.setAdapter(adapter);
        getRecyclerData();
    }


    CableTVModel model;
    private void getRecyclerData(){
        model = new CableTVModel(this,(response)->{
            recyclerData.clear();
            List<CableTVModel.Plan> category = model.getCategories();
            for(CableTVModel.Plan plan : category){
                recyclerData.add(new CableTVModel(
                    plan.getBillsName(),
                    plan.getImage()
                ));
            }
            viewModel.setRecyclerData(recyclerData);
        });
    }
}