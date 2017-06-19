package com.pingan.one.framework.demo.plugin;

import android.app.Application;
import android.util.Log;

import com.pingan.oneplug.ma.MAApplication;

/**
 * Created by zl on 2016/3/28.
 */
public class MainApplication extends MAApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("MainApplication","-==================================================================>");
    }
}
