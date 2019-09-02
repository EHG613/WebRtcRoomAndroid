package com.codyy.live.webtrc;

import android.content.Context;
import android.media.projection.MediaProjection;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Loggable;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;
import org.webrtc.voiceengine.WebRtcAudioManager;
import org.webrtc.voiceengine.WebRtcAudioRecord;
import org.webrtc.voiceengine.WebRtcAudioTrack;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.OkHttpClient;


/**
 * WebRtcClient类 封装PeerConnectionFactory工厂类及Socket.IO信令服务器
 * Created by chengshaobo on 2018/10/24.
 */

public class WebRtcClient {
    //Log Tag
    private final static String TAG = WebRtcClient.class.getCanonicalName();
    //PeerConnectionFactory工厂类
    private PeerConnectionFactory factory;
    //Peer集合
    private HashMap<String, Peer> peers = new HashMap<>();
    //IceServer集合 用于构建PeerConnection
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    //PeerConnectFactory构建参数
    private PeerConnectionParameters pcParams;
    //PeerConnect构建参数
    PeerConnection.RTCConfiguration rtcConfig;
    //PeerConnect 音频约束
    private MediaConstraints audioConstraints;
    //PeerConnect sdp约束
    private MediaConstraints sdpMediaConstraints;
    //本地Video视频资源
    private VideoSource localVideoSource;
    //视频Track
    private VideoTrack localVideoTrack;
    //音频Track
    private AudioTrack localAudioTrack;
    //本地摄像头视频捕获
    private VideoCapturer mVideoCapturer;
    //页面context
    private Context appContext;
    //WebRtc EglContext环境
    private EglBase eglBase;
    //Activity回调接口
    private RtcListener rtcListener;
    private SignalingListener signalingListener;
    //socket.io信令交互
    private Socket client;
    //信令服务器地址
    private String host;
    //本地socket id
    private String socketId;
    //room id
    private String roomId;
    private String role;

    ////webRtc定义常量////
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
    private static final String VIDEO_FLEXFEC_FIELDTRIAL =
            "WebRTC-FlexFEC-03-Advertised/Enabled/WebRTC-FlexFEC-03/Enabled/";
    private static final String VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL = "WebRTC-IntelVP8/Enabled/";
    private static final String DISABLE_WEBRTC_AGC_FIELDTRIAL =
            "WebRTC-Audio-MinimizeResamplingOnMobile/Enabled/";
    public static final int FONT_FACTING = 0;
    public static final int BACK_FACING = 1;

    //构造函数
    public WebRtcClient(Context appContext,
                        EglBase eglBase,
                        PeerConnectionParameters peerConnectionParameters,
                        RtcListener listener,
                        String host) {
        //参数设置
        this.appContext = appContext;
        this.eglBase = eglBase;
        this.pcParams = peerConnectionParameters;
        this.rtcListener = listener;
        this.host = host;
        //PeerConnectionFactory工厂类构建
        createPeerConnectionFactoryInternal();
        //创建iceservers
        createIceServers();
        //创建RTCConfiguration参数
        createRtcConfig();
        //创建Pc及Sdp约束
        createMediaConstraintsInternal();
        //Socket.IO信令服务器构建
        createSocket();
    }

    public RtcListener getRtcListener() {
        return rtcListener;
    }

    public String getSocketId() {
        return socketId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setSignalingListener(SignalingListener signalingListener) {
        this.signalingListener = signalingListener;
    }

    //创建IceServers参数
    private void createIceServers() {
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.xten.com").createIceServer());
    }

    //创建RTCConfiguration参数
    private void createRtcConfig() {
        rtcConfig =
                new PeerConnection.RTCConfiguration(iceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        // Enable DTLS for normal calls and disable for loopback calls.
        rtcConfig.enableDtlsSrtp = !pcParams.loopback;
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
    }


    //创建PeerConnection工厂类
    private void createPeerConnectionFactoryInternal() {
        //创建webRtc连接工厂类
        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;
        final boolean enableH264HighProfile =
                "H264 High".equals(pcParams.videoCodec);
        //音频模式
//        final AudioDeviceModule adm = pcParams.useLegacyAudioDevice
//                ? createLegacyAudioDevice()
//                : createJavaAudioDevice();
        final AudioDeviceModule adm = createJavaAudioDevice();
        //编解码模式【硬件加速，软编码】
        if (pcParams.videoCodecHwAcceleration) {
            encoderFactory = new DefaultVideoEncoderFactory(
                    eglBase.getEglBaseContext(), true /* enableIntelVp8Encoder */, enableH264HighProfile);
            decoderFactory = new DefaultVideoDecoderFactory(eglBase.getEglBaseContext());
        } else {
            encoderFactory = new SoftwareVideoEncoderFactory();
            decoderFactory = new SoftwareVideoDecoderFactory();
        }
        //PeerConnectionFactory.initialize
        String fieldTrials = "";
        if (pcParams.videoFlexfecEnabled) {
            fieldTrials += VIDEO_FLEXFEC_FIELDTRIAL;
        }
        fieldTrials += VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL;
        if (pcParams.disableWebRtcAGCAndHPF) {
            fieldTrials += DISABLE_WEBRTC_AGC_FIELDTRIAL;
        }
        //PeerConnectionFactory.initialize
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(appContext)
                        .setFieldTrials(fieldTrials)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions());
        //构建PeerConnectionFactory
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        factory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(adm)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
    }

    //创建信令服务器及监听
    private void createSocket() {
        //socket模式连接信令服务器
        try {
            //普通连接
            //client = IO.socket(host);

            //SSL加密连接
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .hostnameVerifier((hostname, session) -> true)
                    .sslSocketFactory(getSSLSocketFactory(), new TrustAllCerts())
                    .build();
            IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
            IO.setDefaultOkHttpCallFactory(okHttpClient);
            IO.Options opts = new IO.Options();
            opts.callFactory = okHttpClient;
            opts.webSocketFactory = okHttpClient;
            client = IO.socket(host, opts);
            client.on(Socket.EVENT_DISCONNECT, args -> {
                if (signalingListener != null) {
                    signalingListener.disconnect();
                }
            });
            ////设置消息监听
            //created [id,room,peers]
            client.on("created", createdListener);
            client.on("empty", emptyListener);
            client.on("res", resListener);
            //joined [id,room]
            client.on("joined", joinedListener);
            client.on("mirroring", mirroringListener);
            //offer [from,to,room,sdp]
            client.on("offer", offerListener);
            //answer [from,to,room,sdp]
            client.on("answer", answerListener);
            //candidate [from,to,room,candidate[sdpMid,sdpMLineIndex,sdp]]
            client.on("candidate", candidateListener);
            //exit [from,room]
            client.on("exit", exitListener);
            //开始连接
            client.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * UI操作相关
     */
    //创建并加入
    public void createAndJoinRoom(String roomId, String role) {
        this.role = role;
        //构建信令数据并发送
        try {
            JSONObject message = new JSONObject();
            message.put("room", roomId);
            message.put("role", role);
            //向信令服务器发送信令
            sendMessage("createAndJoinRoom", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //退出room
    public void exitRoom() {
        //信令服务器发送 exit [from room]
        try {
            JSONObject message = new JSONObject();
            message.put("from", socketId);
            message.put("room", roomId);
            //向信令服务器发送信令
            sendMessage("exit", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //循环遍历 peer关闭
        for (Peer pc : peers.values()) {
            pc.getPc().close();
        }
        //数据重置
        socketId = "";
        roomId = "";
        peers.clear();

    }

    /**
     * WebRtc相关
     */
    //构建webRtc连接并返回
    private Peer getOrCreateRtcConnect(String socketId) {
        Peer pc = peers.get(socketId);
        if (pc == null) {
            //构建RTCPeerConnection PeerConnection相关回调进入Peer中
            pc = new Peer(socketId, factory, rtcConfig, WebRtcClient.this);
            //设置本地数据流
            if (localVideoTrack != null)
                pc.getPc().addTrack(localVideoTrack);
            //保存peer连接
            peers.put(socketId, pc);
        }
        return pc;
    }

    public void mirror(List<String> ids) {
        if(ids==null||ids.size()==0)return;
//        createMirrorMessage("mirror");
        String to = null;
        JSONArray p2Mirrors = new JSONArray();
        for (Peer peer : peers.values()) {
            if (Role.PC.equals(peer.getRole())) {
                to = peer.getId();
                break;
            }
        }
        for (String id : ids) {
                p2Mirrors.put(id);
        }
        if (TextUtils.isEmpty(to) || p2Mirrors.length() == 0) return;
        //构建信令数据并发送
        try {
            JSONObject message = new JSONObject();
            message.put("room", roomId);
            message.put("from", socketId);
            message.put("to", to);
            message.put("peers", p2Mirrors);
            //向信令服务器发送信令
            sendMessage("mirror", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void unMirror() {
        createMirrorMessage("unmirror");
    }

    private void createMirrorMessage(String event) {
        String to = null;
        JSONArray p2Mirrors = new JSONArray();
        for (Peer peer : peers.values()) {
            if (Role.PC.equals(peer.getRole())) {
                to = peer.getId();
                break;
            }
        }
        for (Peer peer : peers.values()) {
            if (Role.CLIENT.equals(peer.getRole()) || peer.getRole() == null) {
                p2Mirrors.put(peer.getId());
            }
        }
        if (TextUtils.isEmpty(to) || p2Mirrors.length() == 0) return;
        //构建信令数据并发送
        try {
            JSONObject message = new JSONObject();
            message.put("room", roomId);
            message.put("from", socketId);
            message.put("to", to);
            message.put("peers", p2Mirrors);
            //向信令服务器发送信令
            sendMessage(event, message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public List<String> getMirrors(){
        List<String> mirrors=new ArrayList<>();
        for (Peer peer : peers.values()) {
            if (Role.CLIENT.equals(peer.getRole()) || peer.getRole() == null) {
                mirrors.add(peer.getId());
            }
        }
        return mirrors;
    }

    /**
     * 共享桌面
     */
    public void shareDesktop() {
        if (mVideoCapturer != null) {
            try {
                p2p("desktop", Role.PC);
                mVideoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭桌面共享
     */
    public void closeDesktop() {
        if (mVideoCapturer != null) {
            try {
                p2p("closedesktop", Role.PC);
                mVideoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopCapture() {
        if (mVideoCapturer != null) {
            try {
                p2p("endcall", Role.PC);
                mVideoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void p2p(String event, String role) {
        String to = null;
        for (Peer peer : peers.values()) {
            if (role.equals(peer.getRole())) {
                to = peer.getId();
                break;
            }
        }
        if (TextUtils.isEmpty(to)) return;
        //构建信令数据并发送
        try {
            JSONObject message = new JSONObject();
            message.put("room", roomId);
            message.put("from", socketId);
            message.put("to", to);
            //向信令服务器发送信令
            sendMessage(event, message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 移动鼠标事件
     *
     * @param x
     * @param y
     */
    public void mouseMove(float x, float y) {
        JSONObject content = new JSONObject();
        try {
            content.put("action", "move");
            content.put("x", x);
            content.put("y", y);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        p2pExt("action", Role.PC, content);
    }

    /**
     * 单击事件
     *
     * @param x
     * @param y
     */
    public void onSingleTapConfirmed(float x, float y) {
        JSONObject content = new JSONObject();
        try {
            content.put("action", "singleTap");
            content.put("x", x);
            content.put("y", y);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        p2pExt("action", Role.PC, content);
    }

    /**
     * 双击事件
     *
     * @param x
     * @param y
     */
    public void onDoubleTap(float x, float y) {
        JSONObject content = new JSONObject();
        try {
            content.put("action", "doubleTap");
            content.put("x", x);
            content.put("y", y);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        p2pExt("action", Role.PC, content);
    }

    /**
     * 全屏PPT
     */
    public void onFullScreen() {
        JSONObject content = new JSONObject();
        try {
            content.put("action", "fullscreen");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        p2pExt("action", Role.PC, content);
    }

    /**
     * esc全屏PPT
     */
    public void onESCFullScreen() {
        JSONObject content = new JSONObject();
        try {
            content.put("action", "esc");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        p2pExt("action", Role.PC, content);
    }

    /**
     * 关闭PPT
     */
    public void onClosePPTScreen() {
        JSONObject content = new JSONObject();
        try {
            content.put("action", "close");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        p2pExt("action", Role.PC, content);
    }

    /**
     * PPT画笔
     *
     * @param type pen or mouse
     */
    public void onPPTDrawScreen(String type) {
        JSONObject content = new JSONObject();
        try {
            content.put("action", "pptdraw");
            content.put("type", type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        p2pExt("action", Role.PC, content);
    }

    /**
     * 演示文稿 画笔和鼠标操作
     *
     * @param toggle down or up
     * @param x
     * @param y
     */
    public void onDrag(String toggle, float x, float y) {
        JSONObject content = new JSONObject();
        try {
            content.put("action", "drag");
            content.put("toggle", toggle);
            content.put("x", x);
            content.put("y", y);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        p2pExt("action", Role.PC, content);
    }

    /**
     * 请求资源列表
     *
     * @param path
     */
    public void getResPath(String path) {
        JSONObject content = new JSONObject();
        try {
            content.put("action", "getList");
            content.put("path", path);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        p2pExt("res", Role.PC, content);
    }

    /**
     * 打开文件
     *
     * @param path
     */
    public void openResItem(String path) {
        JSONObject content = new JSONObject();
        try {
            content.put("action", "openItem");
            content.put("path", path);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        p2pExt("res", Role.PC, content);
    }
    public void recorder(@RecorderAction String action) {
        JSONObject content = new JSONObject();
        try {
            content.put("action", action);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        p2pExt("recorder", Role.PC, content);
    }

    public void sendFile(String path) {
        getOrCreateRtcConnect(socketId).sendFile(path);
    }

    /*用户手势事件传递*/
    private void p2pExt(String event, String role, JSONObject content) {
        String to = null;
        for (Peer peer : peers.values()) {
            if (role.equals(peer.getRole())) {
                to = peer.getId();
                break;
            }
        }
        if (TextUtils.isEmpty(to)) return;
        //构建信令数据并发送
        try {
            JSONObject message = new JSONObject();
            message.put("room", roomId);
            message.put("from", socketId);
            message.put("to", to);
            message.put("content", content);
            //向信令服务器发送信令
            sendMessage(event, message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void startCapture() {
        if (mVideoCapturer != null) {
            mVideoCapturer.startCapture(pcParams.videoWidth, pcParams.videoHeight, pcParams.videoFps);
            p2p("call", "pc");
        }
    }

    public void startScreen() {
        if (mVideoCapturer != null) {
            mVideoCapturer.startCapture(pcParams.videoWidth, pcParams.videoHeight, pcParams.videoFps);
        }
    }

    public void stopScreen() {
        if (mVideoCapturer != null) {
            try {
                mVideoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //启动设备视频并关联本地video
    public void startCamera(VideoSink localRender, int type) {
        if (pcParams.videoCallEnabled) {
            //创建VideoCapturer
            if (mVideoCapturer == null) {
                if (!pcParams.screencaptureEnabled) {
                    String cameraname = "";

                    CameraEnumerator cameraEnumerator = Camera2Enumerator.isSupported(appContext) ? new Camera2Enumerator(appContext) : new Camera1Enumerator();
                    String[] deviceNames = cameraEnumerator.getDeviceNames();
                    if (type == FONT_FACTING) {
                        //前置摄像头
                        for (String deviceName : deviceNames) {
                            if (cameraEnumerator.isFrontFacing(deviceName)) {
                                cameraname = deviceName;
                            }
                        }
                    } else {
                        //后置摄像头
                        for (String deviceName : deviceNames) {
                            if (cameraEnumerator.isBackFacing(deviceName)) {
                                cameraname = deviceName;
                            }
                        }
                    }
                    mVideoCapturer = cameraEnumerator.createCapturer(cameraname, null);
                } else {
                    mVideoCapturer = new ScreenCapturerAndroid(pcParams.mediaProjectionPermissionResultData, new MediaProjection.Callback() {
                        @Override
                        public void onStop() {
                            super.onStop();
                        }
                    });
                }
                createVideoSource(localRender);
            } else {
                mVideoCapturer.startCapture(pcParams.videoWidth, pcParams.videoHeight, pcParams.videoFps);
            }
        }
    }

    private void createVideoSource(VideoSink localRender) {
        SurfaceTextureHelper surfaceTextureHelper =
                SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
        localVideoSource = factory.createVideoSource(pcParams.screencaptureEnabled);
        mVideoCapturer.initialize(surfaceTextureHelper, appContext, localVideoSource.getCapturerObserver());
//                mVideoCapturer.startCapture(pcParams.videoWidth, pcParams.videoHeight, pcParams.videoFps);
        localVideoTrack = factory.createVideoTrack("ARDAMSv0", localVideoSource);
        localVideoTrack.setEnabled(true);
        localVideoTrack.addSink(localRender);
    }

    //切换摄像头
    public void switchCamera() {
        if (mVideoCapturer != null && mVideoCapturer instanceof CameraVideoCapturer) {
            ((CameraVideoCapturer) mVideoCapturer).switchCamera(null);
        }
    }

    //关闭摄像头
    public void closeCamera() {
        if (mVideoCapturer != null) {
            try {
                mVideoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 信令服务器处理相关
     **/
    //created [id,room,peers]
    private Emitter.Listener createdListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "created:" + data);
            if (rtcListener != null) {
                rtcListener.onJoin();
            }
            try {
                //设置socket id
                socketId = data.getString("id");
                //设置room id
                roomId = data.getString("room");

                //获取peer数据
                JSONArray peers = data.getJSONArray("peers");
                //根据回应peers 循环创建WebRtcPeerConnection，创建成功后发送offer消息 [from,to,room,sdp]
                for (int i = 0; i < peers.length(); i++) {
                    JSONObject otherPeer = peers.getJSONObject(i);
                    String otherSocketId = otherPeer.getString("id");
                    //创建WebRtcPeerConnection
                    Peer pc = getOrCreateRtcConnect(otherSocketId);
                    pc.setRole(otherPeer.getString("role"));
                    //设置offer  如果是包含pc角色，则创建应答
                    if (!Role.CLIENT.equals(role)) //如果是学生端，则不主动建立rtc连接
                    {
                        if (Role.PC.equals(pc.getRole())) {
                            pc.getPc().createOffer(pc, sdpMediaConstraints);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //joined [id,room]
    private Emitter.Listener joinedListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "joined:" + data);
            try {
                //获取新加入socketId
                String fromId = data.getString("id");
                //构建pcconnection
                getOrCreateRtcConnect(fromId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    //joined [id,room]
    private Emitter.Listener emptyListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "joined:" + data);
            if (rtcListener != null) {
                rtcListener.onEmpty();
            }
        }
    };
    private Emitter.Listener resListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "res:" + data);
            if (rtcListener != null) {
                rtcListener.onResResult(data);
            }
        }
    };
    public static boolean isMirroring;
    private Emitter.Listener mirroringListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "mirroring:" + data);
            try {
                //获取新加入socketId
                isMirroring = data.getBoolean("mirroring");
                if (isMirroring) {
                    startScreen();
                } else {
                    stopScreen();
                }
                if (rtcListener != null) {
                    rtcListener.onMirror(isMirroring);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //offer [from,to,room,sdp]
    private Emitter.Listener offerListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "offer:" + data);
            try {
                //获取id
                String fromId = data.getString("from");
                //获取peer
                Peer pc = getOrCreateRtcConnect(fromId);
                //构建RTCSessionDescription参数
                SessionDescription sdp = new SessionDescription(
                        SessionDescription.Type.fromCanonicalForm("offer"),
                        data.getString("sdp")
                );
                //设置远端setRemoteDescription
                pc.getPc().setRemoteDescription(pc, sdp);
                //设置answer
                pc.getPc().createAnswer(pc, sdpMediaConstraints);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //answer [from,to,room,sdp]
    private Emitter.Listener answerListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "answer:" + data);
            try {
                //获取id
                String fromId = data.getString("from");
                //获取peer
                Peer pc = getOrCreateRtcConnect(fromId);
                //构建RTCSessionDescription参数
                SessionDescription sdp = new SessionDescription(
                        SessionDescription.Type.fromCanonicalForm("answer"),
                        data.getString("sdp")
                );
                //设置远端setRemoteDescription
                pc.getPc().setRemoteDescription(pc, sdp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //candidate [from,to,room,candidate[sdpMid,sdpMLineIndex,sdp]]
    private Emitter.Listener candidateListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "candidate:" + data);
            try {
                //获取id
                String fromId = data.getString("from");
                //获取peer
                Peer pc = getOrCreateRtcConnect(fromId);
                //获取candidate
                JSONObject candidate = data.getJSONObject("candidate");
                IceCandidate iceCandidate = new IceCandidate(
                        candidate.getString("sdpMid"), //描述协议id
                        candidate.getInt("sdpMLineIndex"),//描述协议的行索引
                        candidate.getString("sdp")//描述协议
                );

                //添加远端设备路由描述
                pc.getPc().addIceCandidate(iceCandidate);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //exit [from,room]
    private Emitter.Listener exitListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "exit:" + data);
            try {
                //获取id
                String fromId = data.getString("from");
                //判断是否为当前连接
                Peer pc = peers.get(fromId);
                if (pc != null) {
                    //peer关闭
                    getOrCreateRtcConnect(fromId).getPc().close();
                    //删除peer对象
                    peers.remove(fromId);
                    //通知UI界面移除video
                    rtcListener.onRemoveRemoteStream(fromId);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 信令服务器发送消息
     **/
    public void sendMessage(String event, JSONObject message) {
        client.emit(event, message);
    }

    /**
     * WebRtc 音视频相关辅助函数
     **/
    //创建Media及Sdp约束
    private void createMediaConstraintsInternal() {
        // 音频约束
        audioConstraints = new MediaConstraints();
        // added for audio performance measurements
        if (pcParams.noAudioProcessing) {
            Log.d(TAG, "Disabling audio processing");
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "false"));
        }

        //SDP约束 createOffer  createAnswer
        sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", "true"));
        sdpMediaConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        sdpMediaConstraints.optional.add(new MediaConstraints.KeyValuePair("RtpDataChannels", "false"));
    }

    //创建PeerConnection
    private void createPeerConnectionInternal() {

    }


    //创建音频模式LegacyAudioDevice
    private AudioDeviceModule createLegacyAudioDevice() {
        // Enable/disable OpenSL ES playback.
        if (!pcParams.useOpenSLES) {
            Log.d(TAG, "Disable OpenSL ES audio even if device supports it");
            WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(true /* enable */);
        } else {
            Log.d(TAG, "Allow OpenSL ES audio if device supports it");
            WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(false);
        }

        if (pcParams.disableBuiltInAEC) {
            Log.d(TAG, "Disable built-in AEC even if device supports it");
            WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);
        } else {
            Log.d(TAG, "Enable built-in AEC if device supports it");
            WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(false);
        }

        if (pcParams.disableBuiltInNS) {
            Log.d(TAG, "Disable built-in NS even if device supports it");
            WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(true);
        } else {
            Log.d(TAG, "Enable built-in NS if device supports it");
            WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(false);
        }

        //WebRtcAudioRecord.setOnAudioSamplesReady(saveRecordedAudioToFile);

        // Set audio record error callbacks.
        WebRtcAudioRecord.setErrorCallback(new WebRtcAudioRecord.WebRtcAudioRecordErrorCallback() {
            @Override
            public void onWebRtcAudioRecordInitError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordInitError: " + errorMessage);
            }

            @Override
            public void onWebRtcAudioRecordStartError(
                    WebRtcAudioRecord.AudioRecordStartErrorCode errorCode, String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordStartError: " + errorCode + ". " + errorMessage);
            }

            @Override
            public void onWebRtcAudioRecordError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordError: " + errorMessage);
            }
        });

        WebRtcAudioTrack.setErrorCallback(new WebRtcAudioTrack.ErrorCallback() {
            @Override
            public void onWebRtcAudioTrackInitError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackInitError: " + errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackStartError(
                    WebRtcAudioTrack.AudioTrackStartErrorCode errorCode, String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackStartError: " + errorCode + ". " + errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackError: " + errorMessage);
            }
        });

        return null;
    }

    //创建音频模式JavaAudioDevice
    private AudioDeviceModule createJavaAudioDevice() {
        // Enable/disable OpenSL ES playback.
        if (!pcParams.useOpenSLES) {
            Log.w(TAG, "External OpenSLES ADM not implemented yet.");
            // TODO(magjed): Add support for external OpenSLES ADM.
        }

        // Set audio record error callbacks.
        JavaAudioDeviceModule.AudioRecordErrorCallback audioRecordErrorCallback = new JavaAudioDeviceModule.AudioRecordErrorCallback() {
            @Override
            public void onWebRtcAudioRecordInitError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordInitError: " + errorMessage);
            }

            @Override
            public void onWebRtcAudioRecordStartError(
                    JavaAudioDeviceModule.AudioRecordStartErrorCode errorCode, String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordStartError: " + errorCode + ". " + errorMessage);
            }

            @Override
            public void onWebRtcAudioRecordError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordError: " + errorMessage);
            }
        };

        JavaAudioDeviceModule.AudioTrackErrorCallback audioTrackErrorCallback = new JavaAudioDeviceModule.AudioTrackErrorCallback() {
            @Override
            public void onWebRtcAudioTrackInitError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackInitError: " + errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackStartError(
                    JavaAudioDeviceModule.AudioTrackStartErrorCode errorCode, String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackStartError: " + errorCode + ". " + errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackError: " + errorMessage);
            }
        };

        return JavaAudioDeviceModule.builder(appContext)
                //.setSamplesReadyCallback(saveRecordedAudioToFile)
                .setUseHardwareAcousticEchoCanceler(!pcParams.disableBuiltInAEC)
                .setUseHardwareNoiseSuppressor(!pcParams.disableBuiltInNS)
                .setAudioRecordErrorCallback(audioRecordErrorCallback)
                .setAudioTrackErrorCallback(audioTrackErrorCallback)
                .createAudioDeviceModule();
    }

    //返回SSLSocketFactory 用于ssl连接
    private SSLSocketFactory getSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ssfFactory;
    }
}
