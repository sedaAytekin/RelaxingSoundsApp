package com.example.dev.relaxingsoundsapp.Favorites;

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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.dev.relaxingsoundsapp.Database.Database;
import com.example.dev.relaxingsoundsapp.R;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {


    private Context mContext;
    Database database;
    ArrayList<HashMap<String, String>> favoriteList = new ArrayList<HashMap<String, String>>();

    public static HashMap<Integer, MediaPlayer> mediaPlayerMap = new HashMap<Integer, MediaPlayer>(); // Map dizini oluşturuyorum. sayfa kapandığında sesler çalmaya devam etmemesi için burdan kontrol edeceğim.

    public FavoriteAdapter(Context context, ArrayList<HashMap<String, String>> favoriteList) {
        this.favoriteList = favoriteList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_library_detail, parent, false);

        ViewHolder view_holder = new ViewHolder(v);
        database = new Database(mContext);


        return view_holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.name.setText(favoriteList.get(position).get("title"));

        RequestOptions options = new RequestOptions();
        options.centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(mContext).load(favoriteList.get(position).get("image"))
                .apply(options).into(holder.background);
        int count = database.getFavorite(favoriteList.get(position).get("id"));// databasedeki favoride kayıtlımı
        if (count > 0) {//0 dan fazla ise favori var demektir.
            holder.favoriteIV.setImageResource(R.drawable.ic_favorite);

        } else {
            holder.favoriteIV.setImageResource(R.drawable.ic_not_favorite);
        }
        holder.favoriteIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = database.getFavorite(favoriteList.get(position).get("id"));// databasedeki favoride kayıtlımı
                if (count > 0) {//0 dan fazla ise favori var demektir.
                    holder.favoriteIV.setImageResource(R.drawable.ic_not_favorite);
                    database.deleteFavorite(favoriteList.get(position).get("id"));
                    favoriteList.remove(position);
                    notifyDataSetChanged();
                    if (favoriteList.size() == 0) {
                        Toast.makeText(mContext, "Favori listeniz boş", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    holder.favoriteIV.setImageResource(R.drawable.ic_favorite);
                    database.addFavorite(favoriteList.get(position).get("title"), favoriteList.get(position).get("url"), favoriteList.get(position).get("id"), favoriteList.get(position).get("image"));

                }

            }
        });

        holder.playBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    // kullanıcıyı bilgilendiriyorum. internetten indirmeden müziği dinlettiğim içiin ara belleğe alırken 2-3 saniylik müziğin uzunluğuna göre bekleme süresi var. bunu kullanıcya progresle bildiriyorum.
                    final ProgressDialog pDialog = new ProgressDialog(mContext);
                    pDialog.setTitle("Lütfen Bekleyin...");
                    pDialog.setMessage("Ses Dosyası Çekiliyor");
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(true);
                    pDialog.show();


                    if (!holder.mediaPlayer.isPlaying()) {
                        holder.playBT.setImageResource(R.drawable.ic_pause_button);
                        pDialog.dismiss();

                        holder.mediaPlayer = new MediaPlayer();

                        holder.mediaPlayer.setDataSource(favoriteList.get(position).get("url").toString());

                        float volume = (float) (1 - (Math.log(100 - holder.seekBar.getProgress()) / Math.log(100)));  // Seekbar değeri kadar müziğin sesini açıoyu
                        holder.mediaPlayer.setVolume(volume, volume);
                        holder.mediaPlayer.setLooping(true);
                        mediaPlayerMap.put(position, holder.mediaPlayer);// listedeki mediplayerlı  map dizinine  atıyorum. böylelikle active içinden kontrol edebiliyorum.
                        holder.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {

                                mp.start();

                                Thread timerThread = new Thread() {
                                    public void run() {

                                        int currentPosition = holder.mediaPlayer.getCurrentPosition();

                                        if (currentPosition > 0) // Müzik başlamış ise progresi sonlandırıyorum.
                                            pDialog.dismiss();

                                    }
                                };
                                timerThread.start();
                            }
                        });
                        holder.mediaPlayer.prepareAsync();
                    } else {
                        pDialog.dismiss();
                        holder.playBT.setImageResource(R.drawable.ic_play_button);
                        holder.mediaPlayer.pause();
                        Thread timerThread = new Thread() {
                            public void run() {

                                int currentPosition = holder.mediaPlayer.getCurrentPosition();

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
        // Seekbar ile her müziği kendi ses yüksekliğini ayarlayaibliyoruz.
        holder.seekBar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                float volume = (float) (1 - (Math.log(100 - value) / Math.log(100)));

                holder.mediaPlayer.setVolume(
                        volume, volume);
                return value * 100;
            }
        });

    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
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
