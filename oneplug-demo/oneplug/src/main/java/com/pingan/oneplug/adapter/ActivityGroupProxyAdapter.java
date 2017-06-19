package com.pingan.oneplug.adapter;

/**
 * 
 */

import android.app.Activity;
import android.app.ActivityGroup;

public abstract interface ActivityGroupProxyAdapter extends ActivityProxyAdapter {
    public abstract ActivityGroup getActivityGroup();

    public abstract Activity proxyGetCurrentActivity();
}