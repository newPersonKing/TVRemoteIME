package xllib.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

/**
 * Created by kingt on 2018/2/3.
 */

public class FocusFixedLinearLayoutManager extends LinearLayoutManager {
    public FocusFixedLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public View onInterceptFocusSearch(View focused, int direction) {
        int fromPos = getPosition(focused);
        int count = getItemCount();
        if (direction == View.FOCUS_DOWN) {
            fromPos ++;
        } else if (direction == View.FOCUS_UP) {
            fromPos --;
        }
        if(fromPos < 0) {
            return focused;
        } else if(fromPos >= count) {
            return focused;
        } else {
            return findViewByPosition(fromPos);
//            return getChildAt(fromPos);
        }
    }
}
