package com.ss.android.apker.api;


/**
 * apker请求接口
 * @author chenlifeng1
 */
public class ApkerApi {
	public static final String TAG = "http";
	private static final String HOST = "http://apker.applinzi.com/";

	public static String getPluginsUrl() {
		return HOST + "plugins";
	}
}
