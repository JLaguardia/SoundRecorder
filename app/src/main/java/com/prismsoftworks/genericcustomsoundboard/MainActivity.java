package com.prismsoftworks.genericcustomsoundboard;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.prismsoftworks.genericcustomsoundboard.object.AutoScrollTextView;
import com.prismsoftworks.genericcustomsoundboard.object.GenericDialog;
import com.prismsoftworks.genericcustomsoundboard.object.SoundAdapter;
import com.prismsoftworks.genericcustomsoundboard.object.SoundObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String internalDir = "/sounds";

    private static final String PREF_FILENAME = "prefs";
    private static final String TITLE_KEY = "apptitl";

    private SharedPreferences mPrefs;
    public static boolean isRecording = false;

    private MediaRecorder mRecorder = null;
    private AutoScrollTextView mTxtAppTitle;
    private int mFileNum = -1;

    private RelativeLayout rootView;
    private GenericDialog pop = null;
    private ListView mListView;

    private final int[] mFormats = {MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP};
    private int currentFormat; //0 for mp4 stuff, 1 for 3gpp stuff

    private String userFileName = null;
    private File mSavedRootFile;
    private List<SoundObject> mFileList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    protected void init() {
        rootView = (RelativeLayout) findViewById(R.id.mainActRoot);
        mPrefs = getSharedPreferences(PREF_FILENAME, MODE_PRIVATE);
        mSavedRootFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + internalDir);
        Log.e(TAG, "savedRootFile: " + mSavedRootFile.getAbsolutePath());

        if (!mSavedRootFile.exists()) {
            mSavedRootFile.mkdirs();
        }

        if (mFileList == null) {
            mFileList = new ArrayList<>();
        } else {
            mFileList.clear();
        }

        String defAppTitle = getResources().getString(R.string.app_default_label);
        mTxtAppTitle = (AutoScrollTextView) findViewById(R.id.lblAppTitle);
        defAppTitle = mPrefs.getString(TITLE_KEY, defAppTitle);
        mTxtAppTitle.setText(defAppTitle);
        mTxtAppTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //todo: handle changing of the name and the such
                Log.i(TAG, "longclicked title");
                return false;
            }
        });

        mListView = (ListView) findViewById(R.id.listView);
        populateListView();
    }

    public void dialogPopup(SoundObject sObj, View par, boolean lngClicked) {
        if (pop == null) {
            pop = new GenericDialog(rootView, sObj);
        }

        if (sObj.getSoundFile() == null) {

            rootView.addView(pop.getMainContainer());
            ((RelativeLayout.LayoutParams) pop.getMainContainer().getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL);
            ((RelativeLayout.LayoutParams) pop.getMainContainer().getLayoutParams()).addRule(RelativeLayout.CENTER_VERTICAL);
        }
    }

    protected void dialogDismiss() {
        rootView.removeView(pop.getMainContainer());
    }

    public void startRecording() {
        int[] permissions = new int[]{
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO),
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        };

        if (permissions[0] != PackageManager.PERMISSION_GRANTED
                || permissions[1] != PackageManager.PERMISSION_GRANTED
                || permissions[2] != PackageManager.PERMISSION_GRANTED) {
            String[] neededPerms = new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
            ActivityCompat.requestPermissions(this, neededPerms, 0);
            return;
        }


        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(mFormats[currentFormat]);
        mRecorder.setOutputFile(getFilePath());
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);

        try {
            mRecorder.prepare();
            mRecorder.start();
            isRecording = true;
        } catch (Exception ex) {

        }
    }

    private String getFilePath() {
        //todo: inner currentFormat if should be predefined before getting to this point
        userFileName = userFileName == null ? mFileNum + (currentFormat < 1 ? ".mp4" : ".3gpp") :
                userFileName;

        return mSavedRootFile.getAbsolutePath() + "/" + userFileName;
    }

    public void stopRecording() {
        if (mRecorder != null) {
            isRecording = false;
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    protected void populateListView() {
        File[] files = mSavedRootFile.listFiles();
        if (files == null) {
            mFileNum = 0;
        } else {
            mFileNum = files.length;
        }

        if (mFileNum > 0) {
            for (File file : files) {
                Log.i(TAG, "file: " + file.getName());
                mFileList.add(new SoundObject(file.getName(), file));
            }
        }

        mFileList.add(new SoundObject(getString(R.string.new_label), null));
        SoundAdapter adapt = new SoundAdapter(mFileList, this);
        mListView.setAdapter(adapt);
    }
}
