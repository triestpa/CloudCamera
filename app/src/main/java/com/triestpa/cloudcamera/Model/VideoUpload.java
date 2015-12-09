package com.triestpa.cloudcamera.Model;


import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.triestpa.cloudcamera.CloudCameraApplication;
import com.triestpa.cloudcamera.UploadsScreen.UploadManager;
import com.triestpa.cloudcamera.Utilities.SystemUtilities;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class VideoUpload extends Upload {
    final static String TAG = VideoUpload.class.getName();
    private ParseFile thumbnailFile;

    public VideoUpload(ParseFile videoFile, ParseFile thumbnailFile) {
        super(videoFile);
        this.thumbnailFile = thumbnailFile;
    }

    private ParseFile getThumbnailFile() {
        return thumbnailFile;
    }

    private void setThumbnailFile(ParseFile thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
    }

    public void uploadVideo() {
        if (!SystemUtilities.isOnline(CloudCameraApplication.getAppContext())) {
            SystemUtilities.reportError(TAG, "Cannot upload photo without internet connection. Visit upload screen to retry.");
            this.setAborted(true);
            return;
        }

        final ParseFile vidFile = this.getParseFile();
        final ParseFile thumbnailFile = this.getThumbnailFile();
        vidFile.saveInBackground(new SaveCallback() {
                                     @Override
                                     public void done(ParseException e) {
                                         if (e == null) {
                                             saveThumbnail(vidFile, thumbnailFile);
                                         } else {
                                             VideoUpload.this.showError(e, "Video Upload Failure: ");
                                         }
                                     }
                                 }, new ProgressCallback() {
                                     @Override
                                     public void done(Integer percentDone) {
                                         VideoUpload.this.setProgress(percentDone);
                                     }
                                 }
        );
    }

    private void saveThumbnail(final ParseFile videoFile, final ParseFile thumbnailFile) {
        thumbnailFile.saveInBackground(new SaveCallback() {
                                           @Override
                                           public void done(ParseException e) {
                                               if (e == null) {
                                                   saveVideo(videoFile, thumbnailFile);
                                               } else {
                                                   VideoUpload.this.showError(e, "Video Upload Failure: ");
                                               }
                                           }

                                       }
        );
    }

    private void saveVideo(ParseFile videoFile, ParseFile thumbnailFile) {
        Video newVid = new Video();
        newVid.setVideo(videoFile);
        newVid.setThumbnail(thumbnailFile);

        ParseUser thisUser = ParseUser.getCurrentUser();
        newVid.setParseUser(thisUser);
        newVid.setACL(new ParseACL(thisUser));

        newVid.pinInBackground();
        newVid.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    VideoUpload.this.showSuccess("Video Upload Success");
                    VideoUpload.this.setParseFile(null);
                    VideoUpload.this.setThumbnailFile(null);
                }
                else {
                    VideoUpload.this.showError(e, "Video Upload Failure: ");
                }
            }
        });
    }

    @Override
    public void retryUpload() {
        uploadVideo();
    }

    public static VideoUpload prepareVideoUpload(String filepath) {
        final File videoFile = new File(filepath);
        int size = (int) videoFile.length();
        byte[] videoBytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(videoFile));
            buf.read(videoBytes, 0, videoBytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            SystemUtilities.reportError(TAG, "Error Finding Video File: " + e.getMessage());
        } catch (IOException e) {
            SystemUtilities.reportError(TAG, "Error Reading From Video File: " + e.getMessage());
        }

        if (videoBytes.length > 1048576) {
            SystemUtilities.reportError(TAG, "Video File is Too Large, Must be 10mb or less");
            return null;
        }

        final ParseFile thumbnailFile = new ParseFile("thumbnail.jpeg", createVideoThumbnail(filepath));
        final ParseFile vidFile = new ParseFile("video.mp4", videoBytes);
        final VideoUpload thisUpload = new VideoUpload(vidFile, thumbnailFile);
        videoFile.delete();

        UploadManager.getInstance().addUpload(thisUpload);

        return thisUpload;
    }

    private static byte[] createVideoThumbnail(String filepath) {
        Bitmap thumbnailImage = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Images.Thumbnails.MINI_KIND);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thumbnailImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

}
