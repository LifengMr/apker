package com.ss.android.apker.jar.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import com.ss.android.apker.helper.FileHelper;
import com.ss.android.apker.jar.loader.JarLoader;
import com.ss.android.apker.jar.util.JarUtil;

import java.lang.reflect.Method;
import java.util.HashMap;

public class ProxyService extends JarMainBaseService {
	private static final String TAG = "ProxyService";
	public static final String EXTRA_JARNAME = "extra.jarname";	//插件名称
	public static final String EXTRA_PACKAGENAME = "extra.packagename";	//插件包名
	public static final String EXTRA_CLASS = "extra.class";		//需要启动service类名（类），不要.class后缀
	private String mClass;	//类的名称 不带.class，"Demo"
	private String jarname;
	private String jar_packagename;
    private String mDexPath;
    private Service mRemoteService;
    private HashMap<String, Method> mServiceLifecircleMethods = new HashMap<String, Method>();	//存放插件生命周期函数
    private IBinder binder = new LocalBinder();
    private boolean isBind = false;
    
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	//start方式启动
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		init(intent, flags, startId);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	//bind方式启动
	@Override
	public IBinder onBind(Intent intent) {
		
		isBind = true;
		init(intent, -1, -1);
		
		return binder;
	}
	
	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		
		Method onRebind = mServiceLifecircleMethods.get("onRebind");
        if (onRebind != null) {
            try {
            	onRebind.invoke(mRemoteService, intent);
            } catch (Exception e) {
            }
        }
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Method onUnbind = mServiceLifecircleMethods.get("onUnbind");
        if (onUnbind != null) {
            try {
            	onUnbind.invoke(mRemoteService, intent);
            } catch (Exception e) {
            }
        }
		return super.onUnbind(intent);
	}
	
	@Override
	public void onDestroy() {
		Method onDestroy = mServiceLifecircleMethods.get("onDestroy");
        if (onDestroy != null) {
            try {
                onDestroy.invoke(mRemoteService);
            } catch (Exception e) {
            }
        }
		super.onDestroy();
	}
	
	private void init(Intent intent, int flags, int startId ){
		jarname = intent.getStringExtra(EXTRA_JARNAME);
		jar_packagename = intent.getStringExtra(EXTRA_PACKAGENAME);
		mClass = intent.getStringExtra(EXTRA_CLASS);
		mDexPath = FileHelper.getDexPath(this, jar_packagename, jar_packagename);
		
        //将资源目录重定向到插件路径下
		setResourcePath(true, jarname, jar_packagename);
        
        if (mClass == null) {
            launchTargetService(intent, flags, startId);
        } else {
            launchTargetService(mClass, intent, flags, startId);
        }
	}
	
	/**
	 * 不传具体类名时，启动插件第一个manifest
	 */
	protected void launchTargetService(Intent intent, int flags, int startId) {
        PackageInfo packageInfo = getPackageManager().getPackageArchiveInfo(
                mDexPath, 1);
        if ((packageInfo.services != null)
                && (packageInfo.services.length > 0)) {
            String serviceName = packageInfo.services[0].name;
            mClass = serviceName;
            launchTargetService(mClass, intent, flags, startId);
        }
    }

	/**
	 * 通过具体类，启动Service
	 * @param className
	 */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    protected void launchTargetService(final String className, Intent intent, int flags, int startId) {
        try {
        	Class<?> localClass = JarLoader.loadClass(this, jarname, jar_packagename, className);
        	Object instance = JarLoader.newInstance(localClass, new Class[] {}, new Object[] {});
        	setRemoteActivity(instance);
        	
        	//将插件service生命周期函数存入map
            instantiateLifecircleMethods(localClass);

            //将代理句柄和插件地址传入
            JarLoader.invokeMethodByObj(instance, "setProxy", new Class[] { Service.class }, new Object[] { this });
            
            //调用service.onCreate启动插件service
            Method onCreate = mServiceLifecircleMethods.get("onCreate");
            onCreate.invoke(instance);
            
            if(!isBind){
            	Method onStartCommand = mServiceLifecircleMethods.get("onStartCommand");
                onStartCommand.invoke(instance, intent, flags, startId);
            }else{
            	Method onBind = mServiceLifecircleMethods.get("onBind");
            	onBind.invoke(instance, intent);
            }
        } catch (Exception e) {
        }
    }
    
    /**
     * 保存插件service生命周期函数
     * @param localClass
     */
    protected void instantiateLifecircleMethods(Class<?> localClass) {
        String[] methodNames = new String[] {
                "onCreate",
                "onDestroy"
        };
        for (String methodName : methodNames) {
            Method method = null;
            try {
                method = localClass.getDeclaredMethod(methodName);
                method.setAccessible(true);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            mServiceLifecircleMethods.put(methodName, method);
        }

        if(!isBind){
        	Method onStartCommand = null;
            try {
            	onStartCommand = localClass.getDeclaredMethod("onStartCommand", Intent.class, int.class, int.class);
            	onStartCommand.setAccessible(true);
            	mServiceLifecircleMethods.put("onStartCommand", onStartCommand);
            } catch (NoSuchMethodException e) {
            }
        }else{
        	try {
        		Method onBind = localClass.getDeclaredMethod("onBind", Intent.class);
        		onBind.setAccessible(true);
        		mServiceLifecircleMethods.put("onBind", onBind);
			} catch (NoSuchMethodException e) {
			}
        	
        	try {
        		Method onRebind = localClass.getDeclaredMethod("onRebind", Intent.class);
        		onRebind.setAccessible(true);
        		mServiceLifecircleMethods.put("onRebind", onRebind);
			} catch (NoSuchMethodException e) {
			}
        	
        	try {
        		Method onUnbind = localClass.getDeclaredMethod("onUnbind", Intent.class);
        		onUnbind.setAccessible(true);
        		mServiceLifecircleMethods.put("onUnbind", onUnbind);
			} catch (NoSuchMethodException e) {
			}
        }
    }
    
    //保存插件service实例引用
    protected void setRemoteActivity(Object service) {
        try {
            mRemoteService = (Service) service;
        } catch (ClassCastException e) {
        }
    }
    
    public class LocalBinder extends Binder{
    	public ProxyService getService(){
    		return ProxyService.this;
    	}
    }
}
