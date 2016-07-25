
package com.ss.android.apker.jar.fragment;

import android.support.v4.app.Fragment;

import com.ss.android.apker.jar.loader.JarContext;
import com.ss.android.apker.jar.loader.JarResOverrideInterface;

/**
 * 定义fragment组件的插件规范,fragment插件需要继承该类，遵行此规范.
 */
public abstract class FragmentPlugin extends Fragment {
    private static final String TAG = "FragmentPlugin";
    public static final String EXTRA_JARNAME = "extra.jarname"; // 插件名
    public static final String EXTRA_PACKAGENAME = "extra.packagename"; // 插件包名
    public static final String EXTRA_CLASS = "extra.class"; // 需要启动fragment类名（类），不要.java后缀

    /**
     * 代理fragment，可以当作Context来使用，会根据需要来决定是否指向this
     */
    protected Fragment mProxyFragment;
    protected JarContext mContext;

    protected String mJarname;
    protected String mPackagename;
    /**
     * 设置代理类
     * 
     * @param proxyFragment
     * @param
     */
    public void setProxy(Fragment proxyFragment, String jarname, String packagename) {
        mProxyFragment = proxyFragment;
        mJarname = jarname;
        mPackagename = packagename;
        setJarResource(true);

    }

    /**
     * @param isJar
     */
    public void setJarResource(boolean isJar) {
        if (isJar) {
            ((JarResOverrideInterface) mProxyFragment).setResourcePath(true, mJarname,
                    mPackagename);
        } else {
            ((JarResOverrideInterface) mProxyFragment).setResourcePath(false, null, null);
        }
    }
}
