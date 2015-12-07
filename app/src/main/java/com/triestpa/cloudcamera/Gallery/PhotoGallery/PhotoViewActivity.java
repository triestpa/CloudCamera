package com.triestpa.cloudcamera.Gallery.PhotoGallery;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.triestpa.cloudcamera.Model.Picture;
import com.triestpa.cloudcamera.R;
import com.triestpa.cloudcamera.Utilities.BitmapUtilities;
import com.triestpa.cloudcamera.Utilities.SystemUtilities;

import java.io.IOException;

import uk.co.senab.photoview.PhotoViewAttacher;

public class PhotoViewActivity extends AppCompatActivity {
    static final String TAG = PhotoViewActivity.class.getName();
    public static final String EXTRA_PHOTO_ID = "PHOTO_ID";
    public static final String EXTRA_THUMBNAIL_URL = "THUMBNAIL_URL";
    public static final String EXTRA_THUMBNAIL_BYTES = "THUMBNAIL BYTES";
    public static final String EXTRA_FULLSIZE_URL = "FULLSIZE_URL";

    private ImageView mImageView;
    private PhotoViewAttacher mAttacher;

    private String mFullsizeURL, mThumbnailURL, mPhotoID;
    ;
    private int mWidth, mHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageButton deleteButton = (ImageButton) findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildDeleteDialog();
            }
        });

        ImageButton downloadButton = (ImageButton) findViewById(R.id.download_button);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildDownloadDialog();
            }
        });

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mWidth = size.x;
        mHeight = size.y;

        Intent intent = getIntent();
        mPhotoID = intent.getStringExtra(EXTRA_PHOTO_ID);
        mFullsizeURL = intent.getStringExtra(EXTRA_FULLSIZE_URL);
        mThumbnailURL = intent.getStringExtra(EXTRA_THUMBNAIL_URL);
        byte[] thumbnailBytes = intent.getByteArrayExtra(EXTRA_THUMBNAIL_BYTES);
        Bitmap thumbnailBitmap = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.length);
        mImageView = (ImageView) findViewById(R.id.fullsize_image);
        mImageView.setImageBitmap(thumbnailBitmap);
        mAttacher = new PhotoViewAttacher(mImageView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mImageView == null) {
            mImageView = (ImageView) findViewById(R.id.fullsize_image);
            mAttacher = new PhotoViewAttacher(mImageView);
        }

        downloadImage(mFullsizeURL);
    }

    @Override
    public void onTrimMemory(int level) {
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            mImageView = null;
            mAttacher = null;
            System.gc();
        }
        super.onTrimMemory(level);
    }

    public void downloadImage(String url) {
        ImageDownloadHandler handler = new ImageDownloadHandler();
        handler.execute(url);
    }

    public class ImageDownloadHandler extends AsyncTask<String, Void, Bitmap> {

        OkHttpClient client = new OkHttpClient();

        @Override
        protected Bitmap doInBackground(String... params) {

            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);

            Request request = builder.build();

            try {
                Response response = client.newCall(request).execute();
                byte[] bytes = response.body().bytes();

                if (bytes != null && bytes.length > 0) {

                    //http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
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
                Log.e(TAG, e.getMessage());
                SystemUtilities.showToastMessage("Error Downloading Image: " + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap imageBm) {
            if (imageBm != null) {
                mImageView.setImageBitmap(imageBm);
                mAttacher.update();
            } else {
                Log.e(TAG, "Error Loading Bitmap");
                SystemUtilities.showToastMessage("Error Loading Image");

            }
            super.onPostExecute(imageBm);
        }
    }

    public void deletePhoto() {
        ParseQuery<Picture> query = ParseQuery.getQuery(Picture.class);
        query.getInBackground(mPhotoID, new GetCallback<Picture>() {
            public void done(Picture picture, ParseException e) {
                if (e == null) {
                    picture.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                SystemUtilities.showToastMessage("Photo Deleted");
                                PhotoViewActivity.this.onBackPressed();
                            } else {
                                Log.e(TAG, e.getMessage());
                                SystemUtilities.showToastMessage("Error Deleting File: " + e.getMessage());
                            }
                        }
                    });
                } else {
                    Log.e(TAG, e.getMessage());
                    SystemUtilities.showToastMessage("Error Deleting File: " + e.getMessage());
                }
            }
        });
    }

    private void buildDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Delete Photo From Cloud?");
        builder.setIcon(R.drawable.ic_delete_white_24dp);

        // Add the buttons
        builder.setPositiveButton(R.string.delete_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deletePhoto();
            }
        });
        builder.setNegativeButton(R.string.delete_dialog_canel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.cancel();
            }
        });
        // Create the AlertDialog
        builder.create().show();
    }

    private void buildDownloadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Download Photo From Cloud?");
        builder.setIcon(R.drawable.ic_delete_white_24dp);

        // Add the buttons
        builder.setPositiveButton(R.string.download_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SystemUtilities.downloadFile(mFullsizeURL, mPhotoID, SystemUtilities.MEDIA_TYPE_IMAGE);
            }
        });
        builder.setNegativeButton(R.string.download_dialog_canel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.cancel();
            }
        });
        // Create the AlertDialog
        builder.create().show();
    }
}
