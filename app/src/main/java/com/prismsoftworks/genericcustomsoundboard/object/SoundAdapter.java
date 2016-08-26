package com.prismsoftworks.genericcustomsoundboard.object;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.prismsoftworks.genericcustomsoundboard.MainActivity;
import com.prismsoftworks.genericcustomsoundboard.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by james/CarbonDawg on 8/18/16.
 */
public class SoundAdapter extends BaseAdapter {
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
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get file instance, etc
                ((MainActivity)mContext).dialogPopup(soundObject, result, false);
            }
        });

        container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((MainActivity)mContext).dialogPopup(soundObject, result, true);
                return false;
            }
        });

        AutoScrollTextView txtLabel = (AutoScrollTextView) result.findViewById(R.id.txtSoundTitle);
        txtLabel.setText(soundObject.getTitle());

        return result;
    }
}
