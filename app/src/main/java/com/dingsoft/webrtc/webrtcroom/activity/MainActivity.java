package com.dingsoft.webrtc.webrtcroom.activity;

import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.work.WorkInfo;

import com.codyy.devicelibrary.DeviceUtils;
import com.codyy.live.webtrc.PeerConnectionParameters;
import com.codyy.live.webtrc.Role;
import com.codyy.live.webtrc.RtcListener;
import com.codyy.live.webtrc.WebRtcClient;
import com.codyy.live.webtrc.life.PortWorkLifecycle;
import com.dingsoft.webrtc.webrtcroom.R;
import com.fingdo.statelayout.StateLayout;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RtcListener, View.OnClickListener {
    //控件
    private EditText roomName;
    private Button openCamera,switchCamera,createRoom,exitRoom,shareDesktop,mMirror;
    private Button mFullScreen,mEsc,mClose,mPenOrMouse;
    private StateLayout stateLayout;
    private SurfaceViewRenderer localSurfaceViewRenderer;
    private LinearLayout remoteVideoLl;
    private HashMap<String, View> remoteViews;
    //EglBase
    private EglBase rootEglBase;
    //WebRtcClient
    private WebRtcClient webRtcClient;
    //PeerConnectionParameters
    private PeerConnectionParameters peerConnectionParameters;


    //记录用户首次点击返回键的时间
    private long firstTime = 0;
    //摄像头是否开启
    private boolean isCameraOpen = false;
    private PortWorkLifecycle portWorkLifecycle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mFullScreen = findViewById(R.id.btn_fullscreen);
        mFullScreen.setOnClickListener(this);
        mPenOrMouse = findViewById(R.id.btn_pen_mouse);
        mPenOrMouse.setOnClickListener(this);
        mEsc = findViewById(R.id.btn_esc);
        mEsc.setOnClickListener(this);
        mClose = findViewById(R.id.btn_close);
        mClose.setOnClickListener(this);
        stateLayout = findViewById(R.id.state_layout);
        roomName = findViewById(R.id.room);
        openCamera = findViewById(R.id.openCamera);
        openCamera.setOnClickListener(this);
        switchCamera = findViewById(R.id.switchCamera);
        switchCamera.setOnClickListener(this);
        createRoom = findViewById(R.id.create);
        createRoom.setOnClickListener(this);
        exitRoom = findViewById(R.id.exit);
        exitRoom.setOnClickListener(this);
        shareDesktop = findViewById(R.id.desktop);
        shareDesktop.setOnClickListener(this);
        mMirror = findViewById(R.id.mirror);
        mMirror.setOnClickListener(this);
        findViewById(R.id.stopCapture).setOnClickListener(this);
        findViewById(R.id.startCapture).setOnClickListener(this);
        localSurfaceViewRenderer = findViewById(R.id.localVideo);
        remoteVideoLl = findViewById(R.id.remoteVideoLl);
        remoteViews = new HashMap<>();
        stateLayout.setUseAnimation(true);
        stateLayout.showLoadingView();
        stateLayout.setRefreshListener(new StateLayout.OnViewRefreshListener() {
            @Override
            public void refreshClick() {
                if (portWorkLifecycle == null) return;
                stateLayout.showLoadingView();
                portWorkLifecycle.refresh();
            }

            @Override
            public void loginClick() {

            }
        });
        initCall();
    }


    private void initCall() {
        portWorkLifecycle = new PortWorkLifecycle(this);
        getLifecycle().addObserver(portWorkLifecycle);
        portWorkLifecycle.setListener(new PortWorkLifecycle.PortWorkListener() {
            @Override
            public void CallBack(WorkInfo status) {
                createWebRtcClient(getString(R.string.host_addr, status.getOutputData().getString("ip"), status.getOutputData().getString("port")));
                Log.e("info", getString(R.string.host_addr, status.getOutputData().getString("ip"), status.getOutputData().getString("port")));
                openCamera.performClick();
                createRoom.performClick();
                stateLayout.showContentView();
            }

            @Override
            public void NetWorkError() {
                stateLayout.showNoNetworkView();
//                Toast.makeText(getApplicationContext(), "网络错误", Toast.LENGTH_LONG).show();
            }

            @Override
            public void NotFound() {
                stateLayout.showTimeoutView();
//                Toast.makeText(getApplicationContext(), "未搜索到PC客户端，请确认是否已打开PC客户端再进行操作", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //某些机型锁屏点亮后需要重新开启摄像头
        if (isCameraOpen) {
            webRtcClient.startCamera(localSurfaceViewRenderer, WebRtcClient.FONT_FACTING);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //数据销毁
        localSurfaceViewRenderer.release();
        localSurfaceViewRenderer = null;
        if (portWorkLifecycle != null) {
            getLifecycle().removeObserver(portWorkLifecycle);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 2000) {
                    Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;
                    return true;
                } else {
                    System.exit(0);
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openCamera:
                //开启_关闭摄像头
                if (isCameraOpen) {
                    //关闭
                    webRtcClient.closeCamera();
                    //数据
                    localSurfaceViewRenderer.clearImage();
                    localSurfaceViewRenderer.setBackground(new ColorDrawable(getResources().getColor(R.color.colorBlack)));
                    //localSurfaceViewRenderer.setForeground(new ColorDrawable(R.color.colorBlack));
                    localSurfaceViewRenderer.release();
                    isCameraOpen = false;
                    openCamera.setText("开启摄像头");
                } else {
                    //开启
                    openCamera();
                }
                break;
            case R.id.switchCamera:
                //切换摄像头
                switchCamera();
                break;
            case R.id.create:
                //创建并加入聊天室
                String roomId = roomName.getText().toString();
                if (isCameraOpen) {
                    webRtcClient.createAndJoinRoom(roomId, Role.CTRL);
                    createRoom.setEnabled(false);
                } else {
                    Toast.makeText(this, "请先开启摄像头", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.exit:
                //退出聊天室
                if (webRtcClient != null) {
                    webRtcClient.exitRoom();
                    //通知UI清空远端摄像头
                    clearRemoteCamera();
                    webRtcClient.closeCamera();
                    //数据
                    localSurfaceViewRenderer.clearImage();
                    localSurfaceViewRenderer.setBackground(new ColorDrawable(getResources().getColor(R.color.colorBlack)));
                    //localSurfaceViewRenderer.setForeground(new ColorDrawable(R.color.colorBlack));
                    localSurfaceViewRenderer.release();
                    isCameraOpen = false;
                    openCamera.setText("开启摄像头");
                }
                createRoom.setEnabled(true);
                break;
            case R.id.stopCapture:
                if (webRtcClient != null) {
                    webRtcClient.stopCapture();
                }
                break;
            case R.id.startCapture:
                if (webRtcClient != null) {
                    webRtcClient.startCapture();
                }
                break;
            case R.id.desktop:
                if (webRtcClient != null) {
                    if ("共享桌面".equals(shareDesktop.getText().toString())) {
                        shareDesktop.setText("结束共享");
                        webRtcClient.shareDesktop();
                    } else {
                        shareDesktop.setText("共享桌面");
                        webRtcClient.closeDesktop();
                    }
                }
                break;
            case R.id.mirror:
                if (webRtcClient != null) {
                    if ("镜像".equals(mMirror.getText().toString())) {
                        mMirror.setText("取消镜像");
                        webRtcClient.mirror();
                    } else {
                        mMirror.setText("镜像");
                        webRtcClient.unMirror();
                    }
                }
                break;
            case R.id.btn_fullscreen:
                if(webRtcClient!=null){
                    webRtcClient.onFullScreen();
                }
                break;
            case R.id.btn_esc:
                if(webRtcClient!=null){
                    webRtcClient.onESCFullScreen();
                }
                break;
            case R.id.btn_close:
                if(webRtcClient!=null){
                    webRtcClient.onClosePPTScreen();
                }
                break;
            case R.id.btn_pen_mouse:
                if("鼠标".equals(mPenOrMouse.getText().toString())){
                    mPenOrMouse.setText("画笔");
                    if(webRtcClient!=null)webRtcClient.onPPTDrawScreen("pen");
                }else{
                    mPenOrMouse.setText("鼠标");
                    if(webRtcClient!=null)webRtcClient.onPPTDrawScreen("mouse");
                }
                break;
            default:
                break;
        }
    }

    //创建配置参数
    private void createPeerConnectionParameters() {
        //获取webRtc 音视频配置参数
        Point displaySize = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(displaySize);
        displaySize.set(480, 320);//设置画面的大小
        peerConnectionParameters = new PeerConnectionParameters(true, false,
                false, displaySize.x, displaySize.y, 20,
                0, "H264 High",
                true, false, 0, "OPUS",
                false, false, false, false, false, false,
                false, false, false, false);
    }

    //创建webRtcClient
    private void createWebRtcClient(String host) {
        //配置参数
        createPeerConnectionParameters();
        //创建视频渲染器
        rootEglBase = EglBase.create();
        //WebRtcClient对象
        webRtcClient = new WebRtcClient(getApplicationContext(),
                rootEglBase,
                peerConnectionParameters,
                MainActivity.this,
                host);
        webRtcClient.setSignalingListener(() -> {
            runOnUiThread(() -> {
                exitRoom.performClick();
                stateLayout.showErrorView();
//                Toast.makeText(getApplicationContext(), "服务已断开", Toast.LENGTH_LONG).show();
            });
        });
    }

    //本地摄像头创建
    private void openCamera() {
        if (AndPermission.hasPermissions(this, Permission.Group.CAMERA)) {
            startCamera();
        } else {
            AndPermission.with(this)
                    .runtime()
                    .permission(Permission.Group.CAMERA)
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            //申请权限成功
                            startCamera();
                        }
                    })
                    .onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            //当用户没有允许该权限时，回调该方法
                            Toast.makeText(MainActivity.this, "没有获取照相机权限，该功能无法使用", Toast.LENGTH_SHORT).show();
                        }
                    }).start();
        }
    }

    //开启摄像头
    private void startCamera() {
        //初始化渲染源
        localSurfaceViewRenderer.init(rootEglBase.getEglBaseContext(), null);
        //填充模式
        localSurfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        localSurfaceViewRenderer.setZOrderMediaOverlay(true);
        localSurfaceViewRenderer.setEnableHardwareScaler(false);
        localSurfaceViewRenderer.setMirror(true);
        localSurfaceViewRenderer.setBackground(null);
        //启动摄像头
        webRtcClient.startCamera(localSurfaceViewRenderer, WebRtcClient.FONT_FACTING);
        //状态设置
        isCameraOpen = true;
        openCamera.setText("关闭摄像头");
    }

    //切换摄像头
    private void switchCamera() {
        if (webRtcClient != null) {
            webRtcClient.switchCamera();
        }
    }

    //清空远端摄像头
    public void clearRemoteCamera() {
        remoteVideoLl.removeAllViews();
    }

    private GestureDetector mGestureDetector;

    /* RtcListener 数据回调 */
    @Override
    public void onAddRemoteStream(String peerId, VideoTrack videoTrack) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ////UI线程执行
                //构建远端view
                SurfaceViewRenderer remoteView = new SurfaceViewRenderer(MainActivity.this);

                //初始化渲染源
                remoteView.init(rootEglBase.getEglBaseContext(), null);
                //填充模式
                remoteView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
                remoteView.setZOrderMediaOverlay(true);
                remoteView.setEnableHardwareScaler(false);
                remoteView.setMirror(false);
                //控件布局
                int width = (Integer.parseInt(DeviceUtils.getScreenWidth(MainActivity.this)) - getResources().getDimensionPixelSize(R.dimen.right_margin));
                int height = width * 9 / 16;
                remoteView.setLongClickable(true);
                mGestureDetector = new GestureDetector(remoteVideoLl.getContext(), new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        Log.e("onSingleTapUp",e.getX()+":"+e.getY());
                        return super.onSingleTapUp(e);
                    }

                    @Override
                    public boolean onDown(MotionEvent e) {
                        Log.e("onDown",e.getX()+":"+e.getY());
                        return super.onDown(e);
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        Log.e("onSingleTapConfirmed", e.getX() + ":" + e.getY());
                        if(webRtcClient!=null){
                            webRtcClient.onSingleTapConfirmed(getRealX(e.getX(),width),getRealY(e.getY(),height));
                        }
                        return super.onSingleTapConfirmed(e);
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        Log.e("onDoubleTap", e.getX() + ":" + e.getY());
                        if(webRtcClient!=null){
                            webRtcClient.onDoubleTap(getRealX(e.getX(),width),getRealY(e.getY(),height));
                        }
                        return super.onDoubleTap(e);
                    }

                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                        Log.e("onScroll", e1.getX() + ":" + e1.getY() + ";" + e2.getX() + ":" + e2.getY()+";distance x="+distanceX+";distance y="+distanceY);
//                        if (webRtcClient != null) {
//                            webRtcClient.mouseMove(getRealX(e2.getX(), width), getRealY(e2.getY(), height));
//                        }
                        return super.onScroll(e1, e2, distanceX, distanceY);
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        super.onLongPress(e);
                        Log.e("onLongPress", e.getX() + ":" + e.getY());
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                        Log.e("onFling", e1.getX() + ":" + e1.getY() + ";" + e2.getX() + ":" + e2.getY());
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }
                });
                remoteView.setOnTouchListener((v, event) -> {
//                    Log.e("onTouch",event.getX()+":"+event.getY());
//                    if (webRtcClient != null) {
//                        webRtcClient.mouseMove(getRealX(event.getX(), width), getRealY(event.getY(), height));
//                    }
                    return mGestureDetector.onTouchEvent(event);
                });
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
//                layoutParams.topMargin = 20;
//                layoutParams.rightMargin=120;
                remoteVideoLl.addView(remoteView, layoutParams);
                //添加至hashmap中
                remoteViews.put(peerId, remoteView);
                //添加数据
                //VideoTrack videoTrack = mediaStream.videoTracks.get(0);
                videoTrack.addSink(remoteView);
            }
        });
    }

    private float getRealX(float currentX, int currentWidth) {
        float x = currentX * 1920 / currentWidth;
        return x > 1920 ? 1920 : x;
    }

    private float getRealY(float currentY, int currentHeight) {
        float y = currentY * 1080 / currentHeight;
        return y > 1080 ? 1080 : y;
    }

    @Override
    public void onRemoveRemoteStream(String peerId) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ////UI线程执行
                //移除远端view
                SurfaceViewRenderer remoteView = (SurfaceViewRenderer) remoteViews.get(peerId);
                if (remoteView != null) {
                    remoteVideoLl.removeView(remoteView);
                    remoteViews.remove(peerId);
                    //数据销毁
                    remoteView.release();
                    remoteView = null;
                }
            }
        });
    }

    @Override
    public void onMirror(boolean mirroring) {

    }
}
