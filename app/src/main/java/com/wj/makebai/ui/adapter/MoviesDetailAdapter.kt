package com.wj.makebai.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.abase.view.weight.RecyclerSpace
import com.wj.commonlib.ui.weight.CustomGridManager
import com.wj.makebai.R
import com.wj.makebai.data.mode.MovieDetailTypeMode
import com.wj.makebai.data.mode.MoviesMode
import com.wj.makebai.ui.control.CommControl
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import com.wj.ui.interfaces.RecyerViewItemListener
import kotlinx.android.synthetic.main.moviedetail_playlist_layout.view.*

/**
 * 影视详情
 * @author dchain
 * @version 1.0
 * @date 2019/9/16
 */
class MoviesDetailAdapter : BaseAdapter() {
    /**
     * 是否加载完成
     */
    var isFinish: Boolean = false
        set(value) {
            field = value
            if (itemCount > 1) {
                notifyItemChanged(itemCount - 1)
            }
        }
    var index =0
    //播放点击
    var playClick: RecyerViewItemListener? = null
    var playAdapter: SeriesAdapter? = null

    enum class MovieDetailTypes {
        PLAY_LIST, PUSH_TITLE, PUSHLIST, LOADMORE
    }

    var list = ArrayList<MovieDetailTypeMode>()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < itemCount - 1) {
            val mode = list[position]
            when (mode.type) {
                MovieDetailTypes.PLAY_LIST -> {
                    if (holder.itemView.recyclerView.adapter != null) return
                    val plays = mode.mode as ArrayList<String>
                    playAdapter = SeriesAdapter(plays)
                    holder.itemView.recyclerView.adapter = playAdapter
                    holder.itemView.recyclerView.layoutManager =
                        CustomGridManager(context, if (plays.size > 1) 4 else 2)
                    holder.itemView.recyclerView.addItemDecoration(RecyclerSpace(20))

                    playAdapter!!.choose=index
                    playAdapter!!.onItemClickListener = object : RecyerViewItemListener {
                        override fun click(view: View, position: Int) {
                            playClick?.click(view, position)
                        }
                    }
                    holder.itemView.sort.isVisible=false
                }
                MovieDetailTypes.PUSH_TITLE -> {
                }
                MovieDetailTypes.PUSHLIST -> {
                    val moviesMode = mode.mode as MoviesMode
                    CommControl.bindMoview(context!!, glide!!, holder, moviesMode)
                }
                else -> {
                }
            }
        } else {
            if (isFinish) {
                holder.itemView.findViewById<View>(R.id.foot_loading).visibility = View.GONE
                holder.itemView.findViewById<View>(R.id.foot_finsh).visibility = View.VISIBLE
            } else {
                holder.itemView.findViewById<View>(R.id.foot_loading).visibility = View.VISIBLE
                holder.itemView.findViewById<View>(R.id.foot_finsh).visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(
            when (MovieDetailTypes.values()[viewType]) {
                MovieDetailTypes.PLAY_LIST -> inflater!!.inflate(//播放集数
                    R.layout.moviedetail_playlist_layout,
                    parent,
                    false
                )
                MovieDetailTypes.PUSH_TITLE -> inflater!!.inflate(//推荐标题
                    R.layout.articlepushtitle_item_layout,
                    parent,
                    false
                )
                MovieDetailTypes.PUSHLIST -> inflater!!.inflate(
                    R.layout.movie_item_layout,
                    parent,
                    false
                )
                else -> {
                    val footView = inflater!!.inflate(
                        R.layout.commlib_footview_layout,
                        parent,
                        false
                    )
                    if (isFinish) {
                        footView.findViewById<View>(R.id.foot_loading).visibility = View.GONE
                        footView.findViewById<View>(R.id.foot_finsh).visibility = View.VISIBLE
                    } else {
                        footView.findViewById<View>(R.id.foot_loading).visibility = View.VISIBLE
                        footView.findViewById<View>(R.id.foot_finsh).visibility = View.GONE
                    }
                    footView
                }
            }
        )
    }

    override fun getItemCount(): Int {
        return if (list.size > 0) list.size + 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < list.size) list[position].type!!.ordinal else return MovieDetailTypes.LOADMORE.ordinal
    }
}