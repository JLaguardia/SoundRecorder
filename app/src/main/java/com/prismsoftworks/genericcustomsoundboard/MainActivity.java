package com.prismsoftworks.genericcustomsoundboard;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prismsoftworks.genericcustomsoundboard.adapter.SoundAdapter;
import com.prismsoftworks.genericcustomsoundboard.model.Sound;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String internalDir = "/sounds";

    private static final String PREF_FILENAME = "prefs";
    private static final String TITLE_KEY = "apptitl";
    private static final String EXT_KEY = "fileext";

    private SharedPreferences mPrefs;
    public static boolean isRecording = false;

    private MediaRecorder mRecorder = null;
    private TextView mTxtAppTitle;
    private static MainActivity instance = null;

    private String fileExtension;
    private SoundAdapter soundAdapter;

//    private final int[] mFormats = {MediaRecorder.OutputFormat., MediaRecorder.OutputFormat.THREE_GPP};
//    private int currentFormat; //0 for mp4 stuff, 1 for 3gpp stuff todo: implement this - currently ONLY mp4....

    private String userFileName = null;
    private File mSavedRootFile;
    private List<Sound> mFileList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    public static MainActivity getInstance() {
        return instance;
    }

    protected void init() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
//        rootView = findViewById(R.id.mainActRoot);
        mPrefs = getSharedPreferences(PREF_FILENAME, MODE_PRIVATE);

        String defAppTitle = getResources().getString(R.string.app_default_label);
        mTxtAppTitle = findViewById(R.id.lblAppTitle);
        defAppTitle = mPrefs.getString(TITLE_KEY, defAppTitle);
        fileExtension = mPrefs.getInt(EXT_KEY, MediaRecorder.OutputFormat.MPEG_4) == MediaRecorder.OutputFormat.MPEG_4 ? "mp4" : "3gp";
        mTxtAppTitle.setText(defAppTitle);
//        mTxtAppTitle.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                //todo: handle changing of the name and the such
//                Log.i(TAG, "longclicked title");
//                return false;
//            }
//        });

        populateListView();

        //test: debug button, will add for specific files later.
        findViewById(R.id.btnDropDown).setBackground(null);
        findViewById(R.id.btnDropDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDropDown(v);
            }
        });
    }

//    public void dialogDismiss() {
////        rootView.removeView(pop.getMainContainer());
////        pop = null;
//        soundAdapter.notifyItemInserted(mFileList.size());
//    }
//
//    public void deleteFile(File fileToDelete) {
//        Toast.makeText(this, fileToDelete.getName() + " deleted", Toast.LENGTH_SHORT).show();
//        fileToDelete.delete();
//        soundAdapter.notifyItemRemoved(mFileList.size());
//    }

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
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(getFilePath(true));
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);

        try {
            mRecorder.prepare();
            mRecorder.start();
            isRecording = true;
//            pop.changeBtnIcon(android.R.drawable.ic_media_pause);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getDefaultName() {
        final Calendar c = Calendar.getInstance();
        return (new StringBuilder()
                .append(c.get(Calendar.YEAR)).append("-")
                .append(c.get(Calendar.MONTH) + 1).append("-")
                .append(c.get(Calendar.DAY_OF_MONTH)).append("_")
                .append(c.get(Calendar.HOUR)).append(".")
                .append(c.get(Calendar.MINUTE)).append(".")
                .append(c.get(Calendar.SECOND)).append("." + fileExtension))
                .toString();
    }

    private String getFilePath(boolean newFile) {
        if (newFile) {
            userFileName = null;
        }

        userFileName = userFileName == null ? getDefaultName() : userFileName + ".3gp";
        int copyCount = 0;
        String tempFileName = "";
        for (File file : mSavedRootFile.listFiles()) {
            if (file.getName().equals(userFileName)) {
                copyCount++;
                tempFileName = userFileName + copyCount;
            }
        }

        if (!tempFileName.equals(""))
            userFileName = tempFileName;
        return mSavedRootFile.getAbsolutePath() + "/" + userFileName;
    }

    public void stopRecording() {
        if (mRecorder != null) {
            isRecording = false;
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            Log.d(TAG, "Size: " + mFileList.size());
            int pos = mFileList.size();
            mFileList.add(pos - 1, new Sound(userFileName, new File(mSavedRootFile + "/" + userFileName)));
            soundAdapter.notifyItemInserted(pos - 1);
            Log.d(TAG, "Size: " + mFileList.size());
//            populateListView();
        }
    }

    protected void openDropDown(View sender) {
        final PopupMenu poppy = new PopupMenu(MainActivity.this, sender);
        poppy.getMenuInflater().inflate(R.menu.options_menu, poppy.getMenu());

        poppy.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch ((String) item.getTitleCondensed()) {
                    case "0": //file
                        //open file file list etc.
                        break;
                    case "1": //delete all
                        //delete all
                        File[] files = mSavedRootFile.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                Log.e(TAG, "deleting file: " + file.getName());
                                file.delete();
                            }
                        }

                        Toast.makeText(getInstance(), "All custom sounds deleted.", Toast.LENGTH_SHORT).show();
                        populateListView();
                        break;
                    case "2":
                        MainActivity.getInstance().finish();
                        break;
                    default:
                        return false;
                }

                poppy.dismiss();
                return true;
            }
        });

        poppy.show();
//        Toast.makeText(this, "App made by James L for my friend, Patricia \"Earring\"", Toast.LENGTH_LONG).show();

    }

    public File getRootFile() {
        return mSavedRootFile;
    }

    public void populateListView() {
        mSavedRootFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + internalDir);
        Log.i(TAG, "savedRootFile: " + mSavedRootFile.getAbsolutePath());
        userFileName = null;

        if (!mSavedRootFile.exists()) {
            mSavedRootFile.mkdirs();
            mFileList = new ArrayList<>();
        } else {
            File[] files = mSavedRootFile.listFiles();

            if (mFileList == null) {
                mFileList = new ArrayList<>();
            } else {
                mFileList.clear();
            }

            if (files.length > 0) {
                for (File file : files) {
                    Log.i(TAG, "detected file: " + file.getName());
                    mFileList.add(new Sound(file.getName(), file));
                }
            }
        }

        //add "new sound" and null to end of list for the "add" button
        mFileList.add(new Sound(getString(R.string.new_label), null));
        soundAdapter = new SoundAdapter(mFileList, this);
        RecyclerView recyclerView = findViewById(R.id.sounds_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(soundAdapter);
    }
}
