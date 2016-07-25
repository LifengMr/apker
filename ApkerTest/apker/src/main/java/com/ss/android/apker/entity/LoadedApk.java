package com.ss.android.apker.entity;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.text.TextUtils;

import com.ss.android.apker.helper.JLog;
import com.ss.android.apker.hook.ApkClassLoader;
import com.ss.android.apker.parser.ApkPackageParser;


/**
 * Created by chenlifeng on 16/5/22.
 */
public class LoadedApk {
    public static final String TAG = LoadedApk.class.getName();

    // install status
    public static int ST_UNINSTALL = 1001;
    public static int ST_INSTALLING = 1002;
    public static int ST_FAILED = 1003;
    public static int ST_SUCESS = 1004;

    private ApkClassLoader mClassLoader;
    private Resources mResources;
    private PackageInfo mPackageInfo;
    private Application mApplication;
    private String mPackageName;
    private String mResDir;
    private int mStatus = ST_UNINSTALL;

    public static LoadedApk create(ApkClassLoader classLoader, ApkEntity entity) {
        LoadedApk loadedApk = new LoadedApk(classLoader);
        loadedApk.parsePackage(classLoader.mJarpath, entity);
        return loadedApk.isNotFailed() ? loadedApk : null;
    }

    private LoadedApk(ApkClassLoader classLoader) {
        if (classLoader == null) {
            fail();
            throw new IllegalArgumentException("ApkClassLoader cannot be null");
        }
        mClassLoader = classLoader;
        mPackageName = mClassLoader.mPackagename;
        mResDir = mClassLoader.mJarpath;
        mStatus = ST_INSTALLING;
    }

    private void parsePackage(String packagePath, ApkEntity entity) {
        if (TextUtils.isEmpty(packagePath) || entity == null) {
            fail();
            return;
        }
        ApkPackageParser packageParser = new ApkPackageParser(packagePath, entity.packageName);
        boolean success = packageParser.parsePackage();
        mResources = packageParser.getResources();
        mPackageInfo = packageParser.getPackageInfo();
        if (success) {
            //TODO
//            success = ApkHelper.verifySignature(mPackageInfo.signatures, entity.signature);
//            success = ApkHelper.verifySignature(mPackageInfo.signatures, Constant.SIGNATURE_GAME);
            JLog.i(TAG, "parsePackage-verifySignature packageName=" + entity.packageName + ",success=" + success);
        }
        if (success) {
            success = packageParser.collectActivities();
        }
        if (success) {
            success = packageParser.collectProviders();
        }
        if (!success) {
            fail();
            return;
        }

        ActivityIntentResolver.ins().register(this, packageParser);

        success();
    }

    public boolean isSuccess() {
        return mStatus == ST_SUCESS;
    }

    public boolean isNotFailed() {
        return mStatus != ST_FAILED;
    }

    public void fail() {
        mStatus = ST_FAILED;
    }

    public void success() {
        mStatus = ST_SUCESS;
    }

    public ClassLoader getClassLoader() {
        return mClassLoader;
    }

    public Resources getResources() {
        return mResources;
    }

    public PackageInfo getPackageInfo() {
        return mPackageInfo;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getResDir() {
        return mResDir;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public Application getApplication() {
        return mApplication;
    }

    public void setApplication(Application application) {
        mApplication = application;
    }
}
