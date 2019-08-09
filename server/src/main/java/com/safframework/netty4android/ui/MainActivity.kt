package com.safframework.netty4android.ui

/**
 *
 * @FileName:
 *          com.safframework.netty4android.ui.MainActivity
 * @author: Tony Shen
 * @date: 2019-08-05 19:41
 * @version: V1.0 <描述当前版本功能>
 */

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.safframework.netty4android.R
import com.safframework.netty4android.server.NettyServer
import com.safframework.netty4android.server.NettyServerListener
import com.safframework.netty4android.ui.domain.ClientChanel
import com.safframework.netty4android.ui.domain.MessageBean
import com.safframework.netty4android.ui.adapter.CustomSpinnerAdapter
import com.safframework.netty4android.ui.adapter.MessageAdapter
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener, NettyServerListener<String> {

    private val mSendMessageAdapter = MessageAdapter()
    private val mReceMessageAdapter = MessageAdapter()

    var clientChanelArray: MutableList<ClientChanel> = mutableListOf()  //储存客户端通道信息
    private lateinit var spinnerAdapter: CustomSpinnerAdapter

    private var port:Int = 8888
    private var webSocketPath:String = "/ws"

    private val REQUEST_CODE_CONFIG:Int = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initData()
        initlisteners()
    }

    private fun initData() {

        send_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        send_list.adapter = mSendMessageAdapter

        rece_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rece_list.adapter = mReceMessageAdapter
    }

    private fun initlisteners() {

        configServer.setOnClickListener(this)
        startServer.setOnClickListener(this)
        send_tcp_btn.setOnClickListener(this)
        send_ws_btn.setOnClickListener(this)
        clear_log.setOnClickListener(this)

        spinnerAdapter = CustomSpinnerAdapter(this, clientChanelArray)

        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                val clientChanel = spinnerAdapter.getItem(position)
                Toast.makeText(this@MainActivity, "onItemSelected:" + clientChanel.clientIp, Toast.LENGTH_LONG).show()
                NettyServer.selectorChannel(clientChanel.channel)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

                Toast.makeText(this@MainActivity, "onNothingSelected", Toast.LENGTH_LONG).show()
                NettyServer.selectorChannel(null)
            }
        }
    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.configServer -> configServer()

            R.id.startServer -> startServer()

            R.id.send_tcp_btn -> {

                if (!NettyServer.isServerStart) {

                    Toast.makeText(applicationContext, "未连接,请先连接", LENGTH_SHORT).show()
                } else {
                    val msg = send_tcp_et.text.toString()
                    if (TextUtils.isEmpty(msg.trim { it <= ' ' })) {
                        return
                    }

                    NettyServer.sendMsgToClient(msg, ChannelFutureListener { channelFuture ->

                        if (channelFuture.isSuccess) {
                            Log.d(TAG, "Write auth successful")
                            msgSend(msg)
                        } else {
                            Log.d(TAG, "Write auth error")
                        }
                    })
                    send_tcp_et.setText("")
                }
            }

            R.id.send_ws_btn -> {

                if (!NettyServer.isServerStart) {

                    Toast.makeText(applicationContext, "未连接,请先连接", LENGTH_SHORT).show()
                } else {
                    val msg = send_ws_et.text.toString()
                    Log.i(TAG,msg)
                    if (TextUtils.isEmpty(msg.trim { it <= ' ' })) {
                        return
                    }

                    Log.i(TAG,msg)

                    NettyServer.sendMsgToWS(msg, ChannelFutureListener { channelFuture ->

                        if (channelFuture.isSuccess) {
                            Log.d(TAG, "Write auth successful")
                            msgSend(msg)
                        } else {
                            Log.d(TAG, "Write auth error")
                        }
                    })
                    send_ws_et.setText("")
                }
            }

            R.id.clear_log -> {
                mReceMessageAdapter.dataList.clear()
                mSendMessageAdapter.dataList.clear()
                mReceMessageAdapter.notifyDataSetChanged()
                mSendMessageAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun configServer() {

        val intent = Intent(this@MainActivity,ConfigServerActivity::class.java)
        intent.putExtra("port",port)
        intent.putExtra("webSocketPath",webSocketPath)
        startActivityForResult(intent,REQUEST_CODE_CONFIG)
    }

    private fun startServer() {

        if (!NettyServer.isServerStart) {
            NettyServer.setListener(this@MainActivity)
            NettyServer.port = port
            NettyServer.webSocketPath = webSocketPath
            NettyServer.start()
        } else {
            NettyServer.disconnect()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CONFIG && data!=null) {

            port = data.getStringExtra("port").toInt()
            webSocketPath = data.getStringExtra("webSocketPath")?:"/ws"

            Log.i(TAG,"port=$port, webSocketPath=$webSocketPath")
        }
    }


    override fun onMessageResponseServer(msg: String, uniqueId: String) {

        msgReceive(msg)
    }

    override fun onChannelConnect(channel: Channel) {

        val socketStr = channel.remoteAddress().toString()
        val clientChanel = ClientChanel(socketStr, channel, channel.id().asShortText())

        synchronized(clientChanelArray) {
            clientChanelArray.add(clientChanel)
            runOnUiThread {
                Toast.makeText(this@MainActivity, clientChanel.clientIp + " 建立连接", Toast.LENGTH_LONG).show()
                spinnerAdapter.notifyDataSetChanged()
            }
        }

    }

    override fun onChannelDisConnect(channel: Channel) {
        Log.e(TAG, "onChannelDisConnect:ChannelId" + channel.id().asShortText())

        for (i in clientChanelArray.indices) {
            val clientChanel = clientChanelArray[i]
            if (clientChanel.shortId.equals(channel.id().asShortText())) {

                /**
                 * 当Spinner里第一个item被remove，不会触发onItemSelected，（因为 mSelectedPosition != mOldSelectedPosition）
                 */
                if (i == 0) {
                    try {
                        val field = AdapterView::class.java.getDeclaredField("mOldSelectedPosition")
                        field.isAccessible = true  //设置mOldSelectedPosition可访问
                        field.setInt(spinner, AdapterView.INVALID_POSITION) //设置mOldSelectedPosition的值
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

                synchronized(clientChanelArray) {
                    clientChanelArray.remove(clientChanel)
                    runOnUiThread {
                        Log.e(TAG, "disconncect " + clientChanel.clientIp)
                        Toast.makeText(this@MainActivity, clientChanel.clientIp + " 断开连接", Toast.LENGTH_LONG).show()
                        spinnerAdapter.notifyDataSetChanged()
                    }
                }

                return
            }
        }

    }

    override fun onStartServer() {
        Log.d(TAG, "onStartServer")
        runOnUiThread { startServer.text = "stopServer" }
    }

    override fun onStopServer() {
        Log.d(TAG, "onStopServer")
        runOnUiThread { startServer.text = "startServer" }
    }


    private fun msgSend(msg: String) {
        val msgBean = MessageBean(System.currentTimeMillis(), msg)
        mSendMessageAdapter.dataList.add(0, msgBean)
        runOnUiThread { mSendMessageAdapter.notifyDataSetChanged() }

    }

    private fun msgReceive(msg: String) {
        val msgBean = MessageBean(System.currentTimeMillis(), msg)
        mReceMessageAdapter.dataList.add(0, msgBean)
        runOnUiThread { mReceMessageAdapter.notifyDataSetChanged() }
    }

    companion object {

        private val TAG = "MainActivity"
    }

}
