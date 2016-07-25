/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ss.android.apker.volley.toolbox;

import java.io.UnsupportedEncodingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.ss.android.apker.volley.NetworkResponse;
import com.ss.android.apker.volley.ParseError;
import com.ss.android.apker.volley.Request.Method;
import com.ss.android.apker.volley.Response;
import com.ss.android.apker.volley.Response.ErrorListener;
import com.ss.android.apker.volley.Response.Listener;

/**
 * A request for retrieving a {@link JSONArray} response body at a given URL.
 */
public class JsonArrayRequest extends JsonRequest<JSONArray> {

    /**
     * Creates a new request.
     * @param method the HTTP method to use
     * @param url URL to fetch the JSON from
     * @param requestBody A {@link String} to post with the request. Null is allowed and
     *   indicates no parameters will be posted along with request.
     * @param listener Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public JsonArrayRequest(Context context, int method, String url, String requestBody,
                            Listener<JSONArray> listener, ErrorListener errorListener) {
        super(context, method, url, requestBody, listener,
                errorListener);
    }

    /**
     * Creates a new request.
     * @param url URL to fetch the JSON from
     * @param listener Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public JsonArrayRequest(Context context, String url, Listener<JSONArray> listener, ErrorListener errorListener) {
        super(context, Method.GET, url, null, listener, errorListener);
    }

    /**
     * Creates a new request.
     * @param method the HTTP method to use
     * @param url URL to fetch the JSON from
     * @param listener Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public JsonArrayRequest(Context context, int method, String url, Listener<JSONArray> listener, ErrorListener errorListener) {
        super(context, method, url, null, listener, errorListener);
    }

    /**
     * Creates a new request.
     * @param method the HTTP method to use
     * @param url URL to fetch the JSON from
     * @param jsonRequest A {@link JSONArray} to post with the request. Null is allowed and
     *   indicates no parameters will be posted along with request.
     * @param listener Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public JsonArrayRequest(Context context, int method, String url, JSONArray jsonRequest, 
            Listener<JSONArray> listener, ErrorListener errorListener) {
        super(context, method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener,
                errorListener);
    }

    /**
     * Creates a new request.
     * @param method the HTTP method to use
     * @param url URL to fetch the JSON from
     * @param jsonRequest A {@link JSONObject} to post with the request. Null is allowed and
     *   indicates no parameters will be posted along with request.
     * @param listener Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public JsonArrayRequest(Context context, int method, String url, JSONObject jsonRequest,
                            Listener<JSONArray> listener, ErrorListener errorListener) {
        super(context, method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener,
                errorListener);
    }

    /**
     * Constructor which defaults to <code>GET</code> if <code>jsonRequest</code> is
     * <code>null</code>, <code>POST</code> otherwise.
     *
     * @see #JsonArrayRequest(int, String, JSONArray, Listener, ErrorListener)
     */
    public JsonArrayRequest(Context context, String url, JSONArray jsonRequest, Listener<JSONArray> listener,
                            ErrorListener errorListener) {
        this(context, jsonRequest == null ? Method.GET : Method.POST, url, jsonRequest,
                listener, errorListener);
    }

    /**
     * Constructor which defaults to <code>GET</code> if <code>jsonRequest</code> is
     * <code>null</code>, <code>POST</code> otherwise.
     *
     * @see #JsonArrayRequest(int, String, JSONObject, Listener, ErrorListener)
     */
    public JsonArrayRequest(Context context, String url, JSONObject jsonRequest, Listener<JSONArray> listener,
                             ErrorListener errorListener) {
        this(context, jsonRequest == null ? Method.GET : Method.POST, url, jsonRequest,
                listener, errorListener);
    }

    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            return Response.success(new JSONArray(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
}
