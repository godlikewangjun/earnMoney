package com.wj.makebai.ui.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abase.util.AbStrUtil
import com.wj.commonlib.data.mode.VipMovieItem
import com.wj.commonlib.ui.adapter.base.FootAdapter
import com.wj.makebai.R
import com.wj.makebai.utils.MbTools.load
import com.wj.makebai.utils.MbTools.loadCover
import com.wj.ui.base.viewhoder.CustomVhoder
import kotlinx.android.synthetic.main.vipmovie_item_layout.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * 影视的适配器
 * @author dchain
 * @version 1.0
 * @date 2019/9/10
 */
class VipMoviesAdapter() : FootAdapter(){
    var list= arrayListOf<VipMovieItem>()
    constructor(list:ArrayList<VipMovieItem>) : this() {
        this.list=list
    }


    @SuppressLint("SetTextI18n")
    override fun bindVH(holder: RecyclerView.ViewHolder, position: Int) {
        val moviesMode=list[position]
        holder.itemView.image.loadCover(glide!!,moviesMode.cover)
        holder.itemView.title.text=moviesMode.title
        holder.itemView.desc.text=moviesMode.descs
        holder.itemView.author.text=moviesMode.author
        holder.itemView.tv_tag.text=moviesMode.videoType+"   "
        try {
            holder.itemView.tv_tag.append(AbStrUtil.dateTimeFormat(Date(moviesMode.creationTime.toLong())))
        } catch (e: Exception) {
        }
    }

    override fun creatdVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CustomVhoder(inflater!!.inflate(R.layout.vipmovie_item_layout,parent,false))
    }

    override fun getItemCount(): Int {
        count=list.size
        return super.getItemCount()
    }

}