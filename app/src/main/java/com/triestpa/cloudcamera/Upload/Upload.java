package com.triestpa.cloudcamera.Upload;

import com.parse.ParseFile;

public class Upload {
    private ParseFile parseFile;
    private int progress;
    private boolean completed;

    public Upload() {
        this.parseFile = null;
        this.progress = 0;
        this.completed = false;
    }

    public Upload(ParseFile parseFile) {
        this.parseFile = parseFile;
        this.progress = 0;
        this.completed = false;
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
}
