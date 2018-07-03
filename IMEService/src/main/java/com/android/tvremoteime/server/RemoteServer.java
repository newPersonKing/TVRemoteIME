package com.android.tvremoteime.server;


import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.android.tvremoteime.Environment;
import com.android.tvremoteime.IMEService;
import com.android.tvremoteime.R;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.*;

/**
 * Created by kingt on 2018/1/7.
 */

public class RemoteServer extends NanoHTTPD
{
    public interface DataReceiver{
        /**
         *
         * @param keyCode
         * @param keyAction : 0 = keypressed, 1 = keydown, 2 = keyup
         */
        void onKeyEventReceived(String keyCode, int keyAction);

        /**
         *
         * @param text
         */
        void onTextReceived(String text);
    }

    public static int serverPort = 9978;
    private boolean isStarted = false;
    private DataReceiver mDataReceiver = null;
    private Context mContext = null;
    private RemoteServerFileManager.Factory fileManagerFactory = new RemoteServerFileManager.Factory();
    private ArrayList<RequestProcesser> getRequestProcessers = new ArrayList<>();
    private ArrayList<RequestProcesser> postRequestProcessers = new ArrayList<>();

    public void setDataReceiver(DataReceiver receiver){
        mDataReceiver = receiver;
    }
    public DataReceiver getDataReceiver(){
        return mDataReceiver;
    }
    public boolean isStarting(){
        return isStarted;
    }

    public RemoteServer(int port, Context context) {
        super(port);
        mContext = context;
        this.addGetRequestProcessers();
        this.addPostRequestProcessers();
    }

    @Override
    public void start(int timeout, boolean daemon) throws IOException {
        isStarted = true;
        setTempFileManagerFactory(fileManagerFactory);
        super.start(timeout, daemon);
    }

    @Override
    public void stop() {
        super.stop();
        isStarted = false;
    }

    public static String getLocalIPAddress(Context context){
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        if(ipAddress == 0){
            try {
                Enumeration<NetworkInterface> enumerationNi = NetworkInterface.getNetworkInterfaces();
                while (enumerationNi.hasMoreElements()) {
                    NetworkInterface networkInterface = enumerationNi.nextElement();
                    String interfaceName = networkInterface.getDisplayName();
                    if (interfaceName.equals("eth0") || interfaceName.equals("wlan0")) {
                        Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses();

                        while (enumIpAddr.hasMoreElements()) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                Log.e(IMEService.TAG, "获取本地IP出错", e);
            }
        }else {
            return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        }
        return "0.0.0.0";
    }

    public String getServerAddress() {
        return getServerAddress(mContext);
    }
    public static String getServerAddress(Context context){
        String ipAddress = getLocalIPAddress(context);
        if(Environment.needDebug) {
            Environment.debug(IMEService.TAG, "ip-address:" + ipAddress);
        }
        return "http://" + ipAddress + ":" + RemoteServer.serverPort + "/";
    }

    public static Response createPlainTextResponse(Response.IStatus status, String text){
        return newFixedLengthResponse(status, NanoHTTPD.MIME_PLAINTEXT, text);
    }

    public static Response createJSONResponse(Response.IStatus status, String text){
        return newFixedLengthResponse(status, "application/json", text);
    }

    private void addGetRequestProcessers(){
        this.getRequestProcessers.add(new RawRequestProcesser(this.mContext, "/index.html", R.raw.index, NanoHTTPD.MIME_HTML));
        this.getRequestProcessers.add(new RawRequestProcesser(this.mContext, "/style.css", R.raw.style, "text/css"));
        this.getRequestProcessers.add(new RawRequestProcesser(this.mContext, "/jquery_min.js", R.raw.jquery_min, "application/x-javascript"));
        this.getRequestProcessers.add(new RawRequestProcesser(this.mContext, "/ime_core.js", R.raw.ime_core, "application/x-javascript"));
        this.getRequestProcessers.add(new RawRequestProcesser(this.mContext, "/keys.png", R.raw.keys, "image/png"));
        this.getRequestProcessers.add(new RawRequestProcesser(this.mContext, "/ic_dl_folder.png", R.raw.ic_dl_folder, "image/png"));
        this.getRequestProcessers.add(new RawRequestProcesser(this.mContext, "/ic_dl_other.png", R.raw.ic_dl_other, "image/png"));
        this.getRequestProcessers.add(new RawRequestProcesser(this.mContext, "/ic_dl_video.png", R.raw.ic_dl_video, "image/png"));
        this.getRequestProcessers.add(new RawRequestProcesser(this.mContext, "/favicon.ico", R.drawable.ic_launcher, "image/x-icon"));
        this.getRequestProcessers.add(new FileRequestProcesser(this.mContext));
        this.getRequestProcessers.add(new AppIconRequestProcesser(this.mContext));
        this.getRequestProcessers.add(new TVRequestProcesser(this.mContext));
        this.getRequestProcessers.add(new OtherGetRequestProcesser(this.mContext));
    }
    private void addPostRequestProcessers(){
        this.postRequestProcessers.add(new InputRequestProcesser(this.mContext, this));
        this.postRequestProcessers.add(new UploadRequestProcesser(this.mContext));
        this.postRequestProcessers.add(new AppRequestProcesser(this.mContext));
        this.postRequestProcessers.add(new PlayRequestProcesser(this.mContext));
        this.postRequestProcessers.add(new FileRequestProcesser(this.mContext));
        this.postRequestProcessers.add(new TVRequestProcesser(this.mContext));
        this.postRequestProcessers.add(new TorrentRequestProcesser(this.mContext));
        this.postRequestProcessers.add(new OtherPostRequestProcesser(this.mContext));
    }


    @Override
    public Response serve(IHTTPSession session) {
        Log.i(IMEService.TAG, "接收到HTTP请求：" + session.getMethod() + " " + session.getUri());
        if(!session.getUri().isEmpty()) {
            String fileName = session.getUri().trim();
            if (fileName.indexOf('?') >= 0) {
                fileName = fileName.substring(0, fileName.indexOf('?'));
            }
            if (session.getMethod() == Method.GET) {
                for(RequestProcesser processer : this.getRequestProcessers){
                    if(processer.isRequest(session, fileName)){
                        return processer.doResponse(session, fileName, session.getParms(), null);
                    }
                }
            } else if (session.getMethod() == Method.POST) {
                Map<String, String> files = new HashMap<String, String>();
                try {
                    session.parseBody(files);
                } catch (IOException ioex) {
                    return createPlainTextResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR,  "SERVER INTERNAL ERROR: IOException: " + ioex.getMessage());
                } catch (NanoHTTPD.ResponseException rex) {
                    return createPlainTextResponse(rex.getStatus(),  rex.getMessage());
                }
                for(RequestProcesser processer : this.postRequestProcessers){
                    if(processer.isRequest(session, fileName)){
                        return processer.doResponse(session, fileName, session.getParms(), files);
                    }
                }
            }
        }
        //default page: index.html
        return this.getRequestProcessers.get(0).doResponse(session, "", null, null);
    }
}
