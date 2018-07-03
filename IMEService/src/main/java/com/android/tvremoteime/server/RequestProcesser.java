package com.android.tvremoteime.server;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by kingt on 2018/1/7.
 */

public interface RequestProcesser {
    /**
     * isRequest
     * @param session
     * @param fileName
     * @return
     */
    boolean isRequest(NanoHTTPD.IHTTPSession session, String fileName);
    /**
     * doResponse
     * @param session
     * @param fileName
     * @param params
     * @param files
     * @return
     */
    NanoHTTPD.Response doResponse(NanoHTTPD.IHTTPSession session, String fileName, Map<String, String> params, Map<String, String> files);
}
