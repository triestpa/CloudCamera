package com.triestpa.cloudcamera.Camera;

import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;
import com.triestpa.cloudcamera.Gallery.GalleryActivity;
import com.triestpa.cloudcamera.Model.Picture;
import com.triestpa.cloudcamera.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("deprecation")
// Suppress warnings for the more compatible, deprecated Camera class
public class CameraActivity extends AppCompatActivity {
    protected final static String TAG = CameraActivity.class.getName();
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;


    private Boolean preview_active;
    protected static int cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private String flashStatus = Camera.Parameters.FLASH_MODE_OFF;

    /**
     * ----- Activity Lifecycle Events -----
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        setPictureButton();
        setCameraSwapButton();
        setFlashButton();
        setRecordButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reinitialize the camera and preview on resume
        cameraInit(cameraID);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }

    /* End the camera and view on pause */
    @Override
    protected void onPause() {
        super.onPause();
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeView(mPreview);

        mCamera.stopPreview();
        releaseMediaRecorder(); // if you are using MediaRecorder, release it first
        releaseCamera();
    }

    /**
     * ----- Toolbar Events -----
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_gallery) {
            Intent galleryIntent = new Intent(this, GalleryActivity.class);
            startActivity(galleryIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * ----- Set UI Listeners -----
     */

    public void setPictureButton() {
        // Add a listener to the Capture button
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
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

        });
    }

    //Set a button to flip the camera to front-facing/back-facing
    public void setCameraSwapButton() {
        // Add a listener to the Capture button
        ImageButton swapButton = (ImageButton) findViewById(R.id.button_swap);
        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                // must remove view before swapping it
                preview.removeView(mPreview);
                if (cameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    // switch to front facing camera
                    cameraInit(Camera.CameraInfo.CAMERA_FACING_FRONT);

                    // the flash is disabled if front camera is in use
                    flashStatus = Camera.Parameters.FLASH_MODE_OFF;
                    ImageButton flashButton = (ImageButton) findViewById(R.id.button_flash);
                    flashButton.setVisibility(View.INVISIBLE);
                    flashButton.setImageResource(R.drawable.ic_action_flash_off);
                } else {
                    // switch to back facing camera
                    cameraInit(Camera.CameraInfo.CAMERA_FACING_BACK);
                    ImageButton flashButton = (ImageButton) findViewById(R.id.button_flash);
                    flashButton.setVisibility(View.VISIBLE);
                }
                preview.addView(mPreview);
            }
        });
    }

    //Toggle the camera flash
    public void setFlashButton() {
        // Add a listener to the Capture button
        ImageButton flashButton = (ImageButton) findViewById(R.id.button_flash);
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Not applicable if front facing camera is selected
                if (cameraID == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    return;
                }

                // get Camera parameters
                Camera.Parameters params = mCamera.getParameters();
                ImageButton flashButton = (ImageButton) findViewById(R.id.button_flash);

                // Check if device has flash
                if (params.getFlashMode() == null) {
                    Log.d(TAG, "Device Does Not Have Flash");
                } else if (params.getFlashMode()
                        .contentEquals(Camera.Parameters.FLASH_MODE_ON)) {
                    // turn flash off
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    flashStatus = Camera.Parameters.FLASH_MODE_OFF;
                    flashButton.setImageResource(R.drawable.ic_action_flash_off);
                } else {
                    // turn flash on
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    flashStatus = Camera.Parameters.FLASH_MODE_ON;
                    flashButton.setImageResource(R.drawable.ic_action_flash_on);
                }
                // set Camera parameters
                mCamera.setParameters(params);
            }
        });
    }

    public void setRecordButton() {
        // Add a listener to the Capture button
        Button recordButton = (Button) findViewById(R.id.button_record);

        recordButton.setOnClickListener(
                new View.OnClickListener()

                {
                    @Override
                    public void onClick(View v) {
                        toggleRecording();
                    }
                });
    }

    /**
     * ----- Camera Configuration Methods -----
     */

    public void cameraInit(int camID) {
        mCamera = getCameraInstance(camID);
        cameraID = camID;

        // Sync the device rotation to the camera rotation
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraID, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
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
        mPreview = new CameraPreview(this, mCamera, previewRotate);
        preview_active = true;
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

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
            Log.d(TAG, "Media Recorder Released");
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
            Log.d(TAG, "Camera Released");
        }
    }

    // Callback to receive the captured photo
    private PictureCallback mPicture = new PictureCallback() {

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

    public void toggleRecording() {
        Button buttonRecord = (Button) findViewById(R.id.button_record);

        if (isRecording) {
            // stop recording and release camera
            mMediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            buttonRecord.setText("Record");
            isRecording = false;
        } else {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder.start();

                // inform the user that recording has started
                buttonRecord.setText("Stop");
                isRecording = true;
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                Log.e(TAG, "Prepare Video Recorder Failed");
            }
        }
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
