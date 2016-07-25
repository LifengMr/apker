package com.ss.android.apker.helper;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Base64;

import com.ss.android.apker.Apker;
import com.ss.android.apker.compat.InstrumentationCompat;
import com.ss.android.apker.entity.LoadedApk;
import com.ss.android.apker.hook.ApkClassLoader;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by chenlifeng on 16/5/23.
 */
public class ApkHelper {
    public static final String TAG = ApkHelper.class.getName();
    private static final String[] SYSTEM_PREFIX = new String[] {"android."};

    public static boolean isSystemClass(String className) {
        if (TextUtils.isEmpty(className)) {
            return false;
        }
        for (String prefix : SYSTEM_PREFIX) {
            if (className.startsWith(prefix)) {
                JLog.i(TAG, "isSystemClass true className=" + className);
                return true;
            }
        }
        return false;
    }

    public static boolean isMainProcess(Context context) {
        // TODO if mainProcess is not packageName, you should modify here
        String mainProcess = context.getPackageName();
        return TextUtils.equals(getProcessName(context, android.os.Process.myPid()), mainProcess);
    }

    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    private static boolean checkAndRunApplication(String packageName) {
        LoadedApk loadedApk = Apker.ins().getLoadedApk(packageName);
        if (loadedApk == null || loadedApk.getPackageInfo() == null
                || loadedApk.getPackageInfo().applicationInfo == null) {
            return false;
        }
        ApplicationInfo info = loadedApk.getPackageInfo().applicationInfo;
        if (TextUtils.isEmpty(info.className)) {
            return true;
        }
        try {
            Application application = loadedApk.getApplication();
            if (application != null) {
                return true;
            }
            Context context = Apker.ins().host();
            Class applicationClazz = context.getClassLoader().loadClass(info.className);
            application = Instrumentation.newApplication(applicationClazz, context);
            loadedApk.setApplication(application);
            Instrumentation hostInstrumentation = InstrumentationCompat.getHostInstrumentation();
            if (hostInstrumentation != null && application != null) {
                hostInstrumentation.callApplicationOnCreate(application);
                return true;
            }
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * activity、service buildIntent (only entrance)
     * check application running
     * @param context
     * @param className
     * @return
     */
    public static Intent buildIntent(Context context, String className) {
        Intent intent = new Intent();
        try {
            Class<?> clazz = context.getClassLoader().loadClass(className);
            if (clazz != null) {
                ClassLoader classLoader = clazz.getClassLoader();
                if (classLoader instanceof ApkClassLoader) {
                    String pacakgeName = ((ApkClassLoader) classLoader).mPackagename;
                    boolean success = checkAndRunApplication(pacakgeName);
                    if (!success) {
                        return intent;
                    }
                }
                intent.setClass(context, clazz);
            }
        } catch (ClassNotFoundException e) {
            // pre check
        }
        return intent;
    }

    public static Intent buildIntent(Context context, String packageName, String activityName) {
        String className = packageName + "." + activityName;
        return buildIntent(context, className);
    }

    public static boolean verifySignature(Signature[] signatures, String originSign) {
        if (TextUtils.isEmpty(originSign) || JavaHelper.isArrayEmpty(signatures)) {
            return false;
        }

        for (Signature signature : signatures) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA");
                digest.update(signature.toByteArray());
                String sign = Base64.encodeToString(digest.digest(), Base64.DEFAULT);
                JLog.i(TAG, "verifySignature---sign=" + sign + ",originSign=" + originSign);
                if (TextUtils.equals(sign.trim(), originSign.trim())) {
                    return true;
                }
            } catch (NoSuchAlgorithmException e) {
            }
        }
        return false;
    }

    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager manager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager.getActiveNetworkInfo();
            return (info != null && info.isAvailable());
        } catch (Exception e) {
            // ignore
        }
        return false;
    }
}
