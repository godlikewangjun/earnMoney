package com.wj.commonlib.ui.adapter.base

import android.view.View
import android.view.ViewGroup
import com.wj.commonlib.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.commlib_footview_layout.view.*

/**
 * 自带上拉加载更多
 * @author Admin
 * @version 1.0
 * @date 2018/1/3
 */

abstract class FootAdapter : BaseAdapter() {
    var count: Int = 0//数据数量

    /**
     * 加载更多
     */
    val LOADMORE: Int = -1
    var isLoading = false

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


    override fun getItemCount(): Int {
        return if (count > 0) count + 1 else 0
    }

    override fun onBindViewHolder(
        holder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)
        holder.itemView.setOnClickListener {
            if (onItemClickListener != null && position < itemCount - 1) {
                onItemClickListener!!.click(holder.itemView, position)
            }
        }
        if (position < itemCount - 1) bindVH(holder, position) else {
            if (isFinish) {
                holder.itemView.foot_loading.visibility = View.GONE
                holder.itemView.foot_finsh.visibility = View.VISIBLE
            } else {
                holder.itemView.foot_loading.visibility = View.VISIBLE
                holder.itemView.foot_finsh.visibility = View.GONE
            }
        }
    }

    /**
     * 重写绑定
     */
    abstract fun bindVH(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int)

    /**
     * 重写创建
     */
    abstract fun creatdVH(
        parent: ViewGroup,
        viewType: Int
    ): androidx.recyclerview.widget.RecyclerView.ViewHolder

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return if (viewType != LOADMORE) creatdVH(parent, viewType) else {
            val footView = inflater!!.inflate(R.layout.commlib_footview_layout, parent, false)
            if (isFinish) {
                footView.foot_loading.pauseAnimation()
                footView.foot_loading.visibility = View.GONE
                footView.foot_finsh.visibility = View.VISIBLE
            } else {
                footView.foot_loading.resumeAnimation()
                footView.foot_loading.visibility = View.VISIBLE
                footView.foot_finsh.visibility = View.GONE
            }
            CustomVhoder(footView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position < count) super.getItemViewType(position) else return LOADMORE
        return super.getItemViewType(position)
    }
}
