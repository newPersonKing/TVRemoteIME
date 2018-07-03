package player;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager.WakeLock;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afap.ijkplayer.R;
import com.xunlei.downloadlib.parameter.XLConstant;
import com.xunlei.downloadlib.parameter.XLTaskInfo;

import player.settings.GlobalSettings;
import player.widget.media.IjkVideoView;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import xllib.FileUtils;
import xllib.PlayListItem;
import xllib.PlayListItemAdapter;
import xllib.views.FocusFixedLinearLayoutManager;


public class XLVideoPlayActivity extends Activity implements IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnInfoListener {

    private final String TAG = "XLVideoPlayActivity";
    private static final int MESSAGE_SHOW_PROGRESS = 1;
    private static final int MESSAGE_FADE_OUT = 2;
    private static final int MESSAGE_SEEK_NEW_POSITION = 3;
    protected static final int MESSAGE_HIDE_CENTER_BOX = 4;
    private static final int MESSAGE_RESTART_PLAY = 5;
    private static final int MESSAGE_LIVE_RESTART = 6;

    private static boolean isRunning = false;
    private static XLVideoPlayActivity runningInstance = null;

    private String mVideoPath;
    private String mVideoTitle;
    private int mVideoIndex;
    private Uri mVideoUri;

    protected IjkVideoView mVideoView;
    private TableLayout mHudView;

    protected Query $;
    private boolean isShowing = false;
    private float brightness = -1;
    private int volume = -1;
    private int newPosition = -1;
    protected boolean isLive = false;//是否为直播
    private boolean isLiveRestarted = false;
    private int screenWidthPixels;
    protected AudioManager audioManager;
    private int mMaxVolume;
    private boolean isDragging;
    private int defaultTimeout = 2000;
    private boolean fullScreenOnly = false;
    private SeekBar seekBar;
    private int duration;
    private boolean instantSeeking = false;
    private boolean portrait;
    private int currentPosition;

    private WakeLock mWakeLock = null;

    private View mRoot;

    private int STATUS_ERROR = -1;
    private int STATUS_IDLE = 0;
    private int STATUS_LOADING = 1;
    private int STATUS_PLAYING = 2;
    private int STATUS_PAUSE = 3;
    private int STATUS_COMPLETED = 4;
    private int status = STATUS_IDLE;

    private xllib.DownloadManager xlDownloadManager = xllib.DownloadManager.instance();
    private RecyclerView playListView;
    private PlayListItemAdapter playListItemAdapter;

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /**if (v.getId() == R.id.app_video_fullscreen) {
                if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                updateFullScreenButton();
            } else **/ if (v.getId() == R.id.app_video_play) {
                doPauseResume();
                show(defaultTimeout);
            } else if (v.getId() == R.id.app_video_replay_icon) {
                seekTo(0);
                start();
                doPauseResume();
            }else if(v.getId() == R.id.app_play_btn_play_list){
                if(playListView != null){
                    if(playListView.isShown()){
                        playListView.setVisibility(View.GONE);
                    }else {
                        playListView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    };
    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //if (!fromUser)
            //    return;
            int newPosition = (int) ((duration * progress * 1.0) / 100);
            String time = generateTime(newPosition);
            if (instantSeeking) {
                seekTo(newPosition);
            }
            $.id(R.id.app_video_currentTime).text(time);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isDragging = true;
            show(3600000);
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            if (instantSeeking) {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (!instantSeeking) {
                seekTo((int) ((duration * seekBar.getProgress() * 1.0) / 100));
            }
            show(defaultTimeout);
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            isDragging = false;
            handler.sendEmptyMessageDelayed(MESSAGE_SHOW_PROGRESS, 1000);
        }
    };

    public static <T extends XLVideoPlayActivity> Intent newIntent(Class<T> cls, Context context, String videoPath, String videoTitle, int videoIndex) {
        Intent intent = new Intent(context, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("videoPath", videoPath);
        intent.putExtra("videoTitle", videoTitle);
        intent.putExtra("videoIndex", videoIndex);
        return intent;
    }
    public static <T extends XLVideoPlayActivity> void intentTo(Class<T> cls, Context context, String videoPath, String videoTitle) {
        intentTo(cls, context, videoPath, videoTitle, 0);
    }
    public static <T extends XLVideoPlayActivity> void intentTo(Class<T> cls, Context context, String videoPath, String videoTitle, int videoIndex) {
        if(isRunning && runningInstance != null){
            if(runningInstance.getClass() == cls) {
                runningInstance.resetVideoPath(videoPath, videoIndex);
            }else{
                runningInstance.finish();
                context.startActivity(newIntent(cls, context, videoPath, videoTitle, videoIndex));
            }
        }
        else {
            context.startActivity(newIntent(cls, context, videoPath, videoTitle, videoIndex));
        }
    }

    private void resetVideoPath(final String videoPath, final int videoIndex){
        if(!TextUtils.isEmpty(videoPath)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(videoPath.equalsIgnoreCase(mVideoPath)){
                        if(videoIndex != mVideoIndex){
                            resetVideoIndex(videoIndex);
                        }
                    }else {
                        stop();
                        $.id(R.id.app_video_loading).visible();
                        startDownloadTask(videoPath, videoIndex);
                        playListItemAdapter.notifyDataSetChanged();
                        handler.sendEmptyMessageDelayed(XLVideoPlayActivity.MESSAGE_RESTART_PLAY, 3000);
                    }
                }
            });
        }
    }

    private void resetVideoIndex(int videoIndex){
        if(videoIndex != -1 && xlDownloadManager.taskInstance().getPlayList().size() > 1) {
            stop();
            $.id(R.id.app_video_loading).visible();
            if(xlDownloadManager.taskInstance().changePlayItem(videoIndex)) {
                playListItemAdapter.notifyDataSetChanged();
                handler.sendEmptyMessageDelayed(XLVideoPlayActivity.MESSAGE_RESTART_PLAY, 6000);
            }
        }
    }

    protected void startDownloadTask(String videoPath,  int videoIndex){
        if(TextUtils.isEmpty(videoPath)) {
            Toast.makeText(this, "没有视频播放资源，退出播放任务.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        xlDownloadManager.taskInstance().setUrl(videoPath);
        if(videoIndex > 0)xlDownloadManager.taskInstance().changePlayItem(videoIndex);
        if(!xlDownloadManager.taskInstance().startTask() ||
                TextUtils.isEmpty(xlDownloadManager.taskInstance().getPlayUrl())){
            Toast.makeText(this, "无法运行资源下载任务，退出播放任务.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "无法运行资源下载任务，退出播放任务.");
            finish();
            return;
        }
        isLive = xlDownloadManager.taskInstance().isLiveMedia();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xl_play_video);

        xlDownloadManager.init(getApplicationContext());

//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        mVideoPath = getIntent().getStringExtra("videoPath");
        mVideoTitle = getIntent().getStringExtra("videoTitle");
        mVideoIndex = getIntent().getIntExtra("videoIndex", 0);

        Intent intent = getIntent();
        String intentAction = intent.getAction();
        if (!TextUtils.isEmpty(intentAction)) {
            if (intentAction.equals(Intent.ACTION_VIEW)) {
                mVideoPath = intent.getDataString();
            } else if (intentAction.equals(Intent.ACTION_SEND)) {
                mVideoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    String scheme = mVideoUri.getScheme();
                    if (TextUtils.isEmpty(scheme)) {
                        Log.e(TAG, "Null unknown scheme\n");
                        finish();
                        return;
                    }
                    if (scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                        mVideoPath = mVideoUri.getPath();
                    } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                        Log.e(TAG, "Can not resolve content below Android-ICS\n");
                        finish();
                        return;
                    } else {
                        Log.e(TAG, "Unknown scheme " + scheme + "\n");
                        finish();
                        return;
                    }
                }
            }
        }
        if(mVideoPath == null && mVideoUri != null){
            mVideoPath = mVideoUri.toString();
        }

        if(TextUtils.isEmpty(mVideoPath)){
            Toast.makeText(this, "没有播放资源地址，退出播放任务。", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        startDownloadTask(mVideoPath, mVideoIndex);

        playListView = (RecyclerView)findViewById(R.id.play_list_view);
        playListView.setLayoutManager(new FocusFixedLinearLayoutManager(this));
        playListView.setItemAnimator(new DefaultItemAnimator());
        playListView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        playListItemAdapter = new PlayListItemAdapter();
        playListItemAdapter.setOnPlayListItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playListView.setVisibility(View.GONE);
                int index = ((PlayListItem)view.getTag()).getIndex();
                resetVideoIndex(index);
            }
        });
        playListView.setAdapter(playListItemAdapter);

        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        mVideoView = (IjkVideoView) findViewById(R.id.video_view);
        //mVideoView.setVideoPath(xlDownloadManager.taskInstance().getPlayUrl());
        //Log.d(TAG, "playing url = " + xlDownloadManager.taskInstance().getPlayUrl());
//        mHudView = (TableLayout) findViewById(R.id.hud_view);
//        mVideoView.setHudView(mHudView);
//        mVideoView.setMediaController(mMediaController);
        // prefer mVideoPath
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnInfoListener(this);


        $ = new Query(this);
        seekBar = (SeekBar) findViewById(R.id.app_video_seekBar);
        seekBar.setOnSeekBarChangeListener(mSeekListener);

        $.id(R.id.app_video_play).clicked(onClickListener);
        //$.id(R.id.app_video_fullscreen).clicked(onClickListener);
        $.id(R.id.app_video_replay_icon).clicked(onClickListener);
        $.id(R.id.app_play_btn_play_list).clicked(onClickListener);

        portrait = getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        screenWidthPixels = getResources().getDisplayMetrics().widthPixels;

        handler.sendEmptyMessageDelayed(XLVideoPlayActivity.MESSAGE_RESTART_PLAY, 2000);

        isRunning = true;
        runningInstance = this;
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        finish();
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        switch (i) {
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                if(isLive){
                    isLiveRestarted = false;
                    Log.d(TAG, "MSG:MESSAGE_LIVE_RESTART -> begin");
                    handler.sendEmptyMessageDelayed(MESSAGE_LIVE_RESTART, 3000);
                }
                statusChange(STATUS_LOADING);
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                if(isLive && !isLiveRestarted) {
                    Log.d(TAG, "MSG:MESSAGE_LIVE_RESTART -> remove");
                    handler.removeMessages(MESSAGE_LIVE_RESTART);
                }
                statusChange(STATUS_PLAYING);
                break;
            case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                //显示下载速度
                Log.i(TAG,"onInfo : i=" + i + ",i1=" + i1);
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                statusChange(STATUS_PLAYING);
                break;
        }
        return false;
    }

    /**
     * 播放准备就绪
     */
    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        duration = mVideoView.getDuration();

        final GestureDetector gestureDetector = new GestureDetector(this, new PlayerGestureListener());
        mRoot = findViewById(R.id.touch_area);
        mRoot.setClickable(true);
        mRoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (gestureDetector.onTouchEvent(motionEvent))
                    return true;

                // 处理手势结束
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        endGesture();
                        break;
                }

                return false;
            }
        });

        start();
    }
    /**
     * 获取当前播放位置
     */
    public int getCurrentPosition() {
        if (!isLive) {
            currentPosition = mVideoView.getCurrentPosition();
        } else {
            /**直播*/
            currentPosition = -1;
        }
        return currentPosition;
    }

    protected void seekTo(int position){
        mVideoView.seekTo(position);
    }
    protected void start(){
        mVideoView.start();
    }
    protected void resume(){
        mVideoView.resume();
    }
    protected void setVideoPath(String path){
        if(TextUtils.isEmpty(path)){
            Toast.makeText(this, "没有视频播放资源，退出播放任务.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mVideoView.setVideoPath(path);
    }
    protected void pause(){
        mVideoView.pause();
    }
    protected void stop(){
        mVideoView.stopPlayback();
    }

    protected void doPauseResume() {
        if (status == STATUS_COMPLETED) {
            $.id(R.id.app_video_replay).gone();
            currentPosition = 0;
            seekTo(0);
            start();
        } else if (mVideoView.isPlaying()) {
            getCurrentPosition();
            statusChange(STATUS_PAUSE);
            pause();
        } else {
            if (isLive) {
                resume();
                //seekTo(0);
            } else {
                start();
                //seekTo(currentPosition);
            }
        }
        updatePausePlay();
    }

    private void updateFullScreenButton() {
        if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            //$.id(R.id.app_video_fullscreen).image(R.drawable.ic_fullscreen_exit_white_36dp);
        } else {
            //$.id(R.id.app_video_fullscreen).image(R.drawable.ic_fullscreen_white_24dp);
        }
    }

    private void statusChange(int newStatus) {
        status = newStatus;
        if (!isLive && newStatus == STATUS_COMPLETED) {
            currentPosition = 0;
            hideAll();
            $.id(R.id.app_video_replay).visible();
        } else if (newStatus == STATUS_ERROR) {
            hideAll();
        } else if (newStatus == STATUS_LOADING) {
            hideAll();
            $.id(R.id.app_video_loading).visible();
        } else if (newStatus == STATUS_PLAYING) {
            hideAll();
        }

    }

    private void hideAll() {
        $.id(R.id.app_video_replay).gone();
        $.id(R.id.app_video_loading).gone();
        //$.id(R.id.app_video_fullscreen).invisible();
        showBottomControl(false);
    }

    public void hide(boolean force) {
        if (force || isShowing) {
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            showBottomControl(false);
            //$.id(R.id.app_video_fullscreen).invisible();
            isShowing = false;
        }
    }

    private boolean changeProgressByKey = false;
    private int oldProgressValue = -1;
    private int newProgressValue = -1;
    private int keyDownComboCount = 0;
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if(changeProgressByKey){
                    changeProgressByKey = false;
                    oldProgressValue = -1;
                    endGesture();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if(keyDownComboCount > 20){
                    resume();
                }
                keyDownComboCount = 0;
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_ESCAPE:
            case KeyEvent.KEYCODE_BACK:
                if(playListView.isShown()) {
                    show(defaultTimeout);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if(!changeProgressByKey)changeProgressByKey = true;
                if(oldProgressValue == -1){
                    oldProgressValue = 0;
                    newProgressValue = oldProgressValue;
                }
                newProgressValue += keyCode == KeyEvent.KEYCODE_DPAD_LEFT ? -GlobalSettings.FastForwardInterval : GlobalSettings.FastForwardInterval;
                int max = mVideoView.getDuration();
                //Log.d(TAG, "newProgressValue = " + newProgressValue);
                if(newProgressValue < (0 - max))newProgressValue = (0 - max);
                if(newProgressValue > max)newProgressValue = max;
                float deltaP = oldProgressValue - newProgressValue;
                onProgressSlide(-deltaP / max);
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_UP:
                if(playListView.isShown()){
                    View view = playListView.getLayoutManager().getFocusedChild();
                    if(view != null){
                        View nextView = playListView.getLayoutManager().onInterceptFocusSearch(view, keyCode == KeyEvent.KEYCODE_DPAD_DOWN ? View.FOCUS_DOWN : View.FOCUS_UP);
                        if(nextView != null)nextView.requestFocus();
                    }else {
                        playListView.requestFocus(keyCode == KeyEvent.KEYCODE_DPAD_DOWN ? View.FOCUS_DOWN : View.FOCUS_UP);
                    }
                    return true;
                }else if(xlDownloadManager.taskInstance().getPlayList().size() > 1){
                    playListView.setVisibility(View.VISIBLE);
                    return true;
                }else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                    keyDownComboCount ++;
                    //Log.d(TAG, "keyDownComboCount = " + keyDownComboCount);
                }
                break;
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                doPauseResume();
                show(defaultTimeout);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * @param timeout
     */
    public void show(int timeout) {
        if (!isShowing) {
            showBottomControl(true);
            if (!fullScreenOnly) {
                //$.id(R.id.app_video_fullscreen).visible();
            }
            isShowing = true;
        }
        updatePausePlay();
        handler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS);
        handler.removeMessages(MESSAGE_FADE_OUT);
        if (timeout != 0) {
            handler.sendMessageDelayed(handler.obtainMessage(MESSAGE_FADE_OUT), timeout);
        }
    }

    private void showBottomControl(boolean show) {
        $.id(R.id.app_play_btn_play_list).visibility(show && xlDownloadManager.taskInstance().getPlayList().size() > 1 ? View.VISIBLE : View.GONE);
        $.id(R.id.app_video_play).visibility(show ? View.VISIBLE : View.GONE);
        $.id(R.id.app_video_speed).visibility(show ? View.VISIBLE : View.GONE);
        $.id(R.id.app_video_currentTime).visibility(show ? View.VISIBLE : View.GONE);
        $.id(R.id.app_video_endTime).visibility(show ? View.VISIBLE : View.GONE);
        $.id(R.id.app_video_seekBar).visibility(show ? View.VISIBLE : View.GONE);
        if(show && playListView.isShown())playListView.setVisibility(View.GONE);
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    protected void onVolumeSlide(float percent) {
        if (volume == -1) {
            volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (volume < 0)
                volume = 0;
        }
        hide(true);

        int index = (int) (percent * mMaxVolume) + volume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

        // 变更进度条
        int i = (int) (index * 1.0 / mMaxVolume * 100);
        String s = i + "%";
        if (i == 0) {
            s = "";
        }
        // 显示
        $.id(R.id.app_video_volume_icon).image(i == 0 ? R.drawable.ic_volume_off_white_36dp : R.drawable
                .ic_volume_up_white_36dp);
        $.id(R.id.app_video_brightness_box).gone();
        $.id(R.id.app_video_volume_box).visible();
        $.id(R.id.app_video_volume).text(s).visible();
    }

    /**
     * 滑动改变亮度
     */
    private void onBrightnessSlide(float percent) {
        if (brightness < 0) {
            brightness = getWindow().getAttributes().screenBrightness;
            if (brightness <= 0.00f) {
                brightness = 0.50f;
            } else if (brightness < 0.01f) {
                brightness = 0.01f;
            }
        }
        $.id(R.id.app_video_brightness_box).visible();
        WindowManager.LayoutParams lpa = getWindow().getAttributes();
        lpa.screenBrightness = brightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        $.id(R.id.app_video_brightness).text(((int) (lpa.screenBrightness * 100)) + "%");
        getWindow().setAttributes(lpa);

    }

    protected int setProgress() {
        if (isDragging) {
            return 0;
        }

        XLTaskInfo xlTaskInfo = xlDownloadManager.taskInstance().getTaskInfo();

        int position = mVideoView.getCurrentPosition();
        int duration = mVideoView.getDuration();
        if (seekBar != null) {
            if (duration > 0) {
                seekBar.setProgress((position * 100 / duration));
                if(xlTaskInfo != null){
                    seekBar.setSecondaryProgress((int)Math.floor((double)xlTaskInfo.mDownloadSize * 100 / xlTaskInfo.mFileSize));
                }
            }
        }

        this.duration = duration;

        $.id(R.id.app_video_currentTime).text(generateTime(position));
        $.id(R.id.app_video_endTime).text(generateTime(this.duration));
        $.id(R.id.app_video_speed).text(FileUtils.convertFileSize(mVideoView.getTcpSpeed()) + "/s");
        return position;
    }

    private String generateTime(int time) {
        int totalSeconds = time / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d",
                minutes, seconds);
    }

    private void onProgressSlide(float percent) {
        int position = mVideoView.getCurrentPosition();
        int duration = mVideoView.getDuration();
        int deltaMax = duration - position; //Math.min(300000, duration - position);
        int delta = (int) (deltaMax * percent);

        newPosition = delta + position;
        if (newPosition > duration) {
            newPosition = duration;
        } else if (newPosition <= 0) {
            newPosition = 0;
            delta = -position;
        }
        int showDelta = delta / 1000;
        if (showDelta != 0) {
            $.id(R.id.app_video_fastForward_box).visible();
            String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
            $.id(R.id.app_video_fastForward).text(text + "s");
            $.id(R.id.app_video_fastForward_target).text(generateTime(newPosition) + "/");
            $.id(R.id.app_video_fastForward_all).text(generateTime(duration));
        }
    }

    protected void updatePausePlay() {
        if (mVideoView.isPlaying()) {
            $.id(R.id.app_video_play).image(R.drawable.ic_stop_white_24dp);
        } else {
            $.id(R.id.app_video_play).image(R.drawable.ic_play_arrow_white_24dp);
        }
    }

    /**
     * 手势结束
     */
    private void endGesture() {
        volume = -1;
        brightness = -1f;
        if (newPosition >= 0) {
            handler.removeMessages(MESSAGE_SEEK_NEW_POSITION);
            handler.sendEmptyMessage(MESSAGE_SEEK_NEW_POSITION);
        }
        handler.removeMessages(MESSAGE_HIDE_CENTER_BOX);
        handler.sendEmptyMessageDelayed(MESSAGE_HIDE_CENTER_BOX, 500);

    }

    private int getScreenOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }

    protected Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_FADE_OUT:
                    hide(false);
                    break;
                case MESSAGE_HIDE_CENTER_BOX:
                    $.id(R.id.app_video_volume_box).gone();
                    $.id(R.id.app_video_brightness_box).gone();
                    $.id(R.id.app_video_fastForward_box).gone();
                    break;
                case MESSAGE_SEEK_NEW_POSITION:
                    if (!isLive && newPosition >= 0) {
                        seekTo(newPosition);
                        newPosition = -1;
                        if (!mVideoView.isPlaying()) {
                            doPauseResume();
                        }
                    }
                    break;
                case MESSAGE_SHOW_PROGRESS:
                    int pos = setProgress();
                    if (!isDragging && isShowing) {
                        msg = obtainMessage(MESSAGE_SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1 - pos);
                        updatePausePlay();
                    }
                    break;
                case MESSAGE_RESTART_PLAY:
                    if(mVideoView.isPlaying()){
                        stop();
                    }
                    $.id(R.id.app_video_loading).visible();
                    String uri = xlDownloadManager.taskInstance().getPlayUrl();
                    if(TextUtils.isEmpty(uri)) {
                        Toast.makeText(XLVideoPlayActivity.this, "没有播放资源地址，退出播放任务。", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    setVideoPath(uri);
                    seekTo(0);
                    Log.d(TAG, "playing url = " + uri);
                    break;
                case MESSAGE_LIVE_RESTART:
                    Log.d(TAG, "MSG:MESSAGE_LIVE_RESTART -> handle:" + status);
                    if(status == STATUS_LOADING){
                        resume();
                    }
                    isLiveRestarted = true;
                    break;
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView.isPlaying()) {
            getCurrentPosition();
            pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mWakeLock && (!mWakeLock.isHeld())) {
            mWakeLock.acquire();
        }
        if (!mVideoView.isPlaying()) {
            resume();
            if (isLive) {
                seekTo(0);
            } else {
                seekTo(currentPosition);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mVideoView.isPlaying()) {
            currentPosition = 0;
            pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        runningInstance = null;
        isRunning = false;

        if(mVideoView != null) stop();
        xlDownloadManager.taskInstance().stopTask();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }

    protected class Query {
        private final Activity activity;
        private View view;

        public Query(Activity activity) {
            this.activity = activity;
        }

        public Query id(int id) {
            view = activity.findViewById(id);
            return this;
        }

        public Query image(int resId) {
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(resId);
            }
            return this;
        }

        public Query visible() {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
            return this;
        }

        public Query gone() {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
            return this;
        }

        public Query invisible() {
            if (view != null) {
                view.setVisibility(View.INVISIBLE);
            }
            return this;
        }

        public Query clicked(View.OnClickListener handler) {
            if (view != null) {
                view.setOnClickListener(handler);
            }
            return this;
        }

        public Query text(CharSequence text) {
            if (view != null && view instanceof TextView) {
                ((TextView) view).setText(text);
            }
            return this;
        }

        public Query visibility(int visible) {
            if (view != null) {
                view.setVisibility(visible);
            }
            return this;
        }

        private void size(boolean width, int n, boolean dip) {
            if (view != null) {
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                if (n > 0 && dip) {
                    n = dip2pixel(activity, n);
                }
                if (width) {
                    lp.width = n;
                } else {
                    lp.height = n;
                }
                view.setLayoutParams(lp);
            }
        }

        public void height(int height, boolean dip) {
            size(false, height, dip);
        }

        public int dip2pixel(Context context, float n) {
            int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, n, context.getResources()
                    .getDisplayMetrics());
            return value;
        }

        public float pixel2dip(Context context, float n) {
            Resources resources = context.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float dp = n / (metrics.densityDpi / 160f);
            return dp;

        }
    }

    public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean firstTouch;
        private boolean volumeControl;
        private boolean toSeek;

        /**
         * 双击
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
//            videoView.toggleAspectRatio();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            firstTouch = true;
            return super.onDown(e);

        }

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            float deltaY = mOldY - e2.getY();
            float deltaX = mOldX - e2.getX();
            if (firstTouch) {
                toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
                volumeControl = mOldX > screenWidthPixels * 0.5f;
                firstTouch = false;
            }

            if (toSeek) {
                if (!isLive) {
                    onProgressSlide(-deltaX / mRoot.getWidth());
                }
            } else {
                float percent = deltaY / mRoot.getHeight();
                if (volumeControl) {
                    onVolumeSlide(percent);
                } else {
                    onBrightnessSlide(percent);
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isShowing) {
                hide(false);
            } else {
                show(defaultTimeout);
            }
            return true;
        }
    }
}