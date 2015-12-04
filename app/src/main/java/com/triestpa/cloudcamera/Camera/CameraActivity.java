package com.triestpa.cloudcamera.Camera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.triestpa.cloudcamera.Gallery.GalleryActivity;
import com.triestpa.cloudcamera.R;

// Suppress warnings for the more compatible, deprecated Camera class
public class CameraActivity extends AppCompatActivity {
    protected final static String TAG = CameraActivity.class.getName();
    CameraManager mCameraManager;


    /**
     * ----- Activity Lifecycle Events -----
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mCameraManager = new CameraManager();

        setPictureButton();
        setCameraSwapButton();
        setFlashButton();
        setRecordButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reinitialize the camera and preview on resume
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        mCameraManager.cameraInit(mCameraManager.cameraID, this, preview);
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
                mCameraManager.takePicture();
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
                if (mCameraManager.swapCamera(preview, CameraActivity.this)) {
                    ImageButton flashButton = (ImageButton) findViewById(R.id.button_flash);
                    flashButton.setVisibility(View.VISIBLE);
                } else {
                    ImageButton flashButton = (ImageButton) findViewById(R.id.button_flash);
                    flashButton.setVisibility(View.INVISIBLE);
                    flashButton.setImageResource(R.drawable.ic_action_flash_off);
                }
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
                ImageButton flashButton = (ImageButton) v;

                if (mCameraManager.toggleFlash()) {
                    flashButton.setImageResource(R.drawable.ic_action_flash_on);
                } else {
                    flashButton.setImageResource(R.drawable.ic_action_flash_off);
                }
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
                        Button recordButton = (Button) v;
                        if (mCameraManager.toggleRecording()) {
                            recordButton.setText("Stop");
                            lockOrientation();
                        } else {
                            recordButton.setText("Record");
                            unlockOrientation();
                        }
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
