package com.ss.android.apker.parser;

import android.content.Context;
import android.text.TextUtils;

import com.ss.android.apker.entity.ApkBean;
import com.ss.android.apker.entity.ApkEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chenlifeng on 16/7/6.
 */
public class ApkParser extends BaseParser<ApkBean> {

    @Override
    public ApkBean parse(String data, Context context) {
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        try {
            JSONObject json = new JSONObject(data);
            if (json.optInt("error") != 0) {
                return null;
            }
            JSONArray array = json.getJSONArray("data");
            if (array == null || array.length() == 0) {
                return null;
            }
            ApkBean bean = new ApkBean();
            ArrayList<ApkEntity> apks = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                ApkEntity entity = new ApkEntity();
                JSONObject apkJson = array.getJSONObject(i);
                entity.name = apkJson.optString("name");
                entity.packageName = apkJson.optString("packageName");
                entity.signature = apkJson.optString("signature");
                entity.type = apkJson.optInt("type");
                entity.version = apkJson.optInt("version");
                entity.title = apkJson.optString("title");
                entity.desc = apkJson.optString("desc");
                entity.url = apkJson.optString("url");
                apks.add(entity);
            }
            bean.apks = apks;
            return bean;
        } catch (JSONException e) {
        }

        return null;
    }
}
