package com.triestpa.cloudcamera.GalleryScreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.triestpa.cloudcamera.CloudCameraApplication;
import com.triestpa.cloudcamera.Model.Picture;
import com.triestpa.cloudcamera.R;
import com.triestpa.cloudcamera.Utilities.BitmapUtilities;
import com.triestpa.cloudcamera.Utilities.SystemUtilities;

import java.io.IOException;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Photo View Activity: Show a fullscreen, zoomable view of selected image
 */
public class PhotoViewActivity extends AppCompatActivity {

    // Set constants
    private static final String TAG = PhotoViewActivity.class.getName();
    public static final String EXTRA_PHOTO_ID = "PHOTO_ID";
    public static final String EXTRA_THUMBNAIL_URL = "THUMBNAIL_URL";
    public static final String EXTRA_THUMBNAIL_BYTES = "THUMBNAIL BYTES";
    public static final String EXTRA_FULLSIZE_URL = "FULLSIZE_URL";

    // UI references
    private ImageView mImageView;
    private PhotoViewAttacher mAttacher;

    // Picture data
    private String mFullsizeURL, mThumbnailURL, mPhotoID;

    // Screensize
    private int mWidth, mHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        // Set back arrow button to emulate physical back button
        ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Setup delete photo button
        ImageButton deleteButton = (ImageButton) findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirm dialog
                SystemUtilities.buildDialog(PhotoViewActivity.this, "Delete Photo From Cloud?", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (SystemUtilities.isOnline(PhotoViewActivity.this)) {
                            deletePhoto();// Delete file if device is online
                        }
                        else {
                            SystemUtilities.reportError(TAG, "Error Deleting Image: Device is Offline");
                        }
                    }
                }).show();
            }
        });

        // Setup download photo button
        ImageButton downloadButton = (ImageButton) findViewById(R.id.download_button);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirm dialog
                SystemUtilities.buildDialog(PhotoViewActivity.this, "Downlaod Photo From Cloud?", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (SystemUtilities.isOnline(PhotoViewActivity.this)) {
                            // Download file if device is online
                            SystemUtilities.downloadFile(mFullsizeURL, mPhotoID, SystemUtilities.MEDIA_TYPE_IMAGE);
                        }
                        else {
                            SystemUtilities.reportError(TAG, "Error Downloading Image: Device is Offline");
                        }
                    }
                }).show();
            }
        });

        // Get window size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mWidth = size.x;
        mHeight = size.y;

        // Get photo data from intent
        Intent intent = getIntent();
        mPhotoID = intent.getStringExtra(EXTRA_PHOTO_ID);
        mFullsizeURL = intent.getStringExtra(EXTRA_FULLSIZE_URL);
        mThumbnailURL = intent.getStringExtra(EXTRA_THUMBNAIL_URL);

        // Show thumbnail in imageview
        byte[] thumbnailBytes = intent.getByteArrayExtra(EXTRA_THUMBNAIL_BYTES);
        Bitmap thumbnailBitmap = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.length);
        mImageView = (ImageView) findViewById(R.id.fullsize_image);
        mImageView.setImageBitmap(thumbnailBitmap);

        mAttacher = new PhotoViewAttacher(mImageView); // Make zoomable

        // Download fullsize image
        if (SystemUtilities.isOnline(this)) {
            ImageDownloadHandler handler = new ImageDownloadHandler();
            handler.execute(mFullsizeURL);
            Toast.makeText(this, "Downloading Full Image...", Toast.LENGTH_SHORT).show();
        } else {
            SystemUtilities.reportError(TAG, "Error Downloading Fullsize Image: Device is Offline");
        }
    }

    // Async task to download image
    public class ImageDownloadHandler extends AsyncTask<String, Void, Bitmap> {

        OkHttpClient client = new OkHttpClient();

        @Override
        protected Bitmap doInBackground(String... params) {

            // Build HTTP request
            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);
            Request request = builder.build();

            try {
                // Execute request
                Response response = client.newCall(request).execute();

                // Read data from response
                byte[] bytes = response.body().bytes();

                if (bytes != null && bytes.length > 0) {

                    // Decode bitmap to size that is optimal for the screen
                    // Adapted from: http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    BitmapFactory.decodeByteArray(bytes, 0,
                            bytes.length, options);

                    // Calculate inSampleSize
                    options.inSampleSize = BitmapUtilities.calculateInSampleSize(options, mWidth, mHeight);

                    // Decode bitmap with inSampleSize set
                    options.inJustDecodeBounds = false;
                    return BitmapFactory.decodeByteArray(bytes, 0,
                            bytes.length, options);
                } else {
                    return null;
                }

            } catch (IOException e) {
                SystemUtilities.reportError(TAG, "Error Downloading Image: " + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap imageBm) {
            if (imageBm != null) {
                mImageView.setImageBitmap(imageBm);
                mAttacher.update();
            } else {
                SystemUtilities.reportError(TAG, "Error Loading Image");

            }
            super.onPostExecute(imageBm);
        }
    }

    // Delete the photo from Parse
    private void deletePhoto() {
        Toast.makeText(CloudCameraApplication.getAppContext(), "Deleting...", Toast.LENGTH_SHORT).show();
        ParseQuery<Picture> query = ParseQuery.getQuery(Picture.class);

        // No need to use bandwidth to find photo, it must be cached
        query.fromLocalDatastore();

        // Find the photo by ID
        query.getInBackground(mPhotoID, new GetCallback<Picture>() {
            public void done(final Picture picture, ParseException e) {
                if (e == null) {
                    // Delete photo
                    picture.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                picture.unpinInBackground();
                                Toast.makeText(CloudCameraApplication.getAppContext(), "Photo Deleted", Toast.LENGTH_SHORT).show();
                                PhotoViewActivity.this.onBackPressed(); // Return to gallery
                            } else {
                                Log.e(TAG, e.getMessage());
                                SystemUtilities.reportError(TAG, "Error Deleting File: " + e.getMessage());
                            }
                        }
                    });
                } else {
                    Log.e(TAG, e.getMessage());
                    SystemUtilities.reportError(TAG, "Error Deleting File: " + e.getMessage());
                }
            }
        });
    }
}
