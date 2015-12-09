package com.triestpa.cloudcamera.UploadsScreen;

import com.triestpa.cloudcamera.Model.Upload;

import java.util.ArrayList;

/**
 * Upload Manager: Singleton class to store a list of uploads
 * in order to share data between Parse upload tasks and
 * upload grid UI.
 */
public class UploadManager {

    // There will only ever be one instance
    private static UploadManager instance = null;

    private ArrayList<Upload> mUploads; // List of uploads

    private UploadManager() {
        mUploads = new ArrayList<>();
    }

    // Return saved instance, or create new instance if none
    public static UploadManager getInstance() {
        if(instance == null) {
            instance = new UploadManager();
        }
        return instance;
    }

    public ArrayList<Upload> getUploads() {
        return mUploads;
    }

    public void addUpload(Upload upload) {
        mUploads.add(upload);
    }

    public void clearUploads() {
        mUploads.clear();
    }

}
