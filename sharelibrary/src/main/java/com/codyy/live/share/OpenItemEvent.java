package com.codyy.live.share;

public class OpenItemEvent {
    private String fullPath;

    public OpenItemEvent(String fullPath) {
        this.fullPath = fullPath;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }
}
