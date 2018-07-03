package com.android.tvremoteime.server;

import android.content.Context;

import com.android.tvremoteime.AppPackagesHelper;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by kingt on 2018/1/7.
 */

public class AppRequestProcesser implements RequestProcesser {
    private Context context;

    public AppRequestProcesser(Context context){
        this.context = context;
    }

    @Override
    public boolean isRequest(NanoHTTPD.IHTTPSession session, String fileName) {
        if(session.getMethod() == NanoHTTPD.Method.POST){
            switch (fileName) {
                case "/apps":
                case "/uninstall":
                case "/run":
                case "/runSystem":
                    return true;
            }
        }
        return false;
    }

    @Override
    public NanoHTTPD.Response doResponse(NanoHTTPD.IHTTPSession session, String fileName, Map<String, String> params, Map<String, String> files) {
            switch (fileName) {
                case "/apps":
                    return RemoteServer.createJSONResponse(NanoHTTPD.Response.Status.OK,
                            AppPackagesHelper.getQueryAppInfoJsonString(this.context, "true".equals(params.get("system"))));
                case "/uninstall":
                    if (params.get("packageName") != null) {
                        AppPackagesHelper.uninstallPackage(params.get("packageName"), this.context);
                    }
                    return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK,"ok");
                case "/run":
                    if (params.get("packageName") != null) {
                        AppPackagesHelper.runPackage(params.get("packageName"), this.context);
                    }
                    return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK,"ok");
                case "/runSystem":
                    if (params.get("packageName") != null) {
                        AppPackagesHelper.runSystemPackage(params.get("packageName"), this.context);
                    }
                    return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK,"ok");
                default:
                    return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.NOT_FOUND, "Error 404, file not found.");
            }
    }
}
