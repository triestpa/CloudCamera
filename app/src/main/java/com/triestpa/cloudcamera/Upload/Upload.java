package com.triestpa.cloudcamera.Upload;

import android.util.Log;
import android.widget.Toast;

import com.parse.ParseFile;
import com.triestpa.cloudcamera.CloudCameraApplication;
import com.triestpa.cloudcamera.Utilities.SystemUtilities;

class Upload {
    private static String TAG = Upload.class.getName();

    private ParseFile parseFile;
    private int progress;
    private boolean completed;
    private boolean aborted;

    Upload(ParseFile parseFile) {
        this.parseFile = parseFile;
        this.progress = 0;
        this.completed = false;
        this.aborted = false;
    }

    ParseFile getParseFile() {
        return parseFile;
    }

    void setParseFile(ParseFile parseFile) {
        this.parseFile = parseFile;
    }

    public int getProgress() {
        return progress;
    }

    void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isCompleted() {
        return completed;
    }

    private void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isAborted() {
        return aborted;
    }

    public void setAborted(boolean aborted) {
        this.aborted = aborted;
    }

    void showError(com.parse.ParseException e, String message) {
        Log.e(TAG, e.getMessage());
        Upload.this.setAborted(true);
        SystemUtilities.reportError(TAG, message + e.getMessage());
    }

    void showSuccess(String message) {
        this.setCompleted(true);
        Toast.makeText(CloudCameraApplication.getAppContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void retryUpload() {

    }
}
