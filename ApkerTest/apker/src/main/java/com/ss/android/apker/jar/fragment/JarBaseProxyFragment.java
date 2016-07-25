
package com.ss.android.apker.jar.fragment;

import android.support.v4.app.Fragment;

import com.ss.android.apker.entity.ApkResources;
import com.ss.android.apker.hook.ApkClassLoader;
import com.ss.android.apker.jar.loader.JarLoader;
import com.ss.android.apker.jar.loader.JarResOverrideInterface;

/**
 * 主程序如需调用插件,则需要继承此类 主要功能:实现插件res资源的调用
 */
public class JarBaseProxyFragment extends Fragment implements JarResOverrideInterface {
    private static final String TAG = "JarBaseProxyFragment";
    private ApkResources mJarResources;

    @Override
    public ApkResources getOverrideResources() {
        return mJarResources;
    }

    @Override
    public void setOverrideResources(ApkResources myres) {
        this.mJarResources = myres;
    }

    /**
     * @param isPlugin 是否切换到插件资源
     * @param jarname 插件名称 isPlugin==false时传null
     * @param jar_packagename 插件包名 isPlugin==false时传null
     */
    @Override
    public void setResourcePath(boolean isPlugin, String jarname,
            String jar_packagename) {
        if (isPlugin) {
            ApkClassLoader jcl = JarLoader.getJarClassLoader(getActivity(), jarname,
                    jar_packagename);
            ApkResources jres = ApkResources.getResourceByCl(jcl, getActivity());
            setOverrideResources(jres);
        } else {
            setOverrideResources(null);
        }
    }
}
