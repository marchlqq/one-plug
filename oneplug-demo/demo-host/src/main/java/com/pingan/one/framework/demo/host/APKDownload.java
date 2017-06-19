package com.pingan.one.framework.demo.host;

import android.content.Context;

/**
 * Created by zl on 2016/3/7.
 */
public class APKDownload {

    private APKDownload(){

    }

    private static class SingleHolder{
        private static final APKDownload mInstance = new APKDownload();
    }

    public static final APKDownload getInstance(){
        return SingleHolder.mInstance;
    }

    public void downloadAPK(Context context, String url){

    }

    private void saveAPK(){

    }


}
