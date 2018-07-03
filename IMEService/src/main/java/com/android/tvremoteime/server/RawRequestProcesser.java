package com.android.tvremoteime.server;


import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by kingt on 2018/1/7.
 */

public class RawRequestProcesser implements RequestProcesser {
    private Context context;
    private String fileName;
    private int resourceId;
    private String mimeType;

    public RawRequestProcesser(Context context, String fileName, int resourceId, String mimeType){
        this.context = context;
        this.fileName = fileName;
        this.resourceId = resourceId;
        this.mimeType = mimeType;
    }

    @Override
    public boolean isRequest(NanoHTTPD.IHTTPSession session, String fileName) {
        return session.getMethod() == NanoHTTPD.Method.GET && this.fileName.equalsIgnoreCase(fileName);
    }

    @Override
    public NanoHTTPD.Response doResponse(NanoHTTPD.IHTTPSession session, String fileName, Map<String, String> params, Map<String, String> files) {
        InputStream inputStream = context.getResources().openRawResource(this.resourceId);
        try {
            return RemoteServer.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, mimeType + "; charset=utf-8", inputStream, (long)inputStream.available());
        }catch (IOException ioex){
            return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: IOException: " + ioex.getMessage());
        }
    }
}
