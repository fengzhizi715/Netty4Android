package com.safframework.netty4android.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.safframework.netty4android.ui.domain.ClientChanel

/**
 *
 * @FileName:
 *          com.safframework.netty4android.ui.adapter.CustomSpinnerAdapter
 * @author: Tony Shen
 * @date: 2019-08-05 20:01
 * @version: V1.0 <描述当前版本功能>
 */
class CustomSpinnerAdapter(private val mContext: Context, private val mData: List<ClientChanel>) : BaseAdapter() {

    private val mInflater: LayoutInflater

    init {
        mInflater = LayoutInflater.from(mContext)
    }

    override fun getCount() = mData.size

    override fun getItem(position: Int) = mData[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View
        val text: TextView

        if (convertView == null) {
            view = mInflater.inflate(android.R.layout.simple_spinner_item, parent, false)
        } else {
            view = convertView
        }

        text = view as TextView
        val item = getItem(position)
        text.text = item.clientIp

        return view
    }
}
