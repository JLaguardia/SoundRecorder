package com.prismsoftworks.genericcustomsoundboard.object;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prismsoftworks.genericcustomsoundboard.MainActivity;
import com.prismsoftworks.genericcustomsoundboard.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by james/CarbonDawg on 8/18/16.
 */
public class SoundAdapter extends BaseAdapter {
    private static final String TAG = SoundAdapter.class.getSimpleName();
    private List<SoundObject> mItems;
    private Context mContext;

    public SoundAdapter(List<SoundObject> data, Context context) {
        mItems = new ArrayList<>(data);
        mContext = context;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int index) {
        return mItems.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        final SoundObject soundObject = mItems.get(index);
        final View result = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.sound_item, parent, false);
        //viewholder stuff - we arent using recyclerviews unfortunately
        LinearLayout container = (LinearLayout) result.findViewById(R.id.soundContainer);
        if (soundObject.getSoundFile() != null) {
            ImageView img = (ImageView) result.findViewById(R.id.btnFakeRecord);
            img.setImageResource(android.R.drawable.ic_media_play);
        }

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get file instance, etc
                ((MainActivity) mContext).dialogPopup(soundObject, result, false);
            }
        });

        container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((MainActivity) mContext).dialogPopup(soundObject, result, true);
                return false;
            }
        });

        final TextView txtLabel = (TextView) result.findViewById(R.id.txtSoundTitle);
        txtLabel.setText(soundObject.getTitle());
        if (checkLabelWidth(soundObject.getTitle(), txtLabel)) {
            txtLabel.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    if (action == MotionEvent.ACTION_DOWN && !txtLabel.isSelected()) {
                        txtLabel.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                        txtLabel.setMarqueeRepeatLimit(-1); //Whatever.MARQUEE_FOREVER == -1 according to dox
                        txtLabel.setTextIsSelectable(true);
                        txtLabel.setSelected(true);
                    } else if (action == MotionEvent.ACTION_UP) {
                        txtLabel.setTextIsSelectable(false);
                        txtLabel.setSelected(false);
                    }

                    return false;
                }
            });
        }

        return result;
    }

    protected boolean checkLabelWidth(String label, TextView textView) {
        Paint p = new Paint();
        Rect bounds = new Rect();

        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(textView.getTextSize());

        p.getTextBounds(label, 0, label.length(), bounds);

//        Log.i(TAG, "returning: " + bounds.width() + " > " + textView.getWidth());
        return bounds.width() > textView.getWidth();
    }
}
