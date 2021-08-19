package com.wj.makebai.ui.adapter

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wj.makebai.R
import com.wj.makebai.data.mode.WatchNovelMode
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.novel_loading_next_layout.view.*
import kotlinx.android.synthetic.main.watchnovel_content_layout.view.*


/**
 * 小说阅读适配
 * @author dchain
 * @version 1.0
 * @date 2019/9/23
 */
class WatchNovelAdapter() : BaseAdapter() {
    var list = ArrayList<WatchNovelMode>()
    var index = -1//当前看的第几话
    var total = -1//一共几话
    var color = 0//背景色
    var fontSize = 0//背景色
    /**
     * 是否加载完成
     */
    var isFinish: Boolean = false
        set(value) {
            field = value
            if (itemCount > 1) {
                notifyItemChanged(itemCount - 1)
            }
        }

    constructor(list: ArrayList<WatchNovelMode>) : this() {
        this.list = list
    }

    override fun getItemCount(): Int {
        return if (list.size > 0) list.size + 1 else 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position != itemCount - 1) {
            val mode = list[position]
            holder.itemView.tv_title.text = mode.title
            holder.itemView.text.text = mode.content
            if (color != 0) {
                holder.itemView.setBackgroundColor(color)
                if(isLightColor(color)){
                    holder.itemView.tv_title.setTextColor(context!!.resources.getColor(R.color.text))
                    holder.itemView.text.setTextColor(context!!.resources.getColor(R.color.text))
                }else{
                    holder.itemView.text.setTextColor(context!!.resources.getColor(R.color.text_white))
                    holder.itemView.tv_title.setTextColor(context!!.resources.getColor(R.color.text_white))
                }
            }
            if(fontSize!=0){
                holder.itemView.tv_title.textSize= fontSize.toFloat()+5
                holder.itemView.text.textSize= fontSize.toFloat()
            }
        } else {
            if (isFinish) {
                holder.itemView.novel_loading_next.visibility = View.GONE
                holder.itemView.novel_no_more.visibility = View.VISIBLE
            } else {
                holder.itemView.novel_loading_next.visibility = View.VISIBLE
                holder.itemView.novel_no_more.visibility = View.GONE
            }
            if (color != 0) {
                holder.itemView.setBackgroundColor(color)
                if(isLightColor(color)){
                    holder.itemView.text1.setTextColor(context!!.resources.getColor(R.color.text))
                }else{
                    holder.itemView.text1.setTextColor(context!!.resources.getColor(R.color.text_white))
                }
            }
        }
    }
    private fun isLightColor(color: Int): Boolean {
        val darkness =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness < 0.5
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return when {
            viewType != -1 -> CustomVhoder(
                inflater!!.inflate(
                    R.layout.watchnovel_content_layout,
                    parent,
                    false
                )
            )
            else -> CustomVhoder(
                inflater!!.inflate(
                    R.layout.novel_loading_next_layout,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position < list.size) super.getItemViewType(position) else return -1
        return super.getItemViewType(position)
    }
}