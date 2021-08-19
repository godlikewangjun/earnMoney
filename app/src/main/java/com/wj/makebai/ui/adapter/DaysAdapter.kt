package com.wj.makebai.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wj.makebai.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.days_item_layout.view.*

/**
 * 签到的天数
 * @author Administrator
 * @version 1.0
 * @date 2019/11/14
 */
class DaysAdapter : BaseAdapter() {
    var days = 0
    var isSign = false
    override fun getItemCount(): Int {
        return 7
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.view1.visibility = View.VISIBLE
        if (position == itemCount - 1) {
            holder.itemView.img_day.setImageResource(R.drawable.ic_lw)
            holder.itemView.view1.visibility = View.GONE
        }
        if (days-1 == position && isSign) {
            holder.itemView.day.text = context!!.getString(R.string.today)
        }else if(days == position && !isSign){
            holder.itemView.day.text = context!!.getString(R.string.today)
        } else {
            holder.itemView.day.text =
                String.format(context!!.getString(R.string.sign_count_day), position + 1)
        }
        holder.itemView.img_day.isSelected = position < days || (isSign && position == days-1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.days_item_layout, parent, false))
    }
}