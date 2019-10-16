package com.dingsoft.webrtc.webrtcroom.activity;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class FileUploadEvent {
    private int progress;
    private @State
    int state;

    public FileUploadEvent(@State int state) {
        this.state = state;
    }

    public FileUploadEvent(int progress, @State int state) {
        this.progress = progress;
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public static final int FILE_UPLOADING = 1;
    public static final int FILE_UPLOAD_FAILED = -1;
    public static final int FILE_UPLOAD_SUCCESS = 0;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FILE_UPLOAD_FAILED, FILE_UPLOADING, FILE_UPLOAD_SUCCESS})
    public @interface State {
    }
}
