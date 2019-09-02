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
import org.webrtc.VideoSink;
import org.webrtc.VideoTrack;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
                        byte[] msg = "HelloWorld".getBytes();
                        DataChannel.Buffer buffer = new DataChannel.Buffer(
                                ByteBuffer.wrap(msg),
                                false);
                        Log.e("channel", dataChannel.send(buffer)+"");
                        Log.e("channel", dataChannel.state().name());
                        byte[] msg2 = "HelloWorldddddd".getBytes();
                        DataChannel.Buffer buffer2 = new DataChannel.Buffer(
                                ByteBuffer.wrap(msg2),
                                false);
                        Log.e("channel", dataChannel.send(buffer2)+"");
                        Log.e("channel", dataChannel.state().name());
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

    public void sendFile(String path) {
        Log.e("path",path);
        if (dataChannel != null) {
            Log.e("channel", dataChannel.state().name());
            byte[] msg = "HelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorld".getBytes();
            DataChannel.Buffer buffer = new DataChannel.Buffer(
                    ByteBuffer.wrap(msg),
                    false);
            Log.e("sendFile", dataChannel.send(buffer) + "");
            Log.e("sendFile", dataChannel.send(convertFileToByteArray(path)) + "");
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
