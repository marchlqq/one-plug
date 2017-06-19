package com.pingan.oneplug.externalapi;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class ForceMultiProcessPluginCenter {
    private static Set<String> sForceMultiProcessPlugin = new HashSet<String>();


    public static void addForceMultiProcessPluginProcessName(String aPackageName) {
        sForceMultiProcessPlugin.add(aPackageName);
    }

    public static boolean isNeedToForceMultiPorcess(String aPackageName) {
        return sForceMultiProcessPlugin.contains(aPackageName);
    }
}
