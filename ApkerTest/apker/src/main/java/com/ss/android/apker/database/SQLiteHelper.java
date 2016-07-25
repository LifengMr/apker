package com.ss.android.apker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

	private static final String DATA_NAME = "apker";
	private static final int VERSION = 1;
	private Context mContext;

	public SQLiteHelper(Context context) {
		super(context, DATA_NAME, null, VERSION);
		mContext = context;
	}

	public SQLiteHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
        db.execSQL(new DownloadDAO(mContext).buildCreateTableSql());
		db.execSQL(new PluginDAO(mContext).buildCreateTableSql());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}
