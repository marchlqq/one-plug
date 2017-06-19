package com.pingan.one.framework.demo.plugin;

//import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.pingan.oneplug.ma.MAActivity;
import com.pingan.oneplug.ma.MAFragmentActivity;


/**
 * Created by zl on 2016/4/12.
 */
public class MainFragment extends MAFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_fragment);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        MyFragment one = new MyFragment();
        ft.replace(R.id.my_fragmentlayout,one);

        MyFragmentTow tow = new MyFragmentTow();
        ft.replace(R.id.my_fragmentlayout2,tow);

//        ft.addToBackStack(null);
        ft.commit();

    }


}
