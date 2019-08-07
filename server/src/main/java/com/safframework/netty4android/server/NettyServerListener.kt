package com.safframework.netty4android.server

import io.netty.channel.Channel

/**
 *
 * @FileName:
 *          com.safframework.netty4android.server.NettyServerListener
 * @author: Tony Shen
 * @date: 2019-08-05 18:50
 * @version: V1.0 <描述当前版本功能>
 */
interface NettyServerListener<T> {

    /**
     *
     * @param msg
     * @param ChannelId unique id
     */
    fun onMessageResponseServer(msg: T, ChannelId: String)

    /**
     * server开启成功
     */
    fun onStartServer()

    /**
     * server关闭
     */
    fun onStopServer()

    /**
     * 与客户端建立连接
     *
     * @param channel
     */
    fun onChannelConnect(channel: Channel)

    /**
     * 与客户端断开连接
     * @param
     */
    fun onChannelDisConnect(channel: Channel)
}