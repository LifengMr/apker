package com.ss.android.apker.parser;

import android.content.Context;

import com.ss.android.apker.entity.BaseBean;

/**
 * 解析基础类
 * @author chenlifeng1
 */
public abstract class BaseParser<T extends BaseBean> {

	/**
	 * @param context
	 * @return
	 */
	public abstract T parse(String data, Context context);
}
