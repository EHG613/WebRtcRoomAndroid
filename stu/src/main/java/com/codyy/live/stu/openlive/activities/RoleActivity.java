package com.codyy.live.stu.openlive.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.codyy.live.stu.R;
import com.codyy.live.stu.StuLocalActivity;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.util.List;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;

public class RoleActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        RelativeLayout layout = findViewById(R.id.role_title_layout);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) layout.getLayoutParams();
        params.height += mStatusBarHeight;
        layout.setLayoutParams(params);

        layout = findViewById(R.id.role_content_layout);
        params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
        params.topMargin = (mDisplayMetrics.heightPixels -
                layout.getMeasuredHeight()) * 3 / 7;
        layout.setLayoutParams(params);
    }

    public void onJoinAsBroadcaster(View view) {
        gotoLiveActivity(Constants.CLIENT_ROLE_BROADCASTER);
    }

    public void onJoinAsAudience(View view) {
        gotoLiveActivity(Constants.CLIENT_ROLE_AUDIENCE);
    }

    private void gotoLiveActivity(int role) {

        permissionCheck(role);

    }

    private void start(int role) {
        if (role == Constants.CLIENT_ROLE_AUDIENCE) {
            Intent intent = new Intent(this, LiveActivity.class);
//            Intent intent = new Intent(getIntent());
            intent.putExtra(com.codyy.live.stu.openlive.Constants.KEY_CLIENT_ROLE, role);
//            intent.setClass(getApplicationContext(), LiveActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, StuLocalActivity.class);
            startActivity(intent);
//            intent.setClass(getApplicationContext(), StuLocalActivity.class);
        }
    }

    private void permissionCheck(int role) {
        if (AndPermission.hasPermissions(this, Permission.Group.CAMERA, Permission.Group.MICROPHONE, Permission.Group.STORAGE)) {
            start(role);
        } else {
            AndPermission.with(this)
                    .runtime()
                    .permission(Permission.Group.CAMERA, Permission.Group.MICROPHONE, Permission.Group.STORAGE)
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            //申请权限成功
                            start(role);
                        }
                    })
                    .onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            //当用户没有允许该权限时，回调该方法
                            Toast.makeText(RoleActivity.this, "权限获取失败，该功能无法使用", Toast.LENGTH_SHORT).show();
                        }
                    }).start();
        }
    }

    public void onBackArrowPressed(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RtcEngine.destroy();
        System.exit(0);
    }
}
