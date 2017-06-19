package com.pingan.oneplug.ma;

import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.pingan.oneplug.adapter.PreferenceActivityProxyAdapter;

public class MAPreferenceActivity extends MAListActivity {
    private PreferenceActivityProxyAdapter proxyActivity;

    public void addPreferencesFromIntent(Intent paramIntent) {
        this.proxyActivity.proxyAddPreferencesFromIntent(paramIntent);
    }

    public void addPreferencesFromResource(int paramInt) {
        this.proxyActivity.proxyAddPreferencesFromResource(paramInt);
    }

    public Preference findPreference(CharSequence paramCharSequence) {
        return this.proxyActivity.proxyFindPreference(paramCharSequence);
    }

    public PreferenceManager getPreferenceManager() {
        return this.proxyActivity.proxyGetPreferenceManager();
    }

    public PreferenceScreen getPreferenceScreen() {
        return this.proxyActivity.proxyGetPreferenceScreen();
    }

    public boolean onPreferenceTreeClick(PreferenceScreen paramPreferenceScreen, Preference paramPreference) {
        return false;
    }

    public void setActivityProxy(PreferenceActivityProxyAdapter paramPreferenceActivityProxyAdapter) {
        super.setActivityProxy(paramPreferenceActivityProxyAdapter);
        this.proxyActivity = paramPreferenceActivityProxyAdapter;
    }

    public void setPreferenceScreen(PreferenceScreen paramPreferenceScreen) {
        this.proxyActivity.proxySetPreferenceScreen(paramPreferenceScreen);
    }

}
