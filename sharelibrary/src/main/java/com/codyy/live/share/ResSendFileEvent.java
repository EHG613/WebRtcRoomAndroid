package com.codyy.live.share;

public class ResSendFileEvent {
    private String filePath;

    public ResSendFileEvent(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
