package com.triestpa.cloudcamera.Camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.widget.FrameLayout;

import com.parse.ParseException;
import com.parse.ParseFile;
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

public class CameraManager {
    private final static String TAG = CameraManager.class.getName();
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;

    private Boolean preview_active;

    public int cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private String flashStatus = Camera.Parameters.FLASH_MODE_OFF;
    private boolean isRecording = false;
    private String mVideoOutputFilePath;
    private int mRotate;

    public CameraManager() {
    }

    /**
     * ----- Camera Initialization Methods -----
     */
    public void cameraInit(int camID, Activity activity, FrameLayout preview) {
        mCamera = getCameraInstance(camID);

        if (mCamera != null) {
            cameraID = camID;

            // Sync the device rotation to the camera rotation
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraID, info);
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break; //Natural orientation
                case Surface.ROTATION_90:
                    degrees = 90;
                    break; //Landscape left
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;//Upside down
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;//Landscape right
            }
            int rotate = (info.orientation - degrees + 360) % 360;

            mRotate = rotate;

            Camera.Parameters params = mCamera.getParameters();
            params.setRotation(rotate);

            mCamera.setParameters(params);

            int previewRotate = rotate;
            if (cameraID == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                previewRotate = (rotate + 180) % 360;
            }

            // Create our Preview view and set it as the content of our
            // activity.
            mPreview = new CameraPreview(activity, mCamera, previewRotate);
            preview_active = true;
            preview.addView(mPreview);
        }
        else {
            Log.e(TAG, "Camera Instance Failed");
        }
    }

    // A safe way to get an instance of the Camera object.
    public static Camera getCameraInstance(int camID) {
        Camera c;
        try {
            if (android.os.Build.VERSION.SDK_INT >= 9)
                c = Camera.open(camID);
            else c = Camera.open();
            Log.d(TAG, "camera opened");
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.e(TAG, "camera unavailable");
            return null;
        }

        return c; // returns null if camera is unavailable
    }


    /**
     * ----- Camera Release Methods -----
     */
    public void stopPreview(FrameLayout previewLayout) {
        previewLayout.removeView(mPreview);
        mCamera.stopPreview();
    }

    public void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
            Log.d(TAG, "Media Recorder Released");
        }
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
            Log.d(TAG, "Camera Released");
        }
    }

    /**
     * ----- Video Configuration Methods -----
     */
    private boolean prepareVideoRecorder() {
        mMediaRecorder = new MediaRecorder();

        //Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Set orientation
        mMediaRecorder.setOrientationHint(mRotate);

        // Set a CamcorderProfile (requires API Level 8 or higher)
        if (cameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        }
        else {
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
        }

        // Set output file
        mVideoOutputFilePath = getOutputMediaFile(MEDIA_TYPE_VIDEO).toString();
        mMediaRecorder.setOutputFile(mVideoOutputFilePath);

        // Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.e(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    /**
     * ----- Camera Customization Methods -----
     */
    public boolean toggleFlash() {
        // Not applicable if front facing camera is selected
        if (cameraID == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return false;
        }

        // get Camera parameters
        Camera.Parameters params = mCamera.getParameters();

        // Check if device has flash
        if (params.getFlashMode() == null) {
            Log.d(TAG, "Device Does Not Have Flash");
        } else if (params.getFlashMode()
                .contentEquals(Camera.Parameters.FLASH_MODE_ON)) {
            // turn flash off
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            flashStatus = Camera.Parameters.FLASH_MODE_OFF;
        } else {
            // turn flash on
            params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            flashStatus = Camera.Parameters.FLASH_MODE_ON;
        }
        // set Camera parameters
        mCamera.setParameters(params);

        if (flashStatus == Camera.Parameters.FLASH_MODE_ON) {
            return true;
        } else {
            return false;
        }
    }

    public boolean swapCamera(FrameLayout preview, Activity activity) {
        // must remove view before swapping it
        stopPreview(preview);
        releaseCamera();

        if (cameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
            // switch to front facing camera
            cameraInit(Camera.CameraInfo.CAMERA_FACING_FRONT, activity, preview);

            // the flash is disabled if front camera is in use
            flashStatus = Camera.Parameters.FLASH_MODE_OFF;
        } else {
            // switch to back facing camera
            cameraInit(Camera.CameraInfo.CAMERA_FACING_BACK, activity, preview);
        }

        if (cameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * ----- Media Capture Methods -----
     */
    public void takePicture() {
        if (preview_active) {
            // get an image from the camera
            mCamera.takePicture(null, null, mPicture);
            preview_active = false;
        } else {
            // else reset to preview to take another pic
            mCamera.stopPreview();
            mCamera.startPreview();
            preview_active = true;
        }
    }

    // Callback to receive the captured photo
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] picData, Camera camera) {
            Log.i("TAG", "Picture Taken");
            uploadPhoto(picData);
        }
    };

    private void uploadPhoto(byte[] picData) {
        final ParseFile picFile = new ParseFile("photo.jpeg", picData);
        picFile.saveInBackground(new SaveCallback() {
                                     @Override
                                     public void done(ParseException e) {
                                         if (e == null) {
                                             savePhoto(picFile);
                                         } else {
                                             Log.e(TAG, e.getMessage());
                                         }
                                     }
                                 }
        );
    }

    private void savePhoto(ParseFile photoFile) {
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

    public boolean toggleRecording() {

        if (isRecording) {
            // stop recording and release camera
            mMediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder
            isRecording = false;
            Log.i("TAG", "Video Recorded");
            saveVideoFile();
        } else {
            // initialize video camera
            if (prepareVideoRecorder()) {
                mMediaRecorder.start();
                isRecording = true;
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                Log.e(TAG, "Prepare Video Recorder Failed");
            }
        }
        return isRecording;
    }

    private void saveVideoFile() {
        File file = new File(mVideoOutputFilePath);
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

        Bitmap thumbnailImage = ThumbnailUtils.createVideoThumbnail(mVideoOutputFilePath, MediaStore.Images.Thumbnails.MINI_KIND);
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

    private void saveThumbnail(final ParseFile videoFile,final ParseFile thumbnailFile) {
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

    private void saveVideo(ParseFile videoFile, ParseFile thumbnailFile) {
        Video newVid = new Video();
        newVid.setVideo(videoFile);
        newVid.setThumbnail(thumbnailFile);
        newVid.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Video Uploaded");
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    /*
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /*
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
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
