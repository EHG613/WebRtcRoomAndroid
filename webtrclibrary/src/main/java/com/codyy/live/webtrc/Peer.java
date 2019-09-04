package com.codyy.live.webtrc;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoTrack;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * PeerConnection通道封装，包括PeerConnection创建及状态回调
 * Created by chengshaobo on 2018/10/23.
 */

public class Peer implements SdpObserver, PeerConnection.Observer {
    //PeerConnection对象
    private PeerConnection pc;
    private DataChannel dataChannel;
    //PeerConnection标识
    private String id;
    //用户角色
    private String role;
    //webRtClient对象
    private WebRtcClient webRtcClient;

    //日志Tag
    private final static String TAG = Peer.class.getCanonicalName();

    //构造函数
    public Peer(String id,
                PeerConnectionFactory factory,
                PeerConnection.RTCConfiguration rtcConfig,
                WebRtcClient webRtcClient) {
        Log.d(TAG, "new Peer: " + id);
        this.pc = factory.createPeerConnection(rtcConfig, this);
        this.id = id;
        this.webRtcClient = webRtcClient;
//        if(this.webRtcClient.getSocketId().equals(this.id)) {
//            initDataChannel();
//        }
    }

    void initDataChannel() {
    /*
DataChannel.Init 可配参数说明：
ordered：是否保证顺序传输；
maxRetransmitTimeMs：重传允许的最长时间；
maxRetransmits：重传允许的最大次数；
*/
        DataChannel.Init init = new DataChannel.Init();
        dataChannel = getPc().createDataChannel("dataChannel", init);
        dataChannel.registerObserver(new DataChannel.Observer() {
            @Override
            public void onBufferedAmountChange(long l) {

            }

            @Override
            public void onStateChange() {
                switch (dataChannel.state()) {
                    case OPEN:
                        Log.e("channel", "OPEN");
//                        byte[] msg = "HelloWorld".getBytes();
//                        DataChannel.Buffer buffer = new DataChannel.Buffer(
//                                ByteBuffer.wrap(msg),
//                                false);
//                        Log.e("channel", dataChannel.send(buffer) + "");
//                        Log.e("channel", dataChannel.state().name());
//                        byte[] msg2 = "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".getBytes();
//                        DataChannel.Buffer buffer2 = new DataChannel.Buffer(
//                                ByteBuffer.wrap(msg2),
//                                false);
//                        Log.e("channel", dataChannel.send(buffer2) + "");
//                        Log.e("channel", dataChannel.state().name());
//                        sendFile("/storage/emulated/0/01bf1655e514b16ac7251df840273f.jpg");
                        break;
                    case CLOSED:
                        Log.e("channel", "CLOSED");
                        break;
                    case CLOSING:
                        Log.e("channel", "CLOSING");
                        break;
                    case CONNECTING:
                        Log.e("channel", "CONNECTING");
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onMessage(DataChannel.Buffer buffer) {
                Log.e("onMessage", buffer.toString());
            }
        });
    }

    public void sendFile(String path, FileProgressListener fileProgressListener) {
        Log.e("path", path);
        if (dataChannel != null) {
            new Thread(new SendFileThread(path,fileProgressListener)).start();
//            new SendFileThread(path).run();
        }
    }

    class SendFileThread implements Runnable {
        String path;
        FileProgressListener fileProgressListener;
        public SendFileThread(String path, FileProgressListener fileProgressListener) {
            this.path = path;
            this.fileProgressListener=fileProgressListener;
        }

        @Override
        public void run() {
            Log.e("channel", dataChannel.state().name());
                File2Base64.encodeBase64File(dataChannel, path,fileProgressListener);
        }
    }

    private DataChannel.Buffer convertFileToByteArray(String path) {
        DataChannel.Buffer buffer = null;
        File file;
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            file = new File(path);
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int len = -1;
            while ((len = fis.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            buffer = new DataChannel.Buffer(
                    ByteBuffer.wrap(bos.toByteArray()),
                    false);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) fis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return buffer;
    }

    public PeerConnection getPc() {
        return pc;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setPc(PeerConnection pc) {
        this.pc = pc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * SdpObserver是来回调sdp是否创建(offer,answer)成功，是否设置描述成功(local,remote）的接口
     **/

    //Create{Offer,Answer}成功回调
    @Override
    public void onCreateSuccess(SessionDescription sdp) {
        String type = sdp.type.canonicalForm();
        Log.d(TAG, "onCreateSuccess " + type);
        //设置本地LocalDescription
        pc.setLocalDescription(Peer.this, sdp);
        //构建信令数据
        try {
            JSONObject message = new JSONObject();
            message.put("from", webRtcClient.getSocketId());
            message.put("to", id);
            message.put("room", webRtcClient.getRoomId());
            message.put("sdp", sdp.description);
            //向信令服务器发送信令
            webRtcClient.sendMessage(type, message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Set{Local,Remote}Description()成功回调
    @Override
    public void onSetSuccess() {

    }

    //Create{Offer,Answer}失败回调
    @Override
    public void onCreateFailure(String s) {

    }

    //Set{Local,Remote}Description()失败回调
    @Override
    public void onSetFailure(String s) {

    }

    /**
     * SdpObserver是来回调sdp是否创建(offer,answer)成功，是否设置描述成功(local,remote）的接口
     **/
    //信令状态改变时候触发
    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {

    }

    //IceConnectionState连接状态改变时候触发
    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d(TAG, "onIceConnectionChange " + iceConnectionState);
        if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
            /** ice连接中断处理 **/
        }
    }

    //IceConnectionState连接接收状态改变
    @Override
    public void onIceConnectionReceivingChange(boolean b) {

    }

    //IceConnectionState网络信息获取状态改变
    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

    }

    //新ice地址被找到触发
    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Log.d(TAG, "onIceCandidate " + iceCandidate.sdpMid);
        try {
            //构建信令数据
            JSONObject message = new JSONObject();
            message.put("from", webRtcClient.getSocketId());
            message.put("to", id);
            message.put("room", webRtcClient.getRoomId());
            //candidate参数
            JSONObject candidate = new JSONObject();
            candidate.put("sdpMid", iceCandidate.sdpMid);
            candidate.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
            candidate.put("sdp", iceCandidate.sdp);
            message.put("candidate", candidate);
            //向信令服务器发送信令
            webRtcClient.sendMessage("candidate", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //ice地址被移除掉触发
    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

    }

    //Peer连接远端音视频数据到达时触发 注：用onTrack回调代替
    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d(TAG, "onAddStream " + mediaStream.getId());
    }

    //Peer连接远端音视频数据移除时触发
    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(TAG, "onRemoveStream " + mediaStream.getId());
        //移除Peer连接 & 通知监听远端音视频数据到达
    }

    //Peer连接远端开启数据传输通道时触发
    @Override
    public void onDataChannel(DataChannel dataChannel) {

    }

    //通道交互协议需要重新协商时触发
    @Override
    public void onRenegotiationNeeded() {

    }

    //Triggered when a new track is signaled by the remote peer, as a result of setRemoteDescription.
    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

    }

    @Override
    public void onTrack(RtpTransceiver transceiver) {
        MediaStreamTrack track = transceiver.getReceiver().track();
        Log.d(TAG, "onTrack " + track.id());
        if (track instanceof VideoTrack) {
            webRtcClient.getRtcListener().onAddRemoteStream(id, (VideoTrack) track);
        }
    }
}
