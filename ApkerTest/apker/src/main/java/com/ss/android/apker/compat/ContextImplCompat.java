package com.ss.android.apker.compat;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import com.ss.android.apker.helper.ReflectHelper;
import com.ss.android.apker.helper.JLog;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by chenlifeng on 16/5/25.
 */
public class ContextImplCompat {

    public static Class<?> clazz() {
        try {
            return Class.forName("android.app.ContextImpl");
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    /**
     * 重置系统Resources
     * @param context 必须是ContextImpl
     * @param resources
     */
    public static void setResources(Context context, Resources resources) {
        if (context == null || resources == null) {
            return;
        }
        Class clazz = ContextImplCompat.clazz();
        if (context.getClass() != clazz) {
            throw new IllegalArgumentException("context must be ContextImpl");
        }

        Field field = ReflectHelper.getField(clazz, "mResources", false);
        ReflectHelper.writeField(field, context, resources);

        updateTopLevelResources(context, resources);
    }

    /**
     * 重置系统Resources
     * @param context 必须是ContextImpl
     * @param resources
     */
    private static void updateTopLevelResources(Context context, Resources resources) {
        Class clazz = ContextImplCompat.clazz();
        if (context.getClass() != clazz) {
            throw new IllegalArgumentException("context must be ContextImpl");
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Field field = ReflectHelper.getField(clazz, "mPackageInfo", false);
            Object loadedApk = ReflectHelper.readField(field, context);
            if (loadedApk == null) {
                return;
            }
            field = ReflectHelper.getField(loadedApk.getClass(), "mResources", false);
            ReflectHelper.writeField(field, loadedApk, resources);
        } else {
            Field field = ReflectHelper.getField(clazz, "mResourcesManager", false);
            Object resourcesManager = ReflectHelper.readField(field, context);
            if (resourcesManager == null) {
                return;
            }
            field = ReflectHelper.getField(resourcesManager.getClass(), "mActiveResources", false);
            Map activeResources = ReflectHelper.readField(field, resourcesManager);
            JLog.i("clf", "~~~~~activeResources=" + activeResources);
            if (activeResources == null) {
                return;
            }
            Object k = activeResources.keySet().iterator().next();
            JLog.i("clf", "~~~~~~k=" + k);
            WeakReference<Resources> ref = new WeakReference<>(resources);
            JLog.i("clf", "~~~~ref=" + ref);
            activeResources.put(k, ref);
        }
    }

    /**
     * >=4.4 KITKAT
     * @param contextImpl
     * @return
     */
    public static String getOpPackageName(Context contextImpl) {
        if (contextImpl == null) {
            return null;
        }

        Method method = ReflectHelper.getMethod(clazz(), "getOpPackageName", false, null);
        return ReflectHelper.invoke(method, contextImpl, null);
    }

    /**
     * >= 4.3 JELLY_BEAN_MR2
     * @param contextImpl
     * @return
     */
    public static String getBasePackageName(Context contextImpl) {
        if (contextImpl == null) {
            return null;
        }

        Method method = ReflectHelper.getMethod(clazz(), "getBasePackageName", false, null);
        return ReflectHelper.invoke(method, contextImpl, null);
    }
}
