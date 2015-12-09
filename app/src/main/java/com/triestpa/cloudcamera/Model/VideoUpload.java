package com.triestpa.cloudcamera.Model;


import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.triestpa.cloudcamera.CloudCameraApplication;
import com.triestpa.cloudcamera.Utilities.SystemUtilities;

/**
 * Extension of upload class for video files
 */
public class VideoUpload extends Upload {
    final static String TAG = VideoUpload.class.getName();
    private ParseFile thumbnailFile;

    public VideoUpload(ParseFile videoFile, ParseFile thumbnailFile) {
        super(videoFile);
        this.thumbnailFile = thumbnailFile;
    }

    private ParseFile getThumbnailFile() {
        return thumbnailFile;
    }

    private void setThumbnailFile(ParseFile thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
    }

    // Upload the video stored in this instance
    public void uploadVideo() {
        if (!SystemUtilities.isOnline(CloudCameraApplication.getAppContext())) {
            SystemUtilities.reportError(TAG, "Cannot upload photo without internet connection. Visit upload screen to retry.");
            this.setAborted(true);
            return;
        }

        final ParseFile vidFile = this.getParseFile();
        final ParseFile thumbnailFile = this.getThumbnailFile();

        // First save the video ParseFile
        vidFile.saveInBackground(new SaveCallback() {
                                     @Override
                                     public void done(ParseException e) {
                                         if (e == null) {
                                             saveThumbnail(vidFile, thumbnailFile);
                                         } else {
                                             VideoUpload.this.showError(e, "Video Upload Failure: ");
                                         }
                                     }
                                 }, new ProgressCallback() {
                                     @Override
                                     public void done(Integer percentDone) {
                                         VideoUpload.this.setProgress(percentDone);
                                     }
                                 }
        );
    }

    // Upload the thumbnail ParseFile
    private void saveThumbnail(final ParseFile videoFile, final ParseFile thumbnailFile) {
        thumbnailFile.saveInBackground(new SaveCallback() {
                                           @Override
                                           public void done(ParseException e) {
                                               if (e == null) {
                                                   saveVideo(videoFile, thumbnailFile);
                                               } else {
                                                   VideoUpload.this.showError(e, "Video Upload Failure: ");
                                               }
                                           }

                                       }
        );
    }

    // Save the Video ParseObject
    private void saveVideo(ParseFile videoFile, ParseFile thumbnailFile) {
        Video newVid = new Video();
        newVid.setVideo(videoFile);
        newVid.setThumbnail(thumbnailFile);

        // Associate Video with the current user
        ParseUser thisUser = ParseUser.getCurrentUser();
        newVid.setParseUser(thisUser);

        // Set permissions so that only current user can access
        newVid.setACL(new ParseACL(thisUser));

        newVid.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    VideoUpload.this.showSuccess("Video Upload Success");
                    VideoUpload.this.setParseFile(null);
                    VideoUpload.this.setThumbnailFile(null);
                }
                else {
                    VideoUpload.this.showError(e, "Video Upload Failure: ");
                }
            }
        });
    }

    @Override
    public void retryUpload() {
        uploadVideo();
    }
}
