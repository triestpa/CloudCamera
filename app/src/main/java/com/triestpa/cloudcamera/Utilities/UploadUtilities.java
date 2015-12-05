package com.triestpa.cloudcamera.Utilities;


import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.triestpa.cloudcamera.Model.Picture;
import com.triestpa.cloudcamera.Model.Video;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UploadUtilities {

    final static String TAG = UploadUtilities.class.getName();

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static void uploadPhoto(byte[] picData) {
        final ParseFile picFile = new ParseFile("photo.jpeg", picData);
        final Upload thisUpload = new Upload(picFile);

        UploadManager.getInstance().addUpload(thisUpload);

        picFile.saveInBackground(new SaveCallback() {
                                     @Override
                                     public void done(ParseException e) {
                                         if (e == null) {
                                             savePhoto(picFile);
                                             thisUpload.setCompleted(true);
                                         } else {
                                             Log.e(TAG, e.getMessage());
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

    public static void savePhoto(ParseFile photoFile) {
        Picture newPic = new Picture();
        newPic.setPhoto(photoFile);
        newPic.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Picture Uploaded");
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    public static void saveVideoFile(String filepath) {
        File file = new File(filepath);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        Bitmap thumbnailImage = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Images.Thumbnails.MINI_KIND);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thumbnailImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        final ParseFile thumbnailFile = new ParseFile("thumbnail.jpeg", byteArray);


        final ParseFile vidFile = new ParseFile("video.mp4", bytes);
        vidFile.saveInBackground(new SaveCallback() {
                                     @Override
                                     public void done(ParseException e) {
                                         if (e == null) {
                                             saveThumbnail(vidFile, thumbnailFile);
                                         } else {
                                             Log.e(TAG, e.getMessage());
                                         }
                                     }

                                 }
        );
    }

    public static void saveThumbnail(final ParseFile videoFile,final ParseFile thumbnailFile) {
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

    public static void saveVideo(ParseFile videoFile, ParseFile thumbnailFile) {
        Video newVid = new Video();
        newVid.setVideo(videoFile);
        newVid.setThumbnail(thumbnailFile);
        newVid.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Video Uploaded");
                    //TODO delete video file
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    /*
     * Create a File for saving an image or video
     */
    public static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CloudCamera");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }


}
