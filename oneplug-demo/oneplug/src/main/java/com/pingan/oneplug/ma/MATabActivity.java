package com.pingan.oneplug.ma;

/**
 * 
 */
import android.widget.TabHost;
import android.widget.TabWidget;

import com.pingan.oneplug.adapter.TabActivityProxyAdapter;

public class MATabActivity extends MAActivityGroup {
    private TabActivityProxyAdapter proxyActivity;

    public TabHost getTabHost() {
        return this.proxyActivity.proxyGetTabHost();
    }

    public TabWidget getTabWidget() {
        return this.proxyActivity.proxyGetTabWidget();
    }

    public void setActivityProxy(TabActivityProxyAdapter paramTabActivityProxyAdapter) {
        super.setActivityProxy(paramTabActivityProxyAdapter);
        this.proxyActivity = paramTabActivityProxyAdapter;
    }

    public void setDefaultTab(int paramInt) {
        this.proxyActivity.proxySetDefaultTab(paramInt);
    }

    public void setDefaultTab(String paramString) {
        this.proxyActivity.proxySetDefaultTab(paramString);
    }

}
