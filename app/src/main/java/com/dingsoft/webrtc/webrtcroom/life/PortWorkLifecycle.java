package com.dingsoft.webrtc.webrtcroom.life;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.v4.app.FragmentActivity;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.dingsoft.webrtc.webrtcroom.work.PortWorker;

public class PortWorkLifecycle implements LifecycleObserver {
    private FragmentActivity fragmentActivity;
    private PortWorkListener listener;
    private int retryCount = 10;

    public void setListener(PortWorkListener listener) {
        this.listener = listener;
    }

    public interface PortWorkListener {
        /**
         * 搜索成功
         *
         * @param info ip and port
         */
        void CallBack(WorkInfo info);

        /**
         * 网络异常
         */
        void NetWorkError();

        /**
         * 未搜索到PC客户端，请确认是否已打开PC客户端再进行操作
         */
        void NotFound();
    }

    public PortWorkLifecycle(FragmentActivity fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
        startPortWorker(sPort);
    }
    public PortWorkLifecycle(FragmentActivity fragmentActivity,int retry) {
        this.fragmentActivity = fragmentActivity;
        this.retryCount=retry;
        startPortWorker(sPort);
    }

    private static final int sDefaultPort = 52807;
    /**
     * 默认广播端口为52807，如果后端端口被占用，会启动自增模式，直到找到未被占用的端口号为止;
     * PortWork用来获取信令服务器的IP及端口号
     */
    private static int sPort = sDefaultPort;

    /**
     * 重新尝试
     */
    public void refresh() {
        sPort = sDefaultPort;
        startPortWorker(sPort);
    }

    private void startPortWorker(int port) {
        // Create the Data object:
        Data myData = new Data.Builder()
                .putInt("port", port)
                .build();
        OneTimeWorkRequest.Builder argsWorkBuilder =
                new OneTimeWorkRequest.Builder(PortWorker.class)
                        .setInputData(myData);

        OneTimeWorkRequest mathWork = argsWorkBuilder.build();
        WorkManager.getInstance().getWorkInfoByIdLiveData(mathWork.getId())
                .observe(fragmentActivity, status -> {
                    if (status == null) return;
                    if (status.getState() == WorkInfo.State.FAILED) {
                        if ("SocketTimeoutException".equals(status.getOutputData().getString("exception"))) {
                            if (sPort - sDefaultPort < retryCount) {
                                startPortWorker(++sPort);
                            } else {
                                if (listener != null) {
                                    listener.NotFound();
                                }
                            }
                        } else {
                            if (listener != null) {
                                listener.NetWorkError();
                            }
                        }
                    } else if (status.getState() == WorkInfo.State.SUCCEEDED) {
                        if (listener != null) listener.CallBack(status);
                    }
                });

        WorkManager.getInstance().enqueue(mathWork);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        WorkManager.getInstance().cancelAllWork();
        fragmentActivity = null;
    }
}
