package com.ss.android.apker.helper;

import android.content.Context;
import android.content.Intent;

import com.ss.android.apker.Apker;
import com.ss.android.apker.download.DownLoadService;

/**
 * Created by chenlifeng on 16/7/1.
 */
public class ApkerLauncher {

    public static void launch(Context context) {
        if (!ApkHelper.isMainProcess(context)) {
            return;
        }

        // init apker
        Apker.ins().init(context);
        // start download plugin
        context.startService(new Intent(context, DownLoadService.class));
    }
}
