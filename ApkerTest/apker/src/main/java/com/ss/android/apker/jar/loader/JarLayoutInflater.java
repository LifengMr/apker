package com.ss.android.apker.jar.loader;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

/**
 * LayoutInflater的构造器是protected类型，无法通过new实例化，
 * 通过自定义提高访问权限
 * @author chenlifeng1
 *
 */
public class JarLayoutInflater extends LayoutInflater {
	
	protected JarLayoutInflater(Context context) {
		super(context);
	}

	@Override
	public LayoutInflater cloneInContext(Context newContext) {
		return null;
	}

	@Override
	protected View onCreateView(String name, AttributeSet attrs)
			throws ClassNotFoundException {
		if(name.equals("Surface") || name.equals("SurfaceHolder") || name.equals("SurfaceView") || name.equals("TextureView") || name.equals("View")){
			return super.onCreateView(name, attrs);
		}else if(name.equals("WebView")){
			return createView(name, "android.webkit.", attrs);
		}else{
			return createView(name, "android.widget.", attrs);
		}
	}
}
