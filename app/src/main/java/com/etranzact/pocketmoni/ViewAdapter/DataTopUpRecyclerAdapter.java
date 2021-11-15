package com.etranzact.pocketmoni.ViewAdapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.etranzact.pocketmoni.Model.AirtimeModel;
import com.etranzact.pocketmoni.Model.DataTopUpModel;
import com.etranzact.pocketmoni.R;

import java.util.List;

public class DataTopUpRecyclerAdapter extends RecyclerView.Adapter<DataTopUpRecyclerAdapter.MyViewHolder> implements View.OnClickListener{
    List<DataTopUpModel> details;
    Activity activity;

    public DataTopUpRecyclerAdapter(List<DataTopUpModel> details, Activity activity) {
        this.details = details;
        this.activity = activity;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView discoIcon;
        TextView discoText;
        RelativeLayout relativeLayout;
        public MyViewHolder(View itemView) {
            super(itemView);
            discoIcon = itemView.findViewById(R.id.disco_icon_id);
            discoText = itemView.findViewById(R.id.disco_text_id);
            relativeLayout = itemView.findViewById(R.id.display_layout_id);
        }
    }

    @NonNull
    @Override
    public DataTopUpRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull DataTopUpRecyclerAdapter.MyViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        if(details != null){
            details.size();
        }
        return 0;
    }

    @Override
    public void onClick(View view) {

    }
}
