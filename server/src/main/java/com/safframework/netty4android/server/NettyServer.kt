package com.safframework.netty4android.server

import android.util.Log
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import java.net.InetSocketAddress

/**
 *
 * @FileName:
 *          com.safframework.netty4android.server.NettyServer
 * @author: Tony Shen
 * @date: 2019-08-05 18:04
 * @version: V1.0 <描述当前版本功能>
 */
object NettyServer {

    private val TAG = "NettyServer"

    private var channel: Channel?=null
    private lateinit var listener: NettyServerListener<String>
    private lateinit var bossGroup: EventLoopGroup
    private lateinit var workerGroup: EventLoopGroup

    var port = 8888
        set(value)  {
            field = value
        }

    var webSocketPath = "/ws"
        set(value)  {
            field = value
        }

    var isServerStart: Boolean = false
        private set

    fun start() {
        object : Thread() {
            override fun run() {
                super.run()
                bossGroup = NioEventLoopGroup(1)
                workerGroup = NioEventLoopGroup()
                try {
                    val b = ServerBootstrap()
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel::class.java)
                            .localAddress(InetSocketAddress(port))
                            .childOption(ChannelOption.SO_KEEPALIVE, true)
                            .childOption(ChannelOption.SO_REUSEADDR, true)
                            .childOption(ChannelOption.TCP_NODELAY, true)
                            .childHandler(NettyServerInitializer(listener,webSocketPath))

                    // Bind and start to accept incoming connections.
                    val f = b.bind().sync()
                    Log.i(TAG, NettyServer::class.java.name + " started and listen on " + f.channel().localAddress())

                    isServerStart = true
                    listener.onStartServer()
                    f.channel().closeFuture().sync()
                } catch (e: Exception) {
                    Log.e(TAG, e.localizedMessage)
                    e.printStackTrace()
                } finally {
                    isServerStart = false
                    listener.onStopServer()

                    disconnect()
                }
            }
        }.start()

    }

    fun disconnect() {
        workerGroup.shutdownGracefully()
        bossGroup.shutdownGracefully()
    }

    fun setListener(listener: NettyServerListener<String>) {
        this.listener = listener
    }

    // 异步发送TCP消息
    fun sendMsgToClient(data: String, listener: ChannelFutureListener) = channel?.run {

        val flag = this.isActive

        if (flag) {

            this.writeAndFlush(data + System.getProperty("line.separator")).addListener(listener)
        }

        flag
    } ?: run {

        println("channel is null")
        false
    }

    // 同步发送TCP消息
    fun sendMsgToClient(data: String) = channel?.run {

        if (this.isActive) {

            return this.writeAndFlush(data + System.getProperty("line.separator")).awaitUninterruptibly().isSuccess
        }

        false
    } ?: run {

        println("channel is null")
        false
    }

    // 异步发送WebSocket消息
    fun sendMsgToWS(data: String, listener: ChannelFutureListener) = channel?.run {

        val flag = this.isActive

        if (flag) {

            this.writeAndFlush(TextWebSocketFrame(data)).addListener(listener)
        }

        flag
    } ?: run {

        println("channel is null")
        false
    }

    // 同步发送WebSocket消息
    fun sendMsgToWS(data: String) = channel?.run {

        if (this.isActive) {

            return this.writeAndFlush(TextWebSocketFrame(data)).awaitUninterruptibly().isSuccess
        }

        false
    } ?: run {

        println("channel is null")
        false
    }

    /**
     * 切换通道
     * 设置服务端，与哪个客户端通信
     * @param channel
     */
    fun selectorChannel(channel: Channel?) {
        this.channel = channel
    }
}