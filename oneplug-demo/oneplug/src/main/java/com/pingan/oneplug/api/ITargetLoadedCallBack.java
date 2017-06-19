package com.pingan.oneplug.api;

/**
 * 插件加载回调，用于后台加载插件
 * 
 */
public interface ITargetLoadedCallBack {
    
    /**
     * 插件加载完成
     * 
     * @param packageName
     *            插件包名
     */
    void onTargetLoaded(String packageName);
    
}
