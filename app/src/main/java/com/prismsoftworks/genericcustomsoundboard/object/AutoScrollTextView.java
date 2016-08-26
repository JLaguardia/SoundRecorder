package com.prismsoftworks.genericcustomsoundboard.object;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by james/CarbonDawg on 8/18/16.
 *
 */
public class AutoScrollTextView extends TextView {
    public AutoScrollTextView(Context context) {
        super(context);
    }

    public AutoScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoScrollTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AutoScrollTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean isFocused(){
        return true;
    }
}
