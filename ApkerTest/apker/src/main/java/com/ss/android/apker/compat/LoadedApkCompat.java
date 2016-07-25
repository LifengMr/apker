package com.ss.android.apker.compat;

import com.ss.android.apker.helper.ReflectHelper;

import java.lang.reflect.Field;

/**
 * Created by chenlifeng on 16/5/25.
 */
public class LoadedApkCompat {

    public static Class<?> clazz() {
        try {
            return Class.forName("android.app.LoadedApk");
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    public static void setClassLoader(Object activityThread, String packageName, ClassLoader classLoader) {
        Object loadedApk = ActivityThreadCompat.getPackage(activityThread, packageName);
        Field field = ReflectHelper.getField(clazz(), "mClassLoader", false);
        ReflectHelper.writeField(field, loadedApk, classLoader);
    }
}
