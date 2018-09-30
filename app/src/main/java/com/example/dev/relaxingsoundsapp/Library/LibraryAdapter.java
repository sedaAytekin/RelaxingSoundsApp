package com.example.dev.relaxingsoundsapp.Library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.dev.relaxingsoundsapp.LibraryDetailActivity;
import com.example.dev.relaxingsoundsapp.R;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {

    private Context mContext;
    ArrayList<HashMap<String, String>> list = new ArrayList<>();

    public LibraryAdapter(Context context, ArrayList<HashMap<String, String>> list) {
        this.list = list;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_library, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(v);


        return viewHolder;
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        viewHolder.categoryNameTV.setText(list.get(position).get("title"));
        RequestOptions options = new RequestOptions();
        options.centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(mContext)
                .load(list.get(position).get("url"))
                .apply(options)
                .into(viewHolder.categoryIV);

        viewHolder.categoryIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, LibraryDetailActivity.class);
                intent.putExtra("category", list.get(position));
                intent.putExtra("title", list.get(position).get("title"));
                intent.putExtra("url", list.get(position).get("url"));
                mContext.startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.categoryIV)
        ImageView categoryIV;
        @BindView(R.id.categoryNameTV)
        TextView categoryNameTV;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

        }

    }
}
