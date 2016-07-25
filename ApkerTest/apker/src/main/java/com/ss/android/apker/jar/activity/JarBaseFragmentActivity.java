package com.ss.android.apker.jar.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.ss.android.apker.jar.application.JarApplication;
import com.ss.android.apker.jar.loader.JarContext;
import com.ss.android.apker.jar.loader.JarLayoutInflater;
import com.ss.android.apker.jar.loader.JarResOverrideInterface;
import com.ss.android.apker.helper.JLog;

/**
 * ****导出到jar，导入到插件libs中，最后PluginLoader需要删除本来，否则无法实例化插件Activity*****
 * @author chenlifeng1
 *当插件activity需要被主应用调起时，需要继承此类
 *当layout布局中有自定义组件,需要用JarLayoutInflater和JarContext
 */
public abstract class JarBaseFragmentActivity {
	private static final String TAG = "JarBaseFragmentActivity";
	public static final String EXTRA_JARNAME = "extra.jarname";	//插件名称
	public static final String EXTRA_PACKAGENAME = "extra.packagename";	//插件包名
	public static final String EXTRA_CLASS = "extra.class";		//需要启动activity类名（类），不要.java后缀
    
    /**
     * 代理activity，可以当作Context来使用，会根据需要来决定是否指向this
     */
    protected FragmentActivity proxyActivity;
    protected JarContext context;
    
    public static final String PROXY_VIEW_ACTION = "com.letv.plugin.pluginloader.proxyfragmentactivity.VIEW";

    protected String jarname;
    protected String jar_packagename;
    
    protected ViewGroup root;
    
    protected void onCreate(Bundle savedInstanceState) {
    }
    
    /**
     * 设置代理类
     * @param proxyActivity
     * @param 
     */
    public void setProxy(FragmentActivity proxyActivity, String jarname, String jar_packagename) {
    	JLog.i("clf", "setProxy..proxyActivity="+proxyActivity+",jarname="+jarname+",jar_packagename="+jar_packagename);
        this.proxyActivity = proxyActivity;
        this.jarname = jarname;
        this.jar_packagename = jar_packagename;
        
        //获取根content,FrameLayout
        root = (ViewGroup) proxyActivity.findViewById(android.R.id.content);
        setJarResource(true);
        //创建插件Context
        context = new JarContext(proxyActivity, getClassLoader());
    }
    
    /**
     * 启动其他activity
     * @param className 包名+"activity.classname"
     */
    protected void startActivityByProxy(String className) {
    	Intent intent = new Intent(PROXY_VIEW_ACTION);
        intent.putExtra(EXTRA_JARNAME, jarname);
        intent.putExtra(EXTRA_PACKAGENAME, jar_packagename);
        intent.putExtra(EXTRA_CLASS, className);
        proxyActivity.startActivity(intent);
    }
    
    /**
     * 启动其他activity，并接收返回值，在插件activity中重写onActivityResult处理返回值
     * @param className 包名+"activity.classname"
     * @param requestCode
     */
    public void startActivityForResultByProxy(String className, int requestCode) {
    	Intent intent = new Intent(PROXY_VIEW_ACTION);
        intent.putExtra(EXTRA_JARNAME, jarname);
        intent.putExtra(EXTRA_PACKAGENAME, jar_packagename);
        intent.putExtra(EXTRA_CLASS, className);
        proxyActivity.startActivityForResult(intent, requestCode);
    }

    /**
     * 模拟Activity的setContentView
     */
    public void setContentView(View view) {
		root.addView(view);
    }

    /**
     * 模拟Activity的setContentView
     */
    public void setContentView(View view, LayoutParams params) {
    	root.addView(view, params);
    }

    /**
     * 模拟Activity的setContentView
     */
    public void setContentView(int layoutResID) {
    	JarLayoutInflater.from(context).inflate(layoutResID, root);	//允许自定义组件
    }

    /**
     * 模拟Activity的addContentView
     */
    public void addContentView(View view, LayoutParams params) {
    	root.addView(view, params);
    }

    /**
     * 模拟Activity的findViewById
     */
    public View findViewById(int id) {
    	return proxyActivity.findViewById(id);
    }
    
    /**
     * 模拟Activity的getResources()
     */
    public Resources getResources(){
    	return proxyActivity.getResources();
    }
    
    /**
     * 模拟Activity的onRestart
     */
    protected void onRestart() {
    }
    /**
     * 模拟Activity的onStart
     */ 
    protected void onStart() {
    }
    /**
     * 模拟Activity的onResume
     */ 
    protected void onResume() {
    }
    /**
     * 模拟Activity的onPause
     */ 
    protected void onPause() {
    }
    /**
     * 模拟Activity的onStop
     */ 
    protected void onStop() {
    }
    /**
     * 模拟Activity的onDestroy
     */ 
    protected void onDestroy() {
    }
    /**
     * 模拟Activity的onActivityResult
     */ 
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }
    /**
     * 模拟Activity的onWindowFocusChanged
     */ 
    public void onWindowFocusChanged(boolean hasFocus) {
    }
    /**
     * 模拟Activity的onKeyDown
     */
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
    	return false;
    }
    /**
     * 模拟Activity的dispatchTouchEvent
     */
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }
    /**
     * 模拟Activity的onNewIntent
     */
    protected void onNewIntent(Intent intent) {
    }
    /**
     * 模拟Activity的onSaveInstanceState
     */
    protected void onSaveInstanceState(Bundle outState) {
    }
    /**
     * 模拟Activity的onRestoreInstanceState
     */
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    }
    /**
     * 模拟Activity的onConfigurationChanged
     */
    public void onConfigurationChanged(Configuration newConfig) {
    }
    /**
     * 模拟Activity的onBackPressed
     */
    public void onBackPressed() {
    }
    
    /**
     * 获取activity
     * @return
     */
    public Activity getActivity(){
    	return proxyActivity;
    }
    
    /**
     * 获取Context
     */
    public Context getContext(){
    	return context;
    }
    
    /**
     * @param isJar 是否切换到插件资源管理器
     */
    public void setJarResource(boolean isJar){
    	if(isJar){
    		((JarResOverrideInterface)proxyActivity).setResourcePath(true, jarname, jar_packagename);
    	}else{
    		((JarResOverrideInterface)proxyActivity).setResourcePath(false, null, null);
    	}
    }
    
    public String getJarName(){
    	return jarname;
    }
    
    public String getJarPackageName(){
    	return jar_packagename;
    }
    
    public Context getMainApplication(){
    	return JarApplication.getInstance();
    }
    
    protected abstract ClassLoader getClassLoader();
}
