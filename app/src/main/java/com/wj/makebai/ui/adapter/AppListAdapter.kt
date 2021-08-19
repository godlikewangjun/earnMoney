package com.wj.makebai.ui.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.AbDoubleTool
import com.abase.view.weight.MyDialog
import com.bumptech.glide.request.RequestOptions
import com.wj.makebai.R
import com.wj.makebai.statice.StaticData
import com.wj.makebai.ui.control.TaskConctor
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import ebz.lsds.qamj.os.df.AppSummaryObject
import kotlinx.android.synthetic.main.applist_item_layout.view.*

/**
 *
 * @author dchain
 * @version 1.0
 * @date 2019/10/22
 */
class AppListAdapter() : BaseAdapter() {
    var list = ArrayList<AppSummaryObject>()
    private var dialog: AlertDialog? = null

    constructor(list: ArrayList<AppSummaryObject>) : this() {
        this.list = list
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val appSummaryObject = list[position]
        glide!!.load(appSummaryObject.iconUrl).apply(RequestOptions().skipMemoryCache(true))
            .into(holder.itemView.ad_icon)
        holder.itemView.ad_name.text = appSummaryObject.appName
        var allPoint = appSummaryObject.points
        if (appSummaryObject.extraTaskList != null) {
            for (index in 0 until appSummaryObject.extraTaskList.size()) {
                val ym = appSummaryObject.extraTaskList[index]
                allPoint += ym.points
            }
        }
        if(allPoint!=0)holder.itemView.ad_priced.text = "+${AbDoubleTool.mul(allPoint.toDouble(),StaticData.initMode!!.scale)} 积分"
        holder.itemView.ad_desc.text = appSummaryObject.adSlogan
        holder.itemView.setOnClickListener {
            if (dialog != null) dialog!!.cancel()
            dialog = null
            dialog = TaskConctor.cpaDialog(context!!, appSummaryObject)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.applist_item_layout,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }
}