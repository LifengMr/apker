package com.ss.android.apker.api;

import android.content.Context;

import com.ss.android.apker.entity.BaseBean;
import com.ss.android.apker.volley.AuthFailureError;
import com.ss.android.apker.volley.VolleyRequest;

import java.util.Map;

/**
 * apker请求类
 * @author chenlifeng1
 * @param <T>
 */
public class ApkerRequest<T extends BaseBean> extends VolleyRequest<T> {

	public ApkerRequest(Context context) {
		super(context);
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		return super.getHeaders();
	}
	
	@Override
	public String getUrl() {
		return super.getUrl();
	}
	
	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return mPostParams;
	}
}
