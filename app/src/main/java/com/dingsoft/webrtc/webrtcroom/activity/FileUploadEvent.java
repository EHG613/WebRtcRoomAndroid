package com.dingsoft.webrtc.webrtcroom.activity;

public class FileUploadEvent {
    private int progress;

    public FileUploadEvent(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
