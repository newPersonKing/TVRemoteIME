package com.android.tvremoteime.server;

import android.content.Context;
import android.text.TextUtils;

import com.android.tvremoteime.AppPackagesHelper;
import com.android.tvremoteime.VideoPlayHelper;

import java.io.File;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import xllib.FileUtils;

/**
 * Created by kingt on 2018/1/7.
 */

public class UploadRequestProcesser implements RequestProcesser {
    private Context context;

    public UploadRequestProcesser(Context context){
        this.context = context;
    }

    @Override
    public boolean isRequest(NanoHTTPD.IHTTPSession session, String fileName) {
        return session.getMethod() == NanoHTTPD.Method.POST && "/upload".equalsIgnoreCase(fileName);
    }

    @Override
    public NanoHTTPD.Response doResponse(NanoHTTPD.IHTTPSession session, String fileName, Map<String, String> params, Map<String, String> files) {
        String uploadFileName  = params.get("file");
        Boolean autoInstall = "true".equalsIgnoreCase(params.get("autoInstall"));
        String localFilename = files.get("file");
        if(!TextUtils.isEmpty(uploadFileName)) {
            if (!TextUtils.isEmpty(localFilename)) {
                if(autoInstall) {
                    if (localFilename.endsWith(".apk")) {
                        //执行安装
                        AppPackagesHelper.installPackage(new File(localFilename), this.context);
                    }
                    else if (FileUtils.isMediaFile(localFilename)){
                        //执行播放
                        VideoPlayHelper.playUrl(this.context, localFilename, 0, "true".equalsIgnoreCase(params.get("useSystem")));
                    }
                }
            }
        }
        if(TextUtils.isEmpty(localFilename)){
            return RemoteServer.createJSONResponse(NanoHTTPD.Response.Status.OK,  "{\"success\":false}");
        }else{
            return RemoteServer.createJSONResponse(NanoHTTPD.Response.Status.OK,  String.format("{\"success\":true, \"filePath\":\"%s\"}", localFilename.replaceAll("\\\\", "\\\\")));
        }
    }
}
