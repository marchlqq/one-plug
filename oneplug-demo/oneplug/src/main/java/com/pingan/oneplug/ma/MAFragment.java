package com.pingan.oneplug.ma;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.pingan.oneplug.proxy.activity.FragmentActivityProxy;

/**
 * 虚拟Fragment
 * 
 */
public class MAFragment extends Fragment {

    /**
     * 缺省的构造
     */
    public MAFragment() {
    }

    /**
     * 获取Fragment对应的Activity
     * 
     * @return Activity实例
     */
    public Context getTargetActivity() {
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity instanceof FragmentActivityProxy) {
            return ((FragmentActivityProxy) fragmentActivity).getMAActivity();
        }
        return fragmentActivity;
    }

}
