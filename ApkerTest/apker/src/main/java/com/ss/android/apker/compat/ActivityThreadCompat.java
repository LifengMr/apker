package com.ss.android.apker.compat;

import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.os.Handler;
import android.os.Looper;

import com.ss.android.apker.helper.ReflectHelper;
import com.ss.android.apker.hook.ApkInstrumentation;
import com.ss.android.apker.hook.HCallBack;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by chenlifeng on 16/5/25.
 */
public class ActivityThreadCompat {

    public static Class<?> clazz() {
        try {
            return Class.forName("android.app.ActivityThread");
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    public static Object currentActivityThread() {
        Object activityThread = ReflectHelper.invokeStaticMethod(clazz(), "currentActivityThread", null, null);
        return activityThread;
    }

    private static Object reGetActivityThread() {
        Looper mainLooper = Looper.getMainLooper();
        Handler handler = new Handler(mainLooper);
        final Object lock = new Object();
        final Object[] activityThread = new Object[1];
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    activityThread[0] = ReflectHelper.invokeStaticMethod(clazz(), "currentActivityThread", null, null);
                } catch (Exception e) {
                } finally {
                    lock.notify();
                }
            }
        });
        if (activityThread[0] == null && Looper.myLooper() != mainLooper) {
            try {
                lock.wait(300);
            } catch (InterruptedException e) {
            }
        }
        return activityThread[0];
    }

    public static Object getPackage(Object activityThread, String packageName) {
        Object loadedApk;
        Field field = ReflectHelper.getField(clazz(), "mPackages", false);
        Map mPackages = ReflectHelper.readField(field, activityThread);
        WeakReference ref = (WeakReference) mPackages.get(packageName);
        loadedApk = ref.get();
        return loadedApk;
    }

    public static void setInstrumentation(Object activityThread) {
        Field field = ReflectHelper.getField(clazz(), "mInstrumentation", false);
        Instrumentation instrumentation = ReflectHelper.readField(field, activityThread);
        if (!(instrumentation instanceof ApkInstrumentation)) {
            ApkInstrumentation apkInstrumentation = new ApkInstrumentation(instrumentation);
            ReflectHelper.writeField(field, activityThread, apkInstrumentation);
        }
    }

    public static void setHHandler(Object activityThread) {
        Field field = ReflectHelper.getField(clazz(), "mH", false);
        Handler handler = ReflectHelper.readField(field, activityThread);
        field = ReflectHelper.getField(Handler.class, "mCallback", true);
        ReflectHelper.writeField(field, handler, new HCallBack());
    }

    public static boolean installContentProviders(Context context, List<ProviderInfo> providers) {
        if (providers == null || providers.size() == 0) {
            return true;
        }
        Object activityThread = currentActivityThread();
        Method method = ReflectHelper.getMethod(clazz(), "installContentProviders", false, new Class[] {Context.class, List.class});
        boolean result = ReflectHelper.invokeMethod(method, activityThread, new Object[] {context, providers});
        return result;
    }
}
