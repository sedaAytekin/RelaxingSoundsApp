package com.example.dev.relaxingsoundsapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
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
import com.example.dev.relaxingsoundsapp.Database.Database;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LibraryDetailAdapter extends RecyclerView.Adapter<LibraryDetailAdapter.ViewHolder> {
    Database database;
    private Context mContext;
    ArrayList<HashMap<String, String>> list = new ArrayList<>();
    public static Map<Integer, MediaPlayer> mediaPlayerMap = new HashMap<>();

    public LibraryDetailAdapter(Context context, ArrayList<HashMap<String, String>> list) {
        this.list = list;
        this.mContext = context;
    }

    @NonNull
    @Override
    public LibraryDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_library_detail, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(v);
        database = new Database(mContext);


        return viewHolder;
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        viewHolder.name.setText(list.get(position).get("title"));

        RequestOptions options = new RequestOptions();
        options.centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(mContext).load(list.get(position).get("image")).apply(options).into(viewHolder.background);

        int count = database.getFavorite(list.get(position).get("id"));// databasedeki favoride kayıtlımı
        if (count > 0) {//0 dan fazla ise favori var demektir.
            viewHolder.favoriteIV.setImageResource(R.drawable.ic_favorite);

        } else {
            viewHolder.favoriteIV.setImageResource(R.drawable.ic_not_favorite);
        }
        viewHolder.favoriteIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int count = database.getFavorite(list.get(position).get("id"));// databasedeki favoride kayıtlımı
                if (count > 0) {//0 dan fazla ise favori var demektir.
                    viewHolder.favoriteIV.setImageResource(R.drawable.ic_not_favorite);
                    database.deleteFavorite(list.get(position).get("id"));
                } else {
                    viewHolder.favoriteIV.setImageResource(R.drawable.ic_favorite);
                    database.addFavorite(list.get(position).get("title"), list.get(position).get("url"), list.get(position).get("id"), list.get(position).get("image"));

                }

            }
        });

        viewHolder.playBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    final ProgressDialog pDialog = new ProgressDialog(mContext);
                    pDialog.setTitle("Lütfen Bekleyin...");
                    pDialog.setMessage("Ses dosyası getiriliyor..");
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(true);
                    pDialog.show();


                    if (!viewHolder.mediaPlayer.isPlaying()) {
                        pDialog.dismiss();
                        viewHolder.playBT.setImageResource(R.drawable.ic_pause_button);


                        viewHolder.mediaPlayer = new MediaPlayer();

                        viewHolder.mediaPlayer.setDataSource(list.get(position).get("url").toString());

                        float volume = (float) (1 - (Math.log(100 - viewHolder.seekBar.getProgress()) / Math.log(100)));
                        viewHolder.mediaPlayer.setVolume(volume, volume);
                        viewHolder.mediaPlayer.setLooping(true);
                        mediaPlayerMap.put(position, viewHolder.mediaPlayer);

                        viewHolder.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {

                                mp.start();
                                Thread timerThread = new Thread() {
                                    public void run() {

                                        int currentPosition = viewHolder.mediaPlayer.getCurrentPosition();

                                        if (currentPosition > 0)
                                            pDialog.dismiss();

                                    }
                                };
                                timerThread.start();
                            }
                        });
                        viewHolder.mediaPlayer.prepareAsync();
                    } else {
                        pDialog.dismiss();
                        viewHolder.playBT.setImageResource(R.drawable.ic_play_button);
                        viewHolder.mediaPlayer.pause();
                        Thread timerThread = new Thread() {
                            public void run() {

                                int currentPosition = viewHolder.mediaPlayer.getCurrentPosition();

                                if (currentPosition > 0)
                                    pDialog.dismiss();

                            }
                        };
                        timerThread.start();

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        viewHolder.seekBar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                float volume = (float) (1 - (Math.log(100 - value) / Math.log(100)));

                viewHolder.mediaPlayer.setVolume(
                        volume, volume);
                return value * 100;
            }
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.background)
        ImageView background;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.seekBar)
        DiscreteSeekBar seekBar;
        @BindView(R.id.playBT)
        ImageView playBT;
        @BindView(R.id.favoriteIV)
        ImageView favoriteIV;
        public static MediaPlayer mediaPlayer;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            mediaPlayer = new MediaPlayer();

        }

    }
}
