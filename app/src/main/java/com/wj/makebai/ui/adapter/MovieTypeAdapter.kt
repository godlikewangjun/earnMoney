package com.wj.makebai.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wj.commonlib.data.mode.MovieTypeMode
import com.wj.makebai.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.movietype_item_layout.view.*

/**
 * 电影类型分类
 * @author Administrator
 * @version 1.0
 * @date 2019/12/23
 */
class MovieTypeAdapter() : BaseAdapter() {
    var list = ArrayList<MovieTypeMode>()//按照类型分类
    var choose = -1

    constructor(movieTypes: ArrayList<MovieTypeMode>) : this() {
        this.list = movieTypes
    }


    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val mode = list[position]
        holder.itemView.title.text = mode.name
        if (choose == position)
            holder.itemView.title.setTextColor(context!!.resources.getColor(R.color.colorPrimary))
        else
            holder.itemView.title.setTextColor(context!!.resources.getColor(R.color.black))

        holder.itemView.setOnClickListener {
            choose = if (choose != position) position else -1
            notifyDataSetChanged()
            onItemClickListener?.click(it, position)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.movietype_item_layout, parent, false))
    }
}