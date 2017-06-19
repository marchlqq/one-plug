package com.pingan.one.framework.demo.plugin;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.pingan.oneplug.ma.MAActivity;

/**
 * Created by zl on 2016/4/6.
 */
public class TestActivity extends MAActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.test);

        findViewById(R.id.activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TestActivity.this.getActivity(), "This is Activity!", Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.list_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MyListActivity.class);
            }
        });

        findViewById(R.id.preference_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MyPreferenceActivity.class);
            }
        });

        findViewById(R.id.fragment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainFragment.class);
            }
        });

        findViewById(R.id.tab_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MyTabActivity.class);
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MainActivity.class);
            }
        });
    }

    private void startActivity(Class c){
        Intent intent = new Intent(this,c);
        this.startActivity(intent);
    }
}
