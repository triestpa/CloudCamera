package com.triestpa.cloudcamera.Utilities;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.triestpa.cloudcamera.CloudCameraApplication;
import com.triestpa.cloudcamera.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SystemUtilities {
    public final static int MEDIA_TYPE_IMAGE = 1;
    public final static int MEDIA_TYPE_VIDEO = 2;

    public static boolean isOnlineResult = false;
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        isOnlineResult = netInfo != null && netInfo.isConnectedOrConnecting();
        return isOnlineResult;
    }

    public static void lockOrientation(Activity activity) {
        int orientation = activity.getRequestedOrientation();
        int rotation = ((WindowManager) activity.getSystemService(
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
        activity.setRequestedOrientation(orientation);
    }

    public static void unlockOrientation(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    /*
    * Create a File for saving an image or video
    */
    public static File getOutputMediaFile(int type) {

        File mediaCacheDir = CloudCameraApplication.getAppContext().getCacheDir();

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaCacheDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaCacheDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }


    public static void reportError(String TAG, String message) {
        Log.e(TAG, message);
        Toast.makeText(CloudCameraApplication.getAppContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static void downloadFile(String url, String id, int mediatype) {
        Uri videoUri = Uri.parse(url);
        DownloadManager.Request r = new DownloadManager.Request(videoUri);

        // This put the download in the same Download dir the browser uses
        if (mediatype == MEDIA_TYPE_IMAGE) {
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, id + ".jpeg");
        } else {
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, id + ".mp4");
        }

        r.allowScanningByMediaScanner();

        // Notify user when download is completed
        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Start download
        Context context = CloudCameraApplication.getAppContext();
        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        dm.enqueue(r);
    }

    public static AlertDialog buildDialog(Activity activity, String message, DialogInterface.OnClickListener positiveButtonListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(message);

        // Add the buttons
        builder.setPositiveButton(R.string.dialog_ok,positiveButtonListener);
        builder.setNegativeButton(R.string.dialog_canel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.cancel();
            }
        });
        // Create the AlertDialog
        return builder.create();
    }


    public static void zoomView(final ImageView imageView, int fromDimen, int toDimen, Animation.AnimationListener listener) {
        ResizeAnimation animation = new ResizeAnimation(imageView, fromDimen, fromDimen, toDimen, toDimen);
        animation.setAnimationListener(listener);
        imageView.startAnimation(animation);
    }
}
