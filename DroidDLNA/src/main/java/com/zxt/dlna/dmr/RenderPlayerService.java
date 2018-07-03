/*
 * RenderPlayerService.java
 * Description:
 * Author: zxt
 */

package com.zxt.dlna.dmr;

import com.zxt.dlna.dmp.GPlayer;
import com.zxt.dlna.dmp.IJKPlayer;
import com.zxt.dlna.util.Action;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import player.XLVideoPlayActivity;

public class RenderPlayerService extends Service {

	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onStart(Intent intent, int startId) {
		//xgf fix bug null point
		if (null != intent) {
			super.onStart(intent, startId);
			String type = intent.getStringExtra("type");
			Intent intent2;

			if (type.equals("audio") ||  type.equals("video")) {
				IJKPlayer.intentTo(IJKPlayer.class,this, intent.getStringExtra("playURI"),intent.getStringExtra("name"));
				/**
				intent2 = new Intent(this, IJKPlayer.class);
				intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent2.putExtra("videoTitle", intent.getStringExtra("name"));
				intent2.putExtra("videoPath", intent.getStringExtra("playURI"));
				intent2.putExtra("videoIndex", 0);
				startActivity(intent2);
				 **/
			} else {
				intent2 = new Intent(Action.DMR);
				intent2.putExtra("playpath", intent.getStringExtra("playURI"));
				sendBroadcast(intent2);
			}
		}
	}
}
