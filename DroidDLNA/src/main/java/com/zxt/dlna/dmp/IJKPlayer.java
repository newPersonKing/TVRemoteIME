package com.zxt.dlna.dmp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.zxt.dlna.util.Action;

import player.XLVideoPlayActivity;
import tv.danmaku.ijk.media.player.IMediaPlayer;
/**
 * Created by kingt on 2018/4/10.
 */
public class IJKPlayer extends XLVideoPlayActivity
{
    private static final int MESSAGE_UPDATE_PROGRESS = 10001;
    private Handler selfHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_UPDATE_PROGRESS:
                    if(mVideoView.isPlaying() && mMediaListener != null) {
                        int position = mVideoView.getCurrentPosition();
                        int duration = mVideoView.getDuration();
                        mMediaListener.positionChanged(position);
                        mMediaListener.durationChanged(duration);

                        selfHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS, 500);
                    }
                    break;
            }
        }
    };

    public static GPlayer.MediaListener mMediaListener;

    public static void setMediaListener(GPlayer.MediaListener mediaListener) {
        mMediaListener = mediaListener;
    }

    private void sendUpdateMessage(){
        selfHandler.removeMessages(MESSAGE_UPDATE_PROGRESS);
        selfHandler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS, 500);
    }

    @Override
    protected void startDownloadTask(String videoPath, int videoIndex) {
        super.startDownloadTask(videoPath, videoIndex);
        //非直播源
        isLive = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerBrocast();
    }

    @Override
    protected void onDestroy() {
        unregisterBrocast();
        mMediaListener = null;
        super.onDestroy();
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        if (null != mMediaListener) {
            mMediaListener.endOfMedia();
        }
        super.onCompletion(iMediaPlayer);
        selfHandler.removeMessages(MESSAGE_UPDATE_PROGRESS);
    }

    @Override
    protected void setVideoPath(String path) {
        super.setVideoPath(path);
        if (null != mMediaListener) {
            mMediaListener.start();
        }
        this.sendUpdateMessage();
    }

    @Override
    protected void start() {
        super.start();
        if (null != mMediaListener) {
            mMediaListener.start();
        }
        this.sendUpdateMessage();
    }
    @Override
    protected void pause() {
        super.pause();
        if (null != mMediaListener) {
            mMediaListener.pause();
        }
        selfHandler.removeMessages(MESSAGE_UPDATE_PROGRESS);
    }
    @Override
    protected void resume() {
        super.resume();
        if (null != mMediaListener) {
            mMediaListener.start();
        }
        this.sendUpdateMessage();
    }
    @Override
    protected void stop() {
        if (null != mMediaListener) {
            mMediaListener.stop();
        }
        super.stop();
        selfHandler.removeMessages(MESSAGE_UPDATE_PROGRESS);
    }

    @Override
    protected void seekTo(int position) {
        super.seekTo(position);
        if (null != mMediaListener) {
            mMediaListener.positionChanged(position);
        }
    }

    @Override
    public void finish() {
        mMediaListener = null;
        super.finish();
    }

    private PlayBrocastReceiver playRecevieBrocast = new PlayBrocastReceiver();

    public void registerBrocast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Action.DMR);
        intentFilter.addAction(Action.VIDEO_PLAY);
        registerReceiver(this.playRecevieBrocast, intentFilter);
    }

    public void unregisterBrocast() {
        unregisterReceiver(this.playRecevieBrocast);
    }

    class PlayBrocastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String str1 = intent.getStringExtra("helpAction");

            if (str1.equals(Action.PLAY)) {
                doPauseResume();
            } else if (str1.equals(Action.PAUSE)) {
                doPauseResume();
            } else if (str1.equals(Action.SEEK)) {
                boolean isPaused = false;
                if (!mVideoView.isPlaying()) {
                    isPaused = true;
                }
                int position = intent.getIntExtra("position", 0);
                seekTo(position);
            } else if (str1.equals(Action.SET_VOLUME)) {

                long eventTime = SystemClock.uptimeMillis();

                double vol = intent.getDoubleExtra("volume", 0);
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                int curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int volume = (int) (vol * maxVolume);
                // 变更声音
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);

                Log.d("IJKPlayer", "set_volume:" + vol + ", curVol:" + curVol + ", newVol:" + volume);
                final int i = (int) (volume * 1.0 / maxVolume * 100);
                final String s = i == 0 ? "" : i + "%";
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        $.id(com.afap.ijkplayer.R.id.app_video_volume_icon).image(i == 0 ? com.afap.ijkplayer.R.drawable.ic_volume_off_white_36dp : com.afap.ijkplayer.R.drawable
                                .ic_volume_up_white_36dp);

                        $.id(com.afap.ijkplayer.R.id.app_video_volume_box).visible();
                        $.id(com.afap.ijkplayer.R.id.app_video_volume).text(s).visible();

                        handler.removeMessages(MESSAGE_HIDE_CENTER_BOX);
                        handler.sendEmptyMessageDelayed(MESSAGE_HIDE_CENTER_BOX, 500);
                    }
                });
            } else if (str1.equals(Action.STOP)) {
                mVideoView.stopPlayback();
            }
        }
    }
}
