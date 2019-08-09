package com.safframework.netty4android.ui.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.safframework.netty4android.R
import com.safframework.netty4android.ui.domain.MessageBean

/**
 *
 * @FileName:
 *          com.aihuishou.creative.netty4android.ui.adapter.MessageAdapter
 * @author: Tony Shen
 * @date: 2019-08-05 19:42
 * @version: V1.0 <描述当前版本功能>
 */
class MessageAdapter : RecyclerView.Adapter<MessageAdapter.ItemHolder>() {

    val dataList: MutableList<MessageBean> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val bean = dataList[position]

        holder.mTime.text = bean.mTime
        holder.mMsg.text = bean.mMsg

        holder.itemView.setOnLongClickListener(View.OnLongClickListener { v ->
            val cmb = v.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val msgBean = dataList[holder.getAdapterPosition()]
            val msg = msgBean.mTime + " " + msgBean.mMsg
            cmb.primaryClip = ClipData.newPlainText(null, msg)
            Toast.makeText(v.context, "已复制到剪贴板", Toast.LENGTH_LONG).show()
            true
        })
    }

    override fun getItemCount() = dataList.size

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTime: TextView
        var mMsg: TextView

        init {
            mTime = itemView.findViewById(R.id.time) as TextView
            mMsg = itemView.findViewById(R.id.message) as TextView
        }
    }
}