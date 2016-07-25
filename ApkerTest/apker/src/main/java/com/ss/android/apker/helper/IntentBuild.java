package com.ss.android.apker.helper;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;

import com.ss.android.apker.Apker;
import com.ss.android.apker.Constant;
import com.ss.android.apker.entity.ActivityIntentResolver;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenlifeng on 16/6/30.
 */
public class IntentBuild {
    public static final String TAG = IntentBuild.class.getName();
    private static final String STUB_ACTIVITY_PREFIX = Constant.APKER_PACKAGE_NAME + ".X";
    private static final String STUB_ACTIVITY_TRANSLUCENT = STUB_ACTIVITY_PREFIX + '1';
    private static final int STUB_ACTIVITIES_COUNT = 4;
    private static IntentBuild sIntentBuild;
    private String[] mStubQueue;

    private IntentBuild() {}

    public static IntentBuild ins() {
        if (sIntentBuild == null) {
            synchronized (IntentBuild.class) {
                if (sIntentBuild == null) {
                    sIntentBuild = new IntentBuild();
                }
            }
        }
        return sIntentBuild;
    }

    public void wrapIntent(Intent intent) {
        ComponentName component = intent.getComponent();
        JLog.i(TAG, "wrapIntent..component=" + component);
        String realClazz;
        if (component == null) {
            component = intent.resolveActivity(Apker.ins().host().getPackageManager());
            JLog.i(TAG, "wrapIntent.host.component=" + component);
            if (component != null) return;

            realClazz = resolveActivity(intent);
            JLog.i(TAG, "wrapIntent.realClazz=" + realClazz);
            if (realClazz == null) return;
        } else {
            realClazz = component.getClassName();
            JLog.i(TAG, "wrapIntent.11realClazz=" + realClazz);
        }

        ActivityInfo ai = ActivityIntentResolver.ins().fetchActivityInfo(realClazz);
        JLog.i(TAG, "wrapIntent.ai=" + ai);
        if (ai == null) return;

        int flag = intent.getFlags();
        if ((flag & Intent.FLAG_ACTIVITY_SINGLE_TOP) != 0 && ai.launchMode != ActivityInfo.LAUNCH_SINGLE_TASK) {
            ai.launchMode |= ActivityInfo.LAUNCH_SINGLE_TOP;
        }

        // Carry the real plugin class
        intent.addCategory(Constant.REDIRECT_FLAG + realClazz);
        String stubClazz = dequeueStubActivity(ai, realClazz);
        JLog.i(TAG, "wrapIntent.stubClazz=" + stubClazz);
        intent.setComponent(new ComponentName(Apker.ins().host(), stubClazz));
    }

    public String unwrapIntent(Intent intent) {
        if (intent == null) {
            return null;
        }
        Set<String> categories = intent.getCategories();
        if (categories == null) return null;

        // Get plugin activity class name from categories
        Iterator<String> it = categories.iterator();
        while (it.hasNext()) {
            String category = it.next();
            if (category.charAt(0) == Constant.REDIRECT_FLAG) {
                return category.substring(1);
            }
        }
        return null;
    }

    private String resolveActivity(Intent intent) {;
        Iterator<Map.Entry<String, List<IntentFilter>>> iterator
                = ActivityIntentResolver.ins().getFilters().entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, List<IntentFilter>> entry = iterator.next();
            List<IntentFilter> filters = entry.getValue();
            for (IntentFilter filter : filters) {
                if (filter.hasAction(Intent.ACTION_VIEW)) {
                    // TODO
                }
                if (filter.hasCategory(Intent.CATEGORY_DEFAULT)) {
                    if (filter.hasAction(intent.getAction())) {
                        return entry.getKey();
                    }
                }
            }
        }
        return null;
    }

    private String dequeueStubActivity(ActivityInfo ai, String realActivityClazz) {
        if (ai.launchMode == ActivityInfo.LAUNCH_MULTIPLE) {
            // In standard mode, the stub activity is reusable.
            // Cause the `windowIsTranslucent' attribute cannot be dynamically set,
            // We should choose the STUB activity with translucent or not here.
            Resources.Theme theme = Apker.ins().host().getResources().newTheme();
            theme.applyStyle(ai.getThemeResource(), true);
            TypedArray sa = theme.obtainStyledAttributes(
                    new int[] { android.R.attr.windowIsTranslucent });
            boolean translucent = sa.getBoolean(0, false);
            sa.recycle();
            return translucent ? STUB_ACTIVITY_TRANSLUCENT : STUB_ACTIVITY_PREFIX;
        }

        int availableId = -1;
        int stubId = -1;
        int countForMode = STUB_ACTIVITIES_COUNT;
        int countForAll = countForMode * 3; // 3=[singleTop, singleTask, singleInstance]
        if (mStubQueue == null) {
            // Lazy init
            mStubQueue = new String[countForAll];
        }
        int offset = (ai.launchMode - 1) * countForMode;
        JLog.i(TAG, "dequeueStubActivity ai.launchMode=" + ai.launchMode);
        for (int i = 0; i < countForMode; i++) {
            String usedActivityClazz = mStubQueue[i + offset];
            JLog.i(TAG, "dequeueStubActivity usedActivityClazz=" + usedActivityClazz);
            JLog.i(TAG, "dequeueStubActivity realActivityClazz=" + realActivityClazz);
            if (usedActivityClazz == null) {
                if (availableId == -1) availableId = i;
            } else if (usedActivityClazz.equals(realActivityClazz)) {
                stubId = i;
            }
        }
        if (stubId != -1) {
            availableId = stubId;
        } else if (availableId != -1) {
            mStubQueue[availableId + offset] = realActivityClazz;
        } else {
            // TODO:
            JLog.e(TAG, "Launch mode " + ai.launchMode + " is full");
        }
        return STUB_ACTIVITY_PREFIX + ai.launchMode + availableId;
    }

}
