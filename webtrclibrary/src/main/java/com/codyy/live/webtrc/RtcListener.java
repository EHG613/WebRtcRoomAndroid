package com.codyy.live.webtrc;

import org.json.JSONObject;
import org.webrtc.VideoTrack;

/**
 * UI页面事件监听
 * Created by chengshaobo on 2018/10/26.
 */

public interface RtcListener {

    //远程音视频流加入 Peer通道
    void onAddRemoteStream(String peerId, VideoTrack videoTrack);

    //远程音视频流移除 Peer通道销毁
    void onRemoveRemoteStream(String peerId);

    void onMirror(boolean mirroring);

    /**
     * 课堂号不存在
     */
    void onEmpty();

    /**
     * 加入课堂成功
     */
    void onJoin();

    /**
     * 资源地址返回
     * @param jsonObject
     */
    void onResResult(JSONObject jsonObject);

    /**
     * 自动发现课堂号
     * @param room
     */
    void autoRoom(String room);
}
