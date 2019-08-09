package com.safframework.netty4android.client

import android.os.SystemClock
import android.util.Log
import com.safframework.netty4android.client.constant.ConnectState
import com.safframework.netty4android.client.handler.NettyClientHandler
import com.safframework.netty4android.client.listener.MessageStateListener
import com.safframework.netty4android.client.listener.NettyClientListener
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.timeout.IdleStateHandler
import io.netty.util.CharsetUtil
import java.util.concurrent.TimeUnit

/**
 *
 * @FileName:
 *          com.safframework.netty4android.client.NettyTcpClient
 * @author: Tony Shen
 * @date: 2019-08-05 20:52
 * @version: V1.0 <描述当前版本功能>
 */
class NettyTcpClient private constructor(val host: String, val tcp_port: Int, val index: Int) {

    private lateinit var group: EventLoopGroup

    private lateinit var listener: NettyClientListener<String>

    private var channel: Channel? = null

    /**
     * 获取TCP连接状态
     *
     * @return  获取TCP连接状态
     */
    var connectStatus = false

    /**
     * 最大重连次数
     */
    var maxConnectTimes = Integer.MAX_VALUE
        private set

    private var reconnectNum = maxConnectTimes

    private var isNeedReconnect = true

    var isConnecting = false
        private set

    var reconnectIntervalTime: Long = 5000
        private set

    /**
     * 心跳间隔时间
     */
    var heartBeatInterval: Long = 5
        private set//单位秒

    /**
     * 是否发送心跳
     */
    var isSendheartBeat = false
        private set

    /**
     * 心跳数据，可以是String类型，也可以是byte[].
     */
    private var heartBeatData: Any? = null

    fun connect() {
        if (isConnecting) {
            return
        }

        val clientThread = object : Thread("Netty-Client") {
            override fun run() {
                super.run()
                isNeedReconnect = true
                reconnectNum = maxConnectTimes
                connectServer()
            }
        }
        clientThread.start()
    }


    private fun connectServer() {

        synchronized(this@NettyTcpClient) {

            var channelFuture: ChannelFuture?=null

            if (!connectStatus) {
                isConnecting = true
                group = NioEventLoopGroup()
                val bootstrap = Bootstrap().group(group)
                        .option(ChannelOption.TCP_NODELAY, true)//屏蔽Nagle算法试图
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                        .channel(NioSocketChannel::class.java as Class<out Channel>?)
                        .handler(object : ChannelInitializer<SocketChannel>() {

                            @Throws(Exception::class)
                            public override fun initChannel(ch: SocketChannel) {

                                if (isSendheartBeat) {
                                    ch.pipeline().addLast("ping", IdleStateHandler(0, heartBeatInterval, 0, TimeUnit.SECONDS)) //5s未发送数据，回调userEventTriggered
                                }

                                ch.pipeline().addLast(StringEncoder(CharsetUtil.UTF_8))
                                ch.pipeline().addLast(StringDecoder(CharsetUtil.UTF_8))
                                ch.pipeline().addLast(LineBasedFrameDecoder(1024))//黏包处理,需要客户端、服务端配合
                                ch.pipeline().addLast(NettyClientHandler(listener, index, isSendheartBeat, heartBeatData))
                            }
                        })

                try {
                    channelFuture = bootstrap.connect(host, tcp_port).addListener {
                        if (it.isSuccess) {
                            Log.d(TAG, "连接成功")
                            reconnectNum = maxConnectTimes
                            connectStatus = true
                            channel = channelFuture?.channel()
                        } else {
                            Log.d(TAG, "连接失败")
                            connectStatus = false
                        }
                        isConnecting = false
                    }.sync()

                    // Wait until the connection is closed.
                    channelFuture.channel().closeFuture().sync()
                    Log.d(TAG, " 断开连接")
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    connectStatus = false
                    listener.onClientStatusConnectChanged(ConnectState.STATUS_CONNECT_CLOSED, index)

                    if (channelFuture != null) {
                        if (channelFuture.channel() != null && channelFuture.channel().isOpen) {
                            channelFuture.channel().close()
                        }
                    }
                    group.shutdownGracefully()
                    reconnect()
                }
            }
        }
    }


    fun disconnect() {
        Log.d(TAG, "disconnect")
        isNeedReconnect = false
        group.shutdownGracefully()
    }

    fun reconnect() {
        Log.d(TAG, "reconnect")
        if (isNeedReconnect && reconnectNum > 0 && !connectStatus) {
            reconnectNum--
            SystemClock.sleep(reconnectIntervalTime)
            if (isNeedReconnect && reconnectNum > 0 && !connectStatus) {
                Log.e(TAG, "重新连接")
                connectServer()
            }
        }
    }

    /**
     * 异步发送
     *
     * @param data 要发送的数据
     * @param listener 发送结果回调
     * @return 方法执行结果
     */
    fun sendMsgToServer(data: String, listener: MessageStateListener) = channel?.run {

        val flag = this != null && connectStatus

        if (flag) {

            this.writeAndFlush(data + System.getProperty("line.separator")).addListener { channelFuture -> listener.isSendSuccss(channelFuture.isSuccess) }
        }

        flag

    } ?: false

    /**
     * 同步发送
     *
     * @param data 要发送的数据
     * @return 方法执行结果
     */
    fun sendMsgToServer(data: String) = channel?.run {

        val flag = this != null && connectStatus

        if (flag) {

            val channelFuture = this.writeAndFlush(data + System.getProperty("line.separator")).awaitUninterruptibly()
            return channelFuture.isSuccess
        }

        false

    }?:false

    fun setListener(listener: NettyClientListener<String>) {
        this.listener = listener
    }

    /**
     * Builder 模式创建NettyTcpClient
     */
    class Builder {

        /**
         * 最大重连次数
         */
        private var MAX_CONNECT_TIMES = Integer.MAX_VALUE

        /**
         * 重连间隔
         */
        private var reconnectIntervalTime: Long = 5000
        /**
         * 服务器地址
         */
        private var host: String? = null
        /**
         * 服务器端口
         */
        private var tcp_port: Int = 0
        /**
         * 客户端标识，(因为可能存在多个连接)
         */
        private var mIndex: Int = 0

        /**
         * 是否发送心跳
         */
        private var isSendheartBeat: Boolean = false
        /**
         * 心跳时间间隔
         */
        private var heartBeatInterval: Long = 5

        /**
         * 心跳数据，可以是String类型，也可以是byte[].
         */
        private var heartBeatData: Any? = null


        fun setMaxReconnectTimes(reConnectTimes: Int): Builder {
            this.MAX_CONNECT_TIMES = reConnectTimes
            return this
        }


        fun setReconnectIntervalTime(reconnectIntervalTime: Long): Builder {
            this.reconnectIntervalTime = reconnectIntervalTime
            return this
        }


        fun setHost(host: String): Builder {
            this.host = host
            return this
        }

        fun setTcpPort(tcp_port: Int): Builder {
            this.tcp_port = tcp_port
            return this
        }

        fun setIndex(mIndex: Int): Builder {
            this.mIndex = mIndex
            return this
        }

        fun setHeartBeatInterval(intervalTime: Long): Builder {
            this.heartBeatInterval = intervalTime
            return this
        }

        fun setSendheartBeat(isSendheartBeat: Boolean): Builder {
            this.isSendheartBeat = isSendheartBeat
            return this
        }

        fun setHeartBeatData(heartBeatData: Any): Builder {
            this.heartBeatData = heartBeatData
            return this
        }

        fun build(): NettyTcpClient {
            val nettyTcpClient = NettyTcpClient(host!!, tcp_port, mIndex)
            nettyTcpClient.maxConnectTimes = this.MAX_CONNECT_TIMES
            nettyTcpClient.reconnectIntervalTime = this.reconnectIntervalTime
            nettyTcpClient.heartBeatInterval = this.heartBeatInterval
            nettyTcpClient.isSendheartBeat = this.isSendheartBeat
            nettyTcpClient.heartBeatData = this.heartBeatData
            return nettyTcpClient
        }
    }

    companion object {
        private val TAG = "NettyTcpClient"
        private val CONNECT_TIMEOUT_MILLIS = 5000
    }
}