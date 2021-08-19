package com.wj.makebai.ui.adapter

import android.annotation.SuppressLint
import android.text.Html
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wj.commonlib.data.mode.Message
import com.wj.commonlib.http.Urls
import com.wj.commonlib.ui.adapter.base.FootAdapter
import com.wj.ktutils.isNull
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.comm.WebActivity
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.message_item_layout.view.*


/**
 * 消息列表
 * @author Administrator
 * @version 1.0
 * @date 2019/12/25
 */
class MessageAdapter() : FootAdapter() {
    var list = ArrayList<Message>()

    constructor(list: ArrayList<Message>) : this() {
        this.list = list
    }

    override fun getItemCount(): Int {
        count=list.size
        return super.getItemCount()
    }

    @SuppressLint("SetTextI18n")
    override fun bindVH(holder: RecyclerView.ViewHolder, position: Int) {
        val mode = list[position]
        holder.itemView.tip_title.text = mode.title
        holder.itemView.tip_content.text = Html.fromHtml(mode.content)
        holder.itemView.tip_time.text=mode.creatime
        holder.itemView.tip_to_time.text="到期:"+if(mode.toDate.isNull()) "长期" else mode.toDate
        if (mode.type == "0") {
            holder.itemView.tip_type.text = "公告"
            holder.itemView.tip_type.setTextColor( context!!.resources.getColor(R.color.red))
        } else {
            holder.itemView.tip_type.text = "消息"
            holder.itemView.tip_type.setTextColor( context!!.resources.getColor(R.color.yellow1))
        }

        holder.itemView.setOnClickListener {
            context!!.startActivity<WebActivity>("url" to Urls.announcement + mode.messageid)
        }
    }

    override fun creatdVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CustomVhoder(inflater!!.inflate(R.layout.message_item_layout, parent, false))
    }

}