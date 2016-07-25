package com.ss.android.apker.api;

/**
 * 数据请求flow
 * @author chenlifeng
 */
public interface ApkerRequestImpl {
	
	/**
	 * 请求开始
	 */
	public void start();
	
	/**
	 * 取消请求
	 */
	public void cancel();
}
