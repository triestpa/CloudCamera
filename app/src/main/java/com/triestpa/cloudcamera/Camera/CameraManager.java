package com.triestpa.cloudcamera.Camera;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.widget.FrameLayout;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;
import com.triestpa.cloudcamera.Model.Picture;

import java.io.File;
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
    public String flashStatus = Camera.Parameters.FLASH_MODE_OFF;
    public boolean isRecording = false;

    public CameraManager() {
    }


    /**
     * ----- Camera Initialization Methods -----
     */
    public void cameraInit(int camID, Activity activity, FrameLayout preview) {
        mCamera = getCameraInstance(camID);
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

        //    mCamera = getCameraInstance(cameraID);
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
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
        preview.removeView(mPreview);
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
        }
        else {
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
            final ParseFile picFile = new ParseFile("photo.jpeg", picData);
            picFile.saveInBackground(new SaveCallback() {
                                         @Override
                                         public void done(ParseException e) {
                                             Picture newPic = new Picture();
                                             newPic.setPhoto(picFile);
                                             newPic.saveInBackground();
                                         }
                                     }
            );
        }
    };

    public boolean toggleRecording() {

        if (isRecording) {
            // stop recording and release camera
            mMediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder
            isRecording = false;
        } else {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
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

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
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