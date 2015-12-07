package com.triestpa.cloudcamera.Utilities;


import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import com.parse.ParseFile;
import com.triestpa.cloudcamera.Upload.PhotoUpload;
import com.triestpa.cloudcamera.Upload.UploadManager;
import com.triestpa.cloudcamera.Upload.VideoUpload;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class UploadUtilities {
    final static String TAG = UploadUtilities.class.getName();

    public static PhotoUpload preparePhotoUpload(byte[] picData) {
        if (picData.length > 10485760) {
            Log.e(TAG, "Photo File is Too Large, Must be 10mb or less");
            SystemUtilities.showToastMessage("Photo File is Too Large, Must be 10mb or less");
            return null;
        }

        final ParseFile picFile = new ParseFile("photo.jpeg", picData);
        final PhotoUpload thisUpload = new PhotoUpload(picFile);
        UploadManager.getInstance().addUpload(thisUpload);

        return thisUpload;
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
            Log.e(TAG, e.getMessage());
            SystemUtilities.showToastMessage("Error Finding Video File: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            SystemUtilities.showToastMessage("Error Reading From Video File: " + e.getMessage());
        }

        if (videoBytes.length > 1048576) {
            Log.e(TAG, "Video File is Too Large, Must be 10mb or less");
            SystemUtilities.showToastMessage("Video File is Too Large, Must be 10mb or less");
            return null;
        }

        final ParseFile thumbnailFile = new ParseFile("thumbnail.jpeg", createVideoThumbnail(filepath));
        final ParseFile vidFile = new ParseFile("video.mp4", videoBytes);
        final VideoUpload thisUpload = new VideoUpload(vidFile, thumbnailFile);
        videoFile.delete();

        UploadManager.getInstance().addUpload(thisUpload);

        return thisUpload;
    }

    public static byte[] createVideoThumbnail(String filepath) {
        Bitmap thumbnailImage = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Images.Thumbnails.MINI_KIND);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thumbnailImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
}
