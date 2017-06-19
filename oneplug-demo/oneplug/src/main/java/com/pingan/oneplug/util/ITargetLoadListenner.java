package com.pingan.oneplug.util;

/**
 * 插件加载回调接口
 * 
 */
public interface ITargetLoadListenner {

    /**
     * 加载成功的回调，主线程回调
     * 
     * @param packageName
     *            加载成功的插件包名
     */
    void onLoadFinished(String packageName);
}