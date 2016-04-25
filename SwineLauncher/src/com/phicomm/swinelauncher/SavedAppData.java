package com.phicomm.swinelauncher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

public class SavedAppData {

    private static SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    public static final String SWINE = "swine";
    private static SavedAppData mSavedAppData;

    private SavedAppData() {

    }

    public static SavedAppData getInstance(Context context, String name) {
        mSharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        if (mSavedAppData == null) {
            mSavedAppData = new SavedAppData();
        }
        return mSavedAppData;
    }

    public AppInfo readData(String pkg) {
        Set<String> set = mSharedPreferences.getStringSet(pkg, null);
        AppInfo info = new AppInfo();
        if (set != null) {
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String str = it.next();
                if (str != null) {
                    String strs[] = str.split(":");
                    if ("package".equals(strs[0])) {
                        info.setPakcage(strs[1]);

                    } else if ("classname".equals(strs[0])) {
                        info.setClassname(strs[1]);
                    } else if ("isdir".equals(strs[0])) {
                        info.setIsdir(Boolean.parseBoolean(strs[1]));
                    } else if ("appname".equals(strs[0])) {
                        info.setAppname(strs[1]);
                    } else if ("listfile".equals(strs[0])) {
                        info.setListfile(strs[1]);
                    }
                }
            }
        }
        return info;
    }

    // 读取数据,返回List列表
    public List<AppInfo> readDataAll() {
        Map<String, ?> allMap = mSharedPreferences.getAll();

        List<AppInfo> list = new ArrayList<AppInfo>();

        Set<String> keySet = allMap.keySet();
        Iterator<String> keyIt = keySet.iterator();
        while (keyIt.hasNext()) {
            // 获取每个APP在Preference中对应的键值
            Set<String> set = mSharedPreferences.getStringSet(keyIt.next(), null);
            Iterator<String> ite = set.iterator();
            // 遍历保存应用信息的Set
            AppInfo info = new AppInfo();
            while (ite.hasNext()) {
                String str = ite.next();
                if (str != null) {
                    String strs[] = str.split(":");
                    if ("package".equals(strs[0])) {
                        info.setPakcage(strs[1]);

                    } else if ("classname".equals(strs[0])) {
                        info.setClassname(strs[1]);
                    } else if ("isdir".equals(strs[0])) {
                        info.setIsdir(Boolean.parseBoolean(strs[1]));
                    } else if ("appname".equals(strs[0])) {
                        info.setAppname(strs[1]);
                    } else if ("listfile".equals(strs[0])) {
                        info.setListfile(strs[1]);
                    }
                }
            }
            list.add(info);
        }
        return list;
    }

    // 写入单个数据
    public void writeData(String key, AppInfo info) {
        mEditor = mSharedPreferences.edit();
        Set<String> set = new HashSet<String>();
        set.add("package:" + info.getPakcage());
        set.add("classname:" + info.getClassname());
        set.add("appname:" + info.getAppname());
        set.add("isdir:" + info.isIsdir());
        set.add("listfile:" + info.getListfile());
        mEditor.putStringSet(key, set);
        mEditor.commit();
    }

    // 将应用信息存入Set中,然后将包名作为键值,将数据存入Preference中
    public void writeDataAll(List<AppInfo> list) {
        mEditor = mSharedPreferences.edit();
        List<AppInfo> mapList = list;
        for (int i = 0; i < mapList.size(); i++) {
            AppInfo info = mapList.get(i);
            Set<String> set = new HashSet<String>();
            set.add("package:" + info.getPakcage());
            set.add("classname:" + info.getClassname());
            set.add("isdir:" + info.isIsdir());
            set.add("appname:" + info.getAppname());
            set.add("listfile:" + info.getListfile());
            mEditor.putStringSet(info.getPakcage(), set);
        }
        mEditor.commit();
    }

    // 在桌面上移除一个集合的应用
    public void removeAppList(List<AppInfo> apps) {
        mEditor = mSharedPreferences.edit();
        for (int i = 0; i < apps.size(); i++) {
            mEditor.remove(apps.get(i).getPakcage());
        }
        mEditor.commit();
    }

    // 在桌面移除一个应用
    public void removeApp(AppInfo app) {
        mEditor = mSharedPreferences.edit();
        mEditor.remove(app.getPakcage());
        mEditor.commit();
    }

    // 在桌面移除一个应用
    public void removeApp(String pkg) {
        mEditor = mSharedPreferences.edit();
        mEditor.remove(pkg);
        mEditor.commit();

    }
}
