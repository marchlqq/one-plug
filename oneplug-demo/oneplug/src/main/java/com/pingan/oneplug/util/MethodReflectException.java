package com.pingan.oneplug.util;

/**
 * 方法反射调用异常
 *
 */
public class MethodReflectException extends Exception {
    /** 序列 */
    private static final long serialVersionUID = 1L;
    /**
     * 构造体
     * @param msg 异常信息
     */
    public MethodReflectException(String msg) {
        super(msg);
    }

}
