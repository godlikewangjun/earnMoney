package com.wj.makebai.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wj.ktutils.runOnUiThread
import com.wj.makebai.R
import com.wj.makebai.data.db.AppDatabase
import com.wj.makebai.data.mode.db.SearchHistoryMode
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.adapter_search_tag.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 *
 * @author admin
 * @version 1.0
 * @date 2021/1/29
 */
class SearchTagsAdapter:BaseAdapter() {
    var list = ArrayList<SearchHistoryMode>()
    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.text.text = list[position].name
        holder.itemView.delete.setOnClickListener{
            GlobalScope.launch(Dispatchers.IO){
                AppDatabase.db.searchHistoryDao().delete( list[position])
                context!!.runOnUiThread {
                    list.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
        }
        holder.itemView.setOnClickListener{
            notifyDataSetChanged()
            onItemClickListener?.click(it,position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(inflater!!.inflate(R.layout.adapter_search_tag, parent, false))
    }
}