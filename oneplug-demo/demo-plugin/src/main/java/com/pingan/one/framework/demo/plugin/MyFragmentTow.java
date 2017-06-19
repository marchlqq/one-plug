package com.pingan.one.framework.demo.plugin;

//import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pingan.oneplug.ma.MAFragment;

//import android.support.v4.app.Fragment;

/**
 * Created by zl on 2016/4/12.
 */
public class MyFragmentTow extends MAFragment {


//    public MyFragmentTow(Context context){ };   //Simple constructor to use when creating a view from code
//
//    public MyFragmentTow(Context context, AttributeSet attrs) { };    //Constructor that is called when inflating a view from XML
//
//    public MyFragmentTow(Context context, AttributeSet attrs, int defStyle) { };    //Perform inflation from XML and apply a class-specific base style

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.commont_fragment,container,false);
    }
}
