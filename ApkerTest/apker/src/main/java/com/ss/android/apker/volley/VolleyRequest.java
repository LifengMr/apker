package com.ss.android.apker.volley;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.ss.android.apker.entity.BaseBean;
import com.ss.android.apker.helper.JLog;
import com.ss.android.apker.parser.BaseParser;
import com.ss.android.apker.volley.toolbox.HttpHeaderParser;
import com.ss.android.apker.volley.toolbox.ResponseListener;
import com.ss.android.apker.volley.toolbox.ResponseListener.NetworkResponseState;
import com.ss.android.apker.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * 网络请求类
 * @author chenlifeng1
 * @param <T>
 */
public class VolleyRequest<T extends BaseBean> extends Request<T> {
	/** JSON默认编码格式  */
    protected static final String PROTOCOL_CHARSET = "utf-8";
    /** FastJson解析类 */
	private Class<T> mClass;
	/** 自定义解析器 */
	private BaseParser<T> mParser;
	/** 网络请求回调 */
	private ResponseListener<T> mResponseListener;
	/** POST请求参数 */
	protected Map<String, String> mPostParams;
	
	public VolleyRequest(Context context){
		super(context);
	}
	
	/**
	 * 设置请求方式  post get等
	 * @param method
	 */
	public VolleyRequest<T> setMethod(int method){
		super.mMethod = method;
		return this;
	}
	
	public VolleyRequest<T> setUrl(String url){
		this.mUrl = url;
		return this;
	}
	
	/**
	 * Jsonfast解析
	 * @param clazz
	 */
	public VolleyRequest<T> setClass(Class<T> clazz){
		this.mClass = clazz;
		return this;
	}
	
	/**
	 * 自定义解析器
	 * @param parser
	 * @return
	 */
	public VolleyRequest<T> setParser(BaseParser<T> parser){
		this.mParser = parser;
		return this;
	}
	
	/**
	 * 请求标签
	 */
	public VolleyRequest<T> setTag(String tag) {
        mTag = tag;
        return this;
    }
	
	/**
	 * 设置请求方式
	 * @param type
	 * @return
	 */
	public VolleyRequest<T> setRequestType(RequestManner type){
		mRequestType = type;
		return this;
	}
	
	/**
	 * 设置缓存方式
	 * @param volleyCache
	 * @return
	 */
	public VolleyRequest<T> setVolleyCache(VolleyCache<?> volleyCache){
		mVolleyCache = volleyCache;
		return this;
	}
	
	/**
	 * 设置Post请求参数
	 * 参数为多维数组时，可以先拼参数再传入
	 * @param postParams
	 */
	public VolleyRequest<T> setPostParams(Map<String, String> postParams){
		this.mPostParams = postParams;
		return this;
	}
	
	/**
	 * 设置请求结果回调
	 * @param callback
	 * @return
	 */
	public VolleyRequest<T> setCallback(ResponseListener<T> callback){
		this.mResponseListener = callback;
		return this;
	} 
	
	/**
	 * 加入线程池，并开始启动
	 * @return
	 */
	public VolleyRequest<T> add(){
		JLog.log("http", "url = " + mUrl);
		Volley.getQueue(mContext).add(this);
		return this;
	}
	
	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse response) {
		try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            if(mClass != null){
            	//FastJson解析
            	return Response.success(JSON.parseObject(jsonString, mClass),
            			HttpHeaderParser.parseCacheHeaders(response));
            }else if(mParser != null){
            	//自定义解析
            	return Response.success(mParser.parse(jsonString, mContext),
            			HttpHeaderParser.parseCacheHeaders(response));
            }
            
            return Response.error(new VolleyError(NetworkResponseState.RESULT_ERROR));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
	}

	@Override
	protected void deliverResponse(T response) {
		if(mResponseListener != null){
			mResponseListener.onSuccess(response);
		}
	}
	
	@Override
	public void deliverError(VolleyError error) {
		if(mResponseListener != null){
			mResponseListener.onError(error);
		}
	}
}
