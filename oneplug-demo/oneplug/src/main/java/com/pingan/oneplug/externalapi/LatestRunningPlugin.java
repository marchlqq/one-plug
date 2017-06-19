package com.pingan.oneplug.externalapi;

/**
 *
 */
public class LatestRunningPlugin {
    private static String sLatestRunningPluginPackageName;

    public static void setLastestRunningPlugin(String aPackageName) {
        sLatestRunningPluginPackageName = aPackageName;
    }

    public static String getsLatestRunningPlugin() {
        return sLatestRunningPluginPackageName;
    }
}
