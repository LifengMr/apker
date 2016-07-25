package com.ss.android.apker;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.ss.android.apker.compat.ActivityThreadCompat;
import com.ss.android.apker.compat.LoadedApkCompat;
import com.ss.android.apker.database.PluginDAO;
import com.ss.android.apker.entity.ApkEntity;
import com.ss.android.apker.entity.LoadedApk;
import com.ss.android.apker.helper.FileHelper;
import com.ss.android.apker.helper.JLog;
import com.ss.android.apker.hook.ApkClassLoader;
import com.ss.android.apker.hook.MainClassLoader;
import com.ss.android.apker.parser.ApkPackageParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

;

/**
 * Created by chenlifeng on 16/5/19.
 */
public class Apker {
    public static final String TAG = Apker.class.getName();

    public static String MAIN_PACKAGE_NAME;
    private final ArrayMap<String, LoadedApk> mPackages = new ArrayMap<>();
    private Context mHostContext;
    private PluginDAO mPluginDAO;
    private static Apker sApkLoader;

    private Apker() {}

    public static Apker ins() {
        if (sApkLoader == null) {
            synchronized (Apker.class) {
                if (sApkLoader == null) {
                    sApkLoader = new Apker();
                }
            }
        }
        return sApkLoader;
    }

    @SuppressLint("NewApi")
    public void init(final Context hostContext) {
        if (!(hostContext instanceof Application)) {
            throw new IllegalArgumentException("init hostContext must be appliction");
        }

        mHostContext = hostContext;
        MAIN_PACKAGE_NAME = hostContext.getPackageName();
        mPluginDAO = new PluginDAO(mHostContext);

        MainClassLoader classLoader = new MainClassLoader(hostContext.getClassLoader());
        Object activityThread = ActivityThreadCompat.currentActivityThread();

        ActivityThreadCompat.setInstrumentation(activityThread);
        ActivityThreadCompat.setHHandler(activityThread);
        LoadedApkCompat.setClassLoader(activityThread, MAIN_PACKAGE_NAME, classLoader);

        loadPlugins(mHostContext);
    }

    public void installPlugin(final ApkEntity entity) {
        if (entity == null || TextUtils.isEmpty(entity.packageName)
                || TextUtils.isEmpty(entity.name)) {
            return;
        }
        // TODO add thread manager
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sourcePath = FileHelper.getDownloadFilePath(entity.name);
                String descPath = FileHelper.getDexPath(mHostContext, entity.packageName, entity.name);
                int versionCode = getAvailableVersionCode(sourcePath, entity.packageName);
                if (versionCode <= 0) {
                    return;
                }
                entity.version = versionCode;
                boolean success = FileHelper.copyFile(sourcePath, descPath);
                if (!success) {
                    return;
                }
                try {
                    FileHelper.copySoLibs(mHostContext, entity.packageName, entity.name);
                    // not start yet
                    if (!mPackages.containsKey(entity.packageName)) {
                        boolean addSuccess = addPlugin(mHostContext, entity);
                        if (addSuccess) {
                            mPluginDAO.savePlugindInfo(entity);
                        } else {
                            handleInstallFailed(entity.packageName, entity.name);
                        }
                    } else {
                        mPluginDAO.savePlugindInfo(entity);
                    }
                } catch (Exception e) {
                    JLog.i(TAG, "Apker...installPlugin e=" + e);
                }
            }
        }).start();
    }

    public void loadPlugins(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO TEST
//                JarUtil.updatePlugin(mHostContext, JarUtil.TYPE_LOADED_APK, true);

                // installed plugins
                ArrayList<ApkEntity> plugins = mPluginDAO.getPlugins();
                for (ApkEntity entity : plugins) {
                    addPlugin(context, entity);
                }
                JLog.i(TAG, "apker init end!");
            }
        }).start();
    }

    public boolean addPlugin(Context hostContext, ApkEntity entity) {
        if (entity == null) {
            return false;
        }
        JLog.i(TAG, "Apker...addPlugin packageName=" + entity.packageName);
        String dexPath = FileHelper.getDexPath(hostContext, entity.packageName, entity.name);
        File dexFile = new File(dexPath);
        if (!dexFile.exists()) {
            return false;
        }
        String optDexPath = FileHelper.getOptDexDir(hostContext, entity.packageName);
        String libPath = FileHelper.getLibDir(hostContext, entity.packageName);
        ApkClassLoader classLoader = new ApkClassLoader(entity.packageName, dexPath, optDexPath, libPath,
                hostContext.getClassLoader());
        LoadedApk loadedApk = LoadedApk.create(classLoader, entity);
        JLog.i(TAG, "Apker...addPlugin loadedApk=" + loadedApk);
        if (loadedApk != null) {
            mPackages.put(entity.packageName, loadedApk);
            installContentProviders(loadedApk);
            return true;
        } else {
            // TODO whether need add retry?
            return false;
        }
    }

    private void installContentProviders(LoadedApk loadedApk) {
        if (loadedApk == null || loadedApk.getPackageInfo() == null
                || loadedApk.getPackageInfo().providers == null) {
            return;
        }
        boolean success = ActivityThreadCompat.installContentProviders(mHostContext,
                Arrays.asList(loadedApk.getPackageInfo().providers));
        if (success) {
            loadedApk.success();
        } else {
            loadedApk.fail();
            mPackages.remove(loadedApk.getPackageName());
        }
    }

    private int getAvailableVersionCode(String sourcePath, String packageName) {
        ApkPackageParser packageParser = new ApkPackageParser(sourcePath, packageName);
        if (!packageParser.parsePackage()) {
            return -1;
        }
        PackageInfo packageInfo = packageParser.getPackageInfo();
        if (packageInfo == null || mPluginDAO == null) {
            return -1;
        }
        ApkEntity entity = mPluginDAO.getPluginInfo(packageName);
        if (entity != null && packageInfo.versionCode <= entity.version) {
            return -1;
        }
        return packageInfo.versionCode;
    }

    private void handleInstallFailed(String packageName, String apkName) {
        String dexPath = FileHelper.getDexPath(mHostContext, packageName, apkName);
        File file = new File(dexPath);
        if (file.exists()) {
            file.delete();
        }
        mPluginDAO.deletePlugin(packageName);
    }

    public Class<?> loadClass(String className) {
        Class<?> clazz = loadClassInner(className);
        if (clazz == null) {
            clazz = loadClassGlobal(className);
        }
        JLog.i(TAG, "loadClass clazz=" + clazz);
        return clazz;
    }

    public Class<?> loadClassInner(String className)  {
        JLog.i(TAG, "loadClassInner className=" + className);
        String packageName = getPackageName(className);
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        LoadedApk loadedApk = mPackages.get(packageName);
        if (loadedApk == null) {
            return null;
        }
        ClassLoader classLoader = loadedApk.getClassLoader();
        JLog.i(TAG, "loadClassInner classLoader=" + classLoader);
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    public Class<?> loadClassGlobal(String className) {
        JLog.i(TAG, "loadClassInner className=" + className);
        Iterator<Map.Entry<String, LoadedApk>> it = mPackages.entrySet().iterator();
        while (it.hasNext()) {
            LoadedApk loadedApk = it.next().getValue();
            if (loadedApk == null) {
                continue;
            }
            ClassLoader classLoader = loadedApk.getClassLoader();
            try {
                Class<?> clazz = classLoader.loadClass(className);
                JLog.i(TAG, "loadClassInner clazz=" + clazz);
                if (clazz != null) {
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
            }
        }
        return null;
    }

    public String getPackageName(String className){
        if (TextUtils.isEmpty(className)) {
            return null;
        }
        Set<String> keys = mPackages.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String packageName = iterator.next();
            if (className.startsWith(packageName)) {
                return packageName;
            }
        }
        return null;
    }

    public LoadedApk getLoadedApk(String packageName) {
        return mPackages.get(packageName);
    }

    public boolean isPluginInstalled(String packageName) {
        LoadedApk loadedApk = mPackages.get(packageName);
        return loadedApk != null && loadedApk.isSuccess();
    }

    public Context host() {
        return mHostContext;
    }
}
