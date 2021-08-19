package com.wj.makebai.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wj.makebai.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.series_item_layout.view.*


/**
 * 可以播放的剧集
 * @author dchain
 * @version 1.0
 * @date 2019/9/11
 */
class SeriesAdapter() : BaseAdapter() {
    var list = ArrayList<String>()
    var  choose=0

    constructor(list: ArrayList<String>) : this() {
        this.list = list
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val mode = list[position]
        val match=Regex("/\\[(.+?)\\]/g")
        holder.itemView.text.text = match.replace(mode,"")
        holder.itemView.setOnClickListener {
            choose=position
            notifyDataSetChanged()
            onItemClickListener?.click(it, position)
        }
        holder.itemView.text.isSelected = position==choose
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.series_item_layout, parent, false))
    }
}