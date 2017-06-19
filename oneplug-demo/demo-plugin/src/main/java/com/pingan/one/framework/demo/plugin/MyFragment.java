package com.pingan.one.framework.demo.plugin;


//import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.pingan.oneplug.ma.MAFragment;

//import android.support.v4.app.Fragment;

/**
 * Created by zl on 2016/4/12.
 */
public class MyFragment extends MAFragment {
//    public MyFragment(Context context){ };     //Simple constructor to use when creating a view from code
//
//    public MyFragment(Context context, AttributeSet attrs){} ;    //Constructor that is called when inflating a view from XML
//
//    public MyFragment(Context context, AttributeSet attrs, int defStyle){} ;    //Perform inflation from XML and apply a class-specific base style

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.title_fragment, container, false);

        view.findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getTargetActivity(), "This is Fragment !", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }


}
