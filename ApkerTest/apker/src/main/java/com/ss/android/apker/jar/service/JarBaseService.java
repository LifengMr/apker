package com.ss.android.apker.jar.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class JarBaseService extends Service {
	private static final String TAG = "JarBaseService";
	public static final String PROXY_VIEW_ACTION = "com.letv.plugin.pluginloader.service.ProxyService.VIEW";
	
	/**
     * 代理service
     */
    protected Service mProxyService;
    
    /**
     * 模拟Service的onBind
     * @param intent
     * @return
     */
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
     * 设置代理类
     * @param proxyService
     * @param 
     */
    public void setProxy(Service proxyService) {
    	mProxyService = proxyService;
    }
    
    /**
     * 模拟Service的onCreate
     */
    public void onCreate() {
    }
    /**
     * 模拟Service的onStartCommand
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
    	return 0;
    }
    /**
     * 模拟Service的onRebind
     */
    public void onRebind(Intent intent) {
    }
    /**
     * 模拟Service的onUnbind
     */
    public boolean onUnbind(Intent intent) {
    	return false;
    }
    /**
     * 模拟Service的onDestroy
     */
    public void onDestroy() {
    }
}
