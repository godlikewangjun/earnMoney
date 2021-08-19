package com.wj.makebai.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wj.commonlib.data.mode.UserMode
import com.wj.makebai.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.hasbind_item_layout.view.*

/**
 * 邀请绑定列表
 * @author admin
 * @version 1.0
 * @date 2020/1/7
 */
class HasBindAdapter() : BaseAdapter() {
    private var list = ArrayList<UserMode>()

    constructor(list: ArrayList<UserMode>) : this() {
        this.list = list
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val mode = list[position]
        holder.itemView.user_name.text = mode.username
        holder.itemView.user_createDate.text = mode.creatime
        Glide.with(context!!).load(mode.usericon)
            .apply(RequestOptions.circleCropTransform()).into(holder.itemView.user_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.hasbind_item_layout, parent, false))
    }
}