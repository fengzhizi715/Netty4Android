package com.safframework.netty4android.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.safframework.netty4android.R;
import com.safframework.netty4android.client.NettyTcpClient;
import com.safframework.netty4android.client.constant.ConnectState;
import com.safframework.netty4android.client.listener.MessageStateListener;
import com.safframework.netty4android.client.listener.NettyClientListener;
import com.safframework.netty4android.ui.adapter.MessageAdapter;
import com.safframework.netty4android.ui.domain.MessageBean;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NettyClientListener<String> {

    private static final String TAG = "MainActivity";
    private Button mClearLog;
    private Button mSendBtn;
    private Button mConnect;
    private EditText mSendET;
    private RecyclerView mSendList;
    private RecyclerView mReceList;

    private MessageAdapter mSendMessageAdapter = new MessageAdapter();
    private MessageAdapter mReceMessageAdapter = new MessageAdapter();
    private NettyTcpClient mNettyTcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        initView();

        mNettyTcpClient = new NettyTcpClient.Builder()
                .setHost("10.184.16.77")    //设置服务端地址
                .setTcpPort(1088) //设置服务端端口号
                .setMaxReconnectTimes(5)    //设置最大重连次数
                .setReconnectIntervalTime(5)    //设置重连间隔时间。单位：秒
                .setSendheartBeat(false) //设置发送心跳
                .setHeartBeatInterval(5)    //设置心跳间隔时间。单位：秒
                .setHeartBeatData("I'm is HeartBeatData") //设置心跳数据，可以是String类型，也可以是byte[]，以后设置的为准
                .setIndex(0)    //设置客户端标识.(因为可能存在多个tcp连接)
                .build();

        mNettyTcpClient.setListener(MainActivity.this); //设置TCP监听
    }

    private void initView() {

        LinearLayoutManager manager1 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mSendList.setLayoutManager(manager1);
        mSendList.setAdapter(mSendMessageAdapter);

        LinearLayoutManager manager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mReceList.setLayoutManager(manager2);
        mReceList.setAdapter(mReceMessageAdapter);
    }

    private void findViews() {

        mSendList = findViewById(R.id.send_list);
        mReceList = findViewById(R.id.rece_list);
        mSendET = findViewById(R.id.send_et);
        mConnect = findViewById(R.id.connect);
        mSendBtn = findViewById(R.id.send_btn);
        mClearLog = findViewById(R.id.clear_log);

        mConnect.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
        mClearLog.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.connect:
                connect();
                break;

            case R.id.send_btn:
                if (!mNettyTcpClient.getConnectStatus()) {

                    Toast.makeText(getApplicationContext(), "未连接,请先连接", LENGTH_SHORT).show();
                } else {
                    final String msg = mSendET.getText().toString();
                    if (TextUtils.isEmpty(msg.trim())) {
                        return;
                    }
                    mNettyTcpClient.sendMsgToServer(msg, new MessageStateListener() {
                        @Override
                        public void isSendSuccss(boolean isSuccess) {
                            if (isSuccess) {
                                Log.d(TAG, "Write auth successful");
                                msgSend(msg);
                            } else {
                                Log.d(TAG, "Write auth error");
                            }
                        }
                    });
                    mSendET.setText("");
                }

                break;

            case R.id.clear_log:
                mReceMessageAdapter.getDataList().clear();
                mSendMessageAdapter.getDataList().clear();
                mReceMessageAdapter.notifyDataSetChanged();
                mSendMessageAdapter.notifyDataSetChanged();
                break;
        }
    }

    private void connect() {
        Log.d(TAG, "connect");
        if (!mNettyTcpClient.getConnectStatus()) {
            mNettyTcpClient.connect();//连接服务器
        } else {
            mNettyTcpClient.disconnect();
        }
    }

    @Override
    public void onMessageResponseClient(String msg, int index) {
        Log.e(TAG, "onMessageResponse:" + msg);
        msgReceive(index + ":" + msg);
    }

    @Override
    public void onClientStatusConnectChanged(final int statusCode, final int index) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (statusCode == ConnectState.STATUS_CONNECT_SUCCESS) {
                    Log.e(TAG, "STATUS_CONNECT_SUCCESS:");
                    mConnect.setText("DisConnect:" + index);
                } else {
                    Log.e(TAG, "onServiceStatusConnectChanged:" + statusCode);
                    mConnect.setText("Connect:" + index);
                }
            }
        });

    }

    private void msgSend(String log) {
        MessageBean messageBean = new MessageBean(System.currentTimeMillis(), log);
        mSendMessageAdapter.getDataList().add(0, messageBean);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSendMessageAdapter.notifyDataSetChanged();
            }
        });

    }

    private void msgReceive(String log) {
        MessageBean messageBean = new MessageBean(System.currentTimeMillis(), log);
        mReceMessageAdapter.getDataList().add(0, messageBean);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mReceMessageAdapter.notifyDataSetChanged();
            }
        });

    }

    public void disconnect(View view) {
        mNettyTcpClient.disconnect();
    }
}