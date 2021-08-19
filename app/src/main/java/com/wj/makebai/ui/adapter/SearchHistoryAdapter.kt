package com.wj.makebai.ui.adapter

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wj.ktutils.db.delete
import com.wj.ktutils.runOnUiThread
import com.wj.makebai.R
import com.wj.makebai.data.db.AppDatabase
import com.wj.makebai.data.mode.db.SearchHistoryMode
import com.wj.makebai.data.db.MySqlHelper
import com.wj.makebai.data.db.TypesEnum
import com.wj.makebai.data.db.database
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.searchhistory_item_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 历史搜索
 * @author Administrator
 * @version 1.0
 * @date 2019/9/2
 */
class SearchHistoryAdapter() : BaseAdapter() {
    var list = ArrayList<SearchHistoryMode>()

    constructor(list: ArrayList<SearchHistoryMode>) : this() {
        this.list = list
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (position == 0) {
            holder.itemView.text.gravity = Gravity.CENTER
            holder.itemView.text.text = context!!.resources.getString(R.string.clear_search)
            holder.itemView.text.setTextColor(context!!.resources.getColor(R.color.text_gray))
            holder.itemView.delete.visibility = View.GONE
        } else {
            holder.itemView.text.gravity = Gravity.START
            holder.itemView.delete.visibility = View.VISIBLE
            holder.itemView.text.setTextColor(context!!.resources.getColor(R.color.text))

            val mode = list[position - 1]
            holder.itemView.text.text = mode.name
            holder.itemView.delete.setOnClickListener {

                GlobalScope.launch(Dispatchers.IO){
                    AppDatabase.db.searchHistoryDao().delete( list[position-1])
                    context!!.runOnUiThread {
                        list.remove(mode)
                        notifyDataSetChanged()
                    }
                }
            }
        }
        //设置点击事件
        holder.itemView.setOnClickListener {
            if (position == 0) {
                context!!.database.use {
                    delete(MySqlHelper.SEARCH_TABLE)
                    list.clear()
                    notifyDataSetChanged()
                }
            } else {
                onItemClickListener?.click(it, position - 1)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.searchhistory_item_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return if (list.size > 0) list.size + 1 else 0
    }
}