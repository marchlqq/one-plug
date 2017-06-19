package com.pingan.oneplug;

import java.util.HashMap;
import java.util.Map;

import com.pingan.oneplug.ma.MAActivity;
import com.pingan.oneplug.ma.MAActivityGroup;
import com.pingan.oneplug.ma.MADialogActivity;
import com.pingan.oneplug.ma.MAFragmentActivity;
import com.pingan.oneplug.ma.MAListActivity;
import com.pingan.oneplug.ma.MAPreferenceActivity;
import com.pingan.oneplug.ma.MATabActivity;
import com.pingan.oneplug.proxy.activity.ActivityGroupProxy;
import com.pingan.oneplug.proxy.activity.ActivityGroupProxyExt;
import com.pingan.oneplug.proxy.activity.ActivityGroupProxyTranslucent;
import com.pingan.oneplug.proxy.activity.ActivityGroupProxyTranslucentExt;
import com.pingan.oneplug.proxy.activity.ActivityProxy;
import com.pingan.oneplug.proxy.activity.ActivityProxyExt;
import com.pingan.oneplug.proxy.activity.ActivityProxyTranslucent;
import com.pingan.oneplug.proxy.activity.ActivityProxyTranslucentExt;
import com.pingan.oneplug.proxy.activity.DialogActivityProxy;
import com.pingan.oneplug.proxy.activity.DialogActivityProxyExt;
import com.pingan.oneplug.proxy.activity.FragmentActivityProxy;
import com.pingan.oneplug.proxy.activity.FragmentActivityProxyExt;
import com.pingan.oneplug.proxy.activity.FragmentActivityProxyTranslucent;
import com.pingan.oneplug.proxy.activity.FragmentActivityProxyTranslucentExt;
import com.pingan.oneplug.proxy.activity.TabActivityProxy;
import com.pingan.oneplug.proxy.activity.TabActivityProxyExt;
import com.pingan.oneplug.proxy.activity.TabActivityProxyTranslucent;
import com.pingan.oneplug.proxy.activity.TabActivityProxyTranslucentExt;

/**
 * Activity映射的管理，并且做Activity代理的计数
 * 
 */
public class ProxyActivityCounter {

    private static final Map<Class<?>, String> CLASS_MAP = new HashMap<Class<?>, String>();
    // 透明背景的映射关系。因为透明背景不能动态修改所以需要提前声明
    private static final Map<Class<?>, String> CLASS_MAP_TRANSLUCENT = new HashMap<Class<?>, String>();
    private static final Map<Class<?>, Integer> CLASS_COUNTS = new HashMap<Class<?>, Integer>();
    
    private static final Map<Class<?>, String> CLASS_EXT_MAP = new HashMap<Class<?>, String>();
    // 透明背景的映射关系。因为透明背景不能动态修改所以需要提前声明
    private static final Map<Class<?>, String> CLASS_EXT_MAP_TRANSLUCENT = new HashMap<Class<?>, String>();    

    private static ProxyActivityCounter instance;

    static {
        CLASS_MAP.put(MAActivity.class, ActivityProxy.class.getName());
        CLASS_MAP.put(MAFragmentActivity.class, FragmentActivityProxy.class.getName());
        // 下面两个代理使用硬编码是因为打包的时候可以动态选择是否打包进去这两个类
        CLASS_MAP.put(MAListActivity.class, "com.pingan.oneplug.proxy.activity.ListActivityProxy");
        CLASS_MAP.put(MAPreferenceActivity.class, "com.pingan.oneplug.proxy.activity.PreferenceActivityProxy");
        CLASS_MAP.put(MAActivityGroup.class, ActivityGroupProxy.class.getName());
        CLASS_MAP.put(MATabActivity.class, TabActivityProxy.class.getName());
        CLASS_MAP.put(MADialogActivity.class, DialogActivityProxy.class.getName());
    }
    
    /**
     * 需要在 host app的manifest中声明以下Activity。 主题设置为 Theme.Translucent.NoTitleBar
     */
    static {
        CLASS_MAP_TRANSLUCENT.put(MAActivity.class, ActivityProxyTranslucent.class.getName());
        CLASS_MAP_TRANSLUCENT.put(MAFragmentActivity.class, FragmentActivityProxyTranslucent.class.getName());
        CLASS_MAP_TRANSLUCENT.put(MAListActivity.class, "com.pingan.oneplug.proxy.activity.ListActivityProxyTranslucent");
        CLASS_MAP_TRANSLUCENT.put(MAPreferenceActivity.class, "com.pingan.oneplug.proxy.activity.PreferenceActivityProxyTranslucent");
        CLASS_MAP_TRANSLUCENT.put(MAActivityGroup.class, ActivityGroupProxyTranslucent.class.getName());
        CLASS_MAP_TRANSLUCENT.put(MATabActivity.class, TabActivityProxyTranslucent.class.getName());
        CLASS_MAP_TRANSLUCENT.put(MADialogActivity.class, DialogActivityProxy.class.getName()); // 不需要透明的映射，本身自己透明
    }

    static {
        CLASS_EXT_MAP.put(MAActivity.class, ActivityProxyExt.class.getName());
        CLASS_EXT_MAP.put(MAFragmentActivity.class, FragmentActivityProxyExt.class.getName());
        // 下面两个代理使用硬编码是因为打包的时候可以动态选择是否打包进去这两个类
        CLASS_EXT_MAP.put(MAListActivity.class, "com.pingan.oneplug.proxy.activity.ListActivityProxyExt");
        CLASS_EXT_MAP.put(MAPreferenceActivity.class, "com.pingan.oneplug.proxy.activity.PreferenceActivityProxyExt");
        CLASS_EXT_MAP.put(MAActivityGroup.class, ActivityGroupProxyExt.class.getName());
        CLASS_EXT_MAP.put(MATabActivity.class, TabActivityProxyExt.class.getName());
        CLASS_EXT_MAP.put(MADialogActivity.class, DialogActivityProxyExt.class.getName());
    }
    
    /**
     * 需要在 host app的manifest中声明以下Activity。 主题设置为 Theme.Translucent.NoTitleBar
     */
    static {
        CLASS_EXT_MAP_TRANSLUCENT.put(MAActivity.class, ActivityProxyTranslucentExt.class.getName());
        CLASS_EXT_MAP_TRANSLUCENT.put(MAFragmentActivity.class, FragmentActivityProxyTranslucentExt.class.getName());
        CLASS_EXT_MAP_TRANSLUCENT.put(MAListActivity.class, "com.pingan.oneplug.proxy.activity.ListActivityProxyTranslucentExt");
        CLASS_EXT_MAP_TRANSLUCENT.put(MAPreferenceActivity.class, "com.pingan.oneplug.proxy.activity.PreferenceActivityProxyTranslucentExt");
        CLASS_EXT_MAP_TRANSLUCENT.put(MAActivityGroup.class, ActivityGroupProxyTranslucentExt.class.getName());
        CLASS_EXT_MAP_TRANSLUCENT.put(MATabActivity.class, TabActivityProxyTranslucentExt.class.getName());
        CLASS_EXT_MAP_TRANSLUCENT.put(MADialogActivity.class, DialogActivityProxyExt.class.getName()); // 不需要透明的映射，本身自己透明
    }    
    
    private ProxyActivityCounter() {

    }

    public static synchronized ProxyActivityCounter getInstance() {
        if (instance == null) {
            instance = new ProxyActivityCounter();
        }
        return instance;
    }

    public Class<?> getNextAvailableActivityClass(Class<?> clazz, int theme, boolean useExt) {
        Class<?> iasClass = findIASClass(clazz);
        Integer count = CLASS_COUNTS.get(iasClass);
        if (count == null) {
            count = 0;
        }
        count++;
        CLASS_COUNTS.put(clazz, count);
        String className = null;
        // 使用 Theme_Translucent_NoTitleBar 作为透明背景的映射条件
        if (false == useExt) {
            if (theme == android.R.style.Theme_Translucent_NoTitleBar) {
                className = CLASS_MAP_TRANSLUCENT.get(iasClass);
            } else {
                className = CLASS_MAP.get(iasClass);
            }
        } else {
            if (theme == android.R.style.Theme_Translucent_NoTitleBar) {
                className = CLASS_EXT_MAP_TRANSLUCENT.get(iasClass);
            } else {
                className = CLASS_EXT_MAP.get(iasClass);
            }
        }
        try {
            return Class.forName(className);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Class<?> findIASClass(Class<?> clazz) {

        // 判断是哪类Activity，子类在前，父类在后
        if (MAFragmentActivity.class.isAssignableFrom(clazz)) {
            return MAFragmentActivity.class;
        } else if (MAPreferenceActivity.class.isAssignableFrom(clazz)) {
            return MAPreferenceActivity.class;
        } else if (MAListActivity.class.isAssignableFrom(clazz)) {
            return MAListActivity.class;
        } else if (MATabActivity.class.isAssignableFrom(clazz)) {
            return MATabActivity.class;
        } else if (MAActivityGroup.class.isAssignableFrom(clazz)) {
            return MAActivityGroup.class;
        } else if (MADialogActivity.class.isAssignableFrom(clazz)) {
            return MADialogActivity.class;
        } else if (MAActivity.class.isAssignableFrom(clazz)) {
            return MAActivity.class;
        }
        return MAActivity.class;
    }
}
