package com.dingsoft.webrtc.webrtcroom.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.codyy.live.share.DividerGridItemDecoration;
import com.codyy.live.share.MirrorClientsEvent;
import com.dingsoft.webrtc.webrtcroom.R;
import com.jakewharton.rxbinding2.view.RxView;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.internal.disposables.ListCompositeDisposable;

/**
 * created by lijian on 2019/08/19
 * 同屏客户端列表选项
 */
public class MirrorClientsActivity extends AppCompatActivity {
    private RecyclerView mRv;
    private Toolbar mToolBar;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ListCompositeDisposable listCompositeDisposable = new ListCompositeDisposable();
    private List<String> ids = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mirror_clients);
        mToolBar=findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        mRv = findViewById(R.id.rv_content);
        // use a linear layout manager
        layoutManager = new GridLayoutManager(this, 9);
        mRv.setLayoutManager(layoutManager);
        mRv.addItemDecoration(new DividerGridItemDecoration(this));
        // specify an adapter (see also next example)
        JSONArray array = new JSONArray();
        List<String> items = getIntent().getStringArrayListExtra("list");
        for (String string : items) {
            array.put(string);
        }
        mAdapter = new MyAdapter(array);

        mRv.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_mirror_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_mirror:
                clickMirrorMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clickMirrorMenu() {
        if (ids.size() == 0) {
            ToastUtils.showShort("请至少选择一个客户端");
            return;
        }
        EventBus.getDefault().post(new MirrorClientsEvent(ids));
        finish();

    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private JSONArray mDataset;
        private int count = 0;
        private int max = 4;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            TextView textView;
            ConstraintLayout mRoot;
            CheckBox mCbHeader;

            MyViewHolder(View v) {
                super(v);
                textView = v.findViewById(R.id.tv_name);
                mRoot = v.findViewById(R.id.item_mirror_root);
                mCbHeader = v.findViewById(R.id.cb_header);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(JSONArray myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_mirror, parent, false);
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
            holder.textView.setText(mDataset.optString(position));
            listCompositeDisposable.add(setListener(holder.mRoot)
                    .subscribe(o -> {
                        if (holder.mCbHeader.isChecked()) {
                            holder.mCbHeader.setChecked(!holder.mCbHeader.isChecked());
                            ids.remove(mDataset.optString(position));
                            count--;
                        } else if (!holder.mCbHeader.isChecked()) {//没有选中且数量不大于4
                            if (count < max) {
                                holder.mCbHeader.setChecked(!holder.mCbHeader.isChecked());
                                ids.add(mDataset.optString(position));
                                count++;
                            } else {
                                ToastUtils.showShort("最多选择4个客户端，请先取消其他选项");
                            }

                        }

                    }));
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.length();
        }
    }


}
