package com.wj.makebai.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.Tools
import com.wj.commonlib.data.mode.FoundMode
import com.wj.ktutils.startActivity
import com.wj.makebai.R
import com.wj.makebai.ui.activity.comm.WebActivity
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.found_item_layout.view.*

/**
 * 发现适配
 * @author dchain
 * @version 1.0
 * @date 2019/10/12
 */
class FoundAdapter() : BaseAdapter() {
    var list = ArrayList<FoundMode>()

    constructor(list: ArrayList<FoundMode>) : this() {
        this.list = list
    }


    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val mode = list[position]

        val width = Tools.getScreenWH(context!!)[0]
        holder.itemView.image.layoutParams.height = (width * 0.5).toInt()

        holder.itemView.layoutParams.height = (width * 0.75).toInt()

        glide!!.load(mode.image).into(holder.itemView.image)
        holder.itemView.title.text = mode.title
        holder.itemView.desc.text = mode.describe
        holder.itemView.date.text = mode.creatime.split(" ")[0]

        holder.itemView.setOnClickListener {
            if (mode.key == "web") {
                context!!.startActivity<WebActivity>("url" to mode.value)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.found_item_layout, parent, false))
    }
}