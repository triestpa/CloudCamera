package com.triestpa.cloudcamera.Utilities;


import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.triestpa.cloudcamera.Model.Picture;
import com.triestpa.cloudcamera.Model.Video;
import com.triestpa.cloudcamera.Upload.Upload;
import com.triestpa.cloudcamera.Upload.UploadManager;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class UploadUtilities {
    final static String TAG = UploadUtilities.class.getName();

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static void uploadPhoto(byte[] picData) {
        if (picData.length > 1048576) {
            Log.e(TAG, "Photo File is Too Large, Must be 10mb or less");
            SystemUtilities.showToastMessage("Photo File is Too Large, Must be 10mb or less");
            return;
        }

        final ParseFile picFile = new ParseFile("photo.jpeg", picData);
        final Upload thisUpload = new Upload(picFile, Upload.TYPE_PHOTO);
        UploadManager.getInstance().addUpload(thisUpload);

        picFile.saveInBackground(new SaveCallback() {
                                     @Override
                                     public void done(ParseException e) {
                                         if (e == null) {
                                             savePhoto(picFile);
                                             thisUpload.setCompleted(true);
                                             SystemUtilities.showToastMessage("Photo Upload Success");
                                         } else {
                                             Log.e(TAG, e.getMessage());
                                             thisUpload.setAborted(true);
                                             SystemUtilities.showToastMessage("Photo Upload Failure " + e.getMessage());

                                         }
                                     }
                                 }, new ProgressCallback() {
                                     @Override
                                     public void done(Integer percentDone) {
                                        thisUpload.setProgress(percentDone);
                                     }
                                 }
        );
    }

    private static void savePhoto(ParseFile photoFile) {
        Picture newPic = new Picture();
        newPic.setPhoto(photoFile);
        newPic.saveInBackground();
    }

    public static void uploadVideo(String filepath) {
        final File videoFile = new File(filepath);
        int size = (int) videoFile.length();
        byte[] videoBytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(videoFile));
            buf.read(videoBytes, 0, videoBytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        if (videoBytes.length > 1048576) {
            Log.e(TAG, "Video File is Too Large, Must be 10mb or less");
            SystemUtilities.showToastMessage("Video File is Too Large, Must be 10mb or less");
            return;
        }

        final ParseFile thumbnailFile = new ParseFile("thumbnail.jpeg", createVideoThumbnail(filepath));
        final ParseFile vidFile = new ParseFile("video.mp4", videoBytes);
        final Upload thisUpload = new Upload(vidFile, Upload.TYPE_VIDEO);
        UploadManager.getInstance().addUpload(thisUpload);

        vidFile.saveInBackground(new SaveCallback() {
                                     @Override
                                     public void done(ParseException e) {
                                         if (e == null) {
                                             thisUpload.setCompleted(true);
                                             videoFile.delete();
                                             saveThumbnail(vidFile, thumbnailFile);
                                             SystemUtilities.showToastMessage("Video Upload Success");
                                         } else {
                                             Log.e(TAG, e.getMessage());
                                             thisUpload.setAborted(true);
                                             SystemUtilities.showToastMessage("Video Upload Failure: " + e.getMessage());
                                         }
                                     }
                                 }, new ProgressCallback() {
                                     @Override
                                     public void done(Integer percentDone) {
                                         thisUpload.setProgress(percentDone);
                                     }
                                 }
        );
    }

    private static byte[] createVideoThumbnail(String filepath) {
        Bitmap thumbnailImage = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Images.Thumbnails.MINI_KIND);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thumbnailImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    private static void saveThumbnail(final ParseFile videoFile,final ParseFile thumbnailFile) {
        thumbnailFile.saveInBackground(new SaveCallback() {
                                           @Override
                                           public void done(ParseException e) {
                                               if (e == null) {
                                                   saveVideo(videoFile, thumbnailFile);
                                               } else {
                                                   Log.e(TAG, e.getMessage());
                                               }
                                           }

                                       }
        );
    }

    private static void saveVideo(ParseFile videoFile, ParseFile thumbnailFile) {
        Video newVid = new Video();
        newVid.setVideo(videoFile);
        newVid.setThumbnail(thumbnailFile);
        newVid.saveInBackground();
    }
}