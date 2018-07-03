package com.android.tvremoteime.http;

import android.util.Log;

import com.android.tvremoteime.Environment;

import org.apache.http.util.CharArrayBuffer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
/**
 * Created by kingt on 2018/4/11.
 */
public class HTTPGet {
    public static String readString(String uri){
        HTTPSTrustManager.allowAllSSL();
        try {
            URL url = new URL(uri);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "text/html, application/xhtml+xml, image/jxr, */*");
            conn.setRequestProperty("Accept-Encoding", "identity");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
            int code = conn.getResponseCode();
            if (code == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"));
                try {
                    int length = conn.getContentLength();
                    if (length < 0) {
                        length = 4096;
                    }
                    CharArrayBuffer buffer = new CharArrayBuffer(length);
                    char[] tmp = new char[1024];

                    int l;
                    while ((l = reader.read(tmp)) != -1) {
                        buffer.append(tmp, 0, l);
                    }
                    return buffer.toString();
                }finally {

                    reader.close();
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.e("HTTPGet", "readString", e);
        }
        finally {

        }
        return null;
    }

    public static boolean downloadFile(String uri, File file){
        HTTPSTrustManager.allowAllSSL();
        try {
            URL url = new URL(uri);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "text/html, application/xhtml+xml, image/jxr, */*");
            conn.setRequestProperty("Accept-Encoding", "identity");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");

            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream instream = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);
                try {
                    byte[] tmp = new byte[4096];

                    int l;
                    while((l = instream.read(tmp)) != -1) {
                        fos.write(tmp, 0, l);
                    }
                    return true;
                } finally {
                    instream.close();
                    fos.close();
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e("HTTPGet", "downloadFile", e);
        }
        return false;
    }
}
