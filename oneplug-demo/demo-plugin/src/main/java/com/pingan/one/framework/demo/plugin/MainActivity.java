package com.pingan.one.framework.demo.plugin;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.pingan.oneplug.api.HostCallback;
import com.pingan.oneplug.api.ILoginCallback;
import com.pingan.oneplug.ma.MAActivity;
import com.pingan.oneplug.ma.Util;

public class MainActivity extends MAActivity {

    private BroadcastReceiver timeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("TIMETICK".equals(action)) {
                String timetick = intent.getStringExtra("TIMETICK");
                Log.e("zl5711","timetick === >" + timetick);
                ((TextView)findViewById(R.id.tv_timetick)).setText(timetick);
            }
        }
    };

    private TimeService.MyBinder myBinder;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (TimeService.MyBinder) service;
            myBinder.startTick();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((ImageView)findViewById(R.id.icon)).setImageResource(R.drawable.cheeseburger);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        findViewById(R.id.btn_eval).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String left = ((EditText) findViewById(R.id.et_left)).getText().toString();
                String right = ((EditText) findViewById(R.id.et_right)).getText().toString();
                try {
                    long result = Math.plus(Integer.parseInt(left), Integer.parseInt(right));
                    ((EditText) findViewById(R.id.et_result)).setText(result + "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.btn_json).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = ((EditText) findViewById(R.id.et_name)).getText().toString();
                String age = ((EditText) findViewById(R.id.et_age)).getText().toString();
                Person p = new Person(name, age);
                String json = JSON.toJSONString(p);
                Toast.makeText(getActivity(), json, Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.callback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "call LoginCallback", Toast.LENGTH_LONG).show();
                ILoginCallback iLoginCallback  =  HostCallback.getInstance().getILonginCallback();
                if(null != iLoginCallback) {
                    iLoginCallback.login(Util.getTargetPackageName(MainActivity.this));
                    MainActivity.this.finish();
                }
            }
        });

        findViewById(R.id.getparameter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Map map = TargetActivator.getParameter();
//                Toast.makeText(MainActivity.this, "map = " + map.toString(), Toast.LENGTH_LONG).show();
                MainActivity.this.unbindService(connection);
                MainActivity.this.stopService(new Intent(MainActivity.this, TimeService.class));
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction("TIMETICK");
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        MainActivity.this.registerReceiver(timeReceiver, filter, null, null);

        MainActivity.this.startService(new Intent(this, TimeService.class));

        Intent bindIntent = new Intent(this, TimeService.class);
        MainActivity.this.bindService(bindIntent, connection, BIND_AUTO_CREATE);



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
