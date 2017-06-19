package com.pingan.oneplug.proxy.activity;

import android.content.Context;

import com.pingan.oneplug.ProxyExt;

/**
 * 多进程支持的代理类
 */
public class RootActivityExt extends RootActivity implements ProxyExt {
    protected Context myContext = RootActivityExt.this;
}