package com.ss.android.apker.api;

import android.content.Context;
import android.text.TextUtils;

import com.ss.android.apker.entity.ApkBean;
import com.ss.android.apker.parser.ApkParser;
import com.ss.android.apker.volley.Request;
import com.ss.android.apker.volley.RequestQueue;
import com.ss.android.apker.volley.VolleyError;
import com.ss.android.apker.volley.toolbox.ResponseListener;
import com.ss.android.apker.volley.toolbox.Volley;

/**
 * Created by chenlifeng on 16/7/6.
 */
public class ApkerFlow {
    private static final String FLOW_APKER_TAG = "flow_apker_";
    private static final String REQUEST_PLUGINS = FLOW_APKER_TAG + "request_plugins";

    private Context mContext;
    private RequesPlugins mRequesPlugins;

    public ApkerFlow(Context context) {
        mContext = context;
    }

    public void requestPlugins(FlowCallback<ApkBean> callback) {
        if (mRequesPlugins != null) {
            mRequesPlugins.cancel();
            mRequesPlugins = null;
        }
        mRequesPlugins = new RequesPlugins(callback);
        mRequesPlugins.start();
    }

    public void onDestroy() {
        Volley.getQueue(mContext).cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return request != null && !TextUtils.isEmpty(request.getTag())
                        && request.getTag().startsWith(FLOW_APKER_TAG);
            }
        });
    }

    private class RequesPlugins implements ApkerRequestImpl {
        private FlowCallback<ApkBean> callback;

        public RequesPlugins(FlowCallback<ApkBean> callback) {
            this.callback = callback;
        }

        @Override
        public void start() {
            String url = ApkerApi.getPluginsUrl();
            new ApkerRequest<ApkBean>(mContext).setUrl(url).setParser(new ApkParser())
                    .setTag(REQUEST_PLUGINS).setCallback(new ResponseListener<ApkBean>() {
                @Override
                public void onSuccess(ApkBean result) {
                    if (callback != null) {
                        callback.onFlowSuccess(result);
                    }
                }

                @Override
                public void onError(VolleyError error) {
                    if (callback != null) {
                        callback.onFlowError(error);
                    }
                }
            }).add();
        }

        @Override
        public void cancel() {
            Volley.getQueue(mContext).cancelWithTag(REQUEST_PLUGINS);
        }
    }
}
