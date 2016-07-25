package com.ss.android.apker.api;

import com.ss.android.apker.entity.BaseBean;
import com.ss.android.apker.volley.VolleyError;

/**
 * 请求回调
 * @author chenlifeng1
 * @param <T>
 */
public interface FlowCallback<T extends BaseBean> {
	
	/**
	 * 请求成功
	 */
	public void onFlowSuccess(T data);
	
	/**
	 * 登陆失败
	 */
	public void onFlowError(VolleyError error);
	
}
