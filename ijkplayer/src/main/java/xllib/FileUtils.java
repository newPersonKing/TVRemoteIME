package xllib;

/**
 * Created by kingt on 2018/1/30.
 */

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by kingt on 2018/1/30.
 */
public class FileUtils {

    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f M" : "%.1f M", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f K" : "%.1f K", f);
        } else
            return String.format("%d B", size);
    }

    public static String getFileExt(String fileName){
        if(TextUtils.isEmpty(fileName)) return "";
        int p = fileName.lastIndexOf('.');
        if(p != -1) {
            return fileName.substring(p).toLowerCase();
        }
        return "";
    }
    public static boolean isMediaFile(String fileName){
        switch (getFileExt(fileName)){
            case ".avi":
            case ".mp4":
            case ".m4v":
            case ".mkv":
            case ".mov":
            case ".mpeg":
            case ".mpg":
            case ".mpe":
            case ".rm":
            case ".rmvb":
            case ".3gp":
            case ".wmv":
            case ".asf":
            case ".asx":
            case ".dat":
            case ".vob":
            case ".m3u8":
                return true;
            default: return false;
        }
    }

    public static String getFileNameWithoutExt(String filePath){
        if(TextUtils.isEmpty(filePath)) return "";
        String fileName = filePath;
        int p = fileName.lastIndexOf(File.separatorChar);
        if(p != -1){
            fileName = fileName.substring(p + 1);
        }
        p = fileName.indexOf('.');
        if(p != -1){
            fileName = fileName.substring(0, p);
        }
        return fileName;
    }

    public static String getWebMediaFileName(String url){
        Uri uri = Uri.parse(url);
        return getFileName(uri.getPath());
    }

    public static String getFileName(String filePath){
        if(TextUtils.isEmpty(filePath)) return "";
        String fileName = filePath;
        int p = fileName.lastIndexOf(File.separatorChar);
        if(p != -1){
            fileName = fileName.substring(p + 1);
        }
        return fileName;
    }


    public static void deleteDirFiles(File file){
        File[] files =  file.listFiles();
        for (File f : files) {
            try {
                if(f.isDirectory())deleteDirFiles(f);
                f.delete();
            } catch (SecurityException e) {
            }
        }
    }

    public static boolean isLiveMedia(String url){
        if(TextUtils.isEmpty(url)) return false;
        String uri = url.toLowerCase();
        if(url.startsWith("http://") || url.startsWith("https://") || url.startsWith("rtmp://")  || url.startsWith("rtmps://") || url.startsWith("mms://")){
            return true;
        }else{
            return false;
        }
    }

    public static boolean isNetworkDownloadTask(String url){
        if(TextUtils.isEmpty(url)) return false;
        String uri = url.toLowerCase();
        if(uri.startsWith("thunder://") || url.startsWith("ftp://") || url.startsWith("http://")
                || url.startsWith("https://") || url.startsWith("ed2k://") || url.startsWith("magnet:?")){
            return true;
        }else{
            return false;
        }
    }

    public static String getMagnetHashCode(String url){
        if(TextUtils.isEmpty(url)) return "";
        int p = url.indexOf('=');
        if(p == -1) return "";
        String[] hashCode = url.substring(p).split(":");
        if(hashCode.length > 2){
            p = hashCode[2].indexOf('&');
            if(p != -1)hashCode[2] = hashCode[2].substring(0, p);
            return hashCode[2];
        }
        return "";
    }
}