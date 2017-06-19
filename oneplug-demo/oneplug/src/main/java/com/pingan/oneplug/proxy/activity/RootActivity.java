package com.pingan.oneplug.proxy.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.pingan.oneplug.ProxyEnvironment;
import com.pingan.oneplug.api.ILoadingViewCreator;
import com.pingan.oneplug.externalapi.ForceMultiProcessPluginCenter;
import com.pingan.oneplug.install.IInstallCallBack;
import com.pingan.oneplug.ma.MAActivity;
import com.pingan.oneplug.pm.MAPackageManager;
import com.pingan.oneplug.util.ITargetLoadListenner;

/**
 * 
 */
public class RootActivity extends Activity {
    protected Context myContext = RootActivity.this;

    /** loading的背景 */
    private LinearLayout mRoot;


    @Override
    protected void onCreate(Bundle bundle) {
        overridePendingTransition(0, 0);
        super.onCreate(bundle);

        if (MAActivity.sActivityLifecycleCallbacks != null) {
            MAActivity.sActivityLifecycleCallbacks.onActivityCreated(this, bundle);
        }

        final String packageName = getIntent().getStringExtra(ProxyEnvironment.EXTRA_TARGET_PACKAGNAME);

        if (getIntent().getBooleanExtra(ProxyEnvironment.EXTRA_TARGET_REMOTE_PROCESS, false)) {
            ForceMultiProcessPluginCenter.addForceMultiProcessPluginProcessName(packageName);
        }

        String intents = getIntent().getStringExtra(ProxyEnvironment.EXTRA_TARGET_INTENTS);
        if (!TextUtils.isEmpty(intents)) {
            ProxyEnvironment.addGloadingMap(packageName, ProxyEnvironment.decodeIntentList(intents));
            getIntent().removeExtra(ProxyEnvironment.EXTRA_TARGET_INTENTS);
        }
        
        // 没在loading，说明是进程被杀死，系统把activity起来的，这时候就不处理了
        if (!ProxyEnvironment.isLoading(getApplicationContext(), packageName)) {
            finish();
            return;
        }
        
        ILoadingViewCreator creator = ProxyEnvironment.getLoadingViewCreator(packageName);

        mRoot = new LinearLayout(this);
        mRoot.setGravity(android.view.Gravity.CENTER);
        if (creator != null) {

            // 创建自定义loading
            mRoot.addView(creator.createLoadingView(getApplicationContext()));
            mRoot.setBackgroundColor(0xffffffff);
        } else {

            // 创建默认loading
            ProgressBar bar = new ProgressBar(this);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            barParams.gravity = android.view.Gravity.CENTER;
            mRoot.addView(bar, barParams);
        }
        setContentView(mRoot);

        final Context context = getApplicationContext();
        MAPackageManager.getInstance(context).packageAction(packageName, new IInstallCallBack() {
            
            @Override
            public void onPackageInstallFail(String packageName, String failReason) {
                ProxyEnvironment.clearLoadingIntent(context, packageName);
                finish();
            }
            
            @Override
            public void onPacakgeInstalled(String packageName) {

                ProxyEnvironment.initTarget(context, packageName, new ITargetLoadListenner() {

                    @Override
                    public void onLoadFinished(String packageName) {

                        // 启动插件主界面或指定的界面
                        Intent intent = new Intent(getIntent());
                        String targetActivity = getIntent()
                                .getStringExtra(ProxyEnvironment.EXTRA_TARGET_REDIRECT_ACTIVITY);
                        if (targetActivity == null) { // 防止空指针异常
                            targetActivity = "";
                        }
                        intent.setComponent(new ComponentName(packageName, targetActivity));
                        intent.removeExtra(ProxyEnvironment.EXTRA_TARGET_REDIRECT_ACTIVITY);
                        ProxyEnvironment.launchIntent(myContext, intent);
                        finish();
                    }
                });

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ProxyEnvironment.clearLoadingIntent(getIntent().getStringExtra(ProxyEnvironment.EXTRA_TARGET_PACKAGNAME));
        
        if (MAActivity.sActivityLifecycleCallbacks != null) {
            MAActivity.sActivityLifecycleCallbacks.onActivityDestroyed(this);
        }
    }

    @Override
    public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent) {
        if (paramKeyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(paramInt, paramKeyEvent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        if (MAActivity.sActivityLifecycleCallbacks != null) {
            MAActivity.sActivityLifecycleCallbacks.onActivityPaused(this);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        if (MAActivity.sActivityLifecycleCallbacks != null) {
            MAActivity.sActivityLifecycleCallbacks.onActivityResumed(this);
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        if (MAActivity.sActivityLifecycleCallbacks != null) {
            MAActivity.sActivityLifecycleCallbacks.onActivityStarted(this);
        }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        if (MAActivity.sActivityLifecycleCallbacks != null) {
            MAActivity.sActivityLifecycleCallbacks.onActivityStopped(this);
        }
    }
}
