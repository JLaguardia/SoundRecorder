package com.prismsoftworks.genericcustomsoundboard;

import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prismsoftworks.genericcustomsoundboard.object.AutoScrollTextView;
import com.prismsoftworks.genericcustomsoundboard.object.GenericDialog;
import com.prismsoftworks.genericcustomsoundboard.object.SoundAdapter;
import com.prismsoftworks.genericcustomsoundboard.object.SoundObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MAX_DURATION = 10;
    private static final int MAX_FILESIZE = 10000;//bytes
    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_CHANNEL_IN_STEREO = AudioFormat.CHANNEL_IN_STEREO;
    private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final String internalDir = "/sounds";

    private static final String PREF_FILENAME = "prefs";
    private static final String TITLE_KEY = "apptitl";

    public static File selectedFile = null;

    private SharedPreferences mPrefs;
    public static boolean isRecording = false;

//    private AudioRecord mRecorder = null;
    private MediaRecorder mRecorder = null;
//    private int audioBufferSize = 0;
//    private Thread recordThread = null;
    private AutoScrollTextView mTxtAppTitle;
    private int mFileNum = -1;

    private RelativeLayout rootView;
    private GenericDialog pop = null;
    private ListView mListView;
//    private String mFileNameDef = mFileNum + ".wav";

    private final int[] mFormats = {MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP};
    private int selectedMode; //0 for mp4 stuff, 1 for 3gpp stuff

    private String userFileName = null;
//    private File mSavedRootFile;
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

    public void dialogPopup(SoundObject sObj, View par, boolean lngClicked) {//TODO: FIX THIS, it isnt inflating yo
        if(pop == null){
            pop = new GenericDialog(rootView, sObj);
        }

        if (sObj.getSoundFile() == null) {

            rootView.addView(pop.getMainContainer());
            ((RelativeLayout.LayoutParams) pop.getMainContainer().getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL);
            ((RelativeLayout.LayoutParams) pop.getMainContainer().getLayoutParams()).addRule(RelativeLayout.CENTER_VERTICAL);
        }
    }

    protected void dialogDismiss(){
        rootView.removeView(pop.getMainContainer());
    }

    public void startRecording() {
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLE_RATE,
                AUDIO_CHANNEL_IN_STEREO, AUDIO_ENCODING, audioBufferSize);

        if (mRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
            mRecorder.startRecording();
            isRecording = true;
            recordThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    writeDataToFile();
                }
            }, "Recording Thread");

            recordThread.start();
        }
    }

    private void writeDataToFile() {
        byte[] data = new byte[audioBufferSize];
        FileOutputStream fos = null;
        if (selectedFile == null) {
            selectedFile = new File(getFilePath());//use abs path
        }

        Log.i(TAG, "writing data to filename: " + selectedFile.getName() + " | " + mFileNameDef);

        try {
            fos = new FileOutputStream(selectedFile);
        } catch (FileNotFoundException fnfe) {
            Log.e(TAG, "file not found yo:\n" + fnfe.getMessage());
        }

        int read = 0;
        if (fos != null) {
            while (isRecording) {
                read = mRecorder.read(data, 0, audioBufferSize);

                if (read != AudioRecord.ERROR_INVALID_OPERATION) {
                    try {
                        fos.write(read);
                    } catch (IOException ioe) {
                        Log.e(TAG, "io exception yo:\n" + ioe.getMessage());
                    }
                }
            }

            try {
                fos.flush();
                fos.close();
            } catch (IOException ioe) {
                Log.e(TAG, "io exception flush/closing yo: " + ioe.getMessage());
            }
        }
    }

    private String getFilePath(){
        File temp = new File(Environment.getExternalStorageDirectory() + internalDir);
        if(!temp.exists()){
            temp.mkdirs();
        }

        String absPath = temp.getAbsolutePath() + "/" + userFileName;

    }

    public void stopRecording() {
        if (mRecorder != null) {
            isRecording = false;
            if (mRecorder.getState() == 1) {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
                recordThread = null;
                renameFile(mFileNameDef);
            }
        }
    }

    private void renameFile(final String originFileName) {
        final String origin = originFileName;
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.getLayoutParams().width = -1;
        container.getLayoutParams().height = -1;
        TextView lblPoppy = new TextView(this);
        String label = getResources().getString(R.string.pop_rename_file);
        lblPoppy.setText(label);
        EditText txtInput = new EditText(this);
        txtInput.setText(origin);
        txtInput.setSelected(true);
        txtInput.setImeOptions(EditorInfo.IME_ACTION_SEND);

        txtInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                userFileName = (v.getText().toString() != "" ? v.getText().toString() + ".wav" :
                        originFileName);
                FileInputStream fis = null;
                FileOutputStream fos = null;
                long totLength = 0;
                long totDataLength = totLength + 36;
                long sampleRate = AUDIO_SAMPLE_RATE;
                long byteRate = 16 * AUDIO_SAMPLE_RATE * 2 / 8;
                byte[] data = new byte[audioBufferSize];
                try {
                    fis = new FileInputStream(origin);
                    fos = new FileOutputStream(userFileName);
                    fos.flush();

                    totLength = fis.getChannel().size();
                    totDataLength = totLength + 36;
                    Log.i(TAG, "initiating rename.... file size: " + totDataLength);
                    writeWaveFileHeader(fos, totLength, totDataLength, sampleRate, 2, byteRate);

                    while (fis.read(data) != -1) {
                        fos.write(data);
                    }

                    fis.close();
                    fos.flush();
                    fos.close();
                } catch (IOException ioe) {
                    Log.e(TAG, "io exception when writing to wave file header or whatever:\n" + ioe.getMessage());
                }

                return false;
            }
        });

        container.addView(lblPoppy);
        container.addView(txtInput);
        ((ViewGroup) mListView.getParent().getParent()).addView(container);
    }

    /**
     * no idea wtf this shit is
     *
     * @throws IOException
     */
    private void writeWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    protected void populateListView() {
        File[] files = mSavedRootFile.listFiles();
        mFileNum = files.length;
        mFileNameDef = mFileNum + ".wav";
        if (mFileNum > 1) {
            for (File file : files) {
                mFileList.add(new SoundObject(file.getName(), file));
            }
        }

        mFileList.add(new SoundObject(getString(R.string.new_label), null));
        SoundAdapter adapt = new SoundAdapter(mFileList, this);
        mListView.setAdapter(adapt);
    }
}
