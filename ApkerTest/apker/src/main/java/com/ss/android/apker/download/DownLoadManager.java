
package com.ss.android.apker.download;

import android.content.Context;

import com.ss.android.apker.database.DownloadDAO;
import com.ss.android.apker.entity.ApkEntity;
import com.ss.android.apker.helper.ApkHelper;
import com.ss.android.apker.helper.FileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DownLoadManager {

    public static final String TAG = DownLoadManager.class.getName();
    /** 最大同时下载数 **/
    private static final int MAX_DOWNLOADING_TASK = 5;

    private static final int STATUS_OK = 0;
    private static final int STATUS_FILE_EXISTS = 1;
    private static final int STATUS_TASK_EXISTS = 2;
    private static final int STATUS_FAILED = 3;

    private static DownLoadManager sManager;
    private Map<String, DownLoader> mDownLoaders = new HashMap<>();
    private Map<String, DownLoadListener> mTaskListeners= new HashMap<>();
    /** 服务器是否支持断点续传 */
    private boolean mSupportBreakpoint = true;
    private ThreadPoolExecutor mPool;
    private DownLoadListener mDownLoadListener;

    private DownLoadManager() {
        mPool = new ThreadPoolExecutor(MAX_DOWNLOADING_TASK, MAX_DOWNLOADING_TASK, 30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(2000));
    }

    public static DownLoadManager ins() {
        if (sManager == null) {
            synchronized (DownLoadManager.class) {
                if (sManager == null) {
                    sManager = new DownLoadManager();
                }
            }
        }
        return sManager;
    }

    public void restartTasks(Context context) {
        stopAllTask();
        DownloadDAO downloadDAO = new DownloadDAO(context);
        ArrayList<ApkEntity> downLoadInfos = downloadDAO.getAllDownLoadInfo();
        if (downLoadInfos.size() > 0) {
            int size = downLoadInfos.size();
            for (int i = 0; i < size; i++) {
                ApkEntity downLoadInfo = downLoadInfos.get(i);
                startTask(context, downLoadInfo);
            }
        }
    }

    public void onDestroy() {
        sManager = null;
    }

    public void setDownLoadListener(DownLoadListener listener) {
        mDownLoadListener = listener;
        Iterator<DownLoader> iterator = mDownLoaders.values().iterator();
        while (iterator.hasNext()) {
            DownLoader downLoader = iterator.next();
            downLoader.setDownLoadListener(listener);
        }
    }

    /**
     * must call removeTaskListener to avoid memory leak
     * @param taskId
     * @param listener
     */
    public void addTaskListener(String taskId, DownLoadListener listener) {
        mTaskListeners.put(taskId, listener);
        DownLoader downLoader = mDownLoaders.get(taskId);
        if (downLoader != null) {
            downLoader.setTaskListener(listener);
        }
    }

    public void removeTaskListener(String taskId) {
        mTaskListeners.remove(taskId);
        DownLoader downLoader = mDownLoaders.get(taskId);
        if (downLoader != null) {
            downLoader.setTaskListener(null);
        }
    }

    /**
     * 设置下载管理是否支持断点续传
     */
    public void setSupportBreakpoint(boolean isSupportBreakpoint) {
        if ((!this.mSupportBreakpoint) && isSupportBreakpoint) {
            Iterator<DownLoader> iterator = mDownLoaders.values().iterator();
            while (iterator.hasNext()) {
                DownLoader downLoader = iterator.next();
                downLoader.setSupportBreakpoint(true);
            }
        }
        this.mSupportBreakpoint = isSupportBreakpoint;
    }

    @Deprecated
    public int addTask(Context context, String taskID, String url, String packageName, String fileName) {
        int state = getAttachmentState(taskID, fileName);
        if (state != STATUS_OK) {
            return state;
        }

        ApkEntity downLoadInfo = new ApkEntity();
        downLoadInfo.downloadSize = 0;
        downLoadInfo.fileSize = 0;
        downLoadInfo.taskID = taskID;
        downLoadInfo.name = fileName;
        downLoadInfo.url = url;
        downLoadInfo.filePath = FileHelper.getDownloadFilePath(fileName);

        return startTask(context, downLoadInfo);
    }

    public int addTask(Context context, ApkEntity downLoadInfo) {
        if (downLoadInfo == null) {
            return STATUS_FAILED;
        }
        int state = getAttachmentState(downLoadInfo.taskID, downLoadInfo.name);
        if (state != STATUS_OK) {
            return state;
        }

        downLoadInfo.taskID = downLoadInfo.packageName;
        downLoadInfo.downloadSize = 0;
        downLoadInfo.fileSize = 0;
        downLoadInfo.filePath = FileHelper.getDownloadFilePath(downLoadInfo.name);

        return startTask(context, downLoadInfo);
    }

    private int startTask(Context context, ApkEntity downLoadInfo) {
        if (!ApkHelper.isNetworkAvailable(context)) {
            return STATUS_FAILED;
        }
        DownLoadListener taskListener = mTaskListeners.get(downLoadInfo.taskID);
        DownLoader downLoader = new DownLoader(context, downLoadInfo, mPool, mSupportBreakpoint, true);
        downLoader.setDownLoadCallback(mDownloadCallback);
        downLoader.setDownLoadListener(mDownLoadListener);
        downLoader.setTaskListener(taskListener);
        downLoader.start();
        mDownLoaders.put(downLoadInfo.taskID, downLoader);
        return STATUS_OK;
    }

    private int getAttachmentState(String TaskID, String fileName) {
        Iterator<DownLoader> iterator = mDownLoaders.values().iterator();
        while (iterator.hasNext()) {
            DownLoader downLoader = iterator.next();
            if (downLoader.getTaskID().equals(TaskID)) {
                return STATUS_TASK_EXISTS;
            }
        }
        File file = new File(FileHelper.getDownloadFilePath(fileName));
        if (file.exists()) {
            file.delete();
        }
        return STATUS_OK;
    }

    /**
     * (删除一个任务，包括已下载的本地文件)
     * 
     * @param taskID
     */
    public void deleteTask(String taskID) {
        DownLoader downLoader = mDownLoaders.get(taskID);
        if (downLoader != null) {
            downLoader.destroy();
            mDownLoaders.remove(downLoader);
        }
    }


    /**
     * 停止当前任务列表里的所有任务
     */
    public void stopAllTask() {
        Iterator<DownLoader> iterator = mDownLoaders.values().iterator();
        while (iterator.hasNext()) {
            DownLoader downLoader = iterator.next();
            downLoader.stop();
        }
        mDownLoaders.clear();
    }

    private DownLoader.DownloadCallback mDownloadCallback = new DownLoader.DownloadCallback() {
        @Override
        public void onSuccess(String taskID) {
            DownLoader downLoader = mDownLoaders.get(taskID);
            if (downLoader != null) {
                mDownLoaders.remove(downLoader);
            }
        }
    };
}
