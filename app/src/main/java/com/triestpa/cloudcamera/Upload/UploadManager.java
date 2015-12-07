package com.triestpa.cloudcamera.Upload;

import java.util.ArrayList;

public class UploadManager {
    private static UploadManager instance = null;

    private ArrayList<Upload> mUploads;

    protected UploadManager() {
        mUploads = new ArrayList<Upload>();
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

}
