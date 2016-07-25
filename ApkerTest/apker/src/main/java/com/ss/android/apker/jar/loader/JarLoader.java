package com.ss.android.apker.jar.loader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.ss.android.apker.helper.FileHelper;
import com.ss.android.apker.hook.ApkClassLoader;
import com.ss.android.apker.jar.util.JarUtil;
import com.ss.android.apker.helper.JLog;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author chenlifeng1
 * 同一插件包不要多次new DexClassLoader进行加载，会重新编译分配内存，所有单子模式、实例均失效 **
 */
public class JarLoader {
    private static final int MAX_RELOAD = 1;
    private static int mReloadNum;
    //按包名保存DexClassLoader
    private static HashMap<String, ApkClassLoader> sDexLoaders = new HashMap<String, ApkClassLoader>();

    /**
     * 根据插件包名获取JarClassLoader
     *
     * @param context
     * @param jarname
     * @param packagename
     * @return
     */
    @SuppressLint("NewApi")
    public static ApkClassLoader getJarClassLoader(Context context, String jarname, String packagename) {
        String dexInternalPath = FileHelper.getDexPath(context, packagename, jarname);
        String optimizedDexOutputPath = FileHelper.getOptDexDir(context, packagename);
        ApkClassLoader cl;
        if (sDexLoaders.containsKey(packagename)) {
            cl = sDexLoaders.get(packagename);
        } else {
            cl = new ApkClassLoader(packagename, dexInternalPath,
                    optimizedDexOutputPath, context.getApplicationInfo().nativeLibraryDir, context.getClassLoader());
            sDexLoaders.put(packagename, cl);
        }
        return cl;
    }

    /**
     * 根据类名获取类
     *
     * @param context
     * @param jarname     插件文件名 *.jar  *.apk
     * @param packagename 插件包名
     * @param classname   插件包名后面的路径 不需要.class后缀
     * @return 返回class实例
     * @author chenlifeng1
     */
    @SuppressLint("NewApi")
    public static Class loadClass(Context context, String jarname, String packagename, String classname) {
        String dexInternalPath = FileHelper.getDexPath(context, packagename, jarname);
        String optimizedDexOutputPath = FileHelper.getOptDexDir(context, packagename);
        Class myClass = null;
        if (!JarUtil.isEmpty(dexInternalPath)) {
            try {
                ApkClassLoader cl;
                if (sDexLoaders.containsKey(packagename)) {
                    cl = sDexLoaders.get(packagename);
                } else {
                    cl = new ApkClassLoader(packagename, dexInternalPath,
                            optimizedDexOutputPath, context.getApplicationInfo().nativeLibraryDir, context.getClassLoader());
                    sDexLoaders.put(packagename, cl);
                }
                String clazzname = packagename + "." + classname;
                myClass = cl.loadClass(clazzname);
            } catch (Exception e) {
                JLog.i("clf", "!!!!!!! loadClass--" + packagename + " e is " + e.getMessage());
            }
        } else {
            if (mReloadNum < MAX_RELOAD) {
                mReloadNum++;
                JarUtil.updatePlugin(context, JarUtil.TYPE_JAR, false);
                myClass = loadClass(context, jarname, packagename, classname);
            }
        }
        return myClass;
    }

    /**
     * 根据全类名获取类
     *
     * @param context
     * @param jarname     插件文件名 *.jar  *.apk
     * @param packagename 插件包名
     * @return 返回class实例
     * @author chenlifeng1
     */
    @SuppressLint("NewApi")
    public static Class loadClassFull(Context context, String jarname, String packagename, String classpath) {
        String dexInternalPath = FileHelper.getDexPath(context, packagename, jarname);
        String optimizedDexOutputPath = FileHelper.getOptDexDir(context, packagename);
        Class myClass = null;
        if (!JarUtil.isEmpty(dexInternalPath)) {
            try {
                ApkClassLoader cl;
                if (sDexLoaders.containsKey(packagename)) {
                    cl = sDexLoaders.get(packagename);
                } else {
                    cl = new ApkClassLoader(packagename, dexInternalPath,
                            optimizedDexOutputPath, context.getApplicationInfo().nativeLibraryDir, context.getClassLoader());
                    sDexLoaders.put(packagename, cl);
                }
                myClass = cl.loadClass(classpath);
            } catch (Exception e) {
                JLog.i("clf", "!!!!!!! loadClassFull---" + packagename + " e is " + e.getMessage());
            }
        } else {
            JarUtil.updatePlugin(context, JarUtil.TYPE_JAR, false);
            myClass = loadClassFull(context, jarname, packagename, classpath);
        }
        return myClass;
    }

    /**
     * 通过构造器反射实例化
     *
     * @param clazz
     * @param constructors_class 构造器参数Class类型
     * @param constructors_args  构造器参数
     * @return
     */
    public static Object newInstance(Class clazz, Class[] constructors_class, Object[] constructors_args) {
        Object instance = null;
        try {
            Constructor constructor = clazz.getDeclaredConstructor(constructors_class);
            constructor.setAccessible(true);    //不做反射安全性检测
            instance = constructor.newInstance(constructors_args);
        } catch (NoSuchMethodException e) {
            JLog.i("clf", "NoSuchMethodException..e=" + e.getMessage());
        } catch (IllegalArgumentException e) {
            JLog.i("clf", "IllegalArgumentException..e=" + e.getMessage());
        } catch (InstantiationException e) {
            JLog.i("clf", "InstantiationException..e=" + e.getMessage());
        } catch (IllegalAccessException e) {
            JLog.i("clf", "IllegalAccessException..e=" + e.getMessage());
        } catch (InvocationTargetException e) {
            JLog.i("clf", "InvocationTargetException..e=" + e.getMessage());
        }
        //TODO
//		catch (Exception e){
//		}
        return instance;
    }

    /**
     * 通过反射调用方法
     *
     * @param clazz        类
     * @param method_name  方法名
     * @param method_class 方法参数Class类型
     * @param method_args  方法参数
     * @return
     */
    public static Object invokeMethod(Class clazz, String method_name, Class[] method_class, Object[] method_args) {
        Object result = null;
        try {
            Object obj = clazz.newInstance();
            Method method = clazz.getMethod(method_name, method_class);
            method.setAccessible(true);    //不做反射安全性检测
            result = method.invoke(obj, method_args);
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        } catch (InstantiationException e) {
        }
        return result;
    }

    /**
     * 通过反射调用方法
     *
     * @param obj       实例
     * @param method_name  方法名
     * @param method_class 方法参数Class类型
     * @param method_args  方法参数
     * @return
     */
    public static Object invokeMethodByObj(Object obj, String method_name, Class[] method_class, Object[] method_args) {
        Object result = null;
        try {
            Class clazz = obj.getClass();
            Method method = clazz.getDeclaredMethod(method_name, method_class);
            JLog.i("clf", "invokeMethodByObj..method=" + method);
            method.setAccessible(true);    //不做反射安全性检测
            result = method.invoke(obj, method_args);
        } catch (NoSuchMethodException e) {
            JLog.i("clf", "invokeMethodByObj..NoSuchMethodException e=" + e.getMessage());
        } catch (IllegalArgumentException e) {
            JLog.i("clf", "invokeMethodByObj..IllegalArgumentException e=" + e.getMessage());
        } catch (IllegalAccessException e) {
            JLog.i("clf", "invokeMethodByObj..IllegalAccessException e=" + e.getMessage());
        } catch (InvocationTargetException e) {
            JLog.i("clf", "invokeMethodByObj..InvocationTargetException e=" + Log.getStackTraceString(e));
        }
        return result;
    }

    /**
     * 反射调用静态方法
     *
     * @param clazz
     * @param method_name
     * @param method_class
     * @param method_args
     * @return
     */
    public static Object invokeStaticMethod(Class clazz, String method_name, Class[] method_class, Object[] method_args) {
        Object result = null;
        try {
            Method method = clazz.getMethod(method_name, method_class);
            method.setAccessible(true);
            result = method.invoke(clazz, method_args);
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        //TODO
//		catch (Exception e){
//		}
        return result;
    }
}
