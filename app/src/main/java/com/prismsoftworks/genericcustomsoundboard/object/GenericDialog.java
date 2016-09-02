package com.prismsoftworks.genericcustomsoundboard.object;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prismsoftworks.genericcustomsoundboard.MainActivity;
import com.prismsoftworks.genericcustomsoundboard.R;

/**
 * Created by james/CarbonDog on 8/20/16.
 * <p/>
 * Generic dialog class, use accessors and mutators!!!!
 */
public class GenericDialog {
    private static final String TAG = GenericDialog.class.getSimpleName();
    private static MotionEvent mCachedMotionEvent = null;
    private final RelativeLayout mRootView;
    private final Context mContext;
    private LinearLayout mMainContainer;
    private TextView mLabel;
    private EditText mEditLabel;
    private ImageButton mBtnRecord;
    private ImageButton mBtnClose;
    private SoundObject mSelectedSoundObj;
    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;

    public GenericDialog(RelativeLayout rootView, SoundObject sObj) {
        this.mRootView = rootView;
        this.mContext = rootView.getContext();
        this.mSelectedSoundObj = sObj;
        initMembers();
    }

    private void initMembers() {
        View v = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.rec_del_dialog, mRootView, false);
        mMainContainer = (LinearLayout) v.findViewById(R.id.dialogContainer);
        mBtnRecord = (ImageButton) v.findViewById(R.id.btnDialog);
        mBtnClose = (ImageButton) v.findViewById(R.id.btnCloseDialog);
        mLabel = (TextView) v.findViewById(R.id.lblDialog);
        mEditLabel = (EditText) v.findViewById(R.id.edtLabelDialog);

        if (mSelectedSoundObj.getSoundFile() == null) {
            mBtnRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (MainActivity.isRecording) {
                        ((MainActivity) mContext).stopRecording();
                    } else {
                        ((MainActivity) mContext).startRecording();
                    }
                }
            });
        } else {
            mLabel.setText(mSelectedSoundObj.getTitle());
            mEditLabel.setText(mSelectedSoundObj.getTitle());
            mBtnRecord.setImageResource(android.R.drawable.ic_media_play);
            mBtnRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isPlaying) {
                        stopPlaying();
                    } else {
                        startPlaying();
                    }
                }
            });
        }

        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying){
                    stopPlaying();//lol
                }

                ((MainActivity)mContext).dialogDismiss();
            }
        });
        mLabel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mLabel.setVisibility(View.GONE);
                mEditLabel.setVisibility(View.VISIBLE);
                mEditLabel.setSelected(true);
                float x = mEditLabel.getX(), y = mEditLabel.getY();
                long downTime = SystemClock.uptimeMillis();
                long upTime = SystemClock.uptimeMillis() + 100;
                MotionEvent me = MotionEvent.obtain(downTime, upTime, MotionEvent.ACTION_UP, x, y, 0);
                mEditLabel.dispatchTouchEvent(me);
                Log.e(TAG, "motionevent sent");
                return false;
            }
        });
    }

    public void changeBtnIcon(int resId){
        mBtnRecord.setImageResource(resId);
    }

    private void startPlaying() {
        Log.i(TAG, "starting playback for: " + mSelectedSoundObj.getTitle());
        isPlaying = true;
        mBtnRecord.setImageResource(android.R.drawable.ic_media_pause);
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(mContext, Uri.fromFile(mSelectedSoundObj.getSoundFile()));
        }

        mediaPlayer.start();
    }

    private void stopPlaying() {
        Log.i(TAG, "stopping playback for: " + mSelectedSoundObj.getTitle());
        isPlaying = false;
        mBtnRecord.setImageResource(android.R.drawable.ic_media_play);
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    public LinearLayout getMainContainer() {
        return mMainContainer;
    }

    //TODO: implement the mutators instead of reinstancing the pop variable!!!
    public void setmLabel(TextView mLabel) {
        this.mLabel = mLabel;
    }

    public ImageButton getmBtnRecord() {
        return mBtnRecord;
    }

    public void setmBtnRecord(ImageButton mBtnRecord) {
        this.mBtnRecord = mBtnRecord;
    }

    public ImageButton getmBtnClose() {
        return mBtnClose;
    }

    public void setmBtnClose(ImageButton mBtnClose) {
        this.mBtnClose = mBtnClose;
    }

    public SoundObject getmSelectedSoundObj() {
        return mSelectedSoundObj;
    }

    public void setmSelectedSoundObj(SoundObject mSelectedSoundObj) {
        this.mSelectedSoundObj = mSelectedSoundObj;
    }
}
