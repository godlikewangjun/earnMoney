package com.wj.makebai.ui.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.abase.view.weight.RecyclerSpace
import com.wj.commonlib.data.mode.*
import com.wj.commonlib.ui.weight.CustomGridManager
import com.wj.makebai.R
import com.wj.makebai.data.mode.VipMovieDetailTypeMode
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import com.wj.ui.interfaces.RecyerViewItemListener
import kotlinx.android.synthetic.main.moviedetail_playlist_layout.view.*
import kotlinx.android.synthetic.main.moviedetail_playlist_layout.view.sort
import kotlinx.android.synthetic.main.novel_header_item_layout.view.*
import kotlinx.android.synthetic.main.vipmoviedetail_item_layout.view.*
import kotlinx.android.synthetic.main.vipmoviedetail_item_layout.view.desc

/**
 * 影视详情
 * @author dchain
 * @version 1.0
 * @date 2019/9/16
 */
class VipMoviesDetailAdapter : BaseAdapter() {
    //播放点击
    var playClick: RecyerViewItemListener? = null
    var index =0

    enum class VipMovieDetailTypes {
        DETAIL, PLAY_LIST
    }

    var list = ArrayList<VipMovieDetailTypeMode>()

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val mode = list[position]
        when (mode.type) {
            VipMovieDetailTypes.PLAY_LIST -> {
                if (holder.itemView.recyclerView.adapter != null) return
                val plays = (mode.mode as ArrayList<VipMovieItemMode>).toArrayString()
                holder.itemView.sort.visibility= if (plays.size>50) View.VISIBLE else View.GONE
                val adapter = SeriesAdapter(plays)
                holder.itemView.sort.setOnClickListener {
                    holder.itemView.sort.isSelected = !holder.itemView.isSelected
                    adapter.list = adapter.list.reversed() as ArrayList
                    if (adapter.choose != -1) {
                        adapter.choose =
                            adapter.list.size - adapter.choose
                    }
                    adapter.notifyDataSetChanged()
                }
                adapter.choose=index
                holder.itemView.recyclerView.adapter = adapter
                holder.itemView.recyclerView.layoutManager =
                    CustomGridManager(context, if (plays.size > 1) 4 else 2)
                holder.itemView.recyclerView.addItemDecoration(RecyclerSpace(20))

                adapter.onItemClickListener = object : RecyerViewItemListener {
                    override fun click(view: View, position: Int) {
                        playClick?.click(view, position)
                    }
                }
            }
            VipMovieDetailTypes.DETAIL -> {
                val detailMode = mode.mode as VipMovieItem
                holder.itemView.title.text = detailMode.title
                holder.itemView.date.text = String.format(
                    context!!.resources.getString(R.string.movie_release),
                    detailMode.updateTime
                )
                holder.itemView.desc.text = String.format(
                    context!!.resources.getString(R.string.movie_introduce),
                    detailMode.descs
                )
                holder.itemView.desc.setOnClickListener {
                    holder.itemView.desc.isSelected=!holder.itemView.desc.isSelected
                    if(holder.itemView.desc.isSelected){
                        holder.itemView.desc.maxLines=100
                    }else{
                        holder.itemView.desc.maxLines=2
                    }
                }
            }
        }
    }

    private fun ArrayList<VipMovieItemMode>.toArrayString(): ArrayList<String> {
        val strs = ArrayList<String>()
        for (index in this) {
            strs.add(index.title)
        }
        return strs
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(
            when (VipMovieDetailTypes.values()[viewType]) {
                VipMovieDetailTypes.PLAY_LIST -> inflater!!.inflate(//播放集数
                    R.layout.moviedetail_playlist_layout,
                    parent,
                    false
                )
                VipMovieDetailTypes.DETAIL -> inflater!!.inflate(
                    R.layout.vipmoviedetail_item_layout,
                    parent,
                    false
                )
            }
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].type!!.ordinal
    }
}