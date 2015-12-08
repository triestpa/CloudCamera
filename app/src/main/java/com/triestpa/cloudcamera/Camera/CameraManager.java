package com.triestpa.cloudcamera.Camera;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.Surface;
import android.widget.FrameLayout;

import com.triestpa.cloudcamera.Upload.PhotoUpload;
import com.triestpa.cloudcamera.Upload.VideoUpload;
import com.triestpa.cloudcamera.Utilities.SystemUtilities;
import com.triestpa.cloudcamera.Utilities.UploadUtilities;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("deprecation")
class CameraManager {
    private final static String TAG = CameraManager.class.getName();

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

            if (params.getFlashMode() != null) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }

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
            SystemUtilities.reportError(TAG, "Camera Instance Failed");
        }
    }

    // A safe way to get an instance of the Camera object.
    private static Camera getCameraInstance(int camID) {
        Camera c;
        try {
            c = Camera.open(camID);
            Log.d(TAG, "camera opened");
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            SystemUtilities.reportError(TAG, "Camera Not Available");
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

        // Always use low-quality videos - Parse does not accept if they are larger than 10mb
        if (CamcorderProfile.hasProfile(cameraID, CamcorderProfile.QUALITY_LOW)) {
            mMediaRecorder.setProfile(CamcorderProfile.get(cameraID, CamcorderProfile.QUALITY_LOW));
        }
        else {
            SystemUtilities.reportError(TAG, "Error Setting Up Video Recorder");
            return false;
        }

        // Set output file
        File videoFile = SystemUtilities.getOutputMediaFile(SystemUtilities.MEDIA_TYPE_VIDEO);

        if (videoFile == null) {
            SystemUtilities.reportError(TAG, "Error Creating Video File");
            releaseMediaRecorder();
            return false;
        }

        mVideoOutputFilePath = videoFile.toString();
        mMediaRecorder.setOutputFile(mVideoOutputFilePath);

        // Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            SystemUtilities.reportError(TAG, "Error Preparing Media Recorder");
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            SystemUtilities.reportError(TAG , "Error Preparing Media Recorder");
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
            SystemUtilities.reportError(TAG, "Device Does Not Have Flash");
            return false;
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

        try {
            // set Camera parameters
            mCamera.setParameters(params);
        }
        catch (RuntimeException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }


        if (flashStatus.contentEquals(Camera.Parameters.FLASH_MODE_ON)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean swapCamera(FrameLayout preview, Activity activity) {
        if (Camera.getNumberOfCameras() >= 2) {
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
        else {
            SystemUtilities.reportError(TAG, "Device Does Not Have Second Camera");
            return true;
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
        }
    }

    // Callback to receive the captured photo
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] picData, Camera camera) {
            Log.i("TAG", "Picture Taken");
            // reset to preview to take another pic
            mCamera.stopPreview();
            mCamera.startPreview();
            preview_active = true;
            PhotoUpload photoUpload = UploadUtilities.preparePhotoUpload(picData);

            if (photoUpload != null) {
                photoUpload.uploadPhoto();
            }
        }
    };

    public boolean toggleRecording() {
        if (isRecording) {
            // stop recording and release camera
            mMediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder
            isRecording = false;
            Log.i(TAG, "Video Recorded");
            VideoUpload videoUpload = UploadUtilities.prepareVideoUpload(mVideoOutputFilePath);
            if (videoUpload != null) {
                videoUpload.uploadVideo();
            }
            else {
                Log.e(TAG, "Error Formatting Upload");
            }
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
}
