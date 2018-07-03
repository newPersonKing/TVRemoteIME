package com.android.tvremoteime;

import android.view.KeyEvent;

/**
 * Created by kingt on 2018/1/9.
 */

public class KeyEventUtils {
    public static boolean isKeyboardFocusEvent(int keyCode){
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_CAPS_LOCK:
            case KeyEvent.KEYCODE_ESCAPE:
            case KeyEvent.KEYCODE_BACK:
                return true;
            default:
                return false;
        }
    }
}
