package com.ss.android.apker.hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.ss.android.apker.compat.InstrumentationCompat;
import com.ss.android.apker.helper.IntentBuild;
import com.ss.android.apker.helper.JLog;

public class ApkInstrumentation extends Instrumentation {
    public static final String TAG = ApkInstrumentation.class.getName();

    private Instrumentation mInstrumentation;

    public ApkInstrumentation(Instrumentation instrumentation) {
        mInstrumentation = instrumentation;
    }

    /**
     * api 21...
     * @param who
     * @param contextThread
     * @param token
     * @param target
     * @param intent
     * @param requestCode
     * @param options
     * @return
     */
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        IntentBuild.ins().wrapIntent(intent);
        return InstrumentationCompat.execStartActivity(mInstrumentation,
                who, contextThread, token, target, intent, requestCode, options);
    }

    /**
     * api ...20
     * @param who
     * @param contextThread
     * @param token
     * @param target
     * @param intent
     * @param requestCode
     * @return
     */
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode) {
        IntentBuild.ins().wrapIntent(intent);
        return InstrumentationCompat.execStartActivity(mInstrumentation,
                who, contextThread, token, target, intent, requestCode);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        JLog.i(TAG, "callActivityOnCreate activity.getClass().getName()=" + activity.getClass().getName());

        if (mInstrumentation != null) {
            mInstrumentation.callActivityOnCreate(activity, icicle);
        } else {
            super.callActivityOnCreate(activity, icicle);
        }
    }

    @Override
    public void callActivityOnDestroy(Activity activity) {
        JLog.i(TAG, "callActivityOnDestroy activity.getClass().getName()=" + activity.getClass().getName());

        if (mInstrumentation != null) {
            mInstrumentation.callActivityOnDestroy(activity);
        } else {
            super.callActivityOnDestroy(activity);
        }
    }
}
