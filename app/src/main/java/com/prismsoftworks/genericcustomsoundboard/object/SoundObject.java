package com.prismsoftworks.genericcustomsoundboard.object;

import java.io.File;

/**
 * Created by james/CarbonDawg on 8/18/16.
 *
 */
public class SoundObject {
    private String mTitle;
    private File mSoundFile;
    private boolean editMode = false;

    public SoundObject(String title, File file){
        mTitle = title;
        mSoundFile = file;
    }

    public void setTitle(String title){//probably dont need any mutator methods
        mTitle = title;
    }

    public String getTitle(){
        return mTitle;
    }

    public File getSoundFile(){
        return mSoundFile;
    }

    public boolean isEditMode(){
        return editMode;
    }

    public void toggleEditMode(){
        editMode = !editMode;
    }
}