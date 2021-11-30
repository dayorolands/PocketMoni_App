package com.etranzact.pocketmoni.ViewAdapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.etranzact.pocketmoni.Dialogs.DiscosSelectionDialog;
import com.etranzact.pocketmoni.Model.ElectricityModel;
import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.View.Electricity.ElectricityPaymentMethodActivity;
import com.etranzact.pocketmoni.View.SettingsActivity.TransactionHistoryActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ElectricityRecyclerAdapter extends RecyclerView.Adapter<ElectricityRecyclerAdapter.MyViewHolder> implements View.OnClickListener {
    List<ElectricityModel> details;
    Activity activity;
    DiscosSelectionDialog dialog;

    public ElectricityRecyclerAdapter(List<ElectricityModel> details, Activity activity) {
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
        Glide.with(activity).load(details.get(position).getCategoryUrl()).into(holder.discoIcon);
        holder.discoText.setText(details.get(position).getCategory());
        holder.relativeLayout.setOnClickListener(ElectricityRecyclerAdapter.this);
        holder.relativeLayout.setTag(position);
    }

    @Override
    public int getItemCount() {
        return (details != null) ? details.size() : 0;
    }

    @Override
    public void onClick(View v) {
        int position = Integer.parseInt(v.getTag().toString());
        ElectricityModel model = new ElectricityModel();
        model.setSessionCategory(details.get(position).getCategory());
        dialog = new DiscosSelectionDialog(activity);
        dialog.show();
        LinearLayout makePaymentBtn = dialog.findViewById(R.id.make_payment_id);
        LinearLayout retrieveTokenBtn = dialog.findViewById(R.id.retrieve_token_id);
        ImageView closeBtn = dialog.findViewById(R.id.close_btn_id);
        makePaymentBtn.setOnClickListener(onDialogButtonClicked);
        retrieveTokenBtn.setOnClickListener(onDialogButtonClicked);
        closeBtn.setOnClickListener((view)-> dialog.dismiss());
    }

    View.OnClickListener onDialogButtonClicked = (view)->{
        if(view.getId() == R.id.retrieve_token_id){
            activity.startActivity(new Intent(activity, TransactionHistoryActivity.class));
            //Toast.makeText(activity, "", Toast.LENGTH_SHORT).show();
        }else if(view.getId() == R.id.make_payment_id){
            //activity.startActivity(new Intent(activity, ElectricityCashAmountActivity.class));
            activity.startActivity(new Intent(activity, ElectricityPaymentMethodActivity.class));
        }
        dialog.dismiss();
        activity.finish();
    };
}
