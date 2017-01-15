package com.gree.lkl.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.PathClassLoader;

public class MainActivity extends Activity {

    private Spinner spinner;
    private ImageView iv;
    HashMap<String, String> curplus;
    private ArrayList<HashMap<String, String>> data;
    private SimpleAdapter adapter;
    private Button bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        data = new ArrayList<>();
        spinner = (Spinner) findViewById(R.id.spinner);
        iv = (ImageView) findViewById(R.id.background);
        adapter = new SimpleAdapter(this, data, R.layout.spinner_item, new String[]{"label"}, new int[]{R.id.tv});
        spinner.setAdapter(adapter);
        bt = (Button) findViewById(R.id.bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPluginActivity(curplus);
            }
        });
        findPluginList();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                curplus = data.get(position);
                int resId = findResourceId(curplus);
                try {
                    Context pluginContext = findPluginContext(curplus);
                    Drawable drawable = pluginContext.getResources().getDrawable(resId);
                    iv.setImageDrawable(drawable);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void startPluginActivity(Map<String, String> map) {
        try {
            Context pluginContext = findPluginContext(map);
            String packageName = map.get("packageName");
            ClassLoader classLoader = new PathClassLoader(pluginContext.getPackageResourcePath(), ClassLoader.getSystemClassLoader());
            Class activityClass = Class.forName(packageName + ".MainActivity", true, classLoader);
//            Intent intent = new Intent(this, activityClass);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//            pluginContext.startActivity(intent);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Context findPluginContext(Map<String, String> map) throws PackageManager.NameNotFoundException {
        return this.createPackageContext(map.get("packageName"), 0);
    }

    public int findResourceId(Map<String, String> map) {
        String packageName = map.get("packageName");
        try {
            Context pluginContext = findPluginContext(map);
            PathClassLoader classLoader = new PathClassLoader(pluginContext.getPackageResourcePath(), PathClassLoader.getSystemClassLoader());
            Class clazz = Class.forName(packageName + ".R$mipmap", true, classLoader);
            Field[] fields = clazz.getFields();
            for (Field field : fields) {
                String name = field.getName();
                if (name.equals("ic_launcher")) {
                    return field.getInt(R.drawable.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void findPluginList() {
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(PackageManager.GET_ACTIVITIES);
        try {
            PackageInfo currentPackageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            for (PackageInfo info : packages) {
                String packageName = info.packageName;
                String shareUserId = info.sharedUserId;
                if (shareUserId == null || !shareUserId.equals(currentPackageInfo.sharedUserId) || packageName.equals(currentPackageInfo.packageName)) {
                    continue;
                }
                HashMap<String, String> pluginMap = new HashMap<>();
                String label = info.applicationInfo.loadLabel(getPackageManager()).toString();
                pluginMap.put("label", label);
                pluginMap.put("packageName", packageName);
                data.add(pluginMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }
}
