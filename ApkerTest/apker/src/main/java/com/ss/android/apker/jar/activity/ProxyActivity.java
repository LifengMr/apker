package com.ss.android.apker.jar.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.ss.android.apker.helper.FileHelper;
import com.ss.android.apker.helper.JLog;
import com.ss.android.apker.jar.application.JarApplication;
import com.ss.android.apker.jar.loader.JarLoader;

/**
 * @author
 * 启动插件插件activity时,启动此类,并传入插件路径和类名
 * 
 * *****启动时，比传参数******
 * EXTRA_JARNAME---插件名称 （**.apk）
 * EXTRA_PACKAGENAME---插件包名 (com.letv.***)
 * EXTRA_CLASS-----插件类("Demo"不带.class)
 * **********************
 */
public class ProxyActivity extends JarMainBaseActivity {
	private static final String TAG = "ProxyActivity";
	public static final String EXTRA_JARNAME = "extra.jarname";	//插件名称
	public static final String EXTRA_PACKAGENAME = "extra.packagename";	//插件包名
	public static final String EXTRA_CLASS = "extra.class";		//需要启动activity类名（类），不要.class后缀
	private String mClass;	//类的名称 不带.class，"Demo"
	private String mJarname;
	private String mJarPackagename;
    private String mDexPath;
    private JarBaseActivity mJarActivity;		//插件Activity实例，非acitivity
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		setContentView(R.layout.plugin_root);
		
		init();
		
		if(mJarActivity != null){
			mJarActivity.onCreate(savedInstanceState);
		}
	}
	
	private void init(){
		mJarPackagename = getIntent().getStringExtra(EXTRA_PACKAGENAME);
		mJarname = getIntent().getStringExtra(EXTRA_JARNAME);
		mClass = getIntent().getStringExtra(EXTRA_CLASS);
		mDexPath = FileHelper.getDexPath(this, mJarPackagename, mJarname);

        if (mClass == null) {
			initJarActivity();
        } else {
			initJarActivity(mClass);
        }

        //注册广播
        registerReceiver();
	}
	
	/**
	 * 不传具体类名时，启动插件主Activity
	 */
	protected void initJarActivity() {
        PackageInfo packageInfo = getPackageManager().getPackageArchiveInfo(mDexPath, PackageManager.GET_ACTIVITIES);
        if ((packageInfo.activities != null) && (packageInfo.activities.length > 0)) {
            String activityName = packageInfo.activities[0].name;
            mClass = activityName;
			initJarActivity(mClass);
        }
    }

	/**
	 * 通过具体类，启动acitity
	 * @param className
	 */
    protected void initJarActivity(final String className) {
        try {
        	Class<?> localClass = JarLoader.loadClass(this, mJarname, mJarPackagename, className);
        	Object instance = JarLoader.newInstance(localClass, new Class[] {}, new Object[] {});
        	
        	setRemoteActivity(instance);

			mJarActivity.setProxy(this, mJarname, mJarPackagename);
        } catch (Exception e) {
        	JLog.i("clf", "!!!ProxyActivity e is "+e.getMessage());
        }
    }
    
    //保存插件activity实例引用
    protected void setRemoteActivity(Object activity) {
        try {
			mJarActivity = (JarBaseActivity) activity;
        } catch (ClassCastException e) {
        	JLog.i("clf", "!!!setRemoteActivity e="+e.getMessage());
        }
    }
    
    //获取插件activity
    public JarBaseActivity getRemoteActivity(){
    	return mJarActivity;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(mJarActivity != null){
			mJarActivity.onActivityResult(requestCode, resultCode, data);
    	}
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mJarActivity != null){
			mJarActivity.onStart();
        }
    }
    
    @Override
    protected void onRestart() {
        super.onRestart();
        if(mJarActivity != null){
			mJarActivity.onRestart();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(mJarActivity != null){
			mJarActivity.onResume();
        }
    }
    
    @Override
    protected void onPause() {
    	if(mJarActivity != null){
			mJarActivity.onPause();
    	}
        super.onPause();
    }

    @Override
    protected void onStop() {
    	if(mJarActivity != null){
			mJarActivity.onStop();
    	}
        super.onStop();
    }
    
    @Override
    protected void onDestroy() {
    	unregisterReceiver(mReceiver);
    	if(mJarActivity != null){
			mJarActivity.onDestroy();
    	}
        super.onDestroy();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(mJarActivity != null && mJarActivity.onKeyDown(keyCode, event)){
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    	if(mJarActivity != null && mJarActivity.dispatchTouchEvent(ev)){
    		return true;
    	}
    	return super.dispatchTouchEvent(ev);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	super.onWindowFocusChanged(hasFocus);
    	if(mJarActivity != null){
			mJarActivity.onWindowFocusChanged(hasFocus);
    	}
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
    	if(mJarActivity != null){
			mJarActivity.onNewIntent(intent);
    	}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	if(mJarActivity != null){
			mJarActivity.onSaveInstanceState(outState);
    	}
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	if(mJarActivity != null){
			mJarActivity.onRestoreInstanceState(savedInstanceState);
    	}
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	if(mJarActivity != null){
			mJarActivity.onConfigurationChanged(newConfig);
    	}
    }
    
    @Override
    public void onBackPressed() {
    	super.onBackPressed();
    	if(mJarActivity != null){
			mJarActivity.onBackPressed();
    	}
    }
    
    /**
	 * 注册广播
	 * 可以同时注册多个BroadcastReceiver，所以并不影响插件内继续注册广播
	 */
	private void registerReceiver(){
		//应用内配置语言
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_LOCALE_CHANGED);
		registerReceiver(mReceiver, filter);
	}
	
	/**
	 * 由于插件的Resouces新是新建的，所以这里需要做config同步操作，与主工程同步
	 */
	private void updateLocalConfig(){
		if(JarApplication.getInstance() == null){
			return;
		}
		Resources resources = getResources();
		Configuration config = resources.getConfiguration();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		config.locale = JarApplication.getInstance().getResources().getConfiguration().locale;
		resources.updateConfiguration(config, metrics);
	}
    
    /**
     * 监听系统语言切换广播
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			JLog.i("clf", "config...action="+action);
			if(action.equals(Intent.ACTION_LOCALE_CHANGED)){
				//切换系统语言
				updateLocalConfig();
			}
		}
	};
}
