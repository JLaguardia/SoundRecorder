package com.prismsoftworks.genericcustomsoundboard.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.prismsoftworks.genericcustomsoundboard.MainActivity;
import com.prismsoftworks.genericcustomsoundboard.R;
import com.prismsoftworks.genericcustomsoundboard.model.Sound;
import com.prismsoftworks.genericcustomsoundboard.util.CustomPlayer;
import com.prismsoftworks.genericcustomsoundboard.view.SoundView;

import java.io.File;
import java.util.List;

/**
 * Created by bohregard on 8/4/2017.
 * Edited by carbondawg
 */

public class SoundAdapter extends RecyclerView.Adapter<SoundView> {
    private static final String TAG = SoundAdapter.class.getSimpleName();
    private List<Sound> sounds;
    private Context context;
    private CustomPlayer mediaPlayer;
    private boolean isRecording = false;

    public SoundAdapter(List<Sound> sounds, Context context) {
        this.sounds = sounds;
        this.context = context;
    }

    @Override
    public SoundView onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_sound, parent, false);
        return new SoundView(v);
    }

    @Override
    public void onBindViewHolder(SoundView holder, int position) {
        final Sound sound = sounds.get(position);
        holder.getSoundName().setText(sound.getTitle());
        if (sound.getSoundFile() != null) {
            Log.d(TAG, sound.getSoundFile().getName());
            holder.getSoundPlay().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startPlaying(sound.getSoundFile(), (ImageView) view);
                }
            });
        } else {
            holder.getSoundPlay().setImageResource(R.drawable.ic_mic_black_48dp);
            holder.getSoundPlay().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Do something else?");
                    if(isRecording) {
                        ((ImageView) view).setImageResource(R.drawable.ic_mic_black_48dp);
                        ((MainActivity) context).stopRecording();
                    } else {
                        ((ImageView) view).setImageResource(R.drawable.ic_stop_black_48dp);
                        ((MainActivity) context).startRecording();
                    }
                    isRecording = !isRecording;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return sounds.size();
    }

    /*
     ************************************************************************
     * Private methods
     ************************************************************************
     */

    private void startPlaying(final File file, final ImageView img) {
        Log.d(TAG, "setting up waveform and starting playback for: " + file.getName());

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }

        mediaPlayer = CustomPlayer.create(context, Uri.fromFile(file));
        mediaPlayer.setImageView(img);
        mediaPlayer.setUri(Uri.fromFile(file));

        mediaPlayer.setPlaybackListener(new CustomPlayer.PlaybackListener() {
            @Override
            public void onPlay() {
                Log.d(TAG, "Play");
            }

            @Override
            public void onStoped(ImageView imageView) {
                Log.d(TAG, "Stopped");
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startPlaying(file, (ImageView) view);
                    }
                });
            }

            @Override
            public void onPause() {
                Log.d(TAG, "Paused");
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.stop();
            }
        });

        mediaPlayer.start();
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlaying(file, (ImageView) view);
            }
        });
    }

    private void stopPlaying(final File file, final ImageView view) {
        Log.i(TAG, "stopping playback for: " + file.getName());
        mediaPlayer.stop();
        mediaPlayer.reset();
    }
}
