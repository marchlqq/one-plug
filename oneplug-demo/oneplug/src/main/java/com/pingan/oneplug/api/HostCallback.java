package com.pingan.oneplug.api;

/**
 * Created by zl on 2016/3/3.
 */
public class HostCallback {

    private HostCallback() {

    }

    private static class SingleHolder {
        private static final HostCallback sInstance = new HostCallback();
    }

    public static final HostCallback getInstance() {
        return SingleHolder.sInstance;
    }


    public ILoginCallback getILonginCallback() {
        return HostCallbackManager.getInstance().getILonginCallback();
    }

    public void setILonginCallback(ILoginCallback iLonginCallback) {
        HostCallbackManager.getInstance().setILonginCallback(iLonginCallback);
    }
}
