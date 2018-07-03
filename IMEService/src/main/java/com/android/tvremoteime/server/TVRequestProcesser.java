package com.android.tvremoteime.server;

import android.content.Context;
import android.util.Log;

import com.android.tvremoteime.IMEService;
import com.android.tvremoteime.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by kingt on 2018/3/3.
 */

public class TVRequestProcesser implements RequestProcesser {
    private Context context;
    private File tvFile = new File(RemoteServerFileManager.baseDir, "tv.txt");
    public TVRequestProcesser(Context context){
        this.context = context;
        initTVData();
    }

    private void initTVData(){
        if(!tvFile.exists()){
            try {
                InputStream ins = context.getResources().openRawResource(R.raw.tv);
                FileOutputStream out = new FileOutputStream(tvFile);
                byte[] b = new byte[1024];
                int n = 0;
                while ((n = ins.read(b)) != -1) {
                    out.write(b, 0, n);
                }
                ins.close();
                out.close();
            }catch (Exception e) {
            }
        }
    }

    @Override
    public boolean isRequest(NanoHTTPD.IHTTPSession session, String fileName) {
        return "/tv.txt".equalsIgnoreCase(fileName);
    }

    @Override
    public NanoHTTPD.Response doResponse(NanoHTTPD.IHTTPSession session, String fileName, Map<String, String> params, Map<String, String> files) {
        //if(!tvFile.exists()) return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK, "");
        if(session.getMethod() == NanoHTTPD.Method.POST){
            //edit
            String text = params.get("text");
            try {
                OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(tvFile), "utf-8");
                out.write(text == null ? "" : text);
                out.close();
                return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK, "ok");
            }catch (IOException e) {
                Log.e(IMEService.TAG, "POST /tv.txt", e);
            }
            return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK, "fail");
        }else{
            try {
                InputStream inputStream = tvFile.exists() ? new FileInputStream(tvFile) : context.getResources().openRawResource(R.raw.tv);
                return RemoteServer.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/plain; charset=utf-8", inputStream, (long) inputStream.available());
            } catch (IOException ioex) {
                return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: IOException: " + ioex.getMessage());
            }
        }
    }
}
