package com.wj.makebai.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wj.commonlib.data.mode.NovelPageItemMode
import com.wj.makebai.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.series_item_layout.view.*

/**
 * 漫画目录
 * @author dchain
 * @version 1.0
 * @date 2019/9/17
 */
class ComicSeriesAdapter() : BaseAdapter() {
    var list = ArrayList<NovelPageItemMode>()
    var choose = -1
    var headerView: View? = null

    constructor(list: ArrayList<NovelPageItemMode>) : this() {
        this.list = list
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position != 0)  {
            val mode = list[position]
            val match = Regex("/\\[(.+?)\\]/g")
            holder.itemView.text.text = match.replace(mode.title, "")
            holder.itemView.setOnClickListener {
                choose = position
                notifyDataSetChanged()
                onItemClickListener?.click(it, position)
            }
            holder.itemView.text.isSelected = position == choose
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(
            if (viewType == 1) headerView!! else
                inflater!!.inflate(
                    R.layout.series_item_layout,
                    parent,
                    false
                )
        )
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 1 else super.getItemViewType(position)
    }
}