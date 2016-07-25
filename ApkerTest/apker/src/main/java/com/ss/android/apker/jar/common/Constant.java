package com.ss.android.apker.jar.common;

public interface Constant {
	//调试开关
	boolean DEBUG = true;

	//主程序assets中存放插件文件夹名称
	String JAR_IN_MAIN_NAME = "jars";
	
	//主程序assets中存放插件配置文件名称
	String JARCONFIG_IN_MAIN_NAME = "jarconfig.xml";
	//插件解压存放文件夹名称
	String JAR_OUT_FOLDER_NAME = "outdex";
	
	//插件theme_id传递key
	String JAR_THEME_ID = "theme_id";


    //广告升级相关常量
	String ADS_APK_UPDATE_NAME = "Letv_Ads.apk_update";
    String ADS_APK_NAME = "Letv_Ads.apk";
    String ADS_SO_UPDATE_FOLDER = "adlibs";
    String ADS_SO_FILE_NAME = "libLetvAdSDK.so"; // so文件名
}
