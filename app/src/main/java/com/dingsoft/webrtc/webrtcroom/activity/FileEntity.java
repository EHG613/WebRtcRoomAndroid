package com.dingsoft.webrtc.webrtcroom.activity;

public class FileEntity {

    /**
     * name : dock.png
     * size : 5848
     * mime : png
     * mtime : 1979-12-31 0:00:00
     * path : /Users/lijian/Documents/桌面/desktopcapture/src/dock.png
     */

    private String name;
    private int size;
    private String mime;
    private String mtime;
    private String path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public String getMtime() {
        return mtime;
    }

    public void setMtime(String mtime) {
        this.mtime = mtime;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
