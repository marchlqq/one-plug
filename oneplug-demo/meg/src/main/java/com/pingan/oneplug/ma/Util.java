/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.pingan.oneplug.ma;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * 工具类，主要处理intent
 * @author chenzhiqin
 * @since 2014年4月29日
 */
public class Util {
    
    /** debug开关 */
    private static boolean DEBUG = false;
    
    /**
     * remap 启动Service的Intent
     * @param packageName 插件包名
     * @param intent 原来的intent
     */
    public static void remapStartServiceIntent(String packageName,Intent intent){
//        ProxyEnvironment.getInstance(packageName).remapStartServiceIntent(intent);

    }
    
    /**
     * remap 启动Activity的Intent
     * @param packageName 插件包名
     * @param intent 原来的intent
     */
    public static void remapStartActivityIntent(String packageName,Intent originIntent) {
    }
    
    public static void remapReceiverIntent(String packageName,Intent originIntent) {
    }

    /**
     * 获取host中的资源
     * 
     * @param context
     *            Context
     * @param packageName
     *            插件应用的包名
     * @param resourcesName
     *            资源名称
     * @param resourceType
     *            资源类型
     * @return 资源id
     */
    public static int getHostResourcesId(Context context, String packageName, String resourcesName, String resourceType) {
        if (context != null) {
            return context.getResources().getIdentifier(resourcesName, resourceType, context.getPackageName());
        }
        return 0;
    }
    /**
     * 退出某个插件应用，不是卸载插件
     * @param context Context
     * @param packageName 包名
     */
    public static void  quitApp(String packageName){
    }

    /**
     * 获取插件的包名
     * 
     * @param context
     *            插件的任意context
     * @return 插件包名
     */
    public static String getTargetPackageName(Context context) {

            return context.getPackageName();
    }

    /**
     * 创建插件Activity的启动Intent
     * 
     * @param ctx
     *            host或者插件的application context
     * @param packageName
     *            插件包名
     * @param className
     *            启动的Activity类名
     * @return intent
     */
    public static Intent createActivityIntent(Context ctx, String packageName, String className) {
        Intent intent = new Intent();
        intent.setClassName(ctx.getPackageName(), className);

        return intent;
    }

    /**
     * 根据代理Activity，获取目标Activity
     * 
     * @param activity
     *            代理Activity
     * @return 目标Activity
     */
    public MAActivity getMAActivityByProxy(Activity activity) {

        // 容错

        return null;
    }

    /**
     * 在megapp进程运行
     * @param intent 调用插件的intent
     * @return true使用代理 false不适用代理
     */
    public static boolean isUseExt(Context context, Intent intent) {
        if (intent != null) {
            if (intent.getComponent() != null) {
                return isUseExt(context, intent.getComponent().getPackageName());
            }
        }
        return false;
    }
    
    /**
     * 获取插件安装的apk包全路径
     * 
     * @param context 宿主的context
     * @param packageName 插件包名
     * @return 插件安装的apk包全路径
     */
    public static String getInstalledApkPath(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        return null;
//        return ApkInstaller.getMegappRootPath(context) + File.separator + packageName + ApkInstaller.APK_SUFFIX;
    }

    /**
     * 设置是否debug
     * @param debug 是否debug
     */
    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }
    
    /**
     * 在megapp进程运行
     * @param context context
     * @param targetPackageName 插件包名
     * @return true使用代理 false不使用代理
     */
    public static boolean isUseExt(Context context, String targetPackageName) {
//        if (DEBUG) {
//            return false;
//        }
//
//        // add for force multi-process
//        if (ForceMultiProcessPluginCenter.isNeedToForceMultiPorcess(targetPackageName)) {
//            return true;
//        }
//
//        String hostProcessMode = com.baidu.megapp.util.Util.getHostMetaData(
//                context,"com.baidu.megapp.processmode");
//        if (TextUtils.equals(hostProcessMode, "normal")) {
//            return false;
//        } else if (TextUtils.equals(hostProcessMode, "allsingle")) {
//            return true;
//        }
//
//        String pluginProcessMode = MAPackageManager.PLUGIN_PROCESS_MODE_MAIN;
//        MAPackageInfo packageInfo = MAPackageManager.getInstance(context).getPackageInfo(targetPackageName);
//        if (packageInfo != null) {
//            pluginProcessMode = packageInfo.processMode;
//        }
//
//        if (TextUtils.equals(hostProcessMode, "mainfirst")) {
//            if (TextUtils.equals(pluginProcessMode, "single")){
//                return true;
//            } else {
//                return false;
//            }
//        } else if (TextUtils.equals(hostProcessMode, "singlefirst")) {
//            if (TextUtils.equals(pluginProcessMode, "main")) {
//                return false;
//            } else {
//                return true;
//            }
//        }
        return false;
    }
}
