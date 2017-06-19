package com.pingan.oneplug.install;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.pingan.oneplug.ProxyEnvironment;
import com.pingan.oneplug.pm.ISignatureVerify;
import com.pingan.oneplug.pm.MAPackageManager;
import com.pingan.oneplug.util.Constants;
import com.pingan.oneplug.util.JavaCalls;
import com.pingan.oneplug.util.SignatureParser;
import com.pingan.oneplug.util.Util;

import dalvik.system.DexClassLoader;

/**
 *
 */
public class ApkInstallerImpl {
    public static final boolean DEBUG = true & Constants.DEBUG;
    /**
     * TAG
     */
    public static final String TAG = "ApkInstallerService";

    /**
     * apk 中 lib 目录的前缀标示。比如 lib/x86/libshare_v2.so
     */
    public static final String APK_LIB_DIR_PREFIX = "lib/";
    /**
     * lib中so后缀
     */
    public static final String APK_LIB_SUFFIX = ".so";
    /**
     * lib目录的 cpu abi 其实位置。比如 x86 的起始位置
     */
    public static final int APK_LIB_CPUABI_OFFSITE = APK_LIB_DIR_PREFIX.length();

    private static Object sSigObj;

    public static void handleInstall(Context context, String srcFile) {

        if (srcFile.startsWith(MAPackageManager.SCHEME_ASSETS)) {
            installBuildinApk(context, srcFile);
        } else if (srcFile.startsWith(MAPackageManager.SCHEME_FILE)) {
            installAPKFile(context, srcFile);
        }

    }

    /**
     * 安装内置的apk
     *
     * @param assetsPathWithScheme assets 目录
     */
    private static void installBuildinApk(Context context, String assetsPathWithScheme) {
        String assetsPath = assetsPathWithScheme.substring(MAPackageManager.SCHEME_ASSETS.length());
        // 先把 asset 拷贝到临时文件。
        InputStream is = null;
        try {
            is = context.getAssets().open(assetsPath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        doInstall(context, is, assetsPathWithScheme);
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 安装一个普通的文件 apk，用于外部或者下载后的apk安装。
     *
     * @param apkFilePathWithScheme 文件绝对目录
     */
    private static void installAPKFile(Context context, String apkFilePathWithScheme) {

        String apkFilePath = apkFilePathWithScheme.substring(MAPackageManager.SCHEME_FILE.length());

        File source = new File(apkFilePath);
        InputStream is = null;
        try {
            is = new FileInputStream(source);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            setInstallFail(context, apkFilePathWithScheme, MAPackageManager.VALUE_COPY_FAIL);
            return;
        }

        doInstall(context, is, apkFilePathWithScheme);

        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HandlerThread mParaInstallThread = new HandlerThread("para install thread");
    private static Handler mParaHandler = null;

    private static void startParaInstallThread() {
        if (mParaHandler == null) {
            mParaInstallThread.start();
            mParaInstallThread.setPriority(Thread.MIN_PRIORITY);
            mParaHandler = new Handler(mParaInstallThread.getLooper());
        }
    }


    private static String doInstall(final Context context, InputStream is, final String srcPathWithScheme) {
        if (DEBUG) {
            Log.d(TAG, "--- doInstall : " + srcPathWithScheme);
        }
        startParaInstallThread();
        if (is == null || srcPathWithScheme == null) {
            return null;
        }
        File tempFile = new File(ApkInstaller.getOneplugRootPath(context), System.currentTimeMillis() + "");
        boolean result = Util.copyToFile(is, tempFile);

        if (!result) {
            tempFile.delete();
            setInstallFail(context, srcPathWithScheme, MAPackageManager.VALUE_COPY_FAIL);
            return null;
        }

        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(tempFile.getAbsolutePath(),
                PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA | PackageManager.GET_SERVICES);
        if (pkgInfo == null) {
            tempFile.delete();
            setInstallFail(context, srcPathWithScheme, MAPackageManager.VALUE_PARSE_FAIL);
            return null;
        }


        final String packageName = pkgInfo.packageName;
        if (TextUtils.isEmpty(packageName)) {
            tempFile.delete();
            setInstallFail(context, srcPathWithScheme, MAPackageManager.VALUE_PARSE_FAIL);
            return null;
        }
        String processMode = "";

        if (pkgInfo.activities != null && pkgInfo.activities.length > 0) {
            Bundle metaData = pkgInfo.activities[0].metaData;
            if (metaData != null) {
                processMode = metaData.getString("com.pingan.oneplug.process");
            }
        }


        final File destFile = getPreferedInstallLocation(context, pkgInfo);
        if (destFile.exists()) {
            destFile.delete();
        }

        // 生成安装文件
        if (tempFile.getParent().equals(destFile.getParent())) {
            // 目标文件和临时文件在同一目录下
            tempFile.renameTo(destFile);
        } else {
            // 拷贝到其他目录，比如安装到 sdcard
            try {
                InputStream tempIs = new FileInputStream(tempFile);
                boolean tempResult = Util.copyToFile(tempIs, destFile);
                tempIs.close();
                tempFile.delete(); // 删除临时文件
                if (!tempResult) {
                    setInstallFail(context, srcPathWithScheme, MAPackageManager.VALUE_COPY_FAIL);
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                tempFile.delete();
                setInstallFail(context, srcPathWithScheme, MAPackageManager.VALUE_COPY_FAIL);
                return null;
            }
        }

        final File pkgDir = new File(ApkInstaller.getOneplugRootPath(context), packageName);
        pkgDir.mkdir();

        FutureTask<Boolean> backgroundTask = new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // 校验签名
                if (srcPathWithScheme.startsWith(MAPackageManager.SCHEME_FILE)) {
                    boolean isSignatureValid = verifySignature(context, packageName, destFile.getAbsolutePath(), false);
                    if (!isSignatureValid) {
                        setInstallFail(context, srcPathWithScheme, MAPackageManager.VALUE_SIGNATURE_NOT_MATCH);
                        return false;
                    }
                }

                // 如果是内置app，检查文件名是否以包名命名，处于效率原因，要求内置app必须以包名命名.
                if (srcPathWithScheme.startsWith(MAPackageManager.SCHEME_ASSETS)) {
                    int start = srcPathWithScheme.lastIndexOf("/");
                    int end = srcPathWithScheme.lastIndexOf(ApkInstaller.APK_SUFFIX);
                    String fileName = srcPathWithScheme.substring(start + 1, end);

                    if (!packageName.equals(fileName)) {
                        destFile.delete();
                        throw new RuntimeException(srcPathWithScheme + " must be named with it's package name : "
                                + packageName + ApkInstaller.APK_SUFFIX);
                    }
                }


                File libDir = new File(pkgDir, ApkInstaller.NATIVE_LIB_PATH);
                libDir.mkdirs();
                installNativeLibrary(destFile.getAbsolutePath(), libDir.getAbsolutePath());
                return true;
            }
        });
        // if (srcPathWithScheme.startsWith(MAPackageManager.SCHEME_FILE)) {
        mParaHandler.post(backgroundTask);
        // } else {
        //    backgroundTask.run();
        // }



        // dexopt
        installDex(context, destFile.getAbsolutePath(), packageName);
        boolean isBackgroundSuccess = false;
        try {
            isBackgroundSuccess = backgroundTask.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (!isBackgroundSuccess) {
            try {
                destFile.delete();
                Util.deleteDirectory(pkgDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        Intent intent = new Intent(MAPackageManager.ACTION_PACKAGE_INSTALLED);
        intent.setPackage(context.getPackageName());
        intent.putExtra(MAPackageManager.EXTRA_PKG_NAME, packageName);
        intent.putExtra(MAPackageManager.EXTRA_SRC_FILE, srcPathWithScheme); // 同时返回安装前的安装文件目录。
        intent.putExtra(MAPackageManager.EXTRA_DEST_FILE, destFile.getAbsolutePath()); // 同时返回安装前的安装文件目录。
        intent.putExtra(MAPackageManager.EXTRA_VERSION_CODE, pkgInfo.versionCode);
        intent.putExtra(MAPackageManager.EXTRA_VERSION_NAME, pkgInfo.versionName);
        intent.putExtra(MAPackageManager.EXTRA_PROCESS_MODE, processMode);
        context.sendBroadcast(intent, MAPackageManager.getBroadcastPermission(context));
        return packageName;
    }

    /**
     * 发送安装失败的广播
     *
     * @param srcPathWithScheme 安装文件路径
     * @param failReason        失败原因
     */
    private static void setInstallFail(Context context, String srcPathWithScheme, String failReason) {
        Intent intent = new Intent(MAPackageManager.ACTION_PACKAGE_INSTALLFAIL);
        intent.setPackage(context.getPackageName());
        intent.putExtra(MAPackageManager.EXTRA_SRC_FILE, srcPathWithScheme); // 同时返回安装前的安装文件目录。
        intent.putExtra(MAPackageManager.EXTRA_FAIL_REASON, failReason);
        context.sendBroadcast(intent, MAPackageManager.getBroadcastPermission(context));
    }

    /**
     * 安装 apk 中的 so 库。
     *
     * @param apkFilePath
     * @param libDir      lib目录。
     */
    private static void installNativeLibrary(String apkFilePath, String libDir) {
        final String cpuAbi = Build.CPU_ABI;
        String cpuAbi2 = "undifend";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            cpuAbi2 = Build.CPU_ABI2;
        }

        ZipFile zipFile = null;

        try {
            zipFile = new ZipFile(apkFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (zipFile == null) {
            return;
        }

        boolean hasPrimaryAbi = false;
        boolean hasNativeLibs = false;
        boolean installAnyLib = false;

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        ZipEntry entry;
        String name;
        int lastSlash;
        String targetCupAbi;

        while (entries.hasMoreElements()) {
            entry = entries.nextElement();

            // 比如 lib/x86/libshare_v2.so
            name = entry.getName();

            // 不是 lib 目录 继续
            if (name.startsWith(APK_LIB_DIR_PREFIX) && name.endsWith(APK_LIB_SUFFIX)) {

                hasNativeLibs = true;

                lastSlash = name.lastIndexOf("/");
                targetCupAbi = name.substring(APK_LIB_CPUABI_OFFSITE, lastSlash);

                if (cpuAbi.equals(targetCupAbi)) {
                    hasPrimaryAbi = true;
                } else if (cpuAbi2.equals(targetCupAbi)) {
                    if (hasPrimaryAbi) { // 如果两个abi都有，则只拷贝主abi
                        continue;
                    }
                } else {
                    continue;
                }

                installAnyLib = true;

                try {
                    InputStream entryIS = zipFile.getInputStream(entry);
                    String soFileName = name.substring(lastSlash);
                    Util.copyToFile(entryIS, new File(libDir, soFileName));
                    entryIS.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (hasNativeLibs && !installAnyLib) { // 考虑x86平台有兼容arm的能力
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                name = entry.getName();

                if (name.startsWith(APK_LIB_DIR_PREFIX) && name.endsWith(APK_LIB_SUFFIX)) {

                    lastSlash = name.lastIndexOf("/");

                    try {
                        InputStream entryIS = zipFile.getInputStream(entry);
                        String soFileName = name.substring(lastSlash);
                        Util.copyToFile(entryIS, new File(libDir, soFileName));
                        entryIS.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化 dex，因为第一次loaddex，如果放hostapp 进程，有可能会导致hang住(参考类的说明)。所以在安装阶段独立进程中执行。
     *
     * @param apkFile
     * @param packageName
     */
    private static void installDex(Context context, String apkFile, String packageName) {
        File dexDir = ProxyEnvironment.getDataDir(context, packageName);

        ClassLoader classloader = new DexClassLoader(apkFile, dexDir.getAbsolutePath(), null,
                context.getClassLoader()); // 构造函数会执行loaddex底层函数。

        // android 2.3以及以上会执行dexopt，2.2以及下不会执行。需要额外主动load一次类才可以
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO) {
            try {
                classloader.loadClass(packageName + ".R");
            } catch (ClassNotFoundException e) {
                // e.printStackTrace();
            }
        }
    }

    /**
     * 获取安装路径，可能是外部 sdcard或者internal data dir
     *
     * @param pkgInfo .installLocation 是否优先安装到外部存储器
     * @return 返回插件安装位置，非空
     */
    private static File getPreferedInstallLocation(Context context, PackageInfo pkgInfo) {

        boolean preferExternal = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            int installLocation = (Integer) (JavaCalls.getField(pkgInfo, "installLocation"));

            final int installLocationPreferExternal = 2; // see PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL

            if (installLocation == installLocationPreferExternal) {
                preferExternal = true;
            }
        } else {
            // android 2.1 以及以下不支持安装到sdcard
            preferExternal = false;
        }

        // 查看外部存储器是否可用
        if (preferExternal) {
            String state = Environment.getExternalStorageState();
            if (!Environment.MEDIA_MOUNTED.equals(state)) { // 不可用
                preferExternal = false;
            }
        }

        File destFile = null;
        if (!preferExternal) {
            // 默认安装到 internal data dir
            destFile = new File(ApkInstaller.getOneplugRootPath(context
            ), pkgInfo.packageName + ApkInstaller.APK_SUFFIX);
        } else {
            // 安装到外部存储器
            destFile = new File(context.getExternalFilesDir(ApkInstaller.PLUGIN_PATH),
                                pkgInfo.packageName + ApkInstaller.APK_SUFFIX);
        }
        return destFile;
    }

    /**
     * 安装时进行签名的校验
     *
     * @param packageName
     * @param newFilePath
     * @return 校验成功返回 true
     */
    private static boolean verifySignature(Context context, String packageName, String newFilePath, boolean onlyCheckExecFiles) {
        // 校验签名
        // 新文件如果没有签名不能安装
        Signature[] newSignatures = SignatureParser.collectCertificates(newFilePath, onlyCheckExecFiles);
        if (newSignatures == null) {
            if (DEBUG) {
                Log.e(TAG, "*** install fail : no signature!!!");
            }
            return false;
        }

        ISignatureVerify hostSignatureVerifier = getHostSignatureVerifier(context);


        // 如果存在老的安装包。
        File oldApkFile = ApkInstaller.getInstalledApkFile(context.getApplicationContext(), packageName);

        // 判断是否覆盖安装，如果是，计算旧版本的签名
        boolean isReplace = false;
        Signature[] oldSignatures = null;
        if (oldApkFile != null && oldApkFile.exists()) {
            oldSignatures = SignatureParser.collectCertificatesWithoutCheck(oldApkFile.getAbsolutePath());
            isReplace = true;
        }

        if (hostSignatureVerifier != null) {
            // 由主程序校验
            return verifySignatureByHost(hostSignatureVerifier, packageName, isReplace, oldSignatures, newSignatures);
        } else {

            if (oldSignatures == null // 老版本有可能没签名，这是直接允许安装，因为之前没上线签名校验
                    || Util.compareSignatures(oldSignatures, newSignatures) == PackageManager.SIGNATURE_MATCH) {
                return true;
            }
        }

        if (DEBUG) {
            Log.e(TAG, "### install fail : signature not match!!! , old=" + oldSignatures + ", new=" + newSignatures
                    + ", replace=" + isReplace);
        }

        return false;
    }

    /**
     * 获取hostapp签名类的实例，如果存在，则使用hostapp的签名类进行校验签名
     *
     * @return
     */
    private static ISignatureVerify getHostSignatureVerifier(Context context) {
        if (sSigObj == null) {
            sSigObj = Util
                    .getHostMetaDataClassInstance(context.getApplicationContext(),
                                                  ISignatureVerify.MATA_DATA_VERIFY_CLASS);
            if (sSigObj instanceof ISignatureVerify) {
                if (DEBUG) {
                    Log.d(TAG, "host SignatureVerify class : " + sSigObj.getClass().getName());
                }
            } else {
                sSigObj = null;
            }
        }
        return (ISignatureVerify) sSigObj;
    }

    /**
     * 调用主程序的签名校验类进行校验。
     *
     * @param verifier
     * @param packageName
     * @param isReplace
     * @param signatures
     * @param newSignatures
     * @return
     */
    private static boolean verifySignatureByHost(ISignatureVerify verifier, String packageName,
                                                 boolean isReplace, Signature[] signatures, Signature[] newSignatures) {
        boolean result = verifier.checkSignature(packageName, isReplace, signatures, newSignatures);
        return result;
    }
}
