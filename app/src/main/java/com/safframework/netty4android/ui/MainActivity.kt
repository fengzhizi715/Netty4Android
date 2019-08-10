package com.safframework.netty4android.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.safframework.netty4android.R
import com.safframework.netty4android.client.NettyTcpClient
import com.safframework.netty4android.client.constant.ConnectState
import com.safframework.netty4android.client.listener.MessageStateListener
import com.safframework.netty4android.client.listener.NettyClientListener
import com.safframework.netty4android.ui.adapter.MessageAdapter
import com.safframework.netty4android.ui.domain.MessageBean
import kotlinx.android.synthetic.main.activity_main.*

/**
 *
 * @FileName:
 *          com.safframework.netty4android.ui.MainActivity
 * @author: Tony Shen
 * @date: 2019-08-10 10:31
 * @version: V1.0 <描述当前版本功能>
 */
class MainActivity : AppCompatActivity(), View.OnClickListener, NettyClientListener<String> {

    private val mSendMessageAdapter = MessageAdapter()
    private val mReceMessageAdapter = MessageAdapter()
    private lateinit var mNettyTcpClient: NettyTcpClient

    private var ip:String = "10.184.16.77"
    private var port:Int = 8888

    private val REQUEST_CODE_CONFIG:Int = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViews()
        initView()

        mNettyTcpClient = NettyTcpClient.Builder()
                .setHost(ip)                    //设置服务端地址
                .setTcpPort(port)               //设置服务端端口号
                .setMaxReconnectTimes(5)        //设置最大重连次数
                .setReconnectIntervalTime(5)    //设置重连间隔时间。单位：秒
                .setSendheartBeat(false)        //设置发送心跳
                .setHeartBeatInterval(5)        //设置心跳间隔时间。单位：秒
                .setHeartBeatData("I'm is HeartBeatData") //设置心跳数据，可以是String类型，也可以是byte[]，以后设置的为准
                .setIndex(0)                    //设置客户端标识.(因为可能存在多个tcp连接)
                .build()

        mNettyTcpClient.setListener(this@MainActivity) //设置TCP监听
    }

    private fun findViews() {

        configClient.setOnClickListener(this)
        connect.setOnClickListener(this)
        send_btn.setOnClickListener(this)
        clear.setOnClickListener(this)
    }

    private fun initView() {

        send_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        send_list.adapter = mSendMessageAdapter

        rece_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rece_list.adapter = mReceMessageAdapter
    }

    override fun onClick(v: View) {
        when (v.id) {

            R.id.configClient -> configClient()

            R.id.connect -> connect()

            R.id.send_btn -> if (!mNettyTcpClient.connectStatus) {

                Toast.makeText(applicationContext, "未连接,请先连接", LENGTH_SHORT).show()
            } else {
                val msg = send_et.text.toString()
                if (TextUtils.isEmpty(msg.trim { it <= ' ' })) {
                    return
                }

                mNettyTcpClient.sendMsgToServer(msg, object : MessageStateListener {
                    override fun isSendSuccss(isSuccess: Boolean) {
                        if (isSuccess) {
                            Log.d(TAG, "Write auth successful")
                            msgSend(msg)
                        } else {
                            Log.d(TAG, "Write auth error")
                        }
                    }
                })
                send_et.setText("")
            }

            R.id.clear -> {
                mReceMessageAdapter.dataList.clear()
                mSendMessageAdapter.dataList.clear()
                mReceMessageAdapter.notifyDataSetChanged()
                mSendMessageAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun configClient() {

        val intent = Intent(this@MainActivity,ConfigClientActivity::class.java)
        intent.putExtra("ip",ip)
        intent.putExtra("port",port)
        startActivityForResult(intent,REQUEST_CODE_CONFIG)
    }

    private fun connect() {
        Log.d(TAG, "connect")
        if (!mNettyTcpClient.connectStatus) {
            mNettyTcpClient.connect()//连接服务器
        } else {
            mNettyTcpClient.disconnect()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CONFIG && data!=null) {

            port = data.getStringExtra("port").toInt()
            ip = data.getStringExtra("ip")

            Log.i(TAG," ip=$ip, port=$port")

            mNettyTcpClient = NettyTcpClient.Builder()
                    .setHost(ip)                    //设置服务端地址
                    .setTcpPort(port)               //设置服务端端口号
                    .setMaxReconnectTimes(5)        //设置最大重连次数
                    .setReconnectIntervalTime(5)    //设置重连间隔时间。单位：秒
                    .setSendheartBeat(false)        //设置发送心跳
                    .setHeartBeatInterval(5)        //设置心跳间隔时间。单位：秒
                    .setHeartBeatData("I'm is HeartBeatData") //设置心跳数据，可以是String类型，也可以是byte[]，以后设置的为准
                    .setIndex(0)                    //设置客户端标识.(因为可能存在多个tcp连接)
                    .build()

            mNettyTcpClient.setListener(this@MainActivity) //设置TCP监听
        }
    }

    override fun onMessageResponseClient(msg: String, index: Int) {
        Log.d(TAG, "onMessageResponse:$msg")
        msgReceive("$index:$msg")
    }

    override fun onClientStatusConnectChanged(statusCode: Int, index: Int) {
        runOnUiThread {
            if (statusCode == ConnectState.STATUS_CONNECT_SUCCESS) {
                Log.d(TAG, "STATUS_CONNECT_SUCCESS:")
                connect.text = "DisConnect:$index"
            } else {
                Log.d(TAG, "onServiceStatusConnectChanged:$statusCode")
                connect.text = "Connect:$index"
            }
        }
    }

    private fun msgSend(message: String) {
        val messageBean = MessageBean(System.currentTimeMillis(), message)
        mSendMessageAdapter.dataList.add(0, messageBean)
        runOnUiThread { mSendMessageAdapter.notifyDataSetChanged() }

    }

    private fun msgReceive(message: String) {
        val messageBean = MessageBean(System.currentTimeMillis(), message)
        mReceMessageAdapter.dataList.add(0, messageBean)
        runOnUiThread { mReceMessageAdapter.notifyDataSetChanged() }
    }

    fun disconnect(view: View) {
        mNettyTcpClient.disconnect()
    }

    companion object {

        private val TAG = "MainActivity"
    }
}