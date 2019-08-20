package com.dingsoft.webrtc.webrtcroom.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.codyy.live.share.MimeDrawableUtil;
import com.codyy.live.share.OpenItemEvent;
import com.codyy.live.share.ResPath;
import com.codyy.live.share.ResPathEvent;
import com.codyy.live.share.ResResultEvent;
import com.codyy.live.share.ResType;
import com.dingsoft.webrtc.webrtcroom.R;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * created by lijian on 2019/08/19
 * 资源管理
 */
public class ResActivity extends AppCompatActivity {
    private RadioGroup mRG;
    private RecyclerView mRv;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_res);
        mRG = findViewById(R.id.rg);
        mRv = findViewById(R.id.rv_content);
        setListeners();
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        mRv.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(new JSONArray());
        mRv.setAdapter(mAdapter);
        mRG.getChildAt(1).performClick();
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private JSONArray mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            TextView textView, tvSize;
            ImageView imageView;

            MyViewHolder(View v) {
                super(v);
                textView = v.findViewById(R.id.tv_res_name);
                tvSize = v.findViewById(R.id.tv_size);
                imageView = v.findViewById(R.id.iv_mime);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(JSONArray myDataset) {
            mDataset = myDataset;
        }

        public void setData(JSONArray mDataset) {
            this.mDataset = mDataset;
            this.notifyDataSetChanged();
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_res_path, parent, false);
            MyViewHolder vh = new MyViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            String fileName = mDataset.optJSONObject(position).optString("name");
            String mime = mDataset.optJSONObject(position).optString("mime");
            String path = mDataset.optJSONObject(position).optString("path");
            long size = mDataset.optJSONObject(position).optLong("size");
            holder.textView.setText(fileName);
            if (ResType.parentDir.equals(mime) || ResType.dir.equals(mime)) {
                holder.textView.setOnClickListener(v -> EventBus.getDefault().post(new ResPathEvent(path)));
            } else if (MimeDrawableUtil.isSupportedMimeToOpen(mime)) {
                holder.textView.setOnClickListener(v -> EventBus.getDefault().post(new OpenItemEvent(path)));
            } else {
                holder.textView.setOnClickListener(null);
            }
            if (!ResType.parentDir.equals(mime)) {
                holder.tvSize.setText(byte2FitMemorySize(size));
            } else {
                holder.tvSize.setText(null);
            }
            MimeDrawableUtil.setMimeDrawable(holder.imageView, mime);
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.length();
        }
    }

    private static String byte2FitMemorySize(final long byteNum) {
        if (byteNum < 0) {
            return "shouldn't be less than zero!";
        } else if (byteNum < 1024) {
            return String.format(Locale.getDefault(), "%dB", byteNum);
        } else if (byteNum < 1048576) {
            return String.format(Locale.getDefault(), "%dKB", byteNum / 1024);
        } else if (byteNum < 1073741824) {
            return String.format(Locale.getDefault(), "%dMB", byteNum / 1048576);
        } else {
            return String.format(Locale.getDefault(), "%dGB", byteNum / 1073741824);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void setListeners() {
        mRG.setOnCheckedChangeListener((group, checkedId) -> {
            onChecked(checkedId);
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ResResultEvent event) {
        JSONObject object = event.getObject();
        Log.e("result", object.toString());
        if (mAdapter != null) {
            mAdapter.setData(object.optJSONArray("content"));
        }
    }

    private void onChecked(int checkedId) {
        switch (checkedId) {
            case R.id.rb_download:
                EventBus.getDefault().post(new ResPathEvent(ResPath.DOWNLOAD));
                break;
            case R.id.rb_desktop:
                EventBus.getDefault().post(new ResPathEvent(ResPath.DESKTOP));
                break;
            case R.id.rb_documents:
                EventBus.getDefault().post(new ResPathEvent(ResPath.DOCUMENTS));
                break;
            case R.id.rb_my_device:
                openStorage();
                break;
        }
    }

    private void openStorage() {
        if (AndPermission.hasPermissions(this, Permission.Group.STORAGE)) {
            showFileDir(getSDPath());
        } else {
            AndPermission.with(this)
                    .runtime()
                    .permission(Permission.Group.STORAGE)
                    .onGranted(data -> {
                        //申请权限成功
                        showFileDir(getSDPath());
                    })
                    .onDenied(data -> {
                        //当用户没有允许该权限时，回调该方法
                        Toast.makeText(ResActivity.this, "没有获取SD卡存储权限，该功能无法使用", Toast.LENGTH_SHORT).show();
                    }).start();
        }
    }

    /**
     * 扫描显示文件列表
     *
     * @param path
     */
    private void showFileDir(String path) {
        File file = new File(path);

        File[] files = file.listFiles();
        JSONArray array = new JSONArray();
        //添加所有文件
        for (File f : files) {
            if (!f.getName().startsWith(".")) {
                JSONObject object = new JSONObject();
                try {
                    object.put("name", f.getName());
                    if (f.exists() && f.isFile()) {
                        object.put("size", f.length());
                        object.put("mime", getExtensionName(f.getName()));
                    } else if (f.exists() && f.isDirectory()) {
                        object.put("size", f.length());
                        object.put("mime", "dir");
                    }
                    object.put("path", f.getPath());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                array.put(object);
            }
        }
        JSONObject object = new JSONObject();
        try {
            object.put("name", "..");
            object.put("size", 0);
            object.put("mime", "parentDir");
            object.put("path", path.substring(0, path.lastIndexOf("/")));
            array.put(0, object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (mAdapter != null) {
            mAdapter.setData(array);
        }
    }

    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

}
