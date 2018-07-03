package com.android.tvremoteime;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;
import android.view.WindowManager;

import com.android.tvremoteime.http.HTTPGet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
/**
 * Created by kingt on 2018/4/11.
 */
public class AutoUpdateManager {
    private static String TAG = "AutoUpdateManager";
    private Context context;
    private Handler handler;
    private File localFile = null;
    private static String VERSION_URL = "https://gitee.com/kingthy/TVRemoteIME/raw/master/released/version.json";
    public AutoUpdateManager(Context context, Handler handler){
        this.context = context;
        this.handler = handler;
        this.localFile = new File(context.getExternalCacheDir(), context.getString(R.string.app_name) + ".apk");
        this.startUpdateThread();
    }

    private void startUpdateThread(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    JSONObject versionObj = getServerVersionObj();
                    if(versionObj != null && needUpdate(versionObj)){
                        if(downloadInstallAPK(versionObj)){
                            String message = versionObj.has("message") ? versionObj.getString("message") : "";
                            String versionName = versionObj.has("versionName") ? versionObj.getString("versionName") : AppPackagesHelper.getCurrentPackageVersion(context);
                            StringBuilder msg = new StringBuilder();
                            msg.append("发现新版本：").append(versionName);
                            if(!TextUtils.isEmpty(message)){
                                msg.append("\r\n更新内容：\r\n\r\n").append(message);
                            }
                            final String content = msg.toString();
                            final boolean forced = versionObj.has("forced") && versionObj.getBoolean("forced");

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle(context.getString(R.string.app_name) + "版本更新提示").setIcon(R.drawable.ic_launcher);
                                    builder.setMessage(content);
                                    if (!forced) {
                                        builder.setPositiveButton("稍后更新", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                    }
                                    builder.setNegativeButton("马上更新", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            AppPackagesHelper.installPackage(localFile, context);
                                            dialog.dismiss();
                                        }
                                    });
                                    try {
                                        AlertDialog dialog = builder.create();
                                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                                        dialog.setCanceledOnTouchOutside(false);
                                        dialog.show();
                                    } catch (Exception ex) {
                                        AppPackagesHelper.installPackage(localFile, context);
                                    }
                                }
                            });
                        }
                    }
                }catch (Exception e){
                    Log.e(TAG, "startUpdateThread", e);
                }
            }
        });
        thread.start();
    }

    private int getCurrentPackageVersion(){
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        }catch (PackageManager.NameNotFoundException e){}
        return -1;
    }

    private JSONObject getServerVersionObj(){
        try{
            String jsonData = HTTPGet.readString(VERSION_URL);
            if (!TextUtils.isEmpty(jsonData)) {
                if(Environment.needDebug) Environment.debug(TAG, "getServerVersionObj:\r\n" + jsonData);
                return new JSONObject(jsonData);
            }
        } catch (Exception e) {
            Log.e(TAG, "getServerVersionObj", e);
        }
        return null;
    }

    private boolean needUpdate(JSONObject versionObj){
        int version = getCurrentPackageVersion();
        if(version == -1 || versionObj == null) return false;

        try {
            return (versionObj.has("versionCode") &&
                     versionObj.has("installAPK") &&
                     version < versionObj.getInt("versionCode"));
        } catch (JSONException e) {
            return false;
        }
    }

    private boolean needDownloadAPK(JSONObject versionObj) {
        try {
            if(! this.localFile.exists()) return true;

            PackageManager pm = context.getPackageManager();
            PackageInfo packInfo = pm.getPackageArchiveInfo(this.localFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
            int localVersion = packInfo.versionCode;
            if(Environment.needDebug) Environment.debug(TAG, "needDownloadAPK: localVersion = " + localVersion);
            int version = versionObj.getInt("versionCode");
            return localVersion < version;
        }catch (Exception e){
            return true;
        }
    }

    private boolean downloadInstallAPK(JSONObject versionObj){
        try {
            if(!needDownloadAPK(versionObj)){
                return true;
            }

            String url = versionObj.getString("installAPK");
            if(TextUtils.isEmpty(url)) return false;
            if(Environment.needDebug) Environment.debug(TAG, "downloadInstallAPK starting: " + url);
            boolean flag = HTTPGet.downloadFile(url, this.localFile);
            if(Environment.needDebug) Environment.debug(TAG, "downloadInstallAPK finished. result: " + flag);
            return flag;
        } catch (Exception e) {
            Log.e(TAG, "downloadInstallAPK", e);
        }
        return false;
    }
}
