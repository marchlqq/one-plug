package com.pingan.one.framework.demo.plugin;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pingan.oneplug.ma.MAListActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zl on 2016/4/6.
 */
public class MyListActivity extends MAListActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.setTheme(android.R.style.Theme_Black);
//        setContentView(R.layout.test);
        List<String> list = getlisr();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,list);
        this.setListAdapter(arrayAdapter);

    }
    private List<String> getlisr(){
        List<String> list = new ArrayList<String>();
        list.add("test1");
        list.add("test2");
        list.add("test3");
        list.add("test4");
        return list;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        TextView textView = (TextView) v;
        Toast.makeText(this, textView.getText(), Toast.LENGTH_LONG).show();
    }
}
