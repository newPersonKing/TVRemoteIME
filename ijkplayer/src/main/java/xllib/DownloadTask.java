package xllib;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.xunlei.downloadlib.XLTaskHelper;
import com.xunlei.downloadlib.parameter.TorrentFileInfo;
import com.xunlei.downloadlib.parameter.TorrentInfo;
import com.xunlei.downloadlib.parameter.XLTaskInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kingt on 2018/2/2.
 */

public class DownloadTask {
    public interface DownloadTaskBaseDirGetter{
        public abstract File getBaseDir();
    }

    private final static String TAG = "DownloadTask";
    private static File defaultBaseDir = new File(Environment.getExternalStorageDirectory(), "xlplayer");
    private static DownloadTaskBaseDirGetter downloadTaskBaseDirGetter;
    public static void setBaseDirGetter(DownloadTaskBaseDirGetter baseDirGetter){
        DownloadTask.downloadTaskBaseDirGetter = baseDirGetter;
    }
    private File getBaseDir(){
        DownloadTaskBaseDirGetter baseDirGetter = DownloadTask.downloadTaskBaseDirGetter;
        if(baseDirGetter != null){
            return baseDirGetter.getBaseDir();
        }else {
            return defaultBaseDir;
        }
    }

    public DownloadTask(){

    }

    private String url;
    private String name;
    private long taskId;
    private String localSavePath;
    private boolean mIsLiveMedia;
    private boolean isNetworkDownloadTask;
    private boolean isLocalMedia;
    private TorrentInfo torrentInfo;
    private int[] torrentMediaIndexs = null;
    private int[] torrentUnmediaIndexs = null;
    private int currentPlayMediaIndex = 0;
    private List<PlayListItem> playList = new ArrayList<>();

    public boolean isLiveMedia(){
        return this.mIsLiveMedia;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        //删除旧任务及文件
        this.stopTask();

        this.url = url;
        this.playList.clear();
        this.mIsLiveMedia = FileUtils.isLiveMedia(this.url);
        this.isNetworkDownloadTask = !this.mIsLiveMedia && FileUtils.isNetworkDownloadTask(this.url);
        this.name = this.mIsLiveMedia ? FileUtils.getWebMediaFileName(this.url) :
                     this.isNetworkDownloadTask ? XLTaskHelper.instance().getFileName(this.url) : FileUtils.getFileName(this.url);
        this.localSavePath = (new File(getBaseDir(), FileUtils.getFileNameWithoutExt(this.name)).toString()) + "/";
        this.isLocalMedia = !this.mIsLiveMedia && !this.isNetworkDownloadTask && FileUtils.isMediaFile(this.name);
        this.torrentInfo = null;
        this.torrentMediaIndexs = null;
        this.torrentUnmediaIndexs = null;
        this.currentPlayMediaIndex = 0;
        if(this.isLocalMedia){
            playList.add(new PlayListItem(this.name, 0, new File(this.getUrl()).length()));
        }else if(this.mIsLiveMedia || this.isNetworkDownloadTask){
            playList.add(new PlayListItem(this.name, 0, 0L));
        } else if (".torrent".equals(FileUtils.getFileExt(this.name))) {
            this.torrentInfo = XLTaskHelper.instance().getTorrentInfo(this.url);
            this.initTorrentIndexs();
        }
    }

    private void initTorrentIndexs(){
        this.currentPlayMediaIndex = -1;
        if(this.torrentInfo != null  && this.torrentInfo.mSubFileInfo != null){
            ArrayList<Integer> mediaIndexs = new ArrayList<>();
            ArrayList<Integer> unmediaIndexs = new ArrayList<>();
            for (int i = 0; i < torrentInfo.mSubFileInfo.length; i++) {
                TorrentFileInfo torrentFileInfo = torrentInfo.mSubFileInfo[i];
                if(FileUtils.isMediaFile(torrentFileInfo.mFileName)){
                    mediaIndexs.add(torrentFileInfo.mFileIndex);
                    playList.add(new PlayListItem(
                            TextUtils.isEmpty(torrentFileInfo.mSubPath) ? torrentFileInfo.mFileName :
                                    torrentFileInfo.mSubPath + "/" + torrentFileInfo.mFileName,
                            torrentFileInfo.mFileIndex, torrentFileInfo.mFileSize));
                }else{
                    unmediaIndexs.add(torrentFileInfo.mFileIndex);
                }
            }
            this.torrentMediaIndexs = new int[mediaIndexs.size()];
            this.torrentUnmediaIndexs = new int[unmediaIndexs.size()];
            for(int i=0; i<mediaIndexs.size(); i++)this.torrentMediaIndexs[i] = mediaIndexs.get(i);
            for(int i=0; i<unmediaIndexs.size(); i++)this.torrentUnmediaIndexs[i] = unmediaIndexs.get(i);
            this.currentPlayMediaIndex = this.torrentMediaIndexs.length > 0 ? this.torrentMediaIndexs[0] : -1;
        }
    }

    private int[] getTorrentDeselectedIndexs(){
        int[] indexs = new int[this.torrentUnmediaIndexs.length + this.torrentMediaIndexs.length -1];
        int offset = 0;
        for(int idx : this.torrentMediaIndexs){
            if(idx != this.currentPlayMediaIndex) {
                indexs[offset++] = idx;
            }
        }
        for(int idx : this.torrentUnmediaIndexs){
            indexs[offset ++] = idx;
        }
        return indexs;
    }

    public List<PlayListItem> getPlayList(){
        return this.playList;
    }

    public String getPlayUrl(){
        if(this.isLocalMedia || this.mIsLiveMedia){
            return this.getUrl();
        }else if(this.taskId != 0L){
            if(this.isNetworkDownloadTask){
                return XLTaskHelper.instance().getLoclUrl(this.localSavePath + this.name);
            }else if(this.torrentInfo != null && this.currentPlayMediaIndex != -1){
                for(PlayListItem item : getPlayList()){
                    if(item.getIndex() == this.currentPlayMediaIndex){
                        return XLTaskHelper.instance().getLoclUrl(this.localSavePath + item.getName());
                    }
                }
            }
        }
        return null;
    }
    public boolean changePlayItem(int index){
        if(this.torrentInfo != null && index != this.currentPlayMediaIndex){
            this.currentPlayMediaIndex = index;
            if(this.taskId != 0L){
                this.stopTask();
                return this.startTask();
            }
            return true;
        }
        return false;
    }
    public PlayListItem getCurrentPlayItem(){
        for(PlayListItem item : this.getPlayList()){
            if(item.getIndex() == this.currentPlayMediaIndex) return item;
        }
        return null;
    }


    public boolean startTask(){
        if(TextUtils.isEmpty(this.url) || this.taskId != 0L){
            return false;
        }

        if(this.isNetworkDownloadTask){
            if(this.url.toLowerCase().startsWith("magnet:?")){
                Log.e(TAG, "暂时不支持magnet链的下载播放");
                return false;
            }else {
                taskId = XLTaskHelper.instance().addThunderTask(this.url, localSavePath, null);
            }
        }else if(this.torrentInfo != null) {
            if(this.currentPlayMediaIndex != -1) {
                try {
                    taskId = XLTaskHelper.instance().addTorrentTask(this.url, localSavePath, this.getTorrentDeselectedIndexs());
                } catch (Exception e) {
                }
            }
        }else {
            taskId = this.isLocalMedia || this.mIsLiveMedia ? -9999L : 0L;
        }
        Log.d(TAG, "startTask(" + this.url + "), taskId = " + taskId + ", index = " + currentPlayMediaIndex);
        return  taskId != 0L;
    }

    public void stopTask(){
        if(this.taskId != 0L){
            if(!this.isLocalMedia && !this.mIsLiveMedia) {
                XLTaskHelper.instance().deleteTask(this.taskId, this.localSavePath);
            }
            Log.d(TAG, "stopTask(" + this.url + "), taskId = " + taskId);
            this.taskId = 0L;
        }
    }
    public XLTaskInfo getTaskInfo(){
        return this.taskId == 0L || this.isLocalMedia || this.mIsLiveMedia ? null : XLTaskHelper.instance().getTaskInfo(this.taskId);
    }

    public String getName() {
        return name;
    }
}
