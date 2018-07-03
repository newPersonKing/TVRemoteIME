package com.android.tvremoteime;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * Created by kingt on 2018/1/9.
 */

public class AppPackagesHelper {

    public static class AppInfo implements Serializable{
        private String lable;
        private String packageName;
        private String apkPath;
        private boolean isSysApp;

        public String getLable() {
            return lable;
        }

        public void setLable(String lable) {
            this.lable = lable;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public boolean isSysApp() {
            return isSysApp;
        }

        public void setSysApp(boolean sysApp) {
            isSysApp = sysApp;
        }
        public JSONObject toJSONObject()
        {
            JSONObject obj = new JSONObject();
            try {
                obj.put("lable", getLable());
                obj.put("packageName", getPackageName());
                obj.put("apkPath", getApkPath());
                obj.put("isSysApp",  isSysApp());
            }catch (JSONException e) {
                e.printStackTrace();
            }
            return obj;
        }

        public String getApkPath() {
            return apkPath;
        }

        public void setApkPath(String apkPath) {
            this.apkPath = apkPath;
        }
    }

    public static String getCurrentPackageVersion(Context context){
        String version = "1.0.0";
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = packageInfo.versionName;
        }catch (PackageManager.NameNotFoundException e){}
        return version;
    }

    public static List<AppInfo> queryAppInfo(Context context, boolean containSysApp){
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> listAppcations = pm
                .getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES);
        List<AppInfo> appInfos = new ArrayList<AppInfo>();
        for (ApplicationInfo app : listAppcations) {
            if(containSysApp || (app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                boolean isSysApp = (app.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                //过滤掉系统底层的app
                if(isSysApp &&
                        (app.packageName.startsWith("com.android.") || app.packageName.equals("android")))continue;
                AppInfo appInfo = new AppInfo();
                appInfo.setLable((String) app.loadLabel(pm));
                appInfo.setPackageName(app.packageName);
                appInfo.setApkPath(app.sourceDir);
                appInfo.setSysApp(isSysApp);
                appInfos.add(appInfo);
            }
        }
        Collections.sort(appInfos, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                int i1 = (o1.isSysApp ? 2 : 1);
                int i2 = (o2.isSysApp ? 2 : 1);
                if(i1 == i2){
                    return o1.getLable().compareTo(o2.getLable());
                }else{
                    return (i1 < i2) ? -1 : 1;
                }
            }
        });
        return  appInfos;
    }
    public static String getQueryAppInfoJsonString(Context context, boolean containSysApp){
        List<AppInfo> appInfos = queryAppInfo(context, containSysApp);
        JSONArray array = new JSONArray();
        for(AppInfo app : appInfos){
            array.put(app.toJSONObject());
        }
        return  array.toString();
    }

    private static ApplicationInfo getApplicationInfo(String packageName, Context context){
        ApplicationInfo applicationInfo = null;
        if(!packageName.isEmpty()) {
            PackageManager pm = context.getPackageManager();
            try {
                applicationInfo = pm.getApplicationInfo(packageName, 0);
            }catch (PackageManager.NameNotFoundException ex){
                applicationInfo = null;
            }
        }
        return  applicationInfo;
    }

    public static void installPackage(final File apkFile, final Context context){
        try {
            Uri uri = Uri.fromFile(apkFile);
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            context.startActivity(intent);
            Log.i(IMEService.TAG, String.format("已安装应用包[%s]", apkFile.getName()));
        }catch (Exception ex){
            Log.e(IMEService.TAG, String.format("安装应用包[%s]出错", apkFile.getName()), ex);
        }
    }

    public static void uninstallPackage(final String packageName, final Context context){
        if(getApplicationInfo(packageName, context) == null)return;;
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + packageName));
            context.startActivity(intent);
            Log.i(IMEService.TAG, String.format("已删除应用包[%s]", packageName));
        }catch (Exception ex){
            Log.e(IMEService.TAG, String.format("删除应用包[%s]出错", packageName), ex);
        }
    }

    public static void runPackage(final String packageName, final Context context){
        if(getApplicationInfo(packageName, context) == null)return;;
        try {
            PackageManager pm = context.getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(packageName);
            if(intent != null){
                context.startActivity(intent);
            }
            Log.i(IMEService.TAG, String.format("已运行应用包[%s]", packageName));
        }catch (Exception ex){
            Log.e(IMEService.TAG, String.format("运行应用包[%s]出错", packageName), ex);
        }
    }
    public static void runSystemPackage(final String packageName, final Context context){
        try {
            Intent intent = new Intent(packageName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            Log.i(IMEService.TAG, String.format("已运行系统应用包[%s]", packageName));
        }catch (Exception ex){
            Log.e(IMEService.TAG, String.format("运行系统应用包[%s]出错", packageName), ex);
        }
    }
    public static byte[] getAppIcon(String packageName, Context context){
        ApplicationInfo applicationInfo = getApplicationInfo(packageName, context);
        if(applicationInfo == null) return  null;
        BitmapDrawable bitmap = (BitmapDrawable)applicationInfo.loadIcon(context.getPackageManager());

        ByteArrayOutputStream data = new ByteArrayOutputStream();
        bitmap.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, data);
        return data.toByteArray();
    }
}
