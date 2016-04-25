package com.phicomm.swinelauncher;

import java.util.List;
import java.util.Map;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.LiveFolders;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnDragListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class FolderActivity extends Activity implements OnItemClickListener,OnItemLongClickListener{

    private GridView mGridView;
    private EditText etAppName;
    private List<AppInfo> list;
    private String filelist;
    public static final String APPCOUNT = "app_count";
    public static final String APPNAME = "app_name";
    public static final String FILELIST = "file_list";
    public static final String UPDATE_ACTION = "com.phicomm.swinelauncher.action.UPDATE";
    public static final String UPDATE_CATEGORY = "com.phicomm.swinelauncher.category.UPDATE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        setContentView(R.layout.folder_activity);
        etAppName = (EditText) findViewById(R.id.editText1);
        mGridView = (GridView) findViewById(R.id.gridView1);
        filelist =  getIntent().getStringExtra(FILELIST);
        list = SavedAppData.getInstance(this, filelist).readDataAll();
        mGridView.setAdapter(new MyGrideViewAdapter(this, list, null));
        mGridView.setOnItemClickListener(this);
        mGridView.setOnItemLongClickListener(this);
        AppInfo app = SavedAppData.getInstance(this, SavedAppData.SWINE).readData(filelist);
        etAppName.setText(app.getAppname());
    }
    
 // 给GrideView自定义适配器
    private class MyGrideViewAdapter extends BaseAdapter {

        private List<AppInfo> mList = null;
        private Context mContext = null;
        private OnDragListener mListen;

        public MyGrideViewAdapter(Context context, List<AppInfo> list, OnDragListener listen) {
            mList = list;
            mContext = context;
            mListen = listen;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.gride_item_apps, null);
                vh = new ViewHolder();
                vh.imageView = (ImageView) convertView.findViewById(R.id.imageView1);
                vh.textView = (TextView) convertView.findViewById(R.id.textView1);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            AppInfo info = mList.get(position);
            vh.imageView.setImageDrawable(lookForIcon(info.getPakcage()));
            vh.imageView.setOnDragListener(mListen);
            vh.textView.setText((CharSequence)info.getAppname());
            return convertView;
        }

        private class ViewHolder {
            ImageView imageView;
            TextView textView;
        }
        
        public Drawable lookForIcon(String pck) {
            List<ResolveInfo> apps = null;
            Intent appsIntent = new Intent(Intent.ACTION_MAIN, null);
            appsIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            apps = getPackageManager().queryIntentActivities(appsIntent, 0);
            for (int i = 0; i < apps.size(); i++) {
                if (pck != null && pck.equals(apps.get(i).activityInfo.packageName)) {
                    return apps.get(i).activityInfo.loadIcon(getPackageManager());
                }
            }
            return new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 发送广播通知主界面文件夹发变化
        Intent broadIntent = new Intent();
        broadIntent.setAction(UPDATE_ACTION);
        broadIntent.addCategory(UPDATE_CATEGORY);
        this.sendBroadcast(broadIntent);
        // 获取用户自定义名字
        String appname = etAppName.getText().toString();
        // 获取文件夹对象
        AppInfo app = SavedAppData.getInstance(this, SavedAppData.SWINE).readData(filelist);
        // 修改文件夹名字
        app.setAppname(appname);
        // 重新保存文件夹
        SavedAppData.getInstance(this, SavedAppData.SWINE).writeData(filelist, app);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        AppInfo info = list.get(position);
        SavedAppData.getInstance(this, SavedAppData.SWINE).writeData(info.getPakcage(), info);
        SavedAppData.getInstance(this, filelist).removeApp(info);
        List<AppInfo> list = SavedAppData.getInstance(this, filelist).readDataAll();
        if(list.isEmpty()){
            SavedAppData sad = SavedAppData.getInstance(this, SavedAppData.SWINE);
            sad.removeApp(filelist);
        }
        finish();
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        AppInfo info = list.get(position);
        Intent openIntent = new Intent();
        openIntent.setAction(Intent.ACTION_MAIN);
        openIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        ComponentName componentName = new ComponentName(info.getPakcage(), info.getClassname());
        openIntent.setComponent(componentName);
        startActivity(openIntent);
    }
    
    

}
