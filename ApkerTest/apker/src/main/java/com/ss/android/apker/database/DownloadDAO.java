package com.ss.android.apker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ss.android.apker.entity.ApkEntity;

import java.util.ArrayList;

public class DownloadDAO extends BaseDAO {

    public static final String TABLE_NAME = "download_info";
    public static final String KEY_TASKID = "taskID";
    public static final String KEY_DOWNLOAD_SIZE = "downLoadSize";
    public static final String KEY_FILE_NAME = "fileName";
    public static final String KEY_FILE_PATH = "filePath";
    public static final String KEY_FILE_SIZE = "fileSize";
    public static final String KEY_URL = "url";
    private SQLiteHelper mSQLiteHelper;

    public DownloadDAO(Context context) {
        this.mSQLiteHelper = new SQLiteHelper(context);
    }

    @Override
    public String buildCreateTableSql() {
        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (");
        builder.append("id").append(" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL, ");
        builder.append(KEY_TASKID).append(" VARCHAR, ");
        builder.append(KEY_URL).append(" VARCHAR, ");
        builder.append(KEY_FILE_PATH).append(" VARCHAR, ");
        builder.append(KEY_FILE_NAME).append(" VARCHAR, ");
        builder.append(KEY_FILE_SIZE).append(" VARCHAR, ");
        builder.append(KEY_DOWNLOAD_SIZE).append(" VARCHAR ");
        builder.append(")");
        return builder.toString();
    }

    public void saveDownLoadInfo(ApkEntity downloadInfo) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_TASKID, downloadInfo.taskID);
        cv.put(KEY_DOWNLOAD_SIZE, downloadInfo.downloadSize);
        cv.put(KEY_FILE_NAME, downloadInfo.name);
        cv.put(KEY_FILE_PATH, downloadInfo.filePath);
        cv.put(KEY_FILE_SIZE, downloadInfo.fileSize);
        cv.put(KEY_URL, downloadInfo.url);
        Cursor cursor = null;
        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();
        try {
            cursor = db.rawQuery("SELECT * from " + TABLE_NAME + " WHERE " + KEY_TASKID + " = ? ",
                    new String[] {downloadInfo.taskID});
            if (cursor.getCount() > 0) {
                db.update(TABLE_NAME, cv, KEY_TASKID + " = ? ", new String[] {downloadInfo.taskID});
            } else {
                db.insert(TABLE_NAME, null, cv);
            }
        } catch (Exception e) {
            // NOOP
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    public ApkEntity getDownLoadInfo(String taskID) {
        ApkEntity downloadinfo = null;
        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * from " + TABLE_NAME + "WHERE " + KEY_TASKID + " = ? ",
                new String[] {taskID});
        if (cursor.moveToNext()) {
            downloadinfo = new ApkEntity();
            downloadinfo.downloadSize = cursor.getLong(cursor.getColumnIndex(KEY_DOWNLOAD_SIZE));
            downloadinfo.name = cursor.getString(cursor.getColumnIndex(KEY_FILE_NAME));
            downloadinfo.filePath = cursor.getString(cursor.getColumnIndex(KEY_FILE_PATH));
            downloadinfo.fileSize = cursor.getLong(cursor.getColumnIndex(KEY_FILE_SIZE));
            downloadinfo.url = cursor.getString(cursor.getColumnIndex(KEY_URL));
            downloadinfo.taskID = cursor.getString(cursor.getColumnIndex(KEY_TASKID));
        }
        cursor.close();
        db.close();
        return downloadinfo;
    }

    public ArrayList<ApkEntity> getAllDownLoadInfo() {
        ArrayList<ApkEntity> downloadinfoList = new ArrayList<>();
        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * from " + TABLE_NAME, null);
        while (cursor.moveToNext()) {
            ApkEntity downloadinfo = new ApkEntity();
            downloadinfo.downloadSize = cursor.getLong(cursor.getColumnIndex(KEY_DOWNLOAD_SIZE));
            downloadinfo.name = cursor.getString(cursor.getColumnIndex(KEY_FILE_NAME));
            downloadinfo.filePath = cursor.getString(cursor.getColumnIndex(KEY_FILE_PATH));
            downloadinfo.fileSize = cursor.getLong(cursor.getColumnIndex(KEY_FILE_SIZE));
            downloadinfo.url = cursor.getString(cursor.getColumnIndex(KEY_URL));
            downloadinfo.taskID = cursor.getString(cursor.getColumnIndex(KEY_TASKID));
            downloadinfoList.add(downloadinfo);
        }
        cursor.close();
        db.close();
        return downloadinfoList;

    }

    public void deleteDownLoadInfo(String taskID) {
        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_TASKID + " = ? ", new String[] {taskID});
        db.close();
    }

    public void deleteAllDownLoadInfo() {
        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }
}
