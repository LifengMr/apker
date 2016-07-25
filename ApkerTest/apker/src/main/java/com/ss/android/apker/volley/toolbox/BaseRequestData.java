package com.ss.android.apker.volley.toolbox;

import com.ss.android.apker.volley.Request;
import com.ss.android.apker.volley.Response;
import com.ss.android.apker.volley.ResponseDelivery;
import com.ss.android.apker.volley.VolleyError;
import com.ss.android.apker.entity.BaseBean;

/**
 * 数据获取基类
 * @author chenlifeng1
 */
public class BaseRequestData<T extends BaseBean> {
	private final ResponseDelivery mDelivery;
	
	public BaseRequestData(ResponseDelivery delivery) {
		this.mDelivery = delivery;
	}
	
	/**
	 * 响应请求结果
	 * @param request
	 */
	void postResponse(Request<?> request, Response<T> response){
		if(request == null){
			return;
		}
		
		mDelivery.postResponse(request, response);
	}
	
	/**
	 * 响应请求错误
	 * @param request
	 * @param error
	 */
	void postError(Request<?> request, VolleyError error){
		if(request == null){
			return;
		}
		
		mDelivery.postError(request, error);
	}
	
	/**
     * 响应是否有效
     * 
     * @param request
     * @param response
     * @return
     */
    boolean isResponseAvailabe(Request<?> request, Response<T> response) {
        return (request != null && response != null && response.isSuccess());
    }
}
