package com.wj.makebai.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Paint
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.AbDoubleTool
import com.wj.commonlib.data.mode.VipCardMode
import com.wj.makebai.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.adapter_vipcard.view.*

/**
 *
 * @author admin
 * @version 1.0
 * @date 2020/12/21
 */
class VipCardAdapter() : BaseAdapter() {
    private var list = ArrayList<VipCardMode>()
    var choose = 0

    constructor(list: ArrayList<VipCardMode>) : this() {
        this.list = list
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val mode = list[position]
        holder.itemView.payTitle.text = mode.desc
        holder.itemView.setOnClickListener { choose = position;notifyDataSetChanged() }
        if(mode.discountNum!=0.0 || mode.discountNum!=1.0 ){
            holder.itemView.payMoney.setMoney("${AbDoubleTool.mul(mode.payMoney,mode.discountNum)}元")
            holder.itemView.payMoney1.setMoney("${mode.payMoney}元")
            holder.itemView.payMoney1.paint.flags = Paint. STRIKE_THRU_TEXT_FLAG
            holder.itemView.payMoney1.paint.isAntiAlias=true
        }else  {
            holder.itemView.payMoney.setMoney("${mode.payMoney}元")
            holder.itemView.payMoney1.text=""
        }
        if (position == choose) {
            holder.itemView.setBackgroundResource(R.drawable.shape_vipcard1)
            holder.itemView.payMoney.setTextColor(context!!.resources.getColor(R.color.white))
            holder.itemView.payTitle.setTextColor(context!!.resources.getColor(R.color.white))
        } else {
            holder.itemView.setBackgroundResource(R.drawable.shape_vipcard)
            holder.itemView.payMoney.setTextColor(context!!.resources.getColor(R.color.yellow))
            holder.itemView.payTitle.setTextColor(context!!.resources.getColor(R.color.yellow))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.adapter_vipcard, parent, false))
    }
}

/**
 * 处理金钱的显示。以小数点为界限
 */
fun TextView.setMoney(money: String) {
    val index = money.length - 1

    val span = SpannableStringBuilder(money)
    span.setSpan(
        RelativeSizeSpan(0.8f),
        index,
        span.length,
        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
    )
    text = span

}