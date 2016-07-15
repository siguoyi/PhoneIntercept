package com.buptant.antcl.phoneintercept;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static String ACTION_UPDATE = "update";
    private ListView blackList;
    private TextView tv_title;
    private ArrayList<String> contractDate = new ArrayList<>();
    private ArrayList<String> contactsNumber = new ArrayList<>();
    private PhoneInterceptAdapter mAdapter;
    private SharedPreferences sharedPreferences;
    private Map map;
    private Map sortedMap;
    private int total;
    private UpdateReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        tv_title = (TextView) findViewById(R.id.tv_title);
        blackList = (ListView) findViewById(R.id.blacklist);
        sharedPreferences = getSharedPreferences("black_list", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        for(int i=0; i<5;i++){
//            editor.putString("" + i, "2016-04-20 12:57:0" + i);
//        }
//        editor.commit();
        map = sharedPreferences.getAll();
        if(map != null){
            sortedMap = sortMapByValue(map);
            if(sortedMap != null){
                total = sortedMap.size();
                title = getString(R.string.recent);
                tv_title.setText(title + "\n" + " (共计：" + total + "条)");
                Iterator entries = sortedMap.entrySet().iterator();
                while(entries.hasNext()){
                    Map.Entry entry = (Map.Entry) entries.next();
                    String phoneNumber = (String) entry.getKey();
                    String date = (String) entry.getValue();
                    if(phoneNumber != null){
                        Log.d(TAG, "phoneNumber: " + phoneNumber + " date: " + date);
                        contactsNumber.add(phoneNumber);
                        contractDate.add(date);
                    }
                }
            }
        }

        mAdapter = new PhoneInterceptAdapter(this, contractDate, contactsNumber);
        blackList.setAdapter(mAdapter);
        mReceiver = new UpdateReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ACTION_UPDATE);
        registerReceiver(mReceiver, mIntentFilter);
        Intent intent = new Intent(MainActivity.this, MainService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        new AlertDialog.Builder(this)
                .setTitle("清空陌生号码名单？")
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.clear();
                                editor.commit();
                                contactsNumber.clear();
                                contractDate.clear();
                                mAdapter.notifyDataSetChanged();
                                tv_title.setText(title);
                                Toast.makeText(MainActivity.this, "陌生号码名单已清空！", Toast.LENGTH_SHORT).show();
                            }
                        })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                            }
                        })
                .show();
        return true;
    }

    public static Map<String, String> sortMapByValue(Map<String, String> oriMap) {
        if (oriMap == null || oriMap.isEmpty()) {
            return null;
        }
        Map<String, String> sortedMap = new LinkedHashMap<String, String>();
        List<Map.Entry<String, String>> entryList = new ArrayList<Map.Entry<String, String>>(
                oriMap.entrySet());
        Collections.sort(entryList, new MapValueComparator());

        Iterator<Map.Entry<String, String>> iter = entryList.iterator();
        Map.Entry<String, String> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    static class MapValueComparator implements Comparator<Map.Entry<String, String>> {

        @Override
        public int compare(Map.Entry<String, String> me1, Map.Entry<String, String> me2) {

            return me2.getValue().compareTo(me1.getValue());
        }
    }

    class UpdateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ACTION_UPDATE)){
                contactsNumber.clear();
                contractDate.clear();
                map = sharedPreferences.getAll();
                if(map != null){
                    sortedMap = sortMapByValue(map);
                    if(sortedMap != null){
                        total = sortedMap.size();
                        title = getString(R.string.recent);
                        tv_title.setText(title + "\n" + " (共计：" + total + "条)");
                        Iterator entries = sortedMap.entrySet().iterator();
                        while(entries.hasNext()){
                            Map.Entry entry = (Map.Entry) entries.next();
                            String phoneNumber = (String) entry.getKey();
                            String date = (String) entry.getValue();
                            if(phoneNumber != null){
                                Log.d(TAG, "phoneNumber: " + phoneNumber + " date: " + date);
                                contactsNumber.add(phoneNumber);
                                contractDate.add(date);
                            }
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
