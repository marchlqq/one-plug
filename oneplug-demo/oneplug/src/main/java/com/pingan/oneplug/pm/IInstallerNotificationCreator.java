package com.pingan.oneplug.pm;

import android.app.Notification;
import android.content.Context;

/**
 * 插件安装服务器Foreground Notification的创建器
 * 
 */
public interface IInstallerNotificationCreator {

    /** Installer Notification通知id */
    int INSTALLER_NOTI_ID = 352789001;
    /** Notification的创建器声明的meta-data key */
    String MATA_DATA_NOTI_CREATOR_CLASS = "com.pingan.oneplug.installer.notification.class";

    /**
     * 创建通知
     * 
     * @param context
     *            host的application context
     * @return 通知对象
     */
    Notification createNotification(Context context);

}
