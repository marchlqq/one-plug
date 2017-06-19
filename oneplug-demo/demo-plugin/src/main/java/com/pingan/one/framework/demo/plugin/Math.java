package com.pingan.one.framework.demo.plugin;

public class Math {

    private Math() {
    }

    // 初始化库
    public native static long plus(int left, int right);

    static {
        System.loadLibrary("Math");
    }
}

