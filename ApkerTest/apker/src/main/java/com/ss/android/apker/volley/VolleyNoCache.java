package com.ss.android.apker.volley;

public class VolleyNoCache implements VolleyCache<String> {

	@Override
	public String get(Request<?> request) {
		return null;
	}

	@Override
	public void add(Request<?> request) {
	}

}
