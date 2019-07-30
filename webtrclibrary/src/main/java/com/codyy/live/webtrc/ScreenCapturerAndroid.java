/*
 * 阔地教育科技有限公司版权所有(codyy.com/codyy.cn)
 * Copyright (c) 2019, Codyy and/or its affiliates. All rights reserved.
 */

package com.codyy.live.webtrc;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjection.Callback;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.view.Surface;

import org.webrtc.CapturerObserver;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.ThreadUtils;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;
import static android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

@TargetApi(21)
public class ScreenCapturerAndroid implements VideoCapturer, VideoSink {
    private static final int DISPLAY_FLAGS = VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private static final int VIRTUAL_DISPLAY_DPI = 1;
    private final Intent mediaProjectionPermissionResultData;
    private final Callback mediaProjectionCallback;
    private int width;
    private int height;
    private VirtualDisplay virtualDisplay;
    private SurfaceTextureHelper surfaceTextureHelper;
    private CapturerObserver capturerObserver;
    private long numCapturedFrames;
    private MediaProjection mediaProjection;
    private boolean isDisposed;
    private MediaProjectionManager mediaProjectionManager;

    public ScreenCapturerAndroid(Intent mediaProjectionPermissionResultData, Callback mediaProjectionCallback) {
        this.mediaProjectionPermissionResultData = mediaProjectionPermissionResultData;
        this.mediaProjectionCallback = mediaProjectionCallback;
    }

    private void checkNotDisposed() {
        if (this.isDisposed) {
            throw new RuntimeException("capturer is disposed.");
        }
    }

    public synchronized void initialize(SurfaceTextureHelper surfaceTextureHelper, Context applicationContext, CapturerObserver capturerObserver) {
        this.checkNotDisposed();
        if (capturerObserver == null) {
            throw new RuntimeException("capturerObserver not set.");
        } else {
            this.capturerObserver = capturerObserver;
            if (surfaceTextureHelper == null) {
                throw new RuntimeException("surfaceTextureHelper not set.");
            } else {
                this.surfaceTextureHelper = surfaceTextureHelper;
                this.mediaProjectionManager = (MediaProjectionManager) applicationContext.getSystemService(MEDIA_PROJECTION_SERVICE);
            }
        }
    }

    public synchronized void startCapture(int width, int height, int ignoredFramerate) {
        if (mediaProjection != null) return;
        this.checkNotDisposed();
        this.width = width;
        this.height = height;
        if (this.mediaProjectionManager != null && this.surfaceTextureHelper != null) {
            this.mediaProjection = this.mediaProjectionManager.getMediaProjection(-1, this.mediaProjectionPermissionResultData);
            if (this.mediaProjection != null)
                this.mediaProjection.registerCallback(this.mediaProjectionCallback, this.surfaceTextureHelper.getHandler());
        }
        this.createVirtualDisplay();
        if (this.capturerObserver != null)
            this.capturerObserver.onCapturerStarted(true);
        if (this.surfaceTextureHelper != null)
            this.surfaceTextureHelper.startListening(this);
    }

    public synchronized void stopCapture() {
        this.checkNotDisposed();
        if (this.surfaceTextureHelper == null || this.capturerObserver == null) return;
        ThreadUtils.invokeAtFrontUninterruptibly(this.surfaceTextureHelper.getHandler(), () -> {
            ScreenCapturerAndroid.this.surfaceTextureHelper.stopListening();
            ScreenCapturerAndroid.this.capturerObserver.onCapturerStopped();
            if (ScreenCapturerAndroid.this.virtualDisplay != null) {
                ScreenCapturerAndroid.this.virtualDisplay.release();
                ScreenCapturerAndroid.this.virtualDisplay = null;
            }

            if (ScreenCapturerAndroid.this.mediaProjection != null) {
                ScreenCapturerAndroid.this.mediaProjection.unregisterCallback(ScreenCapturerAndroid.this.mediaProjectionCallback);
                ScreenCapturerAndroid.this.mediaProjection.stop();
                ScreenCapturerAndroid.this.mediaProjection = null;
            }

        });
    }

    public synchronized void dispose() {
        this.isDisposed = true;
    }

    public synchronized void changeCaptureFormat(int width, int height, int ignoredFramerate) {
        this.checkNotDisposed();
        this.width = width;
        this.height = height;
        if (this.virtualDisplay != null && this.surfaceTextureHelper != null) {
            ThreadUtils.invokeAtFrontUninterruptibly(this.surfaceTextureHelper.getHandler(), () -> {
                ScreenCapturerAndroid.this.virtualDisplay.release();
                ScreenCapturerAndroid.this.createVirtualDisplay();
            });
        }
    }

    private void createVirtualDisplay() {
        if (this.surfaceTextureHelper != null && this.mediaProjection != null) {
            this.surfaceTextureHelper.setTextureSize(this.width, this.height);
            this.virtualDisplay = this.mediaProjection.createVirtualDisplay("WebRTC_ScreenCapture", this.width, this.height, VIRTUAL_DISPLAY_DPI, DISPLAY_FLAGS, new Surface(this.surfaceTextureHelper.getSurfaceTexture()), (android.hardware.display.VirtualDisplay.Callback) null, (Handler) null);
        }
    }

    public void onFrame(VideoFrame frame) {
        ++this.numCapturedFrames;
        if (this.capturerObserver != null) {
            this.capturerObserver.onFrameCaptured(frame);
        }
    }

    public boolean isScreencast() {
        return true;
    }

    public long getNumCapturedFrames() {
        return this.numCapturedFrames;
    }
}