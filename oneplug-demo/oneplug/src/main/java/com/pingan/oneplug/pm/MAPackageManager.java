package com.pingan.oneplug.pm;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.pingan.oneplug.ProxyEnvironment;
import com.pingan.oneplug.install.ApkInstaller;
import com.pingan.oneplug.install.IInstallCallBack;
import com.pingan.oneplug.util.Constants;

/**
 * oneplug package manager. <br>
 * 负责安装卸载app，获取安装列表等工作.<br>
 * !!!注意：内置app必须以package name 命名，比如 com.pingan.xxx.apk <br>
 * 
 */
public class MAPackageManager {
    private static final String TAG = "MAPackageManager";
    
    /** 安装完一个包会发送该broadcast，extra 为 {@link #EXTRA_PKG_NAME} {@link#EXTRA_SRC_FILE}
     *  {@link #EXTRA_DEST_FILE} {@link #EXTRA_VERSION_CODE} {@link #EXTRA_VERSION_NAME}
     * */
    public static final String ACTION_PACKAGE_INSTALLED = "com.pingan.oneplug.installed";
    
    /** 安装失败会发送一个broadcast，extra为} */
    public static final String ACTION_PACKAGE_INSTALLFAIL = "com.pingan.oneplug.installfail";

    /** 删除完一个包会发送该broadcast，extra 为 {@link #EXTRA_PKG_NAME} */
    public static final String ACTION_PACKAGE_DELETED = "com.pingan.oneplug.deleted";
    
    /** 安装完的pkg的包名 */
    public static final String EXTRA_PKG_NAME = "package_name"; 
    /** 
     * 支持 assets:// 和 file:// 两种，对应内置和外部apk安装。
     * 比如  assets://oneplug/xxxx.apk , 或者 file:///data/data/com.pingan.xxx/files/xxx.apk  */
    public static final String EXTRA_SRC_FILE = "install_src_file";
    /** 安装完的apk path，没有scheme 比如 /data/data/com.pingan.xxx/xxx.apk */
    public static final String EXTRA_DEST_FILE = "install_dest_file";
    
    /** 安装完的pkg的 version code */
    public static final String EXTRA_VERSION_CODE = "version_code";
    /** 安装完的pkg的 version name */
    public static final String EXTRA_VERSION_NAME = "version_name"; 
    /** 进程模式  */
    public static final String EXTRA_PROCESS_MODE = "process_mode";
    
    public static final String SCHEME_ASSETS = "assets://";
    public static final String SCHEME_FILE = "file://";
    
    /** 安装失败的原因 */
    public static final String EXTRA_FAIL_REASON = "fail_reason";
    /** 安装失败的原因:签名不一致 */
    public static final String VALUE_SIGNATURE_NOT_MATCH = "signature_not_match";
    /** 安装失败的原因:没有签名 */
    public static final String VALUE_NO_SIGNATURE = "no_signature";
    /** 安装失败的原因:apk解析失败 */
    public static final String VALUE_PARSE_FAIL = "parse_fail";
    /** 安装失败的原因:安装文件拷贝失败 */
    public static final String VALUE_COPY_FAIL = "copy_fail";
    /** 安装失败的原因:超时 */
    public static final String VALUE_TIMEOUT = "time_out";
    /** 插件控制键： 独立进程*/
    public static final String PLUGIN_PROCESS_MODE_SINGLE = "single";
    /** 插件控制键： 主进程*/
    public static final String PLUGIN_PROCESS_MODE_MAIN = "main";
    /** 插件的控制键: 所有插件主进程 */
    public static final String HOST_PROCESS_MODE_NORMAL = "normal";
    /** 插件的控制键: 所有插件都在独立进程 */
    public static final String HOST_PROCESS_MODE_ALLSINGLE = "allsingle";
    /** 插件的控制键: 根据插件的定义确定，无定义使用主进程 */
    public static final String HOST_PROCESS_MODE_MAINFIRST = "mainfirst";
    /** 插件的控制键: 根据插件的定义确定，无定义使用独立进程 */
    public static final String HOST_PROCESS_MODE_SINGLEFIRST = "singlefirst";

    private static final String BROADCAST_PERMISSION_APS_INSTALL_SUFFIX = ".permission.APS_INSTALL";

    /** application context */
    private Context mContext; 

    private static MAPackageManager sInstance;
    
    /** 已安装列表。
     * !!!!!!! 不要直接引用该变量。 因为该变量是 lazy init 方式，不需要的是后不进行初始化。  
     * 使用 {@link #getInstalledPkgsInstance()} 获取该实例
     * */
    private Hashtable<String, MAPackageInfo> mInstalledPkgs;
    
    /** 安装包任务队列。 */
    private List<PackageAction> mPackageActions = new LinkedList<MAPackageManager.PackageAction>();
    
    /** 存贮在sharedpreference 的安装列表  */
    private static final String SP_APP_LIST = "packages";
    
    
    /**
     * Return code for when package deletion succeeds. This is passed to the
     * {@link IPackageDeleteObserver} by {@link #deletePackage()} if the system
     * succeeded in deleting the package.
     *
     */
    public static final int DELETE_SUCCEEDED = 1;

    /**
     * Deletion failed return code: this is passed to the
     * {@link IPackageDeleteObserver} by {@link #deletePackage()} if the system
     * failed to delete the package for an unspecified reason.
     *
     */
    public static final int DELETE_FAILED_INTERNAL_ERROR = -1;
    
    
    private MAPackageManager(Context context) {
        mContext = context.getApplicationContext();
        registerInstallderReceiver();
    }
    
    /**
     * lazy init mInstalledPkgs 变量，没必要在构造函数中初始化该列表，减少hostapp 每次初始化时的时间消耗。
     * @return
     */
    private Hashtable<String, MAPackageInfo> getInstalledPkgsInstance() {
        initInstalledPackageListIfNeeded();
        return mInstalledPkgs;
    }
    
    public synchronized static MAPackageManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MAPackageManager(context);
        }
        
        return sInstance;
    }
    
    /**
     * 获取安装列表。
     * @return
     */
    public List<MAPackageInfo> getInstalledApps() {
        Enumeration<MAPackageInfo> packages = getInstalledPkgsInstance().elements();
        ArrayList<MAPackageInfo> list = new ArrayList<MAPackageInfo>();
        while (packages.hasMoreElements()) {
            MAPackageInfo pkg = packages.nextElement();
            list.add(pkg);
        }
        
        return list;
    }
    
    /**
     * 初始化安装列表
     */
    private void initInstalledPackageListIfNeeded() {
        try {
            // 第一次初始化安装列表。
            if (mInstalledPkgs == null) {
                mInstalledPkgs = new Hashtable<String, MAPackageInfo>();

                SharedPreferences sp = mContext.getSharedPreferences(ApkInstaller.SHARED_PREFERENCE_NAME,
                        Context.MODE_PRIVATE);
                String jsonPkgs = sp.getString(SP_APP_LIST, "");

                if (jsonPkgs != null && jsonPkgs.length() > 0) {
                    try {

                        boolean needReSave = false;

                        JSONArray pkgs = new JSONArray(jsonPkgs);
                        int count = pkgs.length();
                        for (int i = 0; i < count; i++) {
                            JSONObject pkg = (JSONObject) pkgs.get(i);
                            MAPackageInfo pkgInfo = new MAPackageInfo();
                            pkgInfo.packageName = pkg.optString(MAPackageInfo.TAG_PKG_NAME);
                            pkgInfo.srcApkPath = pkg.optString(MAPackageInfo.TAG_APK_PATH);
                            pkgInfo.versionCode = pkg.optInt(MAPackageInfo.TAG_PKG_VC, 0);
                            pkgInfo.versionName = pkg.optString(MAPackageInfo.TAG_PKG_VN);
                            pkgInfo.processMode = pkg.optString(MAPackageInfo.TAG_PROCESS_MODE);
                            if (pkgInfo.versionCode == 0 || TextUtils.isEmpty(pkgInfo.versionName)) {
                                // 这两个值是从2.0版本才有的，为了兼容，做下处理。

                                PackageManager pm = mContext.getPackageManager();
                                PackageInfo pi = pm.getPackageArchiveInfo(pkgInfo.srcApkPath, 0);
                                pkgInfo.versionCode = pi.versionCode;
                                pkgInfo.versionName = pi.versionName;

                                needReSave = true;
                            }

                            mInstalledPkgs.put(pkgInfo.packageName, pkgInfo);
                        }

                        if (needReSave) { // 把兼容数据重新写回文件
                            saveInstalledPackageList();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 将安装列表写入文件存储。
     * 
     * 调用这个之前确保  mInstalledPkgs 已经初始化过了
     */
    private void saveInstalledPackageList() {
        synchronized (ProxyEnvironment.class) {
            JSONArray pkgs = new JSONArray();

            // 调用这个之前确保  mInstalledPkgs 已经初始化过了
            Enumeration<MAPackageInfo> packages = mInstalledPkgs.elements();
            while (packages.hasMoreElements()) {
                MAPackageInfo pkg = packages.nextElement();

                JSONObject object = new JSONObject();
                try {
                    object.put(MAPackageInfo.TAG_PKG_NAME, pkg.packageName);
                    object.put(MAPackageInfo.TAG_APK_PATH, pkg.srcApkPath);
                    object.put(MAPackageInfo.TAG_PKG_VC, pkg.versionCode);
                    object.put(MAPackageInfo.TAG_PKG_VN, pkg.versionName);
                    object.put(MAPackageInfo.TAG_PROCESS_MODE, pkg.processMode);

                    pkgs.put(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            SharedPreferences sp = mContext
                    .getSharedPreferences(ApkInstaller.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
            String value = pkgs.toString();
            Editor editor = sp.edit();
            editor.putString(SP_APP_LIST, value);
            editor.apply();
        }
    }
    
    private BroadcastReceiver sApkInstallerReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("hh-tag", "haohua apk install get broadcast 3");
            String permission = getBroadcastPermission(context);
            if (permission != null
                    && context.checkCallingOrSelfPermission(permission)
                        != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            String action = intent.getAction();
            if (ACTION_PACKAGE_INSTALLED.equals(action)) {
                String pkgName = intent.getStringExtra(EXTRA_PKG_NAME);
                String destApkPath = intent.getStringExtra(MAPackageManager.EXTRA_DEST_FILE);
                String versionName = intent.getStringExtra(MAPackageManager.EXTRA_VERSION_NAME);
                int versionCode = intent.getIntExtra(MAPackageManager.EXTRA_VERSION_CODE, 0);
                String processMode = intent.getStringExtra(MAPackageManager.EXTRA_PROCESS_MODE);
                
                MAPackageInfo pkgInfo = new MAPackageInfo();
                pkgInfo.packageName = pkgName;
                pkgInfo.srcApkPath = destApkPath;
                pkgInfo.versionCode = versionCode;
                pkgInfo.versionName = versionName;
                pkgInfo.processMode = processMode;
                
                getInstalledPkgsInstance().put(pkgName, pkgInfo);
                
                saveInstalledPackageList(); // 存储变化后的安装列表
                // 执行等待执行的action
                executePackageAction(pkgName, true, null);
            } else if (ACTION_PACKAGE_INSTALLFAIL.equals(action)) {

                // 针对内置应用，包名为文件名
                String assetsPath = intent.getStringExtra(MAPackageManager.EXTRA_SRC_FILE);
                int start = assetsPath.lastIndexOf("/");
                int end = assetsPath.lastIndexOf(ApkInstaller.APK_SUFFIX);
                try {
                    String mapPackagename = assetsPath.substring(start + 1, end);
                    String failReason = intent.getStringExtra(EXTRA_FAIL_REASON);
                    executePackageAction(mapPackagename, false, failReason);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.d("hh-tag", "haohua apk install get broadcast 4");
        }
        
    };
    
    /**
     * 监听安装列表变化.
     */
    private void registerInstallderReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PACKAGE_INSTALLED);
        filter.addAction(ACTION_PACKAGE_INSTALLFAIL);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);

        mContext.registerReceiver(sApkInstallerReceiver, filter, getBroadcastPermission(mContext), null);
    }
    
    /**
     * 包依赖任务队列对象。
     */
    private class PackageAction {
        long timestamp;
        IInstallCallBack callBack;
        String packageName;
    }
    
    /**
     * 执行依赖于安装包的 runnable，如果该package已经安装，则立即执行。如果oneplug正在初始化，或者该包正在安装，
     * 则放到任务队列中等待安装完毕执行。
     * 
     * @param packageName
     *            插件包名
     * @param callBack
     *            插件安装回调
     */
    public void packageAction(String packageName, IInstallCallBack callBack) {
        installBuildinApk(packageName); // 检查安装当前 apk，是否需要安装或者更新 consider no need to call it in browser
        executeInstallCallBack(packageName, callBack);
    }
        
    /**
     * 安装单个内置apk。此函数用在不需要调用 {@link #installBuildinApps()} 函数的时候，安装单个apk。
     * @param packageName getActivity()
     */
    public void installBuildinApk(String packageName) {
        ApkInstaller.installBuildinApp(mContext, ApkInstaller.ASSETS_PATH + File.separator + packageName
                + ApkInstaller.APK_SUFFIX);
    }
    
    /**
     * 安装apk文件
     * @param filePath apk文件路径
     * @param callBack 安装结果回调
     */
    public void installApkFile(String filePath, IInstallCallBack callBack) {
        String packageName = extractPackageName(filePath);
        if (TextUtils.isEmpty(packageName) && callBack != null) {
            callBack.onPackageInstallFail("", VALUE_PARSE_FAIL);
            return;
        }
        ApkInstaller.installApkFile(mContext, filePath);
        executeInstallCallBack(packageName, callBack);
    }
    
    /**
     * 解析apk包的包名
     * @param filePath apk文件路径
     * @return 包名
     */
    private String extractPackageName(String filePath) {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(filePath, 0);
        if (pkgInfo != null) {
            return pkgInfo.packageName;
        }
        return null;
    }
    
    /**
     * 执行安装回调
     * @param packageName 包名
     * @param callBack 安装结果回调
     */
    private void executeInstallCallBack(String packageName, IInstallCallBack callBack) {
        boolean packageInstalled = isPackageInstalled(packageName);
        
        boolean installing = ApkInstaller.isInstalling(packageName);
        if (Constants.DEBUG) {
            Log.d(TAG, "packageAction , " + packageName + " installed : " + packageInstalled
                    + " installing: " + installing);
        }
        
        if (packageInstalled && (!installing)) { // 安装了，并且没有更新操作
            if (callBack != null) {
                callBack.onPacakgeInstalled(packageName);
            }
            
        } else {
            PackageAction action = new PackageAction();
            action.packageName = packageName;
            action.timestamp = System.currentTimeMillis();
            action.callBack = callBack;
            
            synchronized(this) {
                if (mPackageActions.size() < 1000) { // 防止溢出
                    mPackageActions.add(action);
                }
            }
        }
        
        clearExpiredPkgAction(); 
    }

    /**
     * 静默安装单个插件
     * @param packageName
     *             包名
     * @param callBack
     *             执行安装回调
     */
    public void installBuildinApk(String packageName, IInstallCallBack callBack) {
        installBuildinApk(packageName); // 检查安装当前 apk，是否需要安装或者更新
        executeInstallCallBack(packageName, callBack);
    }
    
    private void executePackageAction(String packageName, boolean isSuccess, String failReason) {
        ArrayList<PackageAction> executeList = new ArrayList<MAPackageManager.PackageAction>();
       
        for (PackageAction action : mPackageActions) {
            if (packageName.equals(action.packageName)) {
                executeList.add(action);
            }
        }
        
        // 首先从总列表中删除
        synchronized(this) {
            for (PackageAction action : executeList) {
                mPackageActions.remove(action);
            }
        }
        
        // 挨个执行
        for (PackageAction action : executeList) {
            if (action.callBack != null) {
                if (isSuccess) {
                    action.callBack.onPacakgeInstalled(packageName);
                } else {
                    action.callBack.onPackageInstallFail(action.packageName, failReason);
                }
            }
        }
    }
    
    /**
     * 删除过期没有执行的 action，可能由于某种原因存在此问题。比如一个找不到package的任务。
     */
    private void clearExpiredPkgAction() {
        long currentTime = System.currentTimeMillis();
        
        ArrayList<PackageAction> deletedList = new ArrayList<PackageAction>();
        
        synchronized (this) {
            // 查找需要删除的
            for (PackageAction action : mPackageActions) {
                if (currentTime - action.timestamp >= 10 * 60 * 1000) {
                    deletedList.add(action);
                }
            }
            // 实际删除
            for (PackageAction action : deletedList) {
                mPackageActions.remove(action);
                action.callBack.onPackageInstallFail(action.packageName, VALUE_TIMEOUT);
            }
        }
    }
    
    

    
    /**
     * 判断一个package是否安装
     */
    public boolean isPackageInstalled(String packageName) {
        if (getInstalledPkgsInstance().containsKey(packageName)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * 判断app是否正在安装
     * @param packageName packageName
     * @return 正在安装返回 true
     */
    public boolean isPackageInstalling(String packageName) {
        return ApkInstaller.isInstalling(packageName);
    }
    
    /**
     * 获取安装apk的信息
     * @param packageName
     * @return 没有安装反馈null
     */
    public MAPackageInfo getPackageInfo(String packageName) {
        if (packageName == null || packageName.length() == 0) {
            return null;
        }
        
        MAPackageInfo info = getInstalledPkgsInstance().get(packageName);
        
        return info;
    }
    
    /**
     * 安装一个 apk file 文件. 用于安装比如下载后的文件，或者从sdcard安装。安装过程采用独立进程异步安装。
     * 安装完会有 {@link #ACTION_PACKAGE_INSTALLED} broadcast。

     * @param filePath apk 文件目录 比如  /sdcard/xxxx.apk
     */
    public void installApkFile(String filePath) {
        ApkInstaller.installApkFile(mContext, filePath);
    }
    
    /**
     * 安装内置在 assets/oneplug 目录下的 apk。
     * 内置app必须以 packageName 命名，比如 com.pingan.xx.apk
     */
    public void installBuildinApps() {
        ApkInstaller.installBuildinApps(mContext);
    }
    
    /**
     * 删除安装包。
     * @param packageName 需要删除的package 的 packageName
     * @param observer 卸载结果回调
     */
    public void deletePackage(final String packageName, IPackageDeleteObserver observer) {
        deletePackage(packageName, observer, false);
    }
    
    /**
     * 删除安装包。
     * @param packageName 需要删除的package 的 packageName
     *      * @param force 是否强制卸载，false表示插件运行时不卸载
     * @param observer 卸载结果回调
     */
    public void deletePackage(final String packageName, IPackageDeleteObserver observer, boolean force) {
        synchronized (ProxyEnvironment.class) {
            // 先停止运行插件
            if (!ProxyEnvironment.exitProxy(packageName, force)) {
                if (observer != null) {
                    observer.packageDeleted(packageName, DELETE_FAILED_INTERNAL_ERROR);
                }
                return;
            }

            //从安装列表中删除，并且更新存储安装列表的文件
            getInstalledPkgsInstance().remove(packageName);
            saveInstalledPackageList();

            // 删除生成的data数据文件
            // 清除environment中相关的数据:按前缀匹配
            ApkInstaller.deleteData(mContext, packageName);

            //删除安装文件，apk，dex，so
            ApkInstaller.deletePackage(mContext, packageName);


            // 回调
            if (observer != null) {
                observer.packageDeleted(packageName, DELETE_SUCCEEDED);
            }

            //发送广播
            Intent intent = new Intent(ACTION_PACKAGE_DELETED);
            intent.putExtra(EXTRA_PKG_NAME, packageName);
            mContext.sendBroadcast(intent);
        }
    }

    public static String getBroadcastPermission(Context context) {
        String permission = context.getPackageName() + BROADCAST_PERMISSION_APS_INSTALL_SUFFIX;
        return null;//permission;
    }
}
