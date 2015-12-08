package com.triestpa.cloudcamera.Upload;


import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.triestpa.cloudcamera.Model.Video;

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

    public void uploadVideo() {
        final ParseFile vidFile = this.getParseFile();
        final ParseFile thumbnailFile = this.getThumbnailFile();
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

    private void saveVideo(ParseFile videoFile, ParseFile thumbnailFile) {
        Video newVid = new Video();
        newVid.setVideo(videoFile);
        newVid.setThumbnail(thumbnailFile);

        ParseUser thisUser = ParseUser.getCurrentUser();
        newVid.setParseUser(thisUser);
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
        super.retryUpload();
        uploadVideo();
    }
}
