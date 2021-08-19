package com.wj.makebai.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.AbDoubleTool
import com.wj.commonlib.data.mode.ExChangProduct
import com.wj.makebai.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.exchange_product_item_layout.view.*

/**
 * 兑换的产品
 * @author Administrator
 * @version 1.0
 * @date 2019/11/16
 */
class ExchangeProjectAdapter() : BaseAdapter() {
    var list=ArrayList<ExChangProduct>()
    var choose = 0
    var sale =1.0

    constructor(list: ArrayList<ExChangProduct>) : this() {
        this.list = list
    }


    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val mode=list[position]
        if (choose == position) {
            holder.itemView.setBackgroundResource(R.drawable.shape_yellow_line)
            holder.itemView.money.setTextColor(context!!.resources.getColor(R.color.yellow1))
            holder.itemView.points.setTextColor(context!!.resources.getColor(R.color.yellow1))
        }else{
            holder.itemView.setBackgroundResource(R.drawable.shape_gray_line)
            holder.itemView.money.setTextColor(context!!.resources.getColor(R.color.text))
            holder.itemView.points.setTextColor(context!!.resources.getColor(R.color.text_gray))
        }
        holder.itemView.money.text=mode.withdrawal_name
        val value=AbDoubleTool.mul(mode.withdrawal_point*100.toDouble(),mode.withdrawal_sale)
        holder.itemView.points.text= String.format(context!!.getString(R.string.exchange_points),AbDoubleTool.mul(value,sale).toInt())
        holder.itemView.setOnClickListener {
            if(choose==position)return@setOnClickListener
            choose=position
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(
            inflater!!.inflate(
                R.layout.exchange_product_item_layout,
                parent,
                false
            )
        )
    }
}