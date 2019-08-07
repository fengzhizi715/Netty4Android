package com.safframework.netty4android.server

import android.util.Log
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelHandler
import java.net.InetSocketAddress


/**
 *
 * @FileName:
 *          com.safframework.netty4android.server.ChannelActiveHandler
 * @author: Tony Shen
 * @date: 2019-08-07 10:30
 * @version: V1.0 <描述当前版本功能>
 */
@ChannelHandler.Sharable
class ChannelActiveHandler(var mListener: NettyServerListener<String>) : ChannelInboundHandlerAdapter() {

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {

        val insocket = ctx.channel().remoteAddress() as InetSocketAddress
        val clientIP = insocket.address.hostAddress
        val clientPort = insocket.port

        Log.i("ChannelActiveHandler","新的连接：$clientIP : $clientPort")
        mListener.onChannelConnect(ctx.channel())
    }

}