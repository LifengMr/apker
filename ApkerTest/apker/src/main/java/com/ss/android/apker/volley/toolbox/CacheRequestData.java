package com.ss.android.apker.volley.toolbox;

import com.ss.android.apker.volley.Request;
import com.ss.android.apker.volley.Response;
import com.ss.android.apker.volley.ResponseDelivery;
import com.ss.android.apker.volley.VolleyCache;
import com.ss.android.apker.volley.VolleyDbCache;
import com.ss.android.apker.volley.VolleyError;
import com.ss.android.apker.volley.VolleyNoCache;
import com.ss.android.apker.volley.toolbox.ResponseListener.NetworkResponseState;
import com.ss.android.apker.entity.BaseBean;

/**
 * 获取缓存数据
 * @author chenlifeng1
 * @param <T>
 */
public class CacheRequestData<T extends BaseBean> extends BaseRequestData<T> {
	
	public CacheRequestData(ResponseDelivery delivery) {
		super(delivery);
	}
	
	/**
	 * 获取数据开始
	 */
	public void start(Request<T> request){
		Response<T> response = fetchResponseFromCache(request);
		
		//获取数据失败
		if(!isResponseAvailabe(request, response)){
			postError(request, new VolleyError(NetworkResponseState.RESULT_ERROR));
			return;
		}
		
		postResponse(request, response);
	}
	
	/**
	 * 根据缓存类型获取数据
	 * @param request
	 * @return
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	private Response<T> fetchResponseFromCache(Request<T> request){
		if(request == null || request.getVolleyCache() == null){
			return Response.error(new VolleyError(NetworkResponseState.RESULT_ERROR));
		}
		
		VolleyCache<?> cache = request.getVolleyCache();
		if(cache instanceof VolleyNoCache){
			return Response.error(new VolleyError(NetworkResponseState.RESULT_ERROR));
		}else if(cache instanceof VolleyDbCache){
			Object object = cache.get(request);
			if(cache == null){
				return Response.error(new VolleyError(NetworkResponseState.RESULT_ERROR));
			}
			if(object instanceof BaseBean){
				return Response.success((T)object, null);
			}
		}
		
		return Response.error(new VolleyError(NetworkResponseState.RESULT_ERROR));
	}
}
