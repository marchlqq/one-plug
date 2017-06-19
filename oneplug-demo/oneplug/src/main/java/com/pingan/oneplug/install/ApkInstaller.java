package com.pingan.oneplug.install;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.pingan.oneplug.ProxyEnvironment;
import com.pingan.oneplug.pm.MAPackageInfo;
import com.pingan.oneplug.pm.MAPackageManager;
import com.pingan.oneplug.util.Constants;
import com.pingan.oneplug.util.SimpleDateTime;
import com.pingan.oneplug.util.Util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * app 安装接口。实际安装采用 {@link ApkInstallerService} 独立进程异步安装。<br>
 * 
 * 插件支持的后缀名为 {@value #APK_SUFFIX}, 内置在 assets/oneplug 目录下，以{@value #APK_SUFFIX}后缀命名，安装完后缀名也是 {@value #APK_SUFFIX}。<br>
 * 
 * 由于android 2.2 以及一下对 asset文件的大小有1M限制。所以我们需要在编译脚本中对 aapt 增加一个 -0 {@value #APK_SUFFIX}参数，告诉aapt不要对{@value #APK_SUFFIX} 进行压缩处理。
 * 对于此问题的解释:http://ponystyle.com/blog/2010/03/26/dealing-with-asset-compression-in-android-apps/
 * 
 * @since 2014年5月8日
 */
public class ApkInstaller {
    
    /** DEBUG 开关 */
    public static final boolean DEBUG = true & Constants.DEBUG;
    /** TAG */
    public static final String TAG = "ApkInstaller";

    public static final String PLUGIN_PATH = "oneplug";

    /** 内置app的目录 assets/oneplug */
    public static final String ASSETS_PATH = "oneplug";

    public static final String APK_SUFFIX = ".apk";

    public static final String NATIVE_LIB_PATH = "lib";

    
    /** shared preference file name. */
    public static final String SHARED_PREFERENCE_NAME = "oneplug";
    
    /** HOST app 的 version code. 用于内置app的安装检查。*/
    private static final String SP_HOSTAPP_VERSIONCODE_FOR_INSTALL = "host_version_code_for_install";
    
    /** 安装/检查安装内置app是否结束。 */
    private static boolean sInstallBuildinAppsFinished = false;
  
    /** {@link #installBuildinApps(Context) 只能调用一次，再次调用，直接返回} */
    private static boolean sInstallBuildinAppsCalled = false;
    /** receiver 只注册一次。 */
    private static boolean sInstallerReceiverRegistered = false;
    
    /** 安装列表，用于存储正在安装的 文件列表（packageName），安装完从中删除。  */
    private static LinkedList<String> sInstallList = new LinkedList<String>();
    
    /** 安装阶段本次需要安装的内置app列表（packageName） */
    private static ArrayList<String> sBuildinAppList  = new ArrayList<String>();


    /**
     * 返回 oneplug 的根目录 /data/data/hostapp/app_oneplug
     * 
     * @param context
     *            host的context
     * 
     * @return 插件跟路径
     */
    public static File getOneplugRootPath(Context context) {
        File repoDir = context.getDir(PLUGIN_PATH, 0);

        if (!repoDir.exists()) {
            repoDir.mkdir();
        }
        return repoDir;
    }


    /**
     * 安装内置在 assets/oneplug 目录下的 apk
     * 
     * @param context
     */
    public synchronized static void installBuildinApps(Context context) {
        if (sInstallBuildinAppsCalled) {
            return;
        }
        sInstallBuildinAppsCalled = true;
        
        registerInstallderReceiver(context);
        
        ApplicationInfo pi = context.getApplicationInfo(); // host app's packageinfo
        
        // debugable 状态下特殊对待，因为开发中每次都需要检查安装。
        // eclipse install的apk debugable = true, release 的为 false。
        boolean debugable = false; // host app debugable 是否为 true.
        if ((pi.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) { 
            debugable = true;
        } 
        // 使用hostapp 默认 sharedpref，减少初始化开销。
        SharedPreferences sp =  PreferenceManager.getDefaultSharedPreferences(context);
        int savedHostVersionCode = sp.getInt(SP_HOSTAPP_VERSIONCODE_FOR_INSTALL, -1);
        int hostVersionCode = -1;
        try {
            hostVersionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
        }
        // 内置的app只需要安装一次，等下载hostapp升级，再次进行安装检查。debugable 模式除外.
        if (!debugable && hostVersionCode == savedHostVersionCode) {
            setInstallBuildinAppsFinished(context, false);
            return;
        }
        
        AssetManager am = context.getAssets();
        try {
            String files[] = am.list(ASSETS_PATH);
            boolean needInstall = false; //是否有文件需要安装或升级.
            for (String file : files) {
                if (!file.endsWith(APK_SUFFIX)) {
                    continue;
                }
                needInstall |= installBuildinApp(context, ASSETS_PATH + "/" + file);
            }
            
            if(!needInstall) { // 没有需要安装/升级的文件
                boolean writeVersioncide = savedHostVersionCode != hostVersionCode;
                setInstallBuildinAppsFinished(context, writeVersioncide);
            }           

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param context
     * @param assetsPath
     * @return 需要安装 返回 true，不需要安装 返回 false.
     */
    public static synchronized boolean installBuildinApp(Context context, String assetsPath) {
        registerInstallderReceiver(context);
        
        // 先判断是否有安装文件存在，然后判断一下更新时间是否一致。
        // 内置app必须以包名命名
        int start = assetsPath.lastIndexOf("/");
        int end = assetsPath.lastIndexOf(ApkInstaller.APK_SUFFIX);
        String mapPackagename = assetsPath.substring(start + 1, end);
        
        boolean isInstalling = isInstalling(mapPackagename);
        if (isInstalling && sBuildinAppList.contains(mapPackagename)) {
            // 有可能已经调用过 安装全部内置app，这里就不需要继续安装了
            return false;
        }
        
        MAPackageInfo pkgInfo = MAPackageManager.getInstance(context).getPackageInfo(mapPackagename);
        
        if (mapPackagename != null && mapPackagename.length() > 0 
                && pkgInfo != null) {
            
            File installedFile =  new File(pkgInfo.srcApkPath);
            // doubt something wrong with lib folder when open
            File pkgDir = new File(ApkInstaller.getOneplugRootPath(context), mapPackagename);
            File libDir = new File(pkgDir, ApkInstaller.NATIVE_LIB_PATH);
            if (installedFile.exists() && installedFile.isFile() && installedFile.length() > 0
                    && libDir.exists()) {
                try {

                    // 内置版本信息 asset 目录下的
                    InputStream buildinIs = context.getAssets().open(assetsPath);
                    SimpleDateTime buildinDateTime = Util.readApkModifyTime(buildinIs);
                    buildinIs.close();

                    // 如果已经存在了 ，则比较两个文件的最后修改日期，决定是否更新。
                    InputStream currentIs = new FileInputStream(installedFile);
                    SimpleDateTime currentDateTime = Util.readApkModifyTime(currentIs);
                    currentIs.close();

                    
                    // 如果当前安装的时间>= 内置app，则直接返回. 这种情况出现在 通过下载安装
                    if (currentDateTime.compareTo(buildinDateTime) >= 0) { 
                        return false;
                    } // else 继续往下执行，进行安装
                } catch (Exception e) {
                     e.printStackTrace();
                    // 异常继续往后执行.
                    return false;
                }
            }
        }
        

        startInstall(context, MAPackageManager.SCHEME_ASSETS + assetsPath);
        return true;
    }
    
    /**
     * 
     * 调用 {@link ApkInstallerService} 进行实际的安装过程。采用独立进程异步操作。
     * 
     * @param context
     * @param filePath 支持两种scheme {@link ApkInstallerService#SCHEME_ASSETS} 和 {@link ApkInstallerService#SCHEME_FILE} 
     */
    private static void startInstall(Context context, String filePath) {
        /*
         * 获取packagename
         * 1、内置app，要求必须以 packagename.apk 命名，处于效率考虑。
         * 2、外部文件的安装，直接从file中获取packagename, 消耗100ms级别，可以容忍。
         */
        String packageName = null;
        
        boolean isBuildin = false;
        
        if (filePath.startsWith(MAPackageManager.SCHEME_ASSETS)) {
            int start = filePath.lastIndexOf("/");
            int end = filePath.lastIndexOf(ApkInstaller.APK_SUFFIX);
            packageName = filePath.substring(start + 1, end);
            
            isBuildin = true;
            
        } else if(filePath.startsWith(MAPackageManager.SCHEME_FILE)) {
            PackageManager pm = context.getPackageManager();     
            String apkFilePath = filePath.substring(MAPackageManager.SCHEME_FILE.length());
            PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkFilePath, 0);
            if (pkgInfo != null) {
                packageName = pkgInfo.packageName;
            }
        }
        
        if (packageName != null) {
            add2InstallList(packageName); // 添加到安装列表中
            if (isBuildin) {
                sBuildinAppList.add(packageName); // 添加到内置app安装列表中
            }
        }
        
        Intent intent = new Intent(ApkInstallerService.ACTION_INSTALL);
        intent.setClass(context, ApkInstallerService.class);
        intent.putExtra(MAPackageManager.EXTRA_SRC_FILE, filePath);

        if (ApkInstallerUtils.isHigherThanICS()) {
            ApkInstallerRunnable.startInstallInSameThread(context, intent);
        } else {
            context.startService(intent);
        }
    }

    /**
     * 安装一个 apk file 文件. 用于安装比如下载后的文件，或者从sdcard安装。安装过程采用独立进程异步安装。
     * 安装完会有 {@link #ACTION_PACKAGE_INSTALLED} broadcast。
     * @param context 
     * @param filePath apk 文件目录 比如  /sdcard/xxxx.apk
     */
    public static void installApkFile(Context context, String filePath) {
        registerInstallderReceiver(context);
        startInstall(context, MAPackageManager.SCHEME_FILE + filePath);
    }
    
    
    /**
     * 返回已安装的应用列表。临时函数。可能为空，安装内置app还没有执行完毕。需要监听安装broadcast来更新安装列表。
     * 
     * Deprecated, use {@link MAPackageManager#getInstalledApps()}
     * 
     * @param context
     * @return 安装apk文件的 列表
     */
    @Deprecated
    public static ArrayList<File> getInstalledApps(Context context) {
        List<MAPackageInfo> pkgList = MAPackageManager.getInstance(context).getInstalledApps();
        
        ArrayList<File> result = new ArrayList<File>();
        for (MAPackageInfo pkg : pkgList) {
            String filePath = pkg.srcApkPath;
            result.add(new File(filePath));
        }
        
        return result;
    }
    
    /**
     * 获取安装插件的apk文件目录
     * 
     * @param context
     *            host的application context
     * @param packageName
     *            包名
     * @return File
     */
    public static File getInstalledApkFile(Context context, String packageName) {
        MAPackageInfo info = MAPackageManager.getInstance(context).getPackageInfo(packageName);
    
        if (info != null && info.srcApkPath !=null && info.srcApkPath.length() > 0) {
            return new File(info.srcApkPath);
        } else {
            return null;
        }
    }
    
    private static BroadcastReceiver sApkInstallerReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("hh-tag", "haohua apk install get broadcast 1");
            if (MAPackageManager.ACTION_PACKAGE_INSTALLED.equals(action)) {
                String pkgName = intent.getStringExtra(MAPackageManager.EXTRA_PKG_NAME);
                
                handleApkInstalled(context, pkgName);
            }
        }
        
    };
    
    
    private static void registerInstallderReceiver(Context context) {
        if (sInstallerReceiverRegistered) {
            // 已经注册过就不再注册
            return;
        }
        sInstallerReceiverRegistered = true;
        Context appcontext = context.getApplicationContext();
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(MAPackageManager.ACTION_PACKAGE_INSTALLED);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        appcontext.registerReceiver(sApkInstallerReceiver, filter);
    }
    
    /**
     * 增加到安装列表
     * @param packagename
     */
    private synchronized static void add2InstallList(String packagename) {
        if (sInstallList.contains(packagename)) {
            return;
        }
        
        sInstallList.add(packagename);
    }
    
    
    private synchronized static void handleApkInstalled(Context context, String packageName) {
        
        sInstallList.remove(packageName); // 从安装列表中删除
        
        // 检查内置app是否安装完成, 
        // 有可能是单个 apk 调用安装的，而不是调用全部安装，所以需要判断sInstallBuildinAppsCalled 来决定是否设置内置app安装完成
        if (sInstallBuildinAppsCalled && !sInstallBuildinAppsFinished) {
            if (sInstallList.isEmpty()) {
                setInstallBuildinAppsFinished(context, true);
            } else {
                boolean hasAssetsFileInstalling = false;
                for (String pkg : sInstallList) {
                    if (sBuildinAppList.contains(pkg)) {
                        hasAssetsFileInstalling = true;
                        break;
                    }
                }
                if (!hasAssetsFileInstalling) {
                    setInstallBuildinAppsFinished(context, true);
                }
            }
        }
        
        sBuildinAppList.remove(packageName);
    }
    
    
    /**
     * 内置app安装处理逻辑完成，有可能是检查不需要安装，有可能是实际安装完成。
     * @param context
     * @param writeVersionCode 是否是真的发生了实际安装，而不是检查完毕不需要安装，如果版本号不一致，也需要记录。
     */
    private static void setInstallBuildinAppsFinished(Context context, boolean writeVersionCode) {
        synchronized (ProxyEnvironment.class) {
            sInstallBuildinAppsFinished = true;

            if (writeVersionCode) {
                // 获取 hostapp verison code
                int hostVersionCode = -1;
                try {
                    hostVersionCode = context.getPackageManager()
                            .getPackageInfo(context.getPackageName(), 0).versionCode;
                } catch (NameNotFoundException e1) {
                    e1.printStackTrace();
                }
                // 保存当前的 hostapp verisoncode // 使用hostapp 默认 sharedpref，减少初始化开销。
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                // context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
                Editor editor = sp.edit();
                editor.putInt(SP_HOSTAPP_VERSIONCODE_FOR_INSTALL, hostVersionCode);
                editor.apply();
            }
            Log.d("hh-tag", "haohua apk install get broadcast 2");
        }
    }
    
    /**
     * 删除安装包的安装文件，apk，dex，so。
     * @param context app context
     * @param packageName 包名
     */
    public static void deletePackage(Context context, String packageName) {
        // 删除 apk 文件，因为有可能安装在sdcard，所以单独删除。
        File apkPath = getInstalledApkFile(context, packageName);
        if (apkPath != null) {
            apkPath.delete();
        }
        
        // 删除 dex,so 安装目录
        File dataDir = ProxyEnvironment.getDataDir(context, packageName);
        if (dataDir != null) {
            try {
                Util.deleteDirectory(dataDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除插件的数据
     * 
     * @param context
     *            host的application context
     * @param packageName
     *            插件包名
     */
    public static void deleteData(Context context, final String packageName) {
        String dataRoot = context.getApplicationInfo().dataDir;
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(packageName);
            }
        };

        // 按前缀删除shared_prefs
        File[] sharedPrefs = new File(dataRoot, "shared_prefs").listFiles(filter);
        if (sharedPrefs != null) {
            for (File file : sharedPrefs) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }

        // 按前缀删除数据库
        File[] databases = new File(dataRoot, "databases").listFiles(filter);
        if (databases != null) {
            for (File file : databases) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }

    }
    
    /**
     * 查看某个app是否正在安装
     * @param packageName
     */
    public static synchronized boolean isInstalling(String packageName) {
        return sInstallList.contains(packageName);
    }
}
