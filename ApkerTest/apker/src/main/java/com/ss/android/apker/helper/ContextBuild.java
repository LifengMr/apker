package com.ss.android.apker.helper;

import android.content.Context;

import com.ss.android.apker.hook.ApkClassLoader;
import com.ss.android.apker.hook.ApkContextImp;

/**
 * Created by chenlifeng on 16/5/25.
 */
public class ContextBuild {

    public static Context build(Context baseContext, Class clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        if (!(classLoader instanceof ApkClassLoader)) {
            return baseContext;
        }
        ApkClassLoader jarClassLoader = (ApkClassLoader) classLoader;
        return new ApkContextImp(baseContext, jarClassLoader);
    }
}
