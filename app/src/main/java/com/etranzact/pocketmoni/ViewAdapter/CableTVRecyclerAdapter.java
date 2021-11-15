package com.etranzact.pocketmoni.ViewAdapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.etranzact.pocketmoni.Model.CableTVModel;
import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.View.CableTV.CableTVAmountActivity;
import com.etranzact.pocketmoni.View.CableTV.CableTVStartimesAmountActivity;

import org.jetbrains.annotations.NotNull;
import java.util.List;

public class CableTVRecyclerAdapter extends RecyclerView.Adapter<CableTVRecyclerAdapter.MyViewHolder> implements View.OnClickListener {
    List<CableTVModel> details;
    Activity activity;

    public CableTVRecyclerAdapter(List<CableTVModel> details, Activity activity) {
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

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.discos_custom_list, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull MyViewHolder holder, int position) {
        Glide.with(activity).load(details.get(position).getCategoryImage()).into(holder.discoIcon);
        holder.discoText.setText(details.get(position).getCategory());
        holder.relativeLayout.setOnClickListener(CableTVRecyclerAdapter.this);
        holder.relativeLayout.setTag(position);
    }

    @Override
    public int getItemCount() {
        return (details != null) ? details.size() : 0;
    }

    @Override
    public void onClick(View v) {
        int position = Integer.parseInt(v.getTag().toString());
        CableTVModel model = new CableTVModel();
        model.setBillsName(details.get(position).getCategory());
        if(model.getBillsName().toLowerCase().contains("startimes")){
            activity.startActivity(new Intent(activity, CableTVStartimesAmountActivity.class));
            activity.finish();
        }else{
            activity.startActivity(new Intent(activity, CableTVAmountActivity.class));
            activity.finish();
        }
    }
}
