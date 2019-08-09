package com.safframework.netty4android.ui.domain

import io.netty.channel.Channel

/**
 *
 * @FileName:
 *          com.safframework.netty4android.ui.domain.ClientChanel
 * @author: Tony Shen
 * @date: 2019-08-05 19:28
 * @version: V1.0 <描述当前版本功能>
 */
data class ClientChanel(var clientIp: String?    //客户端ip
                   , var channel: Channel?    //与客户端建立的通道
                   , var shortId: String?     //通道的唯一标示
)
