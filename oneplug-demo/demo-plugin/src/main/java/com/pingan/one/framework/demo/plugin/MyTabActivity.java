package com.pingan.one.framework.demo.plugin;

//import android.app.TabActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TabHost;

import com.pingan.oneplug.ma.MATabActivity;

/**
 * Created by zl on 2016/4/14.
 */
public class MyTabActivity extends MATabActivity {

    private TabHost tabHost;
    private int myMenuSettingTag = -1;

    private Menu myMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tabHost = this.getTabHost();
        LayoutInflater.from(this).inflate(R.layout.my_tabactivity, tabHost.getTabContentView(), true);
        tabHost.setBackgroundColor(Color.argb(150, 22, 70, 150));

        tabHost.addTab(tabHost.newTabSpec("jj").setIndicator("jj").setContent(R.id.tab_one));
        tabHost.addTab(tabHost.newTabSpec("kk").setIndicator("kk").setContent(R.id.tab_tow));
        tabHost.addTab(tabHost.newTabSpec("ll").setIndicator("ll").setContent(R.id.tab_three));

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                Log.e("zl5711", "tabId = " + tabId);
                if (tabId.equals("jj")) {
                    myMenuSettingTag = 1;
                }
                if (tabId.equals("kk")) {
                    myMenuSettingTag = 2;
                }
                if (tabId.equals("ll")) {
                    myMenuSettingTag = 3;
                }

            }
        });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        myMenu = menu;
        myMenu.clear();

        MenuInflater menuInflater = this.getMenuInflater();

        switch (myMenuSettingTag){
            case 1:
//                menuInflater.inflate(R.id.tab_one,menu);
                break;
            case 2:
                break;
            case 3:
                break;
            default:
                break;
        }



        return super.onCreateOptionsMenu(menu);
    }
}
