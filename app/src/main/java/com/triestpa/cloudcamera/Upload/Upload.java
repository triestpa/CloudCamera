package com.triestpa.cloudcamera.Upload;

import android.util.Log;

import com.parse.ParseFile;
import com.triestpa.cloudcamera.Utilities.SystemUtilities;

public class Upload {
    private static String TAG = Upload.class.getName();

    private ParseFile parseFile;
    private int progress;
    private boolean completed;
    private boolean aborted;

    public Upload(ParseFile parseFile) {
        this.parseFile = parseFile;
        this.progress = 0;
        this.completed = false;
        this.aborted = false;
    }

    public ParseFile getParseFile() {
        return parseFile;
    }

    public void setParseFile(ParseFile parseFile) {
        this.parseFile = parseFile;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isAborted() {
        return aborted;
    }

    public void setAborted(boolean aborted) {
        this.aborted = aborted;
    }

    public void showError(com.parse.ParseException e, String message) {
        Log.e(TAG, e.getMessage());
        Upload.this.setAborted(true);
        SystemUtilities.showToastMessage(message + e.getMessage());
    }

    public void showSuccess(String message) {
        this.setCompleted(true);
        SystemUtilities.showToastMessage(message);
    }

    public void retryUpload() {

    }
}
