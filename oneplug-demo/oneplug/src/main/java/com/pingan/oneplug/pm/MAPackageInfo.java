package com.pingan.oneplug.pm;

/**
 * Oneplug PackageInfo
 */
public class MAPackageInfo {
    /** 包名 */
    public String packageName;
    /** 安装后的apk file path。 */
    public String srcApkPath;
    /** version code 
     *  @since 2.0
     * */
    public int versionCode;
    /** version Name 
     * @since 2.0
     *  */
    public String versionName;
    
    /** 标示插件是在独立进程启动 还是在 主进程启动 */
    public String processMode;
    
    ///////////////////////////
    /** 存储在安装列表中的key */
    final static String TAG_PKG_NAME = "pkgName";
    /** 存储在安装列表中的key */
    final static String TAG_APK_PATH = "srcApkPath";
    /** 存储在安装列表中的key
     *  @since 2.0
     *  */
    final static String TAG_PKG_VC = "versionCode";
    /** 存储在安装列表中的key
     *  @since 2.0
     *  */
    final static String TAG_PKG_VN = "versionName";
    /**
     * 进程启动方式 main：主进程  single：独立进程；
     */
    final static String TAG_PROCESS_MODE = "processMode";
}
