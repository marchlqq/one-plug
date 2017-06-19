package com.pingan.one.framework.demo.host;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.pingan.oneplug.api.TargetActivator;
import com.pingan.oneplug.pm.MAPackageManager;

/**
 * Created by zl on 2016/3/3.
 */
public class LoginActivity extends Activity {

    private String packageName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Intent intent = getIntent();
        packageName = intent.getStringExtra("packageName");

        (findViewById(R.id.bt)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(packageName)){
                    MAPackageManager.getInstance(LoginActivity.this).installBuildinApps();
                    TargetActivator.loadTargetAndRun(LoginActivity.this, packageName);
                    LoginActivity.this.finish();
                }else{
                    Toast.makeText(LoginActivity.this,"packageName is null!",Toast.LENGTH_LONG).show();
                }
            }
        });




    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
