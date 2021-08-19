package com.wj.makebai.ui.adapter.task

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.wj.commonlib.data.mode.AppTaskMode
import com.wj.commonlib.ui.adapter.base.FootAdapter
import com.wj.makebai.R
import com.wj.makebai.data.mode.HomeTypeMode
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.taskstate_item_layout.view.*

/**
 * 任务审核状态的adapter
 * @author admin
 * @version 1.0
 * @date 2020/9/28
 */
class TaskStateListAdapter() :FootAdapter(){
    var list = ArrayList<AppTaskMode>()

    constructor(list: ArrayList<AppTaskMode>) : this() {
        this.list = list
    }


    override fun getItemCount(): Int {
        count=list.size
        return super.getItemCount()
    }

    @SuppressLint("SetTextI18n")
    override fun bindVH(holder: RecyclerView.ViewHolder, position: Int) {
        val mode = list[position]
        glide!!.load(mode.task_icon).apply(RequestOptions().circleCrop()).into(holder.itemView.image)
        holder.itemView.task_desc.text = mode.task_desc
        holder.itemView.task_price.text = "+ ${mode.task_point}金币"
        if(mode.audit_state>1) {
            holder.itemView.state.visibility=View.VISIBLE
            holder.itemView.state.text="原因:${mode.auditDesc}"
        }
        else holder.itemView.state.visibility=View.GONE

        holder.itemView.setOnClickListener { onItemClickListener?.click(it,position) }
    }

    override fun creatdVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CustomVhoder(inflater!!.inflate(R.layout.taskstate_item_layout,parent,false))
    }
}