package com.pingan.oneplug.api;

import android.content.Context;

/**
 * 获取插件的回调接口
 * 
 */
public interface IGetContextCallBack {

    /**
     * 获取插件的Application Context
     * 
     * @param context
     *            插件 Application Context
     */
    void getTargetApplicationContext(Context context);

}
