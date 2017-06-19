package com.pingan.oneplug.ma;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;

import com.pingan.oneplug.ProxyEnvironment;
import com.pingan.oneplug.adapter.ActivityGroupProxyAdapter;
import com.pingan.oneplug.adapter.ActivityProxyAdapter;

/**
 * 
 */
public class MAActivityGroup extends MAActivity {
    private ActivityGroup activity;
    private ActivityGroupProxyAdapter proxyActivity;

    // BEGIN: 自定义的接口

    /**
     * 获取ActivityGroup里当前的Activity（MActivity）
     * 
     * @return MAActivity子类实例
     */
    public Context getCurrentMAActivity() {
        ActivityProxyAdapter adapter = (ActivityProxyAdapter) proxyActivity.proxyGetCurrentActivity();
        return adapter.getTarget();
    }

    public void remapStartActivityIntent(Intent intent) {
        ProxyEnvironment.getInstance(getTargetPackageName()).remapStartActivityIntent(intent);
    }

    // END: 自定义的接口

    /**
     * 获取ActivityGroup里当前的Activity（ActivityProxy）
     * 
     * @return Activity子类实例
     */
    public Activity getCurrentActivity() {
        return this.proxyActivity.proxyGetCurrentActivity();
    }

    public final LocalActivityManager getLocalActivityManager() {
        return this.activity.getLocalActivityManager();
    }

    public void setActivityProxy(ActivityGroupProxyAdapter paramActivityGroupProxyAdapter) {
        super.setActivityProxy(paramActivityGroupProxyAdapter);
        this.activity = paramActivityGroupProxyAdapter.getActivityGroup();
        this.proxyActivity = paramActivityGroupProxyAdapter;
    }
}
