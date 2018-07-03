package com.android.tvremoteime.server;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.tvremoteime.VideoPlayHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import xllib.DownloadTask;
import xllib.PlayListItem;

/**
 * Created by kingt on 2018/3/6.
 */

public class TorrentRequestProcesser implements RequestProcesser  {
    private Context context;
    private DownloadTask downloadTask = new DownloadTask();
    public TorrentRequestProcesser(Context context){
        this.context = context;
    }

    @Override
    public boolean isRequest(NanoHTTPD.IHTTPSession session, String fileName) {
        if(session.getMethod() == NanoHTTPD.Method.POST){
            switch (fileName){
                case "/torrent/data":
                case "/torrent/upload":
                case "/torrent/play":
                    return true;
            }
        }
        return "/torrent".equalsIgnoreCase(fileName);
    }

    @Override
    public NanoHTTPD.Response doResponse(NanoHTTPD.IHTTPSession session, String fileName, Map<String, String> params, Map<String, String> files) {
        if(session.getMethod() == NanoHTTPD.Method.POST){
            switch (fileName){
                case "/torrent/upload":
                    saveTorrent(params, files);
                    break;
                case "/torrent/play":
                    return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.OK, playTorrent(params) ? "ok" : "fail");
            }
        }
        if(RemoteServerFileManager.getPlayTorrentFile().exists()){
            return responseTorrentFileList();
        }else {
            return RemoteServer.createJSONResponse(NanoHTTPD.Response.Status.OK,  "{\"success\":false}");
        }
    }

    private void saveTorrent(Map<String, String> params, Map<String, String> files){
        String uploadFileName  = params.get("file");
        String localFilename = files.get("file");
        boolean r = false;
        if(!TextUtils.isEmpty(uploadFileName)) {
            if (!TextUtils.isEmpty(localFilename)) {
                File localFile = new File(localFilename);
                localFile.renameTo(RemoteServerFileManager.getPlayTorrentFile());
            }
        }
    }

    private NanoHTTPD.Response responseTorrentFileList() {
        downloadTask.setUrl(RemoteServerFileManager.getPlayTorrentFile().getAbsolutePath());
        JSONObject data = new JSONObject();
        try {
            data.put("success", true);
            JSONArray items = new JSONArray();
            for(PlayListItem item : downloadTask.getPlayList()){
                JSONObject playItem = new JSONObject();
                playItem.put("name", item.getName());
                playItem.put("index", item.getIndex());
                playItem.put("size", item.getSize());
                items.put(playItem);
            }
            data.put("files", items);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return RemoteServer.createJSONResponse(NanoHTTPD.Response.Status.OK,  data.toString());
    }

    private boolean playTorrent(Map<String, String> params){
        File torrentFile = RemoteServerFileManager.getPlayTorrentFile();
        if(!torrentFile.exists()) return false;
        int videoIndex = TextUtils.isEmpty(params.get("videoIndex")) ? 0 : Integer.parseInt(params.get("videoIndex"));
        VideoPlayHelper.playUrl(this.context, torrentFile.getAbsolutePath(), videoIndex, "true".equalsIgnoreCase(params.get("useSystem")));
        return true;
    }
}
