package com.ss.android.apker.download;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ss.android.apker.Apker;
import com.ss.android.apker.api.ApkerFlow;
import com.ss.android.apker.api.FlowCallback;
import com.ss.android.apker.database.PluginDAO;
import com.ss.android.apker.entity.ApkBean;
import com.ss.android.apker.entity.ApkEntity;
import com.ss.android.apker.helper.JLog;
import com.ss.android.apker.helper.JavaHelper;
import com.ss.android.apker.volley.VolleyError;

import java.util.Map;

public class DownLoadService extends Service implements FlowCallback<ApkBean> {
    public static final String TAG = DownLoadService.class.getName();

    private ApkerFlow mFlow;
    private Map<String, ApkEntity> mPluginsMap;

    @Override
    public void onCreate() {
        super.onCreate();
        DownLoadManager.ins().setDownLoadListener(mDownLoadListener);
        DownLoadManager.ins().restartTasks(this);

        PluginDAO pluginDAO = new PluginDAO(this);
        mFlow = new ApkerFlow(this);

        mPluginsMap = pluginDAO.getPluginsMap();
        mFlow.requestPlugins(this);
    }

    @Override
    public void onFlowSuccess(ApkBean data) {
        if (mPluginsMap == null || data == null || JavaHelper.isListEmpty(data.apks)) {
            return;
        }

        for (ApkEntity entity : data.apks) {
            ApkEntity originEntity = mPluginsMap.get(entity.packageName);
            if (originEntity != null && originEntity.version >= entity.version) {
                continue;
            }
            DownLoadManager.ins().addTask(this, entity);
        }
    }

    @Override
    public void onFlowError(VolleyError error) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFlow.onDestroy();
        DownLoadManager.ins().stopAllTask();
        DownLoadManager.ins().onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private DownLoadListener mDownLoadListener = new DownLoadListener() {
        @Override
        public void onStart(String taskID) {
            JLog.i(DownLoadManager.TAG, "onStart taskID=" + taskID);
        }

        @Override
        public void onProgress(ApkEntity downLoadInfo) {
            JLog.i(DownLoadManager.TAG, "onProgress getTaskID=" + downLoadInfo.taskID
                    + ",getDownloadSize=" + downLoadInfo.downloadSize
                    + ",getFilePath=" + downLoadInfo.filePath);
        }

        @Override
        public void onStop(String taskID) {
            JLog.i(DownLoadManager.TAG, "onStop taskID=" + taskID);
        }

        @Override
        public void onError(ApkEntity downLoadInfo) {
            JLog.i(DownLoadManager.TAG, "onError taskID=" + downLoadInfo.taskID);
        }

        @Override
        public void onSuccess(ApkEntity downLoadInfo) {
            JLog.i(DownLoadManager.TAG, "onSuccess taskID=" + downLoadInfo.taskID);
            if (downLoadInfo != null) {
                Apker.ins().installPlugin(downLoadInfo);
            }
        }
    };
}
