package com.safframework.netty4android.ui.domain

import java.text.SimpleDateFormat

/**
 *
 * @FileName:
 *          com.safframework.netty4android.ui.domain.MessageBean
 * @author: Tony Shen
 * @date: 2019-08-05 23:18
 * @version: V1.0 <描述当前版本功能>
 */
class MessageBean(time: Long, var mMsg: String) {
    var mTime: String

    init {
        mTime = SimpleDateFormat("HH:mm:ss").format(time)
    }
}