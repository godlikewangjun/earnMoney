package com.wj.makebai.ui.adapter

import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.Tools
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.wj.commonlib.data.mode.WelfareMode
import com.wj.commonlib.http.HttpManager
import com.wj.ktutils.showTip
import com.wj.makebai.R
import com.wj.makebai.utils.MbTools
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.welfare_item_layout.view.*

/**
 * 福利列表
 * @author Administrator
 * @version 1.0
 * @date 2019/11/15
 */
class WelfareListAdapter() : BaseAdapter() {
    var list = ArrayList<WelfareMode>()

    constructor(list: ArrayList<WelfareMode>) : this() {
        this.list = list
    }


    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val mode = list[position]
        holder.itemView.image.layoutParams.height = Tools.getScreenWH(context)[0] / 3
        glide!!.load(mode.image).apply(RequestOptions.bitmapTransform(RoundedCorners(10)))
            .into(holder.itemView.image)
        holder.itemView.title.text = mode.title
        holder.itemView.conditions.text =
            String.format(context!!.resources.getString(R.string.get_the_conditions), mode.describe)
        holder.itemView.date.text = String.format(
            context!!.resources.getString(R.string.valid_time),
            if (mode.toDate != null) mode.toDate else context!!.resources.getString(R.string.long_time)
        )
        if (mode.hasGet) {
            holder.itemView.ok.setBackgroundResource(R.drawable.shape_enable_false)
            holder.itemView.ok.text = context!!.getString(R.string.has_get)
            holder.itemView.ok.isEnabled = false
        }
        holder.itemView.ok.setOnClickListener {
            if (MbTools.isLogin(context!!)) {
                HttpManager.getWelfare(mode.welfareid) {
                    context!!.showTip(context!!.getString(R.string.get_success))
                    mode.hasGet = true
                    notifyItemChanged(position)
                }
            }
        }
        holder.itemView.detail.setOnClickListener {
            AlertDialog.Builder(context!!).setTitle("详细说明").setMessage(mode.detail)
                .setPositiveButton(
                    "确定"
                    , null
                ).show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.welfare_item_layout, parent, false))
    }
}