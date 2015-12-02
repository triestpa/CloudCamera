package com.triestpa.cloudcamera;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

@SuppressWarnings( "deprecation" ) // Suppress warnings for the more compatible, deprecated Camera class
public class CameraActivity extends AppCompatActivity {
    protected final static String TAG = CameraActivity.class.getName();
    public static final int MEDIA_TYPE_IMAGE = 1;

    private Camera mCamera;
    private CameraPreview mPreview;

    private Boolean preview_active;
    protected static int cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private String flashStatus = Camera.Parameters.FLASH_MODE_OFF;

    /* ----- Activity Lifecycle Events -----*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        setPictureButton();
        setCameraSwapButton();
        setFlashButton();
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
        mPreview.stopPreviewAndFreeCamera();
    }


    /* ----- Set UI Listeners -----*/

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

                if (params.getFlashMode() == null) {
                    Log.d(TAG, "Device Does Not Have Flash");
                }
                else if (params.getFlashMode()
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


    /* ----- Camera Configuration Methods -----*/

    public void cameraInit(int camID) {
        mCamera = getCameraInstance(camID);
        cameraID = camID;

        // Create our Preview view and set it as the content of our
        // activity.
        mPreview = new CameraPreview(this, mCamera);
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

    // Callback to receive the captured photo
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Log.i("TAG", "Picture Taken");

           // Intent intent = new Intent(getBaseContext(), SubmitPhotoActivity.class);
           // intent.putExtra("picture", data);
           // startActivity(intent);
        }
    };
}
