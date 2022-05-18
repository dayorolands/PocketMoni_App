package com.etranzact.pocketmoni.ViewAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ElectricityRecyclerAdapter extends RecyclerView.Adapter<ElectricityRecyclerAdapter.MyViewHolder> implements View.OnClickListener {
    List<ElectricityModel> details;
    Activity activity;
    DiscosSelectionDialog dialog;
    ElectricityModel electricityModel;

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
        String imageUrl = details.get(position).getCategoryUrl();
        new Thread(()->{
            Bitmap bitmap = null;
            try {
                bitmap = Glide.with(activity).asBitmap().load(imageUrl).into(200,200).get();
                saveBitMap(activity, bitmap);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
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
        }else if(view.getId() == R.id.make_payment_id){
            activity.startActivity(new Intent(activity, ElectricityPaymentMethodActivity.class));
        }
        dialog.dismiss();
        activity.finish();
    };

    private File saveBitMap(Context context, Bitmap finalBitmap) {
        electricityModel = new ElectricityModel();
        File pictureFileDir = new File(activity.getExternalFilesDir(null).getAbsolutePath() + "/Android/data" + context.getPackageName() + "/ElectricityLogos");
        if (!pictureFileDir.exists()) {
            boolean isDirectoryCreated = pictureFileDir.mkdirs();
            if (!isDirectoryCreated)
                Log.d("Result", "Can't create directory to save the image");
            return null;
        }
        String filename = electricityModel.getSessionCategory() + ".jpg";
        File pictureFile = new File(pictureFileDir.getPath() + File.separator + filename);
        try {
            FileOutputStream oStream = new FileOutputStream(pictureFile);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, oStream);
            oStream.flush();
            oStream.close();
//            activity.runOnUiThread(()->{
//                Toast.makeText(context, "Image saved Successfully..", Toast.LENGTH_SHORT).show();
//            });

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Result", "There was an issue saving the image.");
        }
        return pictureFile;
    }

}
