package com.pingan.oneplug.ma;

import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.pingan.oneplug.adapter.ListActivityProxyAdapter;

public class MAListActivity extends MAActivity {
    private ListActivityProxyAdapter proxyActivity;

    public ListAdapter getListAdapter() {
        return this.proxyActivity.proxyGetListAdapter();
    }

    public ListView getListView() {
        return this.proxyActivity.proxyGetListView();
    }

    public long getSelectedItemId() {
        return this.proxyActivity.proxyGetSelectedItemId();
    }

    public int getSelectedItemPosition() {
        return this.proxyActivity.proxyGetSelectedItemPosition();
    }

    protected void onListItemClick(ListView paramListView, View paramView, int paramInt, long paramLong) {
        this.proxyActivity.proxyOnListItemClick(paramListView, paramView, paramInt, paramLong);
    }

    public void setActivityProxy(ListActivityProxyAdapter paramListActivityProxyAdapter) {
        super.setActivityProxy(paramListActivityProxyAdapter);
        this.proxyActivity = paramListActivityProxyAdapter;
    }

    public void setListAdapter(ListAdapter paramListAdapter) {
        this.proxyActivity.proxySetListAdapter(paramListAdapter);
    }

    public void setSelection(int paramInt) {
        this.proxyActivity.proxySetSelection(paramInt);
    }
}