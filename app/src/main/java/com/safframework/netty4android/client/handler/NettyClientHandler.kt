package com.safframework.netty4android.client.handler

import android.util.Log
import com.safframework.netty4android.client.constant.ConnectState
import com.safframework.netty4android.client.listener.NettyClientListener
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent

/**
 *
 * @FileName:
 *          com.safframework.netty4android.client.handler.NettyClientHandler
 * @author: Tony Shen
 * @date: 2019-08-05 21:00
 * @version: V1.0 <描述当前版本功能>
 */
class NettyClientHandler(private val listener: NettyClientListener<String>, private val index: Int, private val isSendheartBeat: Boolean, private val heartBeatData: Any?) : SimpleChannelInboundHandler<String>() {

    /**
     *
     * 设定IdleStateHandler心跳检测每x秒进行一次读检测，
     * 如果x秒内ChannelRead()方法未被调用则触发一次userEventTrigger()方法
     *
     * @param ctx ChannelHandlerContext
     * @param evt IdleStateEvent
     */
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {

        if (evt is IdleStateEvent) {
            if (evt.state() == IdleState.WRITER_IDLE) {   //发送心跳

                if (isSendheartBeat) {
                    if (heartBeatData == null) {

                        ctx.channel().writeAndFlush("Heartbeat" + System.getProperty("line.separator")!!)
                    } else {

                        if (heartBeatData is String) {
                            Log.d(TAG, "userEventTriggered: String")
                            ctx.channel().writeAndFlush(heartBeatData + System.getProperty("line.separator")!!)
                        } else if (heartBeatData is ByteArray) {
                            Log.d(TAG, "userEventTriggered: byte")
                            val buf = Unpooled.copiedBuffer((heartBeatData as ByteArray?)!!)
                            ctx.channel().writeAndFlush(buf)
                        } else {

                            Log.d(TAG, "userEventTriggered: heartBeatData type error")
                        }
                    }
                } else {
                    Log.d(TAG, "不发送心跳")
                }
            }
        }
    }

    /**
     *
     * 客户端上线
     *
     * @param ctx ChannelHandlerContext
     */
    override fun channelActive(ctx: ChannelHandlerContext) {

        Log.d(TAG, "channelActive")
        listener.onClientStatusConnectChanged(ConnectState.STATUS_CONNECT_SUCCESS, index)
    }

    /**
     *
     * 客户端下线
     *
     * @param ctx ChannelHandlerContext
     */
    override fun channelInactive(ctx: ChannelHandlerContext) {

        Log.d(TAG, "channelInactive")
    }

    /**
     * 客户端收到消息
     *
     * @param channelHandlerContext ChannelHandlerContext
     * @param msg                   消息
     */
    override fun channelRead0(channelHandlerContext: ChannelHandlerContext, msg: String) {

        Log.d(TAG, "channelRead0:")
        listener.onMessageResponseClient(msg, index)
    }

    /**
     * @param ctx   ChannelHandlerContext
     * @param cause 异常
     */
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {

        Log.e(TAG, "exceptionCaught")
        listener.onClientStatusConnectChanged(ConnectState.STATUS_CONNECT_ERROR, index)
        cause.printStackTrace()
        ctx.close()
    }

    companion object {

        private val TAG = "NettyClientHandler"
    }
}
