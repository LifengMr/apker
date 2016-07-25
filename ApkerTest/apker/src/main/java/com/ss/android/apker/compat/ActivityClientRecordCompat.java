package com.ss.android.apker.compat;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;

import com.ss.android.apker.helper.ReflectHelper;

import java.lang.reflect.Field;

/**
 * Created by chenlifeng on 16/5/24.
 */
public class ActivityClientRecordCompat {

    public static Class<?> clazz() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                return Class.forName("android.app.ActivityThread$ActivityClientRecord");
            } else {
                return Class.forName("android.app.ActivityThread$ActivityRecord");
            }
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    public static Intent getIntent(Object r) {
        if (r == null) {
            return null;
        }

        Field field = ReflectHelper.getField(r.getClass(), "intent", false);
        Intent intent = ReflectHelper.readField(field, r);
        return intent;
    }

    public static void setActivityInfo(Object r, ActivityInfo targetInfo) {
        if (r == null) {
            return;
        }

        Field field = ReflectHelper.getField(r.getClass(), "activityInfo", false);
        ReflectHelper.writeField(field, r, targetInfo);
    }
}
