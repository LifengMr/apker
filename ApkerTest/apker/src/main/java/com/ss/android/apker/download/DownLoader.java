package com.ss.android.apker.download;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.ss.android.apker.database.DownloadDAO;
import com.ss.android.apker.entity.ApkEntity;
import com.ss.android.apker.helper.FileHelper;
import com.ss.android.apker.helper.JLog;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ThreadPoolExecutor;

public class DownLoader {
    private int TASK_START = 0;
    private int TASK_STOP = 1;
    private int TASK_PROGESS = 2;
    private int TASK_ERROR = 3;
    private int TASK_SUCCESS = 4;

    /** 失败重新请求次数 **/
    private static final int MAX_DOWNLOAD_TIMES = 3;

    private boolean mSupportBreakpoint = false;
    private DownloadDAO mDownloadDAO;
    private DownLoadListener mDownLoadListener;
    private DownloadCallback mDownloadCallback;
    private DownLoadListener mTaskListener;
    private ApkEntity mDownLoadInfo;
    private DownLoadThread mDownLoadThread;
    private ThreadPoolExecutor mPool;
    /** 当前尝试请求的次数 **/
    private int mDownloadTimes = 0;
    /** 当前任务的状态 */
    private boolean mIsDownloading = false;

    public DownLoader(Context context, ApkEntity downLoadInfo, ThreadPoolExecutor pool,
            boolean isSupportBreakpoint, boolean isNewTask) {
        this.mSupportBreakpoint = isSupportBreakpoint;
        this.mPool = pool;
        this.mDownloadDAO = new DownloadDAO(context);
        this.mDownLoadInfo = downLoadInfo;
        // 新建任务，保存任务信息到数据库
        if (isNewTask) {
            saveDownloadInfo();
        }
    }

    public String getTaskID() {
        return mDownLoadInfo.taskID;
    }

    public void start() {
        String tmpFilePath = FileHelper.getDownloadTempFilePath(mDownLoadInfo.taskID);
        if (mDownLoadThread == null && !TextUtils.isEmpty(tmpFilePath)) {
            mDownloadTimes = 0;
            mIsDownloading = true;
            handler.sendEmptyMessage(TASK_START);
            mDownLoadThread = new DownLoadThread();
            mPool.execute(mDownLoadThread);
        }
    }

    public void stop() {
        if (mDownLoadThread != null) {
            mIsDownloading = false;
            mDownLoadThread.stopDownLoad();
            mPool.remove(mDownLoadThread);
            mDownLoadThread = null;
        }
    }

    public void setDownLoadListener(DownLoadListener listener) {
        mDownLoadListener = listener;
    }

    public void setTaskListener(DownLoadListener listener) {
        mTaskListener = listener;
    }

    public void removeDownLoadListener(String key) {
        mDownLoadListener = null;
    }

    public void setDownLoadCallback(DownloadCallback callback) {
        this.mDownloadCallback = callback;
    }

    public void destroy() {
        if (mDownLoadThread != null) {
            mDownLoadThread.stopDownLoad();
            mDownLoadThread = null;
        }
        mDownloadDAO.deleteDownLoadInfo(mDownLoadInfo.taskID);
        String tmpPath = FileHelper.getDownloadTempFilePath(mDownLoadInfo.taskID);
        if (TextUtils.isEmpty(tmpPath)) {
            return;
        }
        File downloadFile = new File(tmpPath);
        if (downloadFile.exists()) {
            downloadFile.delete();
        }
    }

    public boolean isDownLoading() {
        return mIsDownloading;
    }

    public ApkEntity getSQLDownLoadInfo() {
        return mDownLoadInfo;
    }

    public void setSupportBreakpoint(boolean isSupportBreakpoint) {
        this.mSupportBreakpoint = isSupportBreakpoint;
    }

    class DownLoadThread extends Thread {
        private boolean isdownloading;
        private URL url;
        private HttpURLConnection urlConn;
        private InputStream inputStream;
        private OutputStream out = null;
        private FileDescriptor outFd = null;
        private int progress = -1;

        public DownLoadThread() {
            isdownloading = true;
        }

        @Override
        public void run() {
            while (mDownloadTimes < MAX_DOWNLOAD_TIMES) { // 做3次请求的尝试
                try {
                    if (mDownLoadInfo.downloadSize == mDownLoadInfo.fileSize && mDownLoadInfo.fileSize > 0) {
                        mIsDownloading = false;
                        Message msg = new Message();
                        msg.what = TASK_PROGESS;
                        msg.arg1 = 100;
                        handler.sendMessage(msg);
                        mDownloadTimes = MAX_DOWNLOAD_TIMES;
                        mDownLoadThread = null;
                        return;
                    }

                    setupDestinationFile(mDownLoadInfo);

                    url = new URL(mDownLoadInfo.url);
                    urlConn = (HttpURLConnection) url.openConnection();
                    urlConn.setConnectTimeout(5000);
                    urlConn.setReadTimeout(10000);
                    if (mDownLoadInfo.fileSize < 1) {// 第一次下载，初始化
                        openConnention();
                    } else {
                        if (new File(FileHelper.getDownloadTempFilePath(mDownLoadInfo.taskID)).exists()) {
//                            localFile = new RandomAccessFile(FileHelper.getDownloadTempFilePath(mDownLoadInfo.taskID),
//                                    "rwd");
//                            localFile.seek(mDownLoadInfo.downloadSize);
                            urlConn.setRequestProperty("Range", "bytes=" + mDownLoadInfo.downloadSize + "-");
                        } else {
                            mDownLoadInfo.fileSize = 0;
                            mDownLoadInfo.downloadSize = 0;
                            saveDownloadInfo();
                            openConnention();
                        }
                    }
                    inputStream = urlConn.getInputStream();
                    out = new FileOutputStream(FileHelper.getDownloadTempFilePath(mDownLoadInfo.taskID), true);
                    outFd = ((FileOutputStream) out).getFD();
                    byte[] buffer = new byte[1024 * 4];
                    int length = -1;
                    while ((length = inputStream.read(buffer)) != -1 && isdownloading) {
                        out.write(buffer, 0, length);
                        mDownLoadInfo.downloadSize += length;
                        int nowProgress = (int) ((100 * mDownLoadInfo.downloadSize) / mDownLoadInfo.fileSize);
                        if (nowProgress > progress) {
                            progress = nowProgress;
                            handler.sendEmptyMessage(TASK_PROGESS);
                        }
                    }
                    // 下载完了
                    if (mDownLoadInfo.downloadSize == mDownLoadInfo.fileSize) {
                        boolean renameResult = renameFile();
                        if (renameResult) {
                            handler.sendEmptyMessage(TASK_SUCCESS); // 转移文件成功
                        } else {
                            new File(FileHelper.getDownloadTempFilePath(mDownLoadInfo.taskID)).delete();
                            handler.sendEmptyMessage(TASK_ERROR);// 转移文件失败
                        }
                        // 清除数据库任务
                        mDownloadDAO.deleteDownLoadInfo(mDownLoadInfo.taskID);
                        mDownLoadThread = null;
                        mIsDownloading = false;
                    }
                    mDownloadTimes = MAX_DOWNLOAD_TIMES;
                } catch (Exception e) {
                    JLog.i("clf", "DownLoadThread run mDownLoadInfo e=" + e.getMessage());
                    if (isdownloading) {
                        if (mSupportBreakpoint) {
                            mDownloadTimes++;
                            if (mDownloadTimes >= MAX_DOWNLOAD_TIMES) {
                                if (mDownLoadInfo.fileSize > 0) {
                                    saveDownloadInfo();
                                }
                                mPool.remove(mDownLoadThread);
                                mDownLoadThread = null;
                                mIsDownloading = false;
                                handler.sendEmptyMessage(TASK_ERROR);
                            }
                        } else {
                            mDownLoadInfo.downloadSize = 0;
                            mDownloadTimes = MAX_DOWNLOAD_TIMES;
                            mIsDownloading = false;
                            mDownLoadThread = null;
                            handler.sendEmptyMessage(TASK_ERROR);
                        }

                    } else {
                        mDownloadTimes = MAX_DOWNLOAD_TIMES;
                    }
                    e.printStackTrace();
                } finally {
                    try {
                        if (urlConn != null) {
                            urlConn.disconnect();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        if (out != null) out.flush();
                        if (outFd != null) outFd.sync();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        public void stopDownLoad() {
            isdownloading = false;
            mDownloadTimes = MAX_DOWNLOAD_TIMES;
            if (mDownLoadInfo.downloadSize > 0) {
                saveDownloadInfo();
            }
            handler.sendEmptyMessage(TASK_STOP);
        }

        private void openConnention() throws Exception {
            long urlfilesize = urlConn.getContentLength();
            if (urlfilesize > 0) {
//                localFile = new RandomAccessFile(FileHelper.getDownloadTempFilePath(mDownLoadInfo.taskID),
//                        "rwd");
//                localFile.setLength(urlfilesize);
                mDownLoadInfo.fileSize = urlfilesize;
                if (isdownloading) {
                    saveDownloadInfo();
                }
            }
        }

    }

    private void setupDestinationFile(ApkEntity downLoadInfo) {
        if (downLoadInfo == null) {
            return;
        }
        String tmpFilePath = FileHelper.getDownloadTempFilePath(mDownLoadInfo.taskID);
        if (TextUtils.isEmpty(tmpFilePath)) {
            return;
        }
        File file = new File(tmpFilePath);
        if (file.exists()) {
            long fileLength = file.length();
            if (fileLength <= 0 || downLoadInfo.fileSize <= 0) {
                file.delete();
            } else {
                downLoadInfo.downloadSize = fileLength;
            }
        } else {
            downLoadInfo.downloadSize = 0;
        }
    }

    /**
     * 保存下载信息至数据库
     */
    private void saveDownloadInfo() {
        JLog.i("clf", "saveDownloadInfo mSupportBreakpoint=" + mSupportBreakpoint);
        if (mSupportBreakpoint) {
            mDownloadDAO.saveDownLoadInfo(mDownLoadInfo);
        }
    }

    private void startNotice() {
        if (mDownLoadListener != null) {
            mDownLoadListener.onStart(getSQLDownLoadInfo().taskID);
        }
        if (mTaskListener != null) {
            mTaskListener.onStart(getSQLDownLoadInfo().taskID);
        }
    }

    private void onProgressNotice() {
        if (mDownLoadListener != null) {
            mDownLoadListener.onProgress(getSQLDownLoadInfo());
        }
        if (mTaskListener != null) {
            mTaskListener.onProgress(getSQLDownLoadInfo());
        }
    }

    private void stopNotice() {
        if (!mSupportBreakpoint) {
            mDownLoadInfo.downloadSize = 0;
        }
        if (mDownLoadListener != null) {
            mDownLoadListener.onStop(getSQLDownLoadInfo().taskID);
        }
        if (mTaskListener != null) {
            mTaskListener.onStop(getSQLDownLoadInfo().taskID);
        }
    }

    private void errorNotice() {
        if (mDownLoadListener != null) {
            mDownLoadListener.onError(getSQLDownLoadInfo());
        }
        if (mTaskListener != null) {
            mTaskListener.onStop(getSQLDownLoadInfo().taskID);
        }
    }

    private void successNotice() {
        if (mDownloadCallback != null) {
            mDownloadCallback.onSuccess(mDownLoadInfo.taskID);
        }
        if (mDownLoadListener != null) {
            mDownLoadListener.onSuccess(getSQLDownLoadInfo());
        }
        if (mTaskListener != null) {
            mTaskListener.onSuccess(getSQLDownLoadInfo());
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TASK_START) { // 开始下载
                startNotice();
            } else if (msg.what == TASK_STOP) { // 停止下载
                stopNotice();
            } else if (msg.what == TASK_PROGESS) { // 改变进程
                onProgressNotice();
            } else if (msg.what == TASK_ERROR) { // 下载出错
                errorNotice();
            } else if (msg.what == TASK_SUCCESS) { // 下载完成
                successNotice();
            }
        }
    };

    public boolean renameFile() {
        File newfile = new File(mDownLoadInfo.filePath);
        if (newfile.exists()) {
            newfile.delete();
        }
        File olefile = new File(FileHelper.getDownloadTempFilePath(mDownLoadInfo.taskID));
        String filepath = mDownLoadInfo.filePath;
        filepath = filepath.substring(0, filepath.lastIndexOf("/"));
        File file = new File(filepath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return olefile.renameTo(newfile);
    }

    /**
     * 该接口用于在任务执行完之后通知下载管理器,以便下载管理器将已完成的任务移出任务列表
     */
    public interface DownloadCallback {
        void onSuccess(String TaskID);
    }
}
