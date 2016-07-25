package com.ss.android.apker.helper;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by chenlifeng on 16/5/22.
 */
public class ReflectHelper {
    public static final String TAG = ReflectHelper.class.getName();
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

    private static ArrayMap<String, Method> sMethodMap = new ArrayMap<>();
    private static ArrayMap<String, Field> sFieldMap = new ArrayMap<>();

    public static <T> T invoke(Method method, Object target, Object[] args) {
        if (method == null || target == null) {
            return null;
        }
        args = filterNull(args);
        T result = null;
        try {
            JLog.i(TAG, "invoke method.getName()=" + method.getName());
            result = (T) method.invoke(target, args);
        } catch (IllegalAccessException e) {
            JLog.e(TAG, "invoke..IllegalAccessException e=" + e.getMessage());
        } catch (InvocationTargetException e) {
            JLog.e(TAG, "invoke..InvocationTargetException e=" + e.getMessage());
        }
        return result;
    }

    public static boolean invokeMethod(Method method, Object target, Object[] args) {
        if (method == null || target == null) {
            return false;
        }
        args = filterNull(args);
        try {
            JLog.i(TAG, "invokeMethod method.getName()=" + method.getName());
            method.invoke(target, args);
            return true;
        } catch (IllegalAccessException e) {
            JLog.e(TAG, "IllegalAccessException..e=" + e.getMessage());
        } catch (InvocationTargetException e) {
            JLog.e(TAG, "InvocationTargetException..e=" + e.getMessage());
        }
        return false;
    }

    public static <T> T invokeStaticMethod(Class<?> clazz, String methodName, Class[] types, Object[] args) {
        if (clazz == null || TextUtils.isEmpty(methodName)) {
            return null;
        }
        types = filterNull(types);
        args = filterNull(args);
        Method method = getMethod(clazz, methodName, false, types);
        if (method == null) {
            return null;
        }
        try {
            return (T) method.invoke(null, args);
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return null;
    }

    public static Method getMethod(Class<?> clazz, String methodName, boolean force, Class[] types) {
        if (clazz == null || TextUtils.isEmpty(methodName)) {
            return null;
        }
        types = filterNull(types);
        String key = getMKey(clazz, methodName, types);
        Method method;
        synchronized (sMethodMap) {
            method = sMethodMap.get(key);
        }
        if (method == null) {
            try {
                method = clazz.getMethod(methodName, types);
            } catch (NoSuchMethodException e) {
            }
        }
        if (method == null) {
            try {
                method = clazz.getDeclaredMethod(methodName, types);
            } catch (NoSuchMethodException e) {
            }
        }
        if (method == null && force) {
            method = getMethodFromInterface(clazz, methodName, types);
        }
        if (method == null && force) {
            method = getMethodFromSuperclass(clazz, methodName, types);
        }
        if (method == null) {
            return null;
        }
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        synchronized (sMethodMap) {
            sMethodMap.put(key, method);
        }
        return method;
    }

    private static String getMKey(Class<?> clazz, String methodName, Class[] types) {
        StringBuilder sb = new StringBuilder();
        sb.append(clazz.toString()).append("#").append(methodName);
        if (!JavaHelper.isArrayEmpty(types)) {
            for (Class<?> type : types) {
                sb.append(type.toString()).append("#");
            }
        } else {
            sb.append(Void.class.toString());
        }
        return sb.toString();
    }

    private static String getFKey(Class<?> cls, String fieldName) {
        StringBuilder sb = new StringBuilder();
        sb.append(cls.toString()).append("#").append(fieldName);
        return sb.toString();
    }

    private static Method getMethodFromInterface(Class<?> clazz, String methodName, Class[] types) {
        for (; clazz != null; clazz = clazz.getSuperclass()) {
            final Class<?>[] interfaces = clazz.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                try {
                    return interfaces[i].getDeclaredMethod(methodName,
                            types);
                } catch (final NoSuchMethodException e) {
                }
                Method method = getMethodFromInterface(interfaces[i], methodName, types);
                if (method != null) {
                    return method;
                }
            }
        }
        return null;
    }

    private static Method getMethodFromSuperclass(Class<?> clazz, String methodName, Class[] types) {
        Class<?> parentClass = clazz.getSuperclass();
        while (parentClass != null) {
            if (Modifier.isPublic(parentClass.getModifiers())) {
                try {
                    return parentClass.getDeclaredMethod(methodName, types);
                } catch (final NoSuchMethodException e) {
                    return null;
                }
            }
            parentClass = parentClass.getSuperclass();
        }
        return null;
    }

    public static <T> T readField(Field field, Object target) {
        if (field == null) {
            return null;
        }
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        try {
            return (T) field.get(target);
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    public static void writeField(Field field, Object target, Object value) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
        }
    }

    public static Field getField(Class<?> clazz, String fieldName, boolean force) {
        if (clazz == null || TextUtils.isEmpty(fieldName)) {
            return null;
        }
        String key = getFKey(clazz, fieldName);
        Field field;
        synchronized (sFieldMap) {
            field = sFieldMap.get(key);
        }
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
        }
        if (field == null && force) {
            field = getFieldFromSuperclass(clazz, fieldName);
        }
        if (field == null && force) {
            field = getFieldFromInterface(clazz, fieldName);
        }
        if (field == null) {
            return null;
        }
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        synchronized (sFieldMap) {
            sFieldMap.put(key, field);
        }
        JLog.i(TAG, "getField clazz=" + clazz.getName() + ",field=" + field);
        return field;
    }

    private static Field getFieldFromSuperclass(Class<?> clazz, String fieldName) {
        for (; clazz != null; clazz = clazz.getSuperclass()) {
            try {
                final Field field = clazz.getDeclaredField(fieldName);
                if (!Modifier.isPublic(field.getModifiers())) {
                    field.setAccessible(true);
                }
                return field;
            } catch (final NoSuchFieldException ex) {
            }
        }
        return null;
    }

    private static Field getFieldFromInterface(Class<?> clazz, String fieldName) {
        for (final Class<?> clz : getInterfaces(clazz)) {
            try {
                final Field field = clz.getField(fieldName);
                return field;
            } catch (final NoSuchFieldException ex) {
            }
        }
        return null;
    }

    public static Class[] filterNull(Class[] array) {
        if (JavaHelper.isArrayEmpty(array)) {
            return EMPTY_CLASS_ARRAY;
        }
        return array;
    }

    public static Object[] filterNull(Object[] objects) {
        if (JavaHelper.isArrayEmpty(objects)) {
            return EMPTY_OBJECT_ARRAY;
        }
        return objects;
    }

    public static List<Class<?>> getInterfaces(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        final LinkedHashSet<Class<?>> interfacesSet = new LinkedHashSet<Class<?>>();
        getInterfaces(clazz, interfacesSet);
        return new ArrayList<Class<?>>(interfacesSet);
    }

    private static void getInterfaces(Class<?> clazz, final HashSet<Class<?>> interfacesFound) {
        while (clazz != null) {
            final Class<?>[] interfaces = clazz.getInterfaces();

            for (final Class<?> i : interfaces) {
                if (interfacesFound.add(i)) {
                    getInterfaces(i, interfacesFound);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
}
