package com.wj.makebai.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wj.commonlib.data.mode.BillMode
import com.wj.commonlib.utils.CommTools
import com.wj.ktutils.isNull
import com.wj.makebai.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.payment_item_layout.view.*

/**
 * 收支明细
 * @author Administrator
 * @version 1.0
 * @date 2019/11/21
 */
class PaymentDetailsAdapter() : BaseAdapter() {
    private var list = ArrayList<BillMode>()

    constructor(list: ArrayList<BillMode>) : this() {
        this.list = list
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val mode = list[position]
        holder.itemView.title.text =mode.bill_desc
        holder.itemView.create_date.text = mode.bill_creatime
        holder.itemView.user_points.text = if(mode.bill_money>0){
            "+"+String.format(
                context!!.getString(R.string.show_points),
                CommTools.price2Point(mode.bill_money).toString()
            )
        }else{
            String.format(
                context!!.getString(R.string.show_points),
                CommTools.price2Point(mode.bill_money).toString()
            )
        }
        when {
            mode.bill_money > 0 -> holder.itemView.user_points.setTextColor(context!!.resources.getColor(R.color.colorPrimary))
            mode.bill_money < 0 -> holder.itemView.user_points.setTextColor(context!!.resources.getColor(R.color.red))
            else -> holder.itemView.user_points.setTextColor(context!!.resources.getColor(R.color.text_gray))
        }

        if (mode.bill_chang_points != null) holder.itemView.user_balance.text = String.format(
            context!!.getString(R.string.show_ye),
            mode.bill_chang_points.toInt()
        )
        if(!mode.bill_ad_icon.isNull()){
            glide!!.load(mode.bill_ad_icon).into(holder.itemView.ad_icon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.payment_item_layout, parent, false))
    }
}