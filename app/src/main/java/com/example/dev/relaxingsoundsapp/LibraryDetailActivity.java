package com.example.dev.relaxingsoundsapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

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

public class LibraryDetailActivity extends AppCompatActivity {
    HashMap<String, String> libraryDetailList;
    LibraryDetailAdapter adapterItems;
    ArrayList<HashMap<String, String>> soundsList = new ArrayList<>();
    String categoryId;
    @BindView(R.id.detailRV)
    RecyclerView detailRV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.library_detail);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getSelectedCategory();// Hangi kategori seçilmişse id sini aldık ve id ye göre içerik getirceğiz

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        detailRV.setLayoutManager(layoutManager);
        adapterItems = new LibraryDetailAdapter(this, soundsList);
        detailRV.setAdapter(adapterItems);
        detailRV.setItemAnimator(new DefaultItemAnimator());

    }

    private void getSelectedCategory() {

        if (getIntent().hasExtra("category")) {
            categoryId = getIntent().getStringExtra("category");

            libraryDetailList = (HashMap<String, String>) getIntent().getExtras().getSerializable("category");

        } else {
            Toast.makeText(this, getString(R.string.no_category), Toast.LENGTH_SHORT).show();
            finish();
        }
        //internet kontrolü yapıyoruz.
        if (!MyApp.isNetworkAvailable()) {
            Toast.makeText(this, "Lütfen internet bağlantınızı kontrol ediniz.", Toast.LENGTH_SHORT).show();
        } else {
            new getDetailCategory().execute();
        }
    }

    ProgressDialog pDialog;
    HttpClass post = new HttpClass();

    class getDetailCategory extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
            //  progress dialog
            pDialog = new ProgressDialog(LibraryDetailActivity.this);
            pDialog.setMessage("Kitaplik Dosyaları Getiriliyor...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        Integer resultCode = 0;

        protected Void doInBackground(Void... unused) {

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("param", "categoryItems"));
            params.add(new BasicNameValuePair("catid", libraryDetailList.get("id")));  // Katagori idsini categoryItems e göndererek katagoriye ait müzikleri getiriyoruz.

            String json = post.httpPost(getString(R.string.favori_url), "POST", params, 20000);

            Log.d("Gelen Json", "" + json);//Gelen veriyi logluyoruz.Log Catten kontrol edebiliriz
            try {


                libraryDetailList.clear();
                if (!json.equals("")) {
                    JSONObject cevap = new JSONObject(json);
                    HashMap responseHM;
                    JSONArray cast = cevap.getJSONArray("success");
                    for (int i = 0; i < cast.length(); i++) {
                        responseHM = new HashMap<>();
                        JSONObject actor = cast.getJSONObject(i);
                        resultCode = 1;

                        responseHM.put("id", actor.getString("id"));
                        responseHM.put("title", actor.getString("title"));
                        responseHM.put("url", actor.getString("url"));
                        responseHM.put("image", actor.getString("image"));
                        responseHM.put("catid", actor.getString("catid"));
                        soundsList.add(responseHM);

                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    if (resultCode == 0) {// Sonuç başarılı değil ise
                        AlertDialog alertDialog = new AlertDialog.Builder(LibraryDetailActivity.this).create();
                        alertDialog.setTitle("Kitaplık müzikleri çekilemedi.");
                        alertDialog.setMessage("Üzgünüm."); //Sonuc mesajıyla bilgilendiriyoruz.
                        alertDialog.setCancelable(false);
                        alertDialog.setButton(RESULT_OK, "Tamam", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        alertDialog.show();
                    } else {
                        adapterItems.notifyDataSetChanged();
                    }
                }

            });
        }
    }

    @Override
    public void onPause() {
        for (int i = 0; i < LibraryDetailAdapter.mediaPlayerMap.size(); i++) { // Sayfa durdulduğunda açık olan mediaplayerlerı durduruyoruz.

            if (LibraryDetailAdapter.mediaPlayerMap != null) {
                LibraryDetailAdapter.ViewHolder.mediaPlayer.stop();
            }
        }

        super.onPause();
    }
}
