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
 *          com.safframework.netty4android.ui.adapter.MessageAdapter
 * @author: Tony Shen
 * @date: 2019-08-05 23:18
 * @version: V1.0 <描述当前版本功能>
 */
class MessageAdapter : RecyclerView.Adapter<MessageAdapter.ItemHolder>() {

    val dataList: MutableList<MessageBean> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.log_item, parent, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val bean = dataList[position]

        holder.mTime.text = bean.mTime
        holder.mLog.text = bean.mMsg

        holder.itemView.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(v: View): Boolean {
                val cmb = v.getContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val msgBean = dataList[holder.adapterPosition]
                val msg = msgBean.mTime + " " + msgBean.mMsg
                cmb.setPrimaryClip(ClipData.newPlainText(null, msg))
                Toast.makeText(v.getContext(), "已复制到剪贴板", Toast.LENGTH_LONG).show()
                return true
            }
        })
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTime: TextView
        var mLog: TextView

        init {
            mTime = itemView.findViewById(R.id.time)
            mLog = itemView.findViewById(R.id.logtext)
        }
    }
}