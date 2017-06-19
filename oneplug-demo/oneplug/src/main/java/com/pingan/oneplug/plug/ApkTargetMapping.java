package com.pingan.oneplug.plug;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;

import java.io.File;
import java.util.HashMap;

public class ApkTargetMapping implements TargetMapping {

    private final Context context;
    private final File apkFile;

    private String versionName;
    private int versionCode;
    private String packageName;
    private String applicationClassName;
    private String defaultActivityName;
    private String applicationName;
    private PermissionInfo[] permissions;
    private PackageInfo packageInfo;
    private HashMap<String, ActivityInfo> mAcitivtyMap = new HashMap<String, ActivityInfo>();
    private HashMap<String, ServiceInfo> mServiceMap = new HashMap<String, ServiceInfo>();

    private Bundle metaData;

    /** 缓存获取插件{@link PackageInfo}的Flags */
    private int packageInfoFlags;

    /** 插件{@link ApplicationInfo} */
    private String applicationInfoDataDir;

    /** 初始化是否成功 */
    private boolean initSuccess = true;

    public ApkTargetMapping(Context context, File apkFile) {
        this.context = context;
        this.apkFile = apkFile;
        init();
    }

    private void init() {
        final PackageInfo pkgInfo;
        try {
            packageInfoFlags = PackageManager.GET_ACTIVITIES
                               | PackageManager.GET_META_DATA | PackageManager.GET_SERVICES;
                               //| PackageManager.GET_CONFIGURATIONS | PackageManager.GET_PERMISSIONS;
            pkgInfo = context.getPackageManager().getPackageArchiveInfo(apkFile.getAbsolutePath(), packageInfoFlags);

            if (!fillPackageInfo(pkgInfo, true)) {
                initSuccess = false;
            }

//            packageName = pkgInfo.packageName;
//            applicationClassName = pkgInfo.applicationInfo.className;
//            applicationName = (String) context.getPackageManager().getApplicationLabel(pkgInfo.applicationInfo);
//            defaultActivityName = pkgInfo.activities[0].name;
//            permissions = pkgInfo.permissions;
//            versionCode = pkgInfo.versionCode;
//            versionName = pkgInfo.versionName;
//            packageInfo = pkgInfo;
//
//            // 2.2 上获取不到application的meta-data，所以取默认activity里的meta作为开关
//            metaData = pkgInfo.activities[0].metaData;
//            ActivityInfo[] infos = pkgInfo.activities;
//            ServiceInfo[] serviceInfo = pkgInfo.services;
//            if (infos != null && infos.length > 0) {
//                for (ActivityInfo info : infos) {
//                    mAcitivtyMap.put(info.name, info);
//                }
//            }
//            if (serviceInfo != null && serviceInfo.length > 0) {
//                for (ServiceInfo info : serviceInfo) {
//                    mServiceMap.put(info.name, info);
//                }
//            }
//
//            packageInfo.applicationInfo = pkgInfo.applicationInfo;
//            packageInfo.applicationInfo.publicSourceDir = apkFile.getAbsolutePath();
//            Log.e("lxjlxjlxj", packageName + " : packageInfo.applicationInfo.publicSourceDir: " + packageInfo.applicationInfo.publicSourceDir);
        } catch (RuntimeException e) {
            // e.printStackTrace();
            return;
        }
    }

    /**
     * 填充创建的插件的{@link PackageInfo}
     *
     * @param pkgInfo {@link PackageInfo}
     * @param init 是否在初始化过程中
     *
     * @return true，填充成功；false，填充失败
     */
    private boolean fillPackageInfo(PackageInfo pkgInfo, boolean init) {
        if (pkgInfo == null) {
            return false;
        }
        packageName = pkgInfo.packageName;
        if (pkgInfo.applicationInfo == null) {
            return false;
        }
        applicationClassName = pkgInfo.applicationInfo.className;

        if (pkgInfo.activities != null) {
            ActivityInfo[] aInfos = pkgInfo.activities;
            if (aInfos.length > 0 && aInfos[0] != null) {
                defaultActivityName = aInfos[0].name;
                // Android 2.2获取不到Application的meta-data，默认去第一关Activity的meta-data
                metaData = aInfos[0].metaData;
            }
            for (ActivityInfo info : aInfos) {
                if (info != null) {
                    mAcitivtyMap.put(info.name, info);
                }
            }

        }
        if (pkgInfo.services != null) {
            ServiceInfo[] sInfos = pkgInfo.services;
            for (ServiceInfo info : sInfos) {
                if (info != null) {
                    mServiceMap.put(info.name, info);
                }
            }
        }

        permissions = pkgInfo.permissions;
        versionCode = pkgInfo.versionCode;
        versionName = pkgInfo.versionName;
        packageInfo = pkgInfo;

        packageInfo.applicationInfo = pkgInfo.applicationInfo;
        packageInfo.applicationInfo.publicSourceDir = apkFile.getAbsolutePath();

        if (init) {
            applicationInfoDataDir = pkgInfo.applicationInfo.dataDir;
        } else {
            packageInfo.applicationInfo.dataDir = applicationInfoDataDir;
        }
        try {
            applicationName = (String) context.getPackageManager().getApplicationLabel(pkgInfo.applicationInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return true;
    }

    @Override
    public String getPackageName() {
        return packageName;
    }

    @Override
    public String getApplicationClassName() {
        return applicationClassName;
    }

    @Override
    public String getDefaultActivityName() {
        return defaultActivityName;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    public PermissionInfo[] getPermissions() {
        return permissions;
    }

    @Override
    public String getVersionName() {
        return versionName;
    }

    @Override
    public int getVersionCode() {
        return versionCode;
    }

    @Override
    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    @Override
    /**
     * 根据flags获取packageInfo
     */
    public PackageInfo getPackageInfo(int flags) {
        // 如果新flags比本地缓存的flags多，则求和，重新获取一次，并将结果缓存
        if ((flags ^ packageInfoFlags) > 0 && (flags | packageInfoFlags) > packageInfoFlags) {
            PackageInfo pkgInfo = context.getPackageManager().getPackageArchiveInfo(
                    apkFile.getAbsolutePath(), flags | packageInfoFlags);
//            // 缓存packageInfo
//            packageInfoFlags = flags | packageInfoFlags;
//            packageInfo = pkgInfo;

            if (pkgInfo != null) {
                // 缓存数据
                if (fillPackageInfo(pkgInfo, false)) {
                    packageInfoFlags = flags | packageInfoFlags;
                }
            }
        }

        return packageInfo;
    }

    @Override
    public int getThemeResource(String activity) {
        if (activity == null) {
            return android.R.style.Theme;
        }
        ActivityInfo info = mAcitivtyMap.get(activity);

        /**
         * 指定默认theme为android.R.style.Theme
         * 有些OPPO手机上，把theme设置成0，其实会把Theme设置成holo主题，带ActionBar，导致插件黑屏，目前插件SDK不支持ActionBar
         */
        if (info == null || info.getThemeResource() == 0) {
            return android.R.style.Theme;
        }
        return info.getThemeResource();
    }

    @Override
    public ActivityInfo getActivityInfo(String activity) {
        if (activity == null) {
            return null;
        }
        return mAcitivtyMap.get(activity);
    }

    @Override
    public ServiceInfo getServiceInfo(String service) {
        if (service == null) {
            return null;
        }
        return mServiceMap.get(service);
    }

    /**
     * @return the metaData
     */
    public Bundle getMetaData() {
        return metaData;
    }

    @Override
    public int getTheme() {

        // application的theme取launcher的theme
        return getThemeResource(defaultActivityName);
    }

    @Override
    public void setApplicationInfoDataDir(String dir) {
        // 缓存下后设置的数据
        applicationInfoDataDir = dir;
        if (packageInfo != null && packageInfo.applicationInfo != null) {
            packageInfo.applicationInfo.dataDir = dir;
        }
    }

    @Override
    public boolean initSuccess() {
        return initSuccess;
    }
}
