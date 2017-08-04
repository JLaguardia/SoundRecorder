package com.prismsoftworks.genericcustomsoundboard.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.prismsoftworks.genericcustomsoundboard.R;

import java.io.IOException;

/**
 * Created by bohregard on 8/4/2017.
 */

public class CustomPlayer extends MediaPlayer {

    private static final String TAG = CustomPlayer.class.getSimpleName();
    private Uri file;
    private ImageView imageView;
    private PlaybackListener playbackListener;

    public interface PlaybackListener {
        void onPlay();
        void onStoped(ImageView imageView);
        void onPause();
    }

    public static CustomPlayer create(Context context, Uri uri) {
        try {
            Log.d(TAG, "URI: " + uri);
            CustomPlayer customPlayer = new CustomPlayer();
            customPlayer.setDataSource(context, uri);
            customPlayer.prepare();
            return customPlayer;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        imageView.setImageResource(R.drawable.ic_pause_black_48dp);
        if(playbackListener != null) {
            playbackListener.onPlay();
        }
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
        imageView.setImageResource(R.drawable.ic_play_arrow_black_48dp);
        if(playbackListener != null) {
            playbackListener.onStoped(imageView);
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        if(playbackListener != null) {
            playbackListener.onPause();
        }
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public void setUri(Uri uri) {
        this.file = uri;
    }

    public void setPlaybackListener(PlaybackListener playbackListener) {
        this.playbackListener = playbackListener;
    }


}
