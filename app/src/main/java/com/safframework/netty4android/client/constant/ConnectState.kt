package com.safframework.netty4android.client.constant

/**
 *
 * @FileName:
 *          com.safframework.netty4android.client.constant.ConnectState
 * @author: Tony Shen
 * @date: 2019-08-05 21:01
 * @version: V1.0 <描述当前版本功能>
 */
class ConnectState {

    companion object {

        @JvmField
        val STATUS_CONNECT_ERROR = -1

        @JvmField
        val STATUS_CONNECT_CLOSED = 0

        @JvmField
        val STATUS_CONNECT_SUCCESS = 1
    }

}
