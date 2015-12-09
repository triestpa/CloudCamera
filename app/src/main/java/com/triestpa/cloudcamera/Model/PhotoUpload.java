package com.triestpa.cloudcamera.Model;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.triestpa.cloudcamera.CloudCameraApplication;
import com.triestpa.cloudcamera.Utilities.SystemUtilities;

public class PhotoUpload extends Upload {
    final static String TAG = PhotoUpload.class.getName();

    public PhotoUpload(ParseFile photoFile) {
        super(photoFile);
    }

    public void uploadPhoto() {
        if (!SystemUtilities.isOnline(CloudCameraApplication.getAppContext())) {
            SystemUtilities.reportError(TAG, "Cannot upload photo without internet connection. Visit upload screen to retry.");
            this.setAborted(true);
            return;
        }

        final ParseFile picFile = this.getParseFile();
        picFile.saveInBackground(new SaveCallback() {
                                     @Override
                                     public void done(ParseException e) {
                                         if (e == null) {
                                             savePhoto(picFile);
                                         } else {
                                             PhotoUpload.this.showError(e, "Photo Upload Failure ");
                                         }
                                     }
                                 }, new ProgressCallback() {
                                     @Override
                                     public void done(Integer percentDone) {
                                         PhotoUpload.this.setProgress(percentDone);
                                     }
                                 }
        );
    }

    private void savePhoto(ParseFile photoFile) {
        Picture newPic = new Picture();
        newPic.setPhoto(photoFile);

        ParseUser thisUser = ParseUser.getCurrentUser();
        newPic.setParseUser(thisUser);
        newPic.setACL(new ParseACL(thisUser));

        newPic.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            PhotoUpload.this.showSuccess("Photo Upload Success");
                                            PhotoUpload.this.setParseFile(null);
                                        } else {
                                            PhotoUpload.this.showError(e, "Photo Upload Failure ");
                                        }
                                    }
                                }

        );
    }

    @Override
    public void retryUpload() {
        uploadPhoto();
    }
}