package com.wj.makebai.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wj.commonlib.data.mode.LuckyRecodeMode
import com.wj.makebai.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.luckyrecode_item_layout.view.*

/**
 * 抽奖记录
 * @author Administrator
 * @version 1.0
 * @date 2019/11/21
 */
class LuckyRecodeAdapter():BaseAdapter() {
    private var list=ArrayList<LuckyRecodeMode>()

    constructor(list: ArrayList<LuckyRecodeMode>) : this() {
        this.list = list
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val mode=list[position]
        holder.itemView.title.text= String.format(context!!.getString(R.string.lucky_title),mode.luckyTitle)
        holder.itemView.create_date.text=mode.createtime
        holder.itemView.user_points.text= String.format(context!!.getString(R.string.show_points),mode.points)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.luckyrecode_item_layout,parent,false))
    }
}