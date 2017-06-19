package com.pingan.oneplug.api;

/**
 * Created by zl on 2016/3/3.
 */
public class HostCallbackManager {

    private ILoginCallback mILonginCallback;

    private HostCallbackManager() {

    }

    private static class SingleHolder {
        private static final HostCallbackManager mInstance = new HostCallbackManager();
    }

    public static final HostCallbackManager getInstance() {
        return SingleHolder.mInstance;
    }

    public ILoginCallback getILonginCallback() {
        return mILonginCallback;
    }

    public void setILonginCallback(ILoginCallback iLonginCallback) {
        this.mILonginCallback = iLonginCallback;
    }

}
