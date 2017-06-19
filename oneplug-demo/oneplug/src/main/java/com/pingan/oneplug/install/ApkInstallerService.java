package com.pingan.oneplug.install;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.pingan.oneplug.pm.IInstallerNotificationCreator;
import com.pingan.oneplug.pm.MAPackageManager;
import com.pingan.oneplug.util.Constants;
import com.pingan.oneplug.util.Util;

/**
 * apk 安装service，从srcfile安装到destfile，并且安装so，以及dexopt。
 * 因为android4.1 以下系统dexopt会导致线程hang住无法返回，所以我们放到了一个独立进程，减小概率。
 * dexopt系统bug：http://code.google.com/p/android/issues/detail?id=14962
 * 
 */
public class ApkInstallerService extends IntentService {

    /** DEBUG 开关 */
    public static final boolean DEBUG = true & Constants.DEBUG;
    /** TAG */
    public static final String TAG = "ApkInstallerService";

    public static final String ACTION_INSTALL = "com.pingan.oneplug.action.install";
    
    public static final String ACTION_UNINSTALL = "com.pingan.oneplug.action.uninstall";


    public ApkInstallerService() {
        super(ApkInstallerService.class.getSimpleName());
    } 

    /**
     * @param name
     */
    public ApkInstallerService(String name) {
        super(name);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 去host读取meta里面的接口，判断是否要前台显示
        IInstallerNotificationCreator creator = null;
        Object obj = Util.getHostMetaDataClassInstance(getApplicationContext(),
                IInstallerNotificationCreator.MATA_DATA_NOTI_CREATOR_CLASS);
        if (obj instanceof IInstallerNotificationCreator) {
            if (DEBUG) {
                Log.d(TAG, "host IInstallerNotificationCreator class : " + obj.getClass().getName());
            }
            creator = (IInstallerNotificationCreator) obj;
        }

        if (creator != null) {
            startForeground(IInstallerNotificationCreator.INSTALLER_NOTI_ID, creator.createNotification(getApplicationContext()));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 退出时结束进程
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        
        if (action.equals(ACTION_INSTALL)) {
            String srcFile = intent.getStringExtra(MAPackageManager.EXTRA_SRC_FILE);
            //String destFile = intent.getStringExtra(EXTRA_DEST_FILE);
            //String pkgName = intent.getStringExtra(EXTRA_PKG_NAME);
            
            ApkInstallerImpl.handleInstall(this, srcFile);
        }
    }
}
