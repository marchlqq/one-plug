LOCAL_PATH := $(call my-dir)



include $(CLEAR_VARS)

APP_ABI := armeabi armeabi-v7a mips x86

LOCAL_MODULE    := Math

LOCAL_SRC_FILES := com_pingan_one_framework_demo_plugin_Math.c


include $(BUILD_SHARED_LIBRARY)
