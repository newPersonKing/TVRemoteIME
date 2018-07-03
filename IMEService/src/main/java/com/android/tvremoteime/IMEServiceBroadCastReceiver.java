package com.android.tvremoteime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.tvremoteime.adb.AdbHelper;

/**
 * Created by kingt on 2018/3/5.
 */

public class IMEServiceBroadCastReceiver extends BroadcastReceiver {
    private final String TAG = "IMEServiceBCR";
    private final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
    private final String MEDIA_MOUNTED = "android.intent.action.MEDIA_MOUNTED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "receive msg:" + intent.getAction());
       if (ACTION_BOOT.equals(intent.getAction()) ||
               MEDIA_MOUNTED.equals(intent.getAction())) {
           if(!Environment.isDefaultIME(context)) {
               Log.d(TAG, "startService.....");
               context.startService(new Intent(IMEService.ACTION));
               if(AdbHelper.getInstance() == null) AdbHelper.createInstance();
           }
       }
    }
}
