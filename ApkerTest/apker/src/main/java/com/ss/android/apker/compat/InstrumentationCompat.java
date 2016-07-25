package com.ss.android.apker.compat;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.ss.android.apker.helper.ReflectHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by chenlifeng on 16/5/22.
 */
public class InstrumentationCompat {

    public static Instrumentation getHostInstrumentation() {
        Object activityThread = ActivityThreadCompat.currentActivityThread();
        Field field = ReflectHelper.getField(ActivityThreadCompat.clazz(), "mInstrumentation", false);
        Instrumentation instrumentation = ReflectHelper.readField(field, activityThread);
        return instrumentation;
    }

    /**
     * api 21...
     * @param instrumentation
     * @param who
     * @param contextThread
     * @param token
     * @param target
     * @param intent
     * @param requestCode
     * @param options
     * @return
     */
    public static Instrumentation.ActivityResult execStartActivity(Instrumentation instrumentation, Context who,
            IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options) {
        Class[] types = new Class[] {Context.class, IBinder.class, IBinder.class, Activity.class,
                Intent.class, int.class, Bundle.class};
        Method method = ReflectHelper.getMethod(Instrumentation.class, "execStartActivity", false, types);
        Object[] args = new Object[] {who, contextThread, token, target, intent, requestCode, options};
        return ReflectHelper.invoke(method, instrumentation, args);
    }

    /**
     * api ...20
     * @param instrumentation
     * @param who
     * @param contextThread
     * @param token
     * @param target
     * @param intent
     * @param requestCode
     * @return
     */
    public static Instrumentation.ActivityResult execStartActivity(Instrumentation instrumentation, Context who,
           IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode) {
        Class[] types = new Class[] {Context.class, IBinder.class, IBinder.class, Activity.class,
                Intent.class, int.class};
        Method method = ReflectHelper.getMethod(Instrumentation.class, "execStartActivity", false, types);
        Object[] args = new Object[]{ who, contextThread, token, target, intent, requestCode};
        return ReflectHelper.invoke(method, instrumentation, args);
    }
}
