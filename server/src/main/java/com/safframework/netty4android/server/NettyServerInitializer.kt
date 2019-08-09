package com.safframework.netty4android.server

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.util.CharsetUtil


/**
 *
 * @FileName:
 *          com.safframework.netty4android.server.NettyServerInitializer
 * @author: Tony Shen
 * @date: 2019-08-06 15:51
 * @version: V1.0 <描述当前版本功能>
 */
class NettyServerInitializer(private val mListener: NettyServerListener<String>,private val webSocketPath:String) : ChannelInitializer<SocketChannel>() {

    @Throws(Exception::class)
    public override fun initChannel(ch: SocketChannel) {

        val pipeline = ch.pipeline()

        pipeline.addLast("active",ChannelActiveHandler(mListener))
        pipeline.addLast("socketChoose", SocketChooseHandler(webSocketPath))

        pipeline.addLast("string_encoder",StringEncoder(CharsetUtil.UTF_8))
        pipeline.addLast("linebased",LineBasedFrameDecoder(1024))
        pipeline.addLast("string_decoder",StringDecoder(CharsetUtil.UTF_8))
        pipeline.addLast("commonhandler", CustomerServerHandler(mListener))
    }
}