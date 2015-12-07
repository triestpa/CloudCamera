package com.triestpa.cloudcamera.Upload;


import com.parse.ParseFile;

public class VideoUpload extends Upload {
    private ParseFile thumbnailFile;

    public VideoUpload(ParseFile videoFile, ParseFile thumbnailFile) {
        super(videoFile);
        this.thumbnailFile = thumbnailFile;
    }


    public ParseFile getThumbnailFile() {
        return thumbnailFile;
    }

    public void setThumbnailFile(ParseFile thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
    }



}
