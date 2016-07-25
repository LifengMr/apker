package com.ss.android.apker.jar.loader;

import com.ss.android.apker.entity.ApkResources;

public interface JarResOverrideInterface {
	void setOverrideResources(ApkResources myres);
	ApkResources getOverrideResources();
	void setResourcePath(boolean isPlugin, String jarname, String jar_packagename);
}
