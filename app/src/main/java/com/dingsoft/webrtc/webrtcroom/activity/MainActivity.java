package com.dingsoft.webrtc.webrtcroom.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.constraint.Group;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.work.WorkInfo;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.codyy.devicelibrary.DeviceUtils;
import com.codyy.live.share.OpenItemEvent;
import com.codyy.live.share.ResPathEvent;
import com.codyy.live.share.ResResultEvent;
import com.codyy.live.share.ResSendFileEvent;
import com.codyy.live.webtrc.PeerConnectionParameters;
import com.codyy.live.webtrc.RecorderAction;
import com.codyy.live.webtrc.Role;
import com.codyy.live.webtrc.RtcListener;
import com.codyy.live.webtrc.WebRtcClient;
import com.codyy.live.webtrc.life.PortWorkLifecycle;
import com.dingsoft.webrtc.webrtcroom.R;
import com.fingdo.statelayout.StateLayout;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RtcListener, View.OnClickListener {
    //控件
    private EditText roomName, et1, et2, et3, et4, et5, et6;
    private Group mGroup;
    private Button openCamera, switchCamera, createRoom, exitRoom, shareDesktop, mMirror;
    private Button mFullScreen, mEsc, mClose, mPenOrMouse, mBtnRes;
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
    private CheckBox mCbMenuRecorder,mCbRecorderStartOrStop,mCbRecorderPauseOrResume;
    private CheckBox mCbMenuPhysical,mCbPhysicalStartOrStop,mCbPhysicalSwitch;

    //记录用户首次点击返回键的时间
    private long firstTime = 0;
    //摄像头是否开启
    private boolean isCameraOpen = false;
    private PortWorkLifecycle portWorkLifecycle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_main);
        initRecoder();
        initPhysical();
        mFullScreen = findViewById(R.id.btn_fullscreen);
        mBtnRes = findViewById(R.id.btn_res);
        mBtnRes.setOnClickListener(this);
        mGroup = findViewById(R.id.group);
        et1 = findViewById(R.id.et1);
        et2 = findViewById(R.id.et2);
        et3 = findViewById(R.id.et3);
        et4 = findViewById(R.id.et4);
        et5 = findViewById(R.id.et5);
        et6 = findViewById(R.id.et6);
        et1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) et2.requestFocus();
                getNumLength(et1, et2, et3, et4, et5, et6);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) et3.requestFocus();
                getNumLength(et1, et2, et3, et4, et5, et6);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) et4.requestFocus();
                getNumLength(et1, et2, et3, et4, et5, et6);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) et5.requestFocus();
                getNumLength(et1, et2, et3, et4, et5, et6);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) et6.requestFocus();
                getNumLength(et1, et2, et3, et4, et5, et6);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getNumLength(et1, et2, et3, et4, et5, et6);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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
        createRoom.setEnabled(false);
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

    /**
     * 初始化视频录制
     */
    private void initRecoder() {
        mCbMenuRecorder=findViewById(R.id.cb_menu_recorder);
        mCbMenuRecorder.setOnCheckedChangeListener((buttonView, isChecked) -> findViewById(R.id.ll_recorder).setVisibility(isChecked? View.VISIBLE:View.GONE));
        mCbRecorderStartOrStop=findViewById(R.id.cb_recorder_start_or_stop);
        mCbRecorderPauseOrResume=findViewById(R.id.cb_recorder_pause_or_resume);
        mCbRecorderStartOrStop.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ToastUtils.setGravity(Gravity.CENTER,0,0);
            ToastUtils.setBgColor(Color.parseColor("#273144"));
            ToastUtils.showShort(isChecked?"开始录制":"停止录制");
            mCbRecorderPauseOrResume.setVisibility(isChecked?View.VISIBLE:View.GONE);
            if(webRtcClient!=null)webRtcClient.recorder(isChecked? RecorderAction.START:RecorderAction.STOP);
        });
        mCbRecorderPauseOrResume.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ToastUtils.setGravity(Gravity.CENTER,0,0);
            ToastUtils.setBgColor(Color.parseColor("#273144"));
            ToastUtils.showShort(isChecked?"暂停录制":"恢复录制");
            if(webRtcClient!=null)webRtcClient.recorder(isChecked?RecorderAction.PAUSE:RecorderAction.RESUME);
        });
    }

    /**
     * 初始化实物展台
     */
    private void initPhysical() {
        mCbMenuPhysical=findViewById(R.id.cb_menu_physical);
        mCbMenuPhysical.setOnCheckedChangeListener((buttonView, isChecked) -> findViewById(R.id.ll_physical).setVisibility(isChecked? View.VISIBLE:View.GONE));
        mCbPhysicalStartOrStop=findViewById(R.id.cb_physical_start_or_stop);
        mCbPhysicalSwitch=findViewById(R.id.cb_switch);
        mCbPhysicalStartOrStop.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (webRtcClient != null) {
                    if(isChecked){
                        webRtcClient.startCapture();
                    }else{
                        webRtcClient.stopCapture();
                    }
                }
            mCbPhysicalSwitch.setVisibility(isChecked?View.VISIBLE:View.GONE);
        });
        mCbPhysicalSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(webRtcClient!=null)webRtcClient.switchCamera();
        });
    }

    private void getNumLength(EditText... editText) {
        int i = 0;
        for (EditText text : editText) {
            i += text.getText().length();
        }
        createRoom.setEnabled(i == 6);
    }

    private void initCall() {
        portWorkLifecycle = new PortWorkLifecycle(this);
        getLifecycle().addObserver(portWorkLifecycle);
        portWorkLifecycle.setListener(new PortWorkLifecycle.PortWorkListener() {
            @Override
            public void CallBack(WorkInfo status) {
                createWebRtcClient(getString(R.string.host_addr, status.getOutputData().getString("ip"), status.getOutputData().getString("port")));
                Log.e("info", getString(R.string.host_addr, status.getOutputData().getString("ip"), status.getOutputData().getString("port")));
//                openCamera();
                openCamera.performClick();
//                createRoom.performClick();
//                stateLayout.showContentView();
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
        EventBus.getDefault().unregister(this);
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
                KeyboardUtils.hideSoftInput(this);
                //创建并加入聊天室
                String roomId = et1.getText().toString() + et2.getText().toString() + et3.getText().toString() + et4.getText().toString() + et5.getText().toString() + et6.getText().toString();
                if (isCameraOpen) {
                    webRtcClient.createAndJoinRoom(roomId, Role.CTRL);
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
                if (webRtcClient != null) {
                    webRtcClient.onFullScreen();
                }
                break;
            case R.id.btn_esc:
                if (webRtcClient != null) {
                    mPenOrMouse.setText("鼠标");
                    webRtcClient.onPPTDrawScreen("mouse");
                    webRtcClient.onESCFullScreen();
                }
                break;
            case R.id.btn_close:
                if (webRtcClient != null) {
                    mPenOrMouse.setText("鼠标");
                    webRtcClient.onPPTDrawScreen("mouse");
                    webRtcClient.onClosePPTScreen();
                }
                break;
            case R.id.btn_pen_mouse:
                if ("鼠标".equals(mPenOrMouse.getText().toString())) {
                    mPenOrMouse.setText("画笔");
                    if (webRtcClient != null) webRtcClient.onPPTDrawScreen("pen");
                } else {
                    mPenOrMouse.setText("鼠标");
                    if (webRtcClient != null) webRtcClient.onPPTDrawScreen("mouse");
                }
                break;
            case R.id.btn_res:
                startActivity(new Intent(this, ResActivity.class));
                break;
            default:
                break;
        }
    }

    //创建配置参数
    private void createPeerConnectionParameters() {
        getCameraInfo();
        //获取webRtc 音视频配置参数
        Point displaySize = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(displaySize);
        Log.i("displaySize:", displaySize.x + "*" + displaySize.y);
        displaySize.set(640, 480);//设置画面的大小
        peerConnectionParameters = new PeerConnectionParameters(true, false,
                false, displaySize.x, displaySize.y, 20,
                0, "H264 High",
                true, false, 0, "OPUS",
                false, false, false, false, false, false,
                false, false, false, false);
    }

    public static List<Camera.Size> supportedVideoSizes;
    public static List<Camera.Size> previewSizes;

    public String getCameraInfo() {

        int cameracount = 0;//摄像头数量
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();  //获取摄像头信息
        cameracount = Camera.getNumberOfCameras();
        Log.i("CameraTest", "摄像头数量" + String.valueOf(cameracount));
        for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);
            Camera camera = Camera.open(cameraId); //开启摄像头获得一个Camera的实例
            Camera.Parameters params = camera.getParameters();  //通过getParameters获取参数
            supportedVideoSizes = params.getSupportedPictureSizes();
            previewSizes = params.getSupportedPreviewSizes();
            camera.release();//释放摄像头

            //重新排列后设下摄像头预设分辨率在所有分辨率列表中的地址，用以选择最佳分辨率（保证适配不出错）
            int index = bestVideoSize(previewSizes.get(0).width);
            Log.i("CameraTest", "预览分辨率地址: " + index + " " + supportedVideoSizes.get(index).width + "x" + supportedVideoSizes.get(index).height);
            if (null != previewSizes && previewSizes.size() > 0) {  //判断是否获取到值，否则会报空对象
                Log.i("CameraTest", "摄像头最高分辨率宽: " + String.valueOf(supportedVideoSizes.get(0).width));  //降序后取最高值，返回的是int类型
                Log.i("CameraTest", "摄像头最高分辨率高: " + String.valueOf(supportedVideoSizes.get(0).height));
                Log.i("CameraTest", "摄像头分辨率全部: " + cameraSizeToSting(supportedVideoSizes));
            } else {
                Log.i("CameraTest", "没取到值啊什么鬼");
                Log.i("CameraTest", "摄像头预览分辨率: " + String.valueOf(previewSizes.get(0).width));
            }
        }
        return cameraSizeToSting(supportedVideoSizes);
    }

    //重新排列获取到的分辨率列表
    public static int bestVideoSize(int _wid) {

        //降序排列
        Collections.sort(supportedVideoSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                if (lhs.width > rhs.width) {
                    return -1;
                } else if (lhs.width == rhs.width) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (int i = 0; i < supportedVideoSizes.size(); i++) {
            Log.e("supportedVideoSize", supportedVideoSizes.get(i).width + "<" + _wid);
            if (supportedVideoSizes.get(i).width < _wid) {
                return i;
            }
        }
        return 0;
    }

    //分辨率格式化输出String值
    public static String cameraSizeToSting(Iterable<Camera.Size> sizes) {
        StringBuilder s = new StringBuilder();
        for (Camera.Size size : sizes) {
            if (s.length() != 0)
                s.append(",\n");
            s.append(size.width).append('x').append(size.height);
        }
        return s.toString();
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
                int width = (Integer.parseInt(DeviceUtils.getScreenWidth(MainActivity.this)) - getResources().getDimensionPixelSize(R.dimen.right_margin) / 2);
                int height = width * 9 / 16;
                remoteView.setLongClickable(true);
                mGestureDetector = new GestureDetector(remoteVideoLl.getContext(), new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        Log.e("onSingleTapUp", e.getX() + ":" + e.getY());
//                        if("画笔".equals(mPenOrMouse.getText().toString())&&webRtcClient!=null){
//                            webRtcClient.onDrag("up",getRealX(e.getX(),width),getRealY(e.getY(),height));
//                        }
                        return super.onSingleTapUp(e);
                    }

                    @Override
                    public boolean onDown(MotionEvent e) {
                        Log.e("onDown", e.getX() + ":" + e.getY());
                        return super.onDown(e);
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        Log.e("onSingleTapConfirmed", e.getX() + ":" + e.getY());
                        if (webRtcClient != null) {
                            webRtcClient.onSingleTapConfirmed(getRealX(e.getX(), width), getRealY(e.getY(), height));
                        }
                        return super.onSingleTapConfirmed(e);
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        Log.e("onDoubleTap", e.getX() + ":" + e.getY());
                        if (webRtcClient != null) {
                            webRtcClient.onDoubleTap(getRealX(e.getX(), width), getRealY(e.getY(), height));
                        }
                        return super.onDoubleTap(e);
                    }

                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                        Log.e("onScroll", e1.getX() + ":" + e1.getY() + ";" + e2.getX() + ":" + e2.getY() + ";distance x=" + distanceX + ";distance y=" + distanceY);
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
                    if ("画笔".equals(mPenOrMouse.getText().toString()) && webRtcClient != null) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            webRtcClient.onDrag("down", getRealX(event.getX(), width), getRealY(event.getY(), height));
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            webRtcClient.onDrag("up", getRealX(event.getX(), width), getRealY(event.getY(), height));
                        }
                        webRtcClient.mouseMove(getRealX(event.getX(), width), getRealY(event.getY(), height));
                    }
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

    @Override
    public void onEmpty() {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "课堂号错误，请重试", Toast.LENGTH_LONG).show());
    }

    @Override
    public void onJoin() {
        runOnUiThread(() -> {
            createRoom.setEnabled(false);
            mGroup.setVisibility(View.GONE);
            stateLayout.showContentView();
        });
    }

    @Override
    public void onResResult(JSONObject jsonObject) {
        EventBus.getDefault().post(new ResResultEvent(jsonObject));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ResPathEvent event) {
        if (webRtcClient != null) {
            webRtcClient.getResPath(event.getPath());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(OpenItemEvent event) {
        if (webRtcClient != null) {
            webRtcClient.openResItem(event.getFullPath());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ResSendFileEvent event) {
        if (webRtcClient != null) {
            webRtcClient.sendFile(event.getFilePath());
        }
    }
}
