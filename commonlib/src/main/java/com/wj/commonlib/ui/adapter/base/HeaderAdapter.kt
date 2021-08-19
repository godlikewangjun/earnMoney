package com.wj.commonlib.ui.adapter.base

import android.view.View
import android.view.ViewGroup
import com.wj.commonlib.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.commlib_footview_layout.view.*

/**
 *
 * @author Admin
 * @version 1.0
 * @date 2018/4/4
 */
abstract class HeaderAdapter : BaseAdapter() {
    var count: Int = 0//数据数量

    /**
     * 头
     */
    private val HEADER: Int = 2
    /**
     * 加载更多
     */
    private val LOADMORE: Int = 3
    /**
     * 是否加载完成
     */
    var isFinsh: Boolean = false
        set(value) {
            field = value
            if(itemCount>1){
                notifyItemChanged(itemCount - 1)
            }
        }
    //list头
    var headerView: View? = null

    override fun getItemCount(): Int {
        return if (count > 0) count + 2 else if(count==0) count+1 else 0
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (position in 1..count) bindVH(holder, position - 1) else if(position==itemCount-1) {
            if (isFinsh) {
                holder.itemView.foot_loading.visibility=View.GONE
                holder.itemView.foot_finsh.visibility=View.VISIBLE
            }else{
                holder.itemView.foot_loading.visibility=View.VISIBLE
                holder.itemView.foot_finsh.visibility=View.GONE
            }
        }
        holder.itemView.setOnClickListener {
            if (onItemClickListener != null) {
                if (position in 1..count) {
                    onItemClickListener!!.click(holder.itemView, position - 1)
                }
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
    abstract fun creatdVH(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return when (viewType) {
            0 -> creatdVH(parent, viewType)
            HEADER -> CustomVhoder(headerView!!)
            else -> {
                val footView = inflater!!.inflate(R.layout.commlib_footview_layout, parent, false)
                if (isFinsh) {
                    footView.findViewById<View>(R.id.foot_loading).visibility = View.GONE
                    footView.findViewById<View>(R.id.foot_finsh).visibility = View.VISIBLE
                } else {
                    footView.findViewById<View>(R.id.foot_loading).visibility = View.VISIBLE
                    footView.findViewById<View>(R.id.foot_finsh).visibility = View.GONE
                }
                CustomVhoder(footView)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position > 0 && position < itemCount - 1) super.getItemViewType(position) else if (position == 0) HEADER else LOADMORE
    }
}