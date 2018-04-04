package com.prismsoftworks.genericcustomsoundboard.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.prismsoftworks.genericcustomsoundboard.R;

/**
 * Created by jameslaguardia on 4/3/18.
 */

public class SoundView extends RecyclerView.ViewHolder {
    private ImageView soundPlay;
    private TextView soundName;

    public SoundView(View itemView) {
        super(itemView);
        soundPlay = itemView.findViewById(R.id.sound_play);
        soundName = itemView.findViewById(R.id.sound_name);
    }

    public ImageView getSoundPlay(){
        return soundPlay;
    }

    public TextView getSoundName(){
        return soundName;
    }

}
