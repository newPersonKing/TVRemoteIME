package com.android.tvremoteime.server;

import android.content.Context;

import com.android.tvremoteime.IMEService;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by kingt on 2018/1/7.
 */

public class InputRequestProcesser implements RequestProcesser {
    private Context context;
    private RemoteServer remoteServer;

    public InputRequestProcesser(Context context, RemoteServer remoteServer){
        this.context = context;
        this.remoteServer = remoteServer;
    }

    @Override
    public boolean isRequest(NanoHTTPD.IHTTPSession session, String fileName) {
        if(session.getMethod() == NanoHTTPD.Method.POST){
            switch (fileName) {
                case "/text":
                case "/key":
                case "/keydown":
                case "/keyup":
                    return true;
            }
        }
        return false;
    }

    @Override
    public NanoHTTPD.Response doResponse(NanoHTTPD.IHTTPSession session, String fileName, Map<String, String> params, Map<String, String> files) {
        RemoteServer.DataReceiver mDataReceiver = remoteServer.getDataReceiver();
        switch (fileName) {
            case "/text":
                if (params.get("text") != null && mDataReceiver != null) {
                    mDataReceiver.onTextReceived(params.get("text"));
                }
                return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK,"ok");
            case "/key":
                if (params.get("code") != null && mDataReceiver != null) {
                    mDataReceiver.onKeyEventReceived(params.get("code"), IMEService.KEY_ACTION_PRESSED);
                }
                return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK,"ok");
            case "/keyup":
                if (params.get("code") != null && mDataReceiver != null) {
                    mDataReceiver.onKeyEventReceived(params.get("code"), IMEService.KEY_ACTION_UP);
                }
                return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK,"ok");
            case "/keydown":
                if (params.get("code") != null && mDataReceiver != null) {
                    mDataReceiver.onKeyEventReceived(params.get("code"), IMEService.KEY_ACTION_DOWN);
                }
                return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK,"ok");
            default:
                return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.NOT_FOUND, "Error 404, file not found.");
        }
    }
}
