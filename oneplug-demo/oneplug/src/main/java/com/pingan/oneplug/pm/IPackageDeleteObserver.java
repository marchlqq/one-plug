package com.pingan.oneplug.pm;

/**
 * 卸载package回调接口。
 */
public interface IPackageDeleteObserver {
    /**
     * 卸载操作回调
     * @param packageName 卸载的包名
     * @param returnCode 返回值
     */
    void packageDeleted(String packageName, int returnCode);
}
