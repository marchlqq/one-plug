package com.pingan.oneplug.plug;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;

/**
 * 插件信息映射接口
 * 
 */
public interface TargetMapping {

    int getTheme();

    String getPackageName();

    String getVersionName();

    int getVersionCode();

    PackageInfo getPackageInfo();

    PackageInfo getPackageInfo(int flags);

    int getThemeResource(String activity);

    ActivityInfo getActivityInfo(String activity);
    
    ServiceInfo getServiceInfo(String service);
    
    String getApplicationClassName();

    String getDefaultActivityName();

    PermissionInfo[] getPermissions();

    Bundle getMetaData();

    String getApplicationName();

    /**
     * 设置插件的{@link ApplicationInfo}中的dataDir
     *
     * @param dir 路径
     */
    void setApplicationInfoDataDir(String dir);
    /**
     * {@link TargetMapping}初始化是否成功
     *
     * @return true，初始化成功；false，初始化失败
     */
    boolean initSuccess();
}
