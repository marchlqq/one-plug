package com.pingan.oneplug.install;

import android.os.Build;

/**
 *
 */
public class ApkInstallerUtils {
    public static boolean isHigherThanICS() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }
}
