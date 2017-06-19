#include "com_pingan_one_framework_demo_plugin_Math.h"

jlong JNICALL Java_com_pingan_one_framework_demo_plugin_Math_plus
        (JNIEnv * env, jclass clazz, jint left, jint right) {
  return left + right;
}
