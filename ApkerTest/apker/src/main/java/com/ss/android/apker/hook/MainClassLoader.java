package com.ss.android.apker.hook;

import android.text.TextUtils;

import com.ss.android.apker.Apker;
import com.ss.android.apker.helper.ApkHelper;
import com.ss.android.apker.helper.JLog;

/**
 * Created by chenlifeng on 16/5/20.
 */
public class MainClassLoader extends ClassLoader {
    public static final String TAG = MainClassLoader.class.getName();

    public MainClassLoader(ClassLoader parentLoader) {
        super(parentLoader);
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        JLog.i(TAG, "loadClass className=" + className);
        if (TextUtils.isEmpty(className)) {
            return super.loadClass(className);
        }

        Class<?> clazz = null;
        try {
            clazz = super.loadClass(className);
        } catch (ClassNotFoundException e) {
        }
        if (clazz == null && ApkHelper.isSystemClass(className)) {
            throw new ClassNotFoundException("system class not found");
        }
        if (clazz == null) {
            clazz = Apker.ins().loadClass(className);
        }
        JLog.i(TAG, "loadClass clazz=" + clazz);
        //TODO 添加clazz == NULL throw new ClassNotFoundException("system class not found");
        return clazz;
    }
}
