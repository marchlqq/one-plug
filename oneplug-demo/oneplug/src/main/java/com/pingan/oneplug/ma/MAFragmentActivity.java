package com.pingan.oneplug.ma;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.pingan.oneplug.adapter.FragmentActivityProxyAdapter;

public class MAFragmentActivity extends MAActivity {

    private FragmentActivityProxyAdapter proxyActivity;

    public FragmentManager getSupportFragmentManager() {
        return proxyActivity.proxyGetSupportFragmentManager();
    }

    public void setActivityProxy(FragmentActivityProxyAdapter paramFramtentActivityProxyAdapter) {
        super.setActivityProxy(paramFramtentActivityProxyAdapter);
        this.proxyActivity = paramFramtentActivityProxyAdapter;
    }

    public void onAttachFragment(Fragment paramFragment) {
        proxyActivity.proxyOnAttachFragment(paramFragment);
    }

    public void startActivityFromFragment(Fragment paramFragment, Intent paramIntent, int paramInt) {
        proxyActivity.proxyStartActivityFromFragment(paramFragment, paramIntent, paramInt);
    }

}
