package com.ss.android.apker;

/**
 * Created by chenlifeng on 16/5/24.
 * 必须在com.ss.android.apker目录下
 */
public interface Constant {

    // 调试开关
    boolean DEBUG = true;

    char REDIRECT_FLAG = '>';

    String APKER_PACKAGE_NAME = Constant.class.getPackage().getName();


    // TEST-----TODO 签名需要保存到线上，确保插件apk的安全签名校验
    String SIGNATURE_GAME = "ZwU2gJYfg4qPrGg2G0FbxV5nxA0=";
}
