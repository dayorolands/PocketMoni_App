package com.sdk.pocketmonisdk.ViewAdapter;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.sdk.pocketmonisdk.R;

import java.util.List;

import Utils.AidClass;
import Utils.CardInfo;
import Utils.Emv;

public class AppSelectAdapter extends RecyclerView.Adapter<AppSelectAdapter.MyViewHolder> implements View.OnClickListener {
    List<AidClass> details;
    Activity activity;
    Dialog dialog;

    public AppSelectAdapter(List<AidClass> details, Activity activity, Dialog dialog) {
        this.details = details;
        this.activity = activity;
        this.dialog = dialog;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView appLabel;
        LinearLayout linerLayout;
        public MyViewHolder(View itemView) {
            super(itemView);
            appLabel = itemView.findViewById(R.id.application_label);
            linerLayout = itemView.findViewById(R.id.app_selection_layout);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.appselection_custom_list, null);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.appLabel.setText(details.get(position).Name);
        holder.linerLayout.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    @Override
    public void onClick(View v) {
        TextView tv = v.findViewById(R.id.application_label);
        for(AidClass aid : details){
            if(aid.Name.toUpperCase().equals(tv.getText().toString().toUpperCase())){
                if(aid.Adfname != null)
                {
                    Emv.setEmv("4F", aid.Aid);
                    boolean IsNormalSel = CardInfo.NormalSelection(activity.getApplicationContext(),aid.Adfname + aid.extdSelection);
                    if (!IsNormalSel) return;
                    break;
                }
                else
                {
                    Emv.setEmv("4F", aid.Aid);
                    boolean IsNormalSel = CardInfo.NormalSelection(activity.getApplicationContext(), aid.Aid);
                    if (!IsNormalSel) return;
                    break;
                }
            }
        }
        dialog.dismiss();
        Emv.startCVMProcessing(activity);
    }
}
