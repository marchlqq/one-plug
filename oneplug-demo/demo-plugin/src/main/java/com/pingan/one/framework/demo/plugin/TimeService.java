package com.pingan.one.framework.demo.plugin;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.pingan.oneplug.ma.MAService;

import java.text.SimpleDateFormat;

public class TimeService extends MAService {

    private static boolean falg = true;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("zl5711","TimeService ====== > onCreate ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        falg = false;
        Log.e("zl5711", "TimeService ====== > onDestroy ");
    }

    private MyBinder mBinder = new MyBinder();

    class MyBinder extends Binder {
        public void startTick() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (falg) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {

                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        java.util.Date date = new java.util.Date();
                        String timetick = sdf.format(date);

                        Intent newIntent = new Intent();
                        newIntent.setAction("TIMETICK");
                        newIntent.putExtra("TIMETICK", timetick);
                        sendBroadcast(newIntent, null);
                    }
                }
            }).start();
        }
    }
}
