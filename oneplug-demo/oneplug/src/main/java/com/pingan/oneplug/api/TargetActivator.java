package com.pingan.oneplug.api;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.pingan.oneplug.ProxyEnvironment;
import com.pingan.oneplug.pm.MAPackageManager;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 插件控制器
 * 
 */
public final class TargetActivator {

    /**
     * 加载并启动插件
     * 
     * @param context
     *            host的Activity
     * @param intent
     *            目标intent
     * 
     */
    public static void loadTargetAndRun(final Context context, final Intent intent) {
        loadTargetAndRun(context, intent, false);
    }
    
    /**
     * 加载并启动插件
     * 
     * @param context
     *            host的Activity
     * @param intent
     *            目标Intent
     * @param creator
     *            loading界面创建器
     */
    public static void loadTargetAndRun(final Context context, final Intent intent, ILoadingViewCreator creator) {
        ProxyEnvironment.putLoadingViewCreator(intent.getComponent().getPackageName(), creator);
        loadTargetAndRun(context, intent, false);
    }

    /**
     * 加载并启动插件
     * 
     * @param context
     *            host的Activity
     * @param intent
     *            目标Intent
     * 
     * @param isSilence
     *            是否是静默加载插件
     */
    public static void loadTargetAndRun(final Context context, final Intent intent, boolean isSilence) {
        intent.putExtra(ProxyEnvironment.EXTRA_TARGET_REDIRECT_ISSILENCE, isSilence);
        ProxyEnvironment.enterProxy(context, intent);
    }

    /**
     * 加载并启动插件
     * 
     * @param context
     *            host的Activity
     * @param componentName
     *            目标Component
     */
    public static void loadTargetAndRun(final Context context, final ComponentName componentName) {
        Intent intent = new Intent();
        intent.setComponent(componentName);
        loadTargetAndRun(context, intent);
    }

    /**
     * 加载并启动插件
     * 
     * @param context
     *            host的Activity
     * @param componentName
     *            目标Component
     * 
     * @param creator
     *            loading界面创建器
     */
    public static void loadTargetAndRun(final Context context, final ComponentName componentName,
            ILoadingViewCreator creator) {
        ProxyEnvironment.putLoadingViewCreator(componentName.getPackageName(), creator);
        loadTargetAndRun(context, componentName);
    }

    /**
     * 加载并启动插件
     * 
     * @param context
     *            host的application context
     * @param packageName
     *            插件包名
     */
    public static void loadTargetAndRun(final Context context, String packageName) {
        loadTargetAndRun(context, new ComponentName(packageName, ""));
    }

    /**
     * 加载并启动插件
     * 
     * @param context
     *            host的application context
     * @param packageName
     *            插件包名
     * @param creator
     *            插件loading界面的创建器
     */
    public static void loadTargetAndRun(final Context context, String packageName, ILoadingViewCreator creator) {
        ProxyEnvironment.putLoadingViewCreator(packageName, creator);
        loadTargetAndRun(context, new ComponentName(packageName, ""));
    }

    /**
     * 静默加载插件，异步加载
     * 
     * @param context
     *            application Context
     * @param packageName
     *            插件包名
     */
    public static void loadTarget(final Context context, String packageName) {
        loadTargetAndRun(context, new ComponentName(packageName, ProxyEnvironment.EXTRA_VALUE_LOADTARGET_STUB));
    }
    
    /**
     * 静默加载插件，异步加载，可以设置callback
     * 
     * @param context
     *            application Context
     * @param packageName
     *            插件包名
     * @param callback
     *            加载成功的回调
     */
    public static void loadTarget(final Context context, final String packageName, 
            final ITargetLoadedCallBack callback) {

        // 插件已经加载
        if (ProxyEnvironment.isEnterProxy(packageName)) {
            callback.onTargetLoaded(packageName);
            return;
        }

        if (callback == null) {
            loadTarget(context, packageName);
            return;
        }

        BroadcastReceiver recv = new BroadcastReceiver() {
            public void onReceive(Context ctx, Intent intent) {
                if (ctx.checkCallingOrSelfPermission(MAPackageManager.getBroadcastPermission(ctx))
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                String curPkg = intent.getStringExtra(ProxyEnvironment.EXTRA_TARGET_PACKAGNAME);
                if (ProxyEnvironment.ACTION_TARGET_LOADED.equals(intent.getAction())
                        && TextUtils.equals(packageName, curPkg)) {
                    callback.onTargetLoaded(packageName);
                    try {
                        ctx.unregisterReceiver(this);
                    } catch (RuntimeException e) {
                        // 某些2.3手机上会crash，暂时先捕获一下
                    }
                }
            };
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ProxyEnvironment.ACTION_TARGET_LOADED);
        context.getApplicationContext().registerReceiver(recv, filter);

        Intent intent = new Intent();
        intent.putExtra(ProxyEnvironment.EXTRA_TARGET_REDIRECT_ISSILENCE, true);
        intent.setAction(ProxyEnvironment.ACTION_TARGET_LOADED);
        intent.setComponent(new ComponentName(packageName, recv.getClass().getName()));
        ProxyEnvironment.enterProxy(context, intent);
    }

    /**
     * 获取 package 对应的 classLoader。一般情况下不需要获得插件的classloader。 只有那种纯 jar
     * sdk形式的插件，需要获取classloader。 获取过程为异步回调的方式。此函数，存在消耗ui线程100ms-200ms级别。
     * 
     * @param context
     *            application Context
     * @param packageName
     *            插件包名
     * @param callback
     *            回调，classloader 通过此异步回调返回给hostapp
     */
    public static void loadAndGetClassLoader(final Context context, final String packageName, 
            final IGetClassLoaderCallback callback) {
        
        loadTarget(context, packageName, new ITargetLoadedCallBack() {

            @Override
            public void onTargetLoaded(String packageName) {
                ProxyEnvironment.initProxyEnvironment(context, packageName);
                ProxyEnvironment targetEnv = ProxyEnvironment.getInstance(packageName);
                ClassLoader classLoader = targetEnv.getDexClassLoader();
                
                callback.getClassLoaderCallback(classLoader);

            }
        });

    }
    
    /**
     * 同步获取classloader.
     * @param context app context
     * @param packageName packageName
     * @param initApplication 是否初始化 Application，并调用其 onCreate函数
     * 
     * @return  如果没有安装，或者正在安装， 返回 false。只有安装完成了才返回true.
     */
    public static synchronized ClassLoader loadAndGetClassLoader(final Context context, final String packageName, 
            boolean initApplication) {
        ClassLoader classLoader = null;
        
        // 安装了进行初始化 
        if (MAPackageManager.getInstance(context).isPackageInstalled(packageName)) {
            ProxyEnvironment.initProxyEnvironment(context, packageName);
            ProxyEnvironment targetEnv = ProxyEnvironment.getInstance(packageName);
            
            if (initApplication && targetEnv.getApplication() == null) {
                ComponentName cn = new ComponentName(packageName, ProxyEnvironment.EXTRA_VALUE_LOADTARGET_STUB);
                final Intent intent = new Intent();
                intent.setComponent(cn);
                
                if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                    // 主线程直接进行初始化
                    // FIXME: 外层暂时不要使用这个类，此处在调用异常时会有问题
                    ProxyEnvironment.launchIntent(context, intent); // 初始化 application。
                } else {
                    // 非主线程，把初始化任务交给主线程处理。然后阻塞，等待主线程完成
                    final CountDownLatch latch = new CountDownLatch(1);
                    new AsyncTask<String, Integer, String>() {
                        @Override
                        protected String doInBackground(String... params) {
                            String pPackageName = params[0];
                            return pPackageName;
                        };

                        @Override
                        protected void onPostExecute(String result) {
                            // 使用主线程初始化 application。
                            try {
                                ProxyEnvironment.launchIntent(context, intent); 
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            
                            latch.countDown(); // 通知await 任务完成
                        }
                    }.execute(packageName);
                    
                    try {
                        latch.await(); // 等主线程完成
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            classLoader = targetEnv.getDexClassLoader();
        }
        
        return classLoader;
    }

    /**
     * 加载插件并获取插件的Application Context
     * 
     * @param context
     *            host的 application context
     * @param packageName
     *            插件包名
     * @param callback
     *            获取成功的回调
     */
    public static void loadAndApplicationContext(Context context, String packageName,
            final IGetContextCallBack callback) {

        loadTarget(context, packageName, new ITargetLoadedCallBack() {

            @Override
            public void onTargetLoaded(String packageName) {
                callback.getTargetApplicationContext(ProxyEnvironment.getInstance(packageName).getApplication());
            }
        });

    }

    /**
     * 加载插件并创建插件内的View，View的Context是插件的Application Context
     * 
     * @param context
     *            host的 application context
     * @param packageName
     *            插件包名
     * @param viewClass
     *            view的类名
     * @param callback
     *            view创建成功的回调
     */
    public static void loadAndCreateView(Context context, final String packageName, final String viewClass,
            final ICreateViewCallBack callback) {

        loadTarget(context, packageName, new ITargetLoadedCallBack() {

            @Override
            public void onTargetLoaded(String packageName) {
                View view = null;
                try {
                    Class<?> targetClass = ProxyEnvironment.getInstance(packageName).getDexClassLoader()
                            .loadClass(viewClass);
                    Constructor<?> constructor = targetClass.getConstructor(new Class<?>[] { Context.class });
                    view = (View) constructor.newInstance(ProxyEnvironment.getInstance(packageName).getApplication());
                } catch (Exception e) {
                    Log.e("TargetActivitor", "*** Create View Fail : \r\n" + e.getMessage());
                }
                callback.onViewCreated(packageName, view);
            }
        });

    }

    /**
     * 注销插件App
     * 
     * @param packageName
     *            插件包名
     */
    public static void unLoadTarget(String packageName) {
        ProxyEnvironment.exitProxy(packageName, true);
    }
    
    /**
     * 判断插件是否加载
     * 
     * @param pacakgeName
     *            包名
     * @return true or false
     */
    public static boolean isTargetLoaded(String pacakgeName) {
        return ProxyEnvironment.isEnterProxy(pacakgeName);
    }

    /**
     * 工具类，不需要构造方法
     */
    private TargetActivator() {

    }

    private static Map<Object,Object> mParameter = new HashMap<Object, Object>();

    /**
     * 获取公共参数
     * @return Map<Object,Object>
     */
    public static Map<Object,Object> getParameter(){
        return mParameter;
    }

    /**
     *设置公共参数
     * @param parameter
     *          Map<Object,Object>
     */
    public static void setParameter(Map<Object,Object> parameter){

        if( parameter.isEmpty()){
            return ;
        }
        if( mParameter.isEmpty()){
            mParameter = new HashMap<Object, Object>();
        }
        mParameter = parameter;
    }
}
