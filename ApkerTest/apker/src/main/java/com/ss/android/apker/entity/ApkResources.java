package com.ss.android.apker.entity;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ss.android.apker.Apker;
import com.ss.android.apker.hook.ApkClassLoader;
import com.ss.android.apker.jar.loader.JarResOverrideInterface;

import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

public class ApkResources {
	Resources res;
	AssetManager asset;

	ApkResources(Resources res, AssetManager asset) {
		this.res = res;
		this.asset = asset;
	}

	/**
	 * Resources.getDrawable(id)
	 */
	public Drawable getDrawable(int id) {
		return res.getDrawable(id);
	}

	/**
	 * Resources.getText(id)
	 */
	public CharSequence getText(int id) {
		return res.getText(id);
	}

	/**
	 * Resources.getString(id)
	 */
	public String getString(int id) {
		return res.getString(id);
	}

	/**
	 * Resources.getStringArray(id)
	 */
	public String[] getStringArray(int id) {
		return res.getStringArray(id);
	}

	/**
	 * Resources.getColor(id)
	 */
	public int getColor(int id) {
		return res.getColor(id);
	}

	/**
	 * Resources.getColorStateList(id)
	 */
	public ColorStateList getColorStateList(int id) {
		return res.getColorStateList(id);
	}

	/**
	 * Resources.getDimension(id)
	 */
	public float getDimension(int id) {
		return res.getDimension(id);
	}

	/**
	 * Resources.getDimensionPixelSize(id)
	 */
	public int getDimensionPixelSize(int id) {
		return res.getDimensionPixelSize(id);
	}

	/**
	 * Resources.getDimensionPixelOffset(id)
	 */
	public int getDimensionPixelOffset(int id) {
		return res.getDimensionPixelOffset(id);
	}

	/**
	 * Resources.openRawResource(id)
	 */
	public InputStream openRawResource(int id) {
		return res.openRawResource(id);
	}

	public byte[] getRawResource(int id) {
		InputStream ins = openRawResource(id);
		try {
			int n = ins.available();
			ByteArrayOutputStream bos = new ByteArrayOutputStream(n > 0 ? n
					: 4096);
			byte[] buf = new byte[4096];
			int l;
			while ((l = ins.read(buf)) != -1) {
				bos.write(buf, 0, l);
			}
			ins.close();
			return bos.toByteArray();
		} catch (Exception e) {
			return new byte[0];
		}
	}

	/**
	 * 返回独立的Resources
	 * <p>
	 * 对Resources进行操作时不会处理依赖关系，所有依赖包的内容均不会出现在该Resources中。
	 * 
	 * @return
	 */
	public Resources getResources() {
		return res;
	}

	/**
	 * 返回独立的AssetManager
	 * <p>
	 * 对AssetManager进行操作时不会处理依赖关系，所有依赖包的内容均不会出现在该AssetManager中。
	 * 
	 * @return
	 */
	public AssetManager getAssets() {
		return asset;
	}

	/**
	 * 同LayoutInflater.inflate(id, parent, attachToRoot)
	 * <p>
	 * 不会处理依赖关系，请确保id对应的layout在当前包内
	 * 
	 * @return
	 * @throws Resources.NotFoundException
	 */
	public View inflate(Context context, int id, ViewGroup parent,
			boolean attachToRoot) {
		if (!(context instanceof JarResOverrideInterface)) {
			throw new RuntimeException(
					"unable to inflate without MainActivity context");
		}
		JarResOverrideInterface mri = (JarResOverrideInterface) context;
		ApkResources old = mri.getOverrideResources();
		mri.setOverrideResources(this);
		try {
			View v = LayoutInflater.from(context).inflate(id, parent,
					attachToRoot);
			return v;
		} finally {
			mri.setOverrideResources(old);
		}
	}

	static final HashMap<String, ApkResources> loaders = new HashMap<String, ApkResources>();

	/**
	 * 从当前类所在的包载入MyResource
	 * @param clazz
	 * @return
	 * @throws RuntimeException
	 * 如果当前类不是动态加载包载入的
	 */
	public static ApkResources getResource(Class<?> clazz, Context context) {
		Log.i("aa", "clazz.getClassLoader() is "+clazz.getClassLoader());
		if (!(clazz.getClassLoader() instanceof ApkClassLoader)) {
			throw new RuntimeException(clazz
					+ " is not loaded from dynamic loader");
		}
		Log.i("aa", "getResource is 1");
		return getResourceByCl((ApkClassLoader) clazz.getClassLoader(), context);
	}

	public static ApkResources getResourceByCl(ApkClassLoader mcl, Context context) {
		Log.i("aa", "getResource is 2");
		String pgname = mcl.mPackagename;
		Log.i("aa", "pgname is "+pgname);
		ApkResources rl = loaders.get(pgname);
		if (rl != null)
			return rl;

		String path = mcl.mJarpath;
		Log.i("aa", "path is "+path);
		try {
			AssetManager am = AssetManager.class.newInstance();
			am.getClass().getMethod("addAssetPath", String.class)
					.invoke(am, path);
			Log.i("aa", "path is 1");

			Resources superRes = Apker.ins().host().getResources();
			Log.i("aa", "path is 2");
			Resources res = new Resources(am, superRes.getDisplayMetrics(),
					superRes.getConfiguration());
			Log.i("aa", "path is 3");
			if(TextUtils.isEmpty(pgname)){
				// parse packageName from AndroidManifest.xml
				XmlResourceParser xml = am
						.openXmlResourceParser("AndroidManifest.xml");
				int eventType = xml.getEventType();
				xmlloop: while (eventType != XmlPullParser.END_DOCUMENT) {
					switch (eventType) {
					case XmlPullParser.START_TAG:
						if ("manifest".equals(xml.getName())) {
							pgname = xml.getAttributeValue(null, "package");
							break xmlloop;
						}
					}
					eventType = xml.nextToken();
				}
				xml.close();
				if (pgname == null) {
					throw new RuntimeException(
							"package not found in AndroidManifest.xml [" + path
									+ "]");
				}
			}

			rl = new ApkResources(res, am);
		} catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			throw new RuntimeException(e);
		}

		loaders.put(pgname, rl);
		return rl;
	}
}
