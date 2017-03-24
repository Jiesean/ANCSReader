package com.jiesean.readancs;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jiesean.readancs.dataprocess.Notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {

    //log TAG
    private static final String TAG = Constants.AUTHOR + "MainActivity";

    HashMap<Integer, byte[]> mDataMap = new HashMap<>();

    private List<Notification> list = new ArrayList<Notification>();

    //ui view
    private Button mConnectGattBtn;
    private TextView mShowTitleTextView;
    private FrameLayout mContentLayout;
    private ImageView mShowStateIV;
    private RecyclerView mRecyclerView;
    private LocalAdapter localAdapter;
    //MyOnClickListener
    private MyOnClickListener myOnClickListener = new MyOnClickListener();

    byte[] nid;

    //service connection
    LeService.LocalBinder mService;
    ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected");
            mService = (LeService.LocalBinder) iBinder;
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        initView();
        //开启蓝牙连接的服务
        Intent serviceIntent = new Intent(MainActivity.this, LeService.class);
        bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    /**
     * 初始化界面
     */
    private void initView() {
        mConnectGattBtn = (Button) findViewById(R.id.connect_gatt_btn);
        mConnectGattBtn.setOnClickListener(myOnClickListener);

        mShowTitleTextView = (TextView) findViewById(R.id.title_text_textview);
        mContentLayout = (FrameLayout) findViewById(R.id.layout_content);
        mShowStateIV = (ImageView) findViewById(R.id.show_state_iv);

        // 拿到RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        // 设置LinearLayoutManager
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        // 设置ItemAnimator
        RecyclerView.ItemAnimator itemAnimator = new RecyclerView.ItemAnimator() {
            @Override
            public boolean animateDisappearance(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo) {
                return false;
            }
            @Override
            public boolean animateAppearance(@NonNull RecyclerView.ViewHolder viewHolder, @Nullable ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
                return false;
            }
            @Override
            public boolean animatePersistence(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
                return false;
            }
            @Override
            public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder, @NonNull RecyclerView.ViewHolder newHolder, @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
                return false;
            }
            @Override
            public void runPendingAnimations() {

            }
            @Override
            public void endAnimation(RecyclerView.ViewHolder item) {
            }
            @Override
            public void endAnimations() {
            }

            @Override
            public boolean isRunning() {
                return false;
            }
        };
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        // 设置固定大小
        mRecyclerView.setHasFixedSize(true);
        // 初始化自定义的适配器
        localAdapter = new LocalAdapter((ArrayList<Notification>) list);
        localAdapter.setOnItemClickListener(new LocalAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int positionNoUse, Notification no) {
                //获得被点击的按钮所在的cardview上的隐藏部分
                RelativeLayout moreLayout = (RelativeLayout) ((ViewGroup) view.getParent().getParent()).findViewById(R.id.more_info_layout);
                //获得被点击按钮所在的cardview
                CardView cardView = (CardView) moreLayout.getParent().getParent();
                int position = mRecyclerView.getChildAdapterPosition(cardView);

                //显示更多按钮被点击
                if (view.getId() == R.id.more_info_btn) {
                    if (moreLayout.getVisibility() == View.GONE) {//如果是gone状态，则改为visible
                        moreLayout.setVisibility(View.VISIBLE);
                    } else {//如果是visible状态，则改为gone
                        moreLayout.setVisibility(View.GONE);
                    }
                }
                //点击收起按钮，相应的morelayout消失
                if (view.getId() == R.id.back_btn) {
                    if (moreLayout.getVisibility() == View.VISIBLE) {
                        moreLayout.setVisibility(View.GONE);
                    }
                }
                //negative按钮被点击
                if (view.getId() == R.id.negative_btn) {
                    if (moreLayout.getVisibility() == View.VISIBLE) {
                        moreLayout.setVisibility(View.GONE);
                    }
                    mService.negativeResponseToNotification(list.get(position).getNid());
                    list.remove(position);
                    localAdapter.notifyDataSetChanged();
                }
                //positive按钮被点击
                if (view.getId() == R.id.positive_btn) {
                    mService.positiveResponseToNotification(list.get(position).getNid());
                }
            }
        });
        // 为mRecyclerView设置适配器
        mRecyclerView.setAdapter(localAdapter);
    }


    /**
     * 更新界面
     */
    private void updateUI(Notification notification) {

        //新增消息
        if (notification.getEventId() == 0) {
                list.add(notification);
                localAdapter.notifyDataSetChanged();

        }
        //消息更改
        if (notification.getEventId() == 1) {
            for (Notification notification1 : list) {
                if (notification.getIntegerNid() == notification1.getIntegerNid()) {
                    list.remove(list.indexOf(notification1));
                    list.add(list.indexOf(notification1),notification);
                }
            }
            localAdapter.notifyDataSetChanged();
        }
        //消息删除
        if (notification.getEventId() == 2) {
            for (Notification notification1 : list) {
                if (notification.getIntegerNid() == notification1.getIntegerNid()) {
                    list.remove(list.indexOf(notification1));
                }
            }
            localAdapter.notifyDataSetChanged();
        }

    }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BroadcastReceiver");
            final String action = intent.getAction();
            if (Constants.ACTION_NOTIFICATION_SOURCE.equals(action)) {
                Notification notification = (Notification) intent.getSerializableExtra("notice");
                updateUI(notification);
            }
            if (Constants.ACTION_DATA_SOURCE.equals(action)) {
                Notification notification = (Notification) intent.getSerializableExtra("content");
                updateUI(notification);
            }
            if (Constants.ACTION_STATE_INFO.equals(action)) {
                switch (intent.getStringExtra("state")){
                    case Constants.BOND_FAIL:
                        break;
                    case Constants.DEVICE_FIND:
                        showMessage(Constants.DEVICE_FIND);
                        break;
                    case Constants.NO_BLUETOOTH:
                        showMessage("enable bluetooth");
                        break;
                    case Constants.DISCONNECTED:
                        mConnectGattBtn.setVisibility(View.VISIBLE);
                        mConnectGattBtn.setClickable(true);
                        mShowTitleTextView.setText("No Device");
                        list.clear();
                        localAdapter.notifyDataSetChanged();
                        break;
                    case Constants.CONNECT_SUCCESS:
                        showMessage(Constants.CONNECT_SUCCESS);
                        mConnectGattBtn.setVisibility(View.GONE);
                        mShowStateIV.setVisibility(View.VISIBLE);
                        mShowTitleTextView.setText("Iphone");
                        break;
                }
            }

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1) == BluetoothDevice.BOND_BONDED) {
                    mService.connectToGattServer();
                }
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_DATA_SOURCE);
        intentFilter.addAction(Constants.ACTION_NOTIFICATION_SOURCE);
        intentFilter.addAction(Constants.ACTION_STATE_INFO);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        return intentFilter;
    }

    private void showMessage(String message){
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.connect_gatt_btn:
                    Toast.makeText(MainActivity.this,"start scan",Toast.LENGTH_SHORT);
                    mService.startLeScan();
                    break;
            }
        }
    }

}
