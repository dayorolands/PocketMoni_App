package com.etranzact.pocketmoni.ViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.etranzact.pocketmoni.R;
import com.etranzact.pocketmoni.ViewModel.WelcomeViewHolderModel;

import java.util.List;

public class WelcomeViewHolderAdapter extends PagerAdapter {
    List<WelcomeViewHolderModel> pages;
    Context c;
    public WelcomeViewHolderAdapter(List<WelcomeViewHolderModel> pages, Context c) {
        this.pages = pages;
        this.c = c;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View view = inflater.inflate(R.layout.welcome_custom_page, null);
        TextView bigText = view.findViewById(R.id.big_text);
        TextView smallText = view.findViewById(R.id.small_text);
        ImageView welcomeImage = view.findViewById(R.id.welcome_image);
        bigText.setText(pages.get(position).getBigText());
        smallText.setText(pages.get(position).getSmallText());
        welcomeImage.setImageResource(pages.get(position).getImage());
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
