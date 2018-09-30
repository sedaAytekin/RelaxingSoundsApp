package com.example.dev.relaxingsoundsapp.Favorites;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.dev.relaxingsoundsapp.Database.Database;
import com.example.dev.relaxingsoundsapp.HttpClass;
import com.example.dev.relaxingsoundsapp.LibraryDetailActivity;
import com.example.dev.relaxingsoundsapp.MyApp;
import com.example.dev.relaxingsoundsapp.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

public class FavoritesFragment extends Fragment {

    ArrayList<HashMap<String, String>> favoriteList = new ArrayList<>();
    Database database;
    FavoriteAdapter favoriteAdapter;
    @BindView(R.id.favoritesRV)
    RecyclerView favoritesRV;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.favorites_fragment, container, false);
        ButterKnife.bind(this, view);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        database = new Database(getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        favoritesRV.setLayoutManager(layoutManager);
        favoriteControl();


        getFavoriteList();

        return view;
    }

    private void getFavoriteList() {


        favoriteAdapter = new FavoriteAdapter(getActivity(), favoriteList);
        favoritesRV.setAdapter(favoriteAdapter);
        favoritesRV.setItemAnimator(new DefaultItemAnimator());

    }

    ProgressDialog pDialog;
    HttpClass post = new HttpClass();

    class getFavorites extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
            //  progress dialog
            pDialog = new ProgressDialog(getContext());
            pDialog.setMessage("Favoriler Getiriliyor...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        Integer resultCode = 0;

        protected Void doInBackground(Void... unused) {

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("param", "favorite"));

            String json = post.httpPost(getString(R.string.favori_url), "POST", params, 20000);

            Log.d("Gelen Json", "" + json);//Gelen veriyi logluyoruz.Log Catten kontrol edebiliriz
            try {

                favoriteList.clear();
                if (!json.equals("")) {
                    JSONObject cevap = new JSONObject(json);
                    HashMap muzikmap;
                    // Phone number is agin JSON Object
                    JSONArray cast = cevap.getJSONArray("success");
                    for (int i = 0; i < cast.length(); i++) {
                        muzikmap = new HashMap<>();
                        JSONObject actor = cast.getJSONObject(i);
                        resultCode = 1;

                        muzikmap.put("id", actor.getString("id"));
                        muzikmap.put("title", actor.getString("title"));
                        muzikmap.put("url", actor.getString("url"));
                        muzikmap.put("image", actor.getString("image"));
                        muzikmap.put("catid", actor.getString("catid"));
                        favoriteList.add(muzikmap);
                        database.addFavorite(muzikmap.get("title").toString(), muzikmap.get("url").toString(), muzikmap.get("id").toString(), muzikmap.get("image").toString());

                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            pDialog.dismiss();
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (resultCode == 0) {// Sonuç başarılı değil ise
                        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                        alertDialog.setTitle("Favori müzikleriniz Çekilemedi.");
                        alertDialog.setMessage("Kitaplık Sayfasına Yönlendiriliceksiniz."); //Sonuc mesajıyla bilgilendiriyoruz.
                        alertDialog.setCancelable(false);
                        alertDialog.setButton(RESULT_OK, "Tamam", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alertDialog.show();
                    } else {
                        favoriteAdapter.notifyDataSetChanged();
                    }
                }

            });
        }
    }

    private void favoriteControl() {

        //iinternet kontrolü yapıyoruz.
        if (!MyApp.isNetworkAvailable()) {
            Toast.makeText(getContext(), "Lütfen internet bağlantınızı kontrol ediniz.", Toast.LENGTH_SHORT).show();
        } else {


            SharedPreferences mPreferences = getActivity().getSharedPreferences("isFirstFavorite", Context.MODE_PRIVATE);  // bu sayfa ilk çalıştırıldığında çalışacak olan kod. Ssitemden favori listesini bir kerelik çekecek.
            Boolean firstTime = mPreferences.getBoolean("isFirstFavorite", true);
            if (firstTime) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean("isFirstFavorite", false);
                editor.apply();
                new getFavorites().execute();
            } else {

                int count = database.getRowCount();// databasedeki favori row sayisi
                if (count == 0) {//0 dan fazla ise favori var demektir. o zaman favorilere yönlendiriyruz. Favoriler boş ise Kitaplığa yönlendiriyoruz.

                    Toast.makeText(getActivity(), "Favorileriniz Boş. Kitaplıktan Ekleyebilirisiniz.", Toast.LENGTH_SHORT).show();

                } else {

                    favoriteList.addAll(database.detailFavorite());
                }
            }

        }

    }

    @Override
    public void onPause() {
        for (int i = 0; i < FavoriteAdapter.mediaPlayerMap.size(); i++) { // Sayfa durdulduğunda açık olan mediaplayerlerı durduruyoruz.

            if (FavoriteAdapter.mediaPlayerMap != null) {
                FavoriteAdapter.ViewHolder.mediaPlayer.stop();
            }
        }

        super.onPause();
    }
}
