package com.ss.android.apker.hook;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.ss.android.apker.Apker;
import com.ss.android.apker.entity.ApkResources;
import com.ss.android.apker.helper.JLog;

/**
 * Created by chenlifeng on 16/5/20.
 */
public class ApkContextImp extends ContextWrapper {
    public static final String TAG = ApkContextImp.class.getName();

    private Context mBase;
    private ApkClassLoader mClassLoader;
    private ApkResources mResources;
    private Resources.Theme mTheme;
    private Object mResourcesManager;
    private String mApkResDir;

    public ApkContextImp(Context contextImpl, ApkClassLoader classLoader) {
        super(contextImpl);
        this.mBase = contextImpl;
        this.mClassLoader = classLoader;

        init();
    }

    private void init() {
//        mResourcesManager = RefInvoke.getFieldOjbect("android.app.ResourcesManager", mBase, "mResourcesManager");
//        String resDir = mJarClassLoader.mJarpath;
//        Object displayId = RefInvoke.invokeMethod("android.app.ContextImpl", "getDisplayId",
//                mBase, new Class[]{}, new Object[]{});
        mApkResDir = mClassLoader.mJarpath;
        JLog.i(TAG, "mApkResDir=" + mApkResDir);
        JLog.i(TAG, "mBase=" + mBase);
        mResources = ApkResources.getResourceByCl(mClassLoader, mBase);
        Resources.Theme theme = getResources().newTheme();
        theme.setTo(Apker.ins().host().getTheme());
        //TODO applayStyle
        mTheme = theme;
    }

    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    @Override
    public Resources getResources() {
        return mResources.getResources();
    }

    @Override
    public Resources.Theme getTheme() {
        if (mTheme == null) {
            return mBase.getTheme();
        }
        return mTheme;
    }

    @Override
    public String getPackageResourcePath() {
        return mApkResDir;
    }
}
