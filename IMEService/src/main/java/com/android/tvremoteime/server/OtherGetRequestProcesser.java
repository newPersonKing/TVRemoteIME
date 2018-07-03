package com.android.tvremoteime.server;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import com.android.tvremoteime.AppPackagesHelper;
import com.android.tvremoteime.VideoPlayHelper;

import java.io.File;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by kingt on 2018/1/7.
 */

public class OtherGetRequestProcesser implements RequestProcesser {
    private Context context;

    public OtherGetRequestProcesser(Context context){
        this.context = context;
    }

    @Override
    public boolean isRequest(NanoHTTPD.IHTTPSession session, String fileName) {
        if(session.getMethod() == NanoHTTPD.Method.GET){
            switch (fileName) {
                case "/version":
                case "/sdcard_stat":
                    return true;
            }
        }
        return false;
    }

    @Override
    public NanoHTTPD.Response doResponse(NanoHTTPD.IHTTPSession session, String fileName, Map<String, String> params, Map<String, String> files) {
        switch (fileName) {
            case "/version":
                return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK, AppPackagesHelper.getCurrentPackageVersion(this.context) );
            case "/sdcard_stat":
                return getSDCardStatResponse();
            default:
                return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.NOT_FOUND, "Error 404, file not found.");
        }
    }

    private NanoHTTPD.Response getSDCardStatResponse(){
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long totalBytes, availableBytes;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2){
            totalBytes = (long)stat.getBlockCount() * stat.getBlockSize();
            availableBytes = (long)stat.getAvailableBlocks() * stat.getBlockSize();
        }else{
            totalBytes = stat.getTotalBytes();
            availableBytes = stat.getAvailableBytes();
        }
        return RemoteServer.createJSONResponse(NanoHTTPD.Response.Status.OK,
                "{\"totalBytes\":" + totalBytes + ", \"availableBytes\":" + availableBytes + "}");
    }
}
