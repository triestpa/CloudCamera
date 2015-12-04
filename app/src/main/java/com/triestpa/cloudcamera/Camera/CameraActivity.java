package com.triestpa.cloudcamera.Camera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.github.clans.fab.FloatingActionButton;
import com.triestpa.cloudcamera.Gallery.GalleryActivity;
import com.triestpa.cloudcamera.R;

// Suppress warnings for the more compatible, deprecated Camera class
public class CameraActivity extends AppCompatActivity {
    protected final static String TAG = CameraActivity.class.getName();
    CameraManager mCameraManager;

    private final static int MODE_PICTURE = 0;
    private final static int MODE_VIDEO = 1;
    int mMode = MODE_PICTURE;

    FloatingActionButton mFlashButton, mCaptureButton, mSwapButton, mModeButton, mGalleryButton;

    /**
     * ----- Activity Lifecycle Events -----
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mCameraManager = new CameraManager();


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reinitialize the camera and preview on resume
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        mCameraManager.cameraInit(mCameraManager.cameraID, this, preview);

        mFlashButton = (FloatingActionButton) findViewById(R.id.button_flash);
        mCaptureButton = (FloatingActionButton) findViewById(R.id.button_capture);
        mSwapButton = (FloatingActionButton) findViewById(R.id.button_swap);
        mModeButton = (FloatingActionButton) findViewById(R.id.button_mode);
        mGalleryButton = (FloatingActionButton) findViewById(R.id.button_gallery);

        setCaptureButton();
        setCameraSwapButton();
        setFlashButton();
        setModeButton();
        setGalleryButton();
    }

    /* End the camera and view on pause */
    @Override
    protected void onPause() {
        super.onPause();
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        mCameraManager.stopPreview(preview);
        mCameraManager.releaseMediaRecorder(); // if you are using MediaRecorder, release it first
        mCameraManager.releaseCamera();
    }

    /**
     * ----- Set UI Listeners -----
     */

    public void setCaptureButton() {
        // Add a listener to the Capture button
        mCaptureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mMode == MODE_PICTURE) {
                    mCameraManager.takePicture();
                } else {
                    if (mCameraManager.toggleRecording()) {
                        lockOrientation();
                    } else {
                        unlockOrientation();
                    }
                }
            }

        });
    }

    public void setModeButton() {
        mModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMode == MODE_PICTURE) {
                    mMode = MODE_VIDEO;
                    mModeButton.setImageResource(R.drawable.ic_switch_camera_white_24dp);
                    mModeButton.setLabelText(getString(R.string.camera_mode_photo));
                    mCaptureButton.setImageResource(R.drawable.ic_videocam_white_24dp);
                }
                else {
                    mMode = MODE_PICTURE;
                    mModeButton.setImageResource(R.drawable.ic_switch_video_white_24dp);
                    mModeButton.setLabelText(getString(R.string.camera_mode_video));
                    mCaptureButton.setImageResource(R.drawable.ic_photo_camera_white_24dp);
                }
            }
        });
    }

    //Set a button to flip the camera to front-facing/back-facing
    public void setCameraSwapButton() {
        // Add a listener to the Capture button
        mSwapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                if (mCameraManager.swapCamera(preview, CameraActivity.this)) {
                    mSwapButton.setLabelText(getString(R.string.camera_front));
                    mSwapButton.setImageResource(R.drawable.ic_camera_front_white_24dp);

                    mFlashButton.setVisibility(View.VISIBLE);
                } else {
                    mSwapButton.setLabelText(getString(R.string.camera_rear));
                    mSwapButton.setImageResource(R.drawable.ic_camera_rear_white_24dp);

                    mFlashButton.setVisibility(View.GONE);
                    mFlashButton.setImageResource(R.drawable.ic_flash_off_white_24dp);
                    mFlashButton.setLabelText(getString(R.string.camera_flash_off));
                }
            }
        });
    }

    //Toggle the camera flash
    public void setFlashButton() {
        // Add a listener to the Capture button
        mFlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCameraManager.toggleFlash()) {
                    mFlashButton.setImageResource(R.drawable.ic_flash_off_white_24dp);
                    mFlashButton.setLabelText(getString(R.string.camera_flash_off));
                } else {
                    mFlashButton.setImageResource(R.drawable.ic_flash_on_white_24dp);
                    mFlashButton.setLabelText(getString(R.string.camera_flash_on));
                }
            }
        });
    }

    public void setGalleryButton() {
        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(getApplicationContext(), GalleryActivity.class);
                startActivity(galleryIntent);
            }
        });
    }


    public void lockOrientation() {
        int orientation = getRequestedOrientation();
        int rotation = ((WindowManager) getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
            case Surface.ROTATION_90:
                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            case Surface.ROTATION_180:
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                break;
            case Surface.ROTATION_270:
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                break;
            default:
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
        }
        setRequestedOrientation(orientation);
    }

    public void unlockOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }


}
