package com.pingan.oneplug.api;

import android.content.Context;
import android.view.View;

/**
 * 创建插件加载View的接口
 * 
 */
public interface ILoadingViewCreator {
    
    /**
     * 创建插件加载界面
     * 
     * @param context
     * @return View
     */
    View createLoadingView(Context context);

}
