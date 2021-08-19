package com.wj.makebai.ui.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wj.commonlib.data.mode.NovelPageItemMode
import com.wj.makebai.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.series_item_layout.view.*

/**
 * 目录
 * @author dchain
 * @version 1.0
 * @date 2019/9/19
 */
class DirectoryAdapter() : BaseAdapter() {
    var list = ArrayList<NovelPageItemMode>()
    private var choose = 0

    constructor(list: ArrayList<NovelPageItemMode>) : this() {
        this.list = list
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val mode = list[position]
        holder.itemView.text.text = mode.title
        holder.itemView.setOnClickListener {
            choose = position
            notifyDataSetChanged()
            onItemClickListener?.click(it, position)
        }
        holder.itemView.text.isSelected = position == choose
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.directory_item_layout, parent, false))
    }
}