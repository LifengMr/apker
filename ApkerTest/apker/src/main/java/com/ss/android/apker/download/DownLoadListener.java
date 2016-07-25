package com.ss.android.apker.download;

import com.ss.android.apker.entity.ApkEntity;

public interface DownLoadListener {
    
    void onStart(String taskID);
    
    void onProgress(ApkEntity downLoadInfo);
    
    void onStop(String taskID);
    
    void onError(ApkEntity downLoadInfo);

    void onSuccess(ApkEntity downLoadInfo);
}
