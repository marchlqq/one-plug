package com.pingan.oneplug.adapter;

import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public abstract interface PreferenceActivityProxyAdapter extends ListActivityProxyAdapter {
    public abstract void proxyAddPreferencesFromIntent(Intent paramIntent);

    public abstract void proxyAddPreferencesFromResource(int paramInt);

    public abstract Preference proxyFindPreference(CharSequence paramCharSequence);

    public abstract PreferenceManager proxyGetPreferenceManager();

    public abstract PreferenceScreen proxyGetPreferenceScreen();

    public abstract void proxySetPreferenceScreen(PreferenceScreen paramPreferenceScreen);

}