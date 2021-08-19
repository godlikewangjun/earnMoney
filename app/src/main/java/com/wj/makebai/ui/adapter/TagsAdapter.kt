package com.wj.makebai.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wj.makebai.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import com.xiaomi.push.it
import kotlinx.android.synthetic.main.adapter_tag.view.*

/**
 *
 * @author admin
 * @version 1.0
 * @date 2021/1/29
 */
class TagsAdapter:BaseAdapter() {
    var list = ArrayList<String>()
    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.text.text = list[position]
        holder.itemView.setOnClickListener{
            notifyDataSetChanged()
            onItemClickListener?.click(it,position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.adapter_tag, parent, false))
    }
}