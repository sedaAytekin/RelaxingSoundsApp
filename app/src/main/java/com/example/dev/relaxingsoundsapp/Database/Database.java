package com.example.dev.relaxingsoundsapp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class Database extends SQLiteOpenHelper {

	// Database Versiyonu Güncellediğimizde bu versiyon artacak
	private static final int DATABASE_VERSION = 1;
	// Database ADI
	private static String DatabaseName = "relaxingSound";//database adi
	private final String tableName = "favorites";
	private String favoriteId = "id";
	private String favoriteMusicId = "musicId";
	private String favoriteName = "title";
	private String favoriteUrl = "url";
	private String favoriteImage = "image";


	public Database(Context context) {
		super(context, DatabaseName, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {  // Databesi olusturuyoruz.

		String CREATE_TABLO = "CREATE TABLE " + tableName + "("
				+ favoriteId + " INTEGER PRIMARY KEY,"
				+ favoriteName + " TEXT,"
				+ favoriteUrl + " TEXT,"
				+ favoriteMusicId + " TEXT,"
				+ favoriteImage + " TEXT" + ")";
		db.execSQL(CREATE_TABLO);

	}

	public void addFavorite(String adi, String url, String muzik_id, String resim) {
		//addFavorite methodu ise adi ustunde Databese veri eklemek icin
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(favoriteName, adi);
		values.put(favoriteUrl, url);
		values.put(favoriteMusicId, muzik_id);
		values.put(favoriteImage, resim);

		db.insert(tableName, null, values);
		db.close(); //Database Baglantisini kapattik*/
	}


	public ArrayList<HashMap<String, String>> detailFavorite(){
		//Bu methodda favorileri çekiyoruz

		//HashMap bir iki boyutlu arraydir diyebiliriz Aslinda bir List interfacedir fakat anlamaniz icin cift boyutlu array olarak dusunebiliriz.anahtar-deger ikililerini bir arada tutmak icin tasarlanmistr.
		ArrayList<HashMap<String, String>> Muzikler = new ArrayList<>();
		HashMap<String,String> muzik;
		String selectQuery = "SELECT * FROM " + tableName;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		while(cursor.moveToNext()){
            muzik = new HashMap<String,String>();
            muzik.put(favoriteName, cursor.getString(1));
			muzik.put(favoriteUrl, cursor.getString(2));
			muzik.put(favoriteMusicId, cursor.getString(3));
			muzik.put(favoriteImage, cursor.getString(4));
			Muzikler.add(muzik);
		}
		cursor.close();
		db.close();
		return Muzikler;
	}


	public int getRowCount() { //tabloda kac satir kayitli oldugunu geri doner

		String countQuery = "SELECT  * FROM " + tableName;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int rowCount = cursor.getCount();
		db.close();
		cursor.close();
		return rowCount;
	}
	public int getFavorite(String muzik_id) { //tabloda kac satir kayitli oldugunu geri doner

		String countQuery = "SELECT  * FROM " + tableName +" where "+favoriteMusicId +"= "+muzik_id;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int rowCount = cursor.getCount();
		db.close();
		cursor.close();
 		return rowCount;
	}

	public void deleteFavorite(String muzikid){ //id si belli olan row u silmek için

		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(tableName, favoriteMusicId + " = "+muzikid,
				null);
		db.close();
	}
	public void resetTable(){
		// Tum verileri siler. tabloyu resetler.
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(tableName, null, null);
		db.close();
	}

	//Database güncellendiğinde bu method çalışır
	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
