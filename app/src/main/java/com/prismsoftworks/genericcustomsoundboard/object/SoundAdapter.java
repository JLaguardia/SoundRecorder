package com.prismsoftworks.genericcustomsoundboard.object;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
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
 *
 */
public class SoundAdapter extends BaseAdapter {
    private static final String TAG = SoundAdapter.class.getSimpleName();
    private List<SoundObject> mItems;
    private Context mContext;
    private File playingFile = null;
    private MediaPlayer mediaPlayer;
    private ImageView activeImg = null;
//    private SoundWaveFrag waveFrag = null;
    private final EditText edit;

    public SoundAdapter(List<SoundObject> data, Context context) {
        mItems = new ArrayList<>(data);
        mContext = context;
        edit = new EditText(context);
        edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    ((ViewGroup) v.getParent()).getChildAt(((ViewGroup) v.getParent()).indexOfChild(v)-1).setVisibility(View.VISIBLE);
                    ((ViewGroup) v.getParent()).removeView(v);
                }
            }
        });
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
        final TextView txtLabel = (TextView) result.findViewById(R.id.txtSoundTitle);

        if (soundObject.getSoundFile() != null) {
            img.setImageResource(android.R.drawable.ic_media_play);
        } else {
            txtLabel.setGravity(Gravity.CENTER);//"new sound"
        }

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playingFile != null) {
                    stopPlaying();
                    return;
                }

                if (soundObject.getSoundFile() != null) {
                    startPlaying(soundObject.getSoundFile(), img, result);
                } else {
                    toggleRecording(img);
                }
            }
        });

        container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (playingFile != null) {
                    stopPlaying();
                }

                nameChange(txtLabel, soundObject.getSoundFile(), false);
                return true;
            }
        });

        container.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

        txtLabel.setText(soundObject.getTitle());

        ImageButton btnDelete = (ImageButton) result.findViewById(R.id.btnDeleteFile);
        if (soundObject.getSoundFile() == null) {
            btnDelete.setVisibility(View.GONE);
        } else {
            btnDelete.setBackground(null);
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (soundObject.isEditMode()) {
                        ((MainActivity) mContext).deleteFile(soundObject.getSoundFile());
                    } else {
                        toggleEditMode(soundObject, result);
                    }
                }
            });
        }

        return result;
    }

    private void toggleEditMode(SoundObject sobj, View par) {
        ImageButton btn = (ImageButton) par.findViewById(R.id.btnDeleteFile);
        sobj.toggleEditMode();
        if (sobj.isEditMode()) {
            btn.setImageResource(android.R.drawable.ic_delete);
            nameChange(par.findViewById(R.id.txtSoundTitle), sobj.getSoundFile(), false);
        } else {
            btn.setImageResource(android.R.drawable.ic_menu_edit);
        }

        btn.setBackground(null);
    }

    private void nameChange(final View view, final File file, boolean recursive) {
        final InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context
                .INPUT_METHOD_SERVICE);
        if (!recursive) {
            TextView label = (TextView) view;

            edit.setText(label.getText());
            edit.setTextSize(35);
            edit.setSelectAllOnFocus(true);
            edit.setSingleLine(true);
            edit.setImeOptions(EditorInfo.IME_ACTION_DONE);
            LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) label.getLayoutParams();
            LinearLayout parent = (LinearLayout) label.getParent();
            parent.addView(edit, llp);
            label.setVisibility(View.GONE);

            edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    Log.e(TAG, "input event: " + actionId);
                    if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                        Log.i(TAG, "send detected");
                        String formattedName = v.getText().toString();
                        if(formattedName.trim().equals(""))
                            formattedName = ((MainActivity)mContext).getDefaultName();
                         else
                            formattedName += ".3gp";

                        File f = new File(((MainActivity) mContext).getRootFile().getAbsolutePath() + "/" + formattedName);
                        file.renameTo(f);
                        nameChange(edit, null, true);
                    }
                    return false;
                }
            });


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    edit.requestFocus();
//                    imm.showSoftInputFromInputMethod(edit.getWindowToken(), 0);
                    long curT = SystemClock.uptimeMillis();
                    edit.dispatchTouchEvent(MotionEvent.obtain(curT, curT + 30, MotionEvent.ACTION_DOWN, 0, 0, 0));
                    edit.dispatchTouchEvent(MotionEvent.obtain(curT + 50, curT + 100, MotionEvent.ACTION_UP, 0, 0, 0));
                }
            }, 100);

        } else {
            Log.i(TAG, "removing edittext and replacing textview");
            LinearLayout par = (LinearLayout) view.getParent();
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            par.removeView(view);
            par.findViewById(R.id.txtSoundTitle).setVisibility(View.VISIBLE);
            ((MainActivity) mContext).populateListView();
        }
    }

    private void toggleRecording(ImageView img) {
        if (activeImg == null) {
            activeImg = img;
            activeImg.setImageResource(android.R.drawable.ic_media_pause); // maybe a different icon or an animation
            Log.e(TAG, "STARTED RECORDING");
            ((MainActivity) mContext).startRecording();
        } else {
            activeImg = null;
            Log.e(TAG, "STOPPING RECORDING");
            ((MainActivity) mContext).stopRecording();
        }
    }

    private void startPlaying(File file, ImageView img, View parent) {
        Log.i(TAG, "setting up waveform and starting playback for: " + file.getName());

        if (playingFile != null) {
            stopPlaying();
        }

        playingFile = file;
        activeImg = img;
        activeImg.setImageResource(android.R.drawable.ic_media_pause);
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(mContext, Uri.fromFile(playingFile));
        }


//        mediaPlayer.start();
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

        mediaPlayer.start();
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

    private boolean checkLabelWidth(String label, TextView textView) {
        Paint p = new Paint();
        Rect bounds = new Rect();

        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(textView.getTextSize());

        p.getTextBounds(label, 0, label.length(), bounds);

//        Log.i(TAG, "returning: " + bounds.width() + " > " + textView.getWidth());
        return bounds.width() > textView.getWidth();
    }

}
