package com.prismsoftworks.genericcustomsoundboard.object;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by james/CarbonDawg on 8/18/16.
 */
public class SoundAdapter extends BaseAdapter {
    private static final String TAG = SoundAdapter.class.getSimpleName();
    private List<SoundObject> mItems;
    private Context mContext;
    private static File playingFile = null;
    private static ImageView activeImg = null;
    private static MediaPlayer mediaPlayer;

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
        final ImageView img = (ImageView) result.findViewById(R.id.btnFakeRecord);

        if (soundObject.getSoundFile() != null) {
            img.setImageResource(android.R.drawable.ic_media_play);
        }

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playingFile != null) {
                    stopPlaying();
                    return;
                }

                if (soundObject.getSoundFile() != null) {
                    startPlaying(soundObject.getSoundFile(), img);
                } else {
                    toggleRecording(img);
                }
//                ((MainActivity) mContext).dialogPopup(soundObject, result, false);
            }
        });

        container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (playingFile != null) {
                    stopPlaying();
                }

                //todo handle change name
//                ((MainActivity) mContext).dialogPopup(soundObject, result, true);
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

        ImageButton btnDelete = (ImageButton) result.findViewById(R.id.btnDeleteFile);
        if (soundObject.getSoundFile() == null) {
            btnDelete.setVisibility(View.GONE);
        } else {
            btnDelete.setBackground(null);
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) mContext).deleteFile(soundObject.getSoundFile());
                }
            });
        }

        return result;
    }

    private void toggleRecording(ImageView img) {
        if (activeImg == null) {
            activeImg = img;
            activeImg.setImageResource(android.R.drawable.ic_media_pause);
            Log.e(TAG, "STARTED RECORDING");
            ((MainActivity) mContext).startRecording();
        } else {
            activeImg = null;
            Log.e(TAG, "STOPPING RECORDING");
            ((MainActivity)mContext).stopRecording();
        }
    }

    private void startPlaying(File file, ImageView img) {
        Log.i(TAG, "starting playback for: " + file.getName());
        if (playingFile != null) {
            stopPlaying();
        }

        playingFile = file;
        activeImg = img;
        activeImg.setImageResource(android.R.drawable.ic_media_pause);
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(mContext, Uri.fromFile(playingFile));
        }

        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i(TAG, "playback completed.");
                activeImg.setImageResource(android.R.drawable.ic_media_play);
                playingFile = null;
                activeImg = null;
                mediaPlayer.release();
                mediaPlayer = null;
            }
        });
    }

    private void stopPlaying() {
        Log.i(TAG, "stopping playback for: " + playingFile.getName());
        activeImg.setImageResource(android.R.drawable.ic_media_play);
        playingFile = null;
        activeImg = null;
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
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
