package com.dingsoft.webrtc.webrtcroom.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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

import com.blankj.utilcode.util.ToastUtils;
import com.codyy.live.share.MimeDrawableUtil;
import com.codyy.live.share.OpenItemEvent;
import com.codyy.live.share.ResPath;
import com.codyy.live.share.ResPathEvent;
import com.codyy.live.share.ResResultEvent;
import com.codyy.live.share.ResSendFileEvent;
import com.codyy.live.share.ResType;
import com.codyy.mobile.support.chart.CirclePercentChart;
import com.dingsoft.webrtc.webrtcroom.R;
import com.jakewharton.rxbinding2.view.RxView;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.internal.disposables.ListCompositeDisposable;

/**
 * created by lijian on 2019/08/19
 * 资源管理
 */
public class ResActivity extends AppCompatActivity {
    private RadioGroup mRG;
    private MyAdapter mAdapter;
    private ListCompositeDisposable listCompositeDisposable = new ListCompositeDisposable();
    private CirclePercentChart circlePercentChart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_res);
        mRG = findViewById(R.id.rg);
        circlePercentChart = findViewById(R.id.chart);
        RecyclerView mRv = findViewById(R.id.rv_content);
        setListeners();
        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
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
        MyAdapter(JSONArray myDataset) {
            mDataset = myDataset;
        }

        void setData(JSONArray mDataset) {
            this.mDataset = mDataset;
            this.notifyDataSetChanged();
        }

        // Create new views (invoked by the layout manager)
        @NotNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_res_path, parent, false);
            MyViewHolder vh = new MyViewHolder(v);
            return vh;
        }

        private Observable<Object> setListener(View view) {
            return RxView.clicks(view).throttleFirst(500L, TimeUnit.MILLISECONDS);
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
            if (ResType.parentDir.equals(mime) || ResType.dir.equals(mime) || MimeDrawableUtil.isSupportedMimeToOpen(mime)) {
                listCompositeDisposable.add(setListener(holder.textView)
                        .subscribe(o -> {
                            if (ResType.parentDir.equals(mime) || ResType.dir.equals(mime)) {//打开文件夹
                                if (mRG.getCheckedRadioButtonId() == R.id.rb_my_device) {
                                    //本地文件夹点击事件处理
                                    showFileDir(path);
                                } else {
                                    //获取pc电脑文件夹列表
                                    EventBus.getDefault().post(new ResPathEvent(path));
                                }

                            } else if (MimeDrawableUtil.isSupportedMimeToOpen(mime)) {//是否是支持打开的文件类型
                                if (mRG.getCheckedRadioButtonId() == R.id.rb_my_device) {
                                    //如果是内置存储文件，则发送到PC后打开发送的文件
                                    if (circlePercentChart.getVisibility() == View.VISIBLE) {
                                        ToastUtils.showShort("文件上传中，请稍后");
                                    } else {
                                        new AlertDialog.Builder(holder.textView.getContext())
                                                .setTitle("提示")
                                                .setMessage("上传 " + fileName + " 至我的电脑?")
                                                .setCancelable(true)
                                                .setNegativeButton("取消", (dialog, which) -> {

                                                })
                                                .setPositiveButton("上传", (dialog, which) -> {
                                                    EventBus.getDefault().post(new ResSendFileEvent(path));
                                                })
                                                .create()
                                                .show();

                                    }
                                } else {
                                    //根据当前文件路径，在PC使用默认打开方式打开文件
                                    holder.textView.setOnClickListener(v -> {
                                        new AlertDialog.Builder(holder.textView.getContext())
                                                .setTitle("提示")
                                                .setMessage("是否在我的电脑打开文件 " + fileName + "?")
                                                .setCancelable(true)
                                                .setNegativeButton("取消", (dialog, which) -> {

                                                })
                                                .setPositiveButton("打开", (dialog, which) -> {
                                                    EventBus.getDefault().post(new OpenItemEvent(path));
                                                })
                                                .create()
                                                .show();
                                    });
                                }
                            }
                        }));
            } else {
                holder.textView.setOnClickListener(v -> {
                    ToastUtils.showShort("不支持的文件类型");
                });
            }

            if (!ResType.parentDir.equals(mime) && !ResType.dir.equals(mime)) {
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
            return String.format(Locale.getDefault(), "%.1fKB", byteNum / 1024f);
        } else if (byteNum < 1073741824) {
            return String.format(Locale.getDefault(), "%.1fMB", byteNum / 1048576f);
        } else {
            return String.format(Locale.getDefault(), "%.2fGB", byteNum / 1073741824f);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listCompositeDisposable != null) {
            listCompositeDisposable.clear();
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        if (circlePercentChart.getVisibility() == View.VISIBLE) {
            ToastUtils.showShort("文件上传中，请稍后...");
        } else {
            super.onBackPressed();
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FileUploadEvent event) {
        switch (event.getState()) {
            case FileUploadEvent.FILE_UPLOADING:
                if (circlePercentChart.getVisibility() != View.VISIBLE) {
                    circlePercentChart.setVisibility(View.VISIBLE);
                }
                circlePercentChart.setPercent(event.getProgress());
                break;
            case FileUploadEvent.FILE_UPLOAD_FAILED:
                ToastUtils.showShort("文件上传失败");
                circlePercentChart.setVisibility(View.GONE);
                break;
            case FileUploadEvent.FILE_UPLOAD_SUCCESS:
                ToastUtils.showShort("文件上传成功，可在我的电脑-文档中打开");
                circlePercentChart.postDelayed(() -> circlePercentChart.setVisibility(View.GONE), 1000L);
                break;
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
                openStorage(getSDPath());
                break;
        }
    }

    private void openStorage(String path) {
        if (AndPermission.hasPermissions(this, Permission.Group.STORAGE)) {
            showFileDir(path);
        } else {
            AndPermission.with(this)
                    .runtime()
                    .permission(Permission.Group.STORAGE)
                    .onGranted(data -> {
                        //申请权限成功
                        showFileDir(path);
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
        if (!path.equals(getSDPath())) {//如果path和sd卡根路径一致，则不增加返回上一页选项
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
