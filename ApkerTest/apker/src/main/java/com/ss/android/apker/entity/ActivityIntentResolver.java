package com.ss.android.apker.entity;

import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.support.v4.util.ArrayMap;

import com.ss.android.apker.parser.ApkPackageParser;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenlifeng on 16/5/23.
 */
public class ActivityIntentResolver {

    private static ActivityIntentResolver sResolver;
    private final ArrayMap<String, ActivityInfo> mActivityInfos = new ArrayMap<>();
    private final ArrayMap<String, List<IntentFilter>> mFilters = new ArrayMap<>();

    private ActivityIntentResolver() {
    }

    public static ActivityIntentResolver ins() {
        if (sResolver == null) {
            synchronized (ActivityIntentResolver.class) {
                if (sResolver == null) {
                    sResolver = new ActivityIntentResolver();
                }
            }
        }
        return sResolver;
    }

    public void register(LoadedApk loadedApk, ApkPackageParser parser) {
        if (loadedApk == null || parser == null) {
            return;
        }
        if (loadedApk.getPackageInfo() == null) {
            loadedApk.fail();
            return;
        }
        ActivityInfo[] activityInfos = loadedApk.getPackageInfo().activities;

        if (activityInfos == null) {
            loadedApk.fail();
            return;
        }
        for (ActivityInfo activityInfo : activityInfos) {
            mActivityInfos.put(activityInfo.name, activityInfo);
        }

        ConcurrentHashMap<String, List<IntentFilter>> filters = parser.getIntentFilters();
        if (filters != null) {
            mFilters.putAll(filters);
        }
    }

    public ActivityInfo fetchActivityInfo(String className) {
        return mActivityInfos.get(className);
    }

    public ArrayMap<String, List<IntentFilter>> getFilters() {
        return mFilters;
    }
}
