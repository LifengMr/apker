package com.ss.android.apker.hook;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;

import com.ss.android.apker.compat.ActivityClientRecordCompat;
import com.ss.android.apker.entity.ActivityIntentResolver;
import com.ss.android.apker.helper.IntentBuild;
import com.ss.android.apker.helper.JLog;

/**
 * Created by chenlifeng on 16/5/24.
 */
public class HCallBack implements Handler.Callback {
    public static final String TAG = HCallBack.class.getName();

    private static final int LAUNCH_ACTIVITY = 100;

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what != LAUNCH_ACTIVITY) return false;

        Object r = msg.obj;
        JLog.i(TAG, "handleMessage...msg.obj=" + msg.obj);
        Intent intent = ActivityClientRecordCompat.getIntent(r);
        JLog.i(TAG, "handleMessage...intent=" + intent);
        String targetClass = IntentBuild.ins().unwrapIntent(intent);
        JLog.i(TAG, "handleMessage...targetClass=" + targetClass);
        if (targetClass == null) return false;

        // Replace with the REAL activityInfo
        ActivityInfo targetInfo = ActivityIntentResolver.ins().fetchActivityInfo(targetClass);
        JLog.i(TAG, "handleMessage...targetInfo=" + targetInfo);
        ActivityClientRecordCompat.setActivityInfo(r, targetInfo);
        return false;
    }
}
