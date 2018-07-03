package xllib;

import android.content.Context;

import com.xunlei.downloadlib.XLDownloadManager;
import com.xunlei.downloadlib.XLTaskHelper;

/**
 * Created by kingt on 2018/2/2.
 */

public class DownloadManager {
    private Context context;

    private DownloadManager(){
        this.downloadTask = new DownloadTask();
    }

    private static volatile DownloadManager instance = null;

    public static DownloadManager instance() {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager();
                }
            }
        }
        return instance;
    }

    public void init(Context context){
        if(this.context == null) {
            XLTaskHelper.init(context);
        }
        this.context = context;
    }

    private DownloadTask downloadTask;
    public DownloadTask taskInstance(){
        return this.downloadTask;
    }
}
