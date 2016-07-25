package com.ss.android.apker.compat;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by chenlifeng on 16/5/25.
 */
public class ResourcesCompat {

    public static Resources createResources(Context context, String[]resDirs) {
        AssetManager assetManager = AssetManagerCompat.newInstance();
        AssetManagerCompat.addAssetPaths(assetManager, resDirs);
        Resources hostResources = context.getResources();
        DisplayMetrics dm = hostResources.getDisplayMetrics();
        Configuration configuration = hostResources.getConfiguration();
        if (hostResources.getClass() == Resources.class) {
            return new Resources(assetManager, dm, configuration);
        } else {
            try {
                Constructor constructor = hostResources.getClass().getConstructor(AssetManager.class,
                        DisplayMetrics.class, Configuration.class);
                Resources newResources = (Resources) constructor.newInstance(assetManager, dm, configuration);
                return newResources;
            } catch (NoSuchMethodException e) {
            } catch (IllegalAccessException e) {
            } catch (InstantiationException e) {
            } catch (InvocationTargetException e) {
            }
            return null;
        }
    }
}
