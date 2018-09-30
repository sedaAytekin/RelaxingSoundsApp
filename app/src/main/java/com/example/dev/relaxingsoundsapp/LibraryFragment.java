package com.example.dev.relaxingsoundsapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.example.dev.relaxingsoundsapp.Library.LibraryAdapter;

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


public class LibraryFragment extends Fragment {
    View view;
    LibraryAdapter libraryAdapter;
    ArrayList<HashMap<String, String>> ListOfSound = new ArrayList<>();
    @BindView(R.id.libraryRV)
    RecyclerView libraryRV;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.library_fragment, container, false);
        ButterKnife.bind(this, view);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        libraryRV.setLayoutManager(layoutManager);

        libraryAdapter = new LibraryAdapter(getActivity(), ListOfSound);
        libraryRV.setAdapter(libraryAdapter);
        libraryRV.setItemAnimator(new DefaultItemAnimator());

        new getCategory().execute();

        return view;
    }

    ProgressDialog pDialog;
    HttpClass post = new HttpClass();  // Post ve reguest metodlarını içeren klasımız

    class getCategory extends AsyncTask<Void, Void, Void> {


        protected void onPreExecute() {
            //kullanıcıya beklemesi için süre veriyoruz
            //  progress dialog
            pDialog = new ProgressDialog(getContext());
            pDialog.setMessage("Kitaplik Dosyaları Getiriliyor...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        Integer resultCode = 0;

        protected Void doInBackground(Void... unused) {

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("param", "category"));// Jsonda hangi sistemi çekeceğimi yazıyorum. Burda category kitaplık sayfasını geitiriyor. favorite Favorileri getircek.

            String json = post.httpPost(getString(R.string.favori_url), "POST", params, 20000);

            Log.d("Gelen Json", "" + json);//Gelen veriyi logluyoruz.Log Catten kontrol edebiliriz
            try {

                ListOfSound.clear();
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
                        ListOfSound.add(responseHM);

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
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setTitle(" Sunucudua bir hata oluştu.");
                        alertDialog.setMessage("Tekrar Deneyiniz.");
                        alertDialog.setCancelable(false);
                        alertDialog.setButton(RESULT_OK, "Tamam", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        });
                        alertDialog.show();
                    } else {
                        libraryAdapter.notifyDataSetChanged();
                    }
                }

            });
        }
    }

}
