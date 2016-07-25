package com.ss.android.apker.entity;

/**
 * Created by chenlifeng on 16/6/30.
 */
public class ApkEntity extends BaseBean {
    /** 插件名称、文件名 **/
    public String name;
    /** 插件包名 **/
    public String packageName;
    /** 签名加密字符串 **/
    public String signature;
    /** 插件类型 **/
    public int type;
    /** 插件版本 **/
    public int version;
    /** 插件标题 **/
    public String title;
    /** 插件描述 **/
    public String desc;
    /** 插件状态 0内置，1服务端 **/
    public int status;
    /** 下载地址 **/
    public String url;

    /** for download **/
    /** doanload taskID, equal packageName **/
    public String taskID;
    /** 文件下载路径 **/
    public String filePath;
    /** 文件总大小 **/
    public long fileSize;
    /** 已下载文件大小 **/
    public long downloadSize;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Entity info: ");
        sb.append("name = " + name);
        sb.append(" type = " + type);
        sb.append(" version = " + version);
        sb.append(" title = " + title);
        sb.append(" desc = " + desc);
        sb.append(" status = " + status);
        return sb.toString();
    }
}
