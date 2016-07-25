package com.ss.android.apker.jar.loader;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.view.ContextThemeWrapper;
import android.view.Display;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author chenlifeng1
 */
public class JarContext extends ContextThemeWrapper {
	private JarLayoutInflater mInflater;
	private Context mConctext;
	private ClassLoader mClassLoader;
	
	public JarContext(Context conctext, ClassLoader classLoader) {
		super(conctext,0);
		this.mConctext = conctext;
		this.mClassLoader = classLoader;
	}
	
	@Override
	public AssetManager getAssets() {
		return mConctext.getAssets();
	}

	@Override
	public Resources getResources() {
		return mConctext.getResources();
	}

	@Override
	public PackageManager getPackageManager() {
		return mConctext.getPackageManager();
	}

	@Override
	public ContentResolver getContentResolver() {
		return mConctext.getContentResolver();
	}

	@Override
	public Looper getMainLooper() {
		return mConctext.getMainLooper();
	}

	@Override
	public Context getApplicationContext() {
		return mConctext.getApplicationContext();
	}

	@Override
	public void setTheme(int resid) {
		mConctext.setTheme(resid);
	}

	@Override
	public Theme getTheme() {
		return mConctext.getTheme();
	}

	@Override
	public ClassLoader getClassLoader() {
		return mClassLoader;
	}

	@Override
	public String getPackageName() {
		return mConctext.getPackageName();
	}

	@Override
	public ApplicationInfo getApplicationInfo() {
		return mConctext.getApplicationInfo();
	}

	@Override
	public String getPackageResourcePath() {
		return mConctext.getPackageResourcePath();
	}

	@Override
	public String getPackageCodePath() {
		return mConctext.getPackageCodePath();
	}

	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		return mConctext.getSharedPreferences(name, mode);
	}

	@Override
	public FileInputStream openFileInput(String name)
			throws FileNotFoundException {
		return mConctext.openFileInput(name);
	}

	@Override
	public FileOutputStream openFileOutput(String name, int mode)
			throws FileNotFoundException {
		return mConctext.openFileOutput(name, mode);
	}

	@Override
	public boolean deleteFile(String name) {
		return mConctext.deleteFile(name);
	}

	@Override
	public File getFileStreamPath(String name) {
		return mConctext.getFileStreamPath(name);
	}

	@Override
	public File getFilesDir() {
		return mConctext.getFilesDir();
	}

	@Override
	public File getExternalFilesDir(String type) {
		return mConctext.getExternalFilesDir(type);
	}

	@Override
	public File getObbDir() {
		return mConctext.getObbDir();
	}

	@Override
	public File getCacheDir() {
		return mConctext.getCacheDir();
	}

	@Override
	public File getExternalCacheDir() {
		return mConctext.getExternalCacheDir();
	}

	@Override
	public String[] fileList() {
		return mConctext.fileList();
	}

	@Override
	public File getDir(String name, int mode) {
		return mConctext.getDir(name, mode);
	}

	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode,
			CursorFactory factory) {
		return mConctext.openOrCreateDatabase(name, mode, factory);
	}

	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode,
			CursorFactory factory, DatabaseErrorHandler errorHandler) {
		return mConctext.openOrCreateDatabase(name, mode, factory, errorHandler);
	}

	@Override
	public boolean deleteDatabase(String name) {
		return mConctext.deleteDatabase(name);
	}

	@Override
	public File getDatabasePath(String name) {
		return mConctext.getDatabasePath(name);
	}

	@Override
	public String[] databaseList() {
		return mConctext.databaseList();
	}

	@Override
	@Deprecated
	public Drawable getWallpaper() {
		return mConctext.getWallpaper();
	}

	@Override
	@Deprecated
	public Drawable peekWallpaper() {
		return mConctext.peekWallpaper();
	}

	@Override
	@Deprecated
	public int getWallpaperDesiredMinimumWidth() {
		return mConctext.getWallpaperDesiredMinimumWidth();
	}

	@Override
	@Deprecated
	public int getWallpaperDesiredMinimumHeight() {
		return mConctext.getWallpaperDesiredMinimumHeight();
	}

	@Override
	@Deprecated
	public void setWallpaper(Bitmap bitmap) throws IOException {
		mConctext.setWallpaper(bitmap);
	}

	@Override
	@Deprecated
	public void setWallpaper(InputStream data) throws IOException {
		mConctext.setWallpaper(data);
	}

	@Override
	@Deprecated
	public void clearWallpaper() throws IOException {
		mConctext.clearWallpaper();
	}

	@Override
	public void startActivity(Intent intent) {
		mConctext.startActivity(intent);
	}

	@Override
	public void startActivity(Intent intent, Bundle options) {
		mConctext.startActivity(intent, options);
	}

	@Override
	public void startActivities(Intent[] intents) {
		mConctext.startActivities(intents);
	}

	@Override
	public void startActivities(Intent[] intents, Bundle options) {
		mConctext.startActivities(intents, options);
	}

	@Override
	public void startIntentSender(IntentSender intent, Intent fillInIntent,
			int flagsMask, int flagsValues, int extraFlags)
			throws SendIntentException {
		mConctext.startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags);
	}

	@Override
	public void startIntentSender(IntentSender intent, Intent fillInIntent,
			int flagsMask, int flagsValues, int extraFlags, Bundle options)
			throws SendIntentException {
		mConctext.startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags, options);
	}

	@Override
	public void sendBroadcast(Intent intent) {
		mConctext.sendBroadcast(intent);
	}

	@Override
	public void sendBroadcast(Intent intent, String receiverPermission) {
		mConctext.sendBroadcast(intent, receiverPermission);
	}

	@Override
	public void sendOrderedBroadcast(Intent intent,
			String receiverPermission) {
		mConctext.sendOrderedBroadcast(intent, receiverPermission);
	}

	@Override
	public void sendOrderedBroadcast(Intent intent,
			String receiverPermission, BroadcastReceiver resultReceiver,
			Handler scheduler, int initialCode, String initialData,
			Bundle initialExtras) {
	}

	@Override
	public void sendBroadcastAsUser(Intent intent, UserHandle user) {
		mConctext.sendBroadcastAsUser(intent, user);
	}

	@Override
	public void sendBroadcastAsUser(Intent intent, UserHandle user,
			String receiverPermission) {
		mConctext.sendBroadcastAsUser(intent, user, receiverPermission);
	}

	@Override
	public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user,
			String receiverPermission, BroadcastReceiver resultReceiver,
			Handler scheduler, int initialCode, String initialData,
			Bundle initialExtras) {
		mConctext.sendOrderedBroadcastAsUser(intent, user, receiverPermission, resultReceiver, scheduler, initialCode, initialData, initialExtras);
	}

	@Override
	public void sendStickyBroadcast(Intent intent) {
		mConctext.sendStickyBroadcast(intent);
	}

	@Override
	public void sendStickyOrderedBroadcast(Intent intent,
			BroadcastReceiver resultReceiver, Handler scheduler,
			int initialCode, String initialData, Bundle initialExtras) {
		mConctext.sendStickyOrderedBroadcast(intent, resultReceiver, scheduler, initialCode, initialData, initialExtras);
	}

	@Override
	public void removeStickyBroadcast(Intent intent) {
		mConctext.removeStickyBroadcast(intent);
	}

	@Override
	public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {
		mConctext.sendStickyBroadcastAsUser(intent, user);
	}

	@Override
	public void sendStickyOrderedBroadcastAsUser(Intent intent,
			UserHandle user, BroadcastReceiver resultReceiver,
			Handler scheduler, int initialCode, String initialData,
			Bundle initialExtras) {
		mConctext.sendStickyOrderedBroadcastAsUser(intent, user, resultReceiver, scheduler, initialCode, initialData, initialExtras);
	}

	@Override
	public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {
		mConctext.removeStickyBroadcastAsUser(intent, user);
	}

	@Override
	public Intent registerReceiver(BroadcastReceiver receiver,
			IntentFilter filter) {
		return mConctext.registerReceiver(receiver, filter);
	}

	@Override
	public Intent registerReceiver(BroadcastReceiver receiver,
			IntentFilter filter, String broadcastPermission,
			Handler scheduler) {
		return mConctext.registerReceiver(receiver, filter, broadcastPermission, scheduler);
	}

	@Override
	public void unregisterReceiver(BroadcastReceiver receiver) {
		mConctext.unregisterReceiver(receiver);
	}

	@Override
	public ComponentName startService(Intent service) {
		return mConctext.startService(service);
	}

	@Override
	public boolean stopService(Intent service) {
		return mConctext.stopService(service);
	}

	@Override
	public boolean bindService(Intent service, ServiceConnection conn,
			int flags) {
		return mConctext.bindService(service, conn, flags);
	}

	@Override
	public void unbindService(ServiceConnection conn) {
		mConctext.unbindService(conn);
	}

	@Override
	public boolean startInstrumentation(ComponentName className,
			String profileFile, Bundle arguments) {
		return mConctext.startInstrumentation(className, profileFile, arguments);
	}

	@Override
	public Object getSystemService(String name) {
		if (LAYOUT_INFLATER_SERVICE.equals(name)) {
		    if (mInflater == null) {
		        mInflater = new JarLayoutInflater(this);
		    }
		    return mInflater;
		}
		return mConctext.getSystemService(name);
	}

	@Override
	public int checkPermission(String permission, int pid, int uid) {
		return mConctext.checkPermission(permission, pid, uid);
	}

	@Override
	public int checkCallingPermission(String permission) {
		return mConctext.checkCallingPermission(permission);
	}

	@Override
	public int checkCallingOrSelfPermission(String permission) {
		return mConctext.checkCallingOrSelfPermission(permission);
	}

	@Override
	public void enforcePermission(String permission, int pid, int uid,
			String message) {
		mConctext.enforcePermission(permission, pid, uid, message);
	}

	@Override
	public void enforceCallingPermission(String permission, String message) {
		mConctext.enforceCallingPermission(permission, message);
	}

	@Override
	public void enforceCallingOrSelfPermission(String permission,
			String message) {
		mConctext.enforceCallingOrSelfPermission(permission, message);
	}

	@Override
	public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {
		mConctext.grantUriPermission(toPackage, uri, modeFlags);
	}

	@Override
	public void revokeUriPermission(Uri uri, int modeFlags) {
		mConctext.revokeUriPermission(uri, modeFlags);
	}

	@Override
	public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
		return mConctext.checkUriPermission(uri, pid, uid, modeFlags);
	}

	@Override
	public int checkCallingUriPermission(Uri uri, int modeFlags) {
		return mConctext.checkCallingUriPermission(uri, modeFlags);
	}

	@Override
	public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
		return mConctext.checkCallingOrSelfUriPermission(uri, modeFlags);
	}

	@Override
	public int checkUriPermission(Uri uri, String readPermission,
			String writePermission, int pid, int uid, int modeFlags) {
		return mConctext.checkUriPermission(uri, pid, uid, modeFlags);
	}

	@Override
	public void enforceUriPermission(Uri uri, int pid, int uid,
			int modeFlags, String message) {
		mConctext.enforceUriPermission(uri, pid, uid, modeFlags, message);
	}

	@Override
	public void enforceCallingUriPermission(Uri uri, int modeFlags,
			String message) {
		mConctext.enforceCallingUriPermission(uri, modeFlags, message);
	}

	@Override
	public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags,
			String message) {
		mConctext.enforceCallingOrSelfUriPermission(uri, modeFlags, message);
	}

	@Override
	public void enforceUriPermission(Uri uri, String readPermission,
			String writePermission, int pid, int uid, int modeFlags,
			String message) {
		mConctext.enforceUriPermission(uri, readPermission, writePermission, pid, uid, modeFlags, message);
	}

	@Override
	public Context createPackageContext(String packageName, int flags)
			throws NameNotFoundException {
		return mConctext.createPackageContext(packageName, flags);
	}

	@Override
	public Context createConfigurationContext(
			Configuration overrideConfiguration) {
		return mConctext.createConfigurationContext(overrideConfiguration);
	}

	@Override
	public Context createDisplayContext(Display display) {
		return mConctext.createDisplayContext(display);
	}
	
	public String getBasePackageName() {
		return mConctext.getPackageName();
	}

	@Override
	public File[] getExternalFilesDirs(String type) {
		// TODO Auto-generated method stub
		return mConctext.getExternalFilesDirs(type);
	}

	@Override
	public File[] getObbDirs() {
		return mConctext.getObbDirs();
	}

	@Override
	public File[] getExternalCacheDirs() {
		return mConctext.getExternalCacheDirs();
	}
	
	public String getOpPackageName(){
		return getBasePackageName();
	}

//	@Override
//	public File getNoBackupFilesDir() {
//		return mConctext.getNoBackupFilesDir();
//	}
//	
//	@Override
//	public File getCodeCacheDir() {
//		return mConctext.getCodeCacheDir();
//	}
//	
//	@Override
//	public File[] getExternalMediaDirs() {
//		return mConctext.getExternalMediaDirs();
//	}
}
