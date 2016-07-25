package com.ss.android.apker.jar.activity;

import android.app.ActivityGroup;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;

import com.ss.android.apker.entity.ApkResources;
import com.ss.android.apker.hook.ApkClassLoader;
import com.ss.android.apker.jar.loader.JarLoader;
import com.ss.android.apker.jar.loader.JarResOverrideInterface;

public class JarMainBActivityGroup extends ActivityGroup implements JarResOverrideInterface {
	private static final String TAG = "JarMainBActivityGroup";
	private ApkResources myResources;
	private AssetManager assetManager;
	private Resources resources;
	private Theme theme;
	
	@Override
	public AssetManager getAssets() {
		return assetManager == null ? super.getAssets() : assetManager;
	}

	@Override
	public Resources getResources() {
		return resources == null ? super.getResources() : resources;
	}

	@Override
	public Theme getTheme() {
		return theme == null ? super.getTheme() : theme;
	}

	public ApkResources getOverrideResources() {
		return myResources;
	}

	public void setOverrideResources(ApkResources myres) {
		if (myres == null) {
			this.myResources = null;
			this.resources = null;
			this.assetManager = null;
			this.theme = null;
		} else {
			this.myResources = myres;
			this.resources = myres.getResources();
			this.assetManager = myres.getAssets();
			Theme t = myres.getResources().newTheme();
			t.setTo(getTheme());
			this.theme = t;
		}
	}

	@Override
	public void setResourcePath(boolean isPlugin, String jarname,
			String jar_packagename) {
		if(isPlugin){
			ApkClassLoader jcl = JarLoader.getJarClassLoader(this, jarname, jar_packagename);
			ApkResources jres = ApkResources.getResourceByCl(jcl, this);
			setOverrideResources(jres);
		}else{
			setOverrideResources(null);
		}
	}
}
