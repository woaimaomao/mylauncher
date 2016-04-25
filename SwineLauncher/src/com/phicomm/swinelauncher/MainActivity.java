package com.phicomm.swinelauncher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Text;

import com.phicomm.swinelauncher.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity
        implements OnItemClickListener, OnItemLongClickListener, OnPageChangeListener, OnDragListener {

    private final String TAG = "MainActivity";
    private GridView gridView[];
    private List<AppInfo> mResolveInfos;
    private InstallOrUnInstallReceiver mInstallOrUnInstallReceiver;
    private MyGrideViewAdapter myGrideViewAdapter[];
    private ViewPager mViewPager;
    private TextView tvUninstall;
    private TextView tvAppInfo;
    private TextView tvHide;
    private TextView tvDissolve;
    private ImageView mainApp01;
    private ImageView mainApp02;
    private ImageView mainApp03;
    private ImageView mainApp04;
    private int mCurrentPage = 0;
    private List<AppInfo> list[];
    private ImageView itemImage;
    private Bundle mInstanceState;
    private Map<View, AppInfo>[] imagePosList;
    // 标记位,标记当前被长按的应用在列表中的位置
    private int flag = -1;
    private int ROW = 6;
    private int COLUMN = 4;
    private int pageCount;
    private final static String PACKAGE_ADDED = "android.intent.action.PACKAGE_ADDED";
    private final static String PACKAGE_REMOVED = "android.intent.action.PACKAGE_REMOVED";
    private final static String PACKAGE_CHANGED = "android.intent.action.PACKAGE_CHANGED";
    private final static String CATEGORY_LAUNCHER = "android.intent.category.LAUNCHER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mInstanceState = savedInstanceState;
        setContentView(R.layout.activity_main);
        // 初始化视图
        initViews();
        // 注册广播
        registerReceiver();

    }

    // 初始化视图 24
    private void initViews() {

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        tvUninstall = (TextView) findViewById(R.id.tv01);
        tvAppInfo = (TextView) findViewById(R.id.tv02);
        tvHide = (TextView) findViewById(R.id.tv03);
        tvDissolve = (TextView) findViewById(R.id.textviewSplit);
        mainApp01 = (ImageView) findViewById(R.id.image11);
        mainApp02 = (ImageView) findViewById(R.id.image22);
        mainApp03 = (ImageView) findViewById(R.id.image33);
        mainApp04 = (ImageView) findViewById(R.id.image44);
        // 获取桌面要显示的应用信息
        getResolveInfos();
        // 初始化GridView的列表数据
        initGridListData();
        // 初始化ViewPager
        initViewPager();
    }

    private void initViewPager() {
        gridView = new GridView[pageCount];
        myGrideViewAdapter = new MyGrideViewAdapter[pageCount];
        imagePosList = new HashMap[pageCount];
        List<GridView> viewPagerList = new ArrayList<GridView>();
        for (int i = 0; i < pageCount; i++) {
            imagePosList[i] = new HashMap<View, AppInfo>();
            gridView[i] = new GridView(this);
            LayoutParams params0 = new LayoutParams(dipToPx(45), dipToPx(45));
            gridView[i].setLayoutParams(params0);
            gridView[i].setNumColumns(COLUMN);
            myGrideViewAdapter[i] = new MyGrideViewAdapter(this, list[i], this);
            gridView[i].setAdapter(myGrideViewAdapter[i]);
            gridView[i].setOnItemClickListener(this);
            gridView[i].setOnItemLongClickListener(this);
            // mViewPager.addView(gridView[i]);
            viewPagerList.add(gridView[i]);
        }
        mViewPager.setAdapter(new MyViewPagerAdapter(viewPagerList));
        mViewPager.setOnPageChangeListener(this);
    }

    private void initGridListData() {
        int appTotal = ROW * COLUMN; // 每页个数
        pageCount = mResolveInfos.size() / appTotal + 1; // 总页数
        int residual = mResolveInfos.size() % appTotal; // 余数
        list = new ArrayList[pageCount];
        Log.i(TAG, "应用总数:---" + mResolveInfos.size());
        // 只有一页,则直接显示
        if (pageCount == 1) {
            list[pageCount - 1] = new ArrayList<AppInfo>();
            list[pageCount - 1].addAll(mResolveInfos);
        } else if (pageCount > 1) {
            int x = 0;
            int i = 0;
            int j = 0;
            for (; x < pageCount; x++) {
                list[x] = new ArrayList<AppInfo>();

                if (x < pageCount - 1) {
                    for (; i < appTotal; i++) {
                        list[x].add(mResolveInfos.get(i));
                    }
                } else {
                    for (j = i; j < residual + i; j++) {
                        list[x].add(mResolveInfos.get(j));
                    }
                }
            }
        }
    }

    private void getResolveInfos() {
        if (mResolveInfos == null) {
            Log.i("MainActivity", "mResolveInfos为Null");
            mResolveInfos = SavedAppData.getInstance(this, SavedAppData.SWINE).readDataAll();
            if (mResolveInfos.isEmpty()) {
                Log.i("MainActivity", "mResolveInfos里面没有元素");
                // 重新获取APP信息
                mResolveInfos = loadApps();
                // 将其保存到Preference里
                SavedAppData.getInstance(this, SavedAppData.SWINE).writeDataAll(mResolveInfos);
            }
        }
    }

    // ViewPager的自定义适配器
    private class MyViewPagerAdapter extends PagerAdapter {

        private List<GridView> mGridList;

        public MyViewPagerAdapter() {
            // TODO Auto-generated constructor stub
        }

        public MyViewPagerAdapter(List<GridView> gridList) {
            // TODO Auto-generated constructor stub
            mGridList = gridList;
        }

        @Override
        public Object instantiateItem(View container, int position) {
            // TODO Auto-generated method stub
            ViewPager pv = (ViewPager) container;
            if (pv.indexOfChild(mGridList.get(position)) == -1) {
                ((ViewPager) container).addView(mGridList.get(position));
            }
            return mGridList.get(position);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mGridList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            // TODO Auto-generated method stub
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            // TODO Auto-generated method stub
        }

        @Override
        public void finishUpdate(View container) {
            // TODO Auto-generated method stub
            super.finishUpdate(container);
        }

    }

    // 注册广播
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PACKAGE_ADDED);
        filter.addAction(PACKAGE_REMOVED);
        filter.addAction(PACKAGE_CHANGED);
        filter.addCategory(CATEGORY_LAUNCHER);
        filter.addDataScheme("package");
        mInstallOrUnInstallReceiver = new InstallOrUnInstallReceiver(this);
        this.registerReceiver(mInstallOrUnInstallReceiver, filter);

        IntentFilter broadIntent = new IntentFilter();
        broadIntent.addAction(FolderActivity.UPDATE_ACTION);
        broadIntent.addCategory(FolderActivity.UPDATE_CATEGORY);
        this.registerReceiver(new OnDestroyBroadCast(this), broadIntent);
    }

    private class OnDestroyBroadCast extends BroadcastReceiver{

        private MainActivity activity;
        public OnDestroyBroadCast(MainActivity act) {
            // TODO Auto-generated constructor stub
            this.activity = act;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("收到广播");
            onCreate(null);
                //updateDataForGridView();
           
        }
        
        
    }
    // 更新GridView的数据
    private void updateDataForGridView() {
        getResolveInfos();
        initGridListData();
        for(int i=0; i<pageCount; i++){        
        myGrideViewAdapter[i].mList = list[i];
        myGrideViewAdapter[i].notifyDataSetChanged();
        }
    }
    // 获取手机安装的所有APP
    public List<AppInfo> loadApps() {
        List<ResolveInfo> apps = null;
        List<AppInfo> all = new ArrayList<AppInfo>();
        Intent appsIntent = new Intent(Intent.ACTION_MAIN, null);
        appsIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        apps = getPackageManager().queryIntentActivities(appsIntent, 0);
        for (int i = 0; i < apps.size(); i++) {
            ResolveInfo info = apps.get(i);
            AppInfo map = new AppInfo();
            map.setPakcage(info.activityInfo.packageName);
            map.setClassname(info.activityInfo.name);
            map.setAppicon(info.activityInfo.loadIcon(getPackageManager()));
            map.setAppname((String) info.activityInfo.loadLabel(getPackageManager()));
            map.setIsdir(false);
            all.add(map);
        }
        return all;
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
            imagePosList[mCurrentPage].put(vh.imageView, info);
            vh.textView.setText((CharSequence) info.getAppname());
            return convertView;
        }

        private class ViewHolder {
            ImageView imageView;
            TextView textView;
        }
    }

    // 处理GrideView点击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        List<AppInfo> mApps = list[mCurrentPage];
        AppInfo info = mApps.get(position);
        if (info.isIsdir()) {
            Intent createIntent = new Intent(MainActivity.this, FolderActivity.class);
            createIntent.putExtra(FolderActivity.FILELIST, info.getListfile());
            startActivity(createIntent);
        } else {
            Intent openIntent = new Intent();
            openIntent.setAction(Intent.ACTION_MAIN);
            openIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            ComponentName componentName = new ComponentName(info.getPakcage(), info.getClassname());
            openIntent.setComponent(componentName);
            startActivity(openIntent);
        }
    }

    // 处理长按APP图标的事件
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        List<AppInfo> infos = list[mCurrentPage];
        AppInfo map = infos.get(position);
        final String fPackageName = map.getPakcage();

        ClipData data = ClipData.newPlainText("", "");
        itemImage = (ImageView) view.findViewById(R.id.imageView1);
        itemImage.setOnDragListener(this);
        // view.setOnDragListener(this);
        // 开始执行拖拽
        List<Object> localState = new ArrayList<Object>();
        localState.add(0, fPackageName);
        localState.add(1, itemImage);
        itemImage.startDrag(data, new DragShadowBuilder(itemImage), localState, 0);
        if (!map.isIsdir()) {
            
            // 设置界面变化效果
            tvUninstall.setVisibility(View.VISIBLE);
            tvAppInfo.setVisibility(View.VISIBLE);
            tvHide.setVisibility(View.VISIBLE);
            mainApp01.setVisibility(View.INVISIBLE);
            mainApp02.setVisibility(View.INVISIBLE);
            mainApp03.setVisibility(View.INVISIBLE);
            mainApp04.setVisibility(View.INVISIBLE);
            tvUninstall.setOnDragListener(this);
            tvAppInfo.setOnDragListener(this);
            tvHide.setOnDragListener(this);
        }else{
            tvDissolve.setVisibility(View.VISIBLE);
            tvDissolve.setOnDragListener(this);
        }
        return true;
    }

    private class InstallOrUnInstallReceiver extends BroadcastReceiver {

        private MainActivity mAct;

        public InstallOrUnInstallReceiver() {
        }

        public InstallOrUnInstallReceiver(MainActivity activity) {
            this.mAct = activity;
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            final String packageName = intent.getDataString();
            Log.i("InstallOrUnInstallReceiver", "对比前:被卸载的应用信息:--->" + packageName);

            // 当接收到安装或卸载应用的广播后更新列表数据
            if (intent.getAction().equals(PACKAGE_REMOVED) || intent.getAction().equals(PACKAGE_ADDED)) {
                mResolveInfos = loadApps();
                // SavedAppData.getInstance(this.mAct).writeDataAll(mResolveInfos);
                //onCreate(mInstanceState);
                updateDataForGridView();
            }
        }

    }

    @Override
    protected void onResume() {

        Log.i("InstallOrUnInstallReceiver", "onResume(),已经通知更新列表数据");
        super.onResume();
    }

    @Override
    protected void onPause() {
        // mResolveInfos = loadApps();
        Log.i("InstallOrUnInstallReceiver", "onPause(),已经通知更新列表数据");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mInstallOrUnInstallReceiver != null) {
            this.unregisterReceiver(mInstallOrUnInstallReceiver);
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // getMenuInflater().inflate(R.menu.main, menu);

        menu.add(1, 1, 1, "桌面背景");
        menu.add(1, 2, 2, "设置布局");
        menu.add(1, 3, 3, "隐藏应用");
        menu.add(1, 4, 4, "文件夹");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case 1:
            break;
        case 2:
            break;
        case 3:
            break;
        case 4:

            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub
        Log.i(TAG, "onPageScrollStateChanged,arg0=="+arg0);

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub
        Log.i(TAG, "onPageScrolled,arg0="+arg0);
        Log.i(TAG, "onPageScrolled,arg1="+arg1);
        Log.i(TAG, "onPageScrolled,arg2="+arg2);
    }

    @Override
    public void onPageSelected(int arg0) {
        // TODO Auto-generated method stub
        mCurrentPage = arg0;
        System.out.println("mCurrentPage==" + mCurrentPage);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {

        switch (event.getAction()) {
        case DragEvent.ACTION_DRAG_STARTED:
            break;
        case DragEvent.ACTION_DRAG_ENTERED:
            break;
        case DragEvent.ACTION_DRAG_LOCATION:
            if (v == tvUninstall) {
                tvUninstall.setBackgroundResource(R.drawable.rectangle);
                tvAppInfo.setBackgroundResource(R.drawable.rectangle2);
                tvHide.setBackgroundResource(R.drawable.rectangle2);
            } else if (v == tvAppInfo) {
                tvUninstall.setBackgroundResource(R.drawable.rectangle2);
                tvAppInfo.setBackgroundResource(R.drawable.rectangle);
                tvHide.setBackgroundResource(R.drawable.rectangle2);

            } else if (v == tvHide) {
                tvUninstall.setBackgroundResource(R.drawable.rectangle2);
                tvAppInfo.setBackgroundResource(R.drawable.rectangle2);
                tvHide.setBackgroundResource(R.drawable.rectangle);
            }
            break;
        case DragEvent.ACTION_DROP:
            List<Object> local = (List<Object>) event.getLocalState();
            if (v == tvUninstall) {
                unInstallSoft(event);
            } else if (v == tvAppInfo) {
                // Toast.makeText(this, "应用信息", Toast.LENGTH_SHORT).show();
                seeAppInfo(event);
            } else if (v == tvHide) {
                // Toast.makeText(this, "隐藏应用", Toast.LENGTH_SHORT).show();
            } else if(v == tvDissolve){
                AppInfo app0 = imagePosList[mCurrentPage].get(local.get(1));
                List<AppInfo> list = SavedAppData.getInstance(this, app0.getListfile()).readDataAll();
                SavedAppData.getInstance(this, SavedAppData.SWINE).writeDataAll(list);
                SavedAppData.getInstance(this, SavedAppData.SWINE).removeApp(app0);
                tvDissolve.setVisibility(View.INVISIBLE);
            }else {
                
                if (v != local.get(1)) {

                    AppInfo appInfo = imagePosList[mCurrentPage].get(v);
                    AppInfo appInfo2 = imagePosList[mCurrentPage].get(local.get(1));
                    System.out.println("imagePosList[mCurrentPage]===" + imagePosList[mCurrentPage]);
                    System.out.println("APPINFO===" + appInfo);

                    if (appInfo != null) {
                        // 移动到的是一个文件夹
                        if (appInfo.isIsdir() && appInfo.getListfile() != null && !"".equals(appInfo.getListfile())) {
                            createFolder(appInfo.getListfile(), appInfo2);
                        } else { // 移动到的是一个app
                            // 创建一个新应用,包名使用文件名
                            AppInfo appFile = new AppInfo();
                            String pkg = System.currentTimeMillis() + "";
                            appFile.setPakcage(pkg);
                            appFile.setAppname("未命名");
                            appFile.setIsdir(true);
                            appFile.setListfile(pkg);
                            // 将这个应用(文件夹)保存到桌面的Preference文件中
                            SavedAppData.getInstance(this, SavedAppData.SWINE).writeData(pkg, appFile);
                            List<AppInfo> apps = new ArrayList<AppInfo>();
                            apps.add(appInfo);
                            apps.add(appInfo2);
                            createFolder(pkg, apps);
                        }
                    }
                }
            }
            break;
        case DragEvent.ACTION_DRAG_ENDED:
            tvUninstall.setVisibility(View.INVISIBLE);
            tvAppInfo.setVisibility(View.INVISIBLE);
            tvHide.setVisibility(View.INVISIBLE);
            mainApp01.setVisibility(View.VISIBLE);
            mainApp02.setVisibility(View.VISIBLE);
            mainApp03.setVisibility(View.VISIBLE);
            mainApp04.setVisibility(View.VISIBLE);
            // linear01.setVisibility(View.VISIBLE);
            tvUninstall.setBackgroundResource(R.drawable.rectangle2);
            tvAppInfo.setBackgroundResource(R.drawable.rectangle2);
            tvHide.setBackgroundResource(R.drawable.rectangle2);
            Log.i("onDrag---", v.toString() + "ACTION_DRAG_ENDED");
            break;
        }
        return true;
    }

    // 当移动的两个都是APP时,调用此方法创建文件夹
    private void createFolder(String filename, List<AppInfo> list) {
        SavedAppData.getInstance(this, filename).writeDataAll(list);
        Intent createIntent = new Intent(MainActivity.this, FolderActivity.class);
        createIntent.putExtra(FolderActivity.FILELIST, filename);
        startActivity(createIntent);
        SavedAppData.getInstance(this, SavedAppData.SWINE).removeAppList(list);
    }

    // 当移动到的是一个文件夹时,利用此方法创建文件夹
    private void createFolder(String filename, AppInfo appInfo) {
        SavedAppData.getInstance(this, filename).writeData(appInfo.getPakcage(), appInfo);
        Intent createIntent = new Intent(MainActivity.this, FolderActivity.class);
        createIntent.putExtra(FolderActivity.FILELIST, filename);
        startActivity(createIntent);
        SavedAppData.getInstance(this, SavedAppData.SWINE).removeApp(appInfo);
    }

    // 查看应用信息
    private void seeAppInfo(DragEvent event) {
        Intent infoIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        List<Object> local = (List<Object>) event.getLocalState();
        Uri uri = Uri.fromParts("package", local.get(0).toString(), null);
        infoIntent.setData(uri);
        startActivity(infoIntent);
    }

    // 卸载软件
    private void unInstallSoft(DragEvent event) {
        Intent unInstall = new Intent();
        unInstall.setAction(Intent.ACTION_DELETE);
        List<Object> local = (List<Object>) event.getLocalState();
        unInstall.setData(Uri.parse("package:" + local.get(0)));
        startActivity(unInstall);
    }

    public static int pxToDip(int px) {
        return (int) (px / 1.5 + 0.5);
    }

    public static int dipToPx(int dip) {
        return (int) ((dip - 0.5) / 1.5);
    }

}
