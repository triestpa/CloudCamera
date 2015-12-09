package com.triestpa.cloudcamera.UploadsScreen;

import com.triestpa.cloudcamera.Model.Upload;

import java.util.ArrayList;

public class UploadManager {
    private static UploadManager instance = null;

    private ArrayList<Upload> mUploads;

    private UploadManager() {
        mUploads = new ArrayList<>();
    }

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
