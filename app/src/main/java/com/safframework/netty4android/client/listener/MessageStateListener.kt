package com.safframework.netty4android.client.listener

/**
 *
 * @FileName:
 *          com.safframework.netty4android.client.listener.MessageStateListener
 * @author: Tony Shen
 * @date: 2019-08-05 20:59
 * @version: V1.0 <描述当前版本功能>
 */
interface MessageStateListener {

    fun isSendSuccss(isSuccess: Boolean)
}