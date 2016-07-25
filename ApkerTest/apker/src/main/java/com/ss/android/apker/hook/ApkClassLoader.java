package com.ss.android.apker.hook;

import com.ss.android.apker.helper.JLog;

import dalvik.system.DexClassLoader;

/**
 * Created by chenlifeng on 16/5/19.
 */
public class ApkClassLoader extends DexClassLoader {
    public static final String TAG = ApkClassLoader.class.getName();

    public String mPackagename;
    public String mJarpath;

    public ApkClassLoader(String packagename, String dexPath, String optimizedDirectory,
                          String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
        this.mPackagename = packagename;
        this.mJarpath = dexPath;
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        JLog.i(TAG, "loadClass className=" + className);
        Class clazz = super.loadClass(className);
        JLog.i(TAG, "loadClass clazz=" + clazz);
        return clazz;
    }
}
