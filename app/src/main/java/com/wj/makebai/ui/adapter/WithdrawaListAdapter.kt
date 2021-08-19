package com.wj.makebai.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.Tools
import com.wj.commonlib.data.mode.WithdrawalMode
import com.wj.ktutils.isNull
import com.wj.makebai.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.withdrawalist_item_layout.view.*

/**
 * 提现兑换的列表
 * @author Administrator
 * @version 1.0
 * @date 2019/11/28
 */
class WithdrawaListAdapter() :BaseAdapter(){
    var list=ArrayList<WithdrawalMode>()

    constructor(list: ArrayList<WithdrawalMode>) : this() {
        this.list = list
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val mode=list[position]
        val string=mode.withdrawal_name+":  "
        holder.itemView.title.text = string
        holder.itemView.desc.visibility= View.GONE
        when(mode.withdrawal_state){
            0-> {
                holder.itemView.title.append("兑换中")
                Tools.changTextViewColor(holder.itemView.title,string.length,holder.itemView.title.length(),context!!.resources.getColor(R.color.text))
            }
            1-> {
                holder.itemView.title.append("正在审核")
                Tools.changTextViewColor(holder.itemView.title,string.length,holder.itemView.title.length(),context!!.resources.getColor(R.color.black))
            }
            2-> {
                holder.itemView.title.append("兑换成功")
                Tools.changTextViewColor(holder.itemView.title,string.length,holder.itemView.title.length(),context!!.resources.getColor(R.color.green))
            }
            3-> {
                holder.itemView.title.append("账号异常拒绝受理")
                Tools.changTextViewColor(holder.itemView.title,string.length,holder.itemView.title.length(),context!!.resources.getColor(R.color.yellow))
                if(!mode.describe.isNull())holder.itemView.desc.text=mode.describe else holder.itemView.desc.text="请仔细阅读提现规则"
                holder.itemView.desc.visibility= View.VISIBLE
            }
            4-> {
                holder.itemView.title.append("不再受理")
                Tools.changTextViewColor(holder.itemView.title,string.length,holder.itemView.title.length(),context!!.resources.getColor(R.color.red))
            }
        }
        holder.itemView.create_date.text = mode.creatime
        holder.itemView.user_points.text = if(mode.withdrawal_money>0){
            "+"+String.format(
                context!!.getString(R.string.show_money),
                mode.withdrawal_money.toString()
            )
        }else{
            String.format(
                context!!.getString(R.string.show_money),
                mode.withdrawal_money.toString()
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.withdrawalist_item_layout,parent,false))
    }
}