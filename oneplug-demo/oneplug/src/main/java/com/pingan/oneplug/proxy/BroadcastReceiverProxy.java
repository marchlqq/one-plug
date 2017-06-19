package com.pingan.oneplug.proxy;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.pingan.oneplug.ProxyEnvironment;
import com.pingan.oneplug.api.TargetActivator;
import com.pingan.oneplug.pm.MAPackageManager;
import com.pingan.oneplug.util.Util;

/**
 * 广播代理，插件的广播被代理发送
 */
public class BroadcastReceiverProxy extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, final Intent intent) {
        if (intent != null) {
            final String targetReceiver = intent.getStringExtra(ProxyEnvironment.EXTRA_TARGET_RECEIVER);
            final String targetPkgName = intent.getStringExtra(ProxyEnvironment.EXTRA_TARGET_PACKAGNAME);
            if (TextUtils.isEmpty(targetReceiver) || TextUtils.isEmpty(targetPkgName)) {
            	return;
            }
            if (!MAPackageManager.getInstance(context).isPackageInstalled(targetPkgName)) {
                return;
            }
            try {
                if (!ProxyEnvironment.isEnterProxy(targetPkgName)) { //插件没有启动的话，重新启动，且重新map Intent
                    Intent destIntent = new Intent(intent);
                    destIntent.setComponent(new ComponentName(targetPkgName, targetReceiver));
                    Util.genProxyExtIntent(this, destIntent);
                    TargetActivator.loadTargetAndRun(context, destIntent);
                } else {
                    BroadcastReceiver target = ((BroadcastReceiver) ProxyEnvironment.getInstance(targetPkgName)
                            .getDexClassLoader().loadClass(targetReceiver).asSubclass(BroadcastReceiver.class)
                            .newInstance());
                    target.onReceive(ProxyEnvironment.getInstance(targetPkgName).getApplication(), intent);
                }

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
