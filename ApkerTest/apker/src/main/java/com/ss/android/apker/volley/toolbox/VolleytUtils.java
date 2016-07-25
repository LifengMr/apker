package com.ss.android.apker.volley.toolbox;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class VolleytUtils {
	
	/**
	 * 当前是否有网络
	 * @param context
	 * @return
	 */
	public static boolean isNetAvaiable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if(info != null && info.isAvailable()){
			return true;
		}
        return false;
	}
}
