package com.wj.makebai.ui.adapter.home

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wj.commonlib.data.mode.VideoMode
import com.wj.makebai.R
import com.wj.makebai.data.mode.ArtTypeMode
import com.wj.ui.base.BaseAdapter
import com.wj.ui.base.viewhoder.CustomVhoder
import com.wj.ui.interfaces.RecyerViewItemListener
import kotlinx.android.synthetic.main.home_push_video_item_layout.view.*
import java.util.*


/**
 * 视频推荐列表
 * @author dchain
 * @version 1.0
 * @date 2019/8/26
 */
class HomePushVideoAdapter() : BaseAdapter() {
    var mList = arrayListOf<ArtTypeMode>()
    var likeListener: RecyerViewItemListener? = null
    init {
        setHasStableIds(false)
    }

    constructor(list: ArrayList<ArtTypeMode>) : this() {
        this.mList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        super.onCreateViewHolder(parent, viewType)
        return CustomVhoder(
            inflater!!.inflate(
                R.layout.home_push_video_item_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.setOnClickListener(null)
        val mode = mList[position].mode as VideoMode

        holder.itemView.video_title.text = mode.title
        holder.itemView.times.text=mode.time
        glide!!.load(mode.image).into(holder.itemView.video_play)

        holder.itemView.setOnClickListener { if(likeListener!=null)likeListener!!.click(it,position)}
    }

    override fun getItemCount(): Int {
        return mList.size
    }

}