package com.example.dev.relaxingsoundsapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.dev.relaxingsoundsapp.Database.Database;
import com.example.dev.relaxingsoundsapp.Favorites.FavoritesFragment;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        Database database = new Database(this);

        int count = database.getRowCount();// databasedeki favori sayisi
        if(count > 0){//0 dan fazla ise favori var demektir. o zaman favorilere yönlendiriyoruz. Favoriler boş ise Kitaplığa yönlendiriyoruz.
            ///alt navigasyonda da seçili olan fragment id sin getiriyoruz.
            navigation.setSelectedItemId(R.id.navigation_favorites);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            FavoritesFragment favoritesFragment = new FavoritesFragment();
            ft.replace(R.id.content, favoritesFragment);
            ft.commit();

        }else{
            navigation.setSelectedItemId(R.id.navigation_library);
            FragmentManager fm1 = getSupportFragmentManager();
            FragmentTransaction ft1 = fm1.beginTransaction();
            LibraryFragment libraryFragment = new LibraryFragment();
            ft1.replace(R.id.content, libraryFragment);
            ft1.commit();

        }

        navigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.navigation_favorites:
                                FragmentManager fm = getSupportFragmentManager();
                                FragmentTransaction ft = fm.beginTransaction();
                                FavoritesFragment favoritesFragment = new FavoritesFragment();
                                ft.replace(R.id.content, favoritesFragment);
                                ft.commit();

                                break;
                            case R.id.navigation_library:
                                FragmentManager fm1 = getSupportFragmentManager();
                                FragmentTransaction ft1 = fm1.beginTransaction();
                                LibraryFragment libraryFragment = new LibraryFragment();
                                ft1.replace(R.id.content, libraryFragment);
                                ft1.commit();
                                break;
                        }
                        return true;
                    }
                });


    }

}
