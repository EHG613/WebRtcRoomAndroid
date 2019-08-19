package com.dingsoft.webrtc.webrtcroom.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.RadioGroup;

import com.dingsoft.webrtc.webrtcroom.R;

import org.greenrobot.eventbus.EventBus;

/**
 * created by lijian on 2019/08/19
 * 资源管理
 */
public class ResActivity extends AppCompatActivity {
    private RadioGroup mRG;
    private RecyclerView mRv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_res);
        mRG = findViewById(R.id.rg);
        mRv = findViewById(R.id.rv_content);
        setListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(this);
    }

    private void setListeners() {
        mRG.setOnCheckedChangeListener((group, checkedId) -> {
            onChecked(checkedId);
        });
    }

    private void onChecked(int checkedId) {
        switch (checkedId) {
            case R.id.rb_download:
                EventBus.getDefault().post(new ResPathEvent("download"));
                break;
            case R.id.rb_desktop:
                break;
            case R.id.rb_documents:
                break;
            case R.id.rb_my_device:
                break;
        }
    }

}
