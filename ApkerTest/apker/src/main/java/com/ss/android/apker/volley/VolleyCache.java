package com.ss.android.apker.volley;

/**
 * 缓存接口
 * @author chenlifeng1
 */
public interface VolleyCache<T> {
	
	/**
	 * 获取缓存
	 * @param request
	 * @return
	 */
	public T get(Request<?> request);
	
	/**
	 * 加入缓存
	 * @param request
	 */
	public void add(Request<?> request);
	
}
