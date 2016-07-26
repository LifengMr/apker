package test.apker.android.ss.com.apkertest;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Button;

import com.ss.android.apker.download.DownLoadListener;
import com.ss.android.apker.entity.ApkEntity;
import com.ss.android.apker.helper.JLog;

/**
 * Created by chenlifeng on 16/7/25.
 */
public class DownloadButton extends Button implements DownLoadListener {
    public static final String TAG = DownloadButton.class.getName();

    private String mDefaultText;

    public DownloadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DownloadButton(Context context) {
        super(context);
    }

    @Override
    public void onStart(String taskID) {
        mDefaultText = getText().toString();
        JLog.i(TAG, "onStart mText=" + mDefaultText);
        setText(getContext().getString(R.string.download_start));
    }

    @Override
    public void onProgress(ApkEntity downLoadInfo) {
        if (downLoadInfo == null) {
            return;
        }
        float percent = 1.0f * downLoadInfo.downloadSize / downLoadInfo.fileSize;
        String text = getContext().getString(R.string.download_ing) + "(" + (int)(percent * 100) + "%)";
        setText(text);
    }

    @Override
    public void onStop(String taskID) {
        JLog.i(TAG, "onStop taskID=" + taskID);
        setText(getContext().getString(R.string.download_stop));
    }

    @Override
    public void onError(ApkEntity downLoadInfo) {
        if (downLoadInfo == null) {
            return;
        }
        JLog.i(TAG, "onStop taskID=" + downLoadInfo.taskID);
        setText(getContext().getString(R.string.download_error));
    }

    @Override
    public void onSuccess(ApkEntity downLoadInfo) {
        if (downLoadInfo == null || TextUtils.isEmpty(mDefaultText)) {
            return;
        }
        JLog.i(TAG, "onStop onSuccess=" + downLoadInfo.taskID);
        setText(mDefaultText);
    }
}
