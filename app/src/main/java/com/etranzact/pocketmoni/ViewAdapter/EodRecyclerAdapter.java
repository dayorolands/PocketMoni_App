package com.etranzact.pocketmoni.ViewAdapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.View.EodActivity.ReprintActivity;
import com.sdk.pocketmonisdk.Model.RecyclerModel;

import java.util.List;

import Utils.Emv;
import Utils.TransDB;

public class EodRecyclerAdapter extends RecyclerView.Adapter<EodRecyclerAdapter.MyViewHolder> implements View.OnClickListener {
    List<RecyclerModel> details;
    Context c;
    FragmentActivity activity;

    public EodRecyclerAdapter(List<RecyclerModel> details, Context c, FragmentActivity activity) {
        this.details = details;
        this.c = c;
        this.activity = activity;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView passView;
        TextView cardNoView, amtView, dateTimeView, transType, transRrn;
        LinearLayout linerLayout;
        public MyViewHolder(View itemView) {
            super(itemView);
            passView = itemView.findViewById(R.id.eod_pass_image);
            cardNoView = itemView.findViewById(R.id.eod_card_no);
            amtView = itemView.findViewById(R.id.eod_amount);
            transType = itemView.findViewById(R.id.eod_trans_type);
            transRrn = itemView.findViewById(R.id.eod_rrn);
            dateTimeView = itemView.findViewById(R.id.eod_datete);
            linerLayout = itemView.findViewById(R.id.eod_layout);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.eod_custom_list, null);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String[] data = details.get(position).getData().split("\\|");
        //data[9] == STAN. See the Emv.getTransactionData function in the EMV.java file for the mapping.

        holder.passView.setImageResource(details.get(position).getPassImage());
        holder.cardNoView.setText(details.get(position).getCardNo());
        holder.amtView.setText("₦" + details.get(position).getTransAmt());
        holder.amtView.setTextColor(activity.getResources().getColor(details.get(position).getTextColor()));
        holder.transType.setText(details.get(position).getTransType());
        holder.transRrn.setText("RRN:" + data[9]);
        holder.dateTimeView.setText(details.get(position).getTransTime());
        holder.linerLayout.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    @Override
    public void onClick(View v) {
        TextView transTime = v.findViewById(R.id.eod_datete);
        TextView transType = v.findViewById(R.id.eod_trans_type);
        String eodData;
        TransDB db = new TransDB(c);
        db.open();
        eodData = db.getEODFullData(transType.getText().toString(), transTime.getText().toString());
        db.close();
        Intent intent = new Intent(c, ReprintActivity.class);
        intent.putExtra(Emv.REPRINTKEY, eodData);
        activity.startActivity(intent);
    }
}
