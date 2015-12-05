package com.triestpa.cloudcamera.Gallery.PhotoGallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.triestpa.cloudcamera.R;

import java.io.IOException;

public class PhotoViewActivity extends AppCompatActivity {
    static final String TAG = PhotoViewActivity.class.getName();
    public static final String EXTRA_THUMBNAIL_URL = "THUMBNAIL_URL";
    public static final String EXTRA_THUMBNAIL_BYTES = "THUMBNAIL BYTESL";
    public static final String EXTRA_FULLSIZE_URL = "FULLSIZE_URL";

    private ImageView mImageView;

    private String mFullsizeURL, mThumbnailURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        Intent intent = getIntent();
        mFullsizeURL = intent.getStringExtra(EXTRA_FULLSIZE_URL);
        mThumbnailURL = intent.getStringExtra(EXTRA_THUMBNAIL_URL);

        byte[] thumbnailBytes = intent.getByteArrayExtra(EXTRA_THUMBNAIL_BYTES);
        Bitmap thumbnailBitmap = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.length);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mImageView = (ImageView) findViewById(R.id.fullsize_image);
        mImageView.setImageBitmap(thumbnailBitmap);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mImageView == null) {
            mImageView = (ImageView) findViewById(R.id.fullsize_image);
        }

        downloadImage(mFullsizeURL);
    }

    @Override
    public void onTrimMemory(int level) {
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            mImageView = null;
            System.gc();
        }
        super.onTrimMemory(level);
    }

    public void downloadImage(String url) {
        ImageDownloadHandler handler = new ImageDownloadHandler();
        handler.execute(url);
    }

    public class ImageDownloadHandler extends AsyncTask<String, Void, byte[]> {

        OkHttpClient client = new OkHttpClient();

        @Override
        protected byte[] doInBackground(String... params) {

            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);

            Request request = builder.build();

            try {

                Response response = client.newCall(request).execute();
                return response.body().bytes();

            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            if (bytes != null && bytes.length > 0) {

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
                        bytes.length);
                mImageView.setImageBitmap(bitmap);
            }
            super.onPostExecute(bytes);
        }
    }
}
