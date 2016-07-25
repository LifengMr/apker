package com.ss.android.apker.parser;

import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.PatternMatcher;
import android.util.AttributeSet;

import com.ss.android.apker.Apker;
import com.ss.android.apker.Constant;
import com.ss.android.apker.compat.AssetManagerCompat;
import com.ss.android.apker.helper.JLog;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by chenlifeng on 16/5/22.
 */
public class ApkPackageParser {
    public static final String TAG = ApkPackageParser.class.getName();

    private static final String ANDROID_RESOURCES = "http://schemas.android.com/apk/res/android";
    private static final class R {
        public static final class styleable {
            // for manifest
            public static final int[] AndroidManifest = {
                    0x0101021b, 0x0101021c
            };
            public static final int AndroidManifest_versionCode = 0;
            public static final int AndroidManifest_versionName = 1;
            // for application
            public static int[] AndroidManifestApplication = {
                    0x01010000, 0x01010001, 0x01010003
            };
            public static int AndroidManifestApplication_theme = 0;
            public static int AndroidManifestApplication_label = 1;
            public static int AndroidManifestApplication_name = 2;
            // for activity
            public static int[] AndroidManifestActivity = {
                    0x01010000, 0x01010001, 0x01010002, 0x01010003,
                    0x0101001d, 0x0101001e, 0x0101022b
            };
            public static int AndroidManifestActivity_theme = 0;
            public static int AndroidManifestActivity_label = 1;
            public static int AndroidManifestActivity_icon = 2;
            public static int AndroidManifestActivity_name = 3;
            public static int AndroidManifestActivity_launchMode = 4;
            public static int AndroidManifestActivity_screenOrientation = 5;
            public static int AndroidManifestActivity_windowSoftInputMode = 6;
            // data (for intent-filter)
            public static int[] AndroidManifestData = {
                    0x01010026, 0x01010027, 0x01010028, 0x01010029,
                    0x0101002a, 0x0101002b, 0x0101002c
            };
            public static int AndroidManifestData_mimeType = 0;
            public static int AndroidManifestData_scheme = 1;
            public static int AndroidManifestData_host = 2;
            public static int AndroidManifestData_port = 3;
            public static int AndroidManifestData_path = 4;
            public static int AndroidManifestData_pathPrefix = 5;
            public static int AndroidManifestData_pathPattern = 6;
        }
    }

    private String mPackagePath;
    private String mPackageName;
    private WeakReference<byte[]> mReadBuffer;
    private PackageInfo mPackageInfo;
    private XmlResourceParser mParser;
    private Resources mResources;
    private ConcurrentHashMap<String, List<IntentFilter>> mIntentFilters;

    public ApkPackageParser(String packagePath, String packageName) {
        mPackagePath = packagePath;
        mPackageName = packageName;
    }

    public boolean parsePackage() {
        AssetManager assetManager = null;
        boolean assetError = true;
        try {
            assetManager = AssetManagerCompat.newInstance();
            int cookie = AssetManagerCompat.addAssetPath(assetManager, mPackagePath);
            if(cookie != 0) {
                mParser = assetManager.openXmlResourceParser(cookie, "AndroidManifest.xml");
                assetError = false;
            } else {
                JLog.w(TAG, "Failed adding asset path:" + mPackagePath);
            }
        } catch (Exception e) {
            JLog.w(TAG, "Unable to read AndroidManifest.xml of " + mPackagePath + ",e=" + e);
        }
        if (assetError) {
            if (assetManager != null) assetManager.close();
            return false;
        }

        mResources = new Resources(assetManager, Apker.ins().host().getResources().getDisplayMetrics(), null);
        return parsePackage(mResources, mParser);
    }

    private boolean parsePackage(Resources res, XmlResourceParser parser) {
        AttributeSet attrs = parser;
        mPackageInfo = new PackageInfo();
        try {
            int type;
            while ((type=parser.next()) != XmlResourceParser.START_TAG
                    && type != XmlResourceParser.END_DOCUMENT) ;

            mPackageInfo.packageName = parser.getAttributeValue(null, "package").intern();
            TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifest);
            mPackageInfo.versionCode = sa.getInteger(R.styleable.AndroidManifest_versionCode, 0);
            String versionName = sa.getString(R.styleable.AndroidManifest_versionName);
            if (versionName != null) {
                mPackageInfo.versionName = versionName.intern();
            }

            while ((type=parser.next()) != XmlResourceParser.END_DOCUMENT) {
                if (type == XmlResourceParser.TEXT) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals("application")) {
                    ApplicationInfo app = new ApplicationInfo(Apker.ins().host().getApplicationInfo());
                    sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestApplication);
                    String name = sa.getString(R.styleable.AndroidManifestApplication_name);
                    if (name != null) {
                        app.className = name.intern();
                    } else {
                        app.className = null;
                    }
                    app.theme = sa.getResourceId(R.styleable.AndroidManifestApplication_theme, 0);
                    mPackageInfo.applicationInfo = app;
                    break;
                }
            }
            sa.recycle();
            collectCertificates();
//            fixPackage();
            return true;
        } catch (XmlPullParserException e) {
        } catch (IOException e) {
        } catch (Exception e) {
        }
        return false;
    }

    private void fixPackage() throws Exception {
        PackageManager packageManager = Apker.ins().host().getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(mPackagePath, PackageManager.GET_PROVIDERS);
        if (packageInfo == null) {
            throw new Exception();
        }
        mPackageInfo.versionCode = packageInfo.versionCode;
        mPackageInfo.versionName = packageInfo.versionName;
    }

    public boolean collectActivities() {
        if (mPackageInfo == null || mPackageInfo.applicationInfo == null) {
            return false;
        }
        AttributeSet attrs = mParser;
        int type;
        try {
            List<ActivityInfo> activities = new ArrayList<ActivityInfo>();
            while ((type = mParser.next()) != XmlResourceParser.END_DOCUMENT) {
                if (type != XmlResourceParser.START_TAG) {
                    continue;
                }

                String tagName = mParser.getName();
                if (!tagName.equals("activity")) continue;

                ActivityInfo ai = new ActivityInfo();
                ai.applicationInfo = mPackageInfo.applicationInfo;
                ai.packageName = ai.applicationInfo.packageName;

                TypedArray sa = mResources.obtainAttributes(attrs,
                        R.styleable.AndroidManifestActivity);
                String name = sa.getString(R.styleable.AndroidManifestActivity_name);
                if (name != null) {
                    ai.name = ai.targetActivity = buildClassName(mPackageName, name);
                }
                ai.labelRes = sa.getResourceId(R.styleable.AndroidManifestActivity_label, 0);
                ai.icon = sa.getResourceId(R.styleable.AndroidManifestActivity_icon, 0);
                ai.theme = sa.getResourceId(R.styleable.AndroidManifestActivity_theme, 0);
                ai.launchMode = sa.getInteger(R.styleable.AndroidManifestActivity_launchMode, 0);
                //noinspection ResourceType
                ai.screenOrientation = sa.getInt(
                        R.styleable.AndroidManifestActivity_screenOrientation,
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                ai.softInputMode = sa.getInteger(R.styleable.AndroidManifestActivity_windowSoftInputMode, 0);

                activities.add(ai);
                sa.recycle();

                List<IntentFilter> intents = new ArrayList<IntentFilter>();
                int outerDepth = mParser.getDepth();
                while ((type=mParser.next()) != XmlResourceParser.END_DOCUMENT
                        && (type != XmlResourceParser.END_TAG
                        || mParser.getDepth() > outerDepth)) {
                    if (type == XmlResourceParser.END_TAG || type == XmlResourceParser.TEXT) {
                        continue;
                    }

                    if (mParser.getName().equals("intent-filter")) {
                        IntentFilter intent = new IntentFilter();
                        parseIntent(mResources, mParser, attrs, true, true, intent);

                        if (intent.countActions() == 0) {
                            JLog.w(TAG, "No actions in intent filter at "
                                    + mPackagePath + " "
                                    + mParser.getPositionDescription());
                        } else {
                            intents.add(intent);
                        }
                    }
                }

                if (intents.size() > 0) {
                    if (mIntentFilters == null) {
                        mIntentFilters = new ConcurrentHashMap<String, List<IntentFilter>>();
                    }
                    mIntentFilters.put(ai.name, intents);
                }
            }

            int N = activities.size();
            if (N > 0) {
                mPackageInfo.activities = new ActivityInfo[N];
                mPackageInfo.activities = activities.toArray(mPackageInfo.activities);
            }
            return true;
        } catch (XmlPullParserException e) {
        } catch (IOException e) {
        }
        return false;
    }

    public boolean collectProviders() {
        if (mPackageInfo == null || mPackageInfo.applicationInfo == null) {
            return false;
        }
        PackageManager packageManager = Apker.ins().host().getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(mPackagePath, PackageManager.GET_PROVIDERS);
        if (packageInfo == null) {
            return false;
        }
        mPackageInfo.providers = packageInfo.providers;
        if (mPackageInfo.providers == null) {
            // no providers
            return true;
        }
        for (ProviderInfo info : mPackageInfo.providers) {
            info.applicationInfo = mPackageInfo.applicationInfo;
            info.packageName = info.applicationInfo.packageName;
        }
        return true;
    }

    public boolean collectCertificates() {
        WeakReference<byte[]> readBufferRef;
        byte[] readBuffer = null;
        synchronized (this.getClass()) {
            readBufferRef = mReadBuffer;
            if (readBufferRef != null) {
                mReadBuffer = null;
                readBuffer = readBufferRef.get();
            }
            if (readBuffer == null) {
                readBuffer = new byte[8192];
                readBufferRef = new WeakReference<byte[]>(readBuffer);
            }
        }

        try {
            JarFile jarFile = new JarFile(mPackagePath);
            Certificate[] certs = null;
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry je = (JarEntry)entries.nextElement();
                if (je.isDirectory()) continue;
                if (je.getName().startsWith("META-INF/")) continue;
                Certificate[] localCerts = loadCertificates(jarFile, je,
                        readBuffer);
                if (Constant.DEBUG) {
                    JLog.i(TAG, "File " + mPackagePath + " entry " + je.getName()
                            + ": certs=" + certs + " ("
                            + (certs != null ? certs.length : 0) + ")");
                }
                if (localCerts == null) {
                    JLog.e(TAG, "Package " + mPackageName
                            + " has no certificates at entry "
                            + je.getName() + "; ignoring!");
                    jarFile.close();
                    return false;
                } else if (certs == null) {
                    certs = localCerts;
                } else {
                    // Ensure all certificates match.
                    for (int i=0; i<certs.length; i++) {
                        boolean found = false;
                        for (int j=0; j<localCerts.length; j++) {
                            if (certs[i] != null &&
                                    certs[i].equals(localCerts[j])) {
                                found = true;
                                break;
                            }
                        }
                        if (!found || certs.length != localCerts.length) {
                            JLog.e(TAG, "Package " + mPackageName
                                    + " has mismatched certificates at entry "
                                    + je.getName() + "; ignoring!");
                            jarFile.close();
                            return false;
                        }
                    }
                }
            }

            jarFile.close();
            synchronized (this.getClass()) {
                mReadBuffer = readBufferRef;
            }
            if (certs != null && certs.length > 0) {
                final int N = certs.length;
                mPackageInfo.signatures = new Signature[certs.length];
                for (int i=0; i<N; i++) {
                    mPackageInfo.signatures[i] = new Signature(
                            certs[i].getEncoded());
                }
            } else {
                JLog.e(TAG, "Package " + mPackageName
                        + " has no certificates; ignoring!");
                return false;
            }
        } catch (CertificateEncodingException e) {
            JLog.w(TAG, "CertificateEncodingException reading " + mPackagePath + ",e=" + e);
            return false;
        } catch (IOException e) {
            JLog.w(TAG, "IOException reading " + mPackagePath + ",e=" + e);
            return false;
        } catch (RuntimeException e) {
            JLog.w(TAG, "RuntimeException reading " + mPackagePath + ",e=" + e);
            return false;
        }
        return true;
    }

    private boolean parseIntent(Resources res, XmlResourceParser parser, AttributeSet attrs,
                boolean allowGlobs, boolean allowAutoVerify, IntentFilter outInfo) throws XmlPullParserException, IOException {
        TypedArray sa;
        int outerDepth = parser.getDepth();
        int type;
        while ((type = parser.next()) != XmlResourceParser.END_DOCUMENT
                && (type != XmlResourceParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type == XmlResourceParser.END_TAG || type == XmlResourceParser.TEXT) {
                continue;
            }

            String nodeName = parser.getName();
            if (nodeName.equals("action")) {
                String value = attrs.getAttributeValue(
                        ANDROID_RESOURCES, "name");
                if (value == null || value.length() == 0) {
                    return false;
                }
                skipCurrentTag(parser);
                outInfo.addAction(value);
            } else if (nodeName.equals("category")) {
                String value = attrs.getAttributeValue(
                        ANDROID_RESOURCES, "name");
                if (value == null || value.length() == 0) {
                    return false;
                }
                skipCurrentTag(parser);
                outInfo.addCategory(value);
            } else if (nodeName.equals("data")) {
                sa = res.obtainAttributes(attrs,
                        R.styleable.AndroidManifestData);
                String str = sa.getString(
                        R.styleable.AndroidManifestData_mimeType);
                if (str != null) {
                    try {
                        outInfo.addDataType(str);
                    } catch (IntentFilter.MalformedMimeTypeException e) {
                        sa.recycle();
                        return false;
                    }
                }

                str = sa.getString(
                        R.styleable.AndroidManifestData_scheme);
                if (str != null) {
                    outInfo.addDataScheme(str);
                }

                String host = sa.getString(
                        R.styleable.AndroidManifestData_host);
                String port = sa.getString(
                        R.styleable.AndroidManifestData_port);
                if (host != null) {
                    outInfo.addDataAuthority(host, port);
                }

                str = sa.getString(
                        R.styleable.AndroidManifestData_path);
                if (str != null) {
                    outInfo.addDataPath(str, PatternMatcher.PATTERN_LITERAL);
                }

                str = sa.getString(
                        R.styleable.AndroidManifestData_pathPrefix);
                if (str != null) {
                    outInfo.addDataPath(str, PatternMatcher.PATTERN_PREFIX);
                }

                str = sa.getString(
                        R.styleable.AndroidManifestData_pathPattern);
                if (str != null) {
                    if (!allowGlobs) {
                        return false;
                    }
                    outInfo.addDataPath(str, PatternMatcher.PATTERN_SIMPLE_GLOB);
                }

                sa.recycle();
                skipCurrentTag(parser);
            } else {
                return false;
            }
        }

        return true;
    }

    private static void skipCurrentTag(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int type;
        while ((type=parser.next()) != XmlResourceParser.END_DOCUMENT
                && (type != XmlResourceParser.END_TAG
                || parser.getDepth() > outerDepth)) {
        }
    }

    private Certificate[] loadCertificates(JarFile jarFile, JarEntry je,
                                           byte[] readBuffer) {
        try {
            // We must read the stream for the JarEntry to retrieve
            // its certificates.
            InputStream is = new BufferedInputStream(jarFile.getInputStream(je));
            while (is.read(readBuffer, 0, readBuffer.length) != -1) {
                // not using
            }
            is.close();
            return je != null ? je.getCertificates() : null;
        } catch (IOException e) {
            JLog.w(TAG, "Exception reading " + je.getName() + " in "
                    + jarFile.getName() + ",e=" + e);
        } catch (RuntimeException e) {
            JLog.w(TAG, "Exception reading " + je.getName() + " in "
                    + jarFile.getName() + ",e=" + e);
        }
        return null;
    }

    private static String buildClassName(String pkg, CharSequence clsSeq) {
        if (clsSeq == null || clsSeq.length() <= 0) {
            return null;
        }
        String cls = clsSeq.toString();
        char c = cls.charAt(0);
        if (c == '.') {
            return (pkg + cls).intern();
        }
        if (cls.indexOf('.') < 0) {
            StringBuilder b = new StringBuilder(pkg);
            b.append('.');
            b.append(cls);
            return b.toString().intern();
        }
        if (c >= 'a' && c <= 'z') {
            return cls.intern();
        }
        return null;
    }

    public Resources getResources() {
        return mResources;
    }

    public PackageInfo getPackageInfo() {
        return mPackageInfo;
    }

    public ConcurrentHashMap<String, List<IntentFilter>> getIntentFilters() {
        return mIntentFilters;
    }
}
