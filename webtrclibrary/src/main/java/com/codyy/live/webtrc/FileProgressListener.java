package com.codyy.live.webtrc;

public interface FileProgressListener {
    void progress(int progress);
    void failed(String error);
    void success(String path);
}
