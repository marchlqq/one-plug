package com.pingan.oneplug.install;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;

import com.pingan.oneplug.pm.MAPackageManager;

/**
 *
 */
public class ApkInstallerRunnable implements Runnable {
    private Intent installIntent;
    private Context appContext;
    private static HandlerThread installThread;
    private static Handler installHandler;

    private ApkInstallerRunnable(Context context, Intent intent) {
        installIntent = intent;
        appContext = context;
    }

    @Override
    public void run() {
        String action = installIntent.getAction();
        if (action == null) {
            return;
        }

        if (action.equals(ApkInstallerService.ACTION_INSTALL)) {
            String srcFile = installIntent.getStringExtra(MAPackageManager.EXTRA_SRC_FILE);
            ApkInstallerImpl.handleInstall(appContext, srcFile);
        }
    }

    public static void startInstallInSameThread(final Context context, final Intent intent) {
        if (installHandler == null) {
            installThread = new HandlerThread("oneplug install thread");
            installThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
            installThread.start();
            installHandler = new Handler(installThread.getLooper());
        }
        installHandler.post(new ApkInstallerRunnable(context, intent));
    }
}
