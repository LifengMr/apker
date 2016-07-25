
package com.ss.android.apker.jar.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ss.android.apker.entity.ApkResources;
import com.ss.android.apker.jar.application.JarApplication;
import com.ss.android.apker.jar.loader.JarLoader;
import com.ss.android.apker.helper.JLog;

import java.lang.reflect.Field;

public class ProxyFragment extends JarBaseProxyFragment {
    private static final String TAG = "ProxyFragment";
    private String mClass; // 类的名称 不带包名
    private String mJarPath;
    private String mPackageName;
    protected FragmentPlugin mRemoteFragment; // 插件Fragment实例

    protected void createRemoteFragment(Context context) {
        mPackageName = getArguments().getString(FragmentPlugin.EXTRA_PACKAGENAME);
        mClass = getArguments().getString(FragmentPlugin.EXTRA_CLASS);
        mJarPath = getArguments().getString(FragmentPlugin.EXTRA_JARNAME);
        Class<?> localClass = JarLoader.loadClass(context, mJarPath, mPackageName,
                mClass);
        if (null != localClass) {
            Object instance = JarLoader.newInstance(localClass, new Class[]{}, new Object[]{});
            mRemoteFragment = (FragmentPlugin) instance;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        attachTargetFragment();
        super.onAttach(activity);
        Field mActivityField = null;
        try {
            mActivityField = Fragment.class.getDeclaredField("mActivity");
            Log.d(TAG, "onAttach mActivityField=" + mActivityField);
            mActivityField.setAccessible(true);
            mActivityField.set(mRemoteFragment, activity);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onAttach mActivityField e=" + e);
        }

        if (null == mActivityField) {
            try {
                Field mHostField = Fragment.class.getDeclaredField("mHost");
                mHostField.setAccessible(true);
                Object host = mHostField.get(this);
                mHostField.set(mRemoteFragment, host);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "onAttach mHostField e=" + e);
            }
        }
        updateConfiguration();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void attachTargetFragment() {
        try {
            mRemoteFragment.onAttach(getActivity());
            mRemoteFragment.setProxy(this, mJarPath, mPackageName);

        } catch (Exception e) {
            JLog.i(TAG, "!!!ProxyFragment e is " + e.getMessage());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        if (mRemoteFragment != null) {
            mRemoteFragment.onCreate(savedInstanceState);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRemoteFragment != null) {
            return mRemoteFragment.onCreateView(inflater, container, savedInstanceState);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (mRemoteFragment != null) {
            mRemoteFragment.onViewCreated(view, savedInstanceState);
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (mRemoteFragment != null) {
            mRemoteFragment.onActivityCreated(savedInstanceState);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        if (mRemoteFragment != null) {
            mRemoteFragment.onViewStateRestored(savedInstanceState);
        }
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onStart() {
        if (mRemoteFragment != null) {
            mRemoteFragment.onStart();
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        if (mRemoteFragment != null) {
            mRemoteFragment.onResume();
        }
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mRemoteFragment != null) {
            mRemoteFragment.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    private void updateConfiguration() {
        Configuration configuration;
        DisplayMetrics displayMetrics;
        if (null != JarApplication.getInstance() && null != JarApplication.getInstance()
                .getResources()) {
            Resources superRes = JarApplication.getInstance().getResources();
            configuration = superRes.getConfiguration();
            displayMetrics = superRes.getDisplayMetrics();
            ApkResources jarResources = getOverrideResources();
            if (null != jarResources) {
                Resources resources = jarResources.getResources();
                if (null != resources && null != configuration && null != displayMetrics) {
                    resources.updateConfiguration(configuration, displayMetrics);
                }
            }
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        updateConfiguration();
        if (mRemoteFragment != null) {
            mRemoteFragment.onConfigurationChanged(newConfig);
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPause() {
        if (mRemoteFragment != null) {
            mRemoteFragment.onPause();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mRemoteFragment != null) {
            mRemoteFragment.onStop();
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (mRemoteFragment != null) {
            mRemoteFragment.onDestroyView();
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mRemoteFragment != null) {
            mRemoteFragment.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if (mRemoteFragment != null) {
            mRemoteFragment.onDetach();
        }
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mRemoteFragment != null) {
            mRemoteFragment.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
