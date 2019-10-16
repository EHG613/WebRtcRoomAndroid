package com.codyy.live.stu.openlive.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.codyy.live.stu.R;
import com.codyy.live.stu.StuLocalActivity;

import io.agora.rtc.Constants;

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

        if(role==Constants.CLIENT_ROLE_AUDIENCE) {
            Intent intent = new Intent(getIntent());
            intent.putExtra(com.codyy.live.stu.openlive.Constants.KEY_CLIENT_ROLE, role);
            intent.setClass(getApplicationContext(), LiveActivity.class);
            startActivity(intent);
        }else{
            Intent intent=new Intent(this,StuLocalActivity.class);
            startActivity(intent);
//            intent.setClass(getApplicationContext(), StuLocalActivity.class);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }

    public void onBackArrowPressed(View view) {
        finish();
    }
}
