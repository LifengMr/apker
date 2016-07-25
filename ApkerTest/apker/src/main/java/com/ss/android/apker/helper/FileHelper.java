package com.ss.android.apker.helper;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by chenlifeng on 16/5/27.
 */
public class FileHelper {
    public static final String TAG = FileHelper.class.getName();

    private static String mkdirs(Context context, String dir) {
        File file = new File(context.getCacheDir().getParentFile(), "apker");
        if (file.exists()) {
            file.mkdirs();
        }
        String apkRoot = file.getPath();
        File dirFile = new File(apkRoot, dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        JLog.i(TAG, "mkdirs dirFile.getPath()=" + dirFile.getPath() + ",dirFile.exists()=" + dirFile.exists());
        return dirFile.getPath();
    }

    public static String getDexDir(Context context, String packageName) {
        return mkdirs(context, "dex/" + packageName);
    }

    public static String getDexPath(Context context, String packageName, String apkName) {
        String path = getDexDir(context, packageName) + "/" + apkName;
        JLog.i(TAG, "getDexPath path=" + path);
        return path;
    }

    public static String getOptDexDir(Context context, String packageName) {
        return mkdirs(context, "optdex/" + packageName);
    }

    public static String getLibDir(Context context, String packageName) {
        return mkdirs(context, "lib/" + packageName);
    }

    public static String getDownloadDir() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/apker");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir.getAbsolutePath();
        } else {
            return null;
        }
    }

    public static String getDownloadFilePath(String fileName) {
        String dir = getDownloadDir();
        if (TextUtils.isEmpty(dir)) {
            return null;
        }
        return dir + "/" + fileName;
    }

    public static String getDownloadTempDir() {
        String dir = getDownloadDir();
        if (TextUtils.isEmpty(dir)) {
            return null;
        }
        File dirTmp = new File(dir + "/" + "temp");
        if (!dirTmp.exists()) {
            dirTmp.mkdirs();
        }
        return dirTmp.getAbsolutePath();
    }

    public static String getDownloadTempFilePath(String fileName) {
        String dir = getDownloadTempDir();
        if (TextUtils.isEmpty(dir)) {
            return null;
        }
        return dir + "/" + fileName;
    }

    public static void copySoLibs(Context context, String packageName, String apkName) throws Exception {
        ZipFile zipFile = null;
        try {
            String libDir = getLibDir(context, packageName);
            String apkPath = getDexPath(context, packageName, apkName);
            zipFile = new ZipFile(apkPath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            Map<String, ZipEntry> zipEntryMap = new HashMap<>();
            Map<String, Set<String>> soMap = new HashMap<>(1);

            findSoList(entries, zipEntryMap, soMap);
            boolean success = copySoList(zipFile, zipEntryMap, soMap, libDir);
            if (!success) {
                throw new Exception("copy So failed");
            }
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (Exception e) {}
            }
        }
    }

    private static void findSoList(Enumeration<? extends ZipEntry> entries,
                                   Map<String, ZipEntry> zipEntryMap, Map<String, Set<String>> soMap) {
        if (entries == null || zipEntryMap == null || soMap == null) {
            return;
        }
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            JLog.i(TAG, "findSoList name=" + name);
            if (name.contains("../")) {
                continue;
            }
            if (!entry.isDirectory() && name.startsWith("lib/")) {
                zipEntryMap.put(name, entry);
                File soFile = new File(name);
                String soName = soFile.getName();
                Set<String> fs = soMap.get(soName);
                if (fs == null) {
                    fs = new TreeSet<>();
                    soMap.put(soName, fs);
                }
                fs.add(name);
            }
        }
    }

    private static boolean copySoList(ZipFile zipFile, Map<String, ZipEntry> zipEntryMap,
                                      Map<String, Set<String>> soMap, String libDir) {
        if (zipFile == null || JavaHelper.isMapEmpty(soMap) || JavaHelper.isMapEmpty(zipEntryMap)) {
            return true;
        }
        for (String soName : soMap.keySet()) {
            Set<String> soPaths = soMap.get(soName);
            String soPath = filterSoPath(soPaths);
            if (soPath != null) {
                File file = new File(libDir, soName);
                if (file.exists()) {
                    file.delete();
                }
                InputStream inputStream = null;
                FileOutputStream outputStream = null;
                try {
                    inputStream = zipFile.getInputStream(zipEntryMap.get(soPath));
                    outputStream = new FileOutputStream(file);
                    byte[] buf = new byte[8192];
                    int read = 0;
                    while ((read = inputStream.read(buf)) != -1) {
                        outputStream.write(buf, 0, read);
                    }
                    outputStream.flush();
                    outputStream.getFD().sync();
                } catch (Exception e) {
                    if (file.exists()) {
                        file.delete();
                    }
                    return false;
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e) {}
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (Exception e) {}
                    }
                }
            }
        }
        return true;
    }

    private static String filterSoPath(Set<String> soPaths) {
        if (!JavaHelper.isSetEmpty(soPaths)) {
            if (Build.VERSION.SDK_INT >= 21) {
                String[] abis = Build.SUPPORTED_ABIS;
                for (String soPath : soPaths) {
                    for (String abi : abis) {
                        if (soPath.contains(abi)) {
                            return soPath;
                        }
                    }
                }
            } else {
                for (String soPath : soPaths) {
                    if (!TextUtils.isEmpty(Build.CPU_ABI) && soPath.contains(Build.CPU_ABI)) {
                        return soPath;
                    }
                }

                for (String soPath : soPaths) {
                    if (!TextUtils.isEmpty(Build.CPU_ABI2) && soPath.contains(Build.CPU_ABI2)) {
                        return soPath;
                    }
                }
            }
        }
        return null;
    }

    public static boolean copyFile(String sourcePath, String desPath) {
        JLog.i(TAG, "copyFile sourcePath=" + sourcePath + ", desPath=" + desPath);
        if (TextUtils.isEmpty(sourcePath) || TextUtils.isEmpty(desPath)) {
            return false;
        }

        File srcFile = new File(sourcePath);
        if (!srcFile.exists()) {
            return false;
        }

        File desFile = new File(desPath);
        if (desFile.exists()) {
            desFile.delete();
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(srcFile);
            outputStream = new FileOutputStream(desFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return true;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
            }
        }
        return false;
    }
}
