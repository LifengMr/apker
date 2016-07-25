package com.ss.android.apker.compat;

import android.content.res.AssetManager;
import com.ss.android.apker.helper.ReflectHelper;
import java.lang.reflect.Method;

/**
 * Created by chenlifeng on 16/5/22.
 */
public class AssetManagerCompat {

    public static AssetManager newInstance() {
        AssetManager assetManager = null;
        try {
            assetManager = AssetManager.class.newInstance();
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
        return assetManager;
    }

    public static int addAssetPath(AssetManager assetManager, String resDir) {
        Method method = ReflectHelper.getMethod(AssetManager.class, "addAssetPath", false, new Class[]{String.class});
        Object result = ReflectHelper.invoke(method, assetManager, new Object[]{resDir});
        return result == null ? 0 : (int)result;
    }

    public static int[] addAssetPaths(AssetManager assetManager, String[] resDirs) {
        Method method = ReflectHelper.getMethod(AssetManager.class, "addAssetPaths", false, new Class[]{String[].class});
        return ReflectHelper.invoke(method, assetManager, new Object[]{resDirs});
    }

}
