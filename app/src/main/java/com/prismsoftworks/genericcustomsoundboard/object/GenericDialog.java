package com.prismsoftworks.genericcustomsoundboard.object;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prismsoftworks.genericcustomsoundboard.MainActivity;
import com.prismsoftworks.genericcustomsoundboard.R;

/**
 * Created by james/CarbonDog on 8/20/16.
 *
 * Generic dialog class, use accessors and mutators!!!!
 */
public class GenericDialog {
    private static final String TAG = GenericDialog.class.getSimpleName();
    private final RelativeLayout mRootView;
    private final Context mContext;
    private LinearLayout mMainContainer;
    private TextView mLabel;
    private ImageButton mBtnRecord;
    private ImageButton mBtnClose;
    private SoundObject mSelectedSoundObj;

    public GenericDialog(RelativeLayout rootView, SoundObject sObj){
        this.mRootView = rootView;
        this.mContext = rootView.getContext();
        this.mSelectedSoundObj = sObj;
        initMembers();
    }

    private void initMembers(){
        View v = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.rec_del_dialog, mRootView, false);
        Log.i(TAG, "inflated view: " + v.getClass().getSimpleName());
        mMainContainer = (LinearLayout) v.findViewById(R.id.dialogContainer);
        mBtnRecord = (ImageButton) v.findViewById(R.id.btnDialog);
        mBtnClose = (ImageButton) v.findViewById(R.id.btnCloseDialog);
        mBtnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.isRecording) {
                    ((MainActivity)mContext).stopRecording();
                } else {
                    ((MainActivity)mContext).startRecording();
                }
            }
        });

        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDialog();
            }
        });
    }

    protected void removeDialog(){
        mRootView.removeView(mMainContainer);
    }

    public LinearLayout getMainContainer() {
        return mMainContainer;
    }

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
