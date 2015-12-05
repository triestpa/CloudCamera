package com.triestpa.cloudcamera.Upload;

import com.parse.ParseFile;

public class Upload {
    public final static int TYPE_PHOTO = 0;
    public final static int TYPE_VIDEO = 1;

    private ParseFile parseFile;
    private int progress;
    private boolean completed;
    private boolean aborted;
    private int type;

    public Upload(ParseFile parseFile, int type) {
        this.parseFile = parseFile;
        this.progress = 0;
        this.completed = false;
        this.aborted = false;
        this.type = type;
    }

    public ParseFile getParseFile() {
        return parseFile;
    }

    public void setParseFile(ParseFile parseFile) {
        this.parseFile = parseFile;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isAborted() {
        return aborted;
    }

    public void setAborted(boolean aborted) {
        this.aborted = aborted;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
