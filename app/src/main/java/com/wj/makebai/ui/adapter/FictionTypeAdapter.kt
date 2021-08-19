package com.wj.makebai.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wj.makebai.R
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.adapter_fictiontype.view.*

/**
 *
 * @author admin
 * @version 1.0
 * @date 2021/1/29
 */
class FictionTypeAdapter : BaseAdapter() {
    var list = ArrayList<String>()
    var choose = 0
    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.text.text = list[position]
        holder.itemView.text.isSelected = position == choose
        holder.itemView.setOnClickListener{
            choose=position
            notifyDataSetChanged()
            onItemClickListener?.click(it,position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.adapter_fictiontype, parent, false))
    }
}