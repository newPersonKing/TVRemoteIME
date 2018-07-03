package com.android.tvremoteime;

import android.content.Context;
import android.nfc.Tag;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.List;

/**
 * Created by kingt on 2018/3/6.
 */

public class Environment {
    public final static boolean needDebug = false;

    public static int adbServerPort = 5555;

    private static Handler toastHandler = null;

    public static void debug(String tag, String msg){
        Log.d(tag, msg);
    }
    public static void debug(String tag, String msg, Throwable tr){
        Log.d(tag, msg, tr);
    }

    public static void initToastHandler(){
        if(toastHandler == null) toastHandler = new Handler();
    }
    public static void toastInHandler(final Context context, final String msg){
        if(toastHandler != null){
            toastHandler.post(new Runnable() {
                @Override
                public void run() {
                    toast(context, msg);
                }
            });
        }
    }
    public static void toast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static boolean isEnableIME(Context context){
        try {
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            List<InputMethodInfo> inputs = imm.getEnabledInputMethodList();
            boolean flag = false;
            for(InputMethodInfo input : inputs){
                if(input.getPackageName().equals(IMEService.class.getPackage().getName())){
                    return true;
                }
            }
        }catch (Exception ignored){ }
        return false;
    }

    public  static boolean isDefaultIME(Context context){
        try {
            String defaultImme = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);

            if (defaultImme != null && defaultImme.startsWith(IMEService.class.getPackage().getName())) {
                return true;
            }
        }catch (Exception ignored){ }
        return false;
    }
}
