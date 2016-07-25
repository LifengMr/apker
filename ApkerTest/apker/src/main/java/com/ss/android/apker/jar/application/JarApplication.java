package com.ss.android.apker.jar.application;

import android.app.Application;
import android.content.Context;

public class JarApplication extends Application {
	private static Context instance;

	@Override
	public void onCreate() {
		super.onCreate();
		//如果继承JarApplication  在这里初始化
		instance = this;
	}
	
	/**
	 * 获取application实例
	 * @return
	 */
	public static Context getInstance(){
		return instance;
	}
	
	/**
	 * 传递主工程Application
	 */
	public static void setInstance(Context application){
		instance = application;
	}

}
