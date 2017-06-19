package com.pingan.one.framework.demo.plugin;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.pingan.oneplug.ma.MAPreferenceActivity;

//import android.os.PersistableBundle;

/**
 * Created by zl on 2016/4/8.
 */
public class MyPreferenceActivity extends MAPreferenceActivity implements Preference.OnPreferenceChangeListener,Preference.OnPreferenceClickListener {

    CheckBoxPreference checkBoxPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.my_preference);

        checkBoxPreference = (CheckBoxPreference) findPreference("set_3g");
        checkBoxPreference.setOnPreferenceChangeListener(this);
        checkBoxPreference.setOnPreferenceClickListener(this);

    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        Log.e("zl5711", "onPreferenceClick = " + preference.getKey());
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.e("zl5711", "onPreferenceChange = " + preference.getKey());
        return false;
    }
}
