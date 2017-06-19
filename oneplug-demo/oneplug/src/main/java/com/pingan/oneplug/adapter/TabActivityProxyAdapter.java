package com.pingan.oneplug.adapter;

/**
 * 
 */
import android.app.Activity;
import android.widget.TabHost;
import android.widget.TabWidget;

public abstract interface TabActivityProxyAdapter extends ActivityGroupProxyAdapter {
    public abstract TabHost proxyGetTabHost();

    public abstract TabWidget proxyGetTabWidget();

    public abstract void proxySetDefaultTab(int paramInt);

    public abstract void proxySetDefaultTab(String paramString);

    public abstract Activity getCurrentActivity();
}