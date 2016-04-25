package com.phicomm.swinelauncher;

import android.graphics.drawable.Drawable;

public class AppInfo {

    private String pakcage;
    private String classname;
    private String appname;
    private boolean isdir;
    private String listfile;
    private Drawable appicon;    
    
    public AppInfo() {
        super();
        // TODO Auto-generated constructor stub
    }
    public AppInfo(String pakcage, String classname, String appname, boolean isdir, String listfile) {
        super();
        this.pakcage = pakcage;
        this.classname = classname;
        this.appname = appname;
        this.isdir = isdir;
        this.listfile = listfile;
    }
    
    
    public Drawable getAppicon() {
        return appicon;
    }
    public void setAppicon(Drawable appicon) {
        this.appicon = appicon;
    }
    public String getPakcage() {
        return pakcage;
    }
    public void setPakcage(String pakcage) {
        this.pakcage = pakcage;
    }
    public String getClassname() {
        return classname;
    }
    public void setClassname(String classname) {
        this.classname = classname;
    }
    public String getAppname() {
        return appname;
    }
    public void setAppname(String appname) {
        this.appname = appname;
    }
    public boolean isIsdir() {
        return isdir;
    }
    public void setIsdir(boolean isdir) {
        this.isdir = isdir;
    }
    public String getListfile() {
        return listfile;
    }
    public void setListfile(String listfile) {
        this.listfile = listfile;
    }
    @Override
    public String toString() {
        return "AppInfo [pakcage=" + pakcage + ", classname=" + classname + ", appname=" + appname + ", isdir=" + isdir
                + ", listfile=" + listfile + "]";
    }
    
    
    
}
