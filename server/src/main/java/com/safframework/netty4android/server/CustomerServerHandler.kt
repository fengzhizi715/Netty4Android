package com.safframework.netty4android.server

import android.util.Log
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.util.CharsetUtil
import io.netty.buffer.ByteBuf
import java.net.InetSocketAddress

/**
 *
 * @FileName:
 *          com.safframework.netty4android.server.CustomerServerHandler
 * @author: Tony Shen
 * @date: 2019-08-05 19:00
 * @version: V1.0 <描述当前版本功能>
 */
@ChannelHandler.Sharable
class CustomerServerHandler(private val mListener: NettyServerListener<String>) : SimpleChannelInboundHandler<Any>() {

    @Throws(Exception::class)
    override fun channelReadComplete(ctx: ChannelHandlerContext) {
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext,
                                 cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }

    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Any) {

        val buff = msg as ByteBuf
        val info = buff.toString(CharsetUtil.UTF_8)
        Log.d(TAG,"收到消息内容：$info")
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {

        if (msg is WebSocketFrame) {  // 处理 WebSocket 消息

            val webSocketInfo = (msg as TextWebSocketFrame).text().trim { it <= ' ' }

            Log.d(TAG, "收到WebSocketSocket消息：$webSocketInfo")

            mListener.onMessageResponseServer(webSocketInfo , ctx.channel().id().asShortText())
        } else if (msg is String){   // 处理 Socket 消息

            Log.d(TAG, "收到socket消息：$msg")

            mListener.onMessageResponseServer(msg, ctx.channel().id().asShortText())
        }
    }

    // 断开连接
    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)
        Log.d(TAG, "channelInactive")

        val reAddr = ctx.channel().remoteAddress() as InetSocketAddress
        val clientIP = reAddr.address.hostAddress
        val clientPort = reAddr.port

        Log.d(TAG,"连接断开：$clientIP : $clientPort")

        mListener.onChannelDisConnect(ctx.channel())
    }

    companion object {

        private val TAG = "CustomerServerHandler"
    }
}