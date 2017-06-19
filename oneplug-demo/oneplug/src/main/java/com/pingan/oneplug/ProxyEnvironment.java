package com.pingan.oneplug;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.pingan.oneplug.adapter.ActivityProxyAdapter;
import com.pingan.oneplug.api.ILoadingViewCreator;
import com.pingan.oneplug.externalapi.LatestRunningPlugin;
import com.pingan.oneplug.install.ApkInstaller;
import com.pingan.oneplug.install.IInstallCallBack;
import com.pingan.oneplug.ma.MAActivity;
import com.pingan.oneplug.ma.MAApplication;
import com.pingan.oneplug.ma.MAIntentService;
import com.pingan.oneplug.ma.MAService;
import com.pingan.oneplug.plug.ApkTargetMapping;
import com.pingan.oneplug.plug.TargetMapping;
import com.pingan.oneplug.pm.MAPackageManager;
import com.pingan.oneplug.proxy.BroadcastReceiverProxy;
import com.pingan.oneplug.proxy.BroadcastReceiverProxyExt;
import com.pingan.oneplug.proxy.ServiceProxy;
import com.pingan.oneplug.proxy.activity.ActivityProxy;
import com.pingan.oneplug.proxy.activity.RootActivity;
import com.pingan.oneplug.proxy.activity.RootActivityExt;
import com.pingan.oneplug.util.ClassLoaderInjectHelper;
import com.pingan.oneplug.util.Constants;
import com.pingan.oneplug.util.ITargetLoadListenner;
import com.pingan.oneplug.util.JavaCalls;
import com.pingan.oneplug.util.Util;

import dalvik.system.DexClassLoader;

public class ProxyEnvironment {

    /** DEBUG 开关 */
    public static final boolean DEBUG = true & Constants.DEBUG;
    /** TAG */
    public static final String TAG = "ProxyEnvironment";

    public static final String EXTRA_TARGET_ACTIVITY = "oneplug_extra_target_activity";
    public static final String EXTRA_TARGET_SERVICE = "oneplug_extra_target_service";
    public static final String EXTRA_TARGET_RECEIVER= "oneplug_extra_target_receiver";
    public static final String EXTRA_TARGET_PACKAGNAME = "oneplug_extra_target_pacakgename";
    // public static final String EXTRA_TARGET_ISBASE = "oneplug_extra_target_isbase";
    public static final String EXTRA_TARGET_REDIRECT_ACTIVITY = "oneplug_extra_target_redirect_activity";
    public static final String EXTRA_TARGET_REDIRECT_ISSILENCE = "oneplug_extra_target_redirect_isSilence";
    public static final String EXTRA_VALUE_LOADTARGET_STUB = "oneplug_loadtarget_stub";
    public static final String EXTRA_TARGET_PROXY_EXT = "oneplug_extra_target_proxy_ext";
    
    public static final String EXTRA_TARGET_INTENTS = "oneplug_extra_target_intents";

    // public static final String GLOADING_MAP_FILENAME = "oneplug_loading_file";

    public static final String EXTRA_TARGET_REMOTE_PROCESS = "oneplug_extra_target_ext";


    /** Oneplug开关:data是否和host的路径相同，默认独立路径 TODO 待实现 */
    public static final String META_KEY_DATAINHOST = "oneplug_cfg_datainhost";
    /** Oneplug开关:data是否【去掉】包名前缀，默认加载包名前缀 */
    public static final String META_KEY_DATA_WITHOUT_PREFIX = "oneplug_cfg_data_without_prefix";
    /** Oneplug开关：class是否注入到host，默认不注入 */
    public static final String META_KEY_CLASSINJECT = "oneplug_class_inject";

    /** 插件加载成功的广播 */
    public static final String ACTION_TARGET_LOADED = "com.pingan.oneplug.action.TARGET_LOADED";

    /** 插件包名对应Environment的Hash */
    private static HashMap<String, ProxyEnvironment> sPluginsMap = new HashMap<String, ProxyEnvironment>();

    private final Context context;
    private static Context sAppContext;
    private final File apkFile;

    private ClassLoader dexClassLoader;
    private Resources targetResources;
    private AssetManager targetAssetManager;
    private Theme targetTheme;
    private TargetMapping targetMapping;
    /** data文件是否需要加前缀 */
    private boolean bIsDataNeedPrefix = true;
    private String parentPackagename;
    /** 插件的Activity栈 */
    private LinkedList<Activity> activityStack;
    /** 插件虚拟的Application实例 */
    private MAApplication application;
    /** 插件数据根目录 */
    private File targetDataRoot;
    /** 是否初始化了插件Application */
    private boolean bIsApplicationInit = false;

    /** 初始化是否成功 */
    private boolean initSuccess = true;
    
    /** Loading Map，正在loading中的插件 */
    private static Map<String, List<Intent>> gLoadingMap = new HashMap<String, List<Intent>>();
    /** 插件loading样式的创建器 */
    private static Map<String, ILoadingViewCreator> gLoadingViewCreators = new HashMap<String, ILoadingViewCreator>();

    /**
     * 构造方法，解析apk文件，创建插件运行环境
     * 
     * @param context
     *            host application context
     * @param apkFile
     *            插件apk文件
     */
    private ProxyEnvironment(Context context, File apkFile) {
        this.context = context.getApplicationContext();
        sAppContext = context;
        this.apkFile = apkFile;
        activityStack = new LinkedList<Activity>();
        parentPackagename = context.getPackageName();
        assertApkFile();
        createTargetMapping();
        createDataRoot();
        createClassLoader();
        createTargetResource();
        addPermissions();

    }

    /**
     * data文件是否需要加前缀
     * 
     * @return true or false
     */
    public boolean isDataNeedPrefix() {
        return bIsDataNeedPrefix;
    }

    /**
     * 获取插件数据根路径
     * 
     * @return 根路径文件
     */
    public File getTargetDataRoot() {
        return targetDataRoot;
    }

    /**
     * 获取插件apk路径
     * 
     * @return 绝对路径
     */
    public String getTargetPath() {
        return this.apkFile.getAbsolutePath();
    }

    /**
     * 获取插件lib的绝对路径
     * 
     * @return 绝对路径
     */
    public String getTargetLibPath() {
        return new File(targetDataRoot, ApkInstaller.NATIVE_LIB_PATH).getAbsolutePath();
    }

    /**
     * 获取插件运行环境实例，调用前保证已经初始化，否则会抛出异常
     * 
     * @param packageName
     *            插件包名
     * @return 插件环境对象
     */
    public static ProxyEnvironment getInstance(String packageName) {
        ProxyEnvironment env = null;
        if (packageName != null) {
            env = sPluginsMap.get(packageName);
        }
        if (env == null) {
            throw new IllegalArgumentException(packageName +" not loaded, Make sure you have call the init method!");
        }
        return env;
    }

    /**
     * 是否已经建立对应插件的environment
     * 
     * @param packageName
     *            包名，已经做非空判断
     * @return true表示已经建立
     */
    public static boolean hasInstance(String packageName) {
        if (packageName == null) {
            return false;
        }
        return sPluginsMap.containsKey(packageName);
    }

    /**
     * 插件是否已经进入了代理模式
     * 
     * @param packageName
     *            插件包名
     * @return true or false
     */
    public static boolean isEnterProxy(String packageName) {
        if (packageName == null) {
            return false;
        }
        synchronized (gLoadingMap) {
            ProxyEnvironment env = sPluginsMap.get(packageName);
            if (env != null && env.bIsApplicationInit) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * 清除等待队列，防止异常情况，导致所有Intent都阻塞在等待队列，插件再也起不来就杯具了
     * 
     * @param packageName
     *            包名
     */
    public static void clearLoadingIntent(Context context, String packageName) {
        if (packageName == null) {
            return;
        }

        synchronized (gLoadingMap) {
            gLoadingMap.remove(packageName);
        }
    }

    /**
     * 设置插件加载的loadingView creator
     * 
     * @param packageName
     *            插件包名
     * @param creator
     *            loadingview creator
     */
    public static void putLoadingViewCreator(String packageName, ILoadingViewCreator creator) {
        if (packageName == null) {
            return;
        }
        gLoadingViewCreators.put(packageName, creator);
    }

    /**
     * 获取插件加载的loadingview creator
     * 
     * @param packageName
     *            插件包名
     * @return creator
     */
    public static ILoadingViewCreator getLoadingViewCreator(String packageName) {
        if (packageName == null) {
            return null;
        }
        return gLoadingViewCreators.get(packageName);
    }

    /**
     * 插件是否正在loading中
     * 
     * @param packageName
     *            插件包名
     * @return true or false
     */
    public static boolean isLoading(Context context, String packageName) {
        if (packageName == null) {
            return false;
        }
        
        boolean ret = false;
        synchronized (gLoadingMap) {
            ret = gLoadingMap.containsKey(packageName);
        }
        return ret;
    }

    /**
     * 判断是否加载或者已经加载了，调用要加gLoadingMap作为锁
     * 
     * @param packageName
     *            包名
     * @return true or false
     */
    private static boolean isLoadingOrLoaded(String packageName) {
        if (packageName == null) {
            return false;
        }

        boolean ret = false;

        // 加载中
        if (gLoadingMap.containsKey(packageName)) {
            ret = true;
        }

        // 已经加载
        if (sPluginsMap.containsKey(packageName) && sPluginsMap.get(packageName).bIsApplicationInit) {
            ret = true;
        }
        return ret;
    }
    
    /**
     * 添加正在加载的插件到map中
     * @param packageName 插件包名
     * @param list 插件加载完成后发送的intent列表
     */
    public static synchronized void addGloadingMap(String packageName, List<Intent> list) {
        gLoadingMap.put(packageName, list);
    }

    /**
     * 运行插件代理
     * 
     * @param context
     *            host 的application context
     * @param intent
     *            加载插件运行的intent
     */
    public static void enterProxy(final Context context, final Intent intent) {
        final String packageName = intent.getComponent().getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            throw new RuntimeException("*** loadTarget with null packagename!");
        }
        
        boolean isEnterProxy = false;
        synchronized (gLoadingMap) {
        List<Intent> cacheIntents = gLoadingMap.get(packageName);            
            
            if (cacheIntents != null) {

                // 正在loading，直接返回吧，等着loading完调起
                // 把intent都缓存起来
                cacheIntents.add(intent);
                return;
            }

            isEnterProxy = isEnterProxy(packageName);
            if (!isEnterProxy) {
                List<Intent> intents = new ArrayList<Intent>();
                intents.add(intent);
                gLoadingMap.put(packageName, intents);
            }
        }
        
        if (isEnterProxy) {
            
            // 已经初始化，直接起Intent
            launchIntent(context, intent);
            return;
        }

        boolean isSilent = intent.getBooleanExtra(EXTRA_TARGET_REDIRECT_ISSILENCE, false);

        if (isSilent) {
            MAPackageManager.getInstance(context.getApplicationContext()).packageAction(packageName,
                    new IInstallCallBack() {

                        @Override
                        public void onPackageInstallFail(String packageName, String failReason) {
                            clearLoadingIntent(context, packageName);
                        }

                        @Override
                        public void onPacakgeInstalled(String packageName) {

                            initTarget(context.getApplicationContext(), packageName, new ITargetLoadListenner() {

                                @Override
                                public void onLoadFinished(String packageName) {
                                    launchIntent(context, intent);
                                }
                            });
                        }
                    });
        } else {
            // 出loading界面吧
            Intent newIntent = new Intent(intent);
            boolean isUseExt = com.pingan.oneplug.ma.Util.isUseExt(context.getApplicationContext(), newIntent);
            boolean isMainProcess = TextUtils.equals(context.getPackageName(), 
                    Util.getCurProcessName(context.getApplicationContext()));
            if (isUseExt == isMainProcess) {
                synchronized(gLoadingMap) {
                    List<Intent> list = gLoadingMap.remove(packageName);
                    newIntent.putExtra(ProxyEnvironment.EXTRA_TARGET_INTENTS, encodeIntentList(list));
                }
            }
            
            if(isUseExt) {
                newIntent.setClass(context, RootActivityExt.class);
                newIntent.putExtra(ProxyEnvironment.EXTRA_TARGET_REMOTE_PROCESS, true);
            } else {
                newIntent.setClass(context, RootActivity.class);
            }
            if (!(context instanceof Activity)) {
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            newIntent.putExtra(ProxyEnvironment.EXTRA_TARGET_PACKAGNAME, packageName);
            newIntent.putExtra(ProxyEnvironment.EXTRA_TARGET_ACTIVITY, MAActivity.class.getName());
            newIntent.putExtra(ProxyEnvironment.EXTRA_TARGET_REDIRECT_ACTIVITY, intent.getComponent().getClassName());
            context.startActivity(newIntent);
        }
    }
    
    
    /**
     * 将intent列表转换成相应的字符串
     * @param intentListStr intent列表的字符串
     * @return intent列表
     */
    public static String encodeIntentList(List<Intent> list) {
        if (list == null) {
            return null;
        }
        JSONArray array = new JSONArray();
        for (Intent intent : list) {
            array.put(intent.toURI());
        }
        return array.toString();
    }
    
    /**
     * 将intent列表的字符串表示转换成list列表
     * @param intentListStr intent列表的字符串
     * @return intent列表
     */
    public static List<Intent> decodeIntentList(String intentListStr) {
        List<Intent> list = new ArrayList<Intent>();
        if (TextUtils.isEmpty(intentListStr)) {
            return list;
        }
        
        try {
            JSONArray array = new JSONArray(intentListStr);
            for (int i = 0; i < array.length(); i++) {
                String intentStr = array.getString(i);
                try {
                    Intent intent = Intent.parseUri(intentStr, 0);
                    list.add(intent);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 进入代理模式
     * 
     * @param context
     *            host 的 application context
     * @throws Exception
     *             调用逻辑异常
     * 
     * @return true表示启动了activity，false表示启动失败，或者启动非activity
     */
    public static boolean launchIntent(Context context, Intent intent) {
        try {
            String packageName = intent.getComponent().getPackageName();
            ProxyEnvironment env = sPluginsMap.get(packageName);
            if (env == null) {
                clearLoadingIntent(context, packageName);
                if (DEBUG) {
                    Log.w(TAG, "### launchIntent while env removed!");
                }
                return false;
            }

            List<Intent> cacheIntents = null;
            if (!env.bIsApplicationInit && env.application == null) {

                // Application 创建
                String className = env.targetMapping.getApplicationClassName();
                MAApplication app = null;
                if (className == null || "".equals(className) || Application.class.getName().equals(className)) {

                    // 创建默认的虚拟Application
                    app = new MAApplication();
                } else {

                    try {
                        app = ((MAApplication) env.dexClassLoader.loadClass(className)
                                .asSubclass(MAApplication.class)
                                .newInstance());
                    } catch (Exception e) {
                        // throw new RuntimeException(e.getMessage(), e);
                        // FIXME: 此处不抛出异常，直接返回false，表示加载插件失败
                        e.printStackTrace();
                        return false;
                    }
                }

                app.setApplicationProxy((Application) env.context);
                app.setTargetPackageName(packageName);
                app.onCreate();

                synchronized (gLoadingMap) {
                    if (sPluginsMap.get(packageName) != env) {
                        return false;
                    }
                    env.application = app;
                    env.bIsApplicationInit = true;
                    cacheIntents = gLoadingMap.remove(packageName);
                }
            }
            LatestRunningPlugin.setLastestRunningPlugin(packageName);

            if (cacheIntents == null) {

                // 没有缓存的Intent，取当前的Intent;
                cacheIntents = new ArrayList<Intent>();
                cacheIntents.add(intent);
            }

            boolean haveLaunchActivity = false;
            for (Intent curIntent : cacheIntents) {

                // 获取目标class
                String targetClassName = "";
                if (curIntent.getComponent() != null) {
                    targetClassName = curIntent.getComponent().getClassName();
                }
                if (TextUtils.equals(targetClassName, EXTRA_VALUE_LOADTARGET_STUB)) {

                    // 表示后台加载，不需要处理该Intent
                    continue;
                }
                if (TextUtils.isEmpty(targetClassName)) {
                    targetClassName = env.getTargetMapping().getDefaultActivityName();
                }

                // 处理启动的是service
                Class<?> targetClass;
                try {
                    targetClass = env.dexClassLoader.loadClass(targetClassName);
                } catch (Exception e) {
                    targetClass = MAActivity.class;
                }
                if (MAIntentService.class.isAssignableFrom(targetClass) || MAService.class.isAssignableFrom(targetClass)) {
                    env.remapStartServiceIntent(curIntent, targetClassName);
                    context.startService(curIntent);

                } else if (BroadcastReceiver.class.isAssignableFrom(targetClass)) { // 发一个内部用的动态广播
                    Intent newIntent = new Intent(curIntent);
                    newIntent.setComponent(null);
                    newIntent.putExtra(EXTRA_TARGET_PACKAGNAME, packageName);
                    newIntent.setPackage(context.getPackageName());
                    context.sendBroadcast(newIntent, MAPackageManager.getBroadcastPermission(context));
                } else {
                    Intent newIntent = new Intent(curIntent);
                    newIntent.setClass(context, ActivityProxy.class);
                    if (!(context instanceof Activity)) {
                        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    env.remapStartActivityIntent(newIntent, targetClassName);
                    context.startActivity(newIntent);

                    haveLaunchActivity = true;
                }
            }

            return haveLaunchActivity;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 初始化插件的运行环境，如果已经初始化，则什么也不做
     * 
     * @param context
     *            application context
     * @param packageName
     *            插件包名
     */
    public static synchronized void initProxyEnvironment(Context context, String packageName) {
        if (sPluginsMap.containsKey(packageName)) {
            return;
        }
        ProxyEnvironment newEnv = new ProxyEnvironment(context, ApkInstaller.getInstalledApkFile(context, packageName));
        sPluginsMap.put(packageName, newEnv);
    }

    /**
     * 退出插件
     * 
     * @param packageName
     *            插件包名
     * @return ture表示退出成功，false表示失败
     */
    public static boolean exitProxy(String packageName) {
        return exitProxy(packageName, false);
    }

    /**
     * 退出插件
     * 
     * @param packageName
     *            插件包名
     * @param force
     *            是否强制退出
     * @return ture表示退出成功，false表示失败
     */
    public static boolean exitProxy(String packageName, boolean force) {
        if (packageName == null) {
            return true;
        }


        ProxyEnvironment env = null;
        synchronized (gLoadingMap) {
            if (!force && isLoadingOrLoaded(packageName)) {
                return false;
            }

            env = sPluginsMap.get(packageName);
            if (env == null || env.application == null) {
                return true;
            }

            if (env.bIsApplicationInit) {
                env.application.onTerminate();
            }
            env.ejectClassLoader();
            sPluginsMap.remove(packageName);
        }

        return true;
    }

    /**
     * 当前是否运行在插件代理模式
     * 
     * @return true or false
     */
    public static boolean isProxyMode() {
        return sPluginsMap.size() > 0;
    }

    /**
     * 获取插件的classloader
     * 
     * @return classloader
     */
    public ClassLoader getDexClassLoader() {
        return dexClassLoader;
    }

    /**
     * 获取插件资源
     * 
     * @return 资源对象
     */
    public Resources getTargetResources() {
        return targetResources;
    }

    public AssetManager getTargetAssetManager() {
        return targetAssetManager;
    }

    public Theme getTargetTheme() {
        return targetTheme;
    }

    public TargetMapping getTargetMapping() {
        return targetMapping;
    }

    public String getTargetPackageName() {
        return targetMapping.getPackageName();
    }

    public void remapStartServiceIntent(Intent originIntent) {

        // 隐式启动Service不支持
        if (originIntent.getComponent() == null) {
            return;
        }

        String targetActivity = originIntent.getComponent().getClassName();
        remapStartServiceIntent(originIntent, targetActivity);
    }

    public void remapStartActivityIntent(Intent originIntent) {
        // 启动系统的Activity，例如卸载、安装，getComponent 为null。这样的Intent，不需要remap
        if (originIntent.getComponent() != null) {
            String targetActivity = originIntent.getComponent().getClassName();
            remapStartActivityIntent(originIntent, targetActivity);
        }
    }

    public void pushActivityToStack(Activity activity) {
        activityStack.addFirst(activity);
    }

    public boolean popActivityFromStack(Activity activity) {
        if (!activityStack.isEmpty()) {
            return activityStack.remove(activity);
        } else {
            return false;
        }
    }

    public void dealLaunchMode(Intent intent) {
        String targetActivity = intent.getStringExtra(EXTRA_TARGET_ACTIVITY);
        if (targetActivity == null) {
            return;
        }

        // 其他模式不支持，只支持single top和 single task
        ActivityInfo info = targetMapping.getActivityInfo(targetActivity);
        if (info.launchMode == ActivityInfo.LAUNCH_SINGLE_TOP) {

            // 判断栈顶是否为需要启动的Activity
            Activity activity = null;
            if (!activityStack.isEmpty()) {
                activity = activityStack.getFirst();
            }
            if (activity instanceof ActivityProxyAdapter) {
                ActivityProxyAdapter adp = (ActivityProxyAdapter) activity;
                Object ma = adp.getTarget();
                if (ma != null && TextUtils.equals(targetActivity, ma.getClass().getName())) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                }
            }
        } else if (info.launchMode == ActivityInfo.LAUNCH_SINGLE_TASK) {

            Activity found = null;
            Iterator<Activity> it = activityStack.iterator();
            while (it.hasNext()) {
                Activity activity = it.next();
                if (activity instanceof ActivityProxyAdapter) {
                    ActivityProxyAdapter adp = (ActivityProxyAdapter) activity;
                    Object ma = adp.getTarget();
                    if (ma != null && TextUtils.equals(targetActivity, ma.getClass().getName())) {
                        found = activity;
                        break;
                    }
                }
            }

            // 栈中已经有当前activity
            if (found != null) {
                Iterator<Activity> iterator = activityStack.iterator();
                while (iterator.hasNext()) {
                    Activity activity = iterator.next();
                    if (activity == found) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        break;
                    }

                    activity.finish();
                }
            }
        } else if(info.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }

    }

    public void remapStartActivityIntent(Intent intent, String targetActivity) {

        // 获取不到activity info，说明不是插件的Activity，不需要重映射
        if (targetMapping.getActivityInfo(targetActivity) == null) {
            return;
        }

        intent.putExtra(EXTRA_TARGET_ACTIVITY, targetActivity);
        intent.putExtra(EXTRA_TARGET_PACKAGNAME, targetMapping.getPackageName());
        Class<?> clazz = getRemapedActivityClass(targetMapping.getPackageName(), targetActivity);
        if (clazz != null) {
            intent.setClass(context, clazz);
        }

        // 实现launch mode，目前支持singletop和
        dealLaunchMode(intent);
    }


    /**
     * 获取重映射之后的Activity类
     * 
     * @param packageName
     *            插件packageName
     * @param targetActivity
     *            插件Activity类
     * @return 返回代理Activity类
     */
    public Class<?> getRemapedActivityClass(String packageName, String targetActivity) {
        Class<?> targetClass;
        try {
            targetClass = dexClassLoader.loadClass(targetActivity);
        } catch (Exception e) {
            targetClass = MAActivity.class;
        }
        int theme = targetMapping.getActivityInfo(targetActivity).theme;
        Class<?> clazz = ProxyActivityCounter.getInstance().getNextAvailableActivityClass(targetClass, theme, 
                com.pingan.oneplug.ma.Util.isUseExt(context, packageName));

        return clazz;
    }

    public void remapStartServiceIntent(Intent intent, String targetService) {
        if (targetMapping.getServiceInfo(targetService) == null) {
            return;
        }
        intent.putExtra(EXTRA_TARGET_SERVICE, targetService);
        intent.putExtra(EXTRA_TARGET_PACKAGNAME, targetMapping.getPackageName());
        Class<?> targetClass;
        try {
            targetClass = dexClassLoader.loadClass(targetService);
        } catch (Exception e) {
            targetClass = MAService.class;
        }
        Class<?> proxyClass = ServiceProxy.class;
        try {
            proxyClass = ProxyServiceCounter.getInstance().getAvailableService(targetClass, 
                    com.pingan.oneplug.ma.Util.isUseExt(context, targetMapping.getPackageName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        intent.setClass(context, proxyClass);
    }
    
    /**
     * @param originIntent
     */
    public void remapReceiverIntent(Intent originIntent) {
        if (originIntent.getComponent() != null) {
            String targetReceiver = originIntent.getComponent().getClassName();
            originIntent.putExtra(EXTRA_TARGET_RECEIVER, targetReceiver);
            originIntent.putExtra(EXTRA_TARGET_PACKAGNAME, targetMapping.getPackageName());
            if (false == com.pingan.oneplug.ma.Util.isUseExt(context, targetMapping.getPackageName())) {
                originIntent.setClass(context, BroadcastReceiverProxy.class);
            } else {
                originIntent.setClass(context, BroadcastReceiverProxyExt.class);
            }
        }
    }

    
    private void assertApkFile() {
        boolean isApk = apkFile.isFile() && apkFile.getName().endsWith(ApkInstaller.APK_SUFFIX);
        if (!isApk) {
            throw new IllegalArgumentException("target file is not an apk " + apkFile.getAbsolutePath());
        }
    }

    /**
     * 创建数据根路径
     */
    private void createDataRoot() {

        // TODO 这里需要考虑插件升级后MetaData的配置改变，data路径随之改变，是否需要保存数据
        if (targetMapping != null 
                && targetMapping.getMetaData() != null 
                && targetMapping.getMetaData().getBoolean(META_KEY_DATAINHOST)) {
            targetDataRoot = new File(context.getFilesDir().getParent());
        } else {
            try {
                targetDataRoot = getDataDir(context, targetMapping.getPackageName());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage() + " for plugin " + apkFile.getAbsolutePath());
            }
        }
        targetDataRoot.mkdirs();

        if (!(targetDataRoot.canRead() && targetDataRoot.canWrite())) {
            targetDataRoot.setWritable(true);
            targetDataRoot.setReadable(true);
        }
        
//        PackageInfo packageinfo = targetMapping.getPackageInfo();
//        if (packageinfo != null && packageinfo.applicationInfo != null) {
//            packageinfo.applicationInfo.dataDir = targetDataRoot.getAbsolutePath();
//        }

        targetMapping.setApplicationInfoDataDir(targetDataRoot.getAbsolutePath());
    }
    /**
     * 获取某个插件的 data 根目录。
     * @param packageName
     * @return
     */
    public static File getDataDir(Context context, String packageName) {
       File file = new File(ApkInstaller.getOneplugRootPath(context), packageName);
       return file;
    }

    /**
     * 创建ClassLoader， 需要在 createDataRoot之后调用
     */
    private void createClassLoader() {
        boolean isAppContextClsLoader = true;

        if (super.getClass().getClassLoader() != sAppContext.getClass().getClassLoader()) {
            Log.w(TAG,  "class loader need to be consistent " + sAppContext.getClassLoader() + " super "
                    + super.getClass().getClassLoader());
            isAppContextClsLoader = false;
        }
        dexClassLoader = new DexClassLoader(apkFile.getAbsolutePath(), targetDataRoot.getAbsolutePath(),
                getTargetLibPath(),
                super.getClass().getClassLoader());

        // 把 插件 classloader 注入到 host程序中，方便host app 能够找到 插件 中的class。因为 Intent put
        // serialize extra避免不开了！！！
        if (targetMapping.getMetaData() != null && targetMapping.getMetaData().getBoolean(META_KEY_CLASSINJECT)) {
            if (isAppContextClsLoader) {
                String packageName = targetMapping.getPackageName();
                // TODO: remove this ugly code
                if (packageName.contains("searchbox.reader")) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                        return;
                    }
                }
                ClassLoaderInjectHelper.inject(context.getClassLoader(), dexClassLoader, targetMapping.getPackageName()
                        + ".R");

                if (DEBUG) {
                    Log.i(TAG, "--- Class injecting @ " + targetMapping.getPackageName());
                }
            }
        }
    }


    /**
     * 如果注入了classloader，执行反注入操作。用于卸载时。
     */
    public void ejectClassLoader() {
        if (dexClassLoader != null
                && targetMapping.getMetaData() != null 
                && targetMapping.getMetaData().getBoolean(META_KEY_CLASSINJECT)) {
            ClassLoaderInjectHelper.eject(context.getClassLoader(), dexClassLoader);
        }
    }

    private void createTargetResource() {
        try {
            AssetManager am = (AssetManager) AssetManager.class.newInstance();
            JavaCalls.callMethod(am, "addAssetPath", new Object[] { apkFile.getAbsolutePath() });
            targetAssetManager = am;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 解决一个HTC ONE X上横竖屏会黑屏的问题
        Resources hostRes = context.getResources();
        Configuration config = new Configuration();
        config.setTo(hostRes.getConfiguration());
        config.orientation = Configuration.ORIENTATION_UNDEFINED;
        targetResources = new ResourcesProxy(targetAssetManager, hostRes.getDisplayMetrics(), config, hostRes);
        targetTheme = targetResources.newTheme();
        targetTheme.setTo(context.getTheme());
        targetTheme.applyStyle(targetMapping.getTheme(), true);
    }

    /**
     * 构建{@link TargetMapping}
     *
     * @return true，构建成功
     */
    private boolean createTargetMapping() {
        targetMapping = new ApkTargetMapping(context, apkFile);
        if (!targetMapping.initSuccess()) {
            initSuccess = false;
            return false;
        }

        bIsDataNeedPrefix = targetMapping.getMetaData() == null
                || !targetMapping.getMetaData().getBoolean(META_KEY_DATA_WITHOUT_PREFIX);

        return true;
    }

    private void addPermissions() {
        // TODO add permissions
    }

    /**
     * @return the parentPackagename
     */
    public String getParentPackagename() {
        return parentPackagename;
    }

    public int getTargetActivityThemeResource(String activity) {
        return targetMapping.getThemeResource(activity);
    }

    /**
     * 获取插件Activity的屏幕方向
     * 
     * @param activity
     *            activity类名
     * @return 屏幕方向
     */
    public int getTargetActivityOrientation(String activity) {
        return targetMapping.getActivityInfo(activity).screenOrientation;
    }

    /**
     * @return the application
     */
    public MAApplication getApplication() {
        return application;
    }

    public int getHostResourcesId(String resourcesName,String resourceType) {
        if (context != null) {
            return context.getResources().getIdentifier(resourcesName, resourceType, context.getPackageName());
        }
        return 0;
    }
   
    /**
     * 退出某个应用。不是卸载插件应用
     * @param packageName
     */
    public void quitApp() {
        while (!activityStack.isEmpty()) {
            activityStack.poll().finish();
        }
        activityStack.clear();
    }
    
    /**
     * 加载插件
     * 
     * @param context
     *            application Context
     * @param packageName
     *            插件包名
     * @param listenner
     *            插件加载后的回调
     */
    public static void initTarget(final Context context, String packageName, final ITargetLoadListenner listenner) {
        new AsyncTask<String, Integer, String>() {

            @Override
            protected String doInBackground(String... params) {
                String pPackageName = params[0];
                ProxyEnvironment.initProxyEnvironment(context, pPackageName);
                return pPackageName;
            };

            @Override
            protected void onPostExecute(String result) {
                listenner.onLoadFinished(result);
                super.onPostExecute(result);
            }
        }.execute(packageName);
    }

}
